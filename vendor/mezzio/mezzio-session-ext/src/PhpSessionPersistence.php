<?php

/**
 * @see       https://github.com/mezzio/mezzio-session-ext for the canonical source repository
 * @copyright https://github.com/mezzio/mezzio-session-ext/blob/master/COPYRIGHT.md
 * @license   https://github.com/mezzio/mezzio-session-ext/blob/master/LICENSE.md New BSD License
 */

declare(strict_types=1);

namespace Mezzio\Session\Ext;

use Dflydev\FigCookies\FigRequestCookies;
use Dflydev\FigCookies\FigResponseCookies;
use Dflydev\FigCookies\SetCookie;
use Dflydev\FigCookies\Modifier\SameSite;
use Mezzio\Session\InitializePersistenceIdInterface;
use Mezzio\Session\Session;
use Mezzio\Session\SessionCookiePersistenceInterface;
use Mezzio\Session\SessionInterface;
use Mezzio\Session\SessionPersistenceInterface;
use Psr\Http\Message\ResponseInterface;
use Psr\Http\Message\ServerRequestInterface;

use function bin2hex;
use function filemtime;
use function filter_var;
use function getlastmod;
use function gmdate;
use function ini_get;
use function random_bytes;
use function session_destroy;
use function session_id;
use function session_name;
use function session_start;
use function session_status;
use function session_write_close;
use function sprintf;
use function time;

use const FILTER_NULL_ON_FAILURE;
use const FILTER_VALIDATE_BOOLEAN;
use const PHP_SESSION_ACTIVE;

/**
 * Session persistence using ext-session.
 *
 * Adapts ext-session to work with PSR-7 by disabling its auto-cookie creation
 * (`use_cookies => false`), while simultaneously requiring cookies for session
 * handling (`use_only_cookies => true`). The implementation pulls cookies
 * manually from the request, and injects a `Set-Cookie` header into the
 * response.
 *
 * Session identifiers are generated using random_bytes (and casting to hex).
 * During persistence, if the session regeneration flag is true, a new session
 * identifier is created, and the session re-started.
 */
class PhpSessionPersistence implements InitializePersistenceIdInterface, SessionPersistenceInterface
{
    /**
     * This unusual past date value is taken from the php-engine source code and
     * used "as is" for consistency.
     */
    public const CACHE_PAST_DATE = 'Thu, 19 Nov 1981 08:52:00 GMT';

    public const HTTP_DATE_FORMAT = 'D, d M Y H:i:s T';

    /**
     * Use non locking mode during session initialization?
     *
     * @var bool
     */
    private $nonLocking;

    /**
     * Delete cookie from browser when session becomes empty?
     *
     * @var bool
     */
    private $deleteCookieOnEmptySession;

    /**
     * The time-to-live for cached session pages in minutes as specified in php
     * ini settings. This has no effect for 'nocache' limiter.
     *
     * @var int
     */
    private $cacheExpire;

    /**
     * The cache control method used for session pages as specified in php ini
     * settings. It may be one of the following values: 'nocache', 'private',
     * 'private_no_expire', or 'public'.
     *
     * @var string
     */
    private $cacheLimiter;

    /** @var array */
    private static $supportedCacheLimiters = [
        'nocache'           => true,
        'public'            => true,
        'private'           => true,
        'private_no_expire' => true,
    ];

    /**
     * Memorize session ini settings before starting the request.
     *
     * The cache_limiter setting is actually "stolen", as we will start the
     * session with a forced empty value in order to instruct the php engine to
     * skip sending the cache headers (this being php's default behaviour).
     * Those headers will be added programmatically to the response along with
     * the session set-cookie header when the session data is persisted.
     *
     * @param bool $nonLocking use the non locking mode during initialization?
     * @param bool $deleteCookieOnEmptySession delete cookie from browser when session becomes empty?
     */
    public function __construct(bool $nonLocking = false, bool $deleteCookieOnEmptySession = false)
    {
        $this->nonLocking = $nonLocking;
        $this->deleteCookieOnEmptySession = $deleteCookieOnEmptySession;

        $this->cacheLimiter = ini_get('session.cache_limiter');
        $this->cacheExpire  = (int) ini_get('session.cache_expire');
    }

