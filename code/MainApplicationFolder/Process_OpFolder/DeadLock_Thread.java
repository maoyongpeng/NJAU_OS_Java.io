package MainApplicationFolder.Process_OpFolder;

import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.Job_OpFolder.IRs;
import MainApplicationFolder.Job_OpFolder.JOB;
import MainApplicationFolder.Memory_OpFolder.Memory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class DeadLock_Thread implements Runnable {
    //总共有四类资源，
    //0号指令语句执行需要0号资源，1号指令函数执行需要1号资源，2号指令键盘输入需要2号资源，3号指令屏幕输出需要3号资源

    public static final ReentrantLock lock = new ReentrantLock();//创建一个锁对象
    private static final Integer[] SystemResources = new Integer[4];

    public static PCB[] pcbs;//进程控制块数组

    //最大需求矩阵
    private static int[][] Max;
    //分配矩阵
    private static int[][] Allocation;

    //需求矩阵
    private static int[][] Need;

    private static ArrayList<Integer> SafeSuqence;//安全序列

    private static int[][] Request;//请求资源向量

    private static Integer[] Available = new Integer[4];//可用资源向量


    private static Integer[] Work;//工作向量
    private static boolean[] Finish;//进程完成标志向量


    private static void SetSystemResources() {
        //指令函数执行需要1号资源,泊松分布,表示平均每个时间单位内发生的事件数
        for (int i = 0; i < 2; i++) SystemResources[i] = GetPoisson(15);//指令语句执行需要0号资源,泊松分布,表示平均每个时间单位内发生的事件数

        //屏幕输出需要3号资源,泊松分布,表示平均每个时间单位内发生的事件数
        for (int i = 2; i < 4; i++) SystemResources[i] = GetPoisson(6);//键盘输入需要2号资源,泊松分布,表示平均每个时间单位内发生的事件数
    }

    private static int GetPoisson(double lambda) {//泊松分布,表示平均每个时间单位内发生的事件数
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            ++k;
            p *= Math.random();
        } while (p > L);

        return k - 1;
    }

    private static void SetMaxMatrix(PCB[] pcb) {
        //统计每个进程作业的指令集中的各个类型指令数目，计算每个进程中作业的4个资源的最大需求量，
        for (int i = 0; i < pcb.length; i++) {
            for (int j = 0; j < pcb[i].job.instructs.size(); j++) {
                switch (pcb[i].job.instructs.get(j).Instruc_State) {
                    case 0:
                        Max[i][0]++;
                        break;
                    case 1:
                        Max[i][1]++;
                        break;
                    case 2:
                        Max[i][2]++;
                        break;
                    case 3:
                        Max[i][3]++;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + pcb[i].job.instructs.get(j).Instruc_State);
                }
            }
        }
    }

    private static void SetAllocationMatrix(PCB[] pcbs) {
        //随机初始化分配矩阵，每类资源的分配量不超过该类资源的最大需求量的一半
        for (int i = 0; i < pcbs.length; i++) {
            for (int j = 0; j < 4; j++) {
                Allocation[i][j] = (int) (Math.random() * Max[i][j] / 2);
            }
        }
    }
    private void SetNeedMatrix() {
        //计算需求矩阵
        for (int i = 0; i < pcbs.length; i++) {
            for (int j = 0; j < 4; j++) {
                Need[i][j] = Max[i][j] - Allocation[i][j];
            }
        }
    }
    private void SetRequestMatrix() {
        //随机生成请求资源向量，每类资源的请求量不超过该类资源的最大需求量的一半，模拟进程请求资源
        for (int i = 0; i < pcbs.length; i++) {
            for (int j = 0; j < 4; j++) {
                Request[i][j] = (int) (Math.random() * Max[i][j] / 2);
            }
        }
    }

    public static String[] strInfo = new String[2];

    @Override
    public void run() {

        while (true) {
            synchronized (this) {
                if (Clock_thread.COUNTTIME>=15&& Clock_thread.COUNTTIME % 8 == 0) {//每8秒检查一次是否存在死锁
                    InitResources();
                    CheckDeadLock();
                }
            }
        }
    }
    private void InitResources()
    {
        synchronized (Memory.class)
        {
            pcbs = Memory.ReadyQueue.toArray(new PCB[0]);
        }
        if (Memory.ReadyQueue.isEmpty()) return;//就绪队列为空，不需要检查死锁
        Max = new int[pcbs.length][4];//初始化最大需求矩阵
        Allocation = new int[pcbs.length][4];//初始化分配矩阵
        Need = new int[pcbs.length][4];//初始化需求矩阵
        Request = new int[pcbs.length][4];//初始化请求资源向量
        Finish = new boolean[pcbs.length];//初始化进程完成标志向量


        SetSystemResources();//设置系统资源向量
        SetMaxMatrix(pcbs);//设置最大需求矩阵
        SetAllocationMatrix(pcbs);//设置分配矩阵
        SetNeedMatrix();//设置需求矩阵
        SetRequestMatrix();//设置请求资源向量

    }
    private void CheckDeadLock()
    {
        //检查是否存在死锁
        if (Memory.ReadyQueue.isEmpty()) return;//就绪队列为空，不需要检查死锁
        boolean flag = BankerAlgorithm();
        if (flag)
        {
            strInfo[0] = "安全";
            //输出安全序列,p1->p2->p3->p4形式
            StringBuilder stringBuilder = new StringBuilder();
            for (Integer integer : SafeSuqence) {
                stringBuilder.append("P").append(integer).append("->");
            }
            //删除最后一个->
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            strInfo[1] = stringBuilder.toString();
        }
        else
        {
            strInfo[0] = "死锁";
            StringBuilder stringBuilder = new StringBuilder();
            //遍历进程完成标志向量，输出未完成的进程
            for(int i=0;i<pcbs.length;i++)
            {
                if(!Finish[i])
                {
                    stringBuilder.append("P").append(pcbs[i].ProID).append(" ");
                }
            }
            strInfo[1] = stringBuilder.toString();
        }

        //Test();
    }
    private void Test()
    {
        System.out.println("Max矩阵：");
        PrintMatrix(pcbs,Max);
        System.out.println("Allocation矩阵：");
        PrintMatrix(pcbs,Allocation);
        System.out.println("Need矩阵：");
        PrintMatrix(pcbs,Need);
        System.out.println("Request矩阵：");
        PrintMatrix(pcbs,Request);
        System.out.println("Available向量：");
        PrintVector(Available);
        System.out.println("Work向量：");
        PrintVector(Work);
        System.out.println("Finish向量：");
        for (boolean b : Finish) {
            System.out.print(b + " ");
        }
        System.out.println();
    }




    //实现银行家算法，检测死锁，最后用安全性算法检测是否存在安全序列，若存在则不存在死锁，否则存在死锁。
    //若存在死锁，则输出死锁进程的进程号，否则输出安全序列。
    private static boolean BankerAlgorithm()
    {
        //先判断是否存在进程的需求量大于可用资源量，若存在，则不分配，否则分配。
        for(int i=0;i<pcbs.length;i++)
        {
            for(int j=0;j<4;j++)
            {
                if(Request[i][j]>Available[j])
                {
                    return false;
                }
            }
        }
        //再判断Need<=Request，若不满足，则不分配，否则分配。
        for(int i=0;i<pcbs.length;i++)
        {
            for(int j=0;j<4;j++)
            {
                if(Need[i][j]<Request[i][j])
                {
                    return false;
                }
            }
        }
        //尝试分配资源，若分配后存在安全序列，则分配，否则不分配。检验通过安全性算法，若存在安全序列，则分配，否则回滚。
        for (int index = 0; index < pcbs.length; index++) {
            for (int j = 0; j < 4; j++) {
                Available[j] -= Request[index][j];
                Allocation[index][j] += Request[index][j];
                Need[index][j] -= Request[index][j];
            }
        }
        //检验通过安全性算法，若存在安全序列，则分配，否则回滚。
        if(SafetyAlgorithm())
        {
            return true;
        }
        else
        {
            for (int index = 0; index < pcbs.length; index++) {
                for (int j = 0; j < 4; j++) {
                    Available[j] += Request[index][j];
                    Allocation[index][j] -= Request[index][j];
                    Need[index][j] += Request[index][j];
                }
            }//回滚
            return false;
        }
    }
    private static boolean SafetyAlgorithm()//实现安全性检测算法
    {
        //初始化工作向量
        IntStream.range(0, 4).forEach(i -> Work[i] = Available[i]);
        //初始化进程完成标志向量
        IntStream.range(0, pcbs.length).forEach(i -> Finish[i] = false);
        //初始化安全序列
        SafeSuqence.clear();
        //寻找一个满足条件的进程
        int i=0;
        while(i<pcbs.length)
        {
            if(!Finish[i] &&Need[i][0]<=Work[0]
                    &&Need[i][1]<=Work[1]
                    &&Need[i][2]<=Work[2]
                    &&Need[i][3]<=Work[3])//判断该进程是否满足条件
            {
                //将进程号加入安全序列
                SafeSuqence.add(pcbs[i].ProID);

                //将该进程对应的资源释放
                Work[0]+=Allocation[i][0];
                Work[1]+=Allocation[i][1];
                Work[2]+=Allocation[i][2];
                Work[3]+=Allocation[i][3];
                //将该进程标记为已完成
                Finish[i]=true;
                //从头开始寻找满足条件的进程
                i=0;
            }
            else
            {
                //没有找到满足条件的进程，继续寻找
                i++;
            }
        }
        //判断是否所有进程都已完成
        return IntStream.range(0, pcbs.length).allMatch(j -> Finish[j]);
    }









    public DeadLock_Thread()
    {
        SafeSuqence = new ArrayList<>();//初始化安全序列
        Work = new Integer[4];//初始化工作向量
        Available = SystemResources;//初始化可用资源向量

    }

    


    public static void main(String[] args) {
        JOB job1 = new JOB(1, 1, 20, createIRs(20,1));
        JOB job2 = new JOB(2, 2, 20, createIRs(20,2));
        JOB job3 = new JOB(3, 3, 20, createIRs(20,3));
        JOB job4 = new JOB(4, 4, 20, createIRs(20,4));
         PCB pcb1 = new PCB(job1);
         PCB pcb2 = new PCB(job2);
         PCB pcb3 = new PCB(job3);
            PCB pcb4 = new PCB(job4);
         PCB[] pcbs = new PCB[4];
         pcbs[0] = pcb1;
         pcbs[1] = pcb2;
         pcbs[2] = pcb3;
         pcbs[3] = pcb4;
         DeadLock_Thread deadLockThread = new DeadLock_Thread();
         Thread thread = new Thread(deadLockThread);
         thread.start();
        //将pcbs的进程号加入集合中
//        Set<Integer> set =new HashSet<>();
//        for(PCB pcb:pcbs)
//        {
//            set.add(pcb.ProID);
//        }
//        System.out.println("进程号集合："+set);
//        int index =new Random().nextInt(pcbs.length)+1;//随机选择一个进程
//        System.out.println("随机选择一个进程："+index);
//        set.remove(index);//从集合中移除该进程

//        System.out.println("集合中剩余进程："+set);
//         int index =(int) (Math.random()*set.size())+1;
//        System.out.println("随机选择一个进程："+index);
//
//        set.remove(index);
//        set.remove(index);
//        System.out.println("集合中剩余进程："+set);
//        index =(int) (Math.random()*set.size())+1;
//        System.out.println("重新随机选择一个进程："+index);
//        set.remove(index);
//        System.out.println("集合中剩余进程："+set);
//        index =(int) (Math.random()*set.size())+1;
//        System.out.println("重新随机选择一个进程："+index);
//        set.remove(index);
//        System.out.println("集合中剩余进程："+set);


    }

    private static void PrintVector(Integer[] available) {
        for (Integer integer : available) {
            System.out.print(integer + " ");
        }
        System.out.println();
    }

    private static void PrintMatrix(PCB [] pcbs,int [][] matrix)
  {
      for(int i=0;i<pcbs.length;i++)
      {
          for(int j=0;j<4;j++)
          {
              System.out.print(matrix[i][j]+" ");
          }
          System.out.println();
      }
  }

    private static ArrayList<IRs> createIRs(int InstrucNum, int index) {
        double[] probabilities = {0.40, 0.60, 0.80, 1.0}; // 各状态概率
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
            IRs instruction = new IRs(instructionID, instructionStatus,index,InstrucNum);
            //四个参数分别为指令ID，指令状态，作业索引，指令数目
            instructions.add(instruction);
        }
        return instructions;
    }


}
