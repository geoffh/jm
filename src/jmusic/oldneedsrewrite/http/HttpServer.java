package jmusic.oldneedsrewrite.http;

public class HttpServer {
    private static final HttpServer sInstance = new HttpServer();
    private final RequestListener mRequestListener = new RequestListener();

    private HttpServer() {}

    public void addDocRoot( String inDocRoot ) {
        mRequestListener.addDocRoot( inDocRoot );
    }

    public static HttpServer getInstance() { return sInstance; }

    public String getURL( String inFile ) {
        return mRequestListener.getURL( inFile );
    }

    public void removeDocRoot( String inDocRoot ) {
        mRequestListener.removeDocRoot( inDocRoot );
    }
}
