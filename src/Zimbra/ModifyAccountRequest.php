<?php declare(strict_types=1);

namespace Application\Zimbra;

/**
 * ModifyAccountRequest class
 * Modify an account
 */
class ModifyAccountRequest extends SoapRequest {

    /**
     * Constructor method for ModifyAccountRequest
     * @param string $id
     * @param array  $attrs
     * @return self
     */
    public function __construct($id, array $attrs = []) {
        parent::__construct();
        $this->id = trim($id);
        if (!empty($attrs)) {
            $this->a = $attrs;
        }
    }
}
