package jmusic.cdpoll;

public class CDPollFactory {
    public static CDPoll getCDPoll() {
        return new jmusic.cdpoll.macos.CDPoll();
    }
}