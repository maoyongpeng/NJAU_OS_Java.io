package MainApplicationFolder.GUI_OpFolder;

import MainApplicationFolder.Hard_OpFolder.CPU;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.Memory_OpFolder.MMU;
import MainApplicationFolder.Process_OpFolder.PCB;

import javax.swing.*;
import java.util.LinkedList;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ScreenBlankShow {

    //TODO:打印到屏幕上的作业以及进程信息

    public static void ScreenShowInfoProJob(String StrInfo)
    {
        synchronized (lock) {
            SwingUtilities.invokeLater(() -> {
                UI.osFrameMainForm.textAreaEventArgs.append(String.format("%d:[%s]\n", Clock_thread.COUNTTIME, StrInfo));
            });
        }
    }


    private static final Object lock = new Byte[0];
    public static StringBuffer bufferWriter =new StringBuffer();
    public static StringBuffer MMUBuffer = new StringBuffer();
    private static final String str1 ="BB1:[阻塞队列1,键盘输入:";
    private static final String str2 ="BB2:[阻塞队列2,屏幕显示:";
    public static void ScreenShowInfoStatus()
    {
        StringBuilder str = new StringBuilder();
        str.append(bufferWriter.toString())
                .append("\n")
                .append(str1 + Info3(PCB.BlockQueue1INFO))
                .append("\n")
                .append(str2 + Info3(PCB.BlockQueue2INFO));

        UI.osFrameMainForm.textAreaInfoStatus.setText(String.valueOf(str));
    }
    public static  String MMUInfo()
    {
        StringBuilder strInfo = new StringBuilder();
        strInfo.append("进程MMU内容:\n");
        //打印MMU中的HashTable的键值对内容
        strInfo.append(MMUBuffer.toString());
        return strInfo.toString();
    }
    private static String Info3(LinkedList<Integer> BQ)
    {
        //要求在读入两个元素（两个元素为一组）的中间用","分隔，组与组之间用"/"分隔，最后一组后面不加"/"号，而是加上"]"号
        return IntStream.range(0, BQ.size())
                .mapToObj(i -> {
                   String str =Integer.toString(BQ.get(i));
                     if(i%2!=0&&i<BQ.size()-1)
                     {
                         str = String.format("%s/", str);
                     }
                     else if(i<BQ.size()-1)
                     {
                         str = String.format("%s,", str);
                     }
                     return str;
                })
                .collect(Collectors.joining("", " ", "]"));
    }
    public static void ShowProsTimeInfo(PCB pcb)
    {
        UI.osFrameMainForm.textAreaTimePROINFO.append(pcb.showTime() +"\n");
    }
    private static final String string="当前已经完成进程的:";
    public static void ShowMeanAWT(int select)
    {
        String stringBuilder = string +
                (select == 0 ? "非抢占式时间轮转," : "静态优先级时间轮转,")+
                "平均周转时间:" +
                PCB.CalcMeanTurnRoundTime() +
                "s,平均带权周转时间:" +
                PCB.CalcMeanWeightedTurnRoundTime() +
                "s";
        UI.osFrameMainForm.textAreaTimePROINFO.append(stringBuilder +"\n");
    }



}
