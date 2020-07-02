<?php declare(strict_types=1);

namespace Application\Controllers;

use Application\Zimbra\PreAuth;
use Application\Zimbra\SoapApi;
use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;
use OneLogin\Saml2\Auth;
use Slim\Exception\HttpInternalServerErrorException;
use Slim\Routing\RouteContext;

class SAMLController extends BaseController {
    private $auth;
    private $preAuth;

    public function __construct(Adapter $adapter, Logger $logger, SoapApi $api, Auth $auth, PreAuth $preAuth)
    {
        parent::__construct($adapter, $logger, $api);
        $this->auth = $auth;
        $this->preAuth = $preAuth;
        $this->protocol = 'SAML';
    }

    public function metadata(Request $request, Response $response, array $args = []): Response
    {
        $settings = $this->auth->getSettings();
        $this->logger->debug('saml metadata for service provider {sp}', [
            'sp' => $settings->getSPData()['entityId'],
        ]);
        $metadata = $settings->getSPMetadata();
        $errors = $settings->validateMetadata($metadata);
        if (!empty($errors)) {
            foreach($errors as $error) {
                $this->logger->error($error);
            }
            throw new HttpInternalServerErrorException($request, 'invalid service provider metadata');
        }
        else {
            $response = $response->withHeader('Content-Type', 'application/xml; charset=utf-8');
            $response->getBody()->write($metadata);
        }
        return $response;
    }

    public function login(Request $request, Response $response, array $args = []): Response
    {
        $session = $request->getAttribute('session');
        $redirectUrl = $this->auth->login(NULL, [], FALSE, FALSE, TRUE);
        $session->set('saml.authNRequestId', $this->auth->getLastRequestID());
        $idpData = $this->auth->getSettings()->getIdPData();
        $this->logger->debug('saml login to {idp} with {request_id}', [
            'idp' => $idpData['entityId'],
            'request_id' => $this->auth->getLastRequestID(),
        ]);

        if (!empty($redirectUrl)) {
            $response = $response->withHeader('Location', $redirectUrl)->withStatus(302);
        }
        return $response;
    }

    public function logout(Request $request, Response $response, array $args = []): Response
    {
        $session = $request->getAttribute('session');
        $nameId = $session->get('saml.nameId');
        $sessionIndex = $session->get('saml.sessionIndex');
        $idpData = $this->auth->getSettings()->getIdPData();
        $this->logger->debug('saml logout for {user_name} with {idp}', [
            'user_name' => $this->getUserName($sessionIndex),
            'idp' => $idpData['entityId'],
        ]);
        $this->saveSsoLogout($sessionIndex);
        $redirectUrl = $this->auth->logout(NULL, [], $nameId, $sessionIndex, TRUE);
        $session->set('saml.logoutRequestId', $this->auth->getLastRequestID());

        if (!empty($redirectUrl)) {
            $response = $response->withHeader('Location', $redirectUrl)->withStatus(302);
        }
        return $response;
    }

    public function assertionConsumerService(Request $request, Response $response, array $args = []): Response
    {
        $settings = $request->getAttribute('settings');
        $session = $request->getAttribute('session');
        $requestId = $session->get('saml.authNRequestId');
        $this->auth->processResponse($requestId);
        if ($this->auth->isAuthenticated()) {
            $sessionIndex = $this->auth->getSessionIndex();
            $attributes = $this->auth->getAttributes();
            $userName = $attributes[$settings['sso']['uidMapping']][0];
            $session->set('saml.sessionIndex', $sessionIndex);
            $session->set('saml.nameId', $this->auth->getNameId());
            $this->saveSsoLogin($sessionIndex, $userName, $attributes);
            $idpData = $this->auth->getSettings()->getIdPData();
            $this->logger->debug('saml user attributes send by {idp}: {attributes}', [
                'idp' => $idpData['entityId'],
                'attributes' => json_encode($attributes),
            ]);
            $redirectUrl = $this->preAuth->generatePreauthURL($userName);
        }
        else {
            $idpData = $this->auth->getSettings()->getIdPData();
            $this->logger->info('saml authenticate to {idp} failed', [
                'idp' => $idpData['entityId'],
            ]);
            $errors = $this->auth->getErrors();
            if (!empty($errors)) {
                foreach($errors as $error) {
                    $this->logger->error($error);
                }
            }
            $redirectUrl = RouteContext::fromRequest($request)->getBasePath();
        }

        if (!empty($redirectUrl)) {
            $response = $response->withHeader('Location', $redirectUrl)->withStatus(302);
        }
        return $response;
    }

    public function singleLogout(Request $request, Response $response, array $args = []): Response
    {
        $parsedBody = $request->getParsedBody();
        if(!empty($parsedBody['SAMLRequest'])) {
            $_GET['SAMLRequest'] = $parsedBody['SAMLRequest'];
        }
        $session = $request->getAttribute('session');
        $requestId = $session->get('saml.logoutRequestId');

        $redirectUrl = $this->auth->processSLO(TRUE, $requestId, FALSE, NULL, TRUE);
        if(!empty($redirectUrl) && !$this->auth->getLastErrorReason()){
            $sessionIndex = $session->get('saml.sessionIndex');
            if (!empty($sessionIndex)) {
                $this->zimbraLogout($sessionIndex);
            }
        }
        else {
            $idpData = $this->auth->getSettings()->getIdPData();
            $this->logger->info('saml single logout from {idp} failed', [
                'idp' => $idpData['entityId'],
            ]);
            $errors = $this->auth->getErrors();
            if (!empty($errors)) {
                foreach($errors as $error) {
                    $this->logger->error($error);
                }
            }
        }


        if (empty($redirectUrl)) {
            $redirectUrl = RouteContext::fromRequest($request)->getBasePath();
        }
        if (!empty($redirectUrl)) {
            $response = $response->withHeader('Location', $redirectUrl)->withStatus(302);
        }
        return $response;
    }
}
