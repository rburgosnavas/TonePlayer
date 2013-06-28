package com.rburgos.tone;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Date;

@SuppressWarnings("serial")
public class TonePlayer extends JFrame implements ActionListener
{
    private JPanel mainPanel, statusBar;
	private JLabel status;
	private JButton hit;
	private JSlider frequency;

	private AudioFormat audioFormat;
	private AudioInputStream audioIn;
	private SourceDataLine dataLine;
	private Tone tone = null;
    
    static final float SAMPLE_RATE = 32000.0F;
    static final int BIT_SIZE = 8;
    static final int CHANNELS = 1;
    static final boolean SIGNED = false;
    static final boolean BIG_ENDIAN = false;

	private byte audioData[] = new byte[16000 * 4];

	private int freq;
    
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
        
        frequency = new JSlider(JSlider.HORIZONTAL, 1, 100, 1);
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

	@Override
	public void actionPerformed(ActionEvent e)
	{
		
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
