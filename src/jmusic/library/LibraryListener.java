package jmusic.library;

public interface LibraryListener {
    void onObjectCreate( LibraryItem inObject );
    
    void onObjectDestroy( LibraryItem inObject );
    
    void onObjectUpdate( LibraryItem inObject );
}