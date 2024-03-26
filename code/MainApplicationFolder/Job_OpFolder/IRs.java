package MainApplicationFolder.Job_OpFolder;


import MainApplicationFolder.GUI_OpFolder.ScreenBlankShow;
import MainApplicationFolder.Hard_OpFolder.CPU;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.Memory_OpFolder.Memory;
import MainApplicationFolder.Process_OpFolder.*;


import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class IRs {

    public Integer Instruc_ID;
    public Integer Instruc_State;


    public Integer Logical_Address;//指令的逻辑地址
    public Integer Physical_Address;//指令的物理地址

    private static int baseAddress =0;//基地址


    public IRs(Integer instrucId, Integer instrucState,Integer jobindex,Integer instrucNum) {
        this.Instruc_ID = instrucId;
        this.Instruc_State = instrucState;
        GenerateAddress(jobindex,instrucNum);
    }
    public void GenerateAddress(int jobindex,int instrucNum)
    {
        this.Logical_Address = jobindex * 10000 + instrucNum * 100+baseAddress;
        baseAddress++;
        transformL_P();//将逻辑地址转换为物理地址
    }
    private void transformL_P()
    {
        //将逻辑地址一一映射到物理地址，MMU中的逻辑地址到物理地址的映射，将逻辑地址转换为物理地址
            int ret = this.Logical_Address % 1000;
            this.Physical_Address= ret / 10 * 100 + ret % 10 + 100;//100为基地址
    }

    public Integer NeedRunTime() {
        return Instruc_State == 0 ? 1 : 2;
    }
    private void OutPutFunc() {//输出阻塞
        execblock(3);
        Thread thread = new Thread(new OutputBlock_thread());
        thread.start();
    }
    private void InPutFunc() {//输入阻塞
        execblock(2);
        Thread thread = new Thread(new InputBlock_thread());
        thread.start();
    }

    private static final String strinfo1 = "阻塞进程:"+"Input_thread,";
    private static final String strinfo2 ="阻塞进程:"+"Output_thread,";
    private void execblock(int flag) {
            if (flag==2)
            {
                synchronized (Memory.class)
                {
                    Memory.InPutBlockQueue.add(Memory.RunningPro);
                    CPU.SysBlockType =CPU.SystemCallType.Input;
                    String str = String.format("%s%d", strinfo1, Memory.RunningPro.ProID);
                    System.out.println(str);
                    PCB.BlockQueue1INFO.addLast(Clock_thread.COUNTTIME);
                    PCB.BlockQueue1INFO.addLast(Memory.RunningPro.ProID);
                    ScreenBlankShow.ScreenShowInfoProJob(str);
                    Memory.RunningPro = null;
                }
            }
            else if(flag==3)
            {
                synchronized (Memory.class)
                {
                    Memory.OutPutBlockQueue.add(Memory.RunningPro);

                    CPU.SysBlockType =CPU.SystemCallType.Output;

                    String str = String.format("%s%d", strinfo2, Memory.RunningPro.ProID);
                    System.out.println(str);
                    PCB.BlockQueue2INFO.addLast(Clock_thread.COUNTTIME);
                    PCB.BlockQueue2INFO.addLast(Memory.RunningPro.ProID);
                    ScreenBlankShow.ScreenShowInfoProJob(str);
                    Memory.RunningPro = null;
                }
            }
        }
    private void SegOrFunc(int time) {//判断是语句还是函数
        AtomicInteger nearTime = new AtomicInteger(0);
        AtomicInteger GoneTime = new AtomicInteger(0);
        synchronized (Clock_thread.class) {
            nearTime.set(Clock_thread.COUNTTIME);
        }
        while (true) {
                if (nearTime.get() < Clock_thread.COUNTTIME) {
                    nearTime.set(Clock_thread.COUNTTIME);
                    GoneTime.incrementAndGet();//时间片增加多少 1
                }
                if (GoneTime.get() >= time) {
                    break;
                }
            }
            synchronized (ProcessScheduling_thread.class) {
                ProcessScheduling_thread.Times -= time;//时间片减少
            }
    }
    public void DoIRs(PCB pcb) {
           switch (Instruc_State) {
               case 0:
               case 1:
                   SegOrFunc(NeedRunTime());
                   break;
               case 2:
                   System.out.println("输入指令");
                   InPutFunc();
                   break;
               case 3:
                   System.out.println("输出指令");
                   OutPutFunc();
                   break;
               default:
                   System.out.println("错误指令");
                   break;
           }
    }

    public static void main(String[] args) {


    }
}
