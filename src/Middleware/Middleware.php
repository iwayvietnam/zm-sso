<?php
declare(strict_types=1);

namespace Application\Middleware;

use Psr\Container\ContainerInterface;
use Psr\Http\Message\MiddlewareInterface;

/**
 * Middleware class
 */
abstract class Middleware implements MiddlewareInterface
{
    protected $container;
    
    /**
     * Middleware constructor.
     */
    public function __construct(ContainerInterface $container)
    {
        $this->container = $container;
    }
}
