package MainApplicationFolder.IO_OpFolder;

import MainApplicationFolder.GUI_OpFolder.GetNewSwingForm;
import MainApplicationFolder.GUI_OpFolder.ScreenBlankShow;
import MainApplicationFolder.GUI_OpFolder.UI;
import MainApplicationFolder.Hard_OpFolder.Clock_thread;
import MainApplicationFolder.Job_OpFolder.JobIn_thread;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;


public class File_Design {
   static String TitlePro="作业/进程调度信息:";
   static  String TitleStatus ="状态统计信息:";

    public static String TargetFile = "output2";//输出文件夹,默认为Output2
    public static void SaveText() throws IOException {//保存文本信息

        StringBuilder str =new StringBuilder();
        str.append(TitlePro).append("\n");

        String info1 = UI.osFrameMainForm.textAreaEventArgs.getText();
        str.append(info1).append("\n\n");//获取作业/进程调度信息

        str.append(TitleStatus).append("\n");
        String info2 = UI.osFrameMainForm.textAreaInfoStatus.getText();
        str.append(info2).append("\n\n");//获取状态统计信息

        String infoMMU = ScreenBlankShow.MMUInfo();//获取MMU信息

        StringBuilder savefile = new StringBuilder();
        savefile.append("ProcessResults-").append(Clock_thread.COUNTTIME).append("-SJP.txt");
        String targetfile= String.format("./%s/%s", TargetFile,savefile);

        StringBuilder saveMMU = new StringBuilder();
        saveMMU.append("MMU-").append(Clock_thread.COUNTTIME).append("-SJP.txt");
        String targetlog= String.format("./Memlog/%s", saveMMU);

        CreateFileTXT(targetfile,str.toString());
        CreateFileMMULog(targetlog,infoMMU);

        //稍作等待2s后退出
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void CreateFileMMULog(String targetlog, String infoMMU) {//创建MMU日志文件
        File newfile = new File(targetlog);
        FileWriter writer =null;
        try
        {
            newfile.createNewFile();
            writer =new FileWriter(newfile);
            writer.write(infoMMU);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private static void CreateFileTXT(String path,String info) throws IOException {//创建文件
        File newfile = new File(path);
        FileWriter writer =null;
        try
        {
            newfile.createNewFile();
            writer =new FileWriter(newfile);
            writer.write(info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

}
