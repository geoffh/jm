package jmusic.util;

import jmusic.library.LibraryItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class PlaySelection {
    private final ArrayList< LibraryItem > mItems = new ArrayList<>();
    private int mIndex = -1;
    private final boolean isShuffle;
    private final Random mRandom;

    public PlaySelection( boolean inIsShuffle ) {
        isShuffle = inIsShuffle;
        mRandom = isShuffle ? new Random( System.currentTimeMillis() ) : null;
    }

    public void addItem( LibraryItem inItem ) {
        addItems( Arrays.asList( inItem ) );
    }

    public void addItems( Collection< LibraryItem > inItems ) {
        synchronized( mItems ) {
            mItems.addAll( inItems );
        }
    }

    public LibraryItem current() { return mItems.get( mIndex ); }

    public void forward() {
        ++ mIndex;
    }

    public boolean isEmpty() { return mItems.isEmpty(); }

    public LibraryItem next() {
        synchronized( mItems ) {
            if ( ! isShuffle && mIndex >= mItems.size() - 1 ) {
                return null;
            }
            mIndex = isShuffle ? mRandom.nextInt( mItems.size() ) : mIndex + 1;
            return mItems.get( mIndex );
        }
    }

    public LibraryItem previous() {
        synchronized( mItems ) {
            if ( ! isShuffle ) {
                mIndex = mIndex > 0 ? mIndex - 1 : 0;
            } else {
                mIndex = mRandom.nextInt( mItems.size() );
            }
            return mItems.get( mIndex );
        }
    }

    public void rewind() {
        mIndex = mIndex > 0 ? mIndex - 1 : 0;
    }
}