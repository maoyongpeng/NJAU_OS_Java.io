package MainApplicationFolder.Memory_OpFolder;

import MainApplicationFolder.Job_OpFolder.IRs;
import MainApplicationFolder.Process_OpFolder.PCB;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class Memory {

    public static int MEMORY_SIZE =16;
    public static Queue<PCB> OutPutBlockQueue;
    public static Queue<PCB> InPutBlockQueue;
    public static PCB RunningPro;
    public static int InMemPro ;
    public static ArrayList<Block> AllocationMem; // (isUsed[i] == true) means that the block [i]  is used
    public static LinkedList<PCB> ReadyQueue;//就绪队列


    private static int stackMem;
    private static int heapMem;





    static {
        ReadyQueue =new LinkedList<>();
        OutPutBlockQueue = new LinkedList<>();
        InPutBlockQueue =new LinkedList<>();
        InMemPro=0;
        AllocationMem = new ArrayList<>();
        stackMem = 0;
        heapMem = 0;
        IntStream.range(0, MEMORY_SIZE).forEach(i -> AllocationMem.add(new Block()));
    }

    public static void AllocateMem(PCB pcb)
    {
        int Size =CalculateSize(pcb);//计算进程所占内存大小
        int StartAddress = -1;//分配的起始地址
        int EndAddress = -1;//分配的结束地址
        int Count =0;//计数器
        for(int i=0;i<AllocationMem.size();i++)//遍历位示图
        {
            if(!AllocationMem.get(i).Occupied)//如果当前块未被分配
            {
                if(Count==0)//如果计数器为0，说明是第一次找到未分配的块
                {
                    StartAddress=i;//记录分配的起始地址
                }
                Count++;//计数器加1
                if(Count==Size)//如果计数器等于进程所占内存大小，说明已经找到连续的内存块
                {
                    EndAddress=i;//记录分配的结束地址
                    break;
                }
            }
            else
            {
                Count=0;
            }
        }
        if(StartAddress!=-1&&EndAddress!=-1)//如果找到了连续的内存块
        {
            IntStream.rangeClosed(StartAddress, EndAddress).forEach(i -> AllocationMem.get(i).Occupied=true);//将内存位示图中的对应块置为已分配
            IntStream.rangeClosed(StartAddress, EndAddress).forEach(i -> AllocationMem.get(i).ProID=pcb.ProID);//将内存位示图中的对应块置为已分配
            pcb.StartAddress=StartAddress;//记录分配的起始地址
            pcb.EndAddress=EndAddress;//记录分配的结束地址

            MMU.Write(pcb);
        }
        else
        {
            pcb.StartAddress=-1;//记录分配的起始地址
            pcb.EndAddress=-1;//记录分配的结束地址
        }
        //如果没有找到连续的内存块，那么就不分配内存，直接将进程放入后备队列
    }//采用首次适应算法分配内存,返回分配的起始地址以及结束地址


    public static int CalculateSize(PCB pcb)
    {
        double count =pcb.job.InstrucNum*100;
        double result =count/1000;
        return (int)Math.ceil(result);//向上取整
    }//计算进程所占内存大小
    public static double CalculateAllSize()
    {
        //计算当前就绪队列中以及正在运行的进程所占用的内存大小，假设每条“用户态计算操作语句或者函数”类型的指令占用 100B，在进程创建时动态计算
        int size=0;
        for(PCB pcb: ReadyQueue)
        {
            size+=pcb.job.InstrucNum*100;
        }
        for (PCB pcb: OutPutBlockQueue)
        {
            size+=pcb.job.InstrucNum*100;
        }
        for (PCB pcb: InPutBlockQueue)
        {
            size+=pcb.job.InstrucNum*100;
        }
        if(RunningPro!=null)
        {
            size+=RunningPro.job.InstrucNum*100;
        }
        //size为1000时就占一个物理块
        return size;
    }

    public static void FreeMem(PCB currentProcess) {
        //释放内存
        IntStream.rangeClosed(currentProcess.StartAddress, currentProcess.EndAddress).forEach(i -> AllocationMem.get(i).Occupied=false);
        IntStream.rangeClosed(currentProcess.StartAddress, currentProcess.EndAddress).forEach(i -> AllocationMem.get(i).ProID=-2);
        MMU.Remove(currentProcess);
        MMU.RemoveIR(currentProcess);
    }
    public static boolean MemoryFull() {
        for (Block block : AllocationMem) {
            if (!block.Occupied) return false;
        }
        return true;
    }
    public static boolean MemoryEmpty() {
        for (Block block : AllocationMem) {
            if (block.Occupied) return false;
        }
        return true;
    }

    //计算剩余内存块数
    public static boolean FindAvailableBlock(PCB pcb)
    {
        int Size =CalculateSize(pcb);//计算进程所占内存大小
        int Count =0;//计数器,用于记录连续的内存块数
        //遍历位示图
        for (Block block : AllocationMem) {
            if (!block.Occupied)//如果当前块未被分配
            {
                Count++;//计数器加1
                if (Count == Size)//如果计数器等于进程所占内存大小，说明已经找到连续的内存块
                {
                    return true;
                }
            } else {
                Count = 0;
            }
        }
        return false;//如果没有找到合适的内存块，那么就返回false
    }
    public static double CalculateMemPercent()
    {//内存利用率=已分配内存块数/总内存块数
        int count=0;
        for(Block block:AllocationMem)
        {
            if(block.Occupied)
            {
                count++;
            }
        }
        //保留两位小数
        return (double)count*100.0/MEMORY_SIZE;
    }
    public static String[] CalcStackAndHeap()
    {
        //计算栈和堆的大小
        String[] Info =new String[2];
        PCB pcb =Memory.RunningPro;
        double stack =0.0;
        double heap=CalculateAllSize();

        if (pcb!=null)
        {
            ArrayList<IRs> instructs =pcb.job.instructs;//获取当前进程的指令集

            //栈的大小=指令集中的函数数*100B
            //栈的大小为当前进程的函数数*100B，执行完一个函数后，栈的大小减少100B
            //根据PCB的PC指针，可以判断当前进程执行到了哪个函数，从而计算栈的大小
            stack = IntStream.range(instructs.get(pcb.PC).Instruc_ID, instructs.size())
                    .filter(i -> instructs.get(i).Instruc_State == 1)
                    .mapToDouble(i -> 100).sum();
            //堆的大小=当前进程的总内存大小-栈的大小
            heap =heap-stack;
        }
        //栈的单位为B，
        //堆的单位为KB保留两位小数
        Info[0]=String.format("%.2f%s",stack,"B");
        Info[1]=String.format("%.2f%s",heap/1000,"KB");
        return Info;
    }
}
