import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;

public class AudioOperation {
    private final int numChannels;
    private final int sampleSizeInBits;
    private final float sampleRate;

    private final float frameRate;

    private final double durationInSeconds;

    private final double[] rawAudioData;





    public AudioOperation(String path) throws Exception {
        File file = new File(path);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        this.numChannels = audioInputStream.getFormat().getChannels();
        this.sampleSizeInBits = audioInputStream.getFormat().getSampleSizeInBits();
        this.sampleRate = audioInputStream.getFormat().getSampleRate();
        this.frameRate = audioInputStream.getFormat().getFrameRate();
        this.durationInSeconds = (double) audioInputStream.getFrameLength() / frameRate;
        this.rawAudioData = extractRawAudioData(audioInputStream);

    }

    public double[] extractRawAudioData(AudioInputStream audioInputStream) throws IOException {
        byte[] buffer = new byte[numChannels * (int) (durationInSeconds * sampleRate) * sampleSizeInBits / 8];
        int bytesRead = audioInputStream.read(buffer);
        double[] rawAudioData = new double[bytesRead/4];
        for (int i = 0 ; 4*i+3<bytesRead ; i++){
            double left = (short)((buffer[4*i+1] & 0xff) << 8) | (buffer[4*i] & 0xff);
            double right = (short)((buffer[4*i+3] & 0xff) << 8) | (buffer[4*i+2] & 0xff);
            rawAudioData[i] = (left + right) / 2;
        }
        return rawAudioData;
    }

    public double[]getRawAudioData(){
        return rawAudioData;
    }
    public int getNumChannels(){
        return numChannels;
    }
    public int getSampleSizeInBits(){
        return sampleSizeInBits;
    }
    public float getSampleRate(){
        return sampleRate;
    }
    public float getFrameRate(){
        return frameRate;
    }
    public double getDurationInSeconds(){
        return durationInSeconds;
    }
}
