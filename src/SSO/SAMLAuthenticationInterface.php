<?php declare(strict_types=1);

namespace Application\SSO;

/**
 * SAML authentication interface
 */
interface SAMLAuthenticationInterface extends AuthenticationInterface
{
    function metadata(): ?string;
}
