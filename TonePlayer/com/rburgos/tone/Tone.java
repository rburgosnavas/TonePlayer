package com.rburgos.tone;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;


public class Tone
{
    private ByteBuffer byteBuffer;
    private ShortBuffer shortBuffer;
    
    private int byteLength;
    private int sampleLength;
    private int bytesPerSample = 2;
    
    private double freq;
    
    private static final float SAMPLE_RATE = 44100.0F;
    
    public Tone(byte[] synthDataBuffer)
    {
        this.byteBuffer = ByteBuffer.wrap(synthDataBuffer);
        this.shortBuffer = byteBuffer.asShortBuffer();
        this.byteLength = synthDataBuffer.length;       
    }
    
    public void play()
    {
        sampleLength = byteLength / bytesPerSample;
        
        for (int i = 0; i < sampleLength; i++)
        {
            double time = i / SAMPLE_RATE;
            
            /*
            double sine = (Math.sin(2 * Math.PI * freq * time) + 
                    Math.sin(2 * Math.PI * (freq / 1.8) * time) + 
                    Math.sin(2 * Math.PI * (freq / 1.5) * time)) / 3.0;
            */
            
            double sine = (Math.sin(2 * Math.PI * freq * time) + 
                    Math.sin(2 * Math.PI * freq / 1.8 * time) * 
                    Math.exp(2 * Math.PI * freq * time / 2)) / 8.0;
            
            shortBuffer.put((short) (16000 * sine));
        }
    }
    
    public void setFrequency(int frequency)
    {
        this.freq = (double) frequency;
    }
}
