<?php declare(strict_types=1);

namespace Application\SSO;

use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;

/**
 * CAS auth class
 */
class CASAuth extends BaseAuth
{
    public function __construct(Adapter $adapter, Logger $logger, array $settings = [])
    {
        parent::__construct($adapter, $logger, $settings);

        $version = $settings['version'];
        \phpCAS::client(
            $version,
            $settings['server_host'],
            $settings['server_port'],
            $settings['context']
        );

        $protocols = \phpCAS::getSupportedProtocols();
        $this->protocol = !empty($protocols[$version]) ? $protocols[$version] : 'CAS';
    }

    public function login(Request $request): ?string
    {
        $redirectUrl = NULL;
        if (!$this->isAuthenticated()) {
            $session = $request->getAttribute('session');
            \phpCAS::setPostAuthenticateCallback(function ($ticket) use ($session) {
                $this->userName = \phpCAS::getUser();
                $session->set('casTicket', $ticket);
                $this->saveSsoLogin($ticket);
                $this->logger->debug(strtr('cas login for %user_name% with %server_host%', [
                    '%user_name%' => $this->userName,
                    '%server_host%' => $this->settings['server_host'],
                ]));
            });
            \phpCAS::forceAuthentication();
        }
        return $redirectUrl;
    }

    public function logout(Request $request): ?string
    {
        $redirectUrl = NULL;
        if ($this->isAuthenticated()) {
            $session = $request->getAttribute('session');
            $this->logger->debug(strtr('cas logout for %user_name% with %server_host%', [
                '%user_name%' => \phpCAS::getUser(),
                '%server_host%' => $this->settings['server_host'],
            ]));
            $this->saveSsoLogout($session->get('casTicket'));
            \phpCAS::logout();
        }
        return $redirectUrl;
    }

    public function metadata(): ?string
    {
        return NULL;
    }

    public function isAuthenticated(): bool
    {
        return \phpCAS::isAuthenticated();
    }
}
