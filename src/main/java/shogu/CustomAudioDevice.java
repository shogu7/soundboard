package shogu;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDeviceBase;

public class CustomAudioDevice extends AudioDeviceBase {
    private final SourceDataLine sourceLine;
    private SourceDataLine localLine;
    private float volume;
    private static final int BUFFER_SIZE = 16384;
    private boolean enableLocalPlayback;

    public CustomAudioDevice(Mixer mixer) throws LineUnavailableException {
        this(mixer, 0.7f, true);
    }
    
    public CustomAudioDevice(Mixer mixer, float volume) throws LineUnavailableException {
        this(mixer, volume, true);
    }
    
    public CustomAudioDevice(Mixer mixer, float volume, boolean enableLocalPlayback) throws LineUnavailableException {
        this.volume = volume;
        this.enableLocalPlayback = enableLocalPlayback;
        
        AudioFormat format = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44100,
            16,
            2,
            4,
            44100,
            false
        );
        
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        sourceLine = (SourceDataLine) mixer.getLine(info);
        
        sourceLine.open(format, BUFFER_SIZE * 4);
        sourceLine.start();
        
        if (enableLocalPlayback) {
            try {
                DataLine.Info localInfo = new DataLine.Info(SourceDataLine.class, format);
                localLine = (SourceDataLine) AudioSystem.getLine(localInfo);
                localLine.open(format, BUFFER_SIZE * 4);
                localLine.start();
            } catch (LineUnavailableException e) {
                System.out.println("Could not create local playback line: " + e.getMessage());
            }
        }
        
        applyVolume();
    }
    
    public void setVolume(float volume) {
        this.volume = volume;
        applyVolume();
    }
    
    private void applyVolume() {
        try {
            if (sourceLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log10(Math.max(volume, 0.0001f)) * 20.0f);
                dB = Math.max(dB, gainControl.getMinimum());
                dB = Math.min(dB, gainControl.getMaximum());
                gainControl.setValue(dB);
                System.out.println("MP3 volume set to: " + dB + " dB");
            } else if (sourceLine.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl volumeControl = (FloatControl) sourceLine.getControl(FloatControl.Type.VOLUME);
                volumeControl.setValue(volume);
                System.out.println("MP3 volume set to: " + volume);
            } else {
                System.out.println("Volume control not supported for MP3 line");
            }
            
            if (enableLocalPlayback && localLine != null) {
                if (localLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) localLine.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log10(Math.max(volume, 0.0001f)) * 20.0f);
                    dB = Math.max(dB, gainControl.getMinimum());
                    dB = Math.min(dB, gainControl.getMaximum());
                    gainControl.setValue(dB);
                } else if (localLine.isControlSupported(FloatControl.Type.VOLUME)) {
                    FloatControl volumeControl = (FloatControl) localLine.getControl(FloatControl.Type.VOLUME);
                    volumeControl.setValue(volume);
                }
            }
        } catch (Exception e) {
            System.out.println("Error applying volume: " + e.getMessage());
        }
    }

    @Override
    protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException {
        if (len <= 0 || samples == null || offs >= samples.length) {
            return;
        }
        
        len = Math.min(len, samples.length - offs);
        
        byte[] buffer = new byte[len * 2];
        
        for (int i = 0; i < len; i++) {
            short sample = samples[offs + i];
            
            buffer[2 * i] = (byte) (sample & 0xff);
            buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xff);
        }
        
        int written = 0;
        final int totalToWrite = buffer.length;
        
        while (written < totalToWrite) {
            int remaining = totalToWrite - written;
            int bytesToWrite = Math.min(BUFFER_SIZE, remaining);
            int bytesWritten = sourceLine.write(buffer, written, bytesToWrite);
            
            if (enableLocalPlayback && localLine != null) {
                localLine.write(buffer, written, bytesToWrite);
            }
            
            if (bytesWritten <= 0) {
                Thread.onSpinWait();
                continue;
            }
            
            written += bytesWritten;
        }
    }

    @Override
    public void close() {
        if (sourceLine != null) {
            try {
                sourceLine.drain();
                sourceLine.stop();
                sourceLine.close();
            } catch (Exception e) {
                System.out.println("Error closing audio line: " + e.getMessage());
            }
        }
        
        if (enableLocalPlayback && localLine != null) {
            try {
                localLine.drain();
                localLine.stop();
                localLine.close();
            } catch (Exception e) {
                System.out.println("Error closing local audio line: " + e.getMessage());
            }
        }
    }

    @Override
    public int getPosition() {
        return (sourceLine != null) ? (int) (sourceLine.getMicrosecondPosition() / 1000) : 0;
    }
    
    public SourceDataLine getSourceLine() {
        return sourceLine;
    }
}
