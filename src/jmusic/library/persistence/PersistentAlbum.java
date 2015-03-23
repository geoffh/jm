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
import static jmusic.library.persistence.PersistentObject.getEntityManager;

@Entity
@DiscriminatorValue( "album" )
@NamedQueries( {
    @NamedQuery( name = "getAlbumsForRoot",
                 query = "SELECT p FROM PersistentAlbum p WHERE p.rootId = :rootId" ),
    @NamedQuery( name = "getAlbumForId",
                 query = "SELECT p FROM PersistentAlbum p WHERE p.id = :id" ),
    @NamedQuery( name = "album.browse",
                 query = "SELECT p FROM PersistentTrack p WHERE p.parent = :parent ORDER BY p.number, p.name" ),
    @NamedQuery( name = "getAlbumsForName",
                 query = "SELECT p FROM PersistentAlbum p WHERE p.rootId = :rootId AND UPPER( p.name ) = UPPER( :name )")
} )
public class PersistentAlbum extends PersistentContainer {
    @Override
    protected boolean canAddChild( PersistentObject inObject ) {
        return inObject instanceof PersistentTrack;
    }
    
    @Override
    public List< PersistentObject > browse( int inFirstResult, int inMaxResults ) {
        TypedQuery< PersistentObject > theQuery =
            getEntityManager().createNamedQuery(
                "album.browse", PersistentObject.class );
        theQuery.setParameter( "parent", this );
        if ( inFirstResult > -1 ) {
            theQuery.setFirstResult( inFirstResult );
            theQuery.setMaxResults( inMaxResults );
        }
        return theQuery.getResultList();
    }

    public static PersistentAlbum getAlbumForId( Long inId ) {
        TypedQuery< PersistentAlbum > theQuery =
            getEntityManager().createNamedQuery(
                "getAlbumForId", PersistentAlbum.class );
        theQuery.setParameter( "id", inId );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }

    public static List< PersistentAlbum > getAlbumsForName( Long inRootId, String inName ) {
        TypedQuery< PersistentAlbum > theQuery =
            getEntityManager().createNamedQuery(
                "getAlbumsForName", PersistentAlbum.class );
        theQuery.setParameter( "rootId", inRootId );
        theQuery.setParameter( "name", inName );
        return theQuery.getResultList();
    }

    public static List< PersistentAlbum > getAlbumsForRoot( Long inRootId ) {
        TypedQuery< PersistentAlbum > theQuery =
            getEntityManager().createNamedQuery(
                "getAlbumsForRoot", PersistentAlbum.class );
        theQuery.setParameter( "rootId", inRootId );
        return theQuery.getResultList();
    }

    public static List< PersistentAlbum > getUnknownAlbums( Long inRootId ) {
        return getAlbumsForName( inRootId, LibraryItem.sUnknown );
    }

    protected Collection< ? extends PersistentObject > getTracks() { return children; }
}