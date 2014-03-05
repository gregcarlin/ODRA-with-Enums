package odra.cli.gui.components.storage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JViewport;

import odra.util.HeapSorter;

/**
 * Heap presentation panel subcomponent.
 *
 * @author jacenty
 * @version 2007-01-04
 * @since 2006-12-27
 */
class HeapStructurePanel extends JPanel
{
	/** heap bytes array */
	private byte[] bytes;
	/** heap bytes types array */
	private byte[] types;
	
	/** undefined color */
	private Color undefinedColor = Color.lightGray;
	/** file header color */
	private Color fileHeaderColor = new Color(126, 174, 221);
	/** database root color */
	private Color dbRootColor = new Color(170, 115, 173);
	/** free header color */
	private Color freeHeaderColor = new Color(93, 186, 127);
	/** free data color */
	private Color freeDataColor = new Color(155, 212, 119);
	/** occupied header color */
	private Color occupiedHeaderColor = new Color(162, 62, 77);
	/** occupied data color */
	private Color occupiedDataColor = new Color(200, 69, 76);
	
	/** single block width */
	private int blockWidth = 30;
	/** single block height */
	private int blockHeight = 14;
	/** block space */
	private int blockSpace = 1;
	/** font size */
	private int fontSize = (int)(.8 * blockHeight);

	/** display mode */
	private Mode mode;

	/** current number of columns */
	private int curCols = 1;

	
	/**
	 * The constructor.
	 * 
	 * @param length heap length (no. of bytes)
	 * @param mode display mode
	 */
	HeapStructurePanel(int length, Mode mode)
	{
		super();
		
		this.mode = mode;
		bytes = new byte[length];
		types = new byte[length];
		
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		GridLayout gridLayout = new GridLayout();
		this.setLayout(gridLayout);
		this.setBackground(Color.white);
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);

		g.setFont(new Font("monospaced", Font.PLAIN, fontSize));

		int length = bytes.length;
		Rectangle clip = ((JViewport)getParent()).getViewRect();
		int width = clip.getBounds().width;
		int marginLeft = g.getFontMetrics().stringWidth(Integer.toString(length + 1));
		int marginTop = fontSize;
		int xOffset = blockSpace + marginLeft;
		int yOffset = blockSpace + marginTop + clip.y;

		int x;
		int y;
		int w;
		
		int cols = (width - marginLeft) / (blockWidth + blockSpace);
		int rows = length / cols;
		if(rows * cols < length)
			rows++;
		this.setPreferredSize(new Dimension(width, rows * (blockHeight + blockSpace) + blockHeight + marginTop));
		this.setSize(this.getPreferredSize());		
		
		int start = cols * (int)(clip.getBounds().getY() / (blockHeight + blockSpace)); 
		int stop = Math.min(start + cols * (int)Math.ceil(clip.getBounds().getHeight() / (blockHeight + blockSpace)), length);
		
		curCols = (int)Math.ceil(clip.getBounds().getHeight()) / (blockHeight + blockSpace);
		
