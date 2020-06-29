<?php declare(strict_types=1);

use Application\Controllers\HomeController;
use Application\Controllers\SSOController;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Slim\App;

use OneLogin\Saml2\Auth;

return function (App $app) {
    $app->get('/', HomeController::class . ':index');
    $app->get('/login', SSOController::class . ':login');
    $app->post('/login', SSOController::class . ':login');
    $app->get('/logout', SSOController::class . ':logout');
    $app->get('/metadata', SSOController::class . ':metadata');

    $app->get('/slo', SLOController::class . ':logout');
    $app->post('/slo', SLOController::class . ':logout');
};
