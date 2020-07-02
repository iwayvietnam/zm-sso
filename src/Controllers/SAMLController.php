<?php declare(strict_types=1);

namespace Application\Controllers;

use Application\SSO\AuthInterface;
use Application\Zimbra\PreAuth;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

class SAMLController {
	private $auth;
    private $preAuth;

    public function __construct(AuthInterface $auth, PreAuth $preAuth)
    {
    	$this->auth = $auth;
        $this->preAuth = $preAuth;
    }

    public function login(Request $request, Response $response, array $args = []): Response
    {
        $redirectUrl = $this->auth->login($request);
        if ($this->auth->isAuthenticated()) {
            $redirectUrl = $this->preAuth->generatePreauthURL($this->auth->getUserName());
        }
        if (!empty($redirectUrl)) {
            $response = $response->withHeader('Location', $redirectUrl)->withStatus(302);
        }
        return $response;
    }

    public function logout(Request $request, Response $response, array $args = []): Response
    {
        $redirectUrl = $this->auth->logout($request);
        if (!empty($redirectUrl)) {
            $response = $response->withHeader('Location', $redirectUrl)->withStatus(302);
        }
        return $response;
    }

    public function metadata(Request $request, Response $response, array $args = []): Response
    {
        $metadata = $this->auth->metadata();
        if (!empty($metadata)) {
            if (simplexml_load_string($metadata)) {
                $response = $response->withHeader('Content-Type', 'application/xml; charset=utf-8');
            }
            else {
                json_decode($metadata);
                if (json_last_error() === JSON_ERROR_NONE) {
                    $response = $response->withHeader('Content-Type', 'application/json; charset=utf-8');
                }
            }
            $response->getBody()->write($metadata);
        }
        return $response;
    }
}
