<?php declare(strict_types=1);

use Application\Controllers\HomeController;
use Application\Controllers\CASController;
use Application\Controllers\OIDCController;
use Application\Controllers\SAMLController;

use Application\Controllers\SSOController;
use Application\Controllers\SLOController;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Slim\App;
use Slim\Routing\RouteCollectorProxy;

use OneLogin\Saml2\Auth;

return function (App $app) {
    $app->get('/', HomeController::class . ':index');

    $app->group('/saml', function (RouteCollectorProxy $group) {
        $group->get('/metadata', SAMLController::class . ':metadata');
        $group->get('/login', SAMLController::class . ':login');
        $group->get('/logout', SAMLController::class . ':logout');
        $group->post('/acs', SAMLController::class . ':assertionConsumerService');

        $group->get('/slo', SAMLController::class . ':singleLogout');
        $group->post('/slo', SAMLController::class . ':singleLogout');
    });

    $app->group('/cas', function (RouteCollectorProxy $group) {
        $group->get('/login', CASController::class . ':login');
        $group->get('/logout', CASController::class . ':logout');
        $group->post('/slo', CASController::class . ':singleLogout');
    });

    $app->group('/oidc', function (RouteCollectorProxy $group) {
        $group->get('/login', OIDCController::class . ':login');
        $group->get('/logout', OIDCController::class . ':logout');

        $group->get('/slo', OIDCController::class . ':singleLogout');
        $group->post('/slo', OIDCController::class . ':singleLogout');
    });

    $app->get('/login', SSOController::class . ':login');
    $app->post('/login', SSOController::class . ':login');
    $app->get('/logout', SSOController::class . ':logout');
    $app->get('/metadata', SSOController::class . ':metadata');

    $app->get('/slo', SLOController::class . ':logout');
    $app->post('/slo', SLOController::class . ':logout');
};
