<?php declare(strict_types=1);

use Application\Controllers\{HomeController, CASController, OIDCController, SAMLController};
use Slim\App;
use Slim\Routing\RouteCollectorProxy;

return function (App $app) {
    $settings = $app->getContainer()->get('settings');

    $app->get('/', HomeController::class . ':index');

    if (!empty($settings['sso']['saml'])) {
        $app->group('/saml', function (RouteCollectorProxy $group) {
            $group->get('/metadata', SAMLController::class . ':metadata');
            $group->get('/login', SAMLController::class . ':login');
            $group->get('/logout', SAMLController::class . ':logout');
            $group->post('/acs', SAMLController::class . ':assertionConsumerService');

            $group->get('/slo', SAMLController::class . ':singleLogout');
            $group->post('/slo', SAMLController::class . ':singleLogout');
        });
    }

    if (!empty($settings['sso']['cas'])) {
        $app->group('/cas', function (RouteCollectorProxy $group) {
            $group->get('/login', CASController::class . ':login');
            $group->get('/logout', CASController::class . ':logout');
            $group->post('/slo', CASController::class . ':singleLogout');
        });
    }

    if (!empty($settings['sso']['oidc'])) {
        $app->group('/oidc', function (RouteCollectorProxy $group) {
            $group->get('/login', OIDCController::class . ':login');
            $group->get('/logout', OIDCController::class . ':logout');

            $group->get('/slo', OIDCController::class . ':singleLogout');
            $group->post('/bcLogout', OIDCController::class . ':backChannelLogout');
        });
    }

};
