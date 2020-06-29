<?php declare(strict_types=1);

namespace Application\Zimbra;

/**
 * PreAuth class
 */
class PreAuth {
    private $serverUrl;
    private $key;
    private $timestamp;
    private $expires;
    private $domain;

    /**
     * Constructor method for PreAuth
     * @return self
     */
    public function __construct($serverUrl, $key, $domain, $timestamp = NULL, $expires = NULL)
    {
        $this->serverUrl = trim($serverUrl, '/');
        $this->key = $key;
        $this->domain = trim($domain);
        $this->timestamp = (int) $timestamp < 0 ? time() * 1000 : (int) $timestamp;
        $this->expires = (int) $expires < 0 ? 0 : (int) $expires;
    }

    /**
     * generate preauth url
     *
     * @param  string $account
     * @param  string $domain
     * @return string.
     */
    public function generatePreauthURL($account, $domain = NULL)
    {
        if (!empty($account) && !(bool) filter_var($account, FILTER_VALIDATE_EMAIL)) {
            $account = strtr('%account%@%domain%', [
                '%account%' => $account,
                '%domain%' => !empty($domain) ? $domain : $this->domain,
            ]);
        }
        $preauth = hash_hmac('sha1', $account . '|name|' . $this->expires . '|' . $this->timestamp, $this->key);
        $preauthURL = strtr(
            '%serverUrl%/service/preauth/?account=%account%&by=name&timestamp=%timestamp%&expires=%expires%preauth=%preauth%',
            [
                '%serverUrl%' => $this->serverUrl,
                '%account%' => $account,
                '%timestamp%' => $this->timestamp,
                '%expires%' => $this->expires,
                '%preauth%' => $preauth,
            ]
        );
        return $preauthURL;
    }
}
