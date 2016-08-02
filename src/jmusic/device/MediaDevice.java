package jmusic.device;

public interface MediaDevice {
    enum Protocol {
        UPNP
    }
    String getId();
    String getName();
    Protocol getProtocol();
    String toString();
}