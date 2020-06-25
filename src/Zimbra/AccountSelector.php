<?php declare(strict_types=1);

namespace Application\Zimbra;

/**
 * AccountSelector class.
 */
class AccountSelector extends SoapType {

    /**
     * Account by enums
     *
     * @var array
     */
    private static $_byEnums = [
        'adminName',
        'appAdminName',
        'foreignPrincipal',
        'id',
        'krb5Principal',
        'name',
    ];

    /**
     * Constructor method for AccountSelector
     * @param  string $value
     * @param  string $by
     * @return self
     */
    public function __construct($value, $by = 'name') {
        parent::__construct(trim($value));
        $this->by = in_array($by, self::$_byEnums) ? $by : 'name';
    }

    /**
     * Returns the array representation of this class 
     *
     * @param  string $name
     * @return array
     */
    public function toArray($name = 'account') {
        return parent::toArray($name);
    }
}
