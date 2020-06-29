<?php declare(strict_types=1);

namespace Application\SSO;

use Application\Zimbra\AccountSelector;
use Application\Zimbra\KeyValuePair;
use Application\Zimbra\SoapApi;
use Laminas\Db\Adapter\AdapterInterface as Adapter;
use Laminas\Db\TableGateway\TableGateway;
use Laminas\Db\TableGateway\Feature\RowGatewayFeature;
use Psr\Log\LoggerInterface as Logger;

/**
 * Base single logout class
 */
abstract class BaseSingleLogout implements SingleLogoutInterface
{
    protected $adapter;
    protected $logger;
    protected $api;

    protected $protocol;

    public function __construct(Adapter $adapter, Logger $logger, SoapApi $api)
    {
        $this->adapter = $adapter;
        $this->logger = $logger;
        $this->api = $api;
    }

    protected function doLogout($sessionId)
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
}
