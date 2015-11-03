package jmusic.library.persistence;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Entity
@DiscriminatorValue( "playlistsroot" )
@NamedQueries( {
    @NamedQuery( name = "getPlaylistsRoot",
        query = "SELECT p FROM PersistentPlaylistsRoot p" )
} )
public class PersistentPlaylistsRoot extends PersistentRoot {
    public static PersistentPlaylistsRoot getPlaylistsRoot() {
        TypedQuery< PersistentPlaylistsRoot > theQuery =
            getEntityManager().createNamedQuery(
                "getPlaylistsRoot", PersistentPlaylistsRoot.class );
        try {
            List< PersistentPlaylistsRoot > theResults = theQuery.getResultList();
            if ( theResults == null || theResults.isEmpty() ) {
                throw new NoResultException();
            }
            return theResults.get(0);
            //return theQuery.getResultList().get( 0 );
        } catch( NoResultException theException ) {
            return null;
        }
    }

    protected boolean canAddChild( PersistentObject inObject ) {
       return inObject instanceof PersistentPlaylist;
    }
}