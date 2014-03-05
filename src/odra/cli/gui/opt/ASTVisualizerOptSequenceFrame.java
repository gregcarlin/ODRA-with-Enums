package odra.cli.gui.opt;

import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import odra.cli.CLI;
import odra.cli.gui.ast.ASTVisualizerFrame;
import odra.cli.gui.components.CLIFrame;
import odra.cli.gui.components.opt.OptSequencePanel;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.optimizers.Type;
import odra.system.config.ConfigDebug;

/**
 * Optimization sequence frame for {@link ASTVisualizerFrame}. The {@link OptSequencePanel#accept()} 
 * does not affect the sequence for the CLI instance, it is used only for visualization.
 *
 * @author jacenty
 * @version 2007-08-02
 * @since 2007-07-15
 */
public class ASTVisualizerOptSequenceFrame extends CLIFrame
{
	/** referring CLI instance */
	private CLI cli;
	/** current optimization sequence */
	private OptimizationSequence currentSequence = null;
	
	private JPanel contentPane = null;
	protected OptSequencePanel optSequencePanel = null;
	/**
	 * The constructor.
	 * 
	 * @param cli
	 *          referring CLI instance
	 */
	public ASTVisualizerOptSequenceFrame(CLI cli)
	{
		super();
		
		this.cli = cli;
		
		initialize();
	}
	
	/**
	 * This method initializes this
	 */
	private void initialize()
	{
		this.setSize(300, 300);
		this.setTitle("AST visualizer optimization sequence");
		this.setContentPane(getContentPane());
	}

	@Override
	public JPanel getContentPane()
	{
		if(contentPane == null)
		{
			contentPane = new JPanel();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(getOptSequencePanel(), BorderLayout.CENTER);
		}
		return contentPane;
	}

	/**
	 * This method initializes optSequencePanel	
	 * 	
	 * @return {@link OptSequencePanel}
	 */
	private OptSequencePanel getOptSequencePanel()
	{
		if(optSequencePanel == null)
		{
			optSequencePanel = new OptSequencePanel() 
			{
				@Override
				public void accept()
				{
					try
					{
						currentSequence = optSequencePanel.getOptimizationSequence();
					}
					catch (OptimizationException exc)
					{
						if(ConfigDebug.DEBUG_EXCEPTIONS)
							exc.printStackTrace();
						
						JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				
				@Override
				public void cancel()
				{
					close();
				}
				
				@Override
				public void initFill()
				{
					try
					{
						fillRawTypes(cli, false);
					}
					catch (OptimizationException exc)
					{
						if(ConfigDebug.DEBUG_EXCEPTIONS)
							exc.printStackTrace();
						
						JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				
					if(currentSequence == null)//first display
					{
						try
						{
							currentSequence = getSequenceFromCli(cli, false);
						}
						catch (OptimizationException exc)
						{
							if(ConfigDebug.DEBUG_EXCEPTIONS)
								exc.printStackTrace();
							
							JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					
					for(Type type : currentSequence)
						for(int i = 0; i < model.getRowCount(); i++)
							if(model.getValueAt(i, 1).equals(type.getTypeName()))
								model.setValueAt(new Boolean(true), i, 0);
				}
			};
		}
		return optSequencePanel;
	}
	
	void close()
	{
		getOptSequencePanel().initFill();
		super.setVisible(false);
	}

	/**
	 * Returns the current optimization sequence.
	 * 
	 * @return current optimization sequence
	 */
	public OptimizationSequence getCurrentSequence()
	{
		return currentSequence;
	}
}