    /**
     * @internal
     * @return bool the non-locking mode used during initialization
     */
    public function isNonLocking() : bool
    {
        return $this->nonLocking;
    }

    /**
     * @internal
     * @return bool whether we delete cookie from browser when session becomes empty
     */
    public function isDeleteCookieOnEmptySession(): bool
    {
        return $this->deleteCookieOnEmptySession;
    }

    public function initializeSessionFromRequest(ServerRequestInterface $request) : SessionInterface
    {
        $sessionId = FigRequestCookies::get($request, session_name())->getValue() ?? '';
        if ($sessionId) {
            $this->startSession($sessionId, [
                'read_and_close' => $this->nonLocking,
            ]);
        }
        return new Session($_SESSION ?? [], $sessionId);
    }

    public function persistSession(SessionInterface $session, ResponseInterface $response) : ResponseInterface
    {
        $id = $session->getId();

        // Regenerate if:
        // - the session is marked as regenerated
        // - the id is empty, but the data has changed (new session)
        if ($session->isRegenerated()
            || ($id === '' && $session->hasChanged())
        ) {
            $id = $this->regenerateSession();
        } elseif ($this->nonLocking && $session->hasChanged()) {
            // we reopen the initial session only if there are changes to write
            $this->startSession($id);
        }

        if (session_status() === PHP_SESSION_ACTIVE) {
            $_SESSION = $session->toArray();
            session_write_close();
        }

        // If we do not have an identifier at this point, it means a new
        // session was created, but never written to. In that case, there's
        // no reason to provide a cookie back to the user.
        if ($id === '') {
            return $response;
        }

        // A session that did not change at all does not need to be sent to the browser
        if (! $session->hasChanged()) {
            return $response;
        }

        $response = $this->addSessionCookie($response, $id, $session);
        $response = $this->addCacheHeaders($response);

        return $response;
    }

    public function initializeId(SessionInterface $session) : SessionInterface
    {
        $id = $session->getId();
        if ($id === '' || $session->isRegenerated()) {
            $session = new Session($session->toArray(), $this->generateSessionId());
        }

        session_id($session->getId());

        return $session;
    }

    /**
     * @param array $options Additional options to pass to `session_start()`.
     */
    private function startSession(string $id, array $options = []) : void
    {
        session_id($id);
        session_start([
            'use_cookies'      => false,
            'use_only_cookies' => true,
            'cache_limiter'    => '',
        ] + $options);
    }

    /**
     * Regenerates the session safely.
     */
    private function regenerateSession() : string
    {
        if (session_status() === PHP_SESSION_ACTIVE) {
            session_destroy();
        }

        $id = $this->generateSessionId();
        $this->startSession($id, [
            'use_strict_mode' => false,
        ]);
        return $id;
    }

    /**
     * Generate a session identifier.
     */
    private function generateSessionId() : string
    {
        return bin2hex(random_bytes(16));
    }

    /**
     * Add a session set-cookie to the response
     *
     * @param string $id The id of the last started session
     */
    private function addSessionCookie(
        ResponseInterface $response,
        string $id,
        SessionInterface $session
    ) : ResponseInterface {
        return FigResponseCookies::set(
            $response,
            $this->createSessionCookie(session_name(), $id, $this->getCookieLifetime($session))
        );
    }

    /**
     * Build a SetCookie parsing boolean ini settings
     *
     * @param string $name The session name as the cookie name
     * @param string $id The session id as the cookie value
     * @param int $cookieLifetime The session cookie lifetime
     */
    private function createSessionCookie(string $name, string $id, int $cookieLifetime = 0) : SetCookie
    {
        $secure = filter_var(
            ini_get('session.cookie_secure'),
            FILTER_VALIDATE_BOOLEAN,
            FILTER_NULL_ON_FAILURE
        );
        $httpOnly = filter_var(
            ini_get('session.cookie_httponly'),
            FILTER_VALIDATE_BOOLEAN,
            FILTER_NULL_ON_FAILURE
        );

        $sessionCookie = SetCookie::create($name)
            ->withValue($id)
            ->withPath(ini_get('session.cookie_path'))
            ->withDomain(ini_get('session.cookie_domain'))
            ->withSecure($secure)
            ->withHttpOnly($httpOnly);

        $cookieSameSite = ini_get('session.cookie_samesite');
        if ($cookieSameSite) {
            $sessionCookie = $sessionCookie->withSameSite(SameSite::fromString($cookieSameSite));
        }

        return $cookieLifetime
            ? $sessionCookie->withExpires(time() + $cookieLifetime)
            : $sessionCookie;
    }

    /**
     * Add cache headers to the response when needed
     */
    private function addCacheHeaders(ResponseInterface $response) : ResponseInterface
    {
        if (! $this->cacheLimiter || $this->responseAlreadyHasCacheHeaders($response)) {
            return $response;
        }

        $cacheHeaders = $this->generateCacheHeaders();
        foreach ($cacheHeaders as $name => $value) {
            if ($value !== false) {
                $response = $response->withHeader($name, $value);
            }
        }

        return $response;
    }

    /**
     * Generate cache http headers for this instance's session cache_limiter and
     * cache_expire values
     */
    private function generateCacheHeaders() : array
    {
        // Unsupported cache_limiter
        if (! isset(self::$supportedCacheLimiters[$this->cacheLimiter])) {
            return [];
        }

        // cache_limiter: 'nocache'
        if ($this->cacheLimiter === 'nocache') {
            return [
                'Expires'       => self::CACHE_PAST_DATE,
                'Cache-Control' => 'no-store, no-cache, must-revalidate',
                'Pragma'        => 'no-cache',
            ];
        }

        $maxAge       = 60 * $this->cacheExpire;
        $lastModified = $this->getLastModified();

        // cache_limiter: 'public'
        if ($this->cacheLimiter === 'public') {
            return [
                'Expires'       => gmdate(self::HTTP_DATE_FORMAT, time() + $maxAge),
                'Cache-Control' => sprintf('public, max-age=%d', $maxAge),
                'Last-Modified' => $lastModified,
            ];
        }

        // cache_limiter: 'private'
        if ($this->cacheLimiter === 'private') {
            return [
                'Expires'       => self::CACHE_PAST_DATE,
                'Cache-Control' => sprintf('private, max-age=%d', $maxAge),
                'Last-Modified' => $lastModified,
            ];
        }

        // last possible case, cache_limiter = 'private_no_expire'
        return [
            'Cache-Control' => sprintf('private, max-age=%d', $maxAge),
            'Last-Modified' => $lastModified,
        ];
    }

    /**
     * Return the Last-Modified header line based on main script of execution
     * modified time. If unable to get a valid timestamp we use this class file
     * modification time as fallback.
     *
     * @return string|false
     */
    private function getLastModified()
    {
        $lastmod = getlastmod() ?: filemtime(__FILE__);
        return $lastmod ? gmdate(self::HTTP_DATE_FORMAT, $lastmod) : false;
    }

    /**
     * Check if the response already carries cache headers
     */
    private function responseAlreadyHasCacheHeaders(ResponseInterface $response) : bool
    {
        return $response->hasHeader('Expires')
            || $response->hasHeader('Last-Modified')
            || $response->hasHeader('Cache-Control')
            || $response->hasHeader('Pragma');
    }

    private function getCookieLifetime(SessionInterface $session) : int
    {
        if ($this->deleteCookieOnEmptySession && ! $session->toArray()) {
            return -(time() - 1);
        }

        $lifetime = (int) ini_get('session.cookie_lifetime');
        if ($session instanceof SessionCookiePersistenceInterface
            && $session->has(SessionCookiePersistenceInterface::SESSION_LIFETIME_KEY)
        ) {
            $lifetime = $session->getSessionLifetime();
        }

        return $lifetime > 0 ? $lifetime : 0;
    }
}
