package jmusic.library.persistence;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Entity
@DiscriminatorValue( "cdroot" )
@NamedQueries( {
    @NamedQuery( name = "getCDRoot",
        query = "SELECT p FROM PersistentCDRoot p" )
} )
public class PersistentCDRoot extends PersistentRoot {
    public static PersistentCDRoot getCDRoot() {
        TypedQuery< PersistentCDRoot > theQuery =
            getEntityManager().createNamedQuery(
                "getCDRoot", PersistentCDRoot.class );
        try {
            return theQuery.getResultList().get( 0 );
        } catch( NoResultException theException ) {
            return null;
        }
    }
}