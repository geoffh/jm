package jmusic.cdpoll;

import java.util.ArrayList;

public abstract class AbstractCDPoll implements CDPoll{
    private final ArrayList< CDPollListener > mListeners = new ArrayList<>();

    @Override
    public void addListener( CDPollListener inListener ) {
        synchronized( mListeners ) {
            mListeners.add( inListener );
        }
    }

    @Override
    public void removeListener( CDPollListener inListener ) {
        synchronized( mListeners ) {
            mListeners.remove( inListener );
        }
    }

    protected void notifyCDEjected() {
        synchronized( mListeners ) {
            for ( CDPollListener theListener : mListeners ) {
                theListener.cdEjected();
            }
        }
    }

    protected void notifyCDInserted( String inUri ) {
        synchronized( mListeners ) {
            for ( CDPollListener theListener : mListeners ) {
                theListener.cdInserted( inUri );
            }
        }
    }
}