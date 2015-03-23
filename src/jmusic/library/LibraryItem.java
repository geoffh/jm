package jmusic.library;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LibraryItem extends HashMap< LibraryItem.PropertyName, Object >  {
    public static final String sUnknown = "Unknown";
    public static final int sTrackNumberUnknown = -1;
    public enum Type {
        album,
        artist,
        cdroot,
        playlist,
        playlistsroot,
        track,
        root
    }

    enum PropertyName {
        albumName,
        albumUri,
        artistId,
        artistName,
        artistUri,
        bitRate,
        duration,
        id,
        lastModified,
        number,
        parentId,
        playlistIds,
        rootId,
        size,
        title,
        trackIds,
        type,
        uri
    }

    public LibraryItem() {
        super();
    }

    public LibraryItem( LibraryItem inItem ) {
       super( inItem );
    }

    public int compare( LibraryItem inItem ) {
        return getType() == inItem.getType() ?
            compareWithSameType( inItem ) : compareWithDifferentType( inItem );
    }

    public String getAlbumName() {
        return ( String )get( PropertyName.albumName );
    }

    public Long getArtistId() { return ( Long )get( PropertyName.artistId ); }

    public String getArtistName() {
        return ( String )get( PropertyName.artistName );
    }

    public Integer getBitRate() {
        return ( Integer )get( PropertyName.bitRate );
    }

    public Integer getDuration() {
        return ( Integer )get( PropertyName.duration );
    }

    public Long getId() {
        return ( Long )get( PropertyName.id );
    }

    public Long getLastModified() {
        return ( Long )get( PropertyName.lastModified );
    }

    public Long getParentId() {
        return ( Long )get( PropertyName.parentId );
    }

    public List< Long > getPlaylistIds() {
        Object theValue = get( PropertyName.playlistIds );
        return theValue != null ? ( List< Long > ) theValue : Collections.emptyList();
    }

    public Long getRootId() {
        return ( Long )get( PropertyName.rootId );
    }

    public Long getSize() {
        return ( Long )get( PropertyName.size );
    }

    public String getTitle() {
        return ( String )get( PropertyName.title );
    }

    public Integer getTrackNumber() {
        Object theTrackNumber = get( PropertyName.number );
        return theTrackNumber != null ? ( Integer )theTrackNumber : sTrackNumberUnknown;
    }

    public Type getType() {
        return ( Type )get( PropertyName.type );
    }

    public String getUri() {
        return ( String )get( PropertyName.uri );
    }

    public boolean isAlbum() {
        return Type.album == getType();
    }

    public boolean isArtist() {
        return Type.artist == getType();
    }

    public boolean isCDRoot() { return Type.cdroot == getType(); }

    public boolean isPlaylist() {
        return Type.playlist == getType();
    }

    public boolean isPlaylistsRoot() { return Type.playlistsroot == getType(); }

    public boolean isRoot() {
        return Type.root == getType();
    }

    public boolean isTrack() {
        return Type.track == getType();
    }

    public void setAlbumName( String inValue ) {
        put( PropertyName.albumName, inValue );
    }

    public void setAlbumUri( String inValue ) {
        put( PropertyName.albumUri, inValue );
    }

    public void setArtistId( Long inValue ) { put( PropertyName.artistId, inValue  ); }

    public void setArtistName( String inValue ) {
        put( PropertyName.artistName, inValue );
    }

    public void setArtistUri( String inValue ) {
        put( PropertyName.artistUri, inValue );
    }

    public void setBitRate( Integer inValue ) {
        put( PropertyName.bitRate, inValue );
    }

    public void setDuration( Integer inValue ) {
        put( PropertyName.duration, inValue );
    }

    public void setId( Long inValue ) {
        put( PropertyName.id, inValue );
    }

    public void setLastModified( Long inValue ) {
        put( PropertyName.lastModified, inValue );
    }

    public void setParentId( Long inValue ) {
        put( PropertyName.parentId, inValue );
    }

    public void setPlaylistIds( List< Long > inValue ) {
        put( PropertyName.playlistIds, inValue );
    }

    public void setRootId( Long inValue ) {
        put( PropertyName.rootId, inValue );
    }

    public void setTrackIds( List< Long > inValue ) {
        put( PropertyName.trackIds, inValue );
    }

    public void setTrackNumber( Integer inValue ) {
        put( PropertyName.number, inValue );
    }

    public void setType( Type inValue ) {
        put( PropertyName.type, inValue );
    }

    public void setSize( Long inValue ) {
        put( PropertyName.size, inValue );
    }

    public void setTitle( String inValue ) {
        put( PropertyName.title, inValue );
    }

    public void setUri( String inValue ) {
        put( PropertyName.uri, inValue );
    }

    @Override
    public String toString() {
        return ( String )get( PropertyName.title );
    }

    private int compareWithContainer( LibraryItem inItem ) {
        String theTitle1 = getTitle();
        String theTitle2 = inItem.getTitle();
        if ( theTitle1.equals( theTitle2 ) ) {
            return 0;
        }
        if ( isPlaylistsRoot() ) {
            return -1;
        }
        if ( inItem.isPlaylistsRoot() ) {
            return 1;
        }
        return theTitle1.compareToIgnoreCase( theTitle2 );
    }

    private int compareWithDifferentType( LibraryItem inItem ) {
        if ( isPlaylistsRoot() ) {
            return -1;
        }
        if ( inItem.isPlaylistsRoot() ) {
            return 1;
        }
        return getTitle().compareToIgnoreCase( inItem.getTitle() );
    }

    private int compareWithSameType( LibraryItem inItem ) {
        return isTrack() ?
            compareWithTrack( inItem ) : compareWithContainer( inItem );
    }

    private int compareWithTrack( LibraryItem inItem ) {
        String theValue1 = getArtistName();
        String theValue2 = inItem.getArtistName();
        int theResult = theValue1.compareToIgnoreCase( theValue2 );
        if ( theResult != 0 ) {
            return theResult;
        }
        theValue1 = getAlbumName();
        theValue2 = inItem.getAlbumName();
        theResult = theValue1.compareToIgnoreCase( theValue2 );
        if ( theResult != 0 ) {
            return theResult;
        }
        Integer theTrackNumber1 = getTrackNumber();
        Integer theTrackNumber2 = inItem.getTrackNumber();
        if ( theTrackNumber1 != null && theTrackNumber2 != null ) {
            int theDifference = theTrackNumber1 - theTrackNumber2;
            if ( theDifference != 0 ) {
                return theDifference;
            } else {
                return getTitle().compareToIgnoreCase( inItem.getTitle() );
            }
        }
        if ( theTrackNumber1 != null ) {
            return -1;
        }
        return 1;
    }
}