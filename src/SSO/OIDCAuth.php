<?php declare(strict_types=1);

namespace Application\SSO;

use Jumbojett\OpenIDConnectClient;
use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;

/**
 * OIDC auth class
 */
class OIDCAuth extends BaseAuth
{
    private $oidcClient;
    private $isAuthenticated = FALSE;
    private $accessToken = NULL;
    private $idToken = NULL;

    public function __construct(Adapter $adapter, Logger $logger, array $settings = [])
    {
        parent::__construct($adapter, $logger, $settings);
        $this->protocol = 'OIDC';

        $this->oidcClient = new OpenIDConnectClient(
            $settings['sso']['oidc']['provider_url'],
            $settings['sso']['oidc']['client_id'],
            $settings['sso']['oidc']['client_secret']
        );
        $this->oidcClient->setVerifyHost(false);
        $this->oidcClient->setVerifyPeer(false);
        $this->oidcClient->addScope($settings['sso']['oidc']['scopes']);
    }

    public function login(Request $request): ?string
    {
        if (!$this->isAuthenticated()) {
            $this->isAuthenticated = $this->oidcClient->authenticate();
            $this->userName = $this->oidcClient->requestUserInfo('email');
            $this->logger->debug('oidc login for %user_name% with %provider_url%', [
                'user_name' => $this->userName,
                'provider_url' => $this->settings['sso']['oidc']['provider_url'],
            ]);

            $this->accessToken = $this->oidcClient->getAccessToken();
            $this->idToken = $this->oidcClient->getIdToken();
            $this->saveSsoLogin($this->accessToken);
        }
    }

    public function logout(Request $request): ?string
    {
        $this->logger->debug('oidc logout for %user_name% with %provider_url%', [
            'user_name' => $this->userName,
            'provider_url' => $this->settings['sso']['oidc']['provider_url'],
        ]);
        $this->oidcClient->signOut($this->accessToken, NULL);
    }

    public function isAuthenticated(): bool
    {
        return $this->isAuthenticated;
    }
}
