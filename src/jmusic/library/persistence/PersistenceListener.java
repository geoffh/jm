package jmusic.library.persistence;

public interface PersistenceListener {
    void onPostPersist( PersistentObject inObject );

    void onPostRemove( PersistentObject inObject );

    void onPostUpdate( PersistentObject inObject );
}