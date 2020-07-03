<?php declare(strict_types=1);

use Application\Zimbra\PreAuth;
use Application\Zimbra\SoapApi;

use Laminas\Db\Adapter\Adapter;
use Laminas\Db\Adapter\AdapterInterface;
use Mezzio\Session\SessionPersistenceInterface;
use Mezzio\Session\Ext\PhpSessionPersistence;
use Monolog\Handler\StreamHandler;
use Monolog\Handler\RotatingFileHandler;
use Monolog\Logger;
use Monolog\Processor\PsrLogMessageProcessor;
use Psr\Container\ContainerInterface;
use Psr\Log\LoggerInterface;
use Slim\App;

use Jumbojett\OpenIDConnectClient;
use OneLogin\Saml2\Auth;

return function (App $app) {
    $container = $app->getContainer();
    $settings = $container->get('settings');

    $container->add(SessionPersistenceInterface::class, PhpSessionPersistence::class);

    $container->add(AdapterInterface::class, Adapter::class)
        ->addArgument($container->get('settings')['db']);

    $container->add(LoggerInterface::class, static function (array $settings) {
        return new Logger($settings['name'], [
            new StreamHandler('php://stdout', $settings['level']),
            new RotatingFileHandler($settings['path'], 0, $settings['level']),
        ], [
            new PsrLogMessageProcessor,
        ]);
    })->addArgument($settings['logger']);

    $container->add(PreAuth::class)
        ->addArguments([
            $settings['zimbra']['serverUrl'],
            $settings['zimbra']['preauthKey'],
            $settings['zimbra']['domain'],
        ]);

    $container->add(SoapApi::class)
        ->addArgument($settings['zimbra']['adminSoapUrl']);

    $container->add(OpenIDConnectClient::class)
        ->addArguments([
            $settings['sso']['oidc']['providerUrl'],
            $settings['sso']['oidc']['clientId'],
            $settings['sso']['oidc']['clientSecret'],
        ])
        ->addMethodCall('addScope', [$settings['sso']['oidc']['scopes']]);
    $container->add(Auth::class)
        ->addArgument($settings['sso']['saml']);
};
