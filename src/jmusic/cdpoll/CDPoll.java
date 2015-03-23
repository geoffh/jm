package jmusic.cdpoll;

public interface CDPoll {
    public void addListener( CDPollListener inListener );
    public void poll();
    public void removeListener( CDPollListener inListener );
}
