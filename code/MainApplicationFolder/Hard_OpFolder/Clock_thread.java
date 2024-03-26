package MainApplicationFolder.Hard_OpFolder;

import MainApplicationFolder.Job_OpFolder.JobIn_thread;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Clock_thread implements Runnable{
    public static int COUNTTIME ;//共享的时钟，用于记录时间

    private static final Object lockthis = new byte[0];//锁对象,用于同步时钟

    public Clock_thread()
    {
        COUNTTIME = 0;
    }
    private  static final Object lockJobin = JobIn_thread.class;

    public static final Integer Frequency = 300;//时钟频率


    @Override
    public void run(){
        while(true)
        {
            TIME_COUNT();
        }
    }
    private void TIME_COUNT()
    {
        try{
            TimeUnit.MILLISECONDS.sleep(Frequency);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
        DoincrementAndActiveJob();
    }
    private void DoincrementAndActiveJob() {
       synchronized (lockthis) {//同步时钟
           COUNTTIME++;//时钟加一
           System.out.println("COUNTTIME: " + COUNTTIME);
           synchronized (lockJobin) {
               JobIn_thread.jobIn_thread_running = true;
           }//激活JobIn_thread
       }
    }
}
