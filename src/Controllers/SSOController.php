<?php declare(strict_types=1);

namespace Application\Controllers;

use Application\SSO\AuthInterface;
use Application\Zimbra\PreAuth;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

class SSOController {
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
        return $response->withHeader('Location', $redirectUrl)->withStatus(302);
    }

    public function logout(Request $request, Response $response, array $args = []): Response
    {
        $redirectUrl = $this->auth->logout($request);
        return $response->withHeader('Location', $redirectUrl)->withStatus(302);
    }
}
