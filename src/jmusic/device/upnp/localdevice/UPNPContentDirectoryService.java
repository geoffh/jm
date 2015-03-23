package jmusic.device.upnp.localdevice;

import jmusic.library.Library;
import jmusic.library.LibraryBrowseResult;
import jmusic.library.LibraryItem;
import jmusic.library.LibraryListener;
import jmusic.oldneedsrewrite.http.HttpServer;
import org.teleal.cling.binding.annotations.UpnpStateVariable;
import org.teleal.cling.binding.annotations.UpnpStateVariables;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.types.csv.CSV;
import org.teleal.cling.model.types.csv.CSVString;
import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.container.MusicAlbum;
import org.teleal.cling.support.model.container.MusicArtist;
import org.teleal.cling.support.model.container.PlaylistContainer;
import org.teleal.cling.support.model.container.StorageFolder;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.common.util.MimeType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@UpnpStateVariables({
    @UpnpStateVariable(
        name = "A_ARG_TYPE_ObjectID",
        sendEvents = false,
        datatype = "string"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_Result",
        sendEvents = false,
        datatype = "string"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_BrowseFlag",
        sendEvents = false,
        datatype = "string",
        allowedValuesEnum = BrowseFlag.class),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_Filter",
        sendEvents = false,
        datatype = "string"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_SortCriteria",
        sendEvents = false,
        datatype = "string"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_Index",
        sendEvents = false,
        datatype = "ui4"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_Count",
        sendEvents = false,
        datatype = "ui4"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_UpdateID",
        sendEvents = false,
        datatype = "ui4"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_URI",
        sendEvents = false,
        datatype = "uri"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_TagValueList",
        sendEvents = false,
        datatype = "string"),
    @UpnpStateVariable(
        name = "A_ARG_TYPE_SearchCriteria",
        sendEvents = false,
        datatype = "string")
})

public class UPNPContentDirectoryService extends AbstractContentDirectoryService
    implements LibraryListener {
    private static final String sCreator = "The Creator";
    private static final String sMimeAudio = "audio";
    private static final String sMimeMpeg = "mpeg";
    private static final String sContainerUpdateIds = "ContainerUpdateIDs";

    private final Library mLibrary;
    private final Set< Long > mUpdatedContainerIds = new HashSet<>();
    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    @UpnpStateVariable(
        sendEvents = true,
        eventMaximumRateMilliseconds = 200 )
    private final CSV< String > ContainerUpdateIDs = new CSVString();

    public UPNPContentDirectoryService( Library inLibrary ) {
        mLibrary = inLibrary;
        mLibrary.addListener( this );
    }

    @Override
    public BrowseResult browse(
        String inObjectID, BrowseFlag inBrowseFlag,
        String inFilter, long inFirstResult, long inMaxResults,
        SortCriterion[] inOrderby ) throws ContentDirectoryException {
        DIDLContent theContent = new DIDLContent();
        LibraryBrowseResult theResult =
            mLibrary.browse( Long.valueOf( inObjectID ), ( int )inFirstResult, ( int )inMaxResults );
        if ( theResult.mMaxResults != -1 ) {
            for ( LibraryItem theObject : theResult.mResults ) {
                switch ( theObject.getType() ) {
                    case track:
                        theContent.addItem( createTrack( theObject ) );
                        break;
                    case album:
                        theContent.addContainer( createAlbum( theObject ) );
                        break;
                    case artist:
                        theContent.addContainer( createArtist( theObject ) );
                        break;
                    case root:
                    case playlistsroot:
                    case cdroot:
                        theContent.addContainer( createRoot( theObject ) );
                        break;
                    case playlist:
                        theContent.addContainer( createPlaylist( theObject ) );
                        break;
                    default:
                        break;
                }
            }
        }
        try {
            return new BrowseResult(
                new DIDLParser().generate( theContent ),
                theResult.mResults.size(), theResult.mMaxResults );
        } catch( Exception theCause ) {
            ContentDirectoryException theException =
                new ContentDirectoryException(
                    ContentDirectoryErrorCode.CANNOT_PROCESS,
                    theCause.toString() );
            mLogger.throwing( "ContentDirectoryService", "browse", theException );
            throw theException;
        }
    }

    public void fireLastChange() {
        synchronized( mUpdatedContainerIds ) {
            if ( ! mUpdatedContainerIds.isEmpty() ) {
                ContainerUpdateIDs.clear();
                String theUpdateId = String.valueOf( System.currentTimeMillis() );
                for ( Long theId : mUpdatedContainerIds ) {
                    ContainerUpdateIDs.add( String.valueOf( theId ) );
                    ContainerUpdateIDs.add( theUpdateId );
                }
                mUpdatedContainerIds.clear();
                getPropertyChangeSupport().firePropertyChange(
                    sContainerUpdateIds, null, null );
            }
        }
    }

    @Override
    public void onObjectCreate( LibraryItem inObject ) {
        rememberContainerChange( inObject.getParentId() );
    }

    @Override
    public void onObjectDestroy( LibraryItem inObject ) {
        rememberContainerChange( inObject.getParentId() );
    }

    @Override
    public void onObjectUpdate( LibraryItem inObject ) {
        if ( LibraryItem.Type.track != inObject.getType() ) {
            rememberContainerChange( inObject.getId() );
        }
    }

    private void rememberContainerChange( Long inContainerId ) {
        synchronized( mUpdatedContainerIds ) {
            mUpdatedContainerIds.add(  inContainerId );
        }
    }

    private Container createAlbum( LibraryItem inObject ) {
        return new MusicAlbum(
            String.valueOf( inObject.getId() ),
            String.valueOf( inObject.getParentId() ),
            inObject.getTitle(),
            sCreator,
            0,
            new ArrayList<>() );
    }

    private Container createArtist( LibraryItem inObject ) {
        return new MusicArtist(
            String.valueOf( inObject.getId() ),
            String.valueOf( inObject.getParentId() ),
            inObject.getTitle(),
            sCreator,
            0 );
    }

    private Container createPlaylist( LibraryItem inObject ) {
        return new PlaylistContainer(
            String.valueOf( inObject.getId() ),
            String.valueOf( inObject.getParentId() ),
            inObject.getTitle(),
            sCreator,
            0 );
    }

    private Container createRoot( LibraryItem inObject ) {
        return new StorageFolder(
            String.valueOf( inObject.getId() ),
            String.valueOf( inObject.getParentId() ),
            inObject.getTitle(),
            sCreator,
            0,
            0L );
    }

    private Item createTrack( LibraryItem inObject ) {
        return new MusicTrack(
            String.valueOf( inObject.getId() ),
            String.valueOf( inObject.getParentId() ),
            inObject.getTitle(),
            sCreator,
            inObject.getAlbumName(),
            inObject.getArtistName(),
            new Res(
                new MimeType( sMimeAudio, sMimeMpeg ),
                inObject.getSize(),
                ModelUtil.toTimeString( inObject.getDuration() ),
                ( long )inObject.getBitRate(),
                HttpServer.getInstance().getURL( inObject.getUri() ) ) );
    }
}