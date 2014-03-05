package odra.cli.gui.components.storage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Legend panel.
 *
 * @author jacenty
 * @version 2006-12-31
 * @since 2006-12-31
 */
class LegendPanel extends JPanel
{
	/** colo */
	private final Color color;
	/** decription */
	private final String descr;
	/** heap structure panel */
	private final HeapStructurePanel heapStructurePanel;
	
	
	private JLabel descrLabel = null;
	private JPanel colorPanel = null;

	/**
	 * The constructor.
	 * 
	 * @param heapStructurePanel heap structure panel
	 * @param color color
	 * @param descr description
	 */
	LegendPanel(HeapStructurePanel heapStructurePanel, Color color, String descr)
	{
		super();
		this.heapStructurePanel = heapStructurePanel;
		this.color = color;
		this.descr = descr;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		descrLabel = new JLabel();
		descrLabel.setText(descr);
		this.setSize(300, 200);
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		this.add(getColorPanel(), BorderLayout.CENTER);
		this.add(descrLabel, BorderLayout.EAST);
	}

	/**
	 * This method initializes colorPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getColorPanel()
	{
		if(colorPanel == null)
		{
			colorPanel = new JPanel();
			colorPanel.setBackground(color);
			colorPanel.setPreferredSize(new Dimension(heapStructurePanel.getBlockWidth(), heapStructurePanel.getBlockHeight()));
		}
		return colorPanel;
	}
}
