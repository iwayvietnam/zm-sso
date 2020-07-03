<?php declare(strict_types=1);

namespace Application\Controllers;

use Application\Zimbra\{PreAuth, SoapApi};
use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;
use Slim\Exception\HttpInternalServerErrorException;
use Slim\Routing\RouteContext;

class CASController {
    private $preAuth;

    public function __construct(Adapter $adapter, Logger $logger, SoapApi $api, PreAuth $preAuth)
    {
        parent::__construct($adapter, $logger, $api);
        $this->preAuth = $preAuth;
        $this->protocol = 'CAS';
    }

    public function login(Request $request, Response $response, array $args = []): Response
    {
        $this->initializeCAS($request);
        $redirectUrl = RouteContext::fromRequest($request)->getBasePath();
        if (!$this->isAuthenticated()) {
            $session = $request->getAttribute('session');
            \phpCAS::setPostAuthenticateCallback(function ($ticket, $session) {
                $this->userName = \phpCAS::getUser();
                $session->set('cas.logoutTicket', $ticket);
                $this->saveSsoLogin($ticket);
                $this->logger->debug('cas login for {user_name} with {server_host}', [
                    'user_name' => $this->userName,
                    'server_host' => $this->settings['serverHost'],
                ]);
            },[
                $session,
            ]);
            \phpCAS::forceAuthentication();
        }
        else {
            $redirectUrl = $this->preAuth->generatePreauthURL($this->auth->getUserName());
        }

        return $response->withHeader('Location', $redirectUrl)->withStatus(302);
    }

    public function logout(Request $request, Response $response, array $args = []): Response
    {
        $this->initializeCAS($request);
        $redirectUrl = RouteContext::fromRequest($request)->getBasePath();

        if ($this->isAuthenticated()) {
            $session = $request->getAttribute('session');
            $this->logger->debug('cas logout for {user_name} with {server_host}', [
                'user_name' => \phpCAS::getUser(),
                'server_host' => $this->settings['serverHost'],
            ]);
            $this->saveSsoLogout($session->get('cas.logoutTicket'));
            \phpCAS::logout();
        }

        return $response->withHeader('Location', $redirectUrl)->withStatus(302);
    }

    public function singleLogout(Request $request, Response $response, array $args = []): Response
    {
        $this->initializeCAS($request);
        $redirectUrl = RouteContext::fromRequest($request)->getBasePath();

        \phpCAS::setSingleSignoutCallback(function ($ticket, $settings) {
            $this->api->authByName($settings['adminUser'], $settings['adminPassword']);
            $this->zimbraLogout($ticket);
        },[
            $request->getAttribute('settings')['zimbra'],
        ]);
        \phpCAS::handleLogoutRequests();

        return $response->withHeader('Location', $redirectUrl)->withStatus(302);
    }

    private function isAuthenticated(): bool
    {
        return \phpCAS::isAuthenticated();
    }

    private function initializeCAS(Request $request)
    {
        $settings = $request->getAttribute('settings')['sso']['cas'];
        $version = $settings['version'];
        \phpCAS::client(
            $version,
            $settings['serverHost'],
            $settings['serverPort'],
            $settings['context']
        );

        $protocols = \phpCAS::getSupportedProtocols();
        $this->protocol = !empty($protocols[$version]) ? $protocols[$version] : 'CAS';
    }
}
