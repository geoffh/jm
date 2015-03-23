package jmusic.library.persistence;

import jmusic.library.LibraryItem;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Entity
@DiscriminatorValue( "artist" )
@NamedQueries( {
    @NamedQuery( name = "getArtistsForRoot",
                 query = "SELECT p FROM PersistentArtist p WHERE p.rootId = :rootId" ),
    @NamedQuery( name = "getArtistForId",
                 query = "SELECT p FROM PersistentArtist p WHERE p.id = :id" ),
    @NamedQuery( name = "getArtistsForName",
                 query = "SELECT p FROM PersistentArtist p WHERE p.rootId = :rootId AND UPPER( p.name ) = UPPER( :name )")
} )
public class PersistentArtist extends PersistentContainer {
    @Override
    protected boolean canAddChild( PersistentObject inObject ) {
        return inObject instanceof PersistentAlbum;
    }

    public static PersistentArtist getArtistForId( Long inId ) {
        TypedQuery< PersistentArtist > theQuery =
            getEntityManager().createNamedQuery(
                "getArtistForId", PersistentArtist.class );
        theQuery.setParameter( "id", inId );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }

    public static List< PersistentArtist > getArtistsForName( Long inRootId, String inName ) {
        TypedQuery< PersistentArtist > theQuery =
            getEntityManager().createNamedQuery(
                "getArtistsForName", PersistentArtist.class );
        theQuery.setParameter( "rootId", inRootId );
        theQuery.setParameter( "name", inName );
        return theQuery.getResultList();
    }

    public static List< PersistentArtist > getArtistsForRoot( Long inRootId ) {
        TypedQuery< PersistentArtist > theQuery =
            getEntityManager().createNamedQuery(
                "getArtistsForRoot", PersistentArtist.class );
        theQuery.setParameter( "rootId", inRootId );
        return theQuery.getResultList();
    }

    public static List< PersistentArtist > getUnknownArtists( Long inRootId ) {
        return getArtistsForName( inRootId, LibraryItem.sUnknown );
    }

    protected Collection< ? extends PersistentObject > getTracks() {
        Collection< PersistentObject > theTracks = new LinkedList<>();
        for ( PersistentObject theAlbum : getChildren() ) {
            theTracks.addAll( ( ( PersistentAlbum ) theAlbum ).getTracks() );
        }
        return theTracks;
    }
}