package odra.cli.gui.components.ast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;

import odra.sbql.ast.AST2TextQueryDumper;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.statements.Statement;
import odra.system.config.ConfigDebug;

/**
 * AST visualizer panel.
 *
 * @author jacenty
 * @version 2007-11-30
 * @since 2007-06-08
 */
public class ASTVisualizerPanel extends JPanel
{
	/** query to visualize */
	private ASTNode query;
	/** font size */
	private int fontSize;
	
	private boolean decorated = false;	
	private boolean bindingLevel = false;
	
	
	//  @jve:decl-index=0:
	
	/** current tree level position */
	private int levelPosition = -1;
	/** marked node panel left position */
	private int markedLeft = -1;
	/** marked node panel right position */
	private int markedRight = -1;
	
	private JPanel treePanel = null;

	private JTextPane queryTextPane = null;

	private JScrollPane queryScrollPane = null;

	private JSplitPane splitPane = null;

	private JScrollPane treeScrollPane = null;
	
	/**
	 * The constructor.
	 * 
	 * @param query query to visualize
	 */
	public ASTVisualizerPanel(ASTNode query)
	{
		super();
		this.query = query;
		initialize();
	}
	
	/**
	 * The constructor.
	 */
	public ASTVisualizerPanel()
	{
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		this.setLayout(new BorderLayout());
		this.setSize(627, 434);
		this.setBackground(Color.white);
		showQuery("some message...", -1, -1);
		this.add(getSplitPane(), BorderLayout.CENTER);
		
		refresh();
	}

	/**
	 * Sets the current query and refreshes the panel.
	 * 
	 * @param query query to visualize
	 * @param fontSize font size
	 */
	public void setQuery(ASTNode query, int fontSize)
	{
		this.query = query;
		this.fontSize = fontSize;
		
		refresh();
	}
	
	/**
	 * Refreshes the panel.
	 */
	private void refresh()
	{
		treePanel.removeAll();
		
		if(query == null)
			showQuery("No query currently available...", -1, -1);
		else
		{
			try
			{
				showQuery(new AST2TextQueryDumper().dumpAST(query), -1, -1);
				if(query instanceof Expression || query instanceof Statement)
					treePanel.add(new NodePanel(query, null, this, fontSize, decorated, bindingLevel));
				else
					throw new Exception("Only expression and statements supported now, given: " + query);
			}
			catch (Exception exc)
			{
				JOptionPane.showMessageDialog(this, exc, "Error during query processing", JOptionPane.ERROR_MESSAGE);
				
				if(ConfigDebug.DEBUG_EXCEPTIONS)
					exc.printStackTrace();
			}
		}
		
		validate();
		repaint();
	}

	/**
	 * This method initializes treePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getTreePanel()
	{
		if(treePanel == null)
		{
			treePanel = new JPanel();
			treePanel.setBackground(Color.white);
		}
		return treePanel;
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		Dimension dimension = super.getPreferredSize();
		
		if(getBorder() instanceof TitledBorder)
		{
			TitledBorder border = (TitledBorder)getBorder(); 
			Insets insets = border.getBorderInsets(this);
			String title = border.getTitle();
			int width = dimension.width;
			Graphics g = getGraphics();
			if(g != null)
			{
				g.setFont(border.getTitleFont());
				width = g.getFontMetrics().stringWidth(title);
			}
			width = Math.max(width + insets.left + insets.right, dimension.width);
			dimension.setSize(new Dimension(width, dimension.height));
		}
		
		return dimension;
	}
	
	@Override
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}
	
	/**
	 * Marks the given node in the query string.
	 * 
	 * @param node node to mark
	 */
	protected void markNode(final ASTNode node)
	{
		try
		{
			if(node == null)
				showQuery(new AST2TextQueryDumper().dumpAST(query), -1, -1);
			else
			{
				try
				{
					Object[] marked = new AST2TextQueryDumper().dumpASTWithMark(query, node);
					int position = (Integer)marked[1];
					int length = (Integer)marked[2];
					
					showQuery(new AST2TextQueryDumper().dumpAST(query), position, length);
				}
				catch(RuntimeException exc)
				{
					//unimplemented visitors for terminals
					showQuery(new AST2TextQueryDumper().dumpAST(query), -1, -1);
				}
			}
		}
		catch(Exception exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();
		}
	}
	
