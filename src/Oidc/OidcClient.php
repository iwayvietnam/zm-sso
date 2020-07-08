<?php declare(strict_types=1);

namespace Application\Oidc;

use Jumbojett\OpenIDConnectClient;
use Mezzio\Session\SessionInterface;
use function session_status;
use function session_write_close;
use const PHP_SESSION_ACTIVE;

class OidcClient extends OpenIDConnectClient {

    private $session

    public function getSession()
    {
        return $this->session;
    }

    public function setSession(SessionInterface $session)
    {
        $this->session = $session;
    }

    protected function commitSession() {
        if ($this->session instanceof SessionInterface && session_status() === PHP_SESSION_ACTIVE) {
            $_SESSION = $this->session->toArray();
            session_write_close();
        }
        else {
            parent::commitSession();
        }
    }

    protected function getSessionKey($key) {
        if ($this->session instanceof SessionInterface) {
            return $this->session->get($key);
        }
        else {
            return parent::getSessionKey($key);
        }
    }

    protected function setSessionKey($key, $value) {
        if ($this->session instanceof SessionInterface) {
            $this->session->set($key, $value);
        }
        else {
            return parent::setSessionKey($key, $value);
        }
    }

    protected function unsetSessionKey($key) {
        if ($this->session instanceof SessionInterface) {
            $this->session->unset($key);
        }
        else {
            return parent::unsetSessionKey($key);
        }
    }
}
