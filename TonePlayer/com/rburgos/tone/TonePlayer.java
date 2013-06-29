package com.rburgos.tone;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
    
    static final float SAMPLE_RATE = 8000.0F;
    static final int BIT_SIZE = 8;
    static final int CHANNELS = 1;
    static final boolean SIGNED = false;
    static final boolean BIG_ENDIAN = false;

	private byte buffer[] = new byte[16000 * 4];

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
                tone = new Tone(buffer);
                tone.setFrequency(freq);
                tone.play();
                playData();
	            new Thread(new Runnable()
	            {
		            @Override
		            public void run()
		            {
						while(true)
						{
							status.setText(System.nanoTime()/(100000*10000) + "");
							try
							{
								Thread.sleep(1000);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
		            }
	            }).start();
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
            InputStream in = new ByteArrayInputStream(buffer);
            audioFormat = new AudioFormat(SAMPLE_RATE, BIT_SIZE, CHANNELS,
                    SIGNED, BIG_ENDIAN);
            long len = buffer.length / audioFormat.getFrameSize();
            audioIn = new AudioInputStream(in, audioFormat, len);
            
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,
                    audioFormat);
            dataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            new ListenThread().start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

	class ListenThread extends Thread
    {
		byte playBuffer[] = new byte[16000 * 4];
        public void run()
        {
            try
            {
	            openData();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(0);
            }
        }

	    private void openData() throws LineUnavailableException, IOException
	    {
		    dataLine.open(audioFormat);
		    dataLine.start();

		    int length;

		    while ((length = audioIn.read(playBuffer, 0, playBuffer.length))
				    != -1)
		    {
		        if (length > 0)
		        {
		            dataLine.write(playBuffer, 0, length);
		        }
		    }

		    dataLine.drain();
		    dataLine.stop();
		    dataLine.close();
	    }
    }

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO
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
}
