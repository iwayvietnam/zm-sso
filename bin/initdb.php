<?php
/*
    Mail mass delivery sanner script for Postfix MTA.
    Copyright (C) 2010 iWay Vietnam

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
if (php_sapi_name() !== 'cli') {
    exit('Access deny.');
}

set_time_limit(0);
ini_set('memory_limit', '-1');

require __DIR__ . '/../vendor/autoload.php';

use Application\Init;

$initDb = new Init(require __DIR__ . '/../config/config.php');
$initDb->init();
