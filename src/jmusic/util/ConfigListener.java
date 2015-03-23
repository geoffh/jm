package jmusic.util;

public interface ConfigListener {
    public void onConfigChange( String inKey, String inOldValue, String inNewValue );
}