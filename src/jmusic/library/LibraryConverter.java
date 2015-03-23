package jmusic.library;

import java.util.LinkedList;
import java.util.List;
import jmusic.library.persistence.PersistentAlbum;
import jmusic.library.persistence.PersistentArtist;
import jmusic.library.persistence.PersistentCDRoot;
import jmusic.library.persistence.PersistentContainer;
import jmusic.library.persistence.PersistentObject;
import jmusic.library.persistence.PersistentObjectConverter;
import jmusic.library.persistence.PersistentPlaylist;
import jmusic.library.persistence.PersistentPlaylistsRoot;
import jmusic.library.persistence.PersistentRoot;
import jmusic.library.persistence.PersistentTrack;

class LibraryConverter implements PersistentObjectConverter {
    @Override
    public LibraryItem convert( PersistentObject inObject ) {
        LibraryItem theItem = new LibraryItem();
        theItem.setId( inObject.getId() );
        theItem.setRootId( inObject.getRootId() );
        theItem.setTitle( inObject.getName() );
        PersistentContainer theContainer = inObject.getParent();
        if ( theContainer != null ) {
            theItem.setParentId( theContainer.getId() );
        }
        if ( inObject instanceof PersistentTrack ) {
            convert( ( PersistentTrack )inObject, theItem );
        } else if ( inObject instanceof PersistentAlbum ) {
            convert( ( PersistentAlbum )inObject, theItem );
        } else if ( inObject instanceof PersistentArtist ) {
            convert( ( PersistentArtist )inObject, theItem );
        }  else if ( inObject instanceof PersistentPlaylist ) {
            convert( ( PersistentPlaylist )inObject, theItem );
        } else if ( inObject instanceof PersistentPlaylistsRoot ) {
            convert( ( PersistentPlaylistsRoot )inObject, theItem );
        } else if ( inObject instanceof PersistentCDRoot ) {
            convert( ( PersistentCDRoot )inObject, theItem );
        } else if ( inObject instanceof PersistentRoot ) {
            convert( ( PersistentRoot )inObject, theItem );
        }
        return theItem;
    }
    
    public void convert( PersistentAlbum inObject,  LibraryItem outItem ) {
        outItem.setType( LibraryItem.Type.album );
    }
    
    public void convert( PersistentArtist inObject,  LibraryItem outItem ) {
        outItem.setType( LibraryItem.Type.artist );
    }
    
    public void convert( PersistentPlaylist inObject,  LibraryItem outItem ) {
        outItem.setType( LibraryItem.Type.playlist );
        List< Long > theIds = new LinkedList<>();
        for ( PersistentTrack theTrack : inObject.getTracks() ) {
            theIds.add( theTrack.getId() );
        }
        outItem.setTrackIds( theIds );
    }

    public void convert( PersistentCDRoot inObject,  LibraryItem outItem ) {
        outItem.setType( LibraryItem.Type.cdroot );
    }

    public void convert( PersistentPlaylistsRoot inObject,  LibraryItem outItem ) {
        outItem.setType( LibraryItem.Type.playlistsroot );
    }
    
    public void convert( PersistentRoot inObject,  LibraryItem outItem ) {
        outItem.setType( LibraryItem.Type.root );
    }
    
    public void convert( PersistentTrack inObject,  LibraryItem outItem ) {
        outItem.setAlbumName( inObject.getParent().getName() );
        outItem.setArtistId( inObject.getParent().getParent().getId() );
        outItem.setArtistName( inObject.getParent().getParent().getName() );
        outItem.setBitRate( inObject.getBitRate() );
        outItem.setDuration( inObject.getDuration() );
        outItem.setLastModified( inObject.getLastModified() );
        outItem.setTrackNumber( inObject.getNumber() );
        outItem.setType( LibraryItem.Type.track );
        outItem.setUri( inObject.getUri() );
        List< Long > theIds = new LinkedList<>();
        for ( PersistentPlaylist thePlaylist : inObject.getPlayLists() ) {
            theIds.add( thePlaylist.getId() );
        }
        outItem.setPlaylistIds( theIds );
    }
}
