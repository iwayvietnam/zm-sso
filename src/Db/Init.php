<?php declare(strict_types=1);

namespace Application\Db;

use Laminas\Db\Adapter\Adapter;
use Laminas\Db\Sql\Ddl\{CreateTable, Column, Constraint, Index};
use Laminas\Db\Sql\Sql;
use Monolog\Handler\{StreamHandler, RotatingFileHandler};
use Monolog\Logger;

class Init {
    private $logger;
    private $adapter;
    private $sql;

    public function __construct(array $settings = [])
    {
        $this->logger = new Logger($settings['logger']['name']);
        $this->logger->pushHandler(new StreamHandler('php://stdout', $settings['logger']['level']))
            ->pushHandler(new RotatingFileHandler($settings['logger']['path'], 0, $settings['logger']['level']));
        $this->adapter = new Adapter($settings['db']);
        $this->sql = new Sql($this->adapter);
    }

    public function init()
    {
        $tables = [];

        // Define sso login table
        if (!Utils::tableExists('sso_login', $this->adapter)) {
            $this->logger->info('Define sso login data table schema');

            $ssoTable = new CreateTable('sso_login');
            $ssoTable->addColumn(new AutoIncrementInteger('id'))
                ->addColumn(new Column\Varchar('user_name', 255))
                ->addColumn(new Column\Varchar('session_id', 255))
                ->addColumn(new Column\Varchar('protocol', 45))
                ->addColumn(new Column\Varchar('ip', 45))
                ->addColumn(new Column\Text('data', NULL, TRUE))
                ->addColumn(new Column\Integer('logout_time', FALSE, 0))
                ->addColumn(new Column\Integer('created', FALSE, 0));

            $ssoTable->addConstraint(
                new Constraint\PrimaryKey('id')
            )->addConstraint(
                new Constraint\UniqueKey(['session_id', 'protocol'], 'sso_login_unique')
            )->addConstraint(
                new Index\Index(['user_name'], 'sso_login_user_name')
            )->addConstraint(
                new Index\Index(['session_id'], 'sso_login_session_id')
            )->addConstraint(
                new Index\Index(['protocol'], 'sso_login_protocol')
            )->addConstraint(
                new Index\Index(['ip'], 'sso_login_ip')
            )->addConstraint(
                new Index\Index(['created'], 'sso_login_created')
            );

            $tables['sso_login'] = $ssoTable;
        }

        $total = 0;
        try {
            if (!empty($tables)) {
                foreach ($tables as $name => $table) {
                    $this->logger->info('Create ' . $name . ' table');
                    $this->adapter->query(
                        $this->sql->getSqlStringForSqlObject($table),
                        Adapter::QUERY_MODE_EXECUTE
                    );
                    $this->logger->info('Create ' . $name . ' table completed');
                    $total++;
                }
            }
        }
        catch (\Exception $ex) {
            $this->logger->err($ex->getMessage());
        };

        $this->logger->info('Created ' . $total . ' table(s)');
    }
}
