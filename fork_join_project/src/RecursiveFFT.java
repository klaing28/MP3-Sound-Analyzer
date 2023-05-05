import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;


import static java.lang.Math.abs;
import static javax.swing.UIManager.getColor;

public class RecursiveFFT extends java.util.concurrent.RecursiveAction {

    private final int SEQUENTIAL_THRESHOLD;
    private final double[] data;

    private final double[][] spectrum;

    private final AudioOperation audioOperation;

    private final int binSize;

    private final int overlap;

    private final int binStep;
    private final int startBin;

    private final int endBin;
    private double maxAmp;
    private double minAmp;


    public RecursiveFFT(AudioOperation audioOperation, int binSize, int overlap, int sequentialThreshold) {
        this.SEQUENTIAL_THRESHOLD = sequentialThreshold;
        this.audioOperation = audioOperation;
        this.data = audioOperation.getRawAudioData();
        this.binSize = binSize;
        this.overlap = overlap;
        this.binStep = binSize / overlap;
        this.startBin = 0;
        this.endBin = (data.length - binSize) / binStep;
        this.spectrum = new double[(data.length - binSize) / binStep][binSize / 2 + 1];

    }

    public RecursiveFFT(AudioOperation audioOperation, double[][] spectrum, int binSize, int overlap,
                        int sequentialThreshold, int startBin, int endBin) {
        this.spectrum = spectrum;
        this.SEQUENTIAL_THRESHOLD = sequentialThreshold;
        this.audioOperation = audioOperation;
        this.data = audioOperation.getRawAudioData();
        this.binSize = binSize;
        this.overlap = overlap;
        this.binStep = binSize / overlap;
        this.startBin = startBin;
        this.endBin = endBin;
    }


    @Override
    protected void compute() {
        if (abs(endBin - startBin) > SEQUENTIAL_THRESHOLD) { //if the number of bins is greater than the threshold
            // divide task in half
            try {
                RecursiveFFT subTask1 = new RecursiveFFT(audioOperation, spectrum, binSize,
                        overlap, SEQUENTIAL_THRESHOLD, startBin, startBin+(endBin-startBin) / 2);
                RecursiveFFT subTask2 = new RecursiveFFT(audioOperation, spectrum, binSize,
                        overlap, SEQUENTIAL_THRESHOLD, startBin+(endBin-startBin) / 2, endBin);
                invokeAll(subTask1, subTask2);
            } catch (Exception e) {
                System.out.println("Error at bin " + startBin);
            }
        } else {
            computeDirectly();
        }
    }


    private Complex[] createComplex(double[] data) {

        Complex[] complex_data = new Complex[data.length];
        try {
            for (int i = 0; i < data.length; i++) {
                complex_data[i] = new Complex(data[i], 0.0);
            }
        }catch (Exception e){
            System.out.println("Error create complex at bin " + startBin);
        }
        return complex_data;
    }

    private void computeDirectly() {
        for (int i = startBin; i < endBin; i++) {

            Complex[] complexInput = createComplex(Arrays.copyOfRange(data, i * binStep, i * binStep + binSize));
            Complex[] bin = FFT.fft(complexInput);
            try{
            for (int j = 0; j < binSize / 2+1 ; j++) {
                double amp_square = (bin[j].re() * bin[j].re()) + (bin[j].im() * bin[j].im());
                if (amp_square == 0.0) {
                    spectrum[i][j] = 0.0;
                } else {
                    spectrum[i][j] = 10.0 * Math.log10(amp_square);
                }
            }}
            catch (Exception e){
                System.out.println("Error in compute at bin " + i);
            }
        }

    }
    private double findMaxAmp(){
        double max = 0;
        for (int i = 0; i < spectrum.length; i++) {
            for (int j = 0; j < spectrum[0].length; j++) {
                if(spectrum[i][j] > max){
                    max = spectrum[i][j];
                }
            }
        }
        return max;
    }
private double findMinAmp(){
        double min = 0;
        for (int i = 0; i < spectrum.length; i++) {
            for (int j = 0; j < spectrum[0].length; j++) {
                if(spectrum[i][j] < min){
                    min = spectrum[i][j];
                }
            }
        }
        return min;
    }
    public void normalizeSpectrum() {
        maxAmp = findMaxAmp();
        minAmp = findMinAmp();

        for (int i = 0; i < spectrum.length; i++) {
            for (int j = 0; j < spectrum[0].length; j++) {
                spectrum[i][j] = (spectrum[i][j] - minAmp) / (maxAmp - minAmp);
            }
        }
    }

    public void plotSpectrum(){
        BufferedImage spectrumImage = new BufferedImage(spectrum.length, spectrum[0].length, BufferedImage.TYPE_INT_RGB);
        double ratio;
        for (int i = 0; i < spectrum.length; i++) {
            for (int j = 0; j < spectrum[0].length; j++) {
                ratio = spectrum[i][j];
                Color color =  getColor(1.0-ratio);
                spectrumImage.setRGB(i, j, color.getRGB());
            }
        }
        File outputfile = new File("spectrum.png");
        try {
            ImageIO.write(spectrumImage, "png", outputfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        }
    }



