package MainApplicationFolder.IO_OpFolder;

import MainApplicationFolder.GUI_OpFolder.GetNewSwingForm;
import MainApplicationFolder.GUI_OpFolder.ScreenBlankShow;
import MainApplicationFolder.GUI_OpFolder.UI;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.Job_OpFolder.IRs;
import MainApplicationFolder.Job_OpFolder.JOB;
import MainApplicationFolder.Process_OpFolder.PCB;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobComeFile {
    public static int Index;//作业索引
    public static ArrayList<JOB> JobComeTempList;//临时存放作业
    public static LinkedList<JOB> JobSaveQueue;//作业后备队列
    public static Queue<PCB> GCPCBQueue ;//回收的PCB队列

    static
    {
         JobComeTempList = new ArrayList<>();
         JobSaveQueue = new LinkedList<>();
         Index =0;
         GCPCBQueue = new LinkedList<>();
    }
    //析构函数
    public static void GC()
    {
        JobComeTempList.clear();
        JobSaveQueue.clear();
        Index =0;
        GCPCBQueue.clear();
    }

    public static String dir = "input2";
    public static void LoadJobs() throws IOException {
        String filename = dir + File.separator + "jobs-input.txt";
        try (BufferedReader bfr = new BufferedReader(new FileReader(filename))) {
            JobComeTempList= (ArrayList<JOB>)bfr.lines()
                    .map(line -> line.split(","))
                    .map(jobData -> {
                        int JobsID = Integer.parseInt(jobData[0].trim());
                        int InTimes = Integer.parseInt(jobData[1].trim());
                        int InstrucNum = Integer.parseInt(jobData[2].trim());
                        return createJob(JobsID, InTimes, InstrucNum, LoadIRs(dir, JobsID,InstrucNum));
                    })
                    .collect(Collectors.toList());
        }
    }
    private static JOB createJob(int jobsId, int inTime, int instrucNums, ArrayList<IRs> instructs) {
        GetNewSwingForm.JObIO.set(1);//作业进入
        return new JOB(jobsId, inTime, instrucNums, instructs);
    }
    public static void AddJobs(String path) throws IOException {
        String filename = String.format("%s%sjobs-input.txt", path, File.separator);
        try (BufferedReader bfr = new BufferedReader(new FileReader(filename))) {
            bfr.lines()
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split(","))
                    .forEach(jobData -> {
                        int JobsID = Integer.parseInt(jobData[0].trim());
                        int InTimes;
                           synchronized (Clock_thread.class) {
                               InTimes = Clock_thread.COUNTTIME;
                           }
                        int InstrucNum = Integer.parseInt(jobData[2].trim());
                        JOB job = new JOB(JobsID, InTimes, InstrucNum, LoadIRs(path, JobsID,InstrucNum));

                        String info= String.format("新增作业:%d,%d,%d", job.JobsID, job.InTimes, job.InstrucNum);
                        JobSaveQueue.add(job);
                        GetNewSwingForm.JObIO.set(1);//作业进入
                        ScreenBlankShow.ScreenShowInfoProJob(info);
             });
        }
    }
    private static int TempIndex =new Random().nextInt(50+1)+30;//随机生成一个作业索引
    public static void AddOnePro() throws IOException//添加一个作业直接到后备队列中
    {
        //TODO:添加一个作业
        //随机生成一个作业，然后添加到后备队列中
        TempIndex++;
        int arriveTime;
        synchronized (Clock_thread.class) {
             arriveTime = Clock_thread.COUNTTIME;
        }
        //随机生成指令数目在20-30之间
        int InstrucNum = (int)(Math.random()*10)+20;
        JOB job = createJob(TempIndex, arriveTime, InstrucNum, CreateIRs(InstrucNum));

        JobSaveQueue.add(job);
        String info = String.format("新增作业:%d,%d,%d", job.JobsID, job.InTimes, job.InstrucNum);
        GetNewSwingForm.JObIO.set(1);//作业进入
        ScreenBlankShow.ScreenShowInfoProJob(info);
    }
    //随机生成指令,存放在二维数组中，
    // 每一行代表一个指令，第一列代表指令ID，下标从1开始到InstrucNum，
    // 第二列代表指令状态，有0,1,2,3四种取值，其中0或1取值概率为0.7，2取值概率为0.2，3取值概率为0.1
   private static ArrayList<IRs> CreateIRs(int InstrucNum)//随机生成指令
   {
       double[] probabilities = {0.30, 0.60, 0.80, 1.0}; // 各状态概率
       ArrayList<IRs> instructions = new ArrayList<>();
       Random random = new Random();
       for (int i = 0; i < InstrucNum; i++) {
           // 生成指令ID
           int instructionID = i + 1;

           // 生成指令状态根据概率
           double randomValue = random.nextDouble();
           int instructionStatus = 0;
           for (int j = 0; j < probabilities.length; j++) {
               if (randomValue < probabilities[j]) {
                   instructionStatus = j;
                   break;
               }
           }
           // 创建IRs对象并添加到ArrayList中
           IRs instruction = new IRs(instructionID, instructionStatus,TempIndex,InstrucNum);
           //四个参数分别为指令ID，指令状态，作业索引，指令数目
           instructions.add(instruction);
       }
       return instructions;
   }

    private static ArrayList<IRs> LoadIRs(String str, Integer jobsID,Integer InstrucNum){
        String filename = String.format("%s%s%d.txt", str, File.separator, jobsID);
        try (BufferedReader bfr = new BufferedReader(new FileReader(filename))) {
            return bfr.lines()
                    .filter(line -> !line.isEmpty())
                    .map(line -> {
                        String[] instructionData = line.split(",");
                        Integer Instruc_ID = Integer.parseInt(instructionData[0]);
                        Integer Instruc_State = Integer.parseInt(instructionData[1].trim());
                        return new IRs(Instruc_ID, Instruc_State,jobsID,InstrucNum);
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
//        int InstrucNum = 20; // 指令数量
//        double[] probabilities = {0.30, 0.60, 0.80, 1.0}; // 各状态概率
//        ArrayList<IRs> instructions = new ArrayList<>();
//        Random random = new Random();
//
//        for (int i = 0; i < InstrucNum; i++) {
//            // 生成指令ID
//            int instructionID = i + 1;
//
//            // 生成指令状态根据概率
//            double randomValue = random.nextDouble();
//            int instructionStatus = 0;
//
//            for (int j = 0; j < probabilities.length; j++) {
//                if (randomValue < probabilities[j]) {
//                    instructionStatus = j;
//                    break;
//                }
//            }
//
//            // 创建IRs对象并添加到ArrayList中
//            IRs instruction = new IRs(instructionID, instructionStatus);
//            instructions.add(instruction);
//        }
//
//        // 打印生成的指令
//        for (IRs instruction : instructions) {
//            System.out.println("指令ID: " + instruction.Instruc_ID + ", 状态: " + instruction.Instruc_State);
//        }
         System.out.println(dir);
    }

}
