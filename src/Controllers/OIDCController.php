<?php declare(strict_types=1);

namespace Application\Controllers;

use Application\Zimbra\PreAuth;
use Application\Zimbra\SoapApi;
use Jumbojett\OpenIDConnectClient;
use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;
use Slim\Exception\HttpInternalServerErrorException;
use Slim\Routing\RouteContext;

class OIDCController {
	private $client;
    private $preAuth;

    public function __construct(Adapter $adapter, Logger $logger, SoapApi $api, OpenIDConnectClient $client, PreAuth $preAuth)
    {
        parent::__construct($adapter, $logger, $api);
        $this->client = $client;
        $this->client->setVerifyHost(FALSE);
        $this->client->setVerifyPeer(FALSE);
        $this->protocol = 'OIDC';
    }

    public function login(Request $request, Response $response, array $args = []): Response
    {
        $settings = $request->getAttribute('settings');
        $session = $request->getAttribute('session');

        $isAuthenticated = $this->client->authenticate();
        $userName = $this->client->requestUserInfo($settings['sso']['uidMapping']);

        $accessToken = $this->client->getAccessToken();
        $idToken = $this->client->getIdToken();
        $tokenResponse = $this->client->getTokenResponse();
        $session->set('oidc.accessToken', $accessToken);
        $session->set('oidc.idToken', $idToken);
        $session->set('oidc.userName', $userName);
        $this->saveSsoLogin($idToken, $userName,[
            'tokenResponse' => $tokenResponse,
        ]);

        $this->logger->debug('oidc login for {user_name} with {provider_url}', [
            'user_name' => $userName,
            'provider_url' => $this->client->getProviderURL(),
        ]);

        if ($isAuthenticated) {
            $redirectUrl = $this->preAuth->generatePreauthURL($this->auth->getUserName());
        }
        else {
            $this->logger->info('oidc authenticate to {provider_url} failed', [
                'provider_url' => $this->client->getProviderURL(),
            ]);
            $redirectUrl = RouteContext::fromRequest($request)->getBasePath();
        }
        return $response->withHeader('Location', $redirectUrl)->withStatus(302);
    }

    public function logout(Request $request, Response $response, array $args = []): Response
    {
        $session = $request->getAttribute('session');
        $idToken = $session->get('oidc.idToken');
        $userName = $session->get('oidc.userName');
        $this->saveSsoLogout($idToken);
        $this->logger->debug('oidc logout for {user_name} with {provider_url}', [
            'user_name' => $userName,
            'provider_url' => $this->client->getProviderURL(),
        ]);
        $this->client->signOut($idToken, NULL);

        $redirectUrl = RouteContext::fromRequest($request)->getBasePath();
        return $response->withHeader('Location', $redirectUrl)->withStatus(302);
    }

    public function singleLogout(Request $request, Response $response, array $args = []): Response
    {
        return $response;
    }
}
