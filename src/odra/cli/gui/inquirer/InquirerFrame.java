package odra.cli.gui.inquirer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import odra.cli.gui.components.CLIFrame;
import odra.cli.gui.components.layout.ColumnLayout;
import odra.cli.gui.components.result.ResultPanel;
import odra.sbql.results.runtime.RawResultPrinter;
import odra.sbql.results.runtime.Result;

/**
 * Inquirer frame.
 *
 * @author jacenty
 * @version 2007-06-08
 * @since 2006-12-24
 */
public class InquirerFrame extends CLIFrame
{
	/** editable (default: <code>false</code>) */
	private boolean editable = false;
	/** controller */
	private InquirerController controller;
	/** result printer */
	private RawResultPrinter resultPrinter = new RawResultPrinter();  //  @jve:decl-index=0:
	
	private JPanel contentPane = null;
	private JPanel southPanel = null;
	private JPanel jPanel = null;
	private JButton executeButton = null;
	private JTextField queryTextField = null;
	private JPanel jPanel1 = null;
	private ResultPanel resultPanel = null;
	private JScrollPane scrollPane = null;
	
	/**
	 * The constructor.
	 * 
	 * @param editable is editable?
	 * @param controller controller
	 */
	public InquirerFrame(boolean editable, InquirerController controller)
	{
		super();
		
		this.editable = editable;
		this.controller = controller;
		
		initialize();
	}
	
	/**
	 * The constructor.
	 * 
	 * @param controller controller
	 */
	public InquirerFrame(InquirerController controller)
	{
		super();
		
		this.controller = controller;
		
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize()
	{
		this.setSize(800, 600);
		this.setTitle("Inquirer");
		this.setLocation(400, 300);
		this.setContentPane(getContentPane());
	}

	@Override
	public JPanel getContentPane()
	{
		if(contentPane == null)
		{
			contentPane = new JPanel();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(getSouthPanel(), BorderLayout.SOUTH);
			contentPane.add(getScrollPane(), BorderLayout.CENTER);
			
			southPanel.setVisible(editable);
		}
		return contentPane;
	}

	/**
	 * This method initializes southPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSouthPanel()
	{
		if(southPanel == null)
		{
			southPanel = new JPanel();
			southPanel.setLayout(new BorderLayout());
			southPanel.add(getJPanel(), BorderLayout.EAST);
			southPanel.add(getJPanel1(), BorderLayout.CENTER);
		}
		return southPanel;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel()
	{
		if(jPanel == null)
		{
			jPanel = new JPanel();
			jPanel.setLayout(new ColumnLayout(0, ColumnLayout.Align.FILL, false));
			jPanel.add(getExecuteButton(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes executeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getExecuteButton()
	{
		if(executeButton == null)
		{
			executeButton = new JButton();
			executeButton.setText("execute");
			executeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					executeButtonActionPerformed(e);
				}
			});
		}
		return executeButton;
	}
	
	private void executeButtonActionPerformed(ActionEvent e)
	{
		try
		{
			controller.executeQuery(queryTextField.getText(), controller.getCLI().getCurrMod());
			
			scrollPane.getVerticalScrollBar().setValue(0);
			scrollPane.getHorizontalScrollBar().setValue(0);
		}
		catch (Exception exc)
		{
			JOptionPane.showMessageDialog(this, exc, "Error during query evaluation", JOptionPane.ERROR_MESSAGE);
			
			JOptionPane optionPane = new JOptionPane();
		}
	}

	/**
	 * This method initializes queryTextArea	
	 * 	
	 * @return {@link javax.swing.JTextField}	
	 */
	private JTextField getQueryTextField()
	{
		if(queryTextField == null)
		{
			queryTextField = new JTextField();
			queryTextField.addKeyListener(new KeyAdapter()
			{
				public void keyPressed(KeyEvent e)
				{
					queryTextFieldKeyPressed(e);
				}
			});
		}
		return queryTextField;
	}
	
	private void queryTextFieldKeyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
			executeButtonActionPerformed(null);
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1()
	{
		if(jPanel1 == null)
		{
			jPanel1 = new JPanel();
			jPanel1.setLayout(new ColumnLayout(0, ColumnLayout.Align.FILL, false));
			jPanel1.add(getQueryTextField(), null);
		}
		return jPanel1;
	}

	/**
	 * Sets the current result.
	 * 
	 * @param result result
	 */
	void setResult(Result result)
	{
		resultPanel.setResult(result);
	}

	/**
	 * This method initializes resultPanel	
	 * 	
	 * @return odra.cli.gui.components.ResultPanel	
	 */
	private ResultPanel getResultPanel()
	{
		if(resultPanel == null)
		{
			resultPanel = new ResultPanel();
		}
		return resultPanel;
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
			scrollPane.setViewportView(getResultPanel());
		}
		return scrollPane;
	}
	
	/**
	 * Sets the frame editable.
	 * 
	 * @param editable editable?
	 */
	void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	@Override
	public void setVisible(boolean b)
	{
		southPanel.setVisible(editable);
		
		super.setVisible(b);
		this.toFront();
	}
}
