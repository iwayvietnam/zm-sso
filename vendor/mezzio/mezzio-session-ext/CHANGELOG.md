# Changelog

All notable changes to this project will be documented in this file, in reverse chronological order by release.

## 1.9.0 - 2020-05-27

### Added

- [#13](https://github.com/mezzio/mezzio-session-ext/pull/13) adds the ability to delete the session cookie in the browser once the session becomes empty; see [the documentation for details on configuration of this feature](https://docs.mezzio.dev/mezzio-session-ext/intro/#enabling-deletion-of-cookie-in-browser).

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Nothing.

## 1.8.1 - 2020-03-28

### Added

- Nothing.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Fixed `replace` version constraint in composer.json so repository can be used as replacement of `zendframework/zend-expressive-session-ext:^1.7.0`.

## 1.8.0 - 2020-03-20

### Added

- [#9](https://github.com/mezzio/mezzio-session-ext/pull/9) adds support for `session.cookie_samesite` PHP ini option.

### Changed

- [#9](https://github.com/mezzio/mezzio-session-ext/pull/9) bumps the minimum supported PHP version to 7.3.0.

### Deprecated

- Nothing.

### Removed

- [#9](https://github.com/mezzio/mezzio-session-ext/pull/9) removes support for PHP versions prior to 7.3.0.

- [#9](https://github.com/mezzio/mezzio-session-ext/pull/9) removes support for `dflydev/fig-cookies` v1 releases.

### Fixed

- Nothing.

## 1.7.2 - 2020-03-28

### Added

- Nothing.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Fixed `replace` version constraint in composer.json so repository can be used as replacement of `zendframework/zend-expressive-session-ext:^1.7.0`.

## 1.7.1 - 2020-03-19

### Added

- [#10](https://github.com/mezzio/mezzio-session-ext/pull/10) adds support for PHP 7.4.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- [#7](https://github.com/mezzio/mezzio-session-ext/pull/7) fixes destroying session on regenerate when session is active.

## 1.7.0 - 2019-10-16

### Added

- [zendframework/zend-expressive-session-ext#43](https://github.com/zendframework/zend-expressive-session-ext/pull/43) adds the ability to mark sessions as non-locking; see [the documentation for details on configuration of this feature](https://docs.laminas.dev/laminas-experssive-session-ext/intro/#enabling-non-locking-sessions).

### Changed

- [zendframework/zend-expressive-session-ext#45](https://github.com/zendframework/zend-expressive-session-ext/pull/45) updates `PhpSessionPersistence` to implement `Mezzio\Session\InitializeSessionIdInterface`, allowing access to the generated / regenerated session ID before the session is persisted.

- [zendframework/zend-expressive-session-ext#45](https://github.com/zendframework/zend-expressive-session-ext/pull/45) updates the minimum required version of zendframework/zend-expressive-session to 1.3.0.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- [zendframework/zend-expressive-session-ext#44](https://github.com/zendframework/zend-expressive-session-ext/pull/44) prevents sending cookie and cache limiting headers when session data is unchanged.

## 1.6.0 - 2019-02-28

### Added

- Nothing.

### Changed

- [zendframework/zend-expressive-session-ext#39](https://github.com/zendframework/zend-expressive-session-ext/pull/39) modifies the logic used to determine the value for the `Last-Modified`
  header associated with the session cookie to use PHP's `getlastmod()` method,
  providing a simpler, more reliable source.

- [zendframework/zend-expressive-session-ext#40](https://github.com/zendframework/zend-expressive-session-ext/pull/40) modifies the logic that builds the session cookie to better parse the
  entire spectrum of expected `php.ini` values for boolean flags, ensuring that
  values such as "On" and "Off" evaluate to `true` and `false`, respectively.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Nothing.

## 1.5.1 - 2019-02-27

### Added

- Nothing.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- [zendframework/zend-expressive-session-ext#36](https://github.com/zendframework/zend-expressive-session-ext/pull/36) provides a fix that prevents session files from being created when no
  session cookie was sent by the client.

## 1.5.0 - 2019-02-11

### Added

- Nothing.

### Changed

- [zendframework/zend-expressive-session-ext#34](https://github.com/zendframework/zend-expressive-session-ext/pull/34) modifies the logic used when starting a session to ensure the REQUIRED
  defaults are always set. These include:
  - session.use_cookies = false
  - session.use_only_cookes = true
  - session.cache_limiter = ""

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Nothing.

## 1.4.0 - 2019-01-09

### Added

- [zendframework/zend-expressive-session-ext#31](https://github.com/zendframework/zend-expressive-session-ext/pull/31) adds support for the `session.cookie_domain`, `session.cookie_httponly`,
  and `session.cookie_secure` INI values when creating the `Set-Cookie` header
  value.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Nothing.

## 1.3.0 - 2018-10-31

### Added

- [zendframework/zend-expressive-session-ext#29](https://github.com/zendframework/zend-expressive-session-ext/pull/29) adds support for the mezzio-session `SessionCookiePersistenceInterface`.
  Specifically, `PhpSessionPersistence::persistSession()` now consults the
  session instance for a requested session duration, using it if present, even
  if a `session.cookie_lifetime` INI value was previously set.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Nothing.

## 1.2.0 - 2018-09-12

### Added

- [zendframework/zend-expressive-session-ext#24](https://github.com/zendframework/zend-expressive-session-ext/pull/24) adds support for `session.cookie_lifetime` configuration. When
  present, the generated session cookie will be provided with an expiration date
  based on that value.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- [zendframework/zend-expressive-session-ext#25](https://github.com/zendframework/zend-expressive-session-ext/pull/25) fixes a situation where creating a new session with no data was
  always creating a `SetCookie` header. It now correctly skips creating the header.

## 1.1.1 - 2018-05-14

### Added

- Nothing.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- [zendframework/zend-expressive-session-ext#21](https://github.com/zendframework/zend-expressive-session-ext/pull/21) fixes a situation whereby during persistence, if no identifier existed for
  the session, it was not persisted. Such situations would occur when a new session was created, as
  no identifier would yet exist. It now properly generates an identifier and persists the data in
  such cirumstances.

## 1.1.0 - 2018-05-10

### Added

- Nothing.

### Changed

- [zendframework/zend-expressive-session-ext#12](https://github.com/zendframework/zend-expressive-session-ext/pull/12) updates the `PhpSessionPersistence` class such that it is now responsible for
  emitting the various cache limiter headers (`Expires`, `Cache-Control`, `Last-Modified`, and `Pragma`) normally
  emitted by ext-session and controlled by the `session.cache_limiter` and `session.cache_expire` INI settings.
  This approach ensures that those headers are not overwritten by ext-session if set elsewhere in your
  application.

- [zendframework/zend-expressive-session-ext#9](https://github.com/zendframework/zend-expressive-session-ext/pull/9) swaps a call to `session_commit` to `session_write_close` withing `PhpSessionPersistence`,
  as the former is an alias for the latter.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- [zendframework/zend-expressive-session-ext#13](https://github.com/zendframework/zend-expressive-session-ext/pull/13) fixes an issue whereby a new session cookie is not always sent
  following an ID regeneration.

## 1.0.1 - 2018-03-15

### Added

- Nothing.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- [zendframework/zend-expressive-session-ext#8](https://github.com/zendframework/zend-expressive-session-ext/pull/8)
  fixes how session resets occur, ensuring cookies are reset correctly.

## 1.0.0 - 2018-03-15

### Added

- Nothing.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Removes support for pre-stable 1.0.0 versions of mezzio-session.

### Fixed

- Nothing.

## 0.1.4 - 2018-02-28

### Added

- Nothing.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- [zendframework/zend-expressive-session-ext#1](https://github.com/zendframework/zend-expressive-session-ext/pull/1)
  fixes a problem that occurs when a requested session does not resolve to an
  existing session and/or an existing session with empty data, leading to
  creation of new sessions on each request.

## 0.1.3 - 2018-02-24

### Added

- [zendframework/zend-expressive-session-ext#5](https://github.com/zendframework/zend-expressive-session-ext/pull/5) adds
  support for the ^1.0.0alpha1 release of mezzio-session.

## 0.1.2 - 2017-12-12

### Added

- [zendframework/zend-expressive-session-ext#3](https://github.com/zendframework/zend-expressive-session-ext/pull/3) adds
  support for the 1.0-dev and 1.0 releases of mezzio-session.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Nothing.

## 0.1.1 - 2017-10-10

### Added

- Nothing.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Fixes session regeneration under PHP 7.2 so that it will not raise warnings.

## 0.1.0 - 2017-10-10

Initial release.

### Added

- Everything.

### Changed

- Nothing.

### Deprecated

- Nothing.

### Removed

- Nothing.

### Fixed

- Nothing.
