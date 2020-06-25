<?php declare(strict_types=1);

namespace Application\SSO;

use Application\Zimbra\SoapApi;
use Laminas\Db\Adapter\Adapter;
use Psr\Http\Message\ServerRequestInterface as Request;
use OneLogin\Saml2\Auth;

/**
 * SAML single logout class
 */
class SAMLSingleLogout extends BaseSingleLogout
{
    private $auth;

    public function __construct(Adapter $adapter, SoapApi $api, array $settings = [])
    {
        parent::__construct($adapter, $api, $settings);
        $this->protocol = 'SAML';
        $this->auth = new Auth($settings['sso']['saml']);
    }

    public function logout(Request $request): ?string
    {
        $parsedBody = $request->getParsedBody();
        if(!empty($parsedBody['SAMLRequest'])) {
            $_GET['SAMLRequest'] = $parsedBody['SAMLRequest'];
        }
        $targetUrl = $this->auth->processSLO(TRUE, NULL, FALSE, NULL, TRUE);
        if(!empty($targetUrl) && !$this->auth->getLastErrorReason()){
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
