package odra.cli.gui.components.opt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import odra.cli.CLI;
import odra.cli.gui.components.layout.RowLayout;
import odra.exceptions.rd.RDException;
import odra.sbql.optimizers.OptimizationException;
import odra.sbql.optimizers.OptimizationSequence;
import odra.sbql.optimizers.Type;
import odra.system.config.ConfigDebug;

/**
 * Optimization sequence panel.
 *
 * @author jacenty
 * @version 2007-08-02
 * @since 2007-07-14
 */
public abstract class OptSequencePanel extends JPanel
{
	private final String USER_DEFINED = "user-defined";
	
	/** 
	 * current optimization order (only for table display, it can be inactive); 
	 * the order is set at each {@link #getOptimizationSequence()} call
	 */
	private Type[] currentOrder = null;
	
	private JPanel eastPanel = null;
	private JButton upButton = null;
	private JButton downButton = null;
	private JPanel buttonsPanel = null;
	private JScrollPane scrollPane = null;
	private JTable table = null;
	protected DefaultTableModel model = new DefaultTableModel(new Object[] {"on", "type"}, 0);
	private JPanel southPanel = null;
	private JPanel actionButtonsPanel = null;
	private JButton acceptButton = null;
	private JButton cancelButton = null;
	private JButton resetButton = null;

	private JPanel northPanel = null;

	private JLabel jLabel = null;

	private JComboBox predefinedComboBox = null;
	
	/**
	 * This is the default constructor
	 */
	public OptSequencePanel()
	{
		super();
		initialize();
	}
	
	/**
	 * The method is used for accepting the current sequence.
	 */
	public abstract void accept();
	/**
	 * The method is used for cancelling the current sequence and resetting introduced changes 
	 * (e.g. according to the current CLI state).
	 */
	public abstract void cancel();
	/**
	 * The method is used for initialiazing the sequence on constructing the panel 
	 * (e.g. according to the current CLI state).
	 */
	public abstract void initFill();


	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.add(getEastPanel(), BorderLayout.EAST);
		this.add(getScrollPane(), BorderLayout.CENTER);
		this.add(getSouthPanel(), BorderLayout.SOUTH);
		this.add(getNorthPanel(), BorderLayout.NORTH);
		
		initFill();
	}
	
