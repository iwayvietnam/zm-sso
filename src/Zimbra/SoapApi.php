<?php declare(strict_types=1);

namespace Application\Zimbra;

/**
 * Zimbra Soap Api class
 */
class SoapApi
{
    /**
     * Zimbra api soap client
     * @var ClientInterface
     */
    private $_client;

    public function __construct($endpoint = 'https://localhost/service/soap')
    {
        $this->_client = new HttpSoapClient($endpoint);
    }

    public function auth(
        $name = NULL,
        $password = NULL,
        $authToken = NULL,
        AccountSelector $account = NULL,
        $virtualHost = NULL,
        $persistAuthTokenCookie = NULL,
        $csrfTokenSecured = NULL
    ) {
        $req = new AuthRequest(
            $name, $password, $authToken, $account,
            $virtualHost, $persistAuthTokenCookie, $csrfTokenSecured
        );
        $result = $this->_client->doRequest($req);
        $authToken = NULL;
        if (!empty($result->authToken)){
          if (isset($result->authToken[0]->_content)) {
            $authToken =  $result->authToken[0]->_content;
          }
          $this->_client->setAuthToken($authToken);
        }
        return $authToken;
    }

    public function authByName($name, $password, $vhost = NULL) {
        return $this->auth($name, $password, NULL, NULL, $vhost, TRUE);
    }

    public function authByAccount(AccountSelector $account, $password, $vhost = NULL) {
        return $this->auth(NULL, $password, NULL, $account, $vhost, TRUE);
    }

    public function authByToken($name, $token, $vhost = NULL) {
        return $this->auth($name, NULL, $token, NULL, $vhost, TRUE);
    }

    public function getAccount(AccountSelector $account = NULL, $applyCos = NULL, $attrs = NULL) {
        $req = new GetAccountRequest(
            $account, $applyCos, $attrs
        );
        return $this->_client->doRequest($req);
    }

    public function modifyAccount($id, array $attrs = array()) {
        $req = new ModifyAccountRequest($id, $attrs);
        return $this->_client->doRequest($req);
    }
}
