<?php declare(strict_types=1);

namespace Application\SSO;

use Jumbojett\OpenIDConnectClient;
use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Fig\Http\Message\RequestMethodInterface;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;
use OneLogin\Saml2\Auth;
use Slim\Routing\RouteContext;

/**
 * SAML authentication class
 */
class SAMLAuthentication extends BaseAuthentication implements SAMLAuthenticationInterface
{
    private $saml;

    public function __construct(Adapter $adapter, Logger $logger, Auth $saml)
    {
        parent::__construct($adapter, $logger);
        $this->protocol = 'SAML';
        $this->saml = $saml;
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

    public function login(Request $request): ?string
    {
        $session = $request->getAttribute('session');
        $redirectUrl = $this->saml->login(NULL, [], FALSE, FALSE, TRUE);
        $session->set('authNRequestID', $this->saml->getLastRequestID());
        $idpData = $this->saml->getSettings()->getIdPData();
        $this->logger->debug('saml login to {idp} with {request_id}', [
            'idp' => $idpData['entityId'],
            'request_id' => $this->saml->getLastRequestID(),
        ]);
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

    public function singleLogout(Request $request): ?string
    {
        $parsedBody = $request->getParsedBody();
        if(!empty($parsedBody['SAMLRequest'])) {
            $_GET['SAMLRequest'] = $parsedBody['SAMLRequest'];
        }
        $targetUrl = $this->saml->processSLO(TRUE, NULL, FALSE, NULL, TRUE);
        if(!empty($targetUrl) && !$this->saml->getLastErrorReason()){
            $sessionIndex = $request->getAttribute('session')->get('sessionIndex');
            if (!empty($sessionIndex)) {
                $this->doLogout($sessionIndex);
            }
        }
        if (empty($targetUrl)) {
            $targetUrl = RouteContext::fromRequest($request)->getBasePath();
        }
        return $targetUrl;
    }

    public function assertionConsumerService(Request $request): ?string
    {
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
        return RouteContext::fromRequest($request)->getBasePath();
    }

    public function isAuthenticated(): bool
    {
        return $this->saml->isAuthenticated();
    }
}
