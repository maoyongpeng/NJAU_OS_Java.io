package MainApplicationFolder.Memory_OpFolder;

import MainApplicationFolder.GUI_OpFolder.ScreenBlankShow;
import MainApplicationFolder.IO_OpFolder.JobComeFile;
import MainApplicationFolder.Job_OpFolder.IRs;
import MainApplicationFolder.Job_OpFolder.JOB;
import MainApplicationFolder.Job_OpFolder.JobIn_thread;
import MainApplicationFolder.Process_OpFolder.PCB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class MMU {

    public volatile static ConcurrentHashMap<Integer,PCB> mmu;//进程逻辑地址到物理地址的映射
    static volatile AtomicInteger logiccount =new AtomicInteger();//进程逻辑地址计数器,范围0-1000，CPU生成
    public static ReentrantLock mmulock = new ReentrantLock();//锁对象
    private  static  final int BaseAddress =100;//进程基地址


    public static Map<String,Integer> mapinfo;//映射指令类型，时间
    static {
        mapinfo = new HashMap<>();
        mapinfo.put("语句",1);
        Arrays.asList("函数","键盘输入", "屏幕输出").forEach(s -> mapinfo.put(s, 2));
        IRLogicToPysical = new HashMap<>();
       mmu = new ConcurrentHashMap<>();
       logiccount.set(new Random().nextInt(1000));
    }
    public static void Write(PCB pcb) {
        pcb.LogicAddress =BaseAddress+logiccount.get();
        mmu.put(logiccount.get(),pcb);
        logiccount.incrementAndGet();//逻辑地址计数器加1
        ScreenBlankShow.MMUBuffer.append("(")
                .append(pcb.ProID).append(")")
                .append("LogicAddr:")
                .append(pcb.LogicAddress)
                .append(",PysicalAddr:")
                .append(pcb.showPysicalAddress())
                .append("\n");
    }
    public static void Remove(PCB pcb) {
        mmu.remove(pcb.LogicAddress);
    }
    public static String showPCBMMU() {
        StringBuilder ans = new StringBuilder();

        for (Map.Entry<Integer, PCB> next : mmu.entrySet()) {
            ans.append("LogicAddr:");
            ans.append(next.getKey());
            ans.append(",PysicalAddr:");
            ans.append(next.getValue().showPysicalAddress());
            ans.append("\n");
        }

        return ans.toString();
    }
    public static void Clear() {
        mmu.clear();
        logiccount.set(0);
    }

    //设计指令的逻辑地址到物理地址的最佳的数据结构，HashMap，key为逻辑地址，value为物理地址
    public volatile static HashMap<Integer,Integer> IRLogicToPysical;
    public static void WriteIR(PCB pcb) {
        IntStream.range(0, pcb.job.instructs.size()).
                forEach(i -> IRLogicToPysical.put(pcb.job.instructs.get(i).Logical_Address,
                pcb.job.instructs.get(i).Physical_Address));
    }
    public static void RemoveIR(PCB pcb) {
        IntStream.range(0, pcb.job.instructs.size()).
                forEach(i -> IRLogicToPysical.remove(pcb.job.instructs.get(i).Logical_Address));
    }
    //根据运行进程，获取逻辑地址以及物理地址
    public static String[] GetIRAddr(PCB pcb)
    {
        String [] strings=new String[2];
        if(pcb!=null&&pcb.PC<pcb.job.instructs.size())
        {
            int addr = pcb.job.instructs.get(pcb.PC).Logical_Address;
            strings[0]= String.format("Logic:%d", addr);
            //从IRLogicToPysical中获取物理地址
            strings[1]= String.format("Pysical:%d", IRLogicToPysical.get(addr));
        }
        return strings;
    }


    public static void main(String[] args) {
//           JOB job = new JOB(1,10,20,CreateIRs(20));
//           PCB pcb = new PCB(job);
//         MMU.WriteIR(pcb);
//
//        System.out.println(MMU.IRLogicToPysical);
//        MMU.RemoveIR(pcb);
//        System.out.println(MMU.GetIRAddr(pcb)[0]);
       // System.out.println(showPCBMMU());
    }
//    private static ArrayList<IRs> CreateIRs(int InstrucNum)//随机生成指令
//    {
//        double[] probabilities = {0.30, 0.60, 0.80, 1.0}; // 各状态概率
//        ArrayList<IRs> instructions = new ArrayList<>();
//        Random random = new Random();
//        for (int i = 0; i < InstrucNum; i++) {
//            // 生成指令ID
//            int instructionID = i + 1;
//
//            // 生成指令状态根据概率
//            double randomValue = random.nextDouble();
//            int instructionStatus = 0;
//            for (int j = 0; j < probabilities.length; j++) {
//                if (randomValue < probabilities[j]) {
//                    instructionStatus = j;
//                    break;
//                }
//            }
//            // 创建IRs对象并添加到ArrayList中
//            IRs instruction = new IRs(instructionID, instructionStatus,0,InstrucNum);
//            //四个参数分别为指令ID，指令状态，作业索引，指令数目
//            instructions.add(instruction);
//        }
//        return instructions;
//    }
}
