import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Pavel Borodin on 12.09.2020
 */

public class ArithmeticCode {

    public static void main(String[] args) {

//        String text = readFile(files.getFile());
        String text = "Меня зовут павел и это моя первая программа для сжатия!";


        List<Character> alphabet = new ArrayList<>();
        for (char c : text.toCharArray()) {
            alphabet.add(c);
        }

        double isDecode = 0.0;
        for (char c : text.toCharArray()) {
            double v = adaptiveCoding(c, alphabet);
            isDecode = v;
        }
        String decode = decode(isDecode, alphabet, text.length());
        System.out.println("decode = " + decode);
    }


    private static String decode(double code, List<Character> alphabet, int len) {
        List<Weight> weights = defineWeights(alphabet);
        List<ArraySegments> arraySegments = defineSegments(alphabet);
        String ans = "";

        List<Character> alp = new ArrayList<>(alphabet);

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < alphabet.size(); j++) {
                char c = alp.get(j);
                ArraySegments symbolAlphabet = findSymbolAlphabet(c, arraySegments);

                if (code >= symbolAlphabet.getSegment().getLeft() && code < symbolAlphabet.getSegment().getRight()) {
                    Weight weightSymbol = findWeightSymbol(c, weights);

                    for (int t = 0; t < weights.size(); t++) {
                        if (weights.get(t).getSymbol().equals(weightSymbol.getSymbol())) {
                            weights.get(t).setWeight(weights.get(i).getWeight() + 1);

                        }
                    }

                    double v = (code - symbolAlphabet.getSegment().getLeft());
                    double v1 = (symbolAlphabet.getSegment().getRight() - symbolAlphabet.getSegment().getLeft());
                    code = v / v1;

                    arraySegments = resizeSegments(alphabet, weights, arraySegments);
                    break;
                }
            }
        }

        for (ArraySegments arraySegment : arraySegments) {
            ans = ans + arraySegment.getSymbol();
        }

        return ans;
    }


    private static double adaptiveCoding(char in, List<Character> alphabet) {
        List<Weight> weights = defineWeights(alphabet);
        List<ArraySegments> arraySegments = defineSegments(alphabet);

        Map<Character, ArraySegments> segmentsMap = arraySegments
                .parallelStream()
                .collect(Collectors.toMap(ArraySegments::getSymbol, Function.identity(),
                (address1, address2) -> address1));

        Map<Character, Weight> weightMap = weights
                .parallelStream()
                .collect(Collectors.toMap(Weight::getSymbol, Function.identity(),
                        (address1, address2) -> address1));

        double left = 0;
        double right = 1;

        for (int i = 0; i < alphabet.size(); i++) {
            // увеличение веса символа строки
            weightMap.get(in).setWeight(weights.get(i).getWeight() + 1);
//            ArraySegments symbolAlphabet = findSymbolAlphabet(in, arraySegments);
            ArraySegments symbolAlphabet = segmentsMap.get(in);
//            double newRight = (left + (right - left) * symbolAlphabet.getSegment().getRight());
//            double newLeft = (left + (right - left) * symbolAlphabet.getSegment().getLeft());

            float[] testGpu = getTestGpu(left, right, symbolAlphabet.getSegment().getLeft(), symbolAlphabet.getSegment().getRight());

            left = testGpu[0];
            right = testGpu[1];
            for (float v : testGpu) {
                System.out.println("--------------");
                System.out.println("v = " + v);
            }
            arraySegments = resizeSegments(alphabet, weights, arraySegments);
        }

        return (left + right) / 2;
    }

    private static float[] getTestGpu(double left, double right, double sLeft, double sRight) {

        final float[] a = new float[2];
        final float[] b = new float[2];

        for (int i = 0; i < 2; i++) {
            a[i] = (float) (left + (right - left) * sRight);
            b[i] = (float) (left + (right - left) * sRight);
        }
        final float[] sum = new float[2];

        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int gid = getGlobalId();
                sum[gid] = a[gid] + b[gid];
            }
        };
        kernel.execute(Range.create(2));
        kernel.dispose();
        return sum;
    }

    private static List<ArraySegments> resizeSegments(List<Character> alphabet, List<Weight> weight, List<ArraySegments> arraySegments) {
        double l = 0.0F;
        int sum = 0;

        for (int i = 0; i < alphabet.size(); i++) {
            sum = sum + weight.get(i).getWeight();
        }

        for (int i = 0; i < arraySegments.size(); i++) {
            findSymbolAlphabet(alphabet.get(i), arraySegments).getSegment().setLeft(l);
            findWeightSymbol(alphabet.get(i), weight).getWeight();
            double v = l + (findWeightSymbol(alphabet.get(i), weight).getWeight() / (double) sum);
            findSymbolAlphabet(alphabet.get(i), arraySegments).getSegment().setRight(v);
            l = v;

        }
        return arraySegments;
    }


    private static List<Weight> defineWeights(List<Character> alphabet) {
        List<Weight> weight = new ArrayList<>();
        for (int i = 0; i < alphabet.size(); i++) {
            weight.add(new Weight(alphabet.get(i), 1));
        }
        return weight;
    }

    private static List<ArraySegments> defineSegments(List<Character> alphabet) {
        // определяем размер подотрезков
        double p = 1 / (double) alphabet.size();
        List<Segment> segments = new ArrayList<>();
        // задаём левую и правую границы каждого из отрезков
        double curLeft = 0;
        double curRight = p;

        // разбиваем отрезок [0,1) на подотрезки, соответсвующие символам алфавита
        List<ArraySegments> arraySegments = new ArrayList<>();

        for (int i = 0; i < alphabet.size(); i++) {
            segments.add(new Segment(curLeft, curRight));
            curLeft = curRight;
            curRight = curRight + p;

            arraySegments.add(new ArraySegments(alphabet.get(i), segments.get(i)));
        }
        return arraySegments;
    }

    public static ArraySegments findSymbolAlphabet(char symbol, List<ArraySegments> arraySegments) {
        for (int i = 0; i < arraySegments.size(); i++) {
            if (arraySegments.get(i).getSymbol().equals(symbol)) {
                return arraySegments.get(i);
            }
        }
        return null;
    }

    public static Weight findWeightSymbol(char symbol, List<Weight> weights) {
        for (int i = 0; i < weights.size(); i++) {
            if (weights.get(i).getSymbol().equals(symbol)) {
                return weights.get(i);
            }
        }
        return null;
    }

    public static class Weight {
        private Character symbol;
        private int weight;

        public Weight(Character symbol, int weight) {
            this.symbol = symbol;
            this.weight = weight;
        }

        public Character getSymbol() {
            return symbol;
        }

        public int getWeight() {
            return weight;
        }

        public void setSymbol(Character symbol) {
            this.symbol = symbol;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Weight{" +
                    "symbol=" + symbol +
                    ", weight=" + weight +
                    '}';
        }
    }

    public static class ArraySegments {
        private final Character symbol;
        private final Segment segment;

        public ArraySegments(Character symbol, Segment segment) {
            this.symbol = symbol;
            this.segment = segment;
        }

        public Character getSymbol() {
            return symbol;
        }

        public Segment getSegment() {
            return segment;
        }

        @Override
        public String toString() {
            return "ArraySegments{" +
                    "symbol=" + symbol +
                    ", segment=" + segment +
                    '}';
        }
    }

    public static class Segment {
        private double left;
        private double right;

        public Segment(double left, double right) {
            this.left = left;
            this.right = right;
        }

        public double getLeft() {
            return left;
        }

        public double getRight() {
            return right;
        }

        public void setLeft(double left) {
            this.left = left;
        }

        public void setRight(double right) {
            this.right = right;
        }

        @Override
        public String toString() {
            return "Segment{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }
    }
}

