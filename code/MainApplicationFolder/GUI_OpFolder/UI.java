package MainApplicationFolder.GUI_OpFolder;


import MainApplicationFolder.Hard_OpFolder.CPU;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.IO_OpFolder.JobComeFile;
import MainApplicationFolder.Job_OpFolder.JobIn_thread;
import MainApplicationFolder.Memory_OpFolder.Memory;
import MainApplicationFolder.Process_OpFolder.PCB;
import MainApplicationFolder.Process_OpFolder.ProcessScheduling_thread;
import MainApplicationFolder.Process_OpFolder.DeadLock_Thread;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UI {
    public static OsFrameMainForm osFrameMainForm = new OsFrameMainForm();//创建一个操作系统主界面
    static Thread clockThread = new Thread(new Clock_thread());//创建一个线程，用于记录时间
    static Thread RefreshForm = new Thread(new GetNewSwingForm());//创建一个线程，用于刷新界面
    static  Thread CPUThread = new Thread(new CPU());//创建一个线程，用于模拟CPU
    static  Thread ProcessSchedulingThread = new Thread(new ProcessScheduling_thread());//创建一个线程，用于模拟进程调度
    static  Thread JobThread = new Thread(new JobIn_thread());//创建一个线程，用于模拟作业请求

    static Thread DeadLockThread = new Thread(new DeadLock_Thread());//创建一个线程，用于模拟死锁检测

    static List<Thread> Kernelthreads = Arrays.asList(
            clockThread,
            CPUThread,
            JobThread,
            ProcessSchedulingThread,
            RefreshForm,
            DeadLockThread

    );
    private static void setNames()
    {
        clockThread.setName("ClockThread");
        RefreshForm.setName("RefreshForm");
        CPUThread.setName("CPUThread");
        JobThread.setName("JobThread");
        ProcessSchedulingThread.setName("ProcessSchedulingThread");
        DeadLockThread.setName("DeadLockThread");
    }
    public static void ApplicationEventsBegin() throws IOException {
        setNames();//设置线程名字
        JobComeFile.LoadJobs();//读取作业文件
        Kernelthreads.forEach(Thread::start);

    }
    public static void ApplicationEventsStop() {
        //让所有线程暂停执行
        Kernelthreads.forEach(Thread::suspend);

    }
    public static void ApplicationEventsRestart() {
        //让所有线程恢复执行
        Kernelthreads.forEach(Thread::resume);

    }
    public static  void LoginMainForm()
    {
        SwingUtilities.invokeLater(() -> {
            JFrame OsFrame = new JFrame();
            OsFrame.setContentPane(InitMainPanel());
            OsFrame.setTitle("2023操作系统仿真设计");
            OsFrame.setBounds(100,100,1600,800);
            OsFrame.setVisible(true);//设置窗口可见
            OsFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//设置默认关闭操作
            OsFrame.setResizable(true);//设置窗口可改变大小
            OsFrame.setLocationRelativeTo(null);//设置窗口居中
        });//创建一个线程，用于显示窗口内容
    }
    public static JPanel InitMainPanel()
    {
        return osFrameMainForm.panelMainForm;//返回主界面
    }
}
