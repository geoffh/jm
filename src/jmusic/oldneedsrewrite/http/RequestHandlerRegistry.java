package jmusic.oldneedsrewrite.http;

import java.util.ArrayList;
import java.util.Map;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;

/*
 * This class exists only to allow the retrieval of RequestHandlers given a
 * file path. The docroot can subsequently be queried from the RequestHandler
 * and a URL can in turn be built from the docroot.
 * This class simply "wraps" HttpRequestHandlerRegistry and keeps a record of
 * all registered handlers.
 */
class RequestHandlerRegistry extends HttpRequestHandlerRegistry {
    private final ArrayList< RequestHandler > mRequestHandlers =
        new ArrayList<>();

    @Override
    public void register( String inPattern, HttpRequestHandler inHandler ) {
        synchronized( mRequestHandlers ) {
            super.register( inPattern, inHandler);
            mRequestHandlers.add( ( RequestHandler )inHandler );
        }
    }
    
    @Override
    public void setHandlers( Map inHandlers ) {
        synchronized( mRequestHandlers ) {
            super.setHandlers( inHandlers );
            mRequestHandlers.clear();
            mRequestHandlers.addAll( inHandlers.values() );
        }
    }

    @Override
    public void unregister( String inPattern ) {
        synchronized( mRequestHandlers ) {
            super.unregister( inPattern );
            RequestHandler theHandler = getRequestHandler( inPattern );
            if ( theHandler != null ) {
                mRequestHandlers.remove( theHandler );
            }
        }
    }

    RequestHandler getRequestHandler( String inFile ) {
        RequestHandler theRequestHandler = null;
        synchronized( mRequestHandlers ) {
            for ( RequestHandler theHandler : mRequestHandlers ) {
                if ( inFile.startsWith( theHandler.getDocRoot() ) ) {
                    theRequestHandler = theHandler;
                    break;
                }
            }
        }
        return theRequestHandler;
    }

    boolean hasHandlers() { return ! mRequestHandlers.isEmpty(); }
}