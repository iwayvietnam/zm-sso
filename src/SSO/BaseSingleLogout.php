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
    private $adapter;
    private $api;

    protected $protocol;
    protected $settings = [];

    public function __construct(AdapterInterface $adapter, SoapApi $api, array $settings = [])
    {
        $this->adapter = $adapter;
        $this->api = $api;
        $this->settings = $settings;
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
                $this->api->authByName(
                    $this->settings['zimbra']['admin_user'],
                    $this->settings['zimbra']['admin_password']
                );
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
