package odra.cli.gui.memmonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import odra.cli.gui.components.CLIFrame;
import odra.cli.gui.components.layout.ColumnLayout;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.IntegerResult;

/**
 * Memory monitor frame.
 *
 * @author jacenty
 * @version 2007-11-21
 * @since 2007-02-20
 */
public class MemoryMonitor extends CLIFrame
{
	private enum Mode
	{
		SERVER("server"),
		CLIENT("client");
		
		private String mode;
		
		Mode(String mode)
		{
			this.mode = mode;
		}
		
		String getModeName()
		{
			return mode;
		}
	}
	private final Mode mode;
	
	private final DBConnection connection;
	
  private Runtime runtime = Runtime.getRuntime();

  private long minTotal = Long.MAX_VALUE;
  private long maxTotal = Long.MIN_VALUE;

  private long minUsed = Long.MAX_VALUE;
  private long maxUsed = Long.MIN_VALUE;

  private volatile boolean running = false;

  /**
   * Indicates if the connection should be closed together with the monitor
   */
  private boolean closeConnection = true;
  
  private Color totalColor = Color.green;
  private Color usedColor = Color.red;
  private Color gridColor = Color.darkGray;
  private Color backgroundColor = Color.black;
  private Color labelColor = Color.white;

  JPanel panel1 = new JPanel();
  JPanel jPanel1 = new JPanel();
  JButton zamknijButton = new JButton();
  JLabel kLabel1 = new JLabel();
  JLabel kLabel3 = new JLabel();
  JLabel kLabel5 = new JLabel();
  JPanel jPanel5 = new JPanel();
  GridLayout gridLayout1 = new GridLayout();
  JPanel jPanel3 = new JPanel();
  GridLayout gridLayout2 = new GridLayout();
  JTextField minValueTotal = new JTextField(9);
  JTextField maxValueTotal = new JTextField(9);
  JTextField curValueTotal = new JTextField(9);
  JPanel totalPanel = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel jPanel6 = new JPanel();
  ColumnLayout columnLayout1 = new ColumnLayout(1, ColumnLayout.Align.FILL, false);
  BorderLayout borderLayout2 = new BorderLayout();
  JPanel jPanel7 = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();
  JLabel kLabel7 = new JLabel();
  JComboBox okres = new JComboBox(new Double[] {.1, .2, .5, 1d, 2d, 5d, 10d, 20d, 30d, 60d});
  JLabel kLabel8 = new JLabel();
  PlotPanel plot = new PlotPanel();
  JPanel jPanel8 = new JPanel();
  JButton gcButton = new JButton();
  Border border1;
  TitledBorder titledBorder1;
  JTextField curValueUsed = new JTextField(9);
  JPanel usedPanel = new JPanel();
  JLabel kLabel10 = new JLabel();
  JTextField minValueUsed = new JTextField(9);
  JPanel jPanel10 = new JPanel();
  GridLayout gridLayout5 = new GridLayout();
  JLabel kLabel11 = new JLabel();
  JTextField maxValueUsed = new JTextField(9);
  GridLayout gridLayout6 = new GridLayout();
  JLabel kLabel14 = new JLabel();
  JPanel jPanel11 = new JPanel();
  BorderLayout borderLayout3 = new BorderLayout();
  Border border2;
  TitledBorder titledBorder2;
  JPanel ustawieniaPanel = new JPanel();
  Border border3;
  TitledBorder titledBorder3;
  ColumnLayout columnLayout2 = new ColumnLayout(1, ColumnLayout.Align.FILL, false);
  JPanel koloryPrzyciskiPanel = new JPanel();
  JButton kolorButtonTlo = new JButton();
  JButton kolorButtonSiatka = new JButton();
  JButton kolorButtonWartosci = new JButton();
  JButton kolorButtonTotal = new JButton();
  GridLayout gridLayout3 = new GridLayout();
  JButton kolorButtonUsed = new JButton();
  JPanel koloryPanel = new JPanel();
  BorderLayout borderLayout4 = new BorderLayout();
  JPanel jPanel4 = new JPanel();
  GridLayout gridLayout4 = new GridLayout();
  JLabel kLabel2 = new JLabel();
  JLabel kLabel4 = new JLabel();
  JLabel kLabel6 = new JLabel();
  JLabel kLabel9 = new JLabel();
  JLabel kLabel12 = new JLabel();

  MemoryMonitor(DBConnection connection)
  {
  	super();
  	
  	this.connection = connection;
  	if(connection != null && connection.isConnected())
			mode = Mode.SERVER;
		else
			mode = Mode.CLIENT;
  	
  	setTitle("Memory monitor (" + mode.getModeName() + ")");
  	
  	try
    {
      jbInit();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    border1 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(134, 134, 134));
    titledBorder1 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(134, 134, 134)),"total (bytes)");
    border2 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(134, 134, 134));
    titledBorder2 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(134, 134, 134)),"used (bytes)");
    border3 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(134, 134, 134));
    titledBorder3 = new TitledBorder(border3,"settings");
    panel1.setLayout(borderLayout2);
    zamknijButton.setText("close");
    zamknijButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        zamknijButton_actionPerformed(e);
      }
    });
    kLabel1.setText("minimum:");
    kLabel3.setText("current:");
    kLabel5.setText("maximum:");
    jPanel5.setLayout(gridLayout1);
    gridLayout1.setColumns(1);
    gridLayout1.setRows(3);
    jPanel3.setLayout(gridLayout2);
    gridLayout2.setColumns(1);
    gridLayout2.setRows(3);
    minValueTotal.setHorizontalAlignment(SwingConstants.TRAILING);
    minValueTotal.setEnabled(false);
    minValueTotal.setText("0");
    maxValueTotal.setText("0");
    maxValueTotal.setEnabled(false);
    maxValueTotal.setHorizontalAlignment(SwingConstants.TRAILING);
    curValueTotal.setText("0");
    curValueTotal.setEnabled(false);
    curValueTotal.setHorizontalAlignment(SwingConstants.TRAILING);
    totalPanel.setLayout(borderLayout1);
    jPanel6.setLayout(columnLayout1);
    borderLayout1.setHgap(5);
    jPanel7.setLayout(flowLayout1);
    flowLayout1.setAlignment(FlowLayout.RIGHT);
    kLabel7.setText("refresh rate:");
    kLabel8.setText("s");
    gcButton.setText("force GC");
    gcButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        gcButton_actionPerformed(e);
      }
    });
    plot.setBorder(BorderFactory.createLoweredBevelBorder());
    totalPanel.setBorder(titledBorder1);
    curValueUsed.setHorizontalAlignment(SwingConstants.TRAILING);
    curValueUsed.setEnabled(false);
    curValueUsed.setText("0");
    usedPanel.setLayout(borderLayout3);
    usedPanel.setBorder(titledBorder2);
    kLabel10.setText("current:");
    minValueUsed.setHorizontalAlignment(SwingConstants.TRAILING);
    minValueUsed.setEnabled(false);
    minValueUsed.setText("0");
    jPanel10.setLayout(gridLayout6);
    gridLayout5.setColumns(1);
    gridLayout5.setRows(3);
    kLabel11.setText("minimum:");
    maxValueUsed.setText("0");
    maxValueUsed.setEnabled(false);
    maxValueUsed.setHorizontalAlignment(SwingConstants.TRAILING);
    gridLayout6.setColumns(1);
    gridLayout6.setRows(3);
    kLabel14.setText("maximum:");
    jPanel11.setLayout(gridLayout5);
    borderLayout3.setHgap(5);
    ustawieniaPanel.setBorder(titledBorder3);
    ustawieniaPanel.setLayout(columnLayout2);
    koloryPrzyciskiPanel.setLayout(gridLayout3);
    gridLayout3.setColumns(1);
    gridLayout3.setRows(5);
    koloryPanel.setLayout(borderLayout4);
    jPanel4.setLayout(gridLayout4);
    gridLayout4.setColumns(1);
    gridLayout4.setRows(5);
    kLabel2.setText("background");
    kLabel4.setText("grid");
    kLabel6.setText("labels");
    kLabel9.setText("total");
    kLabel12.setText("used");
    kolorButtonTlo.setBackground(backgroundColor);
    kolorButtonTlo.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        kolorButtonTlo_actionPerformed(e);
      }
    });
    kolorButtonSiatka.setBackground(gridColor);
    kolorButtonSiatka.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        kolorButtonSiatka_actionPerformed(e);
      }
    });
    kolorButtonWartosci.setBackground(labelColor);
    kolorButtonWartosci.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        kolorButtonWartosci_actionPerformed(e);
      }
    });
    kolorButtonTotal.setBackground(totalColor);
    kolorButtonTotal.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        kolorButtonTotal_actionPerformed(e);
      }
    });
    kolorButtonUsed.setBackground(usedColor);
    kolorButtonUsed.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        kolorButtonUsed_actionPerformed(e);
      }
    });
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        this_windowClosing(e);
      }
    });
    koloryPrzyciskiPanel.add(kolorButtonTlo, null);
    koloryPrzyciskiPanel.add(kolorButtonSiatka, null);
    koloryPrzyciskiPanel.add(kolorButtonWartosci, null);
    koloryPrzyciskiPanel.add(kolorButtonTotal, null);
    koloryPrzyciskiPanel.add(kolorButtonUsed, null);
    koloryPanel.add(jPanel4, BorderLayout.WEST);
    ustawieniaPanel.add(koloryPanel, null);
    koloryPanel.add(koloryPrzyciskiPanel,  BorderLayout.CENTER);
    jPanel10.add(minValueUsed, null);
    jPanel10.add(maxValueUsed, null);
    jPanel10.add(curValueUsed, null);
    usedPanel.add(jPanel10, BorderLayout.CENTER);
    usedPanel.add(jPanel11, BorderLayout.WEST);
    jPanel11.add(kLabel11, null);
    jPanel11.add(kLabel14, null);
    jPanel11.add(kLabel10, null);
    getContentPane().add(panel1);
    panel1.add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(zamknijButton, null);
    panel1.add(jPanel6,  BorderLayout.WEST);
    jPanel5.add(kLabel1, null);
    jPanel5.add(kLabel5, null);
    jPanel5.add(kLabel3, null);
    totalPanel.add(jPanel3, BorderLayout.CENTER);
    jPanel7.add(kLabel7, null);
    jPanel7.add(okres, null);
    jPanel7.add(kLabel8, null);
    jPanel6.add(totalPanel, null);
    jPanel6.add(usedPanel, null);
    jPanel6.add(ustawieniaPanel, null);
    jPanel3.add(minValueTotal, null);
    jPanel3.add(maxValueTotal, null);
    jPanel3.add(curValueTotal, null);
    totalPanel.add(jPanel5, BorderLayout.WEST);
    jPanel6.add(jPanel8, null);
    jPanel8.add(gcButton, null);
    ustawieniaPanel.add(jPanel7, null);
    panel1.add(plot, BorderLayout.CENTER);
    jPanel4.add(kLabel2, null);
    jPanel4.add(kLabel4, null);
    jPanel4.add(kLabel6, null);
    jPanel4.add(kLabel9, null);
    jPanel4.add(kLabel12, null);
  }

  void zamknijButton_actionPerformed(ActionEvent e)
  {
    if(JOptionPane.showConfirmDialog(this, "Close the monitor?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
    {
      close();
    }
  }
  
  /**
   * Closes the monitor the server connection (if any).
   *
   */
  void close()
  {
  	running = false;
    
  	if(closeConnection) {
      	if(connection != null && connection.isConnected())
      	{
  				try
  				{
  					connection.close();
  				}
  				catch(IOException exc)
  				{
  					exc.printStackTrace();
  				}
      	}
  	}      
  	
    dispose();
  }

  private String formatLong(long l)
  {
    String lt = Long.toString(l);
    String text = "";

    int i = lt.length();
    while(true)
    {
      try
      {
        text = lt.substring(i - 3, i) + " " + text;
        i -= 3;
      }
      catch(StringIndexOutOfBoundsException exc)
      {
        text = lt.substring(0, i) + " " + text;
        break;
      }
    }

    return text.trim();
  }

  private class Plotter implements Runnable
  {
    public void run()
    {
      while(running)
      {
      	long curTotal = 0;
      	long curFree = 0;
      	if(mode == Mode.CLIENT)
      	{
      		curTotal = runtime.totalMemory();
      		curFree = runtime.freeMemory();
      	}
      	else
      	{
      		try
      		{
	      		DBRequest request = new DBRequest(DBRequest.MEMORY_GET_RQST, new String[] {});
	      		DBReply reply = connection.sendRequest(request);
	      		BagResult result = (BagResult)reply.getResult();
	      		
	      		curTotal = ((IntegerResult)result.elementAt(0)).value;
	      		curFree = ((IntegerResult)result.elementAt(1)).value;
      		}
      		catch(Exception exc)
      		{
      			if(connection.isConnected())
      				exc.printStackTrace();
      			
      			running = false;
      			break;
      		}
      	}
      	
        minTotal = Math.min(minTotal, curTotal);
        maxTotal = Math.max(maxTotal, curTotal);
        curValueTotal.setText(formatLong(curTotal));
        minValueTotal.setText(formatLong(minTotal));
        maxValueTotal.setText(formatLong(maxTotal));

        long curUsed = curTotal - curFree;
        minUsed = Math.min(minUsed, curUsed);
        maxUsed = Math.max(maxUsed, curUsed);
        curValueUsed.setText(formatLong(curUsed));
        minValueUsed.setText(formatLong(minUsed));
        maxValueUsed.setText(formatLong(maxUsed));

        try
        {
          plot.plotValue(curTotal, curUsed);
        }
        catch(NullPointerException exc) {}

        try
        {
          Thread.sleep(Math.round((Double)okres.getSelectedItem() * 1000));
        }
        catch(InterruptedException exc)  {}
      }
    }
  }

  void gcButton_actionPerformed(ActionEvent e)
  {
  	if(mode == Mode.CLIENT)
  		runtime.gc();
  	else
  	{
  		try
  		{
  			running = false;
  			
  			DBReply reply = connection.sendRequest(new DBRequest(DBRequest.MEMORY_GC_RQST, new String[] {}));
  			
  			start();
  		}
  		catch(Exception exc)
  		{
  			exc.printStackTrace();
  		}
  	}
  }
  
  private void start()
  {
  	running = true;
    new Thread(new Plotter()).start();
  }

  private class PlotPanel extends JPanel
  {
    Vector<long[]> pointsTotal = new Vector<long[]>();
    Vector<long[]> pointsUsed = new Vector<long[]>();

    int leftMargin = 50;
    int offset = leftMargin;
    long lastMax = Long.MIN_VALUE;

    public void paint(Graphics g)
    {
      super.paint(g);

      this.setBackground(backgroundColor);

      drawScale(g);

      for(int i = 1; i < pointsTotal.size(); i++)
      {
        try
        {
          int h = getHeight();

          g.setColor(totalColor);
          long[] p1 = pointsTotal.get(i - 1);
          long[] p2 = pointsTotal.get(i);
          g.drawLine((int)p1[0], h - normalize(p1[1]), (int)p2[0], h - normalize(p2[1]));

          g.setColor(usedColor);
          p1 = pointsUsed.get(i - 1);
          p2 = pointsUsed.get(i);
          g.drawLine((int)p1[0], h - normalize(p1[1]), (int)p2[0], h - normalize(p2[1]));
        }
        catch(ArrayIndexOutOfBoundsException exc)
        {}
      }
    }

    void drawScale(Graphics g)
    {
      if(lastMax < 0)
        return;

      double log10 = Math.log(10);

      int h = getHeight();
      int w = getWidth();
      long roundMax = lastMax;

      double row = Math.floor(Math.log(roundMax)/log10);
      roundMax = roundMax - roundMax % (long)Math.pow(10, row);

      long scaleSpan = roundMax / 10;
      int span = normalize(roundMax / 10);

      if(span == 0)
        return;

      leftMargin = g.getFontMetrics().stringWidth(formatLong(roundMax));
      g.setColor(gridColor);
      g.drawLine(leftMargin, 0, leftMargin, h);

      int yOffset = h;
      int scaleOffset = 0;
      while(yOffset >= 0)
      {
        g.setColor(gridColor);
        g.drawLine(leftMargin, yOffset, w, yOffset);
        g.setColor(labelColor);
        String label = formatLong(scaleOffset);
        g.drawString(label, leftMargin - g.getFontMetrics().stringWidth(label), yOffset);
        yOffset -= span;
        scaleOffset += scaleSpan;
      }
    }

    int normalize(long value)
    {
      return (int)(getHeight() * value / lastMax);
    }

    long maxPlotValue(Vector points)
    {
      long max = Long.MIN_VALUE;

      for(int i = 0; i < points.size(); i++)
        max = Math.max(max, ((long[])points.get(i))[1]);

      return max;
    }

    void plotValue(long total, long used)
    {
      lastMax = maxPlotValue(pointsTotal);

      int height = getHeight();
      int yTotal = normalize(total);
      int yUsed = normalize(used);

      Graphics2D g2 = (Graphics2D)getGraphics();
      long[] prevTotal;
      long[] prevUsed;
      try
      {
        prevTotal = pointsTotal.lastElement();
        prevUsed = pointsUsed.lastElement();
      }
      catch(NoSuchElementException exc)
      {
        prevTotal = new long[] {0, height};
        prevUsed = new long[] {0, height};
      }

      if(offset >= getWidth())
      {
        try
        {
          pointsTotal.removeElementAt(0);
          pointsUsed.removeElementAt(0);
          offset--;
          for(int i = 0; i < pointsTotal.size(); i++)
          {
            pointsTotal.get(i)[0]--;
            pointsUsed.get(i)[0]--;
          }
          repaint();
        }
        catch(ArrayIndexOutOfBoundsException exc)
        {}
      }

      double scale = 1;
        scale = total / maxTotal;
      if(scale < 1)
        scale = 1;
      if(scale != 1)
      {
        for(int i = 0; i < pointsTotal.size(); i++)
        {
          pointsTotal.get(i)[1] /= scale;
          pointsUsed.get(i)[1] /= scale;
        }
      }
      repaint();
      yTotal /= scale;
      yUsed /= scale;

      g2.setColor(totalColor);
      g2.drawLine(offset, height - normalize(prevTotal[1]), offset + 1, height - yTotal);
      g2.setColor(usedColor);
      g2.drawLine(offset, height - normalize(prevUsed[1]), offset + 1, height - yUsed);
      offset++;
      pointsTotal.addElement(new long[] {offset, total});
      pointsUsed.addElement(new long[] {offset, used});
    }
  }

  void kolorButtonTlo_actionPerformed(ActionEvent e)
  {
    Color c = JColorChooser.showDialog(kolorButtonTlo, "Choose a colour", kolorButtonTlo.getBackground());
    if(c != null)
    {
      kolorButtonTlo.setBackground(c);
      backgroundColor = c;
      plot.repaint();
    }
  }

  void kolorButtonSiatka_actionPerformed(ActionEvent e)
  {
    Color c = JColorChooser.showDialog(kolorButtonSiatka, "Choose a colour", kolorButtonSiatka.getBackground());
    if(c != null)
    {
      kolorButtonSiatka.setBackground(c);
      gridColor = c;
      plot.repaint();
    }
  }

  void kolorButtonWartosci_actionPerformed(ActionEvent e)
  {
    Color c = JColorChooser.showDialog(kolorButtonWartosci, "Choose a colour", kolorButtonWartosci.getBackground());
    if(c != null)
    {
      kolorButtonWartosci.setBackground(c);
      labelColor = c;
      plot.repaint();
    }
  }

  void kolorButtonTotal_actionPerformed(ActionEvent e)
  {
    Color c = JColorChooser.showDialog(kolorButtonTotal, "Choose a colour", kolorButtonTotal.getBackground());
    if(c != null)
    {
      kolorButtonTotal.setBackground(c);
      totalColor = c;
      plot.repaint();
    }
  }

  void kolorButtonUsed_actionPerformed(ActionEvent e)
  {
    Color c = JColorChooser.showDialog(kolorButtonUsed, "Choose a colour", kolorButtonUsed.getBackground());
    if(c != null)
    {
      kolorButtonUsed.setBackground(c);
      usedColor = c;
      plot.repaint();
    }
  }

  @Override
	public void setVisible(boolean b)
	{
  	if(b)
  	{
	    pack();
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    setSize(screenSize.width, getHeight());
	    Dimension frameSize = getSize();
	    if(frameSize.height > screenSize.height)
	      frameSize.height = screenSize.height;
	    if(frameSize.width > screenSize.width)
	      frameSize.width = screenSize.width;
	    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
	
	    start();
  	}
  	else
  		running = false;
    
    super.setVisible(b);
    this.toFront();
  }

  void this_windowClosing(WindowEvent e)
  {
    zamknijButton_actionPerformed(null);
  }
  
  public String getMode()
  {
  	return mode.getModeName();
  }
  
  /**
   * If true then the connection will be closed together with the monitor. 
   * @param closeConnection
   */
  protected void setCloseConnection(boolean closeConnection) {
	  this.closeConnection = closeConnection;
  }
  
}
