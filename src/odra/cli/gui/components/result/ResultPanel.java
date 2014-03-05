package odra.cli.gui.components.result;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.UnsupportedEncodingException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import odra.cli.gui.components.layout.ColumnLayout;
import odra.cli.gui.components.layout.RowLayout;
import odra.sbql.results.runtime.BagResult;
import odra.sbql.results.runtime.BinderResult;
import odra.sbql.results.runtime.BooleanResult;
import odra.sbql.results.runtime.DoubleResult;
import odra.sbql.results.runtime.IntegerResult;
import odra.sbql.results.runtime.RawResultPrinter;
import odra.sbql.results.runtime.Result;
import odra.sbql.results.runtime.SingleResult;
import odra.sbql.results.runtime.StringResult;
import odra.sbql.results.runtime.StructResult;

/**
 * Result presentation panel.
 *
 * @author jacenty
 * @version 2007-03-24
 * @since 2006-12-24
 */
public class ResultPanel extends JPanel
{
	/** base background color */
	private Color baseBackgroundColor = Color.white;
	
	/** background color decrement */
	private int backgroundColorDecrement = 5;
	
	/** bag result border color */
	private Color bagBorderColor = Color.magenta;  //  @jve:decl-index=0:
	/** structure result border color */
	private Color structureBorderColor = Color.blue;  //  @jve:decl-index=0:
	/** binder result border color */
	private Color binderBorderColor = Color.red;
	
	
	/** "no color" background color */
	private Color noColorBackgroundColor = new Color(235, 239, 244);  //  @jve:decl-index=0:
	/** "no color" tite color */
	private Color noColorTitleColor = Color.gray;
	/** "no color" border color */
	private Color noColorBorderColor = Color.gray;  //  @jve:decl-index=0:
	
	/** result */
	private Result result;  //  @jve:decl-index=0:
	/** nesting level */
	private int nestingLevel;
	/** use colors? */
	private boolean useColors = false;
	
	/** result printer */
	private RawResultPrinter resultPrinter = new RawResultPrinter();
	
	private JLabel messageLabel = null;
	private JPanel resultCentainerPanel = null;
	
	/**
	 * The constructor.
	 * 
	 * @param result result
	 * @param nestingLevel nesting level
	 */
	private ResultPanel(Result result, int nestingLevel)
	{
		super();
		this.result = result;
		this.nestingLevel = nestingLevel;
		initialize();
	}
	
	/**
	 * The constructor.
	 */
	public ResultPanel()
	{
		super();
		initialize();
	}
	
	/**
	 * The constructor.
	 * 
	 * @param result result
	 */
	public ResultPanel(Result result)
	{
		super();
		this.result = result;
		this.nestingLevel = 0;
		initialize();
	}
	
	/**
	 * The constructor.
	 * 
	 * @param useColors use colors?
	 */
	public ResultPanel(boolean useColors)
	{
		super();
		this.useColors = useColors;
		initialize();
	}
	
	/**
	 * The constructor.
	 * 
	 * @param result result
	 * @param useColors use colors?
	 */
	public ResultPanel(Result result, boolean useColors)
	{
		super();
		this.result = result;
		this.nestingLevel = 0;
		this.useColors = useColors;
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
		this.setSize(300, 200);
		messageLabel = new JLabel();
		messageLabel.setText("some message");
		this.add(messageLabel, BorderLayout.NORTH);
		this.add(getResultCentainerPanel(), BorderLayout.CENTER);
		
		refresh();
	}

	/**
	 * Sets the current result and refreshes the panel.
	 * 
	 * @param result result
	 * @param nestingLevel nesting level
	 */
	private void setResult(Result result, int nestingLevel)
	{
		this.result = result;
		this.nestingLevel = nestingLevel;
		
		refresh();
	}
	
	/**
	 * Sets the current result and refreshes the panel.
	 * 
	 * @param result result
	 */
	public void setResult(Result result)
	{
		setResult(result, 0);
		getParent().validate();
	}
	
	@Override
	public Color getBackground()
	{
		if(useColors)
		{
			int r = baseBackgroundColor.getRed();
			int g = baseBackgroundColor.getGreen();
			int b = baseBackgroundColor.getBlue();
			int decrement = nestingLevel * backgroundColorDecrement;
			
			return new Color(r - decrement, g - decrement, b - decrement);
		}
		else
			return noColorBackgroundColor;
	}
	
