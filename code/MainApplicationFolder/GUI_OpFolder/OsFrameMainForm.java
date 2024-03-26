package MainApplicationFolder.GUI_OpFolder;


import MainApplicationFolder.IO_OpFolder.File_Design;
import MainApplicationFolder.IO_OpFolder.JobComeFile;
import MainApplicationFolder.Memory_OpFolder.Memory;
import MainApplicationFolder.Process_OpFolder.ProcessScheduling_thread;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public  class OsFrameMainForm{
    public JPanel panelMainForm;
    public JButton BtnForStart;
    public JButton BtnForAdd;
    public JButton BtnForSysExit;


    public JTextField textReadyQueue;
    public JTextField textFieldInputBlockQ1;
    public JTextField textFieldOutputBlockQ2;
    public JTextArea textAreaEventArgs;
    public JTextArea textAreaInfoStatus;

    public JTextField textComeJobs;
    public JPanel PanelForBtns;
    public JPanel PanelForMessage;
    public JPanel PanelForStatus;
    public JPanel PanelForDetials;
    public JPanel PanelForProcessMem;
    public JPanel PanelForQueues;
    public JPanel PanelForReadyQueue;
    public JPanel PanelForBlockQueue;

    public JPanel PanelForComeJobs;
    public JButton BtnForStop;
    public JPanel PanelForCurrentStatus;
    public JTextField textFieldProcess;
    public JTextField textFieldSysTime;

    public JTextField textEndQueue;
    public JTextArea textAreaAddress;
    public JTextField textFieldCPU;
    public JTextField textFieldProNum;
    private JButton BtnForAddPro1;
    public JTextField textFieldDynamicHeapMem;
    public JTextField textFieldThreadNum;
    public JTextField textFieldAlgorithm;
    public JTextArea textAreaTimePROINFO;
    public JTextField textFieldSysBlockType;

    public JComboBox comboBoxFile;
    private JComboBox comboBoxPriSelect;

    private JButton BtnForCalcAWT;
    private JPanel PanelMemBlock;
    private JLabel LabelPriority;
    public JTextField textFieldIRlogic;
    public JTextField textFieldIRpysical;
    private JTextField LPAddrTextField;
    public JTextField textFieldMemPercent;
    public JTextField textFieldIO;
    public JTextField textFieldLeftTime;
    public JTextField textFieldStackMemory;
    public JTextField textFieldSafeSuq;
    public JTextField textFieldDetectDeadlock;


    public static JTextField[] MemBlocks;





    public OsFrameMainForm()
    {
        BtnForStart.setEnabled(true);
        BtnForAdd.setEnabled(false);
        BtnForSysExit.setEnabled(true);
        BtnForAddPro1.setEnabled(false);
        BtnForStop.setEnabled(false);
        BtnForCalcAWT.setEnabled(false);

        comboBoxFile.addItem("input1");
        comboBoxFile.addItem("input2");
        comboBoxFile.setSelectedIndex(1);//默认选择input2

        comboBoxPriSelect.addItem("Default_RR");
        comboBoxPriSelect.addItem("Static");
        //默认的设置为选择Static
        comboBoxPriSelect.setSelectedIndex(1);
        comboBoxPriSelect.setEnabled(true);//设置为可编辑

        textFieldSysTime.setText("0");
        textFieldProcess.setText("无");
        textFieldCPU.setText("空闲");
        textFieldProNum.setText("0");

        textFieldDynamicHeapMem.setText("0KB");
        textFieldStackMemory.setText("0KB");
        textFieldMemPercent.setText("0.00%");

        textFieldThreadNum.setText("3");//初始化线程数
        textFieldAlgorithm.setText("时间片轮转调度");
        textFieldSysBlockType.setText("无");
        textFieldDetectDeadlock.setText("安全");
        textFieldSafeSuq.setText("<null>");

        textFieldLeftTime.setText("3s");
        List<JTextField> texts =Arrays.asList(
                textFieldSysTime,
                textFieldProcess,
                textFieldCPU,
                textFieldProNum,
                textFieldDynamicHeapMem,
                textFieldThreadNum,
                textFieldAlgorithm,
                textFieldSysBlockType,
                LPAddrTextField,
                textFieldIRlogic,
                textFieldIRpysical,
                textFieldMemPercent,
                textFieldIO,
                textFieldLeftTime,
                textFieldStackMemory,
                textFieldSafeSuq,
                textFieldDetectDeadlock
        );
        texts.forEach(text -> text.setHorizontalAlignment(JTextField.CENTER));

        List<JTextField> textBoxs = Arrays.asList(
                textReadyQueue,//就绪队列
                textEndQueue,//队列
                textFieldInputBlockQ1,//输入阻塞队列
                textFieldOutputBlockQ2,//输出阻塞队列
                textComeJobs,//已到达作业
                textFieldProcess,//正在处理
                textFieldSysTime,//系统时间
                textFieldCPU,//CPU状态
                textFieldProNum,//进程数
                textFieldDynamicHeapMem,//动态内存
                textFieldStackMemory,//栈内存
                textFieldThreadNum,//线程数
                textFieldAlgorithm,//算法
                textFieldSysBlockType,//阻塞类型
                LPAddrTextField,// 逻辑地址
                textFieldIRlogic,//逻辑指令
                textFieldIRpysical,//物理指令
                textFieldMemPercent,//内存占用率
                textFieldIO,//IO状态
                textFieldLeftTime,//剩余时间片
                textFieldSafeSuq,//安全序列
                textFieldDetectDeadlock//死锁检测
        );
        textBoxs.forEach(textBox -> textBox.setEditable(false));//设置不可编辑
        textBoxs.forEach(textBox -> textBox.setHorizontalAlignment(JTextField.CENTER));//设置居中显示
        JTextArea[] textAreas = {textAreaEventArgs, textAreaInfoStatus,textAreaAddress,textAreaTimePROINFO};
        for (JTextArea textArea : textAreas)
        {
                textArea.setLineWrap(true);//设置自动换行,当一行显示不下时,自动换行
                textArea.setEditable(false);//设置不可编辑
        }

        MemBlocks = new JTextField[Memory.MEMORY_SIZE];//初始化内存块
        int[] OptimalDivide = GetOptimalDivide(Memory.MEMORY_SIZE);
        PanelMemBlock.setLayout(new GridLayout(OptimalDivide[0], OptimalDivide[1],1,1));
        for (int i = 0; i < Memory.MEMORY_SIZE; i++) {
            MemBlocks[i] = new JTextField();
            MemBlocks[i].setEditable(false);
            MemBlocks[i].setHorizontalAlignment(JTextField.CENTER);
            MemBlocks[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));//
            //设置为淡蓝色
            MemBlocks[i].setBackground(new Color(175, 238, 238));
            MemBlocks[i].setText("空闲");
            MemBlocks[i].setPreferredSize(new Dimension(50, 50));
            PanelMemBlock.add(MemBlocks[i]);
        }

        //下面为事件区域
        comboBoxPriSelect.addActionListener(e -> {
            String select = Objects.requireNonNull(comboBoxPriSelect.getSelectedItem()).toString();
            switch (select)
            {
                case "Default_RR":
                    ProcessScheduling_thread.selectstaticPri=false;
                    this.LabelPriority.setText("非抢占式RR");
                    break;
                case "Static":
                    ProcessScheduling_thread.selectstaticPri=true;
                    this.LabelPriority.setText("静态抢占式");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + select);
            }
        });
        comboBoxFile.addActionListener(e -> {
            String select = Objects.requireNonNull(comboBoxFile.getSelectedItem()).toString();
            switch (select)
            {
                case "input1":
                    JobComeFile.dir="input1";
                    File_Design.TargetFile="output1";
                    break;
                case "input2":
                    JobComeFile.dir="input2";
                    File_Design.TargetFile="output2";
                    break;
            }
        });

        BtnForStart.addActionListener(e -> {
            try {
                UI.ApplicationEventsBegin();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            BtnForStart.setEnabled(false);
            BtnForAdd.setEnabled(true);
            BtnForStop.setEnabled(true);
            BtnForAddPro1.setEnabled(true);
            BtnForCalcAWT.setEnabled(true);
            comboBoxPriSelect.setEnabled(false);//设置为不可编辑
        });
        BtnForStop.addActionListener(e -> {
            try {
                if (BtnForStop.getText().equals("暂停程序")) {
                    BtnForStop.setText("继续执行");
                    UI.ApplicationEventsStop();
                } else {
                    BtnForStop.setText("暂停程序");
                    UI.ApplicationEventsRestart();
                }
            }
            catch (Exception ee) {
               ee.printStackTrace();//打印异常信息
            }
        });

        BtnForAdd.addActionListener(e -> {
            try {
                String dir = Objects.requireNonNull(UI.osFrameMainForm.comboBoxFile.getSelectedItem()).toString();//获取文件夹名
                JobComeFile.AddJobs(dir);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        BtnForSysExit.addActionListener(e -> {

            //显示对话框，返回值为用户的选择结果，如果选择YES，则退出程序，否则不做任何操作
            GetNewSwingForm.JObIO.set(2);//作业退出
            int result = JOptionPane.showConfirmDialog(null, "是否退出程序？", "退出确认", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                try
                {
                    File_Design.SaveText();
                    JobComeFile.GC();//释放内存
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }
            }
        });
        BtnForAddPro1.addActionListener(e -> {
            try {
                JobComeFile.AddOnePro();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        BtnForCalcAWT.addActionListener(e -> {
            ScreenBlankShow.ShowMeanAWT(comboBoxPriSelect.getSelectedIndex());
        });
    }

    private static int[] GetOptimalDivide(int size)
    {
        //输入的size为二的幂次方，例如16，32，64，128
        //返回一个数组，第一个元素为行数，第二个元素为列数,列数大于等于行数,行数和列数最接近
        int[] result = new int[2];
        int row = 0;
        int col = 0;
        int temp;
        for (int i = 1; i <= size/2; i++) {
            if (size % i == 0) {
                temp = size / i;
                if (temp >= i) {
                    row = i;
                    col = temp;
                }
            }
        }
        result[0] = row;
        result[1] = col;
        return result;
    }

    public static void main(String[] args) {
       int size =16;
       int [] result =GetOptimalDivide(size);
        System.out.println(result[0]);
        System.out.println(result[1]);
    }



}
