<?php declare(strict_types=1);

use Monolog\Logger;
use Slim\App;

return function (App $app) {
    $container = $app->getContainer();
    $container->add('settings', include 'config.php');
};
