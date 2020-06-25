<?php
declare(strict_types=1);

use Slim\App;
use Slim\Middleware\ContentLengthMiddleware;
use Mezzio\Session\SessionMiddleware;
use Mezzio\Session\SessionPersistenceInterface;

return function (App $app) {
    $container = $app->getContainer();

    // Add session middleware
    $app->add(new SessionMiddleware($container->get(SessionPersistenceInterface::class)));

    // Add error middleware
    $app->addErrorMiddleware($container->get('settings')['displayErrorDetails'], FALSE, FALSE);

    // Add body parsing middleware
    $app->addBodyParsingMiddleware();

    // Add content length middleware
    $app->add(new ContentLengthMiddleware);

    // Add routing middleware
    $app->addRoutingMiddleware();
};
