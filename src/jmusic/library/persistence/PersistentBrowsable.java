package jmusic.library.persistence;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.LinkedList;
import java.util.List;

@Entity
@NamedQueries( {
    @NamedQuery( name = "browsable.browse",
        query = "SELECT p FROM PersistentObject p WHERE p.parent = :parent ORDER BY p.name" ),
    @NamedQuery( name = "getBrowsableForId",
        query = "SELECT p FROM PersistentBrowsable p WHERE p.id = :id" )
} )
public abstract class PersistentBrowsable extends PersistentObject {
    public < T > List< T > browse( int inFirstResult, int inMaxResults, PersistentObjectConverter< T > inConverter ) {
        List< T > theObjects = new LinkedList<>();
        for ( PersistentObject theObject : browse( inFirstResult, inMaxResults ) ) {
            theObjects.add( inConverter.convert( theObject ) );
        }
        return theObjects;
    }

    public List< PersistentObject > browse( int inFirstResult, int inMaxResults ) {
        TypedQuery< PersistentObject > theQuery =
            getEntityManager().createNamedQuery(
                "browsable.browse", PersistentObject.class );
        theQuery.setParameter( "parent", this );
        if ( inFirstResult > -1 ) {
            theQuery.setFirstResult( inFirstResult );
            theQuery.setMaxResults( inMaxResults );
        }
        List< PersistentObject > theObjects = theQuery.getResultList();
        if ( ! theObjects.isEmpty() ) {
            int theIndex = 0;
            PersistentPlaylistsRoot thePlaylistsRoot = PersistentPlaylistsRoot.getPlaylistsRoot();
            if ( theObjects.remove( thePlaylistsRoot ) ) {
                theObjects.add( theIndex, thePlaylistsRoot );
                ++ theIndex;
            }
            PersistentCDRoot theCDRoot = PersistentCDRoot.getCDRoot();
            if ( theObjects.remove( theCDRoot ) ) {
                theObjects.add( theIndex, theCDRoot );
            }
        }
        return theObjects;
    }

    public static PersistentBrowsable getBrowsableForId( Long inId ) {
        TypedQuery< PersistentBrowsable > theQuery =
            getEntityManager().createNamedQuery(
                "getBrowsableForId", PersistentBrowsable.class );
        theQuery.setParameter( "id", inId );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }

    public abstract int getMaxBrowseResults();
}