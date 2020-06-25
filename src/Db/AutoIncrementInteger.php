<?php declare(strict_types=1);

namespace Application\Db;

use Laminas\Db\Sql\Ddl\Column;

/**
 * AutoIncrementInteger class.
 * 
 * @author    Nguyen Van Nguyen - nguyennv1981@gmail.com
 * @copyright Copyright © 2020 by iWay Vietnam.
 */
class AutoIncrementInteger extends Column\Integer
{
    /**
     * @var string
     */
    protected $specification = '%s %s AUTO_INCREMENT';
}
