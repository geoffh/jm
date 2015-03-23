package jmusic.library.persistence;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

public class PersistentObjectListener {
    @PrePersist void onPrePersist( Object inObject ) {}
    
    @PostPersist void onPostPersist( Object inObject ) {
        PersistenceManager.onPostPersist( ( PersistentObject )inObject );
    }
    
    @PostLoad void onPostLoad( Object inObject ) {}
    
    @PreUpdate void onPreUpdate( Object inObject ) {}
    
    @PostUpdate void onPostUpdate( Object inObject ) {
        PersistenceManager.onPostUpdate( ( PersistentObject )inObject );
    }
    
    @PreRemove void onPreRemove( Object inObject ) {}
    
    @PostRemove void onPostRemove( Object inObject ) {
        PersistenceManager.onPostRemove( ( PersistentObject )inObject );
    }
}