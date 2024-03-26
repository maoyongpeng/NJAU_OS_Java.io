package MainApplicationFolder.Process_OpFolder;

import MainApplicationFolder.GUI_OpFolder.ScreenBlankShow;
import MainApplicationFolder.Hard_OpFolder.CPU;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.Job_OpFolder.IRs;
import MainApplicationFolder.Memory_OpFolder.Memory;

import java.util.concurrent.atomic.AtomicInteger;

public class InputBlock_thread implements Runnable{
    private static final Object lockcpu = CPU.class;
    private static final Object lockmem = Memory.class;
    private static final Object lockthis = new byte[0];
    private static final Object lockforclock = Clock_thread.class;

    private static final String InfoTag ="重新进入就绪队列:";

    static AtomicInteger neartime = new AtomicInteger();
    static AtomicInteger Updatetime = new AtomicInteger();
    @Override
    public void run() {
        neartime.set(0);
        Updatetime.set(0);
        synchronized (lockforclock) {
           neartime.set(Clock_thread.COUNTTIME);
        }
        while(true)
        {
            synchronized (lockforclock) {
                if (neartime.get() < Clock_thread.COUNTTIME) {
                    neartime.set(Clock_thread.COUNTTIME);
                    Updatetime.incrementAndGet();
                }
            }

            if(Updatetime.get()>=2)
            {
                synchronized (lockcpu)
                {
                    if(!CPU.DoType_01)
                    {
                       synchronized (lockmem) {
                           PCB pcb = Memory.InPutBlockQueue.poll();
                           if (pcb == null) {
                               throw new AssertionError();
                           }
                           Memory.ReadyQueue.add(pcb);

                           CPU.SysBlockType =CPU.SystemCallType.None;

                           ScreenBlankShow.ScreenShowInfoProJob(InfoTag + pcb.ProID + "," + pcb.CalcLeftIRs());
                       }
                        break;
                    }
                }
            }

        }
    }

}
