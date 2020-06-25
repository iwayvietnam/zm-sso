<?php declare(strict_types=1);

namespace Application\Controllers;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

class HomeController {
    public function index(Request $request, Response $response, array $args = []): Response
    {
        $response->getBody()->write('Zimbra single sign on application');
        return $response;
    }
}
