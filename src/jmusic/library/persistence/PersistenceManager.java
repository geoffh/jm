package jmusic.library.persistence;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class PersistenceManager {
    private static final PersistenceManager sInstance = new PersistenceManager();
    private static final Logger sLogger = Logger.getLogger( PersistenceManager.class.getName() );
    
    private final EntityManager mManager;
    private final ArrayList< PersistenceListener > mListeners = new ArrayList<>();
    
    private PersistenceManager() {
        mManager = Persistence.createEntityManagerFactory(
            System.getProperty( "user.home" ) +  "/.jmusic/db/Library.odb" )
                .createEntityManager();
        mManager.getMetamodel().managedType( PersistentRoot.class );
        mManager.getMetamodel().managedType( PersistentArtist.class );
        mManager.getMetamodel().managedType( PersistentAlbum.class );
        mManager.getMetamodel().managedType( PersistentTrack.class);
        mManager.getMetamodel().managedType( PersistentPlaylist.class);
    }
    
    public static void addListener( PersistenceListener inListener ) {
        synchronized( sInstance.mListeners ) {
            sInstance.mListeners.add( inListener );
        }
    }
    
    public static void removeListener( PersistenceListener inListener ) {
        synchronized( sInstance.mListeners ) {
            sInstance.mListeners.remove( inListener );
        }
    }
    
    static void onPostPersist( PersistentObject inObject ) {
        sLogger.log( Level.FINEST, "onPostPersist:{0}", inObject.toString());
        synchronized( sInstance.mListeners ) {
            sInstance.mListeners.stream().forEach( ( theListener ) -> {
                theListener.onPostPersist( inObject );
            } );
        }
    }

    static void onPostRemove( PersistentObject inObject ) {
        sLogger.log( Level.FINEST, "onPostRemove:{0}", inObject.toString());
        synchronized( sInstance.mListeners ) {
            sInstance.mListeners.stream().forEach( ( theListener ) -> {
                theListener.onPostRemove( inObject );
            } );
        }
    }
    
    static void onPostUpdate( PersistentObject inObject ) {
        sLogger.log( Level.FINEST, "onPostUpdate:{0}", inObject.toString());
        synchronized( sInstance.mListeners ) {
            sInstance.mListeners.stream().forEach( ( theListener ) -> {
                theListener.onPostUpdate( inObject );
            } );
        }
    }
    
    static EntityManager getEntityManager() {
        return sInstance.mManager;
    }
}