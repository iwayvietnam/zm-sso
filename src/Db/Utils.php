<?php

namespace Application\Db;

use Laminas\Db\Adapter\{Adapter, AdapterInterface};

/**
 * Utils class.
 * 
 * @author    Nguyen Van Nguyen - nguyennv1981@gmail.com
 * @copyright Copyright © 2017 by iWay Vietnam.
 */
class Utils {
    /**
     * Check table exists
     * @param  string $table
     * @param  Adapter $adapter
     * @return bool
     */
    public static function tableExists($table, AdapterInterface $adapter)
    {
        $exists = [];
        if (empty($exists[$table])) {
            $result = $adapter->query(
                "SHOW TABLES LIKE '" . $table . "';",
                Adapter::QUERY_MODE_EXECUTE
            );
            $exists[$table] = $result->count() > 0;
        }
        return $exists[$table];
    }

    /**
     * Check index exists
     * @param  string $table
     * @param  string $index
     * @param  Adapter $adapter
     * @return bool
     */
    public static function indexExists($table, $index, AdapterInterface $adapter)
    {
        $exists = [];
        if (empty($exists[$table . $index])) {
            $result = $adapter->query(
                "SHOW INDEX FROM " . $table . " WHERE key_name = '" . $index . "';",
                Adapter::QUERY_MODE_EXECUTE
            );
            $exists[$table . $index] = $result->count() > 0;
        }
        return $exists[$table . $index];
    }

    /**
     * Check column exists
     * @param  string $table
     * @param  string $column
     * @param  Adapter $adapter
     * @return bool
     */
    public static function columnExists($table, $column, AdapterInterface $adapter)
    {
        return in_array($column, static::listColumns($table, $adapter));
    }

    /**
     * List columns
     * @param  string $table
     * @param  Adapter $adapter
     * @return array
     */
    public static function listColumns($table, AdapterInterface $adapter)
    {
        $columns = [];
        if (empty($columns[$table])) {
            $columns[$table] = [];
            $result = $adapter->query(
                'SHOW COLUMNS FROM ' . $table,
                Adapter::QUERY_MODE_EXECUTE
            );
            foreach ($result as $row) {
                $columns[$table][] = $row->Field;
            }
        }
        return $columns[$table];
    }

    /**
     * Get column info
     * @param  string $table
     * @param  string $column
     * @param  Adapter $adapter
     * @return mixed
     */
    public static function getColumn($table, $column, AdapterInterface $adapter)
    {
        $columns = [];
        if (empty($columns[$table . $column])) {
            $result = $adapter->query(
                'SHOW COLUMNS FROM ' . $table . ' WHERE `Field` = "' . $column . '"',
                Adapter::QUERY_MODE_EXECUTE
            );
            if ($result->count() > 0) {
                $columns[$table . $column] = $result->current();
            }
        }
        return !empty($columns[$table . $column]) ? $columns[$table . $column] : NULL;
    }
}
