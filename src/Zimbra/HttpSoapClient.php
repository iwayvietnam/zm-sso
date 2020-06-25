<?php declare(strict_types=1);

namespace Application\Zimbra;

use GuzzleHttp\Client as HttpClient;
use GuzzleHttp\Exception\BadResponseException;
use GuzzleHttp\Psr7\Response as HttpResponse;

/**
 * This is a class which provides a http client for SOAP servers
 */
class HttpSoapClient {

    /**
     * Authentication token
     * @var string
     */
    private $_authToken;

    /**
     * Authentication session identify
     * @var string
     */
    private $_sessionId;

    /**
     * @var SoapMessage
     */
    private $_soapMessage;

    /**
     * Http client
     * @var HttpClient
     */
    protected $_httpClient;

    /**
     * Request headers
     * @var array
     */
    private $_headers = [];

    /**
     * Server location
     * @var string
     */
    private $_location;

    /**
     * Last request message
     * @var string
     */
    private $_request;

    /**
     * Last response message
     * @var string
     */
    private $_response;

    /**
     * SoapClient constructor
     *
     * @param string $location  The URL to request.
     */
    public function __construct($location) {
        $this->_location = $location;
        $this->_httpClient = new HttpClient([
            'cookies' => true,
            'verify' => false,
        ]);
    }

    /**
     * Sets authentication token.
     *
     * @param  string $authToken Authentication token
     * @return self
     */
    public function setAuthToken($authToken) {
        $this->_authToken = trim($authToken);
        return $this;
    }

    /**
     * Gets authentication token.
     *
     * @return string
     */
    public function getAuthToken() {
        return $this->_authToken;
    }

    /**
     * Sets authentication session identify.
     *
     * @param  string $sessionId Authentication session identify
     * @return self
     */
    public function setSessionId($sessionId = NULL){
        $this->_sessionId = trim($sessionId);
        return $this;
    }

    /**
     * Gets authentication session identify.
     *
     * @return string
     */
    public function getSessionId() {
        return $this->_sessionId;
    }

    /**
     * Performs a SOAP request
     *
     * @param  SoapRequest $request
     * @return object Soap response
     */
    public function doRequest(SoapRequest $request) {
        $this->_soapMessage = new SoapMessage();
        if (!empty($this->_authToken)) {
            $this->_soapMessage->addHeader('authToken', $this->_authToken);
        }
        if (!empty($this->_sessionId)) {
            $this->_soapMessage->addHeader('sessionId', $this->_sessionId);
        }
        $this->_soapMessage->addHeader('format', 'js');
        $this->_soapMessage->setRequest($request);
        $this->_request = $this->_soapMessage->toJson();
        $response = $this->_doRequest($this->_request, [
            'Content-Type' => 'application/soap+xml; charset=utf-8',
            'Method'       => 'POST',
            'User-Agent'   => isset($_SERVER['HTTP_USER_AGENT']) ? $_SERVER['HTTP_USER_AGENT'] : 'PHP-Zimbra-Soap-API',
            'SoapAction' => $request->getNamespace() . '#' . $request->className()
        ]);
        return new SoapResponse($response);
    }

    /**
     * Returns last SOAP request.
     *
     * @return mix The last SOAP request string.
     */
    public function lastRequest(){
        return $this->_request;
    }

    /**
     * Returns the SOAP headers from the last request.
     *
     * @return mix The last SOAP request headers.
     */
    function lastRequestHeaders(){
        return $this->_headers;
    }

    /**
     * Returns last SOAP response.
     *
     * @return mix The last SOAP response string.
     */
    public function lastResponse(){
        return $this->_response;
    }

    /**
     * Returns the SOAP headers from the last response.
     *
     * @return mix The last SOAP response headers.
     */
    public function lastResponseHeaders() {
        if($this->_response instanceof HttpResponse) {
            return $this->_response->getHeaders();
        }
        return [];
    }

    /**
     * Performs SOAP request over HTTP.
     *
     * @param  string $request The SOAP request.
     * @param  string $headers The HTTP request header.
     * @return mixed
     */
    private function _doRequest($request, array $headers = []) {
        $this->_headers = $headers;

        $options = [
            'headers' => $headers,
            'body' => (string) $request,
        ];

        try
        {
            $this->_response = $this->_httpClient->request('POST', $this->location, $options);
        }
        catch (BadResponseException $ex)
        {
            if ($ex->hasResponse())
            {
                $this->_response = $ex->getResponse();
            }
            throw $ex;
        }
        return $this->_response;
    }
}
