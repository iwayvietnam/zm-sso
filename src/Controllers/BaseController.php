<?php declare(strict_types=1);

namespace Application\Controllers;

use Application\Zimbra\AccountSelector;
use Application\Zimbra\KeyValuePair;
use Application\Zimbra\SoapApi;

use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Laminas\Db\TableGateway\TableGateway;
use Laminas\Db\TableGateway\Feature\RowGatewayFeature;

use Psr\Log\LoggerInterface as Logger;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

abstract class BaseController {

    /**
     * @var Adapter
     */
    protected $adapter;

    /**
     * @var Logger
     */
    protected $logger;

    /**
     * @var SoapApi
     */
    protected $api;

    protected $protocol;

    public function __construct(Adapter $adapter, Logger $logger, SoapApi $api)
    {
        $this->adapter = $adapter;
        $this->logger = $logger;
        $this->api = $api;
    }

    protected function saveSsoLogin($sessionId, $userName, array $data = []): void
    {
        $hashedSessionId = hash('sha256', $sessionId);
        $table = new TableGateway('sso_login', $this->adapter, new RowGatewayFeature('id'));
        $rowset = $table->select([
            'session_id' => $hashedSessionId,
            'protocol' => $this->protocol,
        ]);
        if ($rowset->count() == 0 && !empty($userName)) {
            $this->logger->debug('save sso session login for {user_name} with id: {session_id}', [
                'user_name' => $userName,
                'session_id' => $hashedSessionId,
            ]);
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

    protected function saveSsoLogout($sessionId)
    {
        $hashedSessionId = hash('sha256', $sessionId);
        $table = new TableGateway('sso_login', $this->adapter, new RowGatewayFeature('id'));
        $rowset = $table->select([
            'session_id' => $hashedSessionId,
            'protocol' => $this->protocol,
        ]);
        if ($rowset->count()) {
            $this->logger->debug('save sso session logout for {user_name} with id: {session_id}', [
                'user_name' => $row['user_name'],
                'session_id' => $hashedSessionId,
            ]);
            $row = $rowset->current();
            $row['logout_time'] = time();
            $row->save();
        }
    }

    protected function zimbraLogout($sessionId)
    {
        $hashedSessionId = hash('sha256', $sessionId);
        $table = new TableGateway('sso_login', $this->adapter, new RowGatewayFeature('id'));
        $rowset = $table->select([
            'session_id' => $hashedSessionId,
            'protocol' => $this->protocol,
        ]);
        if ($rowset->count()) {
            $row = $rowset->current();
            if (!empty($row['user_name'])) {
                $account = $this->api->getAccount(new AccountSelector($row['user_name']), 'zimbraAuthTokenValidityValue');
                if (!empty($account->a)) {
                    $zimbraAuthTokenValidityValue = 0;
                    foreach ($account->a as $attr) {
                        if ($attr->n === 'zimbraAuthTokenValidityValue') {
                            $zimbraAuthTokenValidityValue = (int) $attr->_content;
                        }
                    }
                    $this->api->modifyAccount(
                        $account->id,
                        [new KeyValuePair('zimbraAuthTokenValidityValue', $zimbraAuthTokenValidityValue++)]
                    );

                    $row['logout_time'] = time();
                    $row->save();
                }
            }
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
