package jmusic.util;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JMusicExecutor {
    private static final JMusicExecutor sInstance = new JMusicExecutor();
    private static final int sExecutorThreadCount = 10;
    private final ExecutorService mExecutorService =
        Executors.newFixedThreadPool( sExecutorThreadCount );
    private final Timer mTimer = new Timer();

    public static void scheduleTask( TimerTask inTask, long inDelay ) {
        sInstance.mTimer.schedule( inTask, inDelay );
    }

    static <T> List< Future < T > > invokeAll(
        Collection< ? extends Callable< T > > inTasks )
        throws InterruptedException {
        return sInstance.mExecutorService.invokeAll( inTasks );
    }
    
    static <T> Future<T> submit( Callable< T >  inTask ) {
        return sInstance.mExecutorService.submit( inTask) ;
    }
    

}