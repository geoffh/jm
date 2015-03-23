package jmusic.device;

public interface MediaDevice {
    public enum Protocol {
        UPNP
    }
    public String getId();
    public String getName();
    public Protocol getProtocol();
    public String toString();
}