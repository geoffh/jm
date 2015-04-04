package jmusic.oldneedsrewrite.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.logging.Logger;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

public class RequestListener {
    private final RequestHandlerRegistry mRequestHandlerRegistry =
        new RequestHandlerRegistry();
    private final Logger mLogger = Logger.getLogger( RequestListener.class.getName() );
    private RequestListenerThread mRequestListenerThread;


    void addDocRoot( String inDocRoot ) {
        String theDocRoot = urlToPath( inDocRoot );
        String thePattern = theDocRoot + "*";
        mRequestHandlerRegistry.register( thePattern,
                                          new RequestHandler( theDocRoot ) );
        mLogger.finest( "Added DocRoot '" + theDocRoot + "'" );
        synchronized( mRequestHandlerRegistry ) {
            if ( mRequestHandlerRegistry.hasHandlers() &&
                 mRequestListenerThread == null ) {
                startListenerThread();
            }
        }
    }

    String getURL( String inFile ) {
        String theURL = null;
        String thePath = urlToPath( inFile );
        synchronized( mRequestHandlerRegistry ) {
            RequestHandler theHandler = mRequestHandlerRegistry.getRequestHandler( thePath );
            if ( theHandler != null && mRequestListenerThread != null ) {
                theURL =
                    mRequestListenerThread.getURLPrefix() +
                    thePath.replace( " ", "%20");
            }
        }
        return theURL;
    }

    void removeDocRoot( String inDocRoot ) {
        String thePattern = inDocRoot + "*";
        mRequestHandlerRegistry.unregister( thePattern );
        mLogger.finest( "Removed DocRoot '" + thePattern + "'" );
        synchronized( mRequestHandlerRegistry ) {
            if ( ! mRequestHandlerRegistry.hasHandlers() &&
                 mRequestListenerThread != null ) {
                stopListenerThread();
            }
        }
    }

    private void startListenerThread() {
        try {
            mRequestListenerThread = new RequestListenerThread( mRequestHandlerRegistry );
            mRequestListenerThread.setDaemon( true );
            mRequestListenerThread.start();
        } catch( Exception theException ) {
            mLogger.throwing( "RequestListener", "startListenerThread", theException );
        }
    }

    private void stopListenerThread() {
        mRequestListenerThread.shutdown();
        mRequestListenerThread = null;
    }
    
    private String urlToPath( String inUri ) {
        try {
            return new URI( inUri ).getPath();
        } catch( Exception theException ) {
            theException.printStackTrace();
            return null;
        }
    }

    class RequestListenerThread extends Thread {
        private final ServerSocket mServerSocket = new ServerSocket( 0 );
        private final HttpParams mParams = new SyncBasicHttpParams();
        private final HttpService mHttpService;
        private final String mURLPrefix =
            "http://"                +
            IPAddress.getIPAddress() +
            ":"                      +
            mServerSocket.getLocalPort();

        RequestListenerThread( RequestHandlerRegistry inRegistry ) throws Exception {
            mLogger.finest( "Creating new RequestListenerThread" );
            setName( "RequestListenerThread" );
            setDaemon( true );
            mParams
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1")
                .setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
            HttpProcessor theProcessor = new ImmutableHttpProcessor(
                    new HttpResponseInterceptor[] {
                        new ResponseDate(),
                        new ResponseServer(),
                        new ResponseContent(),
                        new ResponseConnControl() } );
            mHttpService = new HttpService(
                    theProcessor,
                    new DefaultConnectionReuseStrategy(),
                    new DefaultHttpResponseFactory(),
                    inRegistry,
                    mParams );
        }

        @Override
        public void run() {
            mLogger.finest( "RequestListenerThread starting on port '" +
                            mServerSocket.getLocalPort() + "'" );
            while ( ! interrupted() ) {
                try {
                    Socket theSocket = mServerSocket.accept();
                    DefaultHttpServerConnection theConnection = new DefaultHttpServerConnection();
                    mLogger.finest( "Incoming connection from '" +
                                    theSocket.getInetAddress() + "'" );
                    theConnection.bind( theSocket, mParams );
                    Thread theWorkerThread = new RequestWorkerThread( mHttpService, theConnection );
                    theWorkerThread.setDaemon( true );
                    theWorkerThread.start();
                } catch ( Exception theException ) {
                    if ( ! interrupted() ) {
                        mLogger.throwing( "RequestListenerThread", "run", theException );
                    }
                    try {
                        mServerSocket.close();
                    } catch( Exception theIgnore ) {}
                    break;
                }
            }
            mLogger.finest( "RequestListenerThread stopping" );
        }

        void shutdown() {
            interrupt();
            try {
                mServerSocket.close();
            } catch( Exception theIgnore ) {}
        }

        String getURLPrefix() { return mURLPrefix; }
    }
}