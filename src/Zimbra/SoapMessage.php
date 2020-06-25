<?php declare(strict_types=1);

namespace Application\Zimbra;

/**
 * A class representation soap message.
 */
class SoapMessage {

    /**
     * Soap headers
     * @var array
     */
    private $_headers = [];

    /**
     * Soap request
     * @var SoapRequest
     */
    private $_request;

    /**
     * The xml namespaces
     * @var array
     */
    private $_namespaces = ['urn:zimbra'];

    /**
     * Sets request
     *
     * @param  SoapRequest $request
     * @return self
     */
    public function setRequest(SoapRequest $request) {
        $this->_request = $request;
        return $this;
    }

    /**
     * Get request
     *
     * @return SoapRequest
     */
    public function getRequest() {
        return $this->_request;
    }

    /**
     * Add header.
     *
     * @param  string|array $name
     * @param  string $value
     * @return self
     */
    public function addHeader($name, $value = NULL) {
        if (is_array($name)) {
            foreach ($name as $n => $v) {
                $this->addHeader($n, $v);
            }
        }
        else {
            $this->_headers[$name] = $value;
        }
        return $this;
    }

    /**
     * Get soap header.
     *
     * @param  string $name
     * @return string|array
     */
    public function header($name = NULL) {
        if (NULL === $name) {
            return $this->_headers;
        }
        else {
            return isset($this->_headers[$name]) ? $this->_headers[$name] : NULL;
        }
    }

    /**
     * Returns the json encoded string representation of this class
     *
     * @return string
     */
    public function toJson(){
        $array = array();
        if (count($this->_headers)) {
            $array['Header'] = [
                'context' => [
                    '_jsns' => 'urn:zimbra',
                ],
            ];
            foreach ($this->_headers as $name => $value) {
                $array['Header']['context'][$name] = $value;
            }
        }
        if ($this->_request instanceof SoapRequest) {
            $reqArray = $this->_request->toArray();
            $reqName = $this->_request->className();
            $array['Body'][$reqName] = $reqArray[$reqName];
        }
        return json_encode((object) $array);
    }

    /**
     * Return a json string.
     *
     * @return string json string
     */
    public function __toString() {
        return trim($this->toJson());
    }
}