	/**
	 * Displays the query and marks the current node.
	 * 
	 * @param query query string
	 * @param markedPosition marked position
	 * @param length marked length
	 */
	private void showQuery(String query, int markedPosition, int length)
	{	
		if(markedPosition < 0 || length < 0)
			getQueryTextPane().setText("<html><font size=\"3\" face=\"monospaced\">" + formatValue(query) + "</font></html");
		else
		{
			String left = formatValue(query.substring(0, markedPosition));
			String middle = formatValue(query.substring(markedPosition, markedPosition + length));
			String right = formatValue(query.substring(markedPosition + length));
			
			getQueryTextPane().setText(
				"<html><font size=\"3\" face=\"monospaced\">" + 
				left + 
				"<b><font color=\"red\">" + middle + "</font></b>" + 
				right + 
				"</font></html>");
			
			getQueryTextPane().select(markedPosition, length);
		}
	}
	
	/**
	 * Formats (replaces some characters with HTML predefined entities) raw values.
	 * 
	 * @param raw raw value
	 * @return formatted string
	 */
	private String formatValue(String value)
	{
		return value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	/**
	 * This method initializes queryTextPane	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextPane getQueryTextPane()
	{
		if(queryTextPane == null)
		{
			queryTextPane = new JTextPane();
			queryTextPane.setEditable(false);
			queryTextPane.setContentType("text/html");
		}
		return queryTextPane;
	}

	/**
	 * This method initializes queryScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getQueryScrollPane()
	{
		if(queryScrollPane == null)
		{
			queryScrollPane = new JScrollPane();
			queryScrollPane.setViewportView(getQueryTextPane());
		}
		return queryScrollPane;
	}

	/**
	 * This method initializes splitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getSplitPane()
	{
		if(splitPane == null)
		{
			splitPane = new JSplitPane();
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPane.setDividerLocation(60);
			splitPane.setDividerSize(1);
			splitPane.setBottomComponent(getTreeScrollPane());
			splitPane.setTopComponent(getQueryScrollPane());
		}
		return splitPane;
	}

	/**
	 * This method initializes treeScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getTreeScrollPane()
	{
		if(treeScrollPane == null)
		{
			treeScrollPane = new JScrollPane();
			treeScrollPane.setViewportView(getTreePanel());
		}
		return treeScrollPane;
	}
	
	/**
	 * Shows a horizontal line to mark the node panel level.
	 * 
	 * @param nodePanel {@link NodePanel}
	 */
	void markLevel(NodePanel nodePanel)
	{
		if(nodePanel == null)
			levelPosition = -1;
		else
		{
			levelPosition = nodePanel.getLocationOnScreen().y - this.getLocationOnScreen().y;
			markedLeft = nodePanel.getLocationOnScreen().x - this.getLocationOnScreen().x - 1;
			markedRight = nodePanel.getLocationOnScreen().x + nodePanel.getSize().width - this.getLocationOnScreen().x;
		}
		
		repaint();
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);

		g.setColor(new Color(212, 212, 212));
		if(levelPosition >= 0)
		{
			g.drawLine(0, levelPosition, markedLeft, levelPosition);
			g.drawLine(markedRight, levelPosition, this.getSize().width, levelPosition);
		}
	}
	
	public boolean isDecorated()
	{
		return decorated;
	}

	public void setDecorated(boolean isDecorated)
	{
		this.decorated = isDecorated;
	}

	public boolean isBindingLevel()
	{
		return bindingLevel;
	}

	public void setBindingLevel(boolean isBindingLevel)
	{
		this.bindingLevel = isBindingLevel;
	}

	
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
