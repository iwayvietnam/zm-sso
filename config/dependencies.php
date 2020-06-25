<?php declare(strict_types=1);

use Application\SSO\AuthInterface;
use Application\SSO\CASAuth;
use Application\SSO\OIDCAuth;
use Application\SSO\SAMLAuth;
use Application\SSO\SingleLogoutInterface;
use Application\Zimbra\PreAuth;
use Application\Zimbra\SoapApi;

use Laminas\Db\Adapter\Adapter;
use Laminas\Db\Adapter\AdapterInterface;
use Mezzio\Session\SessionPersistenceInterface;
use Mezzio\Session\Ext\PhpSessionPersistence;
use Monolog\Handler\StreamHandler;
use Monolog\Logger;
use Monolog\Processor\UidProcessor;
use Psr\Container\ContainerInterface;
use Psr\Log\LoggerInterface;
use Slim\App;

return function (App $app) {
    $container = $app->getContainer();

    $container->add(SessionPersistenceInterface::class, PhpSessionPersistence::class);

    $container->add(AdapterInterface::class, Adapter::class)->addArgument($container->get('settings')['db']);

    $container->add(LoggerInterface::class, static function (ContainerInterface $container) {
        $settings = $container->get('settings')['logger'];
        $logger = new Logger($settings['name']);
        $logger->pushProcessor(new UidProcessor)
            ->pushHandler(new StreamHandler('php://stdout', $settings['level']))
            ->pushHandler(new StreamHandler($settings['path'], $settings['level']));

        return $logger;
    })->addArgument($container);

    $container->add(AuthInterface::class, static function (ContainerInterface $container) {
        $settings = $container->get('settings')['sso'];
        $protocol = $settings['protocol'];
        switch ($protocol) {
            case 'cas':
                $auth = new CASAuth(
                    $container->get(AdapterInterface::class),
                    $container->get(LoggerInterface::class),
                    $container->get('settings')
                );
                break;
            case 'saml':
                $auth = new SAMLAuth(
                    $container->get(AdapterInterface::class),
                    $container->get(LoggerInterface::class),
                    $container->get('settings')
                );
                break;
            default:
                $auth = new OIDCAuth(
                    $container->get(AdapterInterface::class),
                    $container->get(LoggerInterface::class),
                    $container->get('settings')
                );
                break;
        }
        return $auth;
    })->addArgument($container);

    $container->add(PreAuth::class, static function (ContainerInterface $container) {
        $settings = $container->get('settings')['zimbra'];
        $preAuth = new PreAuth($settings['server_url'], $settings['preauth_key']);
        return $preAuth;
    })->addArgument($container);

    $container->add(SoapApi::class, static function (ContainerInterface $container) {
        $settings = $container->get('settings')['zimbra'];
        $api = new SoapApi($settings['admin_soap_url']);
        return $api;
    })->addArgument($container);
};
