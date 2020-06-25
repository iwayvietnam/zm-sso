<?php declare(strict_types=1);

namespace Application\SSO;

use Application\Zimbra\AccountSelector;
use Application\Zimbra\KeyValuePair;
use Application\Zimbra\SoapApi;
use Laminas\Db\Adapter\AdapterInterface;
use Laminas\Db\TableGateway\TableGateway;
use Laminas\Db\TableGateway\Feature\RowGatewayFeature;

/**
 * Base single logout class
 */
abstract class BaseSingleLogout implements SingleLogoutInterface
{
    private $_adapter;
    private $_api;

    protected $_protocol;
    protected $_settings = [];

    public function __construct(AdapterInterface $adapter, SoapApi $api, array $settings = [])
    {
        $this->_adapter = $adapter;
        $this->_api = $api;
        $this->_settings = $settings;
    }

    protected function doLogout($sessionId)
    {
        $hashedSessionId = hash('sha256', $sessionId);
        $table = new TableGateway('sso_login', $this->_adapter, , new RowGatewayFeature('id'));
        $rowset = $table->select(
            'session_id' => $hashedSessionId,
            'protocol' => $this->_protocol,
        );
        if ($rowset->count()) {
            $row = $rowset->current();
            if (!empty($row['user_name'])) {
                $this->_api->authByName(
                    $this->_settings['zimbra']['admin_user'],
                    $this->_settings['zimbra']['admin_password']
                );
                $account = $this->_api->getAccount(new AccountSelector($row['user_name']), 'zimbraAuthTokenValidityValue');
                if (!empty($account->a)) {
                    $zimbraAuthTokenValidityValue = 0;
                    foreach ($account->a as $attr) {
                        if ($attr->n === 'zimbraAuthTokenValidityValue') {
                            $zimbraAuthTokenValidityValue = (int) $attr->_content;
                        }
                    }
                    $this->_api->modifyAccount(
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
