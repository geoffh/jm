package jmusic.library.persistence;

import jmusic.library.LibraryItem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.LinkedList;
import java.util.List;

@Entity
@NamedQueries( {
    @NamedQuery( name = "getTrackForUri",
                 query = "SELECT p FROM PersistentTrack p WHERE p.uri = :uri" ),
    @NamedQuery( name = "getTrackUris",
                 query = "SELECT p.uri FROM PersistentTrack AS p WHERE p.rootId = :rootId" ),
    @NamedQuery( name = "getTrackForId",
                 query = "SELECT p FROM PersistentTrack p WHERE p.id = :id" ),
    @NamedQuery( name = "getTracksForName",
        query = "SELECT p FROM PersistentTrack p WHERE p.rootId = :rootId AND UPPER( p.name ) = UPPER( :name )")
} )
public class PersistentTrack extends PersistentObject implements PersistenceListener {
    private static final PersistentPlaylist[] sModel = new PersistentPlaylist[ 0 ];
    
    private Integer bitRate;
    private Integer duration;
    private Long lastModified;
    private Integer number;
    @ManyToMany( targetEntity = PersistentPlaylist.class, mappedBy = "tracks" )
    private List< PersistentPlaylist > playlists = new LinkedList<>();
    @Column( nullable = false )
    private Long size;
    @Column( nullable = false, unique = true )
    protected String uri;

    public PersistentTrack() {
        super();
        // Need to notify playlists on track removal
        PersistenceManager.addListener( this );
    }
    
    public Integer getBitRate() { return bitRate; }
    public Integer getDuration() { return duration; }
    public Long getLastModified() { return lastModified; }
    public Integer getNumber() { return number; }
    public PersistentPlaylist[] getPlayLists() { return playlists.toArray( sModel ); }
    public Long getSize() { return size; }
    public String getUri() { return uri; }
    
    public void setBitRate( Integer inBitRate ) { bitRate = inBitRate; }
    public void setDuration( Integer inDuration ) { duration = inDuration; }
    public void setLastModified( Long inLastModified ) { lastModified = inLastModified; }
    public void setNumber( Integer inNumber ) { number = inNumber; }
    public void setSize( Long inSize ) { size = inSize; }
    public void setUri( String inUri ) { uri = inUri; }

    public void addPlaylist( PersistentPlaylist inPlaylist ) {
        synchronized( playlists ) {
            playlists.add(  inPlaylist );
        }
    }

    public static PersistentTrack getTrackForId( Long inId ) {
        TypedQuery< PersistentTrack > theQuery =
            getEntityManager().createNamedQuery(
                "getTrackForId", PersistentTrack.class );
        theQuery.setParameter( "id", inId );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }

    public static List< PersistentTrack > getTracksForName( Long inRootId, String inName ) {
        TypedQuery< PersistentTrack > theQuery =
            getEntityManager().createNamedQuery(
                "getTracksForName", PersistentTrack.class );
        theQuery.setParameter( "rootId", inRootId );
        theQuery.setParameter( "name", inName );
        return theQuery.getResultList();
    }
    
    public static PersistentTrack getTrackForUri( String inUri ) {
        TypedQuery< PersistentTrack > theQuery =
            getEntityManager().createNamedQuery(
                "getTrackForUri", PersistentTrack.class );
        theQuery.setParameter( "uri", inUri );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }
    
    public static List< String > getTrackUris( Long inRootId ) {
        TypedQuery< String > theQuery =
            getEntityManager().createNamedQuery(
                "getTrackUris", String.class );
        theQuery.setParameter( "rootId", inRootId );
        return theQuery.getResultList();
    }

    public static List< PersistentTrack > getUnknownTracks( Long inRootId ) {
        return getTracksForName( inRootId, LibraryItem.sUnknown );
    }

    @Override
    public void onPostPersist( PersistentObject inObject ) {}

    @Override
    public void onPostRemove( PersistentObject inObject ) {
        if ( inObject == this ) {
            for ( PersistentPlaylist thePlaylist : playlists ) {
                thePlaylist.removeTrack( this );
                thePlaylist.commit();
            }
        }
    }

    @Override
    public void onPostUpdate( PersistentObject inObject ) {}

    public void removePlaylist( PersistentPlaylist inPlaylist ) {
        synchronized( playlists ) {
            playlists.remove( inPlaylist );
        }
    }
}