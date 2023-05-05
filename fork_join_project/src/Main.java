import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().availableProcessors());
        String path = "C:\\Users\\Asus\\Downloads\\SineWaveMinus16.wav";
        AudioOperation audioOperation = null;
        try {
            audioOperation = new AudioOperation(path);
            ForkJoinPool pool = new ForkJoinPool();
            RecursiveFFT task = new RecursiveFFT(audioOperation, 2048, 4, 1000);
            pool.invoke(task);
            task.normalizeSpectrum();
            task.plotSpectrum();
        } catch (Exception e) {
            System.out.println(e);

        }


    }
}
