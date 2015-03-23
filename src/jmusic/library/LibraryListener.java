package jmusic.library;

public interface LibraryListener {
    public void onObjectCreate( LibraryItem inObject );
    
    public void onObjectDestroy( LibraryItem inObject );
    
    public void onObjectUpdate( LibraryItem inObject );
}