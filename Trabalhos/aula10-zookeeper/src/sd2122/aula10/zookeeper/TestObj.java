package sd2122.aula10.zookeeper;

public class TestObj {

    private int num;
    private String msg;

    public TestObj(int num, String msg){
        this.num = num;
        this.msg = msg;
    }

    public int getNum(){
        return num;
    }

    public String getMsg(){
        return msg;
    }

    public int changeNum(int n){
        this.num = n;
        return num;
    }

    public void changeMSGNum(String msg, int num){
        this.num = num;
        this.msg = msg;
        System.out.println("Msg = "+msg);
        System.out.println("Num = "+num);
    }

}
