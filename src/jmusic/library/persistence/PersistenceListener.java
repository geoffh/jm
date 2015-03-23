package jmusic.library.persistence;

public interface PersistenceListener {

    public void onPostPersist( PersistentObject inObject );

    public void onPostRemove( PersistentObject inObject );

    public void onPostUpdate( PersistentObject inObject );
}