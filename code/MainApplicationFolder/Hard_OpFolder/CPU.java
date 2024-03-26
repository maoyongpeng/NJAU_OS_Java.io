package MainApplicationFolder.Hard_OpFolder;

import MainApplicationFolder.GUI_OpFolder.ScreenBlankShow;
import MainApplicationFolder.Job_OpFolder.IRs;
import MainApplicationFolder.Memory_OpFolder.MMU;
import MainApplicationFolder.Memory_OpFolder.Memory;
import MainApplicationFolder.Process_OpFolder.PCB;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class CPU implements Runnable{

    private AtomicInteger PC;
    private IRs IR;
    public static boolean ActivateFlag;
    public static boolean DoType_01;
    //做类型1的指令不可中断
    public static boolean InterruptFlag;

    public enum  SystemCallType//系统调用类型
    {

        Input,//输入
        Output,//输出
        None//无中断
    }
    public static SystemCallType SysBlockType;//默认无中断
    static Stack<PCB> PCBStack = new Stack<>();//PCB栈,用于保存PCB信息，容量为1




    public CPU() {
        this.PC = new AtomicInteger(-1);
        ActivateFlag=false;
        DoType_01=false;
        InterruptFlag=false;
        SysBlockType =SystemCallType.None;

    }
    @Override
    public void run() {
        while (true)
        {
            synchronized (this)
            {
                if(ActivateFlag)
                {
                    Do();
                    ActivateFlag=!ActivateFlag;
                }
            }
        }
    }
    private void Do()
    {
         PCB pcb=Memory.RunningPro;
         synchronized (Memory.class) {
            if (pcb == null ) {
                String str = "CPU空闲";
                ScreenBlankShow.ScreenShowInfoProJob(str);
                return;
            } else {
                PCBStack.push(pcb);//将当前进程压入栈
            }
        }
        if(pcb.ProEnd())
        {
            System.out.println("当前进程没有可以执行的指令");
            PCBStack.pop();//将当前进程弹出栈
            return;
        }
         CPU_REC();//从PCB中读取PC和IR,恢复现场
         IncrementPC();
         checkMem();
         IRexec(pcb);
    }
    private void IncrementPC() {
        PC.incrementAndGet();//PC+1
    }
    private void IRexec(PCB pcb) {
        DoType_01 =true;
        IR.DoIRs(pcb);
        pcb.RunTimes.add(IR.NeedRunTime());

        CPU_PRO(pcb);//保护现场
        DoType_01=false;
    }

    private void checkMem() {
        PCB pcb =Memory.RunningPro;
        if (Memory.InMemPro>16)
        {
            String info;
            info = "系统最大16进程并发";
            ScreenBlankShow.ScreenShowInfoProJob(info);
            JOptionPane.showMessageDialog(null,info,"提示",JOptionPane.WARNING_MESSAGE);
            System.out.println(info);
            MMU.Clear();
            System.exit(0);
        }
        String info =null;
        if(pcb != null)
        {
            int logicalAddr =pcb.job.instructs.get(pcb.PC).Logical_Address;
            info = "运行进程:" + pcb.ProID + ":" + IR.Instruc_ID + "," + IR.Instruc_State +","+
                    logicalAddr+
                    "," + MMU.IRLogicToPysical.get(logicalAddr);
            System.out.println(info);
        }
        ScreenBlankShow.ScreenShowInfoProJob(info);
    }
    public void CPU_PRO(PCB pcb) {
        PCBStack.push(pcb);
        pcb.SetPCAndIR(PC.get(), IR);

    }//将PC和IR写入PCB
    public void CPU_REC() {

        PCB pcb =PCBStack.pop();
        if (pcb ==null)
            throw new NullPointerException("PCB栈为空");
        this.PC.set(pcb.GetPC());
        this.IR = pcb.GetIR();
    }//从PCB中读取PC和IR

    public static void main(String[] args) {
        String info = "系统最大16进程并发";
        JOptionPane.showMessageDialog(null,info,"提示",JOptionPane.WARNING_MESSAGE);
    }


}


