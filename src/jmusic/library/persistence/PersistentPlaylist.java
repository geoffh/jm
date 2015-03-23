package jmusic.library.persistence;

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
    @NamedQuery( name = "getPlaylistForId",
                 query = "SELECT p FROM PersistentPlaylist p WHERE p.id = :id" )
} )
public class PersistentPlaylist extends PersistentBrowsable {
    private static final PersistentTrack[] sModel = new PersistentTrack[ 0 ];
    
    @ManyToMany( targetEntity = PersistentTrack.class )
    private List< PersistentTrack > tracks = new LinkedList<>();

    public PersistentTrack[] getTracks() { return tracks.toArray( sModel ); }
    
    public void addTrack( PersistentTrack inTrack ) {
        synchronized( tracks ) {
            tracks.add( inTrack );
        }
    }

    @Override
    public List< PersistentObject > browse( int inFirstResult, int inMaxResults ) {
        return new LinkedList( tracks );
    }

    public int getMaxBrowseResults() {
        return tracks.size();
    }

    public static PersistentPlaylist getPlaylistForId( Long inId ) {
        TypedQuery< PersistentPlaylist > theQuery =
            getEntityManager().createNamedQuery(
                "getPlaylistForId", PersistentPlaylist.class );
        theQuery.setParameter( "id", inId );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }
    
    public void removeTrack( PersistentTrack inTrack ) {
        synchronized( tracks ) {
            tracks.remove( inTrack );
        }
    }
}