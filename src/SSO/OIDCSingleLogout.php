<?php declare(strict_types=1);

namespace Application\SSO;

use Application\Zimbra\SoapApi;
use Laminas\Db\Adapter\Adapter;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;

/**
 * OIDC single logout class
 */
class OIDCSingleLogout extends BaseSingleLogout
{
    public function __construct(Adapter $adapter, Logger $logger, SoapApi $api)
    {
        parent::__construct($adapter, $logger, $api);
        $this->protocol = 'OIDC';
    }

    public function logout(Request $request): ?string
    {
    	$targetUrl = NULL;
    	return $targetUrl;
    }
}
