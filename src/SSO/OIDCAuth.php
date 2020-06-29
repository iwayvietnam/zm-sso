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
    private $client;
    private $isAuthenticated = FALSE;

    public function __construct(Adapter $adapter, Logger $logger, OpenIDConnectClient $client)
    {
        parent::__construct($adapter, $logger);
        $this->protocol = 'OIDC';

        $this->client = new $client;
        $this->client->setVerifyHost(FALSE);
        $this->client->setVerifyPeer(FALSE);
    }

    public function login(Request $request): ?string
    {
        if (!$this->isAuthenticated()) {
            $session = $request->getAttribute('session');
            $this->isAuthenticated = $this->client->authenticate();
            $this->userName = $this->client->requestUserInfo($this->uidMapping);

            $accessToken = $this->client->getAccessToken();
            $idToken = $this->client->getIdToken();
            $session->set('accessToken', $accessToken);
            $session->set('idToken', $idToken);
            $this->saveSsoLogin($accessToken);

            $this->logger->debug(strtr('oidc login for %userName% with %providerURL%', [
                '%userName%' => $this->userName,
                '%providerURL%' => $this->client->getProviderURL(),
            ]));
        }
    }

    public function logout(Request $request): ?string
    {
        $this->logger->debug(strtr('oidc logout for %userName% with %providerURL%', [
            '%userName%' => $this->userName,
            '%providerURL%' => $this->client->getProviderURL(),
        ]));
        $session = $request->getAttribute('session');
        $accessToken = $session->get('accessToken');
        $this->saveSsoLogout($accessToken);
        $this->client->signOut($accessToken, NULL);
    }

    public function metadata(): ?string
    {
        return NULL;
    }

    public function isAuthenticated(): bool
    {
        return $this->isAuthenticated;
    }
}
