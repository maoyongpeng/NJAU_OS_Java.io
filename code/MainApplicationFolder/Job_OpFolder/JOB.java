package MainApplicationFolder.Job_OpFolder;

import java.util.ArrayList;

public class JOB {

    public int JobsID;

    public ArrayList<IRs> instructs;
    public int InTimes;

    public int InstrucNum;



    public JOB(int jobsId, int inTime, int instrucNums, ArrayList<IRs> instructs) {
        this.JobsID = jobsId;
        this.InTimes = inTime;
        this.InstrucNum = instrucNums;
        this.instructs = instructs;
    }


    public int getJobsID() {
        return JobsID;
    }
    public ArrayList<IRs> getInstructs() {
        return instructs;
    }
    public int getInTimes() {
        return InTimes;
    }
    public int getInstrucNum() {
        return InstrucNum;
    }

    @Override
    public String toString() {
        return String.format("(JOB %d)", JobsID);
    }




}
