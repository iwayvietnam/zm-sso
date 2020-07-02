<?php declare(strict_types=1);

namespace Application\SSO;

use Psr\Http\Message\ServerRequestInterface as Request;

/**
 * SAML authentication interface
 */
interface SAMLAuthenticationInterface extends AuthenticationInterface
{
    function metadata(): ?string;
    function assertionConsumerService(Request $request): ?string;
}
