<?php declare(strict_types=1);

namespace Application\Zimbra;

/**
 * AuthRequest class
 * Authenticate for administration
 */
class AuthRequest extends SoapRequest {

    /**
     * Constructor method for AuthRequest
     * @return self
     */
    public function __construct(
        $name = NULL,
        $password = NULL,
        $authToken = NULL,
        AccountSelector $account = NULL,
        $virtualHost = NULL,
        $persistAuthTokenCookie = NULL,
        $csrfTokenSecured = NULL
    ) {
        parent::__construct();
        if (NULL !== $name) {
            $this->name = trim($name);
        }
        if (NULL !== $password) {
            $this->password = trim($password);
        }
        if (NULL !== $authToken) {
            $this->authToken = [
                [
                    '_content' => trim($authToken),
                ]
            ];
        }
        if ($account instanceof AccountSelector) {
            $this->account = $account;
        }
        if (NULL !== $virtualHost) {
            $this->virtualHost = trim($virtualHost);
        }
        if (NULL !== $persistAuthTokenCookie) {
            $this->persistAuthTokenCookie = (bool) $persistAuthTokenCookie;
        }
        if (NULL !== $csrfTokenSecured) {
            $this->csrfTokenSecured = (bool) $csrfTokenSecured;
        }
    }
}
