package jmusic.cdpoll;

public interface CDPollListener {
    public void cdEjected();
    public void cdInserted( String inUri );
}