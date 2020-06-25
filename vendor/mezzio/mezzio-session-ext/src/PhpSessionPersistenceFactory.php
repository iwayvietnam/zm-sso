<?php

/**
 * @see       https://github.com/mezzio/mezzio-session-ext for the canonical source repository
 * @copyright https://github.com/mezzio/mezzio-session-ext/blob/master/COPYRIGHT.md
 * @license   https://github.com/mezzio/mezzio-session-ext/blob/master/LICENSE.md New BSD License
 */

declare(strict_types=1);

namespace Mezzio\Session\Ext;

use Psr\Container\ContainerInterface;

/**
 * Create and return an instance of PhpSessionPersistence.
 *
 * In order to use non-locking sessions please provide a configuration entry
 * like the following:
 *
 * <code>
 * //...
 * 'session' => [
 *     'persistence' => [
 *         'ext' => [
 *             'non_locking' => true, // true|false
 *         ],
 *     ],
 * ],
 * //...
 * <code>
 */
class PhpSessionPersistenceFactory
{
    public function __invoke(ContainerInterface $container) : PhpSessionPersistence
    {
        $config = $container->has('config') ? $container->get('config') : null;
        $config = $config['session']['persistence']['ext'] ?? null;

        return new PhpSessionPersistence(
            ! empty($config['non_locking']),
            ! empty($config['delete_cookie_on_empty_session'])
        );
    }
}
