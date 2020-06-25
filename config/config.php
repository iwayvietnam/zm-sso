<?php declare(strict_types=1);

return [
    'displayErrorDetails' => TRUE, // Should be set to false in production
    'logger' => [
        'name' => 'zimbra-sso-application',
        'path' => isset($_ENV['docker']) ? 'php://stdout' : __DIR__ . '/../logs/app.log',
        'level' => \Monolog\Logger::DEBUG,
    ],
    'sso' => [
        'protocol' => 'saml',
        'oidc' => [
            'provider_url' => 'https://openid-connect.onelogin.com/oidc',
            'client_id' => '5fac5d50-966a-0138-fc18-0ac688641284172270',
            'client_secret' => '1fb0d2a17b9d588ffd8d2f1fde35a30351f52c4ff23ed92ee45d67da35b565ce',
            'scopes' => [
                'openid',
                'email',
                'profile',
            ],
            'logout_url' => 'https://sso.quangnam.gov.vn/oidc/logout',
        ],
        'saml' => [
            'debug' => FALSE,
            'user_name_attr' => 'email',
            'baseurl' => 'http://localhost:8080',
            // Service provider settings
            'sp' => [
                'entityId' => 'http://localhost:8080',
                'assertionConsumerService' => [
                    'url' => 'http://localhost:8080/login',
                ],
                'singleLogoutService' => [
                    'url' => 'http://localhost:8080/slo',
                    'binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST',
                ],
            ],
            // Identity provider settings
            'idp' => [
                'entityId' => 'http://www.simplesaml.vn/saml2/idp/metadata.php',
                'singleSignOnService' => [
                    'url' => 'http://www.simplesaml.vn/saml2/idp/SSOService.php',
                ],
                'singleLogoutService' => [
                    'url' => 'http://www.simplesaml.vn/saml2/idp/SingleLogoutService.php',
                ],
                'x509cert' => 'MIIFFzCCA3+gAwIBAgIUfdMOq2XRa2PWcmAVhM3hHqVv9n8wDQYJKoZIhvcNAQELBQAwgZoxCzAJBgNVBAYTAlZOMQ4wDAYDVQQIDAVIYW5vaTEOMAwGA1UEBwwFSGFub2kxFTATBgNVBAoMDGlXYXkgVmlldG5hbTEUMBIGA1UECwwLU2ltcGxlIFNBTUwxGjAYBgNVBAMMEXd3dy5zaW1wbGVzYW1sLnZuMSIwIAYJKoZIhvcNAQkBFhNhZG1pbkBzaW1wbGVzYW1sLnZuMB4XDTIwMDYyMzA3MzgyNFoXDTMwMDYyMzA3MzgyNFowgZoxCzAJBgNVBAYTAlZOMQ4wDAYDVQQIDAVIYW5vaTEOMAwGA1UEBwwFSGFub2kxFTATBgNVBAoMDGlXYXkgVmlldG5hbTEUMBIGA1UECwwLU2ltcGxlIFNBTUwxGjAYBgNVBAMMEXd3dy5zaW1wbGVzYW1sLnZuMSIwIAYJKoZIhvcNAQkBFhNhZG1pbkBzaW1wbGVzYW1sLnZuMIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAz6o7YnwnaUvXdrVOO44mG9Bif7RlscqqaECP6X13dYG01khIrsMeQD0PaHr6PxfuocagNzFmjFRPUgwHBTOGrnKEyVGC1V+B07AQkDGnSPh1qFbqxWh4AN6xl6p2i1sQEGkqvpjreeFz+uHY2wNSC6HgF6x/WdDCoiG3OsGBDLa3PHgulW+eGddv74HP1ElaniN1/pkn65PziZUGWXDxko4JUy49+S+gxzWlIwpwOeHdGcYa3M4zdPE5gm+rSq/XdXP/guavjHEZfv87aWfGDqaDouv9K8ZgdgLdmItSSb7l/K1maunlulDRy1knPPKzw50IqctyYKNM45n8LxyfBvjrYvg6bzP51iiw3FRNSnyBhb9jtGUEWymgmN97rpZiVHwfq2P6cYtD8yiutTs8m6/oLMzbIKIip5yaoyHnc9SS3FgccYGEmN+8+n8KSahQednTTA3glXa101z9CuU88rE18Mdnuixkm9Zd1mU4Mg88udFULHhfsrM1drD6eRnfAgMBAAGjUzBRMB0GA1UdDgQWBBRSLMfNlPWM1ANqgdjH3lAft1ovdDAfBgNVHSMEGDAWgBRSLMfNlPWM1ANqgdjH3lAft1ovdDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBgQBfh+DVBHHKG7Xd7YMil+3royQN8punqe4TzPSUeKSrlw0Fa8lzRJ9OkDgn0VI32NEfCN6D26ctKfNl2qyvXjtAvkyuPTMvLdZN/WWYEGTJwtjNUVUjq/ISdODQ7as8icilDwdHs+SBdi5Pnfc4ouRCcCNRXv7QRFH1P3oOpMVkqagRgUJwwq10KwoPm6pHXrVmf3vlEXe3oabwloMozQy77HHnEpwGVIpD1zp/XYdC+1EPW8QGjQpFrCD18zlotCEwRn+LkJFF4usqfYk0qvSnhS7XrDTVyMP9KLdeuSqETbQU2Cd3p1eUF/fU79p1G4leM0UmFNp5p86xPtXxHVM/yK1MLrqWEbY0ID61QuOYVdQmHc51Sh8qkwHh4/GkfEWIY44n6bhL4+PdIuBCBav6xWtumqDLEA0mYFVK8Zuqc+HOmJaBwv6ZKdQGp9bnxndgz42+CYtejCBb1mI7fZq5VNzW+leIpOp5+DlRYOdXWCdzD5fhgc7icj/KpVzXIbk=',
            ],
        ],
        'cas' => [
            'server_host' => 'sso.quangnam.gov.vn',
            'server_port' => 443,
            'context' => '/cas',
            'version' => '3.0',
        ],
    ],
    'zimbra' => [
        'domain' => 'quangnam.gov.vn',
        'preauth_key' => '749a8ffa97b5acf3f1ef7d9c384baae196b0af4e33a3acb3d87f2e3cdd75425d',
        'server_url' => 'https://mail.quangnam.gov.vn',
        'admin_soap_url' => 'https://mail.quangnam.gov.vn:7071/service/admin/soap',
        'admin_user' => 'admin',
        'admin_password' => '',
    ],
    'db' => [
        'driver' => 'Pdo_Mysql',
        'hostname' => 'localhost',
        'port' => 3306,
        'database' => 'zimbra-sso',
        'username' => 'zimbra-sso',
        'password' => 'zimbra-sso',
        'charset' => 'utf8',
    ],
];
