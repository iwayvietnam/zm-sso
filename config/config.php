<?php declare(strict_types=1);

return [
    'displayErrorDetails' => TRUE, // Should be set to false in production
    'logger' => [
        'name' => 'zimbra-sso',
        'path' => isset($_ENV['docker']) ? 'php://stdout' : __DIR__ . '/../logs/app.log',
        'level' => \Psr\Log\LogLevel::DEBUG,
    ],
    'sso' => [
        'protocol' => 'saml',
        'uidMapping' => 'email',
        'oidc' => [
            'providerUrl' => 'https://openid-connect.onelogin.com/oidc',
            'clientId' => '5fac5d50-966a-0138-fc18-0ac688641284172270',
            'clientSecret' => '1fb0d2a17b9d588ffd8d2f1fde35a30351f52c4ff23ed92ee45d67da35b565ce',
            'scopes' => [
                'openid',
                'email',
                'profile',
            ],
        ],
        'saml' => [
            'debug' => TRUE,
            'baseurl' => 'http://localhost:8080',
            // Service provider settings
            'sp' => [
                'entityId' => 'http://localhost:8080/metadata',
                'assertionConsumerService' => [
                    'url' => 'http://localhost:8080/saml/acs',
                    'binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST',
                ],
                'singleLogoutService' => [
                    'url' => 'http://localhost:8080/saml/slo',
                    'binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST',
                ],
                'x509cert' => 'MIIEATCCAumgAwIBAgIUSaCjkzl10YDX+puzPk+Dd8tk5EgwDQYJKoZIhvcNAQELBQAwgY8xCzAJBgNVBAYTAlZOMQ4wDAYDVQQIDAVIYW5vaTEOMAwGA1UEBwwFSGFub2kxFTATBgNVBAoMDGlXYXkgVmlldG5hbTENMAsGA1UECwwEaVdheTEVMBMGA1UEAwwMaVdheSBWaWV0bmFtMSMwIQYJKoZIhvcNAQkBFhRpd2F5QGl3YXl2aWV0bmFtLmNvbTAeFw0yMDA3MDMwNjQ2NDFaFw00NzExMTkwNjQ2NDFaMIGPMQswCQYDVQQGEwJWTjEOMAwGA1UECAwFSGFub2kxDjAMBgNVBAcMBUhhbm9pMRUwEwYDVQQKDAxpV2F5IFZpZXRuYW0xDTALBgNVBAsMBGlXYXkxFTATBgNVBAMMDGlXYXkgVmlldG5hbTEjMCEGCSqGSIb3DQEJARYUaXdheUBpd2F5dmlldG5hbS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDV2GsiCFAYPQEo/SHWDq83OTETmiTHAkNJ3NfQwQhJis57VTjzkeENhg3EXxXaWxylVysz4BeNGH53S3Wut88cntQBc7YklFPpOvU03WFesYt7meLrKX+1JZBGyoxsSx5ZnEVveNsCl3D0E9nxjE5R1qGYaEuAZR9gcWbfdIOJ6AnX6yx2XwpupLIwvALh6/JshysnswohcabsO6f+NYJW51kwWIGh+ZfxVSvqKZwjz5POiOKWyJihX31oci4F3Z7JGTxLQQrI4GQn3PlpsFVY6BtjBSIjuZBuRBmSh9rAdrBEbW8fW5oxwub+RPMCQMyfCFkLWsdznHHKfHxfHpDfAgMBAAGjUzBRMB0GA1UdDgQWBBTKY1R8HosR7yt18OMbyiuFoOCvxzAfBgNVHSMEGDAWgBTKY1R8HosR7yt18OMbyiuFoOCvxzAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQC6D7l1OJTS8P++wVRH+IdqS1CSFWd662mviTBjFxbvGQlRz3KNKoGk2jX3rKwM2aRwM+TKC/7xWDZayYkcJtFwxuSXF9ju4uhYedcAD1GhVe9bwjh5HxJmDpsOgC7RbbrmSZxxHOAKtJhsCl1C7s2JBditGYr7fvDxty5q0qCDIwYYa+KHdE4+1qFCsKtdG9n6pkYs8nnsZIvOASuCL3TWyfGRVMUsTIYziQCYIeetyHgZOdmKKO4XgtI8VnSTZDFTC6fH+rdRl8cqZwIdvQcPNZqP++3gN7Z4uhFT5wHa0LmZLLKAdqdNFxRsV0SSDs9SkXcUUJq0M1o0FiShUuRM',
                'privateKey' => 'MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDV2GsiCFAYPQEo/SHWDq83OTETmiTHAkNJ3NfQwQhJis57VTjzkeENhg3EXxXaWxylVysz4BeNGH53S3Wut88cntQBc7YklFPpOvU03WFesYt7meLrKX+1JZBGyoxsSx5ZnEVveNsCl3D0E9nxjE5R1qGYaEuAZR9gcWbfdIOJ6AnX6yx2XwpupLIwvALh6/JshysnswohcabsO6f+NYJW51kwWIGh+ZfxVSvqKZwjz5POiOKWyJihX31oci4F3Z7JGTxLQQrI4GQn3PlpsFVY6BtjBSIjuZBuRBmSh9rAdrBEbW8fW5oxwub+RPMCQMyfCFkLWsdznHHKfHxfHpDfAgMBAAECggEBAKBeWJmBGm/S8BfJHaLmCEilv0QwCPylmci+sap/2LMuQYMEel1PPTNjINfvZvgF9AmawW950q+hetYD5RFODygrhFpCaIouIm96I4Gts+PLygq7cQ1iZb93mCc+puhfvTb+lqPuQnBDGhYw/PTEf8en6i/dpyF2tH0jDj7tpee4mRd2Uvo/4ecKL7F0XE6roC3rAfZ02IUGHWLnso5nEePwkIZjtjGJz4cKbqXXdqhmoaWpIv0zWh4D9fA3QRlSI1woxeng1Op7Z8vyrpsI1+OcQprr+XwFp7sIaKzEIbtaMysMHckAN7kfs1dGGYKSpHMwIcEHnfGzeGzyHbaeVsECgYEA673E/TMvBSari3g9nrs47MExqH13P/Z+/c7TGl746lgSkWIrhDYbfjhVH99CM8REh6RelHTOnH3HT8dZmWcpbXAV3y5TrHH0YGnphuNjVRVPdno0RTs44WbIR8c4CqizLpMd0HmGKflKlBbGkAS0RgoyjNqK/CEQc8uQDIYnjhcCgYEA6DjyRZg4pUITk6CTJ5FlVZWbeZ1tHI+BPjR87BkSTA6R1cqHQ9urFz0lhT0QTeSBbpEovTvXZv6p+Lps2fS3AH+ZUs6CvbnS+tlLYPOlWN+BBsvyJ7oQ3QeGTRGliTb5lMq2b64IRaXLXq5wnFMuG0Aol1N+sFWPgJEWwoRf2HkCgYBS33ijxl2OQwTlV9TZu1mMqXIKmGjWnJISKaY+zApXX7TPVSKVLOxHWAhc1wCP8DxjLt5s9A86JmFt41fVPNXseg3GUqPpqHnPdww4djGSVJaFwPolKGebnaVkzl0eKiYrhl5hzobitJeKLeOJ941kXetdZuqbLgh5nKM8M+Et6wKBgGCike70EV5zyMG+KH5musyKnVBWenlQHnBMV0/Ifxy3U2isBu3uE5xwDZUMfCRt2o6wSUu2A0UQ5JpkubdZZm4Gqz8SQMXC9lD866lEuqHpIEA7zmFJxIq1/uVDWqdnASUx1glTWo3hGGgmHyZuVtlwMXQHgsjOUhEmT1pSy6/BAoGARbBWzX+Z0GSC/M6YnVn8l5HWpvZvEFztBMD8aY3ICCUB2H9g6JVo9BnJfWT2b/xVMm8OiWCFujZ1ac8J1SOYCWRVSw9pxXOybaukT81YnY5oXsSYgmIPIJZaVWj5pZ0EJi4y1iK/vEzXuSG1cCM7h5sws2dnPr325LaAsYZ9i4Y=',
            ],
            // Identity provider settings
            'idp' => [
                'entityId' => 'http://www.simplesaml.vn/saml2/idp/metadata.php',
                'singleSignOnService' => [
                    'url' => 'http://www.simplesaml.vn/saml2/idp/SSOService.php',
                    'binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST',
                ],
                'singleLogoutService' => [
                    'url' => 'http://www.simplesaml.vn/saml2/idp/SingleLogoutService.php',
                    'binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST',
                ],
                'x509cert' => 'MIIFFzCCA3+gAwIBAgIUfdMOq2XRa2PWcmAVhM3hHqVv9n8wDQYJKoZIhvcNAQELBQAwgZoxCzAJBgNVBAYTAlZOMQ4wDAYDVQQIDAVIYW5vaTEOMAwGA1UEBwwFSGFub2kxFTATBgNVBAoMDGlXYXkgVmlldG5hbTEUMBIGA1UECwwLU2ltcGxlIFNBTUwxGjAYBgNVBAMMEXd3dy5zaW1wbGVzYW1sLnZuMSIwIAYJKoZIhvcNAQkBFhNhZG1pbkBzaW1wbGVzYW1sLnZuMB4XDTIwMDYyMzA3MzgyNFoXDTMwMDYyMzA3MzgyNFowgZoxCzAJBgNVBAYTAlZOMQ4wDAYDVQQIDAVIYW5vaTEOMAwGA1UEBwwFSGFub2kxFTATBgNVBAoMDGlXYXkgVmlldG5hbTEUMBIGA1UECwwLU2ltcGxlIFNBTUwxGjAYBgNVBAMMEXd3dy5zaW1wbGVzYW1sLnZuMSIwIAYJKoZIhvcNAQkBFhNhZG1pbkBzaW1wbGVzYW1sLnZuMIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAz6o7YnwnaUvXdrVOO44mG9Bif7RlscqqaECP6X13dYG01khIrsMeQD0PaHr6PxfuocagNzFmjFRPUgwHBTOGrnKEyVGC1V+B07AQkDGnSPh1qFbqxWh4AN6xl6p2i1sQEGkqvpjreeFz+uHY2wNSC6HgF6x/WdDCoiG3OsGBDLa3PHgulW+eGddv74HP1ElaniN1/pkn65PziZUGWXDxko4JUy49+S+gxzWlIwpwOeHdGcYa3M4zdPE5gm+rSq/XdXP/guavjHEZfv87aWfGDqaDouv9K8ZgdgLdmItSSb7l/K1maunlulDRy1knPPKzw50IqctyYKNM45n8LxyfBvjrYvg6bzP51iiw3FRNSnyBhb9jtGUEWymgmN97rpZiVHwfq2P6cYtD8yiutTs8m6/oLMzbIKIip5yaoyHnc9SS3FgccYGEmN+8+n8KSahQednTTA3glXa101z9CuU88rE18Mdnuixkm9Zd1mU4Mg88udFULHhfsrM1drD6eRnfAgMBAAGjUzBRMB0GA1UdDgQWBBRSLMfNlPWM1ANqgdjH3lAft1ovdDAfBgNVHSMEGDAWgBRSLMfNlPWM1ANqgdjH3lAft1ovdDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBgQBfh+DVBHHKG7Xd7YMil+3royQN8punqe4TzPSUeKSrlw0Fa8lzRJ9OkDgn0VI32NEfCN6D26ctKfNl2qyvXjtAvkyuPTMvLdZN/WWYEGTJwtjNUVUjq/ISdODQ7as8icilDwdHs+SBdi5Pnfc4ouRCcCNRXv7QRFH1P3oOpMVkqagRgUJwwq10KwoPm6pHXrVmf3vlEXe3oabwloMozQy77HHnEpwGVIpD1zp/XYdC+1EPW8QGjQpFrCD18zlotCEwRn+LkJFF4usqfYk0qvSnhS7XrDTVyMP9KLdeuSqETbQU2Cd3p1eUF/fU79p1G4leM0UmFNp5p86xPtXxHVM/yK1MLrqWEbY0ID61QuOYVdQmHc51Sh8qkwHh4/GkfEWIY44n6bhL4+PdIuBCBav6xWtumqDLEA0mYFVK8Zuqc+HOmJaBwv6ZKdQGp9bnxndgz42+CYtejCBb1mI7fZq5VNzW+leIpOp5+DlRYOdXWCdzD5fhgc7icj/KpVzXIbk=',
            ],
        ],
        'cas' => [
            'serverHost' => 'sso.quangnam.gov.vn',
            'serverPort' => 443,
            'context' => '/cas',
            'version' => '3.0',
        ],
    ],
    'zimbra' => [
        'domain' => 'quangnam.gov.vn',
        'preauthKey' => '749a8ffa97b5acf3f1ef7d9c384baae196b0af4e33a3acb3d87f2e3cdd75425d',
        'serverUrl' => 'https://mail.quangnam.gov.vn',
        'adminSoapUrl' => 'https://mail.quangnam.gov.vn:7071/service/admin/soap',
        'adminUser' => 'admin',
        'adminPassword' => '',
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
