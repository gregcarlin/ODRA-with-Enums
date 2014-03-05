package odra.cli.gui.opt;

import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import odra.cli.CLI;
import odra.cli.gui.components.CLIFrame;
import odra.cli.gui.components.opt.OptSequencePanel;
import odra.exceptions.rd.RDException;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.optimizers.Type;
import odra.system.config.ConfigDebug;
import javax.swing.JTabbedPane;

/**
 * Optimization sequence frame for {@link CLI}. The {@link OptSequencePanel#accept()} sets the current 
 * optimization sequences for the session (evaluation) and optimization tests (reference).
 *
 * @author jacenty
 * @version 2007-08-02
 * @since 2007-07-14
 */
public class CLIOptSequenceFrame extends CLIFrame
{
	/** referring CLI instance */
	private CLI cli;
	private JPanel contentPane = null;
	protected OptSequencePanel evaluationOptSequencePanel = null;
	private JTabbedPane tabbedPane = null;
	private OptSequencePanel referenceOptSequencePanel = null;
	/**
	 * The constructor.
	 * 
	 * @param cli
	 *          referring CLI instance
	 */
	public CLIOptSequenceFrame(CLI cli)
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
		this.setSize(300, 320);
		this.setTitle("Server optimization sequences");
		this.setContentPane(getContentPane());
	}

	@Override
	public JPanel getContentPane()
	{
		if(contentPane == null)
		{
			contentPane = new JPanel();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(getTabbedPane(), BorderLayout.CENTER);
		}
		return contentPane;
	}

	/**
	 * This method initializes evaluationOptSequencePanel	
	 * 	
	 * @return {@link OptSequencePanel}
	 */
	private OptSequencePanel getEvaluationOptSequencePanel()
	{
		if(evaluationOptSequencePanel == null)
		{
			evaluationOptSequencePanel = new OptSequencePanel() 
			{
				@Override
				public void accept()
				{
					setSequence(this);
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
						setSelectedFromCLI(cli, false);
					}
					catch (OptimizationException exc)
					{
						if(ConfigDebug.DEBUG_EXCEPTIONS)
							exc.printStackTrace();
						
						JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			};
		}
		return evaluationOptSequencePanel;
	}
	
	/**
	 * Sets the server-side optimization sequence depending on the panel passed.
	 * 
	 * @param optSequencePanel {@link OptSequencePanel}
	 */
	private void setSequence(OptSequencePanel optSequencePanel)
	{
		try
		{
			boolean reference = optSequencePanel == getReferenceOptSequencePanel();

			OptimizationSequence sequence = optSequencePanel.getOptimizationSequence();
			String[] opts = new String[sequence.size() + 2];
			opts[0] = Boolean.toString(reference);
			opts[1] = Type.NONE.getTypeName();
			for(int i = 0; i < sequence.size(); i++)
				opts[i + 2] = sequence.get(i).getTypeName();
			
			cli.execSetOptimization(opts);
		}
		catch (OptimizationException exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			
			JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			
			JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (RDException exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
			
			JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	void close()
	{
		getEvaluationOptSequencePanel().initFill();
		super.setVisible(false);
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
			tabbedPane.addTab("evaluation", null, getEvaluationOptSequencePanel(), "<html>This sequence is used for actual query <b>evaluation</b>.</html>");
			tabbedPane.addTab("reference", null, getReferenceOptSequencePanel(), "<html>This sequence is used as <b>reference</b> for optimization tests, <br/>with no influence on actual query evaluation.</html>");
		}
		return tabbedPane;
	}

	/**
	 * This method initializes referenceOptSequencePanel	
	 * 	
	 * @return odra.cli.gui.components.opt.OptSequencePanel	
	 */
	private OptSequencePanel getReferenceOptSequencePanel()
	{
		if(referenceOptSequencePanel == null)
		{
			referenceOptSequencePanel = new OptSequencePanel()
			{
				@Override
				public void accept()
				{
					setSequence(this);
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
						fillRawTypes(cli, true);
						setSelectedFromCLI(cli, true);
					}
					catch (OptimizationException exc)
					{
						if(ConfigDebug.DEBUG_EXCEPTIONS)
							exc.printStackTrace();
						JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			};
		}
		return referenceOptSequencePanel;
	}
}
