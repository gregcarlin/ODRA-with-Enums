package odra.cli.gui.ast;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import odra.cli.gui.components.CLIFrame;
import odra.cli.gui.components.ast.ASTVisualizerPanel;
import odra.cli.gui.components.layout.ColumnLayout;
import odra.cli.gui.opt.ASTVisualizerOptSequenceFrame;
import odra.sbql.ast.ASTNode;
import odra.system.config.ConfigDebug;

/**
 * AST visualizer frame.
 *
 * @author jacenty
 * @version 2007-11-02
 * @since 2007-06-08
 */
public class ASTVisualizerFrame extends CLIFrame
{
	/** controller */
	private ASTVisualizerController controller;
	/** optimization sequence frame */
	private ASTVisualizerOptSequenceFrame optSequenceFrame;
	
	private JPanel contentPane = null;
	private JPanel southPanel = null;
	private JPanel jPanel = null;
	private JButton visualizeButton = null;
	private JTextField queryTextField = null;
	private JPanel jPanel1 = null;
	private ASTVisualizerPanel typecheckedAstPanel = null;
	private JTabbedPane tabbedPane = null;
	private ASTVisualizerPanel parsedAstPanel = null;
	private ASTVisualizerPanel optimizedAstPanel = null;
	private JPanel optionsPanel = null;
	private JCheckBox parsedCheckBox = null;
	private JCheckBox typecheckedCheckBox = null;
	private JCheckBox optimizedCheckBox = null;
	private JCheckBox decoratedCheckBox = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JButton optimizationButton = null;
	private JPanel showPanel = null;
	private JPanel additionalPanel = null;
	private JCheckBox bindingLevelsCheckBox = null;
	private JPanel viewPanel = null;
	private JSpinner fontSpinner = null;
	private JLabel jLabel3 = null;

	/**
	 * The constructor.
	 * 
	 * @param controller
	 *          controller
	 */
	public ASTVisualizerFrame(ASTVisualizerController controller)
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
		this.setTitle("AST visualizer");
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
			contentPane.add(getTabbedPane(), BorderLayout.CENTER);
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
			southPanel.add(getOptionsPanel(), BorderLayout.NORTH);
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
			jPanel.add(getVisualizeButton(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes visualizeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getVisualizeButton()
	{
		if(visualizeButton == null)
		{
			visualizeButton = new JButton();
			visualizeButton.setText("visualize");
			visualizeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					visualizeButtonActionPerformed(e);
				}
			});
		}
		return visualizeButton;
	}
	
	void visualizeButtonActionPerformed(@SuppressWarnings("unused") ActionEvent e)
	{
		if(!(parsedCheckBox.isSelected() || typecheckedCheckBox.isSelected() || optimizedCheckBox.isSelected()))
		{
			JOptionPane.showMessageDialog(this, "Choose at least one visualization option!", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		try
		{
			String query = queryTextField.getText();
			
			parsedAstPanel.setDecorated(decoratedCheckBox.isSelected());
			optimizedAstPanel.setDecorated(decoratedCheckBox.isSelected());
			typecheckedAstPanel.setDecorated(decoratedCheckBox.isSelected());
			
			parsedAstPanel.setBindingLevel(bindingLevelsCheckBox.isSelected());
			optimizedAstPanel.setBindingLevel(bindingLevelsCheckBox.isSelected());
			typecheckedAstPanel.setBindingLevel(bindingLevelsCheckBox.isSelected());
			
			if(parsedCheckBox.isSelected())
				controller.parseQuery(query);
			else
				parsedAstPanel.setQuery(null, (Integer)getFontSpinner().getValue());
				
			if(typecheckedCheckBox.isSelected())
				controller.typecheckQuery(query);
			else
				typecheckedAstPanel.setQuery(null, (Integer)getFontSpinner().getValue());
			
			if(optimizedCheckBox.isSelected())
				controller.optimizeQuery(query, getOptSequenceFrame().getCurrentSequence());
			else
				optimizedAstPanel.setQuery(null, (Integer)getFontSpinner().getValue());
		}
		catch (Exception exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			
			JOptionPane.showMessageDialog(this, exc, "Error during query processing", JOptionPane.ERROR_MESSAGE);
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
				@Override
				public void keyPressed(KeyEvent e)
				{
					queryTextFieldKeyPressed(e);
				}
			});
		}
		return queryTextField;
	}
	
	void queryTextFieldKeyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
			visualizeButtonActionPerformed(null);
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
	 * Sets the current parsed query.
	 * 
	 * @param query query to visualize
	 * @throws Exception 
	 */
	void setParsedQuery(ASTNode query) throws Exception
	{
		parsedAstPanel.setQuery(query, (Integer)getFontSpinner().getValue());
	}

	/**
	 * Sets the current typechecked query.
	 * 
	 * @param query query to visualize
	 * @throws Exception 
	 */
	void setTypecheckedQuery(ASTNode query) throws Exception
	{
		typecheckedAstPanel.setQuery(query, (Integer)getFontSpinner().getValue());
	}
	
	/**
	 * Sets the current optimized query.
	 * 
	 * @param query query to visualize
	 * @throws Exception 
	 */
	void setOptimizedQuery(ASTNode query) throws Exception
	{
		optimizedAstPanel.setQuery(query, (Integer)getFontSpinner().getValue());
	}

	/**
	 * This method initializes typecheckedAstPanel	
	 * 	
	 * @return {@link ASTVisualizerPanel}
	 */
	private ASTVisualizerPanel getTypecheckedAstPanel()
	{
		if(typecheckedAstPanel == null)
		{
			typecheckedAstPanel = new ASTVisualizerPanel();
		}
		return typecheckedAstPanel;
	}

	@Override
	public void setVisible(boolean b)
	{
		super.setVisible(b);
		
		queryTextField.grabFocus();
		
		this.toFront();
	}

	/**
	 * This method initializes tabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbedPane()
	{
		if(tabbedPane == null)
		{
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab("raw (parsed)", null, getParsedAstPanel(), null);
			tabbedPane.addTab("typechecked", null, getTypecheckedAstPanel(), null);
			tabbedPane.addTab("optimized", null, getOptimizedAstPanel(), null);
			tabbedPane.setSelectedComponent(getParsedAstPanel());
		}
		return tabbedPane;
	}

	/**
	 * This method initializes parsedAstPanel	
	 * 	
	 * @return odra.cli.gui.components.ast.ASTVisualizerPanel	
	 */
	private ASTVisualizerPanel getParsedAstPanel()
	{
		if(parsedAstPanel == null)
		{
			parsedAstPanel = new ASTVisualizerPanel();
		}
		return parsedAstPanel;
	}

	/**
	 * This method initializes optimizedAstPanel	
	 * 	
	 * @return odra.cli.gui.components.ast.ASTVisualizerPanel	
	 */
	private ASTVisualizerPanel getOptimizedAstPanel()
	{
		if(optimizedAstPanel == null)
		{
			optimizedAstPanel = new ASTVisualizerPanel();
		}
		return optimizedAstPanel;
	}

	/**
	 * This method initializes optionsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOptionsPanel()
	{
		if(optionsPanel == null)
		{
			jLabel1 = new JLabel();
			jLabel1.setText("additional options:");
			jLabel = new JLabel();
			jLabel.setText("show:");
			optionsPanel = new JPanel();
			optionsPanel.setLayout(new BorderLayout());
			optionsPanel.add(getShowPanel(), BorderLayout.WEST);
			optionsPanel.add(getAdditionalPanel(), BorderLayout.EAST);
			optionsPanel.add(getViewPanel(), BorderLayout.CENTER);
		}
		return optionsPanel;
	}

	/**
	 * This method initializes parsedCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getParsedCheckBox()
	{
		if(parsedCheckBox == null)
		{
			parsedCheckBox = new JCheckBox();
			parsedCheckBox.setText("raw (parsed)");
			parsedCheckBox.setSelected(true);
		}
		return parsedCheckBox;
	}

	/**
	 * This method initializes typecheckedCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getTypecheckedCheckBox()
	{
		if(typecheckedCheckBox == null)
		{
			typecheckedCheckBox = new JCheckBox();
			typecheckedCheckBox.setText("typechecked");
			typecheckedCheckBox.setSelected(true);
		}
		return typecheckedCheckBox;
	}

	/**
	 * This method initializes optimizedCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getOptimizedCheckBox()
	{
		if(optimizedCheckBox == null)
		{
			optimizedCheckBox = new JCheckBox();
			optimizedCheckBox.setText("optimized");
			optimizedCheckBox.setSelected(true);
		}
		return optimizedCheckBox;
	}
	
	/**
	 * This method initializes decoratedCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getDecoratedCheckBox() {
		if (decoratedCheckBox == null) {
			decoratedCheckBox = new JCheckBox();
			decoratedCheckBox.setActionCommand("decorated");
			decoratedCheckBox.setText("link decorated");
		}
		return decoratedCheckBox;
	}

	/**
	 * This method initializes optimizationButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOptimizationButton()
	{
		if(optimizationButton == null)
		{
			optimizationButton = new JButton();
			optimizationButton.setText("...");
			optimizationButton.setMargin(new Insets(0, 0, 0, 0));
			optimizationButton.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					optimizationButtonActionPerformed(e);
				}
			});
		}
		return optimizationButton;
	}
	
	private ASTVisualizerOptSequenceFrame getOptSequenceFrame()
	{
		if(optSequenceFrame == null)
		{
			optSequenceFrame = new ASTVisualizerOptSequenceFrame(controller.getCLI());
		}
		
		return optSequenceFrame;
	}

	void optimizationButtonActionPerformed(@SuppressWarnings("unused") ActionEvent e)
	{
		getOptSequenceFrame().setVisible(true);
	}

	/**
	 * This method initializes showPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getShowPanel()
	{
		if(showPanel == null)
		{
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setVgap(2);
			showPanel = new JPanel();
			showPanel.setLayout(flowLayout);
			showPanel.add(jLabel, null);
			showPanel.add(getParsedCheckBox(), null);
			showPanel.add(getTypecheckedCheckBox(), null);
			showPanel.add(getOptimizedCheckBox(), null);
			showPanel.add(getOptimizationButton(), null);
		}
		return showPanel;
	}

	/**
	 * This method initializes additionalPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getAdditionalPanel()
	{
		if(additionalPanel == null)
		{
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setVgap(2);
			additionalPanel = new JPanel();
			additionalPanel.setLayout(flowLayout1);
			additionalPanel.add(jLabel1, null);
			additionalPanel.add(getBindingLevelsCheckBox(), null);
			additionalPanel.add(getDecoratedCheckBox(), null);
		}
		return additionalPanel;
	}

	/**
	 * This method initializes BindingLevelsCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getBindingLevelsCheckBox()
	{
		if (bindingLevelsCheckBox == null)
		{
			bindingLevelsCheckBox = new JCheckBox();
			bindingLevelsCheckBox.setText("binding levels");
		}
		return bindingLevelsCheckBox;
	}

	/**
	 * This method initializes viewPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getViewPanel()
	{
		if(viewPanel == null)
		{
			jLabel3 = new JLabel();
			jLabel3.setText("font:");
			viewPanel = new JPanel();
			viewPanel.setLayout(new FlowLayout());
			viewPanel.add(jLabel3, null);
			viewPanel.add(getFontSpinner());
		}
		return viewPanel;
	}
	
	private JSpinner getFontSpinner()
	{
		if(fontSpinner == null)
		{
			fontSpinner = new JSpinner(new SpinnerNumberModel(10, 2, 36, 1));
		}
		
		return fontSpinner;
	}
}
