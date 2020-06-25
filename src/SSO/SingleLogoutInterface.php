<?php declare(strict_types=1);

namespace Application\SSO;

use Psr\Http\Message\ServerRequestInterface as Request;

/**
 * Single logout interface
 */
interface SingleLogoutInterface
{
    function logout(Request $request): ?string;
}
