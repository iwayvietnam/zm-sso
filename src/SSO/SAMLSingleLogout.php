<?php declare(strict_types=1);

namespace Application\SSO;

use Application\Zimbra\SoapApi;
use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;
use OneLogin\Saml2\Auth;

/**
 * SAML single logout class
 */
class SAMLSingleLogout extends BaseSingleLogout
{
    private $saml;

    public function __construct(Adapter $adapter, Logger $logger, SoapApi $api, Auth $saml)
    {
        parent::__construct($adapter, $logger, $api);
        $this->protocol = 'SAML';
        $this->saml = $saml;
    }

    public function logout(Request $request): ?string
    {
        $parsedBody = $request->getParsedBody();
        if(!empty($parsedBody['SAMLRequest'])) {
            $_GET['SAMLRequest'] = $parsedBody['SAMLRequest'];
        }
        $targetUrl = $this->saml->processSLO(TRUE, NULL, FALSE, NULL, TRUE);
        if(!empty($targetUrl) && !$this->saml->getLastErrorReason()){
            $sessionIndex = $request->getAttribute('session')->get('sessionIndex');
            if (!empty($sessionIndex)) {
                $this->doLogout($sessionIndex);
            }
        }
        if (empty($targetUrl)) {
            # code...
        }
        return $targetUrl;
    }
}