	@Override
	public Border getBorder()
	{
		Color borderColor;
		String title;
		
		if(result != null)
		{
			if(result instanceof BagResult)
			{
				borderColor = bagBorderColor;
				title = "bag";
			}
			else if(result instanceof BinderResult)
			{
				borderColor = binderBorderColor;
				title = "binder: " + ((BinderResult)result).getName();
			}
			else if(result instanceof StructResult)
			{
				borderColor = structureBorderColor;
				title = "structure";
			}
			else
				return null;
			
			if(!useColors)
				borderColor = noColorBorderColor;
			
			TitledBorder border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(borderColor, 1),
				title,
				TitledBorder.LEFT,
				TitledBorder.TOP
				);
			if(useColors)
				border.setTitleColor(borderColor);
			else
				border.setTitleColor(noColorTitleColor);
			
			return border;
		}
		else
			return null;
	}
	
	/**
	 * Refreshes the panel.
	 */
	private void refresh()
	{
		setBorder(getBorder());
		
		resultCentainerPanel.removeAll();
		resultCentainerPanel.setBackground(this.getBackground());
		
		messageLabel.setText("");
		
		if(result == null)
		{
			messageLabel.setText("No result currently available...");
		}
		else if(result instanceof BagResult)
		{
			resultCentainerPanel.setLayout(new ColumnLayout(1, ColumnLayout.Align.LEFT, false));
			
			BagResult bagResult = (BagResult)result;
			SingleResult[] singleResults =  bagResult.elementsToArray();
			for(int i = 0; i < singleResults.length; i++)
				resultCentainerPanel.add(new ResultPanel(singleResults[i], nestingLevel + 1));
		}
		else if(result instanceof BinderResult)
		{
			resultCentainerPanel.setLayout(new RowLayout(1, RowLayout.Align.TOP, false));
			
			resultCentainerPanel.add(new ResultPanel(((BinderResult)result).value, nestingLevel + 1));
		}
		else if(result instanceof StructResult)
		{
			resultCentainerPanel.setLayout(new RowLayout(1, RowLayout.Align.TOP, false));
			
			StructResult structResult = (StructResult)result;
			SingleResult[] singleResults = structResult.fieldsToArray();
			for(int i = 0; i < singleResults.length; i++)
				resultCentainerPanel.add(new ResultPanel(singleResults[i], nestingLevel + 1));
		}
		else
		{
			resultCentainerPanel.setLayout(new RowLayout(1, RowLayout.Align.TOP, false));
			
			String type = "";
			if(result instanceof StringResult)
				type = "string";
			else if(result instanceof IntegerResult)
				type = "integer";
			else if(result instanceof BooleanResult)
				type = "boolean";
			else if(result instanceof DoubleResult)
				type = "double";
			
			try
			{
				messageLabel.setText(type + ": " + resultPrinter.print(result));
			}
			catch(UnsupportedEncodingException exc)
			{
				messageLabel.setText(type + ": <cannot output result in the current encoding>");
			}
		}
	}

	/**
	 * This method initializes resultCentainerPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getResultCentainerPanel()
	{
		if(resultCentainerPanel == null)
		{
			resultCentainerPanel = new JPanel();
		}
		return resultCentainerPanel;
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
	 * Returns the current bag result border color for <code>useColor = true</code>.
	 * 
	 * @return bag result border color
	 */
	public Color getBagBorderColor()
	{
		return bagBorderColor;
	}

	/**
	 * Sets the current bag result border color for <code>useColor = true</code>.
	 * 
	 * @param bagBorderColor bag result border color
	 */
	public void setBagBorderColor(Color bagBorderColor)
	{
		this.bagBorderColor = bagBorderColor;
	}

	/**
	 * Returns the current base background color for <code>useColor = true</code>.
	 * 
	 * @return base background color
	 */
	public Color getBaseBackgroundColor()
	{
		return baseBackgroundColor;
	}

	/**
	 * Sets the current base background color for <code>useColor = true</code>.
	 * 
	 * @param baseBackgroundColor base background color
	 */
	public void setBaseBackgroundColor(Color baseBackgroundColor)
	{
		this.baseBackgroundColor = baseBackgroundColor;
	}

	/**
	 * Returns the current binder result border color for <code>useColor = true</code>.
	 * 
	 * @return binder result border color
	 */
	public Color getBinderBorderColor()
	{
		return binderBorderColor;
	}

	/**
	 * Sets the current binder result border color for <code>useColor = true</code>.
	 * 
	 * @param binderBorderColor binder result border color
	 */
	public void setBinderBorderColor(Color binderBorderColor)
	{
		this.binderBorderColor = binderBorderColor;
	}

	/**
	 * Returns the current background color for <code>useColor = false</code>.
	 * 
	 * @return background color
	 */
	public Color getNoColorBackgroundColor()
	{
		return noColorBackgroundColor;
	}

	/**
	 * Sets the current background color for <code>useColor = false</code>.
	 * 
	 * @param noColorBackgroundColor background color
	 */
	public void setNoColorBackgroundColor(Color noColorBackgroundColor)
	{
		this.noColorBackgroundColor = noColorBackgroundColor;
	}

	/**
	 * Returns the current border color for <code>useColor = false</code>.
	 * 
	 * @return border color
	 */
	public Color getNoColorBorderColor()
	{
		return noColorBorderColor;
	}

	/**
	 * Sets the current border color for <code>useColor = false</code>.
	 * 
	 * @param noColorBorderColor border color
	 */
	public void setNoColorBorderColor(Color noColorBorderColor)
	{
		this.noColorBorderColor = noColorBorderColor;
	}

	/**
	 * Returns the current border title color for <code>useColor = false</code>.
	 * 
	 * @return border title color
	 */
	public Color getNoColorTitleColor()
	{
		return noColorTitleColor;
	}

	/**
	 * Sets the current border title color for <code>useColor = false</code>.
	 * 
	 * @param noColorTitleColor border title color
	 */
	public void setNoColorTitleColor(Color noColorTitleColor)
	{
		this.noColorTitleColor = noColorTitleColor;
	}

	/**
	 * Returns the current structure result border color for <code>useColor = true</code>.
	 * 
	 * @return structure result border color
	 */
	public Color getStructureBorderColor()
	{
		return structureBorderColor;
	}

	/**
	 * Sets the current structure result border color for <code>useColor = true</code>.
	 * 
	 * @param structureBorderColor structure result border color
	 */
	public void setStructureBorderColor(Color structureBorderColor)
	{
		this.structureBorderColor = structureBorderColor;
	}

	/**
	 * Sets whether to use colors.
	 * 
	 * @return use colors?
	 */
	public boolean isUseColors()
	{
		return useColors;
	}

	/**
	 * Returns whether to use colors.
	 * 
	 * @param useColors use colors?
	 */
	public void setUseColors(boolean useColors)
	{
		this.useColors = useColors;
	}
}
