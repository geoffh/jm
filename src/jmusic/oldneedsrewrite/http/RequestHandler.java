package jmusic.oldneedsrewrite.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.logging.Logger;

import jmusic.mp3encode.FileMP3Encoder;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

class RequestHandler implements HttpRequestHandler {
    private static final String sMsgPrefix = "<html><body><h1>";
    private static final String sMsgSuffix = "</h1></body></html>";
    private static final String sMsgNotFound = sMsgPrefix + "File not found" + sMsgSuffix;
    private static final String sMsgAccessDenied = sMsgPrefix + "Access denied" + sMsgSuffix;
    private final String mDocRoot;
    private final Logger mLogger = Logger.getLogger( RequestHandler.class.getName() );
    
    RequestHandler( String inDocRoot ) {
        mDocRoot = inDocRoot;
        mLogger.finest( "RequestHandler created for DocRoot '" + mDocRoot + "'" );
    }

    public void handle( HttpRequest  inRequest,
                        HttpResponse inResponse,
                        HttpContext  inContext )
        throws HttpException, IOException {
        ensureMethodSupported( inRequest );
        String theUri = URLDecoder.decode( inRequest.getRequestLine().getUri() );
        File theFile = new File( theUri );
        if ( ! theFile.exists() ) {
            sendNotFound( inResponse );
        } else if (!theFile.canRead() || theFile.isDirectory()) {
            sendAccessDenied( inResponse );
        } else {
            sendFile( inResponse, theUri );
        }
    }
    
    String getDocRoot() { return mDocRoot; }

    private void ensureMethodSupported( HttpRequest inRequest )
        throws MethodNotSupportedException {
        String theMethod =
           inRequest.getRequestLine().getMethod().toUpperCase( Locale.ENGLISH );
        if ( ! theMethod.equals( "GET" )  &&
             ! theMethod.equals( "HEAD" ) &&
             ! theMethod.equals( "POST" ) ) {
            throw new MethodNotSupportedException(
                theMethod + " method not supported");
        }
    }

    private void sendAccessDenied( HttpResponse inResponse ) {
        sendError( inResponse, HttpStatus.SC_FORBIDDEN, sMsgAccessDenied );
    }

    private void sendError( HttpResponse inResponse, int inStatus, final String inMsg ) {
        inResponse.setStatusCode( inStatus );
        EntityTemplate theBody = new EntityTemplate( new ContentProducer() {
            @Override
            public void writeTo( OutputStream theOStream ) throws IOException {
                OutputStreamWriter theWriter = new OutputStreamWriter( theOStream, "UTF-8");
                theWriter.write( inMsg );
                theWriter.flush();
            }
        } );
        theBody.setContentType( "text/html; charset=UTF-8" );
        inResponse.setEntity( theBody );
    }

    private void sendFile( HttpResponse inResponse, File inFile ) {
        inResponse.setStatusCode( HttpStatus.SC_OK );
        inResponse.setEntity( new FileEntity( inFile, "audio/mpeg" ) );
    }

    private void sendFile( HttpResponse inResponse, String inUri ) throws IOException {
        inResponse.setStatusCode( HttpStatus.SC_OK );
        FileMP3Encoder theEncoder = new FileMP3Encoder();
        inResponse.setEntity( new InputStreamEntity( theEncoder.getInputStream( inUri, null ), theEncoder.getContentLength( inUri ) ) );
    }

    private void sendNotFound( HttpResponse inResponse ) {
        sendError( inResponse, HttpStatus.SC_NOT_FOUND, sMsgNotFound );
    }
}