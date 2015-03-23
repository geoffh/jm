package jmusic.library.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

@MappedSuperclass
@SequenceGenerator( name = "seq", initialValue = 0 )
@EntityListeners( PersistentObjectListener.class )
@NamedQueries( {
    @NamedQuery( name = "getObjectForId",
                 query = "SELECT p FROM PersistentObject p WHERE p.id = :id" )
} )
public abstract class PersistentObject implements Serializable {
    @Transient
    protected final Logger mLogger;
    @Id @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "seq" )
    protected Long id;
    @Column( nullable = false )
    protected String name;
    protected PersistentContainer parent;
    protected Long rootId;
    
    PersistentObject() {
        mLogger = Logger.getLogger(  getClass().getName() );
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public PersistentContainer getParent() { return parent; }
    public Long getRootId() { return rootId; }
    
    public void setName( String inName ) { name = inName; }
    public void setParent( PersistentContainer inParent ) { parent = inParent; }
    public void setRootId( Long inRootId ) { rootId = inRootId; }

    public static void beginTransaction() {
        getEntityManager().getTransaction().begin();
    }
    
    public < T > T commit() {
        EntityManager theManager = PersistenceManager.getEntityManager();
        EntityTransaction theTransaction = theManager.getTransaction();
        boolean isActive = theTransaction.isActive();
        if ( ! isActive ) {
            theTransaction.begin();
        }
        theManager.persist( this );
        if ( ! isActive ) {
            theTransaction.commit();
        }
        return ( T ) this;
    }
    
    public void destroy() {
        EntityManager theManager = PersistenceManager.getEntityManager();
        EntityTransaction theTransaction = theManager.getTransaction();
        boolean isActive = theTransaction.isActive();
        if ( ! isActive ) {
            theTransaction.begin();
        }
        theManager.remove( this );
        if ( ! isActive ) {
            theTransaction.commit();
        }
    }

    public static void endTransaction() {
        getEntityManager().getTransaction().commit();
    }
    
    public static EntityManager getEntityManager() {
        return PersistenceManager.getEntityManager();
    }

    public static PersistentObject getObjectForId( Long inId ) {
        TypedQuery< PersistentObject > theQuery =
            getEntityManager().createNamedQuery(
                "getObjectForId", PersistentObject.class );
        theQuery.setParameter( "id", inId );
        try {
            return theQuery.getSingleResult();
        } catch( NoResultException theException ) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "[ " + id + " ] " + name;
    }
}