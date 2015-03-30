package jmusic.library;

import jmusic.library.backend.Backend;
import jmusic.library.backend.BackendFactory;
import jmusic.library.backend.cd.CDBackend;
import jmusic.library.persistence.PersistenceListener;
import jmusic.library.persistence.PersistenceManager;
import jmusic.library.persistence.PersistentAlbum;
import jmusic.library.persistence.PersistentArtist;
import jmusic.library.persistence.PersistentBrowsable;
import jmusic.library.persistence.PersistentCDRoot;
import jmusic.library.persistence.PersistentContainer;
import jmusic.library.persistence.PersistentObject;
import jmusic.library.persistence.PersistentPlaylist;
import jmusic.library.persistence.PersistentPlaylistsRoot;
import jmusic.library.persistence.PersistentRoot;
import jmusic.library.persistence.PersistentTrack;
import jmusic.util.Config;
import jmusic.util.ConfigConstants;
import jmusic.util.ConfigListener;
import jmusic.util.JMusicExecutor;
import jmusic.util.ProgressListener;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Library implements PersistenceListener, ConfigListener {
    private static final long sDefaultMonitorDelay = 5000; //20000;
    private static final String sRootOfRootsName = "Root Of Roots";
    private static final String sRootOfRootsUri = sRootOfRootsName;
    private static final PersistentRoot sRootOfRoots;
    private static final String sPlaylistsName = "Playlists";
    private static final String sPlaylistsUri = sPlaylistsName;
    private static final PersistentPlaylistsRoot sPlaylists;
    private static final String sCDName = "cd";
    private static final String sCDUri = CDBackend.sUriScheme;
    private static final PersistentRoot sCD;
    
    private final String mName;
    private final HashMap< PersistentRoot, RootRefreshTask > mRefreshTasks = new HashMap<>();
    private final Logger mLogger = Logger.getLogger( Library.class.getName() );
    private final ArrayList< LibraryListener > mListeners = new ArrayList<>();
    
    static {
        PersistentRoot theRoot = PersistentRoot.getRootForUri( sRootOfRootsUri );
        if ( theRoot == null ) {
            theRoot = new PersistentRoot();
            theRoot.setName( sRootOfRootsName );
            theRoot.setUri( sRootOfRootsUri );
        }
        sRootOfRoots = theRoot;
        theRoot = PersistentPlaylistsRoot.getPlaylistsRoot();
        if ( theRoot == null ) {
            theRoot = createRoot( sPlaylistsUri, sPlaylistsName, LibraryItem.Type.playlistsroot );
        }
        sPlaylists = ( PersistentPlaylistsRoot )theRoot;
        sRootOfRoots.commit();
        theRoot = PersistentCDRoot.getCDRoot();
        if ( theRoot == null ) {
            theRoot = createRoot( sCDUri, sCDName, LibraryItem.Type.cdroot );
        }
        sCD = theRoot;
        sRootOfRoots.commit();
    }
    
    public Library( String inName ) {
        mName = inName;
        PersistenceManager.addListener( this );
        initialiseRootRefreshTasks();
        Config.getInstance().addListener( this );
    }
    
    public void addListener( LibraryListener inListener ) {
        synchronized( mListeners ) {
            mListeners.add(  inListener );
        }
    }
    
    public LibraryItem addPlaylist( String inName ) {
        return new LibraryConverter().convert( createPlaylist( inName ) );
    }

    public LibraryItem addRoot( String inUri, String inName )
        throws LibraryException {
        ensureBackend( inUri );
        ensureRootDoesntExist( inUri );
        PersistentRoot theRoot = createRoot( inUri, inName );
        scheduleRootRefreshTask( theRoot, 0 );
        return new LibraryConverter().convert( theRoot );
    }
    
    public void addTrackToPlaylist( Long inTrackId, Long inPlaylistId ) {
        PersistentTrack theTrack = PersistentTrack.getTrackForId( inTrackId );
        if ( theTrack == null ) {
            mLogger.warning( "Track id:" + inTrackId + " doesn't exist" );
            return;
        }
        PersistentPlaylist thePlaylist = PersistentPlaylist.getPlaylistForId( inPlaylistId );
        if ( thePlaylist == null ) {
            mLogger.warning( "Playlist id:" + inPlaylistId + " doesn't exist" );
            return;
        }
        PersistentObject.beginTransaction();
        thePlaylist.addTrack( theTrack );
        thePlaylist.commit();
        theTrack.addPlaylist( thePlaylist );
        theTrack.commit();
        PersistentObject.endTransaction();
    }
    
    public LibraryBrowseResult browse( Long inId ) {
        return browse( inId, -1, -1 );
    }
    
    public LibraryBrowseResult browse(
        Long inId, int inFirstResult, int inMaxResults ) {
        LibraryBrowseResult theResult = new LibraryBrowseResult();
        PersistentBrowsable theContainer = PersistentBrowsable.getBrowsableForId( inId );
        if ( theContainer != null ) {
            theResult.mResults =
                theContainer.browse( inFirstResult, inMaxResults, new LibraryConverter() );
            theResult.mMaxResults = theContainer.getMaxBrowseResults();
        } else {
            mLogger.warning( "id " + inId + " is not a valid container id" );
            theResult.mMaxResults = -1;
        }
        return theResult;
    }

    public void findBrokenTracks( Long inRootId, BrokenTrackCallback inCallback ) throws LibraryException {
        JMusicExecutor.scheduleTask( new FindBrokenTracksTask( inRootId, inCallback ), 0 );
    }

    public void fixBrokenTrack( LibraryItem inTrack ) throws LibraryException {
        String theUri = inTrack.getUri();
        Backend theBackend = BackendFactory.getBackend( inTrack.getUri() );
        if ( ! theBackend.isWriteable() ) {

            LibraryException theException = new LibraryException( "Backend doesnt support update" );
            mLogger.throwing( "Library", "fixBrokenTrack", theException );
            throw theException;
        }
        theBackend.fixTrack( theUri, inTrack );
    }

    public LibraryItem getItem( Long inId ) {
        PersistentObject theObject = PersistentObject.getObjectForId( inId );
        return theObject != null ? new LibraryConverter().convert( theObject ) : null;
    }
    
    public String getName() {
        return mName;
    }
    
    public LibraryItem getRootOfRoots() {
        return new LibraryConverter().convert( sRootOfRoots );
    }
/*
    public InputStream getTrackInputStream( LibraryItem inItem, ProgressListener inListener ) throws LibraryException {
        String theUri = inItem.getUri();
        Backend theBackend = BackendFactory.getBackend( theUri );
        return theBackend.getTrackInputStream( theUri, inListener );
    }

    public long getTrackInputStreamLength( LibraryItem inItem ) throws LibraryException {
        String theUri = inItem.getUri();
        Backend theBackend = BackendFactory.getBackend( theUri );
        return theBackend.getTrackInputStreamLength( theUri );
    }
*/
    public List< LibraryItem > getTracks( LibraryItem inContainer ) {
        PersistentContainer theContainer = PersistentContainer.getContainerForId( inContainer.getId() );
        return theContainer != null ?
            theContainer.getTracks( new LibraryConverter() ) :
            Collections.EMPTY_LIST;
    }

    public List< LibraryItem > getUnknownTracks( Long inRootId ) {
        Set< LibraryItem > theTracks = new HashSet<>();
        LibraryConverter theConverter = new LibraryConverter();
        for ( PersistentArtist theArtist : PersistentArtist.getUnknownArtists( inRootId ) ) {
            theTracks.addAll( theArtist.getTracks( theConverter ) );
        }
        for ( PersistentAlbum theAlbum : PersistentAlbum.getUnknownAlbums( inRootId ) ) {
            theTracks.addAll( theAlbum.getTracks( theConverter ) );
        }
        for ( PersistentTrack theTrack : PersistentTrack.getUnknownTracks( inRootId ) ) {
            theTracks.add( theConverter.convert( theTrack ) );
        }
        return new ArrayList<>( theTracks );
    }

    public void importTracks( Map< Long, LibraryItem > inTracks, Long inTargetRootId, ProgressListener inListener ) {
        JMusicExecutor.scheduleTask( new ImportTracksTask( inTracks, inTargetRootId, inListener ), 0 );
    }

    public boolean isWriteable( LibraryItem inItem ) {
        PersistentRoot theRoot = PersistentRoot.getRootForId( inItem.getRootId() );
        if ( theRoot == null ) {
            return false;
        }
        Backend theBackend = BackendFactory.getBackend( theRoot.getUri() );
        return theBackend.isWriteable();
    }

    @Override
    public void onConfigChange( String inKey, String inOldValue, String inNewValue ) {
        if ( ! ConfigConstants.isCategoryMusicSource( inKey ) ) {
            return;
        }
        synchronized( mRefreshTasks ) {
            for ( PersistentRoot theRoot : PersistentRoot.getRoots() ) {
                Long theRootId = theRoot.getId();
                if ( theRootId.equals( sRootOfRoots.getId() ) || theRootId.equals( sPlaylists.getId() ) ) {
                    continue;
                }
                long theInterval = getRefreshInterval( theRoot );
                RootRefreshTask theTask = mRefreshTasks.get( theRoot );
                if ( theTask != null ) {
                    if ( theInterval == -1 || theInterval != theTask.getInterval() ) {
                        theTask.cancel();
                        mRefreshTasks.put( theRoot, null );
                    }
                }
                scheduleRootRefreshTask( theRoot, theInterval );
            }
        }
    }
    
    @Override
    public void onPostPersist( PersistentObject inObject ) {
        LibraryItem theObject = new LibraryConverter().convert( inObject );
        synchronized( mListeners ) {
            mListeners.stream().forEach( (theListener) -> {
                theListener.onObjectCreate( theObject );
            } );
        }
    }

    @Override
    public void onPostRemove( PersistentObject inObject ) {
        LibraryItem theObject = new LibraryConverter().convert( inObject );
        synchronized( mListeners ) {
            mListeners.stream().forEach( (theListener) -> {
                theListener.onObjectDestroy( theObject );
            } );
        }
    }

    @Override
    public void onPostUpdate( PersistentObject inObject ) {
        LibraryItem theObject = new LibraryConverter().convert( inObject );
        synchronized( mListeners ) {
            mListeners.stream().forEach( (theListener) -> {
                theListener.onObjectUpdate( theObject );
            } );
        }
    }

    public void refresh( Long inRootId ) {
        PersistentRoot theRoot = PersistentRoot.getRootForId( inRootId );
        if ( theRoot != null ) {
            scheduleRootRefreshTask( theRoot, 0 );
        }
    }
    
    public void removeListener( LibraryListener inListener ) {
        synchronized( mListeners ) {
            mListeners.remove(  inListener );
        }
    }
    
    public void removePlaylist( Long inId ) {
        PersistentPlaylist thePlaylist = PersistentPlaylist.getPlaylistForId( inId );
        if ( thePlaylist == null ) {
            mLogger.warning( "Playlist id:" + inId + " doesn't exist" );
            return;
        }
        sPlaylists.removeChild( thePlaylist );
        sPlaylists.commit();
        thePlaylist.destroy();
    }
    
    public void removeRoot( Long inId ) {
        PersistentRoot theRoot = PersistentRoot.getRootForId( inId );
        if ( theRoot == null ) {
            mLogger.warning( "Root id:" + inId + " doesn't exist" );
            return;
        }
        removeRoot( theRoot );
    }
    
    public void removeTrackFromPlaylist( Long inTrackId, Long inPlaylistId ) {
        PersistentTrack theTrack = PersistentTrack.getTrackForId( inTrackId );
        if ( theTrack == null ) {
            mLogger.warning( "Track id:" + inTrackId + " doesn't exist" );
            return;
        }
        PersistentPlaylist thePlaylist = PersistentPlaylist.getPlaylistForId( inPlaylistId );
        if ( thePlaylist == null ) {
            mLogger.warning( "Playlist id:" + inPlaylistId + " doesn't exist" );
            return;
        }
        PersistentObject.beginTransaction();
        thePlaylist.removeTrack( theTrack );
        thePlaylist.commit();
        theTrack.removePlaylist( thePlaylist );
        theTrack.commit();
        PersistentObject.endTransaction();
    }

    public void updatePlaylist( LibraryItem inPlaylist ) {
        Long theId = inPlaylist.getId();
        PersistentPlaylist thePlaylist = PersistentPlaylist.getPlaylistForId( theId );
        if ( thePlaylist == null ) {
            mLogger.warning( "Playlist id:"  + theId + " doesn't exist" );
            return;
        }
        thePlaylist.setName( inPlaylist.getTitle() );
        thePlaylist.commit();
    }

    public void updateRoot( LibraryItem inRoot ) {
        Long theId = inRoot.getId();
        PersistentRoot theRoot = PersistentRoot.getRootForId( theId );
        if ( theRoot == null ) {
            mLogger.warning( "Root id:"  + theId + " doesn't exist" );
            return;
        }
        theRoot.setName( inRoot.getTitle() );
        theRoot.commit();
    }

    public void updateTracks( Map< Long, LibraryItem > inTracks, ProgressListener inListener ) {
        JMusicExecutor.scheduleTask( new UpdateTracksTask( inTracks, inListener ), 0 );
    }
    
    private PersistentAlbum createAlbum( PersistentArtist inArtist, String inName ) {
        PersistentAlbum theAlbum = ( PersistentAlbum )inArtist.getChild( inName );
        if ( theAlbum == null ) {
            theAlbum = new PersistentAlbum();
            theAlbum.setName( inName );
            theAlbum.setParent( inArtist );
            theAlbum.setRootId( inArtist.getRootId() );
            inArtist.addChild( theAlbum );
            inArtist.commit();
        }
        return theAlbum;
    }
    
    private PersistentArtist createArtist( PersistentRoot inRoot, String inName ) {
        PersistentArtist theArtist = ( PersistentArtist )inRoot.getChild( inName );
        if ( theArtist == null ) {
            theArtist = new PersistentArtist();
            theArtist.setName( inName );
            theArtist.setParent( inRoot );
            theArtist.setRootId( inRoot.getId() );
            inRoot.addChild( theArtist );
            inRoot.commit();
        }
        return theArtist;
    }
    
    private PersistentPlaylist createPlaylist( String inName ) {
        PersistentPlaylist thePlaylist = new PersistentPlaylist();
        thePlaylist.setName( inName );
        thePlaylist.setParent( sPlaylists );
        sPlaylists.addChild( thePlaylist );
        sPlaylists.commit();
        return thePlaylist;
    }

    private static PersistentRoot createRoot( String inUri, String inName ) {
        return createRoot( inUri, inName, LibraryItem.Type.root );
    }

    private static PersistentRoot createRoot( String inUri, String inName, LibraryItem.Type inType ) {
        PersistentRoot theRoot;
        switch ( inType ) {
            case playlistsroot:
                theRoot = new PersistentPlaylistsRoot();
                break;
            case cdroot:
                theRoot = new PersistentCDRoot();
                break;
            default:
                theRoot = new PersistentRoot();
                break;
        }
        theRoot.setName( inName );
        theRoot.setParent( sRootOfRoots );
        theRoot.setRootId( sRootOfRoots.getRootId() );
        theRoot.setUri( inUri );
        sRootOfRoots.addChild( theRoot );
        sRootOfRoots.commit();
        return theRoot;
    }
    
    private PersistentTrack createTrack( PersistentRoot inRoot, LibraryItem inTrack ) {
        PersistentAlbum theAlbum =
            createAlbum( createArtist( inRoot, inTrack.getArtistName() ), inTrack.getAlbumName() );
        PersistentTrack theTrack = new PersistentTrack();
        theTrack.setBitRate( inTrack.getBitRate() );
        theTrack.setDuration( inTrack.getDuration() );
        theTrack.setLastModified( inTrack.getLastModified() );
        theTrack.setName( inTrack.getTitle() );
        theTrack.setNumber( inTrack.getTrackNumber() );
        theTrack.setParent( theAlbum );
        theTrack.setRootId( inRoot.getId() );
        theTrack.setUri( inTrack.getUri() );
        theAlbum.addChild( theTrack );
        theAlbum.commit();
        return theTrack;
    }
    
    private void ensureBackend( String inUri ) throws LibraryException {
        if ( BackendFactory.getBackend( inUri ) == null ) {
            throw new LibraryException(
                "Error: No backend available for uri '" + inUri + "'" );
        }
    }
    
    private void ensureRootDoesntExist( String inUri ) throws LibraryException {
        if ( PersistentRoot.getRootForUri( inUri ) != null ) {
            throw new LibraryException(
                "Error: Root for uri '" + inUri + "' already exists" );
        }
    }

    private long getRefreshInterval( PersistentRoot inRoot ) {
        return getRefreshInterval( inRoot.getId() );
    }

    private long getRefreshInterval( Long inId ) {
        ConfigConstants.RefreshChoice theChoice = ConfigConstants.RefreshChoice.valueOf(
            Config.getInstance().getProperty(
                ConfigConstants.sPropNameRefreshType, ConfigConstants.sPropRefreshTypeDefault.name() ) );
        boolean isEnabled = Boolean.valueOf(
            Config.getInstance().getProperty(
                ConfigConstants.getPropNameMusicSourceRefreshEnabled( inId ),
                ConfigConstants.sPropMusicSourceRefreshEnabledDefault ) );
        if ( ConfigConstants.RefreshChoice.configureAllDisabled.equals( theChoice ) || ! isEnabled ) {
            return -1;
        }
        if ( ConfigConstants.RefreshChoice.configureAllEnabled.equals( theChoice ) ) {
            return Long.valueOf( Config.getInstance().getProperty(
                ConfigConstants.sPropNameRefreshInterval, ConfigConstants.sPropRefreshIntervalDefault ) ) * 1000;
        }
        return Long.valueOf( Config.getInstance().getProperty(
            ConfigConstants.getPropNameMusicSourceRefreshInterval( inId ),
            ConfigConstants.sPropRefreshIntervalDefault ) ) * 1000;
    }

    private void initialiseRootRefreshTasks() {
        synchronized( mRefreshTasks ) {
            for ( PersistentRoot theRoot : PersistentRoot.getRoots() ) {
                Long theRootId = theRoot.getId();
                if ( theRootId.equals( sRootOfRoots.getId() ) || theRootId.equals( sPlaylists.getId() ) ) {
                    continue;
                }
                RootRefreshTask theTask =
                    getRefreshInterval( theRoot ) == -1 ?
                        null : new RootRefreshTask( theRoot, 0 );
                mRefreshTasks.put( theRoot, theTask );
                if ( theTask != null ) {
                    JMusicExecutor.scheduleTask( theTask, 0 );
                }
            }
        }
    }
    
    private void refreshBackend( PersistentRoot inRoot ) throws LibraryException {
        Backend theBackend = BackendFactory.getBackend( inRoot.getUri() );
        Map< String, LibraryItem > theBackendTracks = theBackend.listTracks();
        refreshDeletions( inRoot, theBackendTracks );
        refreshCreations( inRoot, theBackend, theBackendTracks );
        refreshCleanup( inRoot );
    }
    
    private void refreshCleanup( PersistentRoot inRoot ) {
        Long theRootId = inRoot.getId();
        removeEmptyContainers( PersistentAlbum.getAlbumsForRoot( theRootId ) );
        removeEmptyContainers( PersistentArtist.getArtistsForRoot( theRootId ) );
    }
    
    private void refreshCreations( PersistentRoot inRoot, Backend inBackend,
        Map< String, LibraryItem > inBackendTracks ) {
        LinkedList< String > theMissingTagUris = new LinkedList<>();
        for ( String theBackendUri : inBackendTracks.keySet() ) {
            try {
                LibraryItem theBackendTrack = inBackendTracks.get( theBackendUri );
                PersistentTrack theTrack = PersistentTrack.getTrackForUri( theBackendUri );
                if ( theTrack == null ) {
                    createTrack( inRoot, inBackend.getTrack( theBackendUri ) );
                } else if ( theBackendTrack.getLastModified() > theTrack.getLastModified() ) {
                    updateTrack( theTrack, inBackend.getTrack( theBackendUri ) );
                }
            } catch( LibraryException theException ) {
                if ( LibraryException.ErrorCode.NoTag == theException.getErrorCode() ) {
                    try {
                        theMissingTagUris.add( URLDecoder.decode( theBackendUri, "UTF-8" ) );
                    } catch( UnsupportedEncodingException theException1 ) {
                        theMissingTagUris.add( theBackendUri );
                    }
                } else {
                    mLogger.warning( theException.toString() );
                }
            }
        }
        if ( ! theMissingTagUris.isEmpty() ) {
            StringBuilder theBuilder = new StringBuilder( "The following files have no tag information:" );
            for ( String theUri : theMissingTagUris ) {
                theBuilder.append( "\n " ).append( theUri );
            }
            mLogger.warning( theBuilder.toString() );
        }
    }
    
    private void refreshDeletions( PersistentRoot inRoot, Map< String, LibraryItem > inBackendTracks ) {
        Set< String > theBackendUris = inBackendTracks.keySet();
        PersistentTrack.getTrackUris( inRoot.getId() ).stream().filter( theDBUri -> !theBackendUris.contains( theDBUri ) ).forEach( theDBUri -> {
            PersistentTrack theTrack = PersistentTrack.getTrackForUri( theDBUri );
            if ( theTrack != null ) {
                PersistentAlbum theAlbum = ( PersistentAlbum ) theTrack.getParent();
                theAlbum.removeChild( theTrack );
                theTrack.destroy();
                theAlbum.commit();
            }
        } );
    }
    
    private void removeEmptyContainers( Collection< ? extends PersistentContainer > inContainers ) {
        if ( inContainers.isEmpty() ) {
            return;
        }
        HashSet< PersistentContainer > theParents = new HashSet<>();
        for ( PersistentContainer theContainer : inContainers ) {
            if ( theContainer.getChildCount() == 0 ) {
                PersistentContainer theParent = theContainer.getParent();
                theParents.add( theParent );
                theParent.removeChild( theContainer );
                theContainer.destroy();
            }
        }
        for ( PersistentContainer theContainer : theParents ) {
            theContainer.commit();
        }
    }

    private void removeRoot( PersistentRoot inRoot ) {
        synchronized( mRefreshTasks ) {
            if ( mRefreshTasks.containsKey( inRoot ) ) {
                RootRefreshTask theTask = mRefreshTasks.get( inRoot );
                if ( theTask != null ) {
                    theTask.cancel();
                }
                mRefreshTasks.remove( inRoot );
            }
        }
        sRootOfRoots.removeChild( inRoot );
        sRootOfRoots.commit();
        inRoot.destroy();
    }

    private void rescheduleCompletedRootRefreshTask( PersistentRoot inRoot ) {
        synchronized( mRefreshTasks ) {
            if ( mRefreshTasks.containsKey( inRoot ) ) {
                scheduleRootRefreshTask( inRoot, getRefreshInterval( inRoot ) );
            }
        }
    }

    private void scheduleRootRefreshTask( PersistentRoot inRoot, long inInterval ) {
        if ( inInterval == -1 ) {
            return;
        }
        RootRefreshTask theTask = new RootRefreshTask( inRoot, inInterval );
        mRefreshTasks.put( inRoot, theTask );
        JMusicExecutor.scheduleTask( theTask, inInterval );
    }

    private void updateTrack( PersistentTrack inOriginal, LibraryItem inUpdated ) {
        PersistentObject.beginTransaction();
        PersistentAlbum theAlbum =
            createAlbum( createArtist( PersistentRoot.getRootForId( inOriginal.getRootId() ),
                                       inUpdated.getArtistName() ),
                         inUpdated.getAlbumName() );
        PersistentAlbum theOriginalAlbum = ( PersistentAlbum )inOriginal.getParent();
        boolean needsCommit = false;
        if ( theAlbum != theOriginalAlbum ) {
            theOriginalAlbum.removeChild( inOriginal );
            theOriginalAlbum.commit();
            theAlbum.addChild( inOriginal );
            theAlbum.commit();
            inOriginal.setParent( theAlbum );
            needsCommit = true;
        }
        String theOriginalName = inOriginal.getName();
        String theUpdatedName = inUpdated.getTitle();
        if ( ! theOriginalName.equals( theUpdatedName ) ) {
            inOriginal.setName( theUpdatedName );
            needsCommit = true;
        }
        Integer theOriginalTrackNumber = inOriginal.getNumber();
        Integer theUpdatedTrackNumber = inUpdated.getTrackNumber();
        if ( ! theOriginalTrackNumber.equals( theUpdatedTrackNumber ) ) {
            inOriginal.setNumber( theUpdatedTrackNumber );
            needsCommit = true;
        }
        if ( needsCommit ) {
            inOriginal.setLastModified( System.currentTimeMillis() );
            inOriginal.commit();
        }
        PersistentObject.endTransaction();
    }

    private String uriToName( String inUri ) {
        int theIndex = inUri.lastIndexOf( "/" );
        return ( theIndex != -1 ? inUri.substring( theIndex + 1 ) : inUri ).replace( "%20", " " );
    }

    public interface BrokenTrackCallback {
        public void onBrokenTrackFound( String inUri );
        public void onComplete();
    }

    class FindBrokenTracksTask extends TimerTask {
        private final Long mRootId;
        private final BrokenTrackCallback mCallback;

        FindBrokenTracksTask( Long inRootId, BrokenTrackCallback inCallback ) {
            mRootId = inRootId;
            mCallback = inCallback;
        }

        @Override
        public void run() {
            try {
                PersistentRoot theRoot = PersistentRoot.getRootForId( mRootId );
                Backend theBackend = BackendFactory.getBackend( theRoot.getUri() );
                Map< String, LibraryItem > theBackendTracks = theBackend.listTracks();
                for ( String theBackendUri : theBackendTracks.keySet() ) {
                    try {
                        theBackend.getTrack( theBackendUri );
                    } catch ( LibraryException theException ) {
                        if ( LibraryException.ErrorCode.NoTag == theException.getErrorCode() ) {
                            try {
                                mCallback.onBrokenTrackFound( URLDecoder.decode( theBackendUri, "UTF-8" ) );
                            } catch ( UnsupportedEncodingException theException1 ) {
                                mCallback.onBrokenTrackFound( theBackendUri );
                            }
                        } else {
                            mLogger.warning( theException.toString() );
                        }
                    }
                }
            } catch( Exception theException ) {
                mLogger.throwing( "Library", "findBrokenTracks", theException );
            } finally {
                mCallback.onComplete();
            }
        }
    }

    class ImportTracksTask extends TimerTask {
        private final Map< Long, LibraryItem > mTracks;
        private final Long mTargetRootId;
        private final ProgressListener mListener;

        ImportTracksTask( Map< Long, LibraryItem > inTracks, Long inTargetRootId, ProgressListener inListener ) {
            mTracks = inTracks;
            mTargetRootId = inTargetRootId;
            mListener = inListener;
        }

        @Override
        public void run() {
            Backend theTargetBackend;
            try {
                PersistentRoot theRoot = PersistentRoot.getRootForId( mTargetRootId );
                if ( theRoot == null ) {
                    throw new LibraryException( "Root id:" + mTargetRootId + " doesn't exist" );
                }
                theTargetBackend = BackendFactory.getBackend( theRoot.getUri() );
                if ( !theTargetBackend.isWriteable() ) {
                    throw new LibraryException( "Target backend doesnt support import" );
                }
            } catch( Exception theException ) {
                mListener.onErrorMessage( theException.getMessage() );
                mListener.onProgress( 100 );
                mListener.onComplete();
                return;
            }
            int theCount = mTracks.size();
            int theIndex = 0;
            int thePercent = 0;
            for ( Long theId : mTracks.keySet() ) {
                try {
                    PersistentTrack theSourceTrack = PersistentTrack.getTrackForId( theId );
                    if ( theSourceTrack == null ) {
                        throw new LibraryException( "Track id:" + theId + " doesn't exist" );
                    }
                    String theSourceUri = theSourceTrack.getUri();
                    String theSourceName = uriToName( theSourceUri );
                    mListener.onStatusMessage( "Importing Track:" + theSourceName );
                    Backend theSourceBackend = BackendFactory.getBackend( theSourceUri );
                    final int theBasePercent = thePercent;
                    theTargetBackend.importTrack( theSourceBackend, theSourceUri, mTracks.get( theId ), new ProgressListener() {
                        @Override
                        public void onProgress( int inPercent ) {
                            mListener.onProgress( theBasePercent + ( inPercent / theCount) );
                        }
                    } );
                    mListener.onStatusMessage( "Track import complete:" + theSourceName );
                } catch( Exception theException ) {
                    mListener.onErrorMessage( theException.getMessage() );
                } finally {
                    ++ theIndex;
                    thePercent = theIndex * 100 / theCount;
                    mListener.onProgress( thePercent );

                }
            }
            mListener.onProgress( 100 );
            mListener.onComplete();
        }
    }

    class RootRefreshTask extends TimerTask {
        private final PersistentRoot mRoot;
        private final long mInterval;

        RootRefreshTask( PersistentRoot inRoot, long inInterval ) {
            mRoot = inRoot;
            mInterval = inInterval;
        }

        long getInterval() {
            return mInterval;
        }

        @Override
        public void run() {
            try {
                mLogger.fine( "Starting monitor root:" + mRoot.getName() + " interval:" + mInterval );
                refreshBackend( mRoot );
            } catch( Exception theException ) {
                mLogger.throwing( "Library.RootRefreshTask", "run", theException );
            } finally {
                rescheduleCompletedRootRefreshTask( mRoot );
            }
        }
    }

    class UpdateTracksTask extends TimerTask {
        private final Map< Long, LibraryItem > mTracks;
        private final ProgressListener mListener;

        UpdateTracksTask( Map< Long, LibraryItem > inTracks, ProgressListener inListener ) {
            mTracks = inTracks;
            mListener = inListener;
        }

        @Override
        public void run() {
            int theCount = mTracks.size();
            int theIndex = 0;
            for ( Long theId : mTracks.keySet() ) {
                try {
                    LibraryItem theItem = mTracks.get( theId );
                    mListener.onStatusMessage( "Updating Track:" +  theItem.getTitle() );
                    PersistentTrack theTrack = PersistentTrack.getTrackForId( theId );
                    if ( theTrack == null ) {
                        throw new LibraryException( "Track id:" + theId + " doesn't exist" );
                    }
                    String theUri = theTrack.getUri();
                    Backend theBackend = BackendFactory.getBackend( theTrack.getUri() );
                    if ( !theBackend.isWriteable() ) {
                        throw new LibraryException( "Backend doesnt support update" );
                    }
                    theBackend.updateTrack( theUri, theItem );
                    mListener.onStatusMessage( "Track update complete:" +  theItem.getTitle() );
                } catch( Exception theException ) {
                    mListener.onErrorMessage( theException.getMessage() );
                } finally {
                    ++ theIndex;
                    mListener.onProgress( theIndex * 100 / theCount );
                }
            }
            mListener.onProgress( 100 );
            mListener.onComplete();
        }
    }
}