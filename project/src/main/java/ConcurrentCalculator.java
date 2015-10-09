import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by liuwei on 2015/10/9.
 */
public class ConcurrentCalculator {
    private ExecutorService exec;
    private int cpuCoreNumber;
    private List<Future<Long>> tasks = new ArrayList<Future<Long>>();

    class SumCalculator implements Callable<Long>{
        private int[] numbers;
        private int start;
        private int end;

        public SumCalculator(final int[] numbers, int start, int end){
            this.numbers = numbers;
            this.start = start;
            this.end = end;
        }

        public Long call() throws Exception {
            Long sum = 0l;
            for (int i = start; i<end; i++){
                sum += numbers[i];
            }
            return sum;
        }
    }

    public ConcurrentCalculator(){
        cpuCoreNumber = Runtime.getRuntime().availableProcessors();
        exec = Executors.newFixedThreadPool(cpuCoreNumber);
    }

    public Long sum(final int[] numbers){
        for (int i=0; i<cpuCoreNumber; i++){
            int increment = numbers.length/cpuCoreNumber + 1;
            int start = increment * i;
            int end = increment * i + increment;
            if(end > numbers.length){
                end = numbers.length;
                SumCalculator subCalc = new SumCalculator(numbers, start, end);
                FutureTask<Long> task = new FutureTask<Long>(subCalc);
                tasks.add(task);
                if (!exec.isShutdown()){
                    exec.submit(task);
                }
            }
        }
        return getResult();
    }

    public Long getResult() {
        Long result = 0l;
        for (Future<Long> task:tasks){
            Long subSum = null;
            try {
                subSum = task.get();
                result += subSum;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void close(){
        exec.shutdownNow();
    }

    public static void main(String[] args){
        int[] numbers = new int[]{1,2,3,4,5,6,7,8,10,11};
        ConcurrentCalculator calc = new ConcurrentCalculator();
        Long sum = calc.sum(numbers);
        System.out.println(sum);
        calc.close();
    }
}
