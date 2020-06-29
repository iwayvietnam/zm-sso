<?php declare(strict_types=1);

namespace Application\SSO;

use Application\Zimbra\SoapApi;
use Laminas\Db\Adapter\Adapter;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Log\LoggerInterface as Logger;

/**
 * CAS single logout class
 */
class CASSingleLogout extends BaseSingleLogout
{
    
    public function __construct(Adapter $adapter, Logger $logger, SoapApi $api, $version = '3.0')
    {
        parent::__construct($adapter, $logger, $api);
        $protocols = \phpCAS::getSupportedProtocols();
        $this->protocol = !empty($protocols[$version]) ? $protocols[$version] : 'CAS';
    }

    public function logout(Request $request): ?string
    {
        $targetUrl = NULL;
        $parsedBody = $request->getParsedBody();
        if (!empty($parsedBody['logoutRequest'])) {
            $logoutRequest = utf8_encode(urldecode($parsedBody['logoutRequest']));
            $logoutRequestXml = new \SimpleXMLElement($logoutRequest);
            $namespaces = $logoutRequestXml->getNameSpaces();
            $xsearch = 'SessionIndex';
            if (isset($namespaces['samlp'])) {
                $sessionIndexes = $logoutRequestXml->children($namespaces['samlp'])->SessionIndex;
            }
            else {
                $sessionIndexes = $logoutRequestXml->xpath($xsearch);
            }
            if (!empty($sessionIndexes)) {
                $this->doLogout((string) $sessionIndexes[0]);
            }
        }
        return $targetUrl;
    }
}
