package jmusic.library.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import static jmusic.library.persistence.PersistentObject.getEntityManager;

@Entity
@DiscriminatorValue( "root" )
@NamedQueries( {
    @NamedQuery( name = "getRootForUri",
                 query = "SELECT p FROM PersistentRoot p WHERE p.uri = :uri" ),
    @NamedQuery( name = "getRoots",
                 query = "SELECT p FROM PersistentRoot p ORDER BY p.name" ),
    @NamedQuery( name = "getRootForId",
                 query = "SELECT p FROM PersistentRoot p WHERE p.id = :id" ),
    @NamedQuery( name = "getTracks",
                 query = "SELECT p FROM PersistentTrack p WHERE p.rootId = :rootId" )
} )
public class PersistentRoot extends PersistentContainer {
    @Column( nullable = false, unique = true )
    protected String uri;
    
    public String getUri() { return uri; }
    
    public void setUri( String inUri ) { uri = inUri; }
    
    @Override
    protected boolean canAddChild( PersistentObject inObject ) {
        return inObject instanceof PersistentArtist ||
               inObject instanceof PersistentRoot ;
    }
    
    public static PersistentRoot getRootForUri( String inUri ) {
        TypedQuery< PersistentRoot > theQuery =
            getEntityManager().createNamedQuery(
                "getRootForUri", PersistentRoot.class );
        theQuery.setParameter( "uri", inUri );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }

    public Long getRootId() { return id; }
    
    public static List< PersistentRoot > getRoots() {
        TypedQuery< PersistentRoot > theQuery =
            getEntityManager().createNamedQuery(
                "getRoots", PersistentRoot.class );
        try {
            return theQuery.getResultList();
        } catch( NoResultException theException ) {
            return Collections.emptyList();
        }
    }

    public static PersistentRoot getRootForId( Long inId ) {
        TypedQuery< PersistentRoot > theQuery =
            getEntityManager().createNamedQuery(
                "getContainerForId", PersistentRoot.class );
        theQuery.setParameter( "id", inId );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }

    protected Collection< ? extends PersistentObject > getTracks() {
        TypedQuery< PersistentTrack > theQuery =
            getEntityManager().createNamedQuery(
                "getTracks", PersistentTrack.class );
        theQuery.setParameter( "rootId", id );
        try {
            return theQuery.getResultList();
        } catch( NoResultException theException ) {
            return Collections.emptyList();
        }
    }
}