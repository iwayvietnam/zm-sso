<?php
declare(strict_types=1);

use Slim\App;
use Slim\Middleware\ContentLengthMiddleware;
use Mezzio\Session\SessionMiddlewareFactory;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Http\Server\RequestHandlerInterface as RequestHandler;

return function (App $app) {
    $container = $app->getContainer();

    // Add session middleware
    $app->add((new SessionMiddlewareFactory())($container));

    // Add body parsing middleware
    $app->addBodyParsingMiddleware();

    // Add content length middleware
    $app->add(new ContentLengthMiddleware);

    // Add routing middleware
    $app->addRoutingMiddleware();

    // Add error middleware
    $app->addErrorMiddleware($container->get('settings')['displayErrorDetails'], FALSE, FALSE);

    // Add settings middleware
    $app->add(function (Request $request, RequestHandler $handler) {
        $request = $request->withAttribute('settings', $this->get('settings'));
        return $handler->handle($request);
    });
};
