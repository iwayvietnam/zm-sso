<?php declare(strict_types=1);

namespace Application\SSO;

use Application\Zimbra\SoapApi;
use Laminas\Db\Adapter\Adapter;
use Psr\Http\Message\ServerRequestInterface as Request;

/**
 * OIDC single logout class
 */
class OIDCSingleLogout extends BaseSingleLogout
{
    public function __construct(Adapter $adapter, SoapApi $api, array $settings = [])
    {
        parent::__construct($adapter, $api, $settings);
        $this->protocol = 'OIDC';
    }

    public function logout(Request $request): ?string
    {
    	$targetUrl = NULL;
    	return $targetUrl;
    }
}
