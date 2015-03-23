package jmusic.library.backend;

import java.util.HashMap;
import java.util.Map;

import jmusic.library.backend.cd.CDBackend;
import jmusic.library.backend.file.FileBackend;

public class BackendFactory {
    private static final Map< String, Backend > sBackends = new HashMap<>();
    
    public static Backend getBackend( String inUri ) {
        synchronized( sBackends ) {
            Backend theBackend = sBackends.get( inUri );
            if ( theBackend != null ) {
                return theBackend;
            }
            for ( Backend theExistingBackend : sBackends.values() ) {
                if ( theExistingBackend.canHandleUri( inUri ) ) {
                    return theExistingBackend;
                }
            }
            theBackend = createBackend( inUri );
            if ( theBackend != null ) {
                sBackends.put( inUri, theBackend );
            }
            return theBackend;
        }
    }

    private static Backend createBackend( String inUri ) {
        Backend theBackend = null;
        if ( inUri.startsWith( CDBackend.sUriScheme ) ) {
            theBackend = new CDBackend();
        } else {
            theBackend = new FileBackend( inUri );
        }
        return theBackend;
    }
}