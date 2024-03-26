package MainApplicationFolder.Memory_OpFolder;

public class Block {//内存块类
    public Boolean Occupied;//是否被占用
    public Integer ProID;//占用该块的进程号


    public Block()
    {
        this.ProID=-2;
        this.Occupied=false;
    }
}