//	/**
//	 * Fills the list with available optimization kinds.
//	 *
//	 */
//	private void fill()
//	{
//		for(Type type : Type.values())
//			if(!type.equals(Type.NONE))
//			 model.addRow(new Object[] {false, type.getTypeName()});
//		
//		try
//		{
//			String sequenceString = cli.execShow(new String[] {OptimizationSequence.OPTIMIZATION, Boolean.toString(false)});
//			String[] types = sequenceString.split("\\|");
//			for(String type : types)
//				for(int i = 0; i < model.getRowCount(); i++)
//					if(model.getValueAt(i, 1).equals(type.trim()))
//						model.setValueAt(new Boolean(true), i, 0);
//		}
//		catch (IOException exc)
//		{
//			if(ConfigDebug.DEBUG_EXCEPTIONS)
//				exc.printStackTrace();
//			
//			JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//		}
//		catch (RDException exc)
//		{
//			if(ConfigDebug.DEBUG_EXCEPTIONS)
//				exc.printStackTrace();
//			
//			JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//		}
//	}

	/**
	 * This method initializes eastPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getEastPanel()
	{
		if(eastPanel == null)
		{
			eastPanel = new JPanel();
			eastPanel.setLayout(new RowLayout(5, RowLayout.Align.MIDDLE, false));
			eastPanel.add(getButtonsPanel(), null);
		}
		return eastPanel;
	}

	/**
	 * This method initializes upButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUpButton()
	{
		if(upButton == null)
		{
			upButton = new JButton();
			upButton.setText("up");
			upButton.setEnabled(false);
			upButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					upButtonActionPerformed(e);
				}
			});
		}
		return upButton;
	}
	
	/**
	 * This method initializes downButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDownButton()
	{
		if(downButton == null)
		{
			downButton = new JButton();
			downButton.setText("down");
			downButton.setEnabled(false);
			downButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					downButtonActionPerformed(e);
				}
			});
		}
		return downButton;
	}

	void upButtonActionPerformed(@SuppressWarnings("unused") ActionEvent e)
	{
		int[] rows = getTable().getSelectedRows();
		if(rows.length < 1 || rows[0] == 0)
			return;
		
		TableModel model = getTable().getModel();
		Object[][] data = new Object[model.getRowCount()][model.getColumnCount()];
		
		for(int i = 0; i < model.getRowCount(); i++)
			for(int j = 0; j < model.getColumnCount(); j++)
				data[i][j] = model.getValueAt(i, j);
		
		for(int i = 0; i < rows.length; i++)
		{
			Object[] first = data[rows[i]];
			Object[] second = data[rows[i] - 1];
			
			data[rows[i]] = second;
			data[rows[i] - 1] = first;
		}
		
		for(int i = 0; i < data.length; i++)
			for(int j = 0; j < data[i].length; j++)
				model.setValueAt(data[i][j], i, j);
		((DefaultTableModel)model).fireTableDataChanged();
		
		for(int i = 0; i < rows.length; i++)
			getTable().getSelectionModel().addSelectionInterval(rows[i] - 1, rows[i] - 1);
	}
	
	void downButtonActionPerformed(@SuppressWarnings("unused") ActionEvent e)
	{
		int[] rows = getTable().getSelectedRows();
		if(rows.length < 1 || rows[rows.length - 1] == getTable().getModel().getRowCount())
			return;
		
		TableModel model = getTable().getModel();
		Object[][] data = new Object[model.getRowCount()][model.getColumnCount()];
		
		for(int i = 0; i < model.getRowCount(); i++)
			for(int j = 0; j < model.getColumnCount(); j++)
				data[i][j] = model.getValueAt(i, j);
		
		for(int i = rows.length - 1; i >= 0; i--)
		{
			Object[] first = data[rows[i]];
			Object[] second = data[rows[i] + 1];
			
			data[rows[i]] = second;
			data[rows[i] + 1] = first;
		}
		
		for(int i = 0; i < data.length; i++)
			for(int j = 0; j < data[i].length; j++)
				model.setValueAt(data[i][j], i, j);
		((DefaultTableModel)model).fireTableDataChanged();
		
		for(int i = 0; i < rows.length; i++)
			getTable().getSelectionModel().addSelectionInterval(rows[i] + 1, rows[i] + 1);
	}

	/**
	 * This method initializes buttonsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonsPanel()
	{
		if(buttonsPanel == null)
		{
			GridLayout gridLayout1 = new GridLayout();
			gridLayout1.setRows(2);
			gridLayout1.setVgap(10);
			gridLayout1.setColumns(1);
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout(gridLayout1);
			buttonsPanel.add(getUpButton(), null);
			buttonsPanel.add(getDownButton(), null);
		}
		return buttonsPanel;
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
			scrollPane.setViewportView(getTable());
		}
		return scrollPane;
	}

	/**
	 * This method initializes table	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getTable()
	{
		if(table == null)
		{
			table = new JTable(model) 
			{
				@Override
				public TableCellEditor getCellEditor(int row, int col)
				{
					if(col == 0)
					{
						JCheckBox checkBox = new JCheckBox();
						checkBox.setHorizontalAlignment(SwingConstants.CENTER);
						checkBox.setOpaque(true);
						checkBox.setBackground(table.getBackground());
						return new DefaultCellEditor(checkBox);
					}
					else
						return super.getCellEditor(row, col);
				}
				
				@Override
				public TableCellRenderer getCellRenderer(@SuppressWarnings("unused") int row, int col)
				{
					if(col == 0)
						return new DefaultTableCellRenderer() 
						{
							@Override
							public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, @SuppressWarnings("unused") boolean hasFocus, @SuppressWarnings("unused") int row, @SuppressWarnings("unused") int col)
							{
								JCheckBox checkBox = new JCheckBox();
								checkBox.setSelected((Boolean)value);
								checkBox.setOpaque(true);
								checkBox.setHorizontalAlignment(SwingConstants.CENTER);
								if(isSelected)
									checkBox.setBackground(table.getSelectionBackground());
								else
									checkBox.setBackground(table.getBackground());
								return checkBox;
							}
						};
					else
						return new DefaultTableCellRenderer() 
						{
							@Override
							public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, @SuppressWarnings("unused") boolean hasFocus, @SuppressWarnings("unused") int row, @SuppressWarnings("unused") int col)
							{
								JLabel label = new JLabel();
								label.setText((String)value);
								label.setOpaque(true);
								if(isSelected)
								{
									label.setBackground(table.getSelectionBackground());
									label.setForeground(table.getSelectionForeground());
								}
								else
								{
									label.setBackground(table.getBackground());
									label.setForeground(table.getForeground());
								}
								return label;
							}
						};
				}
				
				@Override
				public boolean isCellEditable(@SuppressWarnings("unused") int row, int col)
				{
					return col == 0;
				}
				
				@Override
				public boolean getShowVerticalLines()
				{
					return false;
				}
			};
		}
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(e.getValueIsAdjusting())
					return;
				
				upButton.setEnabled(false);
				downButton.setEnabled(false);
				
				int[] rows = table.getSelectedRows();
				if(rows.length < 1)
					return;
				upButton.setEnabled(rows[0] >= 1);
				downButton.setEnabled(rows[rows.length - 1] < table.getModel().getRowCount() - 1);
			}});
		
		return table;
	}
	
	/**
	 * Returns the current optimization sequence.
	 * 
	 * @return {@link OptimizationSequence}
	 * @throws OptimizationException 
	 */
	public OptimizationSequence getOptimizationSequence() throws OptimizationException
	{
		OptimizationSequence sequence = new OptimizationSequence();
		
		TableModel model = getTable().getModel();

		currentOrder = new Type[model.getRowCount()];
		
		for(int i = 0; i < model.getRowCount(); i++)
		{
			currentOrder[i] = Type.getTypeForString((String)model.getValueAt(i, 1));
			if((Boolean)model.getValueAt(i, 0))
				sequence.addType(Type.getTypeForString((String)model.getValueAt(i, 1)));
		}
		
		return sequence;
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
			southPanel.setLayout(new FlowLayout());
			southPanel.add(getActionButtonsPanel(), null);
		}
		return southPanel;
	}

	/**
	 * This method initializes actionButtonsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getActionButtonsPanel()
	{
		if(actionButtonsPanel == null)
		{
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setHgap(10);
			actionButtonsPanel = new JPanel();
			actionButtonsPanel.setLayout(gridLayout);
			actionButtonsPanel.add(getAcceptButton(), null);
			actionButtonsPanel.add(getCancelButton(), null);
			actionButtonsPanel.add(getResetButton(), null);
		}
		return actionButtonsPanel;
	}

	/**
	 * This method initializes acceptButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAcceptButton()
	{
		if(acceptButton == null)
		{
			acceptButton = new JButton();
			acceptButton.setText("accept");
			acceptButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
				{
					accept();
				}
			});
		}
		return acceptButton;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton()
	{
		if(cancelButton == null)
		{
			cancelButton = new JButton();
			cancelButton.setText("cancel");
			cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
				{
					cancel();
				}
			});
		}
		return cancelButton;
	}

	/**
	 * This method initializes resetButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getResetButton()
	{
		if(resetButton == null)
		{
			resetButton = new JButton();
			resetButton.setText("reset");
			resetButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
				{
					initFill();
				}
			});
		}
		return resetButton;
	}

	/**
	 * Returns the current optimization type order.
	 * 
	 * @return current optimization type order
	 */
	protected Type[] getCurrentOrder()
	{
		return currentOrder;
	}
	
	/**
	 * Sets the current optimization type order.
	 * 
	 * @param order order to set
	 */
	protected void setCurrentOrder(Type[] order)
	{
		this.currentOrder = order;
	}
	
	/**
	 * Sets the current optimization type order.
	 * 
	 * @param order order to set
	 */
	protected void setCurrentOrder(Vector<Type> order)
	{
		setCurrentOrder(order.toArray(new Type[0]));
	}
	
	/**
	 * Fills the table with rows corresponding to raw types. If the currentOrder is not <code>null</code>, 
	 * it is used here.
	 * 
	 * @param cli {@link CLI} instance
	 * @param reference reference optimization sequence?
	 * @throws OptimizationException 
	 */
	protected void fillRawTypes(CLI cli, boolean reference) throws OptimizationException
	{
		for(int i = model.getRowCount() - 1; i >= 0; i--)
			model.removeRow(i);
		
		if(getCurrentOrder() == null)
		{
			OptimizationSequence first = getSequenceFromCli(cli, reference);//to ensure first display of the current sequence in its actual order
			Vector<Type> order = new Vector<Type>();
			order.addAll(first);
			
			for(Type type : Type.values())
				if(!type.equals(Type.NONE) && !first.contains(type))
					order.addElement(type);
			
			for(Type type : order)
				model.addRow(new Object[] {false, type.getTypeName()});
			
			setCurrentOrder(order);
		}
		else
			for(Type type : getCurrentOrder())
				model.addRow(new Object[] {false, type.getTypeName()});
	}
	
	/**
	 * Sets selected rows according to the current optimization sequence obtained from the {@link CLI} instance.
	 * 
	 * @param cli {@link CLI} instance
	 * @param reference reference optimization sequence?
	 * @throws OptimizationException 
	 */
	protected void setSelectedFromCLI(CLI cli, boolean reference) throws OptimizationException
	{
		OptimizationSequence sequence = getSequenceFromCli(cli, reference);
		for(Type type : sequence)
			for(int i = 0; i < model.getRowCount(); i++)
				if(model.getValueAt(i, 1).equals(type.getTypeName()))
					model.setValueAt(new Boolean(true), i, 0);
	}
	
	/**
	 * Returns the optimization sequence obtained from {@link CLI}.
	 * 
	 * @param cli {@link CLI} instance
	 * @param reference reference optimization sequence?
	 * @return {@link OptimizationSequence} read from CLI
	 * @throws OptimizationException 
	 */
	protected OptimizationSequence getSequenceFromCli(CLI cli, boolean reference) throws OptimizationException
	{
		OptimizationSequence sequence = new OptimizationSequence();
		try
		{
			String show = OptimizationSequence.OPTIMIZATION;
			if(reference)
				show = OptimizationSequence.REFOPTIMIZATION;
			
			String sequenceString = cli.execShow(new String[] {show, Boolean.toString(false)});
			String[] types = sequenceString.split("\\|");
			
			for(String type : types)
			{
				type = type.trim();
				sequence.addType(Type.getTypeForString(type));
			}
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
		
		return sequence;
	}

	/**
	 * This method initializes northPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getNorthPanel()
	{
		if(northPanel == null)
		{
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
			jLabel = new JLabel();
			jLabel.setText("choose:");
			northPanel = new JPanel();
			northPanel.setLayout(flowLayout);
			northPanel.add(jLabel, null);
			northPanel.add(getPredefinedComboBox(), null);
		}
		return northPanel;
	}

	/**
	 * This method initializes predefinedComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getPredefinedComboBox()
	{
		if(predefinedComboBox == null)
		{
			predefinedComboBox = new JComboBox();
			predefinedComboBox.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					predefinedComboBoxItemStateChanged(e);
				}
			});
			predefinedComboBox.addItem(USER_DEFINED);
			for(String predefined : OptimizationSequence.PREDEFINED.keySet())
				predefinedComboBox.addItem(predefined);
		}
		return predefinedComboBox;
	}
	
	void predefinedComboBoxItemStateChanged(ItemEvent e)
	{
		if(e.getStateChange() != ItemEvent.SELECTED)
			return;
		
		String selected = getPredefinedComboBox().getSelectedItem().toString();
		if(selected.equals(USER_DEFINED))
		{
			getTable().setEnabled(true);
			getTable().setBackground(Color.white);
		}
		else
		{
			getTable().setEnabled(false);
			getTable().setBackground(SystemColor.control);
			
			OptimizationSequence sequence = OptimizationSequence.getForName(selected);
			
			for(int i = model.getRowCount() - 1; i >= 0; i--)
				model.removeRow(i);

			Vector<Type> order = new Vector<Type>();
			order.addAll(sequence);
			
			for(Type type : Type.values())
				if(!type.equals(Type.NONE) && !sequence.contains(type))
					order.addElement(type);
			
			for(Type type : order)
				model.addRow(new Object[] {false, type.getTypeName()});
			
			setCurrentOrder(order);
			
			for(Type type : sequence)
				for(int i = 0; i < model.getRowCount(); i++)
					if(model.getValueAt(i, 1).equals(type.getTypeName()))
						model.setValueAt(new Boolean(true), i, 0);
		}
	}
}
