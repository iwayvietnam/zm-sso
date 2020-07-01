<?php declare(strict_types=1);

use Application\SSO\AuthInterface;
use Application\SSO\CASAuth;
use Application\SSO\OIDCAuth;
use Application\SSO\SAMLAuth;

use Application\SSO\SingleLogoutInterface;
use Application\SSO\CASSingleLogout;
use Application\SSO\OIDCSingleLogout;
use Application\SSO\SAMLSingleLogout;

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
    })->addArgument($container->get('settings')['logger']);

    $zimbraSettings = $container->get('settings')['zimbra'];
    $container->add(PreAuth::class)
        ->addArguments([
            $zimbraSettings['serverUrl'],
            $zimbraSettings['preauthKey'],
            $zimbraSettings['domain'],
        ]);

    $container->add(SoapApi::class)
        ->addArgument($zimbraSettings['adminSoapUrl'])
        ->addMethodCall('addScope', [
            $zimbraSettings['adminUser'],
            $zimbraSettings['adminPassword'],
        ]);

    $protocol = $container->get('settings')['sso']['protocol'];
    switch ($protocol) {
        case 'cas':
            $container->add(AuthInterface::class, CASAuth::class)
                ->addArguments([
                    AdapterInterface::class,
                    LoggerInterface::class,
                    $container->get('settings')['sso']['cas'],
                ]);
            $container->add(SingleLogoutInterface::class, CASSingleLogout::class)
                ->addArguments([
                    AdapterInterface::class,
                    LoggerInterface::class,
                    SoapApi::class,
                ]);
            break;
        case 'oidc':
            $settings = $container->get('settings')['sso']['oidc'];
            $container->add(OpenIDConnectClient::class)
                ->addArguments([
                    $settings['providerUrl'],
                    $settings['clientId'],
                    $settings['clientSecret'],
                ])
                ->addMethodCall('addScope', [$settings['scopes']]);
            $container->add(AuthInterface::class, OIDCAuth::class)
                ->addArguments([
                    AdapterInterface::class,
                    LoggerInterface::class,
                    OpenIDConnectClient::class,
                ])
                ->addMethodCall('setUidMapping', [$container->get('settings')['sso']['uidMapping']]);
            $container->add(SingleLogoutInterface::class, OIDCSingleLogout::class)
                ->addArguments([
                    AdapterInterface::class,
                    LoggerInterface::class,
                    SoapApi::class,
                ]);
            break;
        default:
            $container->add(Auth::class)
                ->addArgument($container->get('settings')['sso']['saml']);
            $container->add(AuthInterface::class, SAMLAuth::class)
                ->addArguments([
                    AdapterInterface::class,
                    LoggerInterface::class,
                    Auth::class,
                ])
                ->addMethodCall('setUidMapping', [$container->get('settings')['sso']['uidMapping']]);
            $container->add(SingleLogoutInterface::class, SAMLSingleLogout::class)
                ->addArguments([
                    AdapterInterface::class,
                    LoggerInterface::class,
                    SoapApi::class,
                    Auth::class,
                ]);
            break;
    }
};
