package MainApplicationFolder.Job_OpFolder;

import MainApplicationFolder.GUI_OpFolder.GetNewSwingForm;
import MainApplicationFolder.GUI_OpFolder.ScreenBlankShow;
import MainApplicationFolder.Hard_OpFolder.CPU;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.IO_OpFolder.JobComeFile;
import MainApplicationFolder.Process_OpFolder.ProcessScheduling_thread;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class JobIn_thread implements Runnable{
    public static boolean jobIn_thread_running = false;
    private static final Object LockForThis = new byte[0];
    private static final Object lockforclock =Clock_thread.class;
    private int arrive_time =-2;
    @Override
    public void run() {
        while (true)
        {
            synchronized (lockforclock){
                if (arrive_time < Clock_thread.COUNTTIME) {
                    arrive_time = Clock_thread.COUNTTIME;
                    if (arrive_time % 10 == 0) {
                        CheckJob();
                    }
                }
            }
            synchronized (LockForThis) {
                if (jobIn_thread_running) {
                    jobIn_thread_running = false;
                    synchronized (ProcessScheduling_thread.class) {
                        ProcessScheduling_thread.running = true;//唤醒进程调度线程
                    }
                }
            }
        }
    }
    private void CheckJob() {
        synchronized (LockForThis) {
            int currentindex = JobComeFile.Index;//当前作业索引
            List<JOB> jobsToAdd =JobComeFile.JobComeTempList.stream()
                    .skip(currentindex)
                    .filter(job->job.InTimes<=Clock_thread.COUNTTIME)
                    .collect(Collectors.toList());//获取当前时刻到达的作业
            synchronized (JobComeFile.class) {
                JobComeFile.JobSaveQueue.addAll(jobsToAdd);//将作业加入后备队列
            }
            currentindex += jobsToAdd.size();//更新当前作业索引
            jobsToAdd.forEach(job->{
                String str = String.format("新增作业:%d,%d,%d", job.JobsID, job.InTimes, job.InstrucNum);
                ScreenBlankShow.ScreenShowInfoProJob(str);
            });
            JobComeFile.Index = currentindex;
        }
        GetNewSwingForm.JObIO.set(0);//作业结束
    }

}
