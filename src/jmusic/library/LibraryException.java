package jmusic.library;

public class LibraryException extends Exception {
    public enum ErrorCode {
        NoTag,
        UnsupportedOperation,
        Unknown
    }
    
    private ErrorCode mErrorCode;
    
    public LibraryException( String inMessage ) {
        super( inMessage );
    }
    
    public LibraryException( Exception inCause ) {
        super( inCause );
    }
    
    public LibraryException( String inMessage, Exception inCause ) {
        super( inMessage, inCause );
    }
    
    public ErrorCode getErrorCode() {
        return mErrorCode;
    }
    
    public void setErrorCode( ErrorCode inErrorCode ) {
        mErrorCode = inErrorCode;
    }
}