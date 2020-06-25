<?php

declare(strict_types=1);

require __DIR__ . '/../vendor/autoload.php';

use League\Container\Container;
use League\Container\ReflectionContainer;
use Psr\Container\ContainerInterface;
use Slim\Factory\AppFactory;
use Slim\App;

(function () {
    $container = (new Container)->delegate(new ReflectionContainer)->defaultToShared();

    $container->add(App::class, static function (ContainerInterface $container) {
        return AppFactory::createFromContainer($container);
    })->addArgument($container);

    $app = $container->get(App::class);

    // Set up settings
    (require __DIR__ . '/../config/settings.php')($app);

    // Set up dependencies
    (require __DIR__ . '/../config/dependencies.php')($app);

    // Register middleware
    (require __DIR__ . '/../config/middleware.php')($app);

    // Register routes
    (require __DIR__ . '/../config/routes.php')($app);

    $app->run();
})();
