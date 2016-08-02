package jmusic.library.persistence;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.TypedQuery;
import static jmusic.library.persistence.PersistentObject.getEntityManager;

@Entity
@DiscriminatorColumn( name = "type", discriminatorType = DiscriminatorType.STRING, length = 10 )
@NamedQueries( {
    @NamedQuery( name = "container.browse",
                 query = "SELECT p FROM PersistentObject p WHERE p.parent = :parent ORDER BY p.name" ),
    @NamedQuery( name = "getContainerForId",
                 query = "SELECT p FROM PersistentContainer p WHERE p.id = :id" )
} )
public abstract class PersistentContainer extends PersistentBrowsable {
    private static final PersistentObject[] sPersistentObjectModel =
        new PersistentObject[ 0 ];
    @OneToMany( cascade = CascadeType.ALL, mappedBy = "parent" )
    protected List< PersistentObject > children = new LinkedList<>();
    
    public PersistentObject[] getChildren() {
        return children.toArray( sPersistentObjectModel );
    }
    
    public boolean addChild( PersistentObject inChild ) {
        if ( ! canAddChild( inChild ) ) {
            mLogger.log( Level.WARNING,
                "Cannot add child:{0} to parent:{1}",
                new Object[ ]{ inChild, this });
            return false;
        }
        synchronized( children ) {
            return children.add( inChild );
        }
    }
    
    public PersistentObject getChild( String inName ) {
        for ( PersistentObject theChild : children ) {
            if ( theChild.getName().equals(  inName ) ) {
                return theChild;
            }
        }
        return null;
    }

    public static PersistentContainer getContainerForId( Long inId ) {
        TypedQuery< PersistentContainer > theQuery =
            getEntityManager().createNamedQuery(
                "getContainerForId", PersistentContainer.class );
        theQuery.setParameter( "id", inId );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }
    
    public int getChildCount() { return children.size(); }

    public int getMaxBrowseResults() {
        return getChildCount();
    }

    // Returns unordered list
    public < T > List< T > getTracks( PersistentObjectConverter< T > inConverter ) {
        return getTracks().stream().map( ( Function< PersistentObject, T > ) inConverter::convert ).collect( Collectors.toCollection( LinkedList::new ) );
    }
    
    public boolean removeChild( PersistentObject inChild ) {
        synchronized( children ) {
            return children.remove( inChild );
        }
    }
    
    protected abstract boolean canAddChild( PersistentObject inObject );

    protected Collection< ? extends PersistentObject > getTracks() {
        throw new UnsupportedOperationException( "getTracks isn't implemented" );
    }
}