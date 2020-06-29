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

    public function __construct(Adapter $adapter, Logger $logger, array $settings = [])
    {
        parent::__construct($adapter, $logger, $settings);
        $this->protocol = 'SAML';
        $this->saml = new Auth($settings);
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
                $this->userName = $attributes[$this->settings['uid_mapping']][0];
                $session->set('sessionIndex', $sessionIndex);
                $session->set('nameId', $nameId);
                $this->saveSsoLogin($sessionIndex, [
                    'attributes' => $attributes,
                    'nameId' => $nameId,
                    'sessionExpiration' => $this->saml->getSessionExpiration(),
                ]);
                $this->logger->debug(strtr('saml login for %user_name% with %idp%', [
                    '%user_name%' => $this->userName,
                    '%idp%' => $this->settings['idp']['entityId'],
                ]));
            }
        }
        if (!$this->isAuthenticated()) {
            $redirectUrl = $this->saml->login(NULL, [], FALSE, FALSE, TRUE);
            $session->set('authNRequestID', $this->saml->getLastRequestID());
            $this->logger->debug(strtr('saml login to %idp% with %request_id%', [
                '%idp%' => $this->settings['idp']['entityId'],
                '%request_id%' => $this->saml->getLastRequestID(),
            ]));
        }
        return $redirectUrl;
    }

    public function logout(Request $request): ?string
    {
        $nameId = $request->getAttribute('session')->get('nameId');
        $sessionIndex = $request->getAttribute('session')->get('sessionIndex');
        $this->logger->debug(strtr('saml logout for %user_name% with %idp%', [
            '%user_name%' => $this->getUserName($sessionIndex),
            '%idp%' => $this->settings['idp']['entityId'],
        ]));
        $this->saveSsoLogout($sessionIndex);
        return $this->saml->logout(NULL, [], $nameId, $sessionIndex, TRUE);
    }

    public function metadata(): ?string
    {
        $settings = $this->saml->getSettings();
        $metadata = $settings->getSPMetadata();
        $errors = $settings->validateMetadata($metadata);
        return empty($errors) ? $metadata : NULL;
    }

    public function isAuthenticated(): bool
    {
        return $this->saml->isAuthenticated();
    }
}