		for(int i = start; i < stop; i++)
		{
			if(i == start)
			{
				g.setColor(Color.black);
				String startByte = Integer.toString(i);
				g.drawString(startByte, blockSpace + marginLeft - g.getFontMetrics().stringWidth(startByte), yOffset + fontSize);
				
				for(int j = 1; j <= cols; j++)
				{
					String colOffset = "+" + Integer.toString(j);
					g.drawString(colOffset, j * (blockWidth + blockSpace) + marginLeft - g.getFontMetrics().stringWidth(colOffset), yOffset);
				}
			}
			
			byte b = bytes[i];
			byte t = types[i];
			Point position = new Point(xOffset, yOffset);
			if(clip.contains(position))
			{
				String bs = "";
				if(mode == Mode.BYTE)
					bs = Byte.toString(b);
				else if(mode == Mode.CHARACTER)
					bs = Character.toString((char)b);
				else if(mode == Mode.HEXADECIMAL)
				{
					bs = Integer.toHexString(b);
					if(bs.length() > 2)
						bs = bs.substring(0, 2);
				}
				
				switch (t)
				{
					case HeapSorter.FILE_HEADER:
						g.setColor(fileHeaderColor);
						break;
					case HeapSorter.DB_ROOT:
						g.setColor(dbRootColor);
						break;
					case HeapSorter.FREE_DATA:
						g.setColor(freeDataColor);
						break;
					case HeapSorter.FREE_HEADER:
						g.setColor(freeHeaderColor);
						break;
					case HeapSorter.OCCUPIED_DATA:
						g.setColor(occupiedDataColor);
						break;
					case HeapSorter.OCCUPIED_HEADER:
						g.setColor(occupiedHeaderColor);
						break;
					default:
						g.setColor(undefinedColor);
						break;
				}
				
				g.fillRect(xOffset, yOffset, blockWidth, blockHeight);
				
				g.setColor(Color.white);
				w = g.getFontMetrics().stringWidth(bs);
				x = xOffset + (blockWidth + blockSpace - w) / 2;
				y = yOffset - blockSpace + (blockHeight + fontSize) / 2;
				g.drawString(bs, x, y);
				
				xOffset += blockWidth + blockSpace;
				if(xOffset + blockWidth + blockSpace > width)
				{
					xOffset = blockSpace + marginLeft;
					yOffset += blockHeight + blockSpace;
					
					g.setColor(Color.black);
					String startByte = Integer.toString(i + 1);
					g.drawString(startByte, blockSpace + marginLeft - g.getFontMetrics().stringWidth(startByte), yOffset + fontSize);
				}
			}
			else if(yOffset < clip.y + clip.height)
				continue;
			else
				break;
		}
	}

	public int getCurrentColumns() {
		return curCols;		
	}
	
	/**
	 * Returns the file header color.
	 * 
	 * @return file header color
	 */
	public Color getFileHeaderColor()
	{
		return fileHeaderColor;
	}

	/**
	 * Sets the file header color.
	 * 
	 * @param fileHeaderColor file header color
	 */
	public void setFileHeaderColor(Color fileHeaderColor)
	{
		this.fileHeaderColor = fileHeaderColor;
	}

	/**
	 * Returns the free data color.
	 * 
	 * @return free data color
	 */
	public Color getFreeDataColor()
	{
		return freeDataColor;
	}

	/**
	 * Sets the free data color.
	 * 
	 * @param freeDataColor free data color
	 */
	public void setFreeDataColor(Color freeDataColor)
	{
		this.freeDataColor = freeDataColor;
	}

	/**
	 * Returns the free header color.
	 * 
	 * @return free header color
	 */
	public Color getFreeHeaderColor()
	{
		return freeHeaderColor;
	}

	/**
	 * Sets the free header color.
	 * 
	 * @param freeHeaderColor free header color
	 */
	public void setFreeHeaderColor(Color freeHeaderColor)
	{
		this.freeHeaderColor = freeHeaderColor;
	}

	/**
	 * Returns the occupied data color.
	 * 
	 * @return occupied data color
	 */
	public Color getOccupiedDataColor()
	{
		return occupiedDataColor;
	}

	/**
	 * Sets the occupied data color.
	 * 
	 * @param occupiedDataColor occupied data color
	 */
	public void setOccupiedDataColor(Color occupiedDataColor)
	{
		this.occupiedDataColor = occupiedDataColor;
	}

	/**
	 * Returns the occupied header color.
	 * 
	 * @return occupied header color
	 */
	public Color getOccupiedHeaderColor()
	{
		return occupiedHeaderColor;
	}

	/**
	 * Sets the occupied header color.
	 * 
	 * @param occupiedHeaderColor occupied header color
	 */
	public void setOccupiedHeaderColor(Color occupiedHeaderColor)
	{
		this.occupiedHeaderColor = occupiedHeaderColor;
	}

	/**
	 * Returns the database root color.
	 * 
	 * @return database root color
	 */
	public Color getDbRootColor()
	{
		return dbRootColor;
	}

	/**
	 * Sets the database root color.
	 * 
	 * @param dbRootColor database root color
	 */
	public void setDbRootColor(Color dbRootColor)
	{
		this.dbRootColor = dbRootColor;
	}
	
	/**
	 * Returns the undefined color.
	 * 
	 * @return undefined color
	 */
	public Color getUndefinedColor()
	{
		return undefinedColor;
	}

	/**
	 * Sets the undefined color.
	 * 
	 * @param undefinedColor undefined color
	 */
	public void setUndefinedColor(Color undefinedColor)
	{
		this.undefinedColor = undefinedColor;
	}

	/**
	 * Returns a single block height.
	 * 
	 * @return single block height
	 */
	public int getBlockHeight()
	{
		return blockHeight;
	}

	/**
	 * Sets a single block height.
	 * 
	 * @param blockHeight single block height
	 */
	public void setBlockHeight(int blockHeight)
	{
		this.blockHeight = blockHeight;
		fontSize = (int)(.8 * blockHeight);
	}

	/**
	 * Returns a single block width.
	 * 
	 * @return single block width
	 */
	public int getBlockWidth()
	{
		return blockWidth;
	}

	/**
	 * Sets a single block width.
	 * <br />
	 * Notice that a font size is calculated basing on a block height and ensure its width fits a maximum string length. 
	 * 
	 * @param blockWidth single block width
	 */
	public void setBlockWidth(int blockWidth)
	{
		this.blockWidth = blockWidth;
	}

	/**
	 * Returns the current display mode.
	 * 
	 * @return display mode
	 */
	public Mode getMode()
	{
		return mode;
	}

	/**
	 * Sets the current display mode.
	 * 
	 * @param mode display mode
	 */
	public void setMode(Mode mode)
	{
		this.mode = mode;
		repaint();
	}
	
	/**
	 * Returns a block space.
	 * 
	 * @return block space
	 */
	public int getBlockSpace()
	{
		return blockSpace;
	}

	/**
	 * Sets a block apce.
	 * 
	 * @param blockSpace block space
	 */
	public void setBlockSpace(int blockSpace)
	{
		this.blockSpace = blockSpace;
	}
	
	/**
	 * Sets byte values.
	 * 
	 * @param index byte index
	 * @param bytes byte values
	 */
	void setBytes(int index, byte[] bytes)
	{
		if(this.bytes != null)
			System.arraycopy(bytes, 0, this.bytes, index, bytes.length);
	}
	
	/**
	 * Sets byte types.
	 * 
	 * @param index byte index
	 * @param types byte types
	 */
	void setTypes(int index, byte[] types)
	{
		if(this.types != null)
			System.arraycopy(types, 0, this.types, index, types.length);
	}
	
	/**
	 * Frees the memory.
	 */
	void cleanup()
	{
		bytes = null;
		types = null;
	}
}
