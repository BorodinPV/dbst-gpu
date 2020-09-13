public class App2 {

    public static void main(String[] args) {

        final int size = 50000000;

        final float[] a = new float[size];
        final float[] b = new float[size];

        for (int i = 0; i < size; i++) {
            a[i] = (float) (Math.random() * 100);
            b[i] = (float) (Math.random() * 100);
        }

        final float[] sum = new float[size];
        long t1 = System.currentTimeMillis();
        for(int i=0;i<size;i++) {
            sum[i]=a[i]+b[i];
//            sum[i] = (float)(Math.cos(Math.sin(a[i])) + Math.sin(Math.cos(b[i])));;
        }

        long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);

    }
}