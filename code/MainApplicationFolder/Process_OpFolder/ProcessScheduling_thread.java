package MainApplicationFolder.Process_OpFolder;

import MainApplicationFolder.GUI_OpFolder.ScreenBlankShow;
import MainApplicationFolder.Hard_OpFolder.CPU;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.Job_OpFolder.JOB;
import MainApplicationFolder.IO_OpFolder.JobComeFile;
import MainApplicationFolder.Memory_OpFolder.MMU;
import MainApplicationFolder.Memory_OpFolder.Memory;

import java.util.*;

import static MainApplicationFolder.Memory_OpFolder.Memory.FreeMem;

public class ProcessScheduling_thread implements Runnable{
    public static boolean running=false;
    public static int Times =3;
    private static final Object lockcpu = CPU.class;

    //获取选择框的值
    public static boolean selectstaticPri =true;//默认选择优先级调度




    @Override
    public void run()
    {
        while (true) {
            synchronized (lockcpu) {
                if (!CPU.DoType_01) {
                        ProcessScheduling();
                }
            }
            synchronized (this) {
                if (running) {
                    running = false;
                    synchronized (lockcpu) {
                        CPU.ActivateFlag = true;//激活CPU
                    }
                }
            }

        }
    }
    private static synchronized void ProcessScheduling() {

        JobAnswer();
        TimeRRScheduling();
    }



    private static void JobAnswer() {
        while(!JobComeFile.JobSaveQueue.isEmpty()) {
         synchronized (Memory.class) {
             if (Memory.InMemPro >16) break;//内存中进程数大于16，不再创建进程
         }
            JOB job = JobComeFile.JobSaveQueue.peek() ;
            if (job == null) {
                throw new AssertionError();}
            PCB pcb = new PCB(job);
            boolean flag = Memory.FindAvailableBlock(pcb);//判断内存是否足够
            if(flag) //内存足够
            {
                   JobComeFile.JobSaveQueue.poll();
                    synchronized (Memory.class) {
                        Memory.InMemPro++;
                        Memory.AllocateMem(pcb); // allocate memory for the process
                        MMU.WriteIR(pcb);//指令集写入MMU
                        System.out.println("进程分配内存成功！" + "-->" + pcb.ProID);
                    }

                    System.out.println("进程创建成功！" + "-->" + pcb.ProID);
                    String s = String.format("创建进程:%d,%d,首次适应分配", pcb.ProID, pcb.StartAddress);
                    ScreenBlankShow.ScreenShowInfoProJob(s);
                    Memory.ReadyQueue.add(pcb);

                    DeadLock_Thread.lock.lock();
                    try {
                        DeadLock_Thread.pcbs = Memory.ReadyQueue.toArray(new PCB[0]);
                    }
                    finally {
                        DeadLock_Thread.lock.unlock();
                    }

                    String s2 = "进入就绪队列:" + pcb.ProID + ":" + pcb.CalcLeftIRs();
                    ScreenBlankShow.ScreenShowInfoProJob(s2);

                    synchronized (Clock_thread.class) {
                        pcb.InTimes = Clock_thread.COUNTTIME;
                    }
            }
            else
            {
                break;
            }
        }
    }
    private static void TimeRRScheduling(){
        PCB pcb =Memory.RunningPro;
        boolean[] flag =calculateCases(Memory.ReadyQueue,Times,pcb);

        if(!(flag[0]||flag[1]||flag[2]||flag[3])) return;
        synchronized (Memory.class) {
            if (Memory.RunningPro != null) {
                if (!Memory.RunningPro.ProEnd()) {
                    Memory.ReadyQueue.add(Memory.RunningPro);

                    ScreenBlankShow.ScreenShowInfoProJob("重新进入就绪队列:" +
                            Memory.RunningPro.ProID +
                            "," +
                            Memory.RunningPro.CalcLeftIRs());
                } else {
                    FreeMem(Memory.RunningPro);
                    PCB pcb1 = Memory.RunningPro;
                    String EndInfo ="终止进程:"+pcb1.ProID;
                    synchronized (Clock_thread.class) {
                        pcb1.EndTimes = Clock_thread.COUNTTIME;
                    }

                    ScreenBlankShow.ScreenShowInfoProJob(EndInfo);//显示进程结束信息

                    ScreenBlankShow.ShowProsTimeInfo(pcb1);//显示进程的时间类评价数据

                    JobComeFile.GCPCBQueue.add(pcb1);//回收进程
                    CalcAWT_ART(pcb1);//记录已经完成的进程的周转时间，带权周转时间

                    String s = String.format("%d:%d+%d+%d", pcb1.ProID, pcb1.job.InTimes, pcb1.InTimes, pcb1.CalcRunningTimes());
                    StringBuffer bufferWriter = ScreenBlankShow.bufferWriter;
                    bufferWriter.append(Clock_thread.COUNTTIME);
                    for (String string : Arrays.asList(":", "[", s, "]", "\n")) {
                        bufferWriter.append(string);
                    }
                    Memory.InMemPro--;
                }

            }
            if(selectstaticPri)
            {
                //System.out.println("优先级调度");
                Memory.ReadyQueue.sort(PCB.ORDER_BY_Pri);//按照优先级排序
            }
            Memory.RunningPro = Memory.ReadyQueue.poll();
            if (Memory.RunningPro != null) {
                MMU.GetIRAddr(Memory.RunningPro);
            }

        }
        Times =3;//时间片重置
    }
    private static void CalcAWT_ART(PCB pcb)
    {
        //将JobComeFile.GCPCBQueue中的进程的周转时间，带权周转时间计算出来,并添加到PCB类中
        PCB.TurnRoundTime.add(pcb.EndTimes-pcb.job.InTimes);//周转时间
        double time = (double) (pcb.EndTimes - pcb.job.InTimes) /pcb.CalcRunningTimes();
        PCB.WeightedTurnRoundTime.add(time);//带权周转时间
    }
    public static boolean[] calculateCases(Queue<PCB> readyQueue, int timeSlice, PCB process) {
        return new boolean[]{
                !readyQueue.isEmpty() && process == null,
                timeSlice <= 0,
                process != null && process.ProEnd(),
                process != null && !process.ProEnd() && process.job.instructs.get(process.PC).NeedRunTime() > timeSlice
        };
    }

    public static void main(String[] args) {
        PCB pcb = new PCB(new JOB(1, 1, 1, null));
        Memory.ReadyQueue.add(pcb);
        PCB pcb1 = new PCB(new JOB(2, 1, 1, null));
        Memory.ReadyQueue.add(pcb1);
        DeadLock_Thread.pcbs = Memory.ReadyQueue.toArray(new PCB[0]);//将就绪队列中的进程赋值给死锁检测类的PCB[]类型的pcbs
        System.out.println(Arrays.toString(DeadLock_Thread.pcbs));
        System.out.println(DeadLock_Thread.pcbs[0].ProID);
    }
}
