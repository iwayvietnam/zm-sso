<?php declare(strict_types=1);

use Application\Controllers\{HomeController, CASController, OIDCController, SAMLController};
use Slim\App;
use Slim\Routing\RouteCollectorProxy;

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
};
