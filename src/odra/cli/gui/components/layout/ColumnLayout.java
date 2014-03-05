package odra.cli.gui.components.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Column layout manager.
 *
 * @author jacenty
 * @version 2006-12-28
 * @since 2006-12-24
 */
public class ColumnLayout implements LayoutManager
{
	public enum Align
	{
		LEFT,
		CENTER,
		RIGHT,
		FILL;
	}
	
	private int vGap;
	private Align align;
	private boolean spring;
	
	private int minWidth = 0;
	private int minHeight = 0;
	private int preferredWidth = 0;
	private int preferredHeight = 0;
	private boolean sizeUnknown = true;

	public ColumnLayout(int vGap, Align align, boolean spring)
	{
		this.vGap = vGap;
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
		int nComps = parent.getComponentCount();
		Dimension dim = null;

		preferredWidth = 0;
		preferredHeight = 0;
		minWidth = 0;
		minHeight = 0;

		for(int i = 0; i < nComps; i++)
		{
			Component component = parent.getComponent(i);
			if(component.isVisible())
			{
				dim = component.getPreferredSize();

				if(i > 0)
					preferredHeight += vGap;
				
				preferredWidth = Math.max(dim.width, preferredWidth);
				preferredHeight += dim.height;

				minWidth = Math.max(component.getMinimumSize().width, minWidth);
				minHeight = preferredHeight;
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
		int ySpring = 0;
		int xOffset = 0;

		if(sizeUnknown)
			setSizes(parent);

		if(spring && maxHeight > preferredHeight)
			ySpring = (maxHeight - preferredHeight) / (componentCount - 1);

		for(int i = 0; i < componentCount; i++)
		{
			Component component = parent.getComponent(i);
			Dimension dim = component.getPreferredSize();
			
			if(component.isVisible())
			{
				if(align.equals(Align.LEFT))
					x = 0;
				else if(align.equals(Align.RIGHT))
					x = parent.getWidth() - insets.left - component.getPreferredSize().width;
				else if(align.equals(Align.CENTER))
					x = (maxWidth - component.getPreferredSize().width) / 2;
				else if(align.equals(Align.FILL))
				{
					x = 0;
					dim = new Dimension(maxWidth, dim.height);
					component.setPreferredSize(dim);
				}
				
				if(i > 0)
					y += previousHeight + vGap + ySpring;

				component.setBounds(x, y, dim.width, dim.height);

				previousWidth = dim.width;
				previousHeight = dim.height;
			}
		}
	}

	public String toString()
	{
		String str = "";
		return getClass().getName() + "[vgap=" + vGap + str + "]";
	}
}
