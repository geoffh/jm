package jmusic.util;

public interface ConfigListener {
    void onConfigChange( String inKey, String inOldValue, String inNewValue );
}