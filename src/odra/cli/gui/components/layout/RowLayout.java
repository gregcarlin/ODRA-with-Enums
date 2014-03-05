package odra.cli.gui.components.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Row layout manager.
 *
 * @author jacenty
 * @version 2006-12-28
 * @since 2006-12-26
 */
public class RowLayout implements LayoutManager
{
	public enum Align
	{
		TOP,
		MIDDLE,
		BOTTOM,
		FILL;
	}
	
	private int hGap;
	private Align align;
	private boolean spring;
	
	private int minWidth = 0;
	private int minHeight = 0;
	private int preferredWidth = 0;
	private int preferredHeight = 0;
	private boolean sizeUnknown = true;

	public RowLayout(int hGap, Align align, boolean spring)
	{
		this.hGap = hGap;
		this.align = align;
		this.spring = spring;
	}

	public void addLayoutComponent(String name, Component comp)
	{
		//nothing happens
	}

	public void removeLayoutComponent(Component comp)
	{
		//nothing happens
	}

	private void setSizes(Container parent)
	{
		int compCount = parent.getComponentCount();
		Dimension dim = null;

		preferredWidth = 0;
		preferredHeight = 0;
		minWidth = 0;
		minHeight = 0;

		for(int i = 0; i < compCount; i++)
		{
			Component component = parent.getComponent(i);
			if(component.isVisible())
			{
				dim = component.getPreferredSize();

				if(i > 0)
					preferredWidth += hGap;
				
				preferredWidth += dim.width;
				preferredHeight = Math.max(dim.height, preferredHeight);

				minWidth = preferredWidth;
				minHeight = Math.max(component.getMinimumSize().height, minHeight);
			}
		}
	}

	public Dimension preferredLayoutSize(Container parent)
	{
		Dimension dim = new Dimension(0, 0);

		setSizes(parent);

		Insets insets = parent.getInsets();
		dim.width = preferredWidth + insets.left + insets.right;
		dim.height = preferredHeight + insets.top + insets.bottom;

		sizeUnknown = false;

		return dim;
	}

	public Dimension minimumLayoutSize(Container parent)
	{
		return preferredLayoutSize(parent);
	}

	public void layoutContainer(Container parent)
	{
		Insets insets = parent.getInsets();
		int maxWidth = parent.getWidth() - (insets.left + insets.right);
		int maxHeight = parent.getHeight() - (insets.top + insets.bottom);
		int componentCount = parent.getComponentCount();
		int previousWidth = 0;
		int previousHeight = 0;
		int x = insets.left;
		int y = insets.top;
		int rowh = 0;
		int start = 0;
		int xSpring = 0;
		int yOffset = 0;

		if(sizeUnknown)
			setSizes(parent);

		if(spring && maxWidth > preferredWidth)
			xSpring = (maxWidth - preferredWidth) / (componentCount - 1);

		for(int i = 0; i < componentCount; i++)
		{
			Component component = parent.getComponent(i);
			Dimension dim = component.getPreferredSize();
			
			if(component.isVisible())
			{
				if(align.equals(Align.TOP))
					y = 0;
				else if(align.equals(Align.BOTTOM))
					y = parent.getHeight() - insets.top - component.getPreferredSize().height;
				else if(align.equals(Align.MIDDLE))
					y = (maxHeight - component.getPreferredSize().height) / 2;
				else if(align.equals(Align.FILL))
				{
					y = 0;
					dim = new Dimension(dim.width, maxHeight);
					component.setPreferredSize(dim);
				}
				
				if(i > 0)
					x += previousWidth + hGap + xSpring;

				component.setBounds(x, y, dim.width, dim.height);

				previousWidth = dim.width;
				previousHeight = dim.height;
			}
		}
	}

	public String toString()
	{
		String str = "";
		return getClass().getName() + "[hgap=" + hGap + str + "]";
	}
}
