import com.aparapi.Kernel;
import com.aparapi.Range;

public class Application
{
    public static void main( String[] args )
    {
        final int size = 2;

        final double[] a = new double[size];
        final double[] b = new double[size];
        double left = 0.1F;
        double right = 0.1F;

        for (int i = 0; i < size; i++) {
            a[i] = left + left;
            b[i] = left + right;
        }

        final double[] sum = new double[size];

        Kernel kernel = new Kernel(){
            @Override public void run() {
                int gid = getGlobalId();
                sum[gid] = a[gid] + b[gid];
//                sum[gid] = (float)(Math.cos(Math.sin(a[gid])) + Math.sin(Math.cos(b[gid])));
            }
        };
        long t1 = System.currentTimeMillis();
        kernel.execute(Range.create(size));
        long t2 = System.currentTimeMillis();
        System.out.println("sum = " + sum[0]);
        System.out.println("Execution mode = "+kernel.getExecutionMode());
        kernel.dispose();
        System.out.println(t2-t1);
    }
}
