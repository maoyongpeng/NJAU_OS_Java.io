package MainApplicationFolder.Process_OpFolder;

import MainApplicationFolder.Job_OpFolder.IRs;
import MainApplicationFolder.Job_OpFolder.JOB;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.Memory_OpFolder.MMU;

import java.util.*;

public class PCB {

    public Integer ProID;
    public Integer InTimes;//进程进入时间
    public Integer EndTimes;//结束时间
    public ArrayList<Integer> RunTimes;
    public Integer TurnTimes;
    public Integer InstrucNum;
    public Integer PC;//程序计数器
    public IRs IR;
    public JOB job;
    public int StartAddress;//进程起始地址
    public int EndAddress;//进程结束地址
    public int LogicAddress;//进程逻辑地址
    public int Priority;
    public static ArrayList<Integer> TurnRoundTime= new ArrayList<>();//作业周转时间数组
    public static ArrayList<Double>  WeightedTurnRoundTime = new ArrayList<>();//带权周转时间数组

    public static LinkedList<Integer> BlockQueue1INFO = new LinkedList<>();//阻塞队列1信息
    public static LinkedList<Integer> BlockQueue2INFO = new LinkedList<>();//阻塞队列2信息、

    public int PSW;//程序状态字


    public void SetPCAndIR(int PC, IRs IR) {
        this.PC = PC;
        this.IR = IR;
    }
    public int GetPC()
    {
       return this.PC;
    }
    public IRs GetIR()
    {
        return this.job.instructs.get(this.PC);
    }

    static String IRinfo ="指令类型:%s,需要时间:%d";


    public String GetIRState()
    {
        String s="";
        //检查PC是否越界
        if(this.PC>=this.job.instructs.size())
        {
            return "";
        }
        switch (this.job.instructs.get(this.PC).Instruc_State) {
            case 0:
                s=String.format(IRinfo,"语句",MMU.mapinfo.get("语句"));
                break;
            case 1:
                s=String.format(IRinfo,"函数",MMU.mapinfo.get("函数"));
                break;
            case 2:
                s=String.format(IRinfo,"键盘输入",MMU.mapinfo.get("键盘输入"));
                break;
            case 3:
                s=String.format(IRinfo,"屏幕输出",MMU.mapinfo.get("屏幕输出"));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this.job.instructs.get(this.PC).Instruc_State);
        }
        return s;
    }

    public PCB(JOB job) {
        this.job = job;//这里的job是从job队列中取出的job
        this.ProID = job.JobsID;
        synchronized (Clock_thread.class) {
            this.InTimes = Clock_thread.COUNTTIME;
        }
        this.EndTimes = 0;
        this.RunTimes = new ArrayList<>();
        this.TurnTimes = 0;
        this.InstrucNum = job.InstrucNum;
        this.PC = 0;
        this.IR = null;
        this.PSW = -1;//程序状态字
        this.EndAddress=-1;
        this.StartAddress=-1;
        this.LogicAddress=-1;
        this.Priority =new Random().nextInt(5)+1;//随机生成优先级范围1-5

    }
    public boolean ProEnd() {
        boolean equals = Objects.equals(PC, InstrucNum);
        return equals;
    }
    public Integer CalcLeftIRs() {
        return this.InstrucNum - this.PC;
    }

    @Override
    public String toString() {
        return String.format("(PCB %d)", ProID);
    }

   //显示进程的时间类评价数据
    public String showTime()
    {
        return "进程ID:" + this.ProID + "," +
               "作业提交时间:" + this.job.InTimes + "s," +
               "进程创建时间:" + this.InTimes + "s," +
               "结束时间:" + this.EndTimes + "s," +
               "周转时间:" + CalcTurnTime() + "s";
    }

    static final Comparator<PCB> ORDER_BY_ArriveTime=(e1,e2)->
    {
       int ret =e1.job.InTimes-e2.job.InTimes;
       return Integer.compare(ret,0);
    };
    static final Comparator<PCB> ORDER_BY_Pri=(e1,e2)->
    {
        int ret =e1.Priority-e2.Priority;
        if(ret>0)
            return 1;
        else if (ret<0) {
            return -1;
        }
        ret =ORDER_BY_ArriveTime.compare(e1,e2);
        return Integer.compare(ret,0);
    };//优先比较优先级，相同时，再比较作业到来时间，预防饥饿

    //计算周转时间
    private String  CalcTurnTime()
    {
        return String.format("%d",this.EndTimes-this.job.InTimes);
    }
    public String showPysicalAddress()
    {
        return String.format("%d-%d",StartAddress,EndAddress);
    }
    public int CalcRunningTimes() {
        //计算RunTimes的所有值加和
        return RunTimes.stream().mapToInt(runTime -> runTime).sum();
    }
    public static double CalcMeanTurnRoundTime() //计算平均周转时间，保留2位小数
    {
        int time =TurnRoundTime.stream().mapToInt(turn->turn).sum();
        double result = time/(double)TurnRoundTime.size();
        return (double)Math.round(result*100)/100;
    }
    //计算平均带权周转时间
    public static double CalcMeanWeightedTurnRoundTime()
    {
        double time =WeightedTurnRoundTime.stream().mapToDouble(turn->turn).sum();
        double result = time /(double)WeightedTurnRoundTime.size();
        return (double)Math.round(result*100)/100;
    }



}
