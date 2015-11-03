package jmusic.youtube;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Youtube {
    private static final String sApplicationName = "JMusic";
    private static final String sAPIKey = "AIzaSyAYhQtEBRSUneIdh-rqFuPcqNR0T5C_aCE";
    private static final String sSearchFields = "items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)";
    private static final String sSearchPart = "id,snippet";
    private static final String sSearchTypeVideo = "video";
    private static final long sDefaultMaxResults = 25;

    public void search( String inSearchTerm ) throws IOException {
        search( inSearchTerm, null, sDefaultMaxResults );
    }

    public void search( String inSearchTerm, String inPageToken, long inMaxResults ) throws IOException {
        YouTube theYouTube = new YouTube.Builder( new NetHttpTransport(), new GsonFactory(), new HttpRequestInitializer() {
            public void initialize( HttpRequest request ) throws IOException {
            }
        } ).setApplicationName( sApplicationName ).build();
        YouTube.Search.List theSearch = theYouTube.search().list( sSearchPart );
        theSearch.setFields( sSearchFields );
        theSearch.setKey( sAPIKey );
        theSearch.setQ( inSearchTerm );
        theSearch.setMaxResults( inMaxResults );
        theSearch.setType( sSearchTypeVideo );
        if ( inPageToken != null ) {
            theSearch.setPageToken( inPageToken );
        }
        SearchListResponse theResponse = theSearch.execute();
    }

    public void vSearch( List< SearchResult > inSearchResultList ) throws IOException {
        StringBuilder theStringBuilder = null;
        for ( SearchResult theSearchResult : inSearchResultList ) {
            String theId = theSearchResult.getId().getVideoId();
            if ( theStringBuilder != null ) {
                theStringBuilder.append( "," );
            } else {
                theStringBuilder = new StringBuilder();
            }
            theStringBuilder.append(theId);
        }
        YouTube theYouTube = new YouTube.Builder( new NetHttpTransport(), new GsonFactory(), new HttpRequestInitializer() {
            public void initialize( HttpRequest request ) throws IOException {
            }
        } ).setApplicationName( sApplicationName ).build();
        YouTube.Videos.List vSearch = theYouTube.videos().list( "snippet, player" );
        vSearch.setKey( "AIzaSyAYhQtEBRSUneIdh-rqFuPcqNR0T5C_aCE" );
        vSearch.setId( theStringBuilder.toString() );
        VideoListResponse vListResponse = vSearch.execute();
        for ( Video theVideo : vListResponse.getItems() ) {
            VideoPlayer thePlayer = theVideo.getPlayer();
            if ( thePlayer != null ) {
                String s = theVideo.getPlayer().getEmbedHtml();
                int theIndex1 = s.indexOf( "src=" );
                if ( theIndex1 > 0 ) {
                    int theIndex2 = s.indexOf( "\"", theIndex1 + 5 );
                    if ( theIndex2 > 0 ) {
                        String theUrl = s.substring( theIndex1 + 5, theIndex2 );
                        System.out.println(theUrl);
                    }
                }
            }
        }
    }

    private void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + sDefaultMaxResults + " videos for search on \"" + query + "\"." );
        System.out.println( "=============================================================\n" );

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

                System.out.println(" Video Id:" + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }
}