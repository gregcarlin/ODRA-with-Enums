package odra.cli.gui.components.storage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import odra.cli.gui.components.layout.ColumnLayout;
import odra.cli.gui.components.layout.RowLayout;
import odra.exceptions.rd.RDException;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.IntegerResult;

/**
 * Heap structure presentation panel.
 *
 * @author jacenty
 * @version 2007-01-05
 * @since 2006-12-31
 */
public class HeapPanel extends JPanel
{
	/** byte fragment length for requests */
	private final int fragmentSize;
	/** run flag */
	private volatile boolean run = true;
	/** shutdown flag */
	private volatile boolean shutdown = false;
	
	/** database connection */
	private final DBConnection connection;
	/** display mode */
	private final Mode mode;
	/** heap length (no. of bytes) */
	private final int length;
	/** current byte index */
	private int index = 0;
	/** downloader */
	private Downloader downloader = new Downloader(this);  //  @jve:decl-index=0:

	private JPanel legendPanel = null;
	private JScrollPane scrollPane = null;
	private HeapStructurePanel heapStructurePanel = null;
	private JPanel freePanel = null;
	private JPanel occupiedPanel = null;	
	private JPanel northPanel = null;
	private JPanel modePanel = null;
	private JComboBox modeComboBox = null;
	private JLabel modeLabel = null;
	private JPanel statusPanel = null;
	private JLabel lengthLabel = null;
	private JLabel bytesTextLabel = null;
	private JProgressBar progressBar = null;
	private JPanel burronsPanel = null;
	private JButton pauseButton = null;
	private JButton resumeButton = null;
	private JButton refreshButton = null;
	private JLabel downloadedTextLabel = null;
	private JLabel totalTextLabel = null;
	
	/**
	 * The constructor. 
	 *
	 * @param connection database connetction
	 * @param mode display mode
	 * @param persistent persistent store? (else: transient)
	 * @param byte fragment length for requests
	 * @throws RDException 
	 * @throws IOException 
	 */
	public HeapPanel(DBConnection connection, Mode mode, boolean persistent, int fragmenSize) throws IOException, RDException 
	{
		super();
		
		this.connection = connection;
		this.mode = mode;
		this.fragmentSize = fragmenSize;
		
		DBReply reply = connection.sendRequest(new DBRequest(DBRequest.HEAP_STRUCTURE_INIT_RQST, new String[] {new Boolean(persistent).toString()}));
		length = ((IntegerResult)reply.getResult()).value;
		
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() 
	{
	    this.setLayout(new BorderLayout());
	    this.setSize(new Dimension(800, 600));
	    this.add(getScrollPane(), BorderLayout.CENTER);
	    this.add(getNorthPanel(), BorderLayout.NORTH);
	    this.add(getStatusPanel(), BorderLayout.SOUTH);
	}

	/**
	 * This method initializes legendPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getLegendPanel()
	{
		if(legendPanel == null)
		{
			legendPanel = new JPanel();
			legendPanel.setLayout(new RowLayout(1, RowLayout.Align.MIDDLE, false));
			legendPanel.add(new LegendPanel(getHeapStructurePanel(), getHeapStructurePanel().getUndefinedColor(), "unknown"));
			legendPanel.add(new LegendPanel(getHeapStructurePanel(), getHeapStructurePanel().getFileHeaderColor(), "file header"));
			legendPanel.add(new LegendPanel(getHeapStructurePanel(), getHeapStructurePanel().getDbRootColor(), "database root"));
			legendPanel.add(getFreePanel(), null);
			legendPanel.add(getOccupiedPanel(), null);
			getFreePanel().add(new LegendPanel(getHeapStructurePanel(), getHeapStructurePanel().getFreeHeaderColor(), "free header"));
			getFreePanel().add(new LegendPanel(getHeapStructurePanel(), getHeapStructurePanel().getFreeDataColor(), "free data"));
			getOccupiedPanel().add(new LegendPanel(getHeapStructurePanel(), getHeapStructurePanel().getOccupiedHeaderColor(), "occupied header"));
			getOccupiedPanel().add(new LegendPanel(getHeapStructurePanel(), getHeapStructurePanel().getOccupiedDataColor(), "occupied data"));
		}
		return legendPanel;
	}

	/**
	 * This method initializes scrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrollPane()
	{
		if(scrollPane == null)
		{
			scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setViewportView(getHeapStructurePanel());
			
			scrollPane.getVerticalScrollBar().setBlockIncrement(getHeapStructurePanel().getCurrentColumns() *
					(getHeapStructurePanel().getBlockHeight() + getHeapStructurePanel().getBlockSpace()));
			scrollPane.getVerticalScrollBar().setUnitIncrement(getHeapStructurePanel().getBlockHeight() + getHeapStructurePanel().getBlockSpace());
			
			scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
			{
				public void adjustmentValueChanged(AdjustmentEvent e)
				{
					vertcalScrollBarValueChanged(e);
				}
			});
		}
		return scrollPane;
	}
	
	void vertcalScrollBarValueChanged(AdjustmentEvent e)
	{		
		scrollPane.getVerticalScrollBar().setBlockIncrement(getHeapStructurePanel().getCurrentColumns() *
				(getHeapStructurePanel().getBlockHeight() + getHeapStructurePanel().getBlockSpace()));
		
		if(!e.getValueIsAdjusting())
			scrollPane.getViewport().repaint();
	}

	/**
	 * This method initializes heapStructurePanel	
	 * 	
	 * @return odra.cli.gui.components.storage.HeapStructurePanel	
	 */
	private HeapStructurePanel getHeapStructurePanel()
	{
		if(heapStructurePanel == null)
		{
			heapStructurePanel = new HeapStructurePanel(length, mode);
		}
		return heapStructurePanel;
	}

	/**
	 * This method initializes freePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getFreePanel()
	{
		if(freePanel == null)
		{
			freePanel = new JPanel();
			freePanel.setLayout(new ColumnLayout(1, ColumnLayout.Align.LEFT, false));
		}
		return freePanel;
	}

	/**
	 * This method initializes occupiedPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOccupiedPanel()
	{
		if(occupiedPanel == null)
		{
			occupiedPanel = new JPanel();
			occupiedPanel.setLayout(new ColumnLayout(1, ColumnLayout.Align.LEFT, false));
		}
		return occupiedPanel;
	}

	/**
	 * This method initializes northPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getNorthPanel()
	{
		if(northPanel == null)
		{
			northPanel = new JPanel();
			northPanel.setLayout(new BorderLayout());
			northPanel.add(getLegendPanel(), BorderLayout.WEST);
			northPanel.add(getModePanel(), BorderLayout.CENTER);
		}
		return northPanel;
	}

	/**
	 * This method initializes modePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getModePanel()
	{
		if(modePanel == null)
		{
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
			flowLayout.setVgap(1);
			modeLabel = new JLabel();
			modeLabel.setText("display mode");
			modePanel = new JPanel();
			modePanel.setLayout(flowLayout);
			modePanel.add(modeLabel, null);
			modePanel.add(getModeComboBox(), null);
		}
		return modePanel;
	}

	/**
	 * This method initializes modeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getModeComboBox()
	{
		if(modeComboBox == null)
		{
			modeComboBox = new JComboBox(Mode.values());
			modeComboBox.setSelectedItem(heapStructurePanel.getMode());
			modeComboBox.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					modeComboBoxItemStateChanged(e);
				}
			});
		}
		return modeComboBox;
	}
	
	void modeComboBoxItemStateChanged(ItemEvent e)
	{
		if(e.getStateChange() == ItemEvent.SELECTED)
			heapStructurePanel.setMode((Mode)e.getItem());
	}

	/**
	 * This method initializes statusPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getStatusPanel()
	{
		if(statusPanel == null)
		{
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
			statusPanel = new JPanel();
			statusPanel.setLayout(flowLayout);
			bytesTextLabel = new JLabel();
			bytesTextLabel.setText("bytes");
			lengthLabel = new JLabel();
			lengthLabel.setText(Integer.toString(this.length));
			totalTextLabel = new JLabel();
			totalTextLabel.setText("of total");
			downloadedTextLabel = new JLabel();
			downloadedTextLabel.setText("downloaded:");
			statusPanel.add(downloadedTextLabel, null);
			statusPanel.add(getProgressBar(), null);
			statusPanel.add(totalTextLabel, null);
			statusPanel.add(lengthLabel, null);
			statusPanel.add(bytesTextLabel, null);
			statusPanel.add(getBurronsPanel(), null);
		}
		return statusPanel;
	}

	/**
	 * This method initializes progressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getProgressBar()
	{
		if(progressBar == null)
		{
			progressBar = new JProgressBar(0, length);
			progressBar.setStringPainted(true);
		}
		return progressBar;
	}

	/**
	 * This method initializes burronsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getBurronsPanel()
	{
		if(burronsPanel == null)
		{
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setHgap(5);
			gridLayout.setColumns(3);
			burronsPanel = new JPanel();
			burronsPanel.setLayout(gridLayout);
			burronsPanel.add(getPauseButton(), null);
			burronsPanel.add(getResumeButton(), null);
			burronsPanel.add(getRefreshButton(), null);
		}
		return burronsPanel;
	}

	/**
	 * This method initializes pauseButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getPauseButton()
	{
		if(pauseButton == null)
		{
			pauseButton = new JButton();
			pauseButton.setText("pause");
			pauseButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					pauseButtonActionPerformed(e);
				}
			});
		}
		return pauseButton;
	}
	
	void pauseButtonActionPerformed(@SuppressWarnings("unused") ActionEvent e)
	{
		getPauseButton().setEnabled(false);
		getResumeButton().setEnabled(true);
		
		run = false;
	}

	/**
	 * This method initializes resumeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getResumeButton()
	{
		if(resumeButton == null)
		{
			resumeButton = new JButton();
			resumeButton.setText("resume");
			resumeButton.setEnabled(false);
			resumeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					resumeButtonActionPerformed(e);
				}
			});
		}
		return resumeButton;
	}
	
	void resumeButtonActionPerformed(@SuppressWarnings("unused") ActionEvent e)
	{
		getResumeButton().setEnabled(false);
		getPauseButton().setEnabled(true);
		
		run = true;
		downloader.go();
	}

	/**
	 * This method initializes refreshButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRefreshButton()
	{
		if(refreshButton == null)
		{
			refreshButton = new JButton();
			refreshButton.setText("refresh");
			refreshButton.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					refreshButtonActionPerformed(e);
				}
			});
		}
		return refreshButton;
	}
	
	void refreshButtonActionPerformed(@SuppressWarnings("unused") ActionEvent e)
	{
		getHeapStructurePanel().repaint();
	}
	
	/**
	 * Starts heap structure download.
	 */
	public void startDownload()
	{
		index = 0;
		downloader.go();
	}
	
	/**
	 * Receives the next fragment of heap bytes.
	 */
	void getNextFragment()
	{
		Exception exception = null;
		DBReply reply;
		while(index < length && run)
		{
			try
			{
				int fragmentLength = fragmentSize;
				if(index + fragmentLength > length)
					fragmentLength = length - index;

				reply = connection.sendRequest(new DBRequest(DBRequest.HEAP_STRUCTURE_FRAGMENT_DATA_RQST, new String[] {Integer.toString(index), Integer.toString(fragmentLength)}));
				heapStructurePanel.setBytes(index, reply.getRawResult());

				reply = connection.sendRequest(new DBRequest(DBRequest.HEAP_STRUCTURE_FRAGMENT_TYPES_RQST, new String[] {Integer.toString(index), Integer.toString(fragmentLength)}));
				heapStructurePanel.setTypes(index, reply.getRawResult());
				
				index += fragmentLength;
				progressBar.setValue(index);
			}
			catch(IOException exc)
			{
				exception = exc;
				break;
			}
			catch(RDException exc)
			{
				exception = exc;
				break;
			}
		}
		
		if(exception != null)
		{
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(true);
			
			exception.printStackTrace();
			JOptionPane.showMessageDialog(this, exception, "Error while retrieving data", JOptionPane.ERROR_MESSAGE);
		}
		else if(!shutdown)//no shutdown was forced
		{
			if(run)//naturally finished
			{
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(false);
				
				int choice = JOptionPane.showConfirmDialog(
					this, 
					"Data completely retrieved. Do you want to refresh the diagram now?\nIf not, you can press the 'refresh' button manually.", 
					"Finished", 
					JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION)
					heapStructurePanel.repaint();
			}
			else//paused
			{
				JOptionPane.showMessageDialog(
					this, 
					"Data transfer has been paused. In order to refresh, press the 'refresh' button.\nIf you want to proceed with retrieval, press 'resume'.", 
					"Paused", 
					JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/**
	 * Stops smootly the download thread.
	 */
	public void stop()
	{
		shutdown = true;
		run = false;
	}
	
	/**
	 * Frees the memory.
	 */
	public void cleanup()
	{
		heapStructurePanel.cleanup();
		System.gc();
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
