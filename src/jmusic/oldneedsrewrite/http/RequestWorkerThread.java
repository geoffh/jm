package jmusic.oldneedsrewrite.http;

import java.util.logging.Logger;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;

class RequestWorkerThread extends Thread {
    private final HttpService mHttpService;
    private final HttpServerConnection mHttpServerConnection;
    private final Logger mLogger = Logger.getLogger( RequestWorkerThread.class.getName() );

    RequestWorkerThread( HttpService inHttpService,
                         HttpServerConnection inHttpServerConnection ) {
            super();
            setName( "RequestWorkerThread" );
            mHttpService = inHttpService;
            mHttpServerConnection = inHttpServerConnection;
        }

    @Override
        public void run() {
            HttpContext theContext = new BasicHttpContext( null );
            try {
                while ( ! interrupted() && mHttpServerConnection.isOpen() ) {
                    mHttpService.handleRequest( mHttpServerConnection, theContext );
                }
            } catch ( Exception theException ) {
                // Getting lots of Broken Pipe Socket Exceptions but they
                // don't seem to be a problem
                //mLogger.throwing( "RequestWorkerThread", "run", theException );
            } finally {
                try {
                    mHttpServerConnection.shutdown();
                } catch ( Exception theException ) {}
            }
        }
}