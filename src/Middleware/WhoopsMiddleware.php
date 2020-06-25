<?php
declare(strict_types=1);

namespace Application\Middleware;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Http\Server\MiddlewareInterface as Middleware;
use Psr\Http\Server\RequestHandlerInterface as Handler;

use Slim\Psr7\Response as HtmlResponse;

use Whoops\Handler\JsonResponseHandler;
use Whoops\Handler\PlainTextHandler;
use Whoops\Handler\PrettyPageHandler;
use Whoops\Handler\XmlResponseHandler;
use Whoops\Run;

/**
 * Whoops middleware
 */
class WhoopsMiddleware implements Middleware
{
    private static $_formats = [
        'html' => ['text/html', 'application/xhtml+xml'],
        'json' => ['application/json', 'text/json', 'application/x-json'],
        'xml'  => ['text/xml', 'application/xml', 'application/x-xml'],
        'txt'  => ['text/plain']
    ];

    /**
     * {@inheritdoc}
     */
    public function process(Request $request, Handler $handler) : Response
    {
        try {
            return $handler->handle($request);
        }
        catch (\Exception $e) {
            return self::whoopsHandle($e, $request);
        }
    }

    private static function whoopsHandle($error, Request $request) : Response
    {
        $method = Run::EXCEPTION_HANDLER;

        $whoops = self::whoopsInstance($request);

        // Output is managed by the middleware pipeline
        $whoops->allowQuit(false);
        
        ob_start();
        $whoops->$method($error);
        $response = ob_get_clean();

        return new HtmlResponse($response, 500);
    }

    private static function whoopsInstance(Request $request)
    {
        $whoops = new Run();
        if (php_sapi_name() === 'cli') {
            $whoops->pushHandler(new PlainTextHandler);
            return $whoops;
        }

        $format = self::preferredFormat($request);
        switch ($format) {
            case 'json':
                $handler = new JsonResponseHandler;
                $handler->addTraceToOutput(true);
                break;
            case 'html':
                $handler = new PrettyPageHandler;
                break;
            case 'txt':
                $handler = new PlainTextHandler;
                $handler->addTraceToOutput(true);
                break;
            case 'xml':
                $handler = new XmlResponseHandler;
                $handler->addTraceToOutput(true);
                break;
            default:
                if (empty($format)) {
                    $handler = new PrettyPageHandler;
                }
                else {
                    $handler = new PlainTextHandler;
                    $handler->addTraceToOutput(true);
                }
        }

        $whoops->pushHandler($handler);
        return $whoops;

    }

    public static function preferredFormat(Request $request)
    {
        $acceptTypes = $request->getHeader('accept');

        if (count($acceptTypes) > 0) {
            $acceptType = $acceptTypes[0];

            $counters = [];
            foreach (self::$_formats as $format => $values) {
                foreach ($values as $value) {
                    $counters[$format] = isset($counters[$format]) ? $counters[$format] : 0;
                    $counters[$format] += intval(strpos($acceptType, $value) !== false);
                }
            }

            // Sort the array to retrieve the format that best matches the Accept header
            asort($counters);
            end($counters);
            return key($counters);
        }

        return 'html';
    }
}
