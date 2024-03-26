package MainApplicationFolder.GUI_OpFolder;


import MainApplicationFolder.Hard_OpFolder.CPU;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.IO_OpFolder.JobComeFile;
import MainApplicationFolder.Memory_OpFolder.Block;
import MainApplicationFolder.Memory_OpFolder.MMU;
import MainApplicationFolder.Memory_OpFolder.Memory;
import MainApplicationFolder.Process_OpFolder.DeadLock_Thread;
import  MainApplicationFolder.Process_OpFolder.PCB;
import  MainApplicationFolder.Job_OpFolder.JOB;
import MainApplicationFolder.Process_OpFolder.ProcessScheduling_thread;

import javax.swing.*;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class GetNewSwingForm implements Runnable{//线程用于刷新界面


    private  static  final Object lockJobsCome = JobComeFile.class;//作业进入锁对象
    private  static  final  Object lockthis = new byte[0];//本类锁对象
    private static  final  Object lockformem = Memory.class;



    private String GetProNum()//获取进程数
    {
        synchronized (lockformem)
        {
            return String.valueOf(Memory.InMemPro);
        }
    }
    private String[] GetDynamicMem()//获取动态内存
    {
        String [] strings = new String[2];
        synchronized (lockformem)
        {
            strings[0]=Memory.CalcStackAndHeap()[0];//栈
            strings[1]=Memory.CalcStackAndHeap()[1];//堆
        }
        return strings;
    }

    private static String GetSysTime()//获取系统时间
    {
        return Clock_thread.COUNTTIME+"s";
    }

    //TODO从内存中获取PCB信息
    private String GetCurrentPID()//获取当前运行进程的ID号
    {
       String string ="";
       PCB pcb = null;
       pcb = Memory.RunningPro;
       if(pcb!=null)
       {
           string ="("+ pcb.ProID +")";
           return string;
       }
       return "无";
    }
    private  String GetCPUStatus()//获取CPU状态
   {
       StringBuilder status =new StringBuilder();
       if (!Objects.equals(CPU.SysBlockType.toString(), "None"))
           status.append("内核");
       else
           status.append("用户");

       return status.append("态").toString();
   }
   private String GetBlockType()//获取阻塞类型
   {
       String Type ="null";
       switch (CPU.SysBlockType.toString())
       {
           case "None":
               Type ="null";
               break;
           case "Input":
               Type ="键盘输入";
               break;
           case "Output":
               Type ="屏幕输出";
               break;
           default:
               throw new IllegalStateException("Unexpected value: " + CPU.SysBlockType.toString());
       }
       return Type;
   }

    private String GetPCBQueueInfo(Queue<PCB> PQ)//获取PCB队列信息
    {
        if (PQ.isEmpty())
            return " ";
        LinkedList<PCB> PCBQueue = new LinkedList<>(PQ);
        StringBuilder stringBuilder = new StringBuilder();
        for (PCB pcb : PCBQueue) {
            stringBuilder.append(pcb.toString()).append(",");
        }
        return stringBuilder.toString();
    }

    private String GetJobQueueInfo(Queue<JOB> JQ)//获取作业队列信息
    {
        if (JQ.isEmpty()) return "";
        LinkedList<JOB> JobQueue = new LinkedList<>(JQ);
        StringBuilder stringBuilder = new StringBuilder();
        for (JOB job : JobQueue) {
            stringBuilder.append(job.toString()).append(",");
        }
        return stringBuilder.toString();
    }
    private static void GetMemBlock()//获取内存块信息
    {
        for (int i = 0; i < Memory.MEMORY_SIZE; i++) {
            Block block = Memory.AllocationMem.get(i);
            String blockInfo;
            Color backgroundColor;
            if (!block.Occupied) {
                blockInfo = "空闲";
                backgroundColor = new Color(175, 238, 238); // 淡蓝色
            } else {
                blockInfo = String.format("(%d)占用", block.ProID);
                backgroundColor = new Color(255, 192, 203); // 淡粉色
            }
            OsFrameMainForm.MemBlocks[i].setText(blockInfo);
            OsFrameMainForm.MemBlocks[i].setBackground(backgroundColor);
        }

    }
    private String GetMMU()//获取MMU信息
    {
        StringBuilder stringBuilder = new StringBuilder();
        synchronized (Memory.class) {
            if (!Memory.ReadyQueue.isEmpty()) {
                for (PCB pcb : Memory.ReadyQueue) {
                    stringBuilder.append("[就绪进程ID:").
                            append(pcb.ProID).
                            append("],[起始地址:").
                            append(pcb.StartAddress).
                            append("],[结束地址:").
                            append(pcb.EndAddress).
                            append("],[PC计数:").
                            append(pcb.PC).
                            append("],[优先级:").
                            append(pcb.Priority).
                            append("]").
                            append("\n\n");
                }
            }
            if (Memory.RunningPro != null) {
                stringBuilder.append("[运行进程ID:").
                        append(Memory.RunningPro.ProID).
                        append("],[起始地址:").
                        append(Memory.RunningPro.StartAddress).
                        append("],[结束地址:").
                        append(Memory.RunningPro.EndAddress).
                        append("],[PC计数:").
                        append(Memory.RunningPro.PC).
                        append("],[优先级:").
                        append(Memory.RunningPro.Priority).
                        append("],[").
                        append(Memory.RunningPro.GetIRState()).
                        append("]\n\n");
            }
        }
        stringBuilder.append("\n\n");
        //MMU内容
        stringBuilder.append("MMU内容:\n");
        MMU.mmulock.lock();//加锁
        try {
            stringBuilder.append(MMU.showPCBMMU());
        }
        finally {
            MMU.mmulock.unlock();
        }
        return stringBuilder.toString();
    }
    private String  GetCompletePro()//
    {
        StringBuilder stringGCProInfo= new StringBuilder();
            for (PCB pcb : JobComeFile.GCPCBQueue) {
               stringGCProInfo.append("(PCB").append(pcb.ProID).append(")").append(",");
            }
        return stringGCProInfo.toString();
    }
    private static ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
    public static String GetThreadNum()//获取线程数
    {
        int activeCount = currentGroup.activeCount();
        return String.valueOf(activeCount);
    }
    private String[] GetCurrentIRAddr()
    {
        PCB pcb =Memory.RunningPro;
        String [] strings=new String[2];
        if(pcb!=null)
        {
           strings[0]="Logic:"+ MMU.GetIRAddr(pcb)[0];
           strings[1]="Physic:"+MMU.GetIRAddr(pcb)[1];
           return strings;
        }
        strings[0]=strings[1]= "\u65E0";
        return strings;
    }
    private double getMemPercent()
    {
        return Memory.CalculateMemPercent();
    }

    public static AtomicInteger JObIO = new AtomicInteger(0);
    private static ReentrantLock lock = new ReentrantLock();
    private static void IOStatus()
    {
        lock.lock();
        try {
            UI.osFrameMainForm.textFieldIO.setText(JObIO.get() == 1 ? "Input" : JObIO.get() == 2 ? "Output" : "NullOp");
        }
        finally {
            lock.unlock();
        }
    }

    private static void ShowDeadLock()
    {
        String[] strings;
        strings =DeadLock_Thread.strInfo;
            if (Objects.equals(strings[0], "死锁")) {
                UI.osFrameMainForm.textFieldDetectDeadlock.setText(strings[0]);
            } else if (Objects.equals(strings[0], "安全"))
            {
                UI.osFrameMainForm.textFieldDetectDeadlock.setText(strings[0]);
            }
            else
            {
                UI.osFrameMainForm.textFieldDetectDeadlock.setText("安全");
            }
            if (strings[1] != null) {
                UI.osFrameMainForm.textFieldSafeSuq.setText(strings[1]);
        }
            if (Memory.ReadyQueue.isEmpty()) {
                UI.osFrameMainForm.textFieldSafeSuq.setText("<NULL>");
                UI.osFrameMainForm.textFieldDetectDeadlock.setText("安全");
            }

    }
    private static String LeftTimeSlice()
    {
        return ProcessScheduling_thread.Times+"s";
    }
    private static final OsFrameMainForm Form = UI.osFrameMainForm;
    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(Clock_thread.Frequency);
            } catch (Exception e) {
                throw new RuntimeException(e.getCause());
            }
            synchronized (lockthis) {
                updateForm();
            }
        }
    }

    private void updateForm() {

        SwingUtilities.invokeLater(() -> {
            GetNewSwingForm.Form.textFieldSysTime.setText(GetSysTime());//刷新系统时间
            GetNewSwingForm.Form.textFieldProcess.setText(GetCurrentPID());//显示正在运行的进程ID号
            GetNewSwingForm.Form.textFieldLeftTime.setText(LeftTimeSlice());//显示剩余时间片
            GetNewSwingForm.Form.textFieldInputBlockQ1.setText(GetPCBQueueInfo(Memory.InPutBlockQueue));//显示阻塞队列1
            GetNewSwingForm.Form.textFieldOutputBlockQ2.setText(GetPCBQueueInfo(Memory.OutPutBlockQueue));//显示阻塞队列2
            GetNewSwingForm.Form.textReadyQueue.setText(GetPCBQueueInfo(Memory.ReadyQueue));//显示就绪队列
            GetNewSwingForm.Form.textAreaAddress.setText(GetMMU());//显示地址信息
            ScreenBlankShow.ScreenShowInfoStatus();//刷新屏幕显示,状态信息
            IOStatus();//刷新IO状态
            if (Clock_thread.COUNTTIME % 3 == 0){//每3s刷新一次
                GetNewSwingForm.GetMemBlock();//刷新内存块信息
                GetNewSwingForm.Form.textComeJobs.setText(GetJobQueueInfo(JobComeFile.JobSaveQueue));//刷新作业队列
            }
            Form.textFieldIRlogic.setText(GetCurrentIRAddr()[0]);
            Form.textFieldIRpysical.setText(GetCurrentIRAddr()[1]);
            GetNewSwingForm.Form.textFieldCPU.setText(GetCPUStatus());//显示CPU状态
            GetNewSwingForm.Form.textFieldProNum.setText(GetProNum());//显示进程数

            GetNewSwingForm.Form.textFieldStackMemory.setText(GetDynamicMem()[0]);//显示栈内存
            GetNewSwingForm.Form.textFieldDynamicHeapMem.setText(GetDynamicMem()[1]);//显示动态堆内存


            GetNewSwingForm.Form.textFieldThreadNum.setText(GetThreadNum());//显示线程数
            if(Clock_thread.COUNTTIME%5==0) {//每5s刷新一次
                GetNewSwingForm.Form.textEndQueue.setText(GetCompletePro());
            }//显示已完成进程ID
            Form.textFieldMemPercent.setText(getMemPercent() +"%");//显示内存占用率
            GetNewSwingForm.Form.textFieldSysBlockType.setText(GetBlockType());//显示阻塞类型
            if (Clock_thread.COUNTTIME%10==0) {
                ShowDeadLock();//显示死锁信息
            }
        });
    }
    public static void main(String[] args) {
        System.out.println(GetThreadNum());
    }
}
