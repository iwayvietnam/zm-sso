<?php declare(strict_types=1);

namespace Application\SSO;

use Application\Zimbra\SoapApi;
use Laminas\Db\Adapter\Adapter;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;
use Slim\Routing\RouteContext;

/**
 * CAS single logout class
 */
class CASSingleLogout extends BaseSingleLogout
{
    private $settings;
    
    public function __construct(Adapter $adapter, Logger $logger, SoapApi $api, array $settings = [])
    {
        parent::__construct($adapter, $logger, $api);
        $this->settings = $settings;

        $version = $settings['version'];
        \phpCAS::client(
            $version,
            $settings['serverHost'],
            $settings['serverPort'],
            $settings['context']
        );

        $protocols = \phpCAS::getSupportedProtocols();
        $this->protocol = !empty($protocols[$version]) ? $protocols[$version] : 'CAS';
    }

    public function logout(Request $request): ?string
    {
        \phpCAS::setSingleSignoutCallback(function ($ticket) {
            $this->doLogout($ticket);
        });
        \phpCAS::handleLogoutRequests();
        return RouteContext::fromRequest($request)->getBasePath();
    }
}
