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

        $version = $settings['sso']['cas']['version'];
        \phpCAS::client(
            $version,
            $settings['sso']['cas']['server_host'],
            $settings['sso']['cas']['server_port'],
            $settings['sso']['cas']['context']
        );

        $protocols = \phpCAS::getSupportedProtocols();
        $this->protocol = !empty($protocols[$version]) ? $protocols[$version] : 'CAS';

        \phpCAS::setPostAuthenticateCallback(function ($ticket)) {
            $this->userName = \phpCAS::getUser();
            $this->logger->debug('cas login for %user_name% with %server_host%', [
                'user_name' => $this->userName,
                'server_host' => $this->settings['sso']['cas']['server_host'],
            ]);
            $this->saveSsoLogin($ticket);
        });
    }

    public function login(Request $request): ?string
    {
        if (!$this->isAuthenticated()) {
            \phpCAS::forceAuthentication();
        }
    }

    public function logout(Request $request): ?string
    {
        $this->logger->debug('cas logout for %user_name% with %server_host%', [
            'user_name' => $this->userName,
            'server_host' => $this->settings['sso']['cas']['server_host'],
        ]);
        \phpCAS::logout();
    }

    public function isAuthenticated(): bool
    {
        return \phpCAS::isAuthenticated();
    }
}
