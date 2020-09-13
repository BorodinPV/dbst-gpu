/**
 * Created by Pavel Borodin on 11.09.2020
 */
public class KernelTest extends com.aparapi.Kernel {
    private int values[];
    private int squares[];
    private String test = "Test";

    public KernelTest(int values[]){
        this.values = values;
        squares = new int[values.length];
    }

    public void run() {
        int gid = getGlobalId();
        squares[gid] = values[gid]*values[gid];
    }

    public int[] getSquares(){
        return(squares);
    }

    public String getTest() {
        return test;
    }
}