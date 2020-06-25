<?php declare(strict_types=1);

namespace Application\SSO;

use Application\Zimbra\PreAuth;
use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Laminas\Db\TableGateway\TableGateway;
use Laminas\Db\TableGateway\Feature\RowGatewayFeature;
use Mezzio\Session\SessionInterface;
use Psr\Log\LoggerInterface as Logger;

/**
 * Base auth class
 */
abstract class BaseAuth implements AuthInterface
{
    protected $adapter;
    protected $logger;

    protected $protocol;
    protected $userName;
    protected $settings = [];

    public function __construct(Adapter $adapter, Logger $logger, array $settings = [])
    {
        $this->adapter = $adapter;
        $this->logger = $logger;
        $this->settings = $settings;
    }

    public function getUserName(): string
    {
        return $this->userName;
    }

    protected function saveSsoLogin($sessionId, array $data = []): void
    {
        $this->logger->debug('Save sso session for %user_name% with id: %session_id%', [
            'user_name' => $this->userName,
            'session_id' => $hashedSessionId,
        ]);
        $hashedSessionId = hash('sha256', $sessionId);
        $table = new TableGateway('sso_login', $this->adapter, new RowGatewayFeature('id'));
        $rowset = $table->select([
            'session_id' => $hashedSessionId,
            'protocol' => $this->protocol,
        ]);
        if ($rowset->count() == 0 && !empty($this->userName)) {
            $insert = [
                'user_name' => $this->userName,
                'session_id' => $hashedSessionId,
                'protocol' => $this->protocol,
                'ip' => self::remoteIp(),
                'created' => time(),
            ];
            if (!empty($data)) {
                $insert['data'] = json_encode($data);
            }
            $table->insert($insert);
        }
    }

    protected static function remoteIp(): string
    {
        static $remoteIp;
        if (empty($remoteIp)) {
            if (isset($_SERVER)) {
                if (isset($_SERVER['HTTP_CLIENT_IP'])) {
                    $remoteIp = $_SERVER['HTTP_CLIENT_IP'];
                }
                elseif (isset($_SERVER['HTTP_FORWARDED_FOR'])) {
                    $remoteIp = $_SERVER['HTTP_FORWARDED_FOR'];
                }
                elseif (isset($_SERVER['HTTP_X_FORWARDED_FOR'])) {
                    $remoteIp = $_SERVER['HTTP_X_FORWARDED_FOR'];
                }
                else {
                    $remoteIp = $_SERVER['REMOTE_ADDR'];
                }
            }
            else {
                if (getenv('HTTP_CLIENT_IP')) {
                    $remoteIp = getenv('HTTP_CLIENT_IP');
                }
                elseif (getenv('HTTP_FORWARDED_FOR')) {
                    $remoteIp = getenv('HTTP_FORWARDED_FOR');
                }
                elseif (getenv('HTTP_X_FORWARDED_FOR')) {
                    $remoteIp = getenv('HTTP_X_FORWARDED_FOR');
                }
                else {
                    $remoteIp = getenv('REMOTE_ADDR');
                }
            }
        }
        return $remoteIp;
    }
}
