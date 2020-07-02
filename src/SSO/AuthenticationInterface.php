<?php declare(strict_types=1);

namespace Application\SSO;

use Psr\Http\Message\ServerRequestInterface as Request;

/**
 * Authentication interface
 */
interface AuthenticationInterface
{
    function login(Request $request): ?string;
    function logout(Request $request): ?string;
    function singleLogout(Request $request): ?string;
    function isAuthenticated(): bool;
    function getUserName(string $sessionId = NULL): string;
}
