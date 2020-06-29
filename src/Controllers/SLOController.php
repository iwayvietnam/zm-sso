<?php declare(strict_types=1);

namespace Application\Controllers;

use Application\SSO\SingleLogoutInterface;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

class SLOController {
	private $slo;

    public function __construct(SingleLogoutInterface $slo)
    {
    	$this->slo = $slo;
    }

    public function logout(Request $request, Response $response, array $args = []): Response
    {
        $this->slo->logout();
        return $response;
    }
}
