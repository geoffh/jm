package jmusic.library.persistence;

public interface PersistentObjectConverter< T > {
    T convert( PersistentObject inObject );
}