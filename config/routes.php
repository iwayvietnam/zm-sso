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

    $app->post('/acs', function (Request $request, Response $response) {
        $requestID = $request->getAttribute('session')->get('AuthNRequestID');
        $auth = new Auth($this->get('settings')['sso']['saml']);
        $auth->processResponse();
        // $errors = $auth->getLastErrorException();
        // echo(base64_decode($_POST['SAMLResponse']));exit;
        var_dump($_SESSION);exit;
        var_dump($auth->getAttributes());exit;
        return $response;
    });

    $app->get('/slo', function (Request $request, Response $response) {
        return $response;
    });
    $app->post('/slo', function (Request $request, Response $response) {
        return $response;
    });
};
