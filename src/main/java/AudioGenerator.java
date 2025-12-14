package com.example;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioGenerator {
    private SourceDataLine line;
    private volatile boolean isPlaying = false;
    private Thread playbackThread;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private int frequency = 440; // Default to A4 note
    private int amplitude = 50;  // 0-100 scale
    private String waveformType = "Sine";

    public void setFrequency(int freq) {
        this.frequency = freq;
    }

    public void setAmplitude(int amp) {
        this.amplitude = amp;
    }

    public void setWaveformType(String type) {
        this.waveformType = type;
    }

    public void startPlayback() {
        if (!isPlaying) {
            isPlaying = true;
            openAudioLine();
            
            playbackThread = new Thread(this::playLoop);
            playbackThread.start();
        }
    }

    public void stopPlayback() {
        isPlaying = false;
        if (playbackThread != null) {
            playbackThread.interrupt();
            playbackThread = null;
        }
        closeAudioLine();
    }

    private void openAudioLine() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line matching " + info + " is not supported.");
                return;
            }

            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable: " + e.getMessage());
        }
    }

    private void closeAudioLine() {
        if (line != null) {
            line.drain();
            line.stop();
            line.close();
            line = null;
        }
    }

    private void playLoop() {
        byte[] buffer = new byte[1024];  // 1024 samples
        int sampleRate = 44100;
        int currentSample = 0;

        while (isPlaying && !Thread.currentThread().isInterrupted()) {
            // Generate samples
            for (int i = 0; i < buffer.length; i += 2) {
                double value = generateSample(currentSample);
                
                // Convert to 16-bit signed integer and store in byte array
                short shortValue = (short) (value * (amplitude / 100.0) * Short.MAX_VALUE);
                
                buffer[i] = (byte) (shortValue & 0xff);
                buffer[i + 1] = (byte) ((shortValue >> 8) & 0xff);
                
                currentSample++;
            }

            // Write to audio line
            if (line != null) {
                line.write(buffer, 0, buffer.length);
            }
        }
    }

    private double generateSample(int sample) {
        double period = 44100.0 / frequency;
        double angle = 2.0 * Math.PI * sample / period;

        switch (waveformType) {
            case "Sine":
                return Math.sin(angle);
            case "Square":
                return Math.sin(angle) >= 0 ? 1.0 : -1.0;
            case "Triangle":
                // Triangle wave: 4 * (|x - floor(x + 0.5)| - 0.5) where x = angle/2pi
                double normalizedAngle = (angle / (2 * Math.PI)) % 1.0;
                if (normalizedAngle < 0) normalizedAngle += 1.0;
                return 4 * (Math.abs(normalizedAngle - Math.floor(normalizedAngle + 0.5)) - 0.5);
            case "Sawtooth":
                // Sawtooth wave: 2 * (x - floor(x + 0.5)) where x = angle/2pi
                double normAngle = (angle / (2 * Math.PI)) % 1.0;
                if (normAngle < 0) normAngle += 1.0;
                return 2 * (normAngle - 0.5);
            default:
                return Math.sin(angle);
        }
    }
}