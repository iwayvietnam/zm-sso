<?php declare(strict_types=1);

namespace Application\SSO;

use Jumbojett\OpenIDConnectClient;
use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Fig\Http\Message\RequestMethodInterface;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;
use OneLogin\Saml2\Auth;

/**
 * SAML auth class
 */
class SAMLAuth extends BaseAuth
{
    private $saml;

    public function __construct(Adapter $adapter, Logger $logger, Auth $saml)
    {
        parent::__construct($adapter, $logger);
        $this->protocol = 'SAML';
        $this->saml = $saml;
    }

    public function login(Request $request): ?string
    {
        $redirectUrl = NULL;
        $session = $request->getAttribute('session');
        if ($request->getMethod() === RequestMethodInterface::METHOD_POST) {
            $requestID = $session->get('authNRequestID');
            $this->saml->processResponse($requestID);
            if ($this->saml->isAuthenticated()) {
                $sessionIndex = $this->saml->getSessionIndex();
                $nameId = $this->saml->getNameId();
                $attributes = $this->saml->getAttributes();
                $this->userName = $attributes[$this->uidMapping][0];
                $session->set('sessionIndex', $sessionIndex);
                $session->set('nameId', $nameId);
                $this->saveSsoLogin($sessionIndex, [
                    'attributes' => $attributes,
                    'nameId' => $nameId,
                    'sessionExpiration' => $this->saml->getSessionExpiration(),
                ]);
                $idpData = $this->saml->getSettings()->getIdPData();
                $this->logger->debug('saml login for {user_name} with {idp}', [
                    'user_name' => $this->userName,
                    '{idp}' => $idpData['entityId'],
                ]);
            }
        }
        if (!$this->isAuthenticated()) {
            $redirectUrl = $this->saml->login(NULL, [], FALSE, FALSE, TRUE);
            $session->set('authNRequestID', $this->saml->getLastRequestID());
            $idpData = $this->saml->getSettings()->getIdPData();
            $this->logger->debug('saml login to {idp} with {request_id}', [
                'idp' => $idpData['entityId'],
                'request_id' => $this->saml->getLastRequestID(),
            ]);
        }
        return $redirectUrl;
    }

    public function logout(Request $request): ?string
    {
        $nameId = $request->getAttribute('session')->get('nameId');
        $sessionIndex = $request->getAttribute('session')->get('sessionIndex');
        $idpData = $this->saml->getSettings()->getIdPData();
        $this->logger->debug('saml logout for {user_name} with {idp}', [
            'user_name' => $this->getUserName($sessionIndex),
            'idp' => $idpData['entityId'],
        ]);
        $this->saveSsoLogout($sessionIndex);
        return $this->saml->logout(NULL, [], $nameId, $sessionIndex, TRUE);
    }

    public function metadata(): ?string
    {
        $settings = $this->saml->getSettings();
        $this->logger->debug('saml metadata for service provider {sp}', [
            'sp' => $settings->getSPData()['entityId'],
        ]);
        $metadata = $settings->getSPMetadata();
        $errors = $settings->validateMetadata($metadata);
        return empty($errors) ? $metadata : NULL;
    }

    public function isAuthenticated(): bool
    {
        return $this->saml->isAuthenticated();
    }
}
