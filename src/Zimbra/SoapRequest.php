<?php declare(strict_types=1);

namespace Application\Zimbra;

/**
 * Soap request class.
 */
abstract class SoapRequest extends SoapType {

    /**
     * SoapRequest constructor
     *
     * @param  string $value
     * @return self
     */
    public function __construct($value = NULL) {
        parent::__construct($value);
        $this->setNamespace('urn:zimbraAdmin');
    }
}
