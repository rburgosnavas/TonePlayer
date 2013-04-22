package com.rburgos.tone;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class TonePlayer extends JFrame
{
    JPanel mainPanel, statusBar;
    JLabel status;
    JButton hit;
    JSlider frequency;
    
    AudioFormat audioFormat;
    AudioInputStream audioIn;
    SourceDataLine dataLine;
    Tone tone = null;
    
    static final float SAMPLE_RATE = 32000.0F;
    static final int BIT_SIZE = 8;
    static final int CHANNELS = 1;
    static final boolean SIGNED = false;
    static final boolean BIG_ENDIAN = false;
    
    byte audioData[] = new byte[16000 * 4];
    
    int freq;
    
    public TonePlayer()
    {
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(200, 200);
        createGUI();
        getContentPane().add(mainPanel);
        freq = frequency.getMinimum();
    }
    
    public void createGUI()
    {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        hit = new JButton("hit!");
        hit.setBackground(Color.BLACK);
        hit.setForeground(Color.WHITE);
        hit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                tone = new Tone(audioData);
                tone.setFrequency(freq);
                tone.play();
                playData();
            }
        });

        status = new JLabel("...");
        status.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusBar = new JPanel();
        statusBar.add(status);
        
        frequency = new JSlider(JSlider.HORIZONTAL, 1, 1000, 1);
        frequency.addChangeListener(new ChangeListener()
        {
            
            @Override
            public void stateChanged(ChangeEvent e)
            {
                freq = frequency.getValue();
            }
        });
        
        mainPanel.add(hit, BorderLayout.CENTER);
        mainPanel.add(frequency, BorderLayout.NORTH);
        mainPanel.add(statusBar, BorderLayout.SOUTH);
    }
    
    public void playData()
    {
        try
        {
            // InputStream takes a byte array audioData as buffer
            InputStream in = new ByteArrayInputStream(audioData);
            
            // Initialize audioFormat
            audioFormat = new AudioFormat(SAMPLE_RATE, BIT_SIZE, CHANNELS, 
                    SIGNED, BIG_ENDIAN);
            
            // Calculate length of data stream
            long len = audioData.length / audioFormat.getFrameSize();
            
            // Create audioIn
            audioIn = new AudioInputStream(in, audioFormat, len);
            
            // 
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, 
                    audioFormat);
            
            //
            dataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            
            //
            new ListenThread().start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new TonePlayer();
            }
        });
    }
    
    class ListenThread extends Thread
    {
        byte playBuffer[] = new byte[16000];
        
        public void run()
        {
            try
            {
                dataLine.open(audioFormat);
                dataLine.start();
                
                int length;
                
                while ((length = audioIn.read(playBuffer, 0, playBuffer.length)) != -1)
                {
                    if (length > 0)
                    {
                        dataLine.write(playBuffer, 0, length);
                    }
                }
                
                dataLine.drain();
                dataLine.stop();
                dataLine.close();
                
                status.setText(new Date().getTime() + "");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
























