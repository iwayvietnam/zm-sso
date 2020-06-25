<?php declare(strict_types=1);

namespace Application\Zimbra;

/**
 * A class representation soap type.
 */
abstract class SoapType implements SoapTypeInterface {

    /**
     * Struct value
     * @var string
     */
    private $_value;

    /**
     * Properties
     *
     * @var array
     */
    private $_properties = [];

    /**
     * soap struct namespace
     * @var string
     */
    private $_namespace = NULL;

    /**
     * Constructor method for SoapStruct
     *
     * @param  string $value
     * @return self
     */
    public function __construct($value = NULL){
        if (NULL !== $value) {
            $this->_value = trim($value);
        }
    }

    /**
     * Sets value
     *
     * @param  string $value
     * @return self
     */
    public function setValue($value) {
        $this->_value = trim($value);
        return $this;
    }

    /**
     * Gets value
     *
     * @return string
     */
    public function getValue() {
        return $this->_value;
    }

    /**
     * Sets namespace
     *
     * @param  string $value
     * @return self
     */
    public function setNamespace($namespace) {
        $this->_namespace = trim($namespace);
        return $this;
    }

    /**
     * Gets namespace
     *
     * @return string
     */
    public function getNamespace() {
        return $this->_namespace;
    }

    /**
     * Returns name representation of this class 
     *
     * @return string
     */
    public function className(){
        $ref = new \ReflectionObject($this);
        return $ref->getShortName();
    }

    /**
     * Returns the array representation of this class
     *
     * @param  string $name
     * @return array
     */
    public function toArray($name = NULL) {
        $name = !empty($name) ? $name : $this->className();
        $arr = [];
        if (NULL !== $this->_value) {
            $arr['_content'] = $this->_value;
        }
        if (!empty($this->_namespace)) {
            $arr['_jsns'] = $this->_namespace;
        }
        if (!empty($this->_properties)) {
            foreach ($this->_properties as $key => $value) {
                if ($value instanceof SoapTypeInterface) {
                    $arr += $value->toArray($key);
                }
                elseif (is_array($value) && !empty($value)) {
                    $arr[$key] = [];
                    foreach ($value as $child) {
                        if ($child instanceof SoapTypeInterface) {
                            $childArr = $child->toArray($key);
                            $arr[$key][] = $childArr[$key];
                        }
                        else {
                            $arr[$key][] = $child;
                        }
                    }
                }
                else {
                    $arr[$key] = $value;
                }
            }
        }
        return [$name => $arr];
    }

    /**
     * Get a data by key
     *
     * @param string $key
     *   The key data to retrieve
     */
    public function &__get($key) {
        return $this->_properties[$key];
    }

    /**
     * Assigns a value to the specified data
     *
     * @param string $key
     *   The data key to assign the value to
     * @param mixed  $value
     *   The value to set
     */
    public function __set($key, $value) {
        $this->_properties[$key] = $value;
    }

    /**
     * Whether or not an data exists by key
     *
     * @param string $key
     *   An data key to check for
     * @return boolean
     */
    public function __isset($key) {
        return isset($this->_properties[$key]);
    }

    /**
     * Unsets an data by key
     *
     * @param string $key
     *   The key to unset
     */
    public function __unset($key) {
        unset($this->_properties[$key]);
    }

    /**
     * Assigns a value to the specified offset.
     *
     * @param string $offset
     *   The offset to assign the value to
     * @param mixed  $value
     *   The value to set
     */
    public function offsetSet($offset, $value) {
        if (is_null($offset)) {
            $this->_properties[] = $value;
        }
        else {
            $this->_properties[$offset] = $value;
        }
    }

    /**
     * Whether or not an offset exists
     *
     * @param string $offset
     *   An offset to check for
     * @return boolean
     */
    public function offsetExists($offset) {
        return isset($this->_properties[$offset]);
    }

    /**
     * Unsets an offset
     *
     * @param string $offset
     *   The offset to unset
     */
    public function offsetUnset($offset) {
        if ($this->offsetExists($offset)) {
            unset($this->_properties[$offset]);
        }
    }

    /**
     * Returns the value at specified offset
     *
     * @param string $offset
     *   The offset to retrieve
     * @return mixed
     */
    public function offsetGet($offset) {
        return $this->offsetExists($offset) ? $this->_properties[$offset] : NULL;
    }
}
