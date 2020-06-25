<?php declare(strict_types=1);

namespace Application\Zimbra;

/**
 * GetAccountRequest class
 * Get attributes related to an account
 */
class GetAccountRequest extends SoapRequest {

    /**
     * Constructor method for GetAccountRequest
     * @return self
     */
    public function __construct(AccountSelector $account = NULL, $applyCos = NULL, $attrs = NULL) {
        parent::__construct();
        if ($account instanceof AccountSelector) {
            $this->account = $account;
        }
        if (NULL !== $applyCos) {
            $this->applyCos = (bool) $applyCos;
        }
        if (NULL !== $attrs) {
            $this->attrs = trim($attrs);
        }
    }
}
