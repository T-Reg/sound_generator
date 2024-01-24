package io.github.mertguner.sound_generator.generators;

import java.util.ArrayList;
import java.util.List;
 

public class signalDataGenerator {
    private final float _2Pi = 2.0f * (float) Math.PI;

    private int sampleRate = 48000;
    private float phCoefficient = _2Pi / (float) sampleRate;
    private float smoothStep = 1f / (float) sampleRate * 20f;

    private float frequencyLeft = 50;
    private float oldFrequencyLeft = 50;
    private float phLeft = 0;
    private float frequencyRight = 50;
    private float oldFrequencyRight = 50;
    private float phRight = 0;
    private baseGenerator generator = new sinusoidalGenerator();

    private short[] backgroundBuffer;
    private short[] buffer;
    private int bufferSamplesSize;
    private boolean creatingNewData = false;

    public int getSampleRate() { return sampleRate; }
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        phCoefficient = _2Pi / (float) sampleRate;
        smoothStep = 1f / (float) sampleRate * 20f;
    }

    public baseGenerator getGenerator() {
        return generator;
    }
    public void setGenerator(baseGenerator generator) {
        this.generator = generator;
    }

    public float getLeftEarFrequency() {
        return frequencyLeft;
    }
    public float getRightEarFrequency() {
        return frequencyRight;
    }
    public void setLeftEarFrequency(float frequency) {
        this.frequencyLeft = frequency;
    }
    public void setRightEarFrequency(float frequency) {
        this.frequencyRight = frequency;
    }

    public signalDataGenerator(int bufferSamplesSize, int sampleRate) {
        this.bufferSamplesSize = bufferSamplesSize;
        backgroundBuffer = new short[bufferSamplesSize];
        buffer = new short[bufferSamplesSize];
        setSampleRate(sampleRate);
        updateData();
    }

    private void updateData() {
        creatingNewData = true;
        for (int i = 0; i < bufferSamplesSize; i++) {
            if (i % 2 == 0) { // even numbers are left ear and odd numbers are right ear
                oldFrequencyLeft += ((frequencyLeft - oldFrequencyLeft) * smoothStep);
                backgroundBuffer[i] = generator.getValue(phLeft, _2Pi);
                phLeft += (oldFrequencyLeft * phCoefficient);

                //performance of this block is higher than ph %= _2Pi;
                // ifBlock  Test score =  2,470ns
                // ModBlock Test score = 27,025ns
                if (phLeft > _2Pi) {
                    phLeft -= _2Pi;
                }
            } else {
                oldFrequencyRight += ((frequencyRight - oldFrequencyRight) * smoothStep);
                backgroundBuffer[i] = generator.getValue(phRight, _2Pi);
                phRight += (oldFrequencyRight * phCoefficient);

                //performance of this block is higher than ph %= _2Pi;
                // ifBlock  Test score =  2,470ns
                // ModBlock Test score = 27,025ns
                if (phRight > _2Pi) {
                    phRight -= _2Pi;
                }
            }
        }
        creatingNewData = false;
    }

    public short[] getData() {
        if (!creatingNewData) {
            System.arraycopy(backgroundBuffer, 0, buffer, 0, bufferSamplesSize);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateData();
                }
            }).start();
        }
        return this.buffer;
    }
}
