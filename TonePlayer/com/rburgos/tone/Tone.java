package com.rburgos.tone;

public class Tone
{
    private byte[] dataBuffer;
    private int byteLength;
    private double freq;
    private static final float SAMPLE_RATE = 8000.0F;
    
    public Tone(byte[] dataBuffer)
    {
        this.dataBuffer = dataBuffer;
        this.byteLength = dataBuffer.length;
    }
    
    public void play()
    {
	    for (int i = 0; i < byteLength; i++)
        {
            double angle = (2.0 * Math.PI * i) / SAMPLE_RATE;
	        double sine = freq * (Math.sin(angle));
	        dataBuffer[i] = (byte)((short)sine * 16000);
        }
    }
    
    public void setFrequency(int frequency)
    {
        this.freq = (double) frequency;
    }
}
