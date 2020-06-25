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
        $this->saml = new Auth($settings['sso']['saml']);
    }

    public function login(Request $request): ?string
    {
        $redirectUrl = NULL;
        $session = $request->getAttribute('session');
        if ($request->getMethod() === RequestMethodInterface::METHOD_POST) {
            $requestID = $session->get('AuthNRequestID');
            $this->saml->processResponse($requestID);
            if ($this->saml->isAuthenticated()) {
                $sessionIndex = $this->saml->getSessionIndex();
                $nameId = $this->saml->getNameId();
                $attributes = $this->saml->getAttributes();
                $this->userName = $attributes[$this->settings['sso']['saml']['user_name_attr']][0];
                $session->set('sessionIndex', $sessionIndex);
                $session->set('nameId', $nameId);
                $this->saveSsoLogin($sessionIndex, [
                    'attributes' => $attributes,
                    'nameId' => $nameId,
                    'sessionExpiration' => $this->saml->getSessionExpiration(),
                ]);
                $this->logger->debug('saml login for %user_name% with %idp%', [
                    'user_name' => $this->userName,
                    'idp' => $this->settings['sso']['saml']['idp']['entityId'],
                ]);
            }
        }
        if (!$this->isAuthenticated()) {
            // $this->saml->login();
            $redirectUrl = $this->saml->login(NULL, [], FALSE, FALSE, TRUE);
            $session->set('AuthNRequestID', $this->saml->getLastRequestID());
            $this->logger->debug('saml login to %idp% with %request_id%', [
                'idp' => $this->settings['sso']['saml']['idp']['entityId'],
                'request_id' => $this->saml->getLastRequestID(),
            ]);
        }
        return $redirectUrl;
    }

    public function logout(Request $request): ?string
    {
        $nameId = $request->getAttribute('session')->get('nameId');
        $sessionIndex = $request->getAttribute('session')->get('sessionIndex');
        return $this->saml->logout(NULL, [], $nameId, $sessionIndex, TRUE);
    }

    public function isAuthenticated(): bool
    {
        return $this->saml->isAuthenticated();
    }
}
