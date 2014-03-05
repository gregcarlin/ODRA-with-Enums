package odra.cli.gui.components.ast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import odra.cli.gui.components.layout.RowLayout;
import odra.sbql.SBQLException;
import odra.sbql.ast.ASTNode;
import odra.sbql.ast.ASTVisitor;
import odra.sbql.ast.declarations.ClassDeclaration;
import odra.sbql.ast.declarations.ClassFieldDeclaration;
import odra.sbql.ast.declarations.ClassInstanceDeclaration;
import odra.sbql.ast.declarations.EnumDeclaration;
import odra.sbql.ast.declarations.EnumFieldDeclaration;
import odra.sbql.ast.declarations.InterfaceDeclaration;
import odra.sbql.ast.declarations.InterfaceFieldDeclaration;
import odra.sbql.ast.declarations.MethodFieldDeclaration;
import odra.sbql.ast.declarations.ModuleDeclaration;
import odra.sbql.ast.declarations.NamedTypeDeclaration;
import odra.sbql.ast.declarations.ProcedureDeclaration;
import odra.sbql.ast.declarations.ProcedureFieldDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderDeclaration;
import odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration;
import odra.sbql.ast.declarations.RecordDeclaration;
import odra.sbql.ast.declarations.RecordTypeDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefDeclaration;
import odra.sbql.ast.declarations.ExternalSchemaDefFieldDeclaration;
import odra.sbql.ast.declarations.SessionVariableFieldDeclaration;
import odra.sbql.ast.declarations.TypeDefDeclaration;
import odra.sbql.ast.declarations.TypeDefFieldDeclaration;
import odra.sbql.ast.declarations.VariableDeclaration;
import odra.sbql.ast.declarations.VariableFieldDeclaration;
import odra.sbql.ast.declarations.ViewDeclaration;
import odra.sbql.ast.declarations.ViewFieldDeclaration;
import odra.sbql.ast.expressions.AsExpression;
import odra.sbql.ast.expressions.AssignExpression;
import odra.sbql.ast.expressions.AtLeastExpression;
import odra.sbql.ast.expressions.AtMostExpression;
import odra.sbql.ast.expressions.AvgExpression;
import odra.sbql.ast.expressions.BagExpression;
import odra.sbql.ast.expressions.BinaryExpression;
import odra.sbql.ast.expressions.BooleanExpression;
import odra.sbql.ast.expressions.CastExpression;
import odra.sbql.ast.expressions.CloseByExpression;
import odra.sbql.ast.expressions.CloseUniqueByExpression;
import odra.sbql.ast.expressions.CommaExpression;
import odra.sbql.ast.expressions.CountExpression;
import odra.sbql.ast.expressions.CreateExpression;
import odra.sbql.ast.expressions.CreateLocalExpression;
import odra.sbql.ast.expressions.CreatePermanentExpression;
import odra.sbql.ast.expressions.CreateTemporalExpression;
import odra.sbql.ast.expressions.DateExpression;
import odra.sbql.ast.expressions.DateprecissionExpression;
import odra.sbql.ast.expressions.DeleteExpression;
import odra.sbql.ast.expressions.DerefExpression;
import odra.sbql.ast.expressions.DeserializeOidExpression;
import odra.sbql.ast.expressions.DotExpression;
import odra.sbql.ast.expressions.EmptyExpression;
import odra.sbql.ast.expressions.EqualityExpression;
import odra.sbql.ast.expressions.ExecSqlExpression;
import odra.sbql.ast.expressions.ExistsExpression;
import odra.sbql.ast.expressions.Expression;
import odra.sbql.ast.expressions.ExternalNameExpression;
import odra.sbql.ast.expressions.ExternalProcedureCallExpression;
import odra.sbql.ast.expressions.ForAllExpression;
import odra.sbql.ast.expressions.ForSomeExpression;
import odra.sbql.ast.expressions.GroupAsExpression;
import odra.sbql.ast.expressions.IfThenElseExpression;
import odra.sbql.ast.expressions.IfThenExpression;
import odra.sbql.ast.expressions.InExpression;
import odra.sbql.ast.expressions.InsertCopyExpression;
import odra.sbql.ast.expressions.InsertExpression;
import odra.sbql.ast.expressions.InstanceOfExpression;
import odra.sbql.ast.expressions.IntegerExpression;
import odra.sbql.ast.expressions.IntersectExpression;
import odra.sbql.ast.expressions.JoinExpression;
import odra.sbql.ast.expressions.LazyFailureExpression;
import odra.sbql.ast.expressions.LeavesByExpression;
import odra.sbql.ast.expressions.LeavesUniqueByExpression;
import odra.sbql.ast.expressions.MaxExpression;
import odra.sbql.ast.expressions.MinExpression;
import odra.sbql.ast.expressions.MinusExpression;
import odra.sbql.ast.expressions.NameExpression;
import odra.sbql.ast.expressions.NonAlgebraicExpression;
import odra.sbql.ast.expressions.RenameExpression;
import odra.sbql.ast.expressions.SerializeOidExpression;
import odra.sbql.ast.expressions.OrderByExpression;
import odra.sbql.ast.expressions.ParallelExpression;
import odra.sbql.ast.expressions.ParallelUnionExpression;
import odra.sbql.ast.expressions.ProcedureCallExpression;
import odra.sbql.ast.expressions.RandomExpression;
import odra.sbql.ast.expressions.RangeExpression;
import odra.sbql.ast.expressions.RangeAsExpression;
import odra.sbql.ast.expressions.RealExpression;
import odra.sbql.ast.expressions.RefExpression;
import odra.sbql.ast.expressions.RemoteQueryExpression;
import odra.sbql.ast.expressions.SequentialExpression;
import odra.sbql.ast.expressions.SimpleBinaryExpression;
import odra.sbql.ast.expressions.SimpleUnaryExpression;
import odra.sbql.ast.expressions.StringExpression;
import odra.sbql.ast.expressions.StructExpression;
import odra.sbql.ast.expressions.SumExpression;
import odra.sbql.ast.expressions.ToBagExpression;
import odra.sbql.ast.expressions.ToBooleanExpression;
import odra.sbql.ast.expressions.ToDateExpression;
import odra.sbql.ast.expressions.ToIntegerExpression;
import odra.sbql.ast.expressions.ToRealExpression;
import odra.sbql.ast.expressions.ToSingleExpression;
import odra.sbql.ast.expressions.ToStringExpression;
import odra.sbql.ast.expressions.TransitiveClosureExpression;
import odra.sbql.ast.expressions.UnaryExpression;
import odra.sbql.ast.expressions.UnionExpression;
import odra.sbql.ast.expressions.UniqueExpression;
import odra.sbql.ast.expressions.WhereExpression;
import odra.sbql.ast.statements.BlockStatement;
import odra.sbql.ast.statements.BreakStatement;
import odra.sbql.ast.statements.ContinueStatement;
import odra.sbql.ast.statements.DoWhileStatement;
import odra.sbql.ast.statements.EmptyStatement;
import odra.sbql.ast.statements.ExpressionStatement;
import odra.sbql.ast.statements.ForEachStatement;
import odra.sbql.ast.statements.ForStatement;
import odra.sbql.ast.statements.IfElseStatement;
import odra.sbql.ast.statements.IfStatement;
import odra.sbql.ast.statements.ReturnWithValueStatement;
import odra.sbql.ast.statements.ReturnWithoutValueStatement;
import odra.sbql.ast.statements.SequentialStatement;
import odra.sbql.ast.statements.Statement;
import odra.sbql.ast.statements.ThrowStatement;
import odra.sbql.ast.statements.TransactionAbortStatement;
import odra.sbql.ast.statements.TryCatchFinallyStatement;
import odra.sbql.ast.statements.VariableDeclarationStatement;
import odra.sbql.ast.statements.WhileStatement;
import odra.sbql.ast.terminals.BooleanLiteral;
import odra.sbql.ast.terminals.DateLiteral;
import odra.sbql.ast.terminals.IntegerLiteral;
import odra.sbql.ast.terminals.Name;
import odra.sbql.ast.terminals.Operator;
import odra.sbql.ast.terminals.RealLiteral;
import odra.sbql.ast.terminals.StringLiteral;
import odra.sbql.ast.terminals.Terminal;
import odra.sbql.results.runtime.DateResult;
import odra.system.config.ConfigDebug;

/**
 * Query node visualisation panel.
 * 
 * @author jacenty
 * @version 2007-12-01
 * @since 2007-06-08
 */
class NodePanel extends JPanel implements ASTVisitor
{
	/** maximum text line length in tree */
	private final int MAX_LINE_LENGTH = 50;
	
	/** node */
	private ASTNode node;
	
	private boolean decorated;
	private boolean bindingLevel;
	
	/** node state definitions */
	enum State
	{
		NORMAL,
		TEXTUAL,
		COLLAPSED
	}
	private int stateIndex = 0;
	/** node state */
	private State state = State.values()[stateIndex];  //  @jve:decl-index=0:

	/**
	 * parent panel (not in component hierarchy, but in tree structure), can be
	 * <code>null</code> for root
	 */
	private NodePanel parent;

	/** {@link ASTVisualizerPanel} container */
	private ASTVisualizerPanel visualizerPanel;
	/** font size */
	private int fontSize;

	/** bakcground colour */
	private final Color BACKGROUND_COLOR = Color.white;
	/** literal colour */
	private final Color LITERAL_EXPRESSION_COLOR = Color.yellow; // @jve:decl-index=0:
	/** unary expression colour */
	private final Color UNARY_COLOR = Color.cyan; // @jve:decl-index=0:
	/** binary expression colour */
	private final Color BINARY_COLOR = Color.green;
	/** other expression colour */
	private final Color OTHER_COLOR = Color.orange; // @jve:decl-index=0:
	/** statement colour */
	private final Color STATEMENT_COLOR = Color.magenta;
	/** terminal colour */
	private final Color TERMINAL_COLOR = Color.lightGray;  //  @jve:decl-index=0:
	
	/** normal (inactive) border */
	private final Border NORMAL_BORDER = BorderFactory.createLineBorder(Color.white, 1); // @jve:decl-index=0:
	/** selected (active) border */
	private final Border SELECTED_BORDER = BorderFactory.createLineBorder(Color.red, 1);

	/** text for the description */
	private String value = "";  //  @jve:decl-index=0:
	/** text for the tooltip */
	private String tooltip = "";

	private JLabel descriptionLabel = null;
	private JPanel northPanel = null;
	private JPanel childrenPanel = null;
	private JPanel centerSpanPanel = null;

	private JPanel southPanel = null;

	private String modName = "";
	/**
	 * The constructor.
	 * 
	 * @param node
	 *          query node
	 * @param parent
	 *          parent panel (not in component hierarchy, but in tree structure),
	 *          can be <code>null</code> for root
	 * @param visualizerPanel
	 *          {@link ASTVisualizerPanel} container
	 * @param fontSize font size
	 */
	NodePanel(ASTNode node, NodePanel parent, ASTVisualizerPanel visualizerPanel, int fontSize)
	{
		super();

		this.node = node;
		this.parent = parent;
		this.visualizerPanel = visualizerPanel;
		this.fontSize = fontSize;

		if ( parent != null)
			decorated = parent.isDecorated();
		else
			decorated = false;

		if ( parent != null)
			bindingLevel = parent.isBindingLevel();
		else
			bindingLevel = false;
		
		initialize();
	}

	/**
	 * 
	 * The constructor.
	 * 
	 * @param node
	 *          query node
	 * @param parent
	 *          parent panel (not in component hierarchy, but in tree structure),
	 *          can be <code>null</code> for root
	 * @param visualizerPanel
	 *          {@link ASTVisualizerPanel} container
	 * @param fontSize 
	 *          font size
	 * @param decorated 
	 *           show link decoration?
	 * @param bindingLevel 
	 *           show binding levels
	 */
	NodePanel(ASTNode node, NodePanel parent, ASTVisualizerPanel visualizerPanel, int fontSize, boolean decorated, boolean bindingLevel)
	{		
		super();

		this.node = node;
		this.parent = parent;
		this.visualizerPanel = visualizerPanel;
		this.fontSize = fontSize;
		
		this.decorated = decorated;
		this.bindingLevel = bindingLevel;

		initialize();	
	}


	/**
	 * Visualizes the node.
	 */
	private void visualize()
	{		
		value = "<html><div align=\"center\">" + node.getClass().getSimpleName() + "<br />";

		if(node instanceof Terminal)
			visualizeTerminal((Terminal)node);
		else try
		{
			node.accept(this, null);
		}
		catch (Exception exc)
		{
			if(ConfigDebug.DEBUG_EXCEPTIONS)
				exc.printStackTrace();

			JOptionPane.showMessageDialog(visualizerPanel, exc, "Error during query processing", JOptionPane.ERROR_MESSAGE);
		}
		
		if(bindingLevel)
			decorateWithBindingLevels();
		
		if(decorated)
			decorateNodeWithLinks();
		
		value += "</div></html>";
		tooltip += "</html>";

		descriptionLabel.setText(value);

		if(node.line > 0)
		{
			tooltip = "<html>line, column: <b>" + formatValue((node.line) + ", " + node.column, MAX_LINE_LENGTH) + "</b>";
			descriptionLabel.setToolTipText(tooltip);
		}
	}

	

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		descriptionLabel = new JLabel();
		Font font = descriptionLabel.getFont();
		font = new Font(font.getName(), font.getStyle(), fontSize);
		descriptionLabel.setFont(font);
		descriptionLabel.setText("description");
		descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		descriptionLabel.setOpaque(true);
		descriptionLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseExited(MouseEvent e)
			{
				descriptionLabelMouseExited(e);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				descriptionLabelMouseEntered(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				descriptionLabelMouseClicked(e);
			}
		});
		
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.setBackground(BACKGROUND_COLOR);
		this.setBorder(NORMAL_BORDER);
		this.add(getNorthPanel(), BorderLayout.NORTH);
		this.add(getCenterSpanPanel(), BorderLayout.CENTER);

		this.add(getSouthPanel(), BorderLayout.SOUTH);
		visualize();
	}

	/**
	 * Formats (replaces some characters with HTML predefined entities and wraps
	 * to long ones) raw values.
	 * 
	 * @param raw raw value
	 * @param length maximum allowed line length
	 * @return formatted string
	 */
	protected final String formatValue(Object raw, int length)
	{
		if(raw == null)
			return "";

		String value = raw.toString();
		value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

		String formatted = "";
		int space = 0;
		while(value.length() > length)
		{
			space = value.indexOf(" ", length);
			if(space >= 0)
			{
				formatted += value.substring(0, space) + "<br />";
				value = value.substring(space);
			}
			else break;
		}
		formatted += value;

		formatted = formatted.replaceAll(" ", "&nbsp;");

		return formatted;
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
			flowLayout.setHgap(0);
			flowLayout.setVgap(0);
			northPanel = new JPanel();
			northPanel.setLayout(flowLayout);
			northPanel.setBackground(BACKGROUND_COLOR);
			northPanel.add(descriptionLabel, null);
		}
		return northPanel;
	}

	/**
	 * This method initializes childrenPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getChildrenPanel()
	{
		if(childrenPanel == null)
		{
			childrenPanel = new JPanel();
			childrenPanel.setLayout(new RowLayout(10, RowLayout.Align.TOP, false));
			childrenPanel.setName("childrenPanel");
			childrenPanel.setBackground(BACKGROUND_COLOR);
		}
		return childrenPanel;
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		if(state != State.NORMAL)
			return;

		g.setColor(Color.black);

		int bottomCenterX = descriptionLabel.getLocation().x + descriptionLabel.getPreferredSize().width / 2;
		int bottomCenterY = descriptionLabel.getLocation().y + descriptionLabel.getPreferredSize().height;

		for(Component component : getChildrenPanel().getComponents())
		{
			int topCenterX = getChildrenPanel().getLocation().x + component.getLocation().x + component.getPreferredSize().width / 2;
			int topCenterY = getSouthPanel().getLocation().y + component.getLocation().y;

			//single component, some approximations may cause non-veritcal line
			if(getChildrenPanel().getComponentCount() == 1 && topCenterX != bottomCenterX)
				topCenterX = bottomCenterX;
			
			g.drawLine(bottomCenterX, bottomCenterY, topCenterX, topCenterY);
		}
	}

	/**
	 * This method initializes centerSpanPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getCenterSpanPanel()
	{
		if(centerSpanPanel == null)
		{
			centerSpanPanel = new JPanel();
			centerSpanPanel.setLayout(null);
			centerSpanPanel.setBackground(BACKGROUND_COLOR);
			centerSpanPanel.setPreferredSize(new Dimension(0, 20));
		}
		return centerSpanPanel;
	}

	protected void descriptionLabelMouseEntered(@SuppressWarnings("unused") MouseEvent e)
	{
		activate(false);
	}

	protected void descriptionLabelMouseExited(@SuppressWarnings("unused") MouseEvent e)
	{
		deactivate(false);
	}
	
	protected void descriptionLabelMouseClicked(@SuppressWarnings("unused") MouseEvent e)
	{
		if(e.getClickCount() == 2)
			switchState(e.getButton() == MouseEvent.BUTTON1);
	}
	
	/**
	 * Collapses and expands this panel.
	 *
	 * @param forward forward switch?
	 */
	protected void switchState(boolean forward)
	{
		if(node instanceof Terminal)
			return;
		
		if(forward)
			nextState();
		else
			prevState();
		
		getCenterSpanPanel().setVisible(state == State.NORMAL);
		
		getSouthPanel().removeAll();
		getSouthPanel().add(state == State.NORMAL ? getChildrenPanel() : (state == State.TEXTUAL ? new TextualPlaceholder(this) : new CollapsedPlaceholder(this)));
		
		visualizerPanel.validate();
		visualizerPanel.markLevel(this);
	}

	/**
	 * Activates this panel and deactivates the parent;
	 * 
	 * @param fromTerminal
	 *          the command is sent from the underlying terminal?
	 */
	protected void activate(boolean fromTerminal)
	{
		if(node instanceof Terminal)
		{
			parent.activate(true);
			return;
		}

		if(parent != null)
			parent.deactivate(false);

		this.setBorder(SELECTED_BORDER);

		if(!fromTerminal)
			for(Component component : getChildrenPanel().getComponents())
				((NodePanel)component).deactivate(true);

		visualizerPanel.markNode(node);
		visualizerPanel.markLevel(this);
	}

	/**
	 * Deactivates this panel and deactivates the parent;
	 * 
	 * @param fromParent
	 *          the command is sent from the parent?
	 */
	protected void deactivate(boolean fromParent)
	{
		if(node instanceof Terminal && !fromParent)// !fromParent: prevent
			// deactivation loop
			parent.deactivate(false);

		this.setBorder(NORMAL_BORDER);

		visualizerPanel.markNode(null);
		visualizerPanel.markLevel(null);
	}

	public Object visitAsExpression(AsExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitAssignExpression(AssignExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitAvgExpression(AvgExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitBagExpression(BagExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitBlockStatement(BlockStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitBooleanExpression(BooleanExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeLiteralExpression(expr);
		return null;
	}

	public Object visitBreakStatement(BreakStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitCastExpression(CastExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitClassDeclaration(@SuppressWarnings("unused") ClassDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}
	
	public Object visitInterfaceDeclaration(@SuppressWarnings("unused") InterfaceDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}	

	public Object visitInterfaceFieldDeclaration(@SuppressWarnings("unused") InterfaceFieldDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}	
	
	public Object visitClassFieldDeclaration(@SuppressWarnings("unused") ClassFieldDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitClassInstanceDeclaration(@SuppressWarnings("unused") ClassInstanceDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitCloseByExpression(CloseByExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeNonalgebraicExpression(expr);
		return null;
	}

	public Object visitCloseUniqueByExpression(CloseUniqueByExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeNonalgebraicExpression(expr);
		return null;
	}

	public Object visitCommaExpression(CommaExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitContinueStatement(ContinueStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitCountExpression(CountExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitCreateExpression(CreateExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitCreateLocalExpression(CreateLocalExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitCreatePermanentExpression(CreatePermanentExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitCreateTemporalExpression(CreateTemporalExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitDateExpression(DateExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeLiteralExpression(expr);
		return null;
	}

	public Object visitDateprecissionExpression(DateprecissionExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitDeleteExpression(DeleteExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitDerefExpression(DerefExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}


	public Object visitDotExpression(DotExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeNonalgebraicExpression(expr);
		return null;
	}

	public Object visitDoWhileStatement(DoWhileStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitEmptyExpression(EmptyExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitEmptyStatement(EmptyStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitEqualityExpression(EqualityExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitExecSqlExpression(ExecSqlExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitExistsExpression(ExistsExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitExpressionStatement(ExpressionStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitExternalNameExpression(ExternalNameExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitExternalProcedureCallExpression(ExternalProcedureCallExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitForAllExpression(ForAllExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeNonalgebraicExpression(expr);
		return null;
	}

	public Object visitForEachStatement(ForEachStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitForSomeExpression(ForSomeExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeNonalgebraicExpression(expr);
		return null;
	}

	public Object visitForStatement(ForStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitGroupAsExpression(GroupAsExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitIfElseStatement(IfElseStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitIfStatement(IfStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitIfThenElseExpression(IfThenElseExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitIfThenExpression(IfThenExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitInExpression(InExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitInsertCopyExpression(InsertCopyExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitInsertExpression(InsertExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitInstanceOfExpression(InstanceOfExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitIntegerExpression(IntegerExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeLiteralExpression(expr);
		return null;
	}

	public Object visitIntersectExpression(IntersectExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitJoinExpression(JoinExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeNonalgebraicExpression(expr);
		return null;
	}

	public Object visitLeavesByExpression(LeavesByExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeNonalgebraicExpression(expr);
		return null;
	}

	public Object visitLeavesUniqueByExpression(LeavesUniqueByExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeNonalgebraicExpression(expr);
		return null;
	}

	public Object visitMaxExpression(MaxExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitMethodFieldDeclaration(@SuppressWarnings("unused") MethodFieldDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitMinExpression(MinExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}
	
	
	public Object visitLazyFailureExpression(LazyFailureExpression expr,@SuppressWarnings("unused") Object attr) throws SBQLException {
		
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitMinusExpression(MinusExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitModuleDeclaration(@SuppressWarnings("unused") ModuleDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitNamedTypeDeclaration(@SuppressWarnings("unused") NamedTypeDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitNameExpression(NameExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitOrderByExpression(OrderByExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitProcedureCallExpression(ProcedureCallExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitProcedureDeclaration(@SuppressWarnings("unused") ProcedureDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitProcedureFieldDeclaration(@SuppressWarnings("unused") ProcedureFieldDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitProcedureHeaderDeclaration(odra.sbql.ast.declarations.ProcedureHeader, java.lang.Object)
	 */
	public Object visitProcedureHeaderDeclaration(ProcedureHeaderDeclaration decl,
		Object attr) throws SBQLException {
	    throw new RuntimeException("Declarations are not supported...");
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitProcedureHeaderFieldDeclaration(odra.sbql.ast.declarations.ProcedureHeaderFieldDeclaration, java.lang.Object)
	 */
	public Object visitProcedureHeaderFieldDeclaration(
		ProcedureHeaderFieldDeclaration procedureHeaderFieldDeclaration,
		Object attr) throws SBQLException {
	    throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitRandomExpression(RandomExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitRangeExpression(RangeExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitRealExpression(RealExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeLiteralExpression(expr);
		return null;
	}

	public Object visitRecordDeclaration(@SuppressWarnings("unused") RecordDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitRecordTypeDeclaration(@SuppressWarnings("unused") RecordTypeDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}
	
	public Object visitEnumFieldDeclaration(@SuppressWarnings("unused") EnumFieldDeclaration declaration, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Enums are not supported...");
	}
	
	public Object visitEnumDeclaration(@SuppressWarnings("unused") EnumDeclaration declaration, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Enums are not supported...");
	}

	public Object visitRefExpression(RefExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitRemoteQueryExpression(RemoteQueryExpression expr, Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}
	
	public Object visitReturnWithoutValueStatement(ReturnWithoutValueStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitReturnWithValueStatement(ReturnWithValueStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitSequentialExpression(SequentialExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeOtherExpression(expr);
		return null;
	}

	public Object visitSequentialStatement(SequentialStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}

	public Object visitSessionVariableFieldDeclaration(@SuppressWarnings("unused") SessionVariableFieldDeclaration node, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitSimpleBinaryExpression(SimpleBinaryExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeSimpleBinaryExpression(expr);
		return null;
	}

	public Object visitSimpleUnaryExpression(SimpleUnaryExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeSimpleUnaryExpression(expr);
		return null;
	}

	public Object visitStringExpression(StringExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeLiteralExpression(expr);
		return null;
	}

	public Object visitStructExpression(StructExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitSumExpression(SumExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitToBagExpression(ToBagExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitToBooleanExpression(ToBooleanExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitToDateExpression(ToDateExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitToIntegerExpression(ToIntegerExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitToRealExpression(ToRealExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitToSingleExpression(ToSingleExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitToStringExpression(ToStringExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitTypeDefDeclaration(@SuppressWarnings("unused") TypeDefDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitTypeDefFieldDeclaration(@SuppressWarnings("unused") TypeDefFieldDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitUnionExpression(UnionExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeBinaryExpression(expr);
		return null;
	}

	public Object visitUniqueExpression(UniqueExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeUnaryExpression(expr);
		return null;
	}

	public Object visitVariableDeclaration(@SuppressWarnings("unused") VariableDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitVariableDeclarationStatement(@SuppressWarnings("unused") VariableDeclarationStatement node, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitVariableFieldDeclaration(@SuppressWarnings("unused") VariableFieldDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitViewDeclaration(@SuppressWarnings("unused") ViewDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}

	public Object visitViewFieldDeclaration(@SuppressWarnings("unused") ViewFieldDeclaration decl, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		throw new RuntimeException("Declarations are not supported...");
	}


	public Object visitWhereExpression(WhereExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeNonalgebraicExpression(expr);
		return null;
	}

	public Object visitWhileStatement(WhileStatement stmt, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}
	
	/**
	 * Visualizes {@link StringExpression}, {@link IntegerExpression},
	 * {@link RealExpression}, {@link BooleanExpression} and
	 * {@link DateExpression}.
	 * 
	 * @param expr
	 *          expression to visualize
	 */
	private void visualizeLiteralExpression(Expression expr)
	{
		assert expr instanceof StringExpression || expr instanceof IntegerExpression || expr instanceof RealExpression || expr instanceof BooleanExpression || expr instanceof DateExpression : "Wrong expression for this method: " + expr;

		descriptionLabel.setBackground(LITERAL_EXPRESSION_COLOR);

		if(expr instanceof StringExpression)
		{
			(((StringExpression)expr).getLiteral()).addLinkDecoration( expr.getLinksDecoration() );
			childrenPanel.add(new NodePanel(((StringExpression)expr).getLiteral(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof IntegerExpression)
		{
			(((IntegerExpression)expr).getLiteral()).addLinkDecoration( expr.getLinksDecoration() );
			childrenPanel.add(new NodePanel(((IntegerExpression)expr).getLiteral(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof BooleanExpression)
		{
			(((BooleanExpression)expr).getLiteral()).addLinkDecoration( expr.getLinksDecoration() );
			childrenPanel.add(new NodePanel(((BooleanExpression)expr).getLiteral(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof RealExpression)
		{
			(((RealExpression)expr).getLiteral()).addLinkDecoration( expr.getLinksDecoration() );
			childrenPanel.add(new NodePanel(((RealExpression)expr).getLiteral(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof DateExpression)
		{
			(((DateExpression)expr).getLiteral()).addLinkDecoration( expr.getLinksDecoration() );
			childrenPanel.add(new NodePanel(((DateExpression)expr).getLiteral(), this, visualizerPanel, fontSize));
		}
		
	}

	/**
	 * Visualizes {@link UnaryExpression} and its subclasses.
	 * 
	 * @param expr
	 *          expression to visualize
	 */
	private void visualizeUnaryExpression(UnaryExpression expr)
	{
		descriptionLabel.setBackground(UNARY_COLOR);

		getChildrenPanel().add(new NodePanel(expr.getExpression(), this, visualizerPanel, fontSize));
		
		if(expr instanceof AsExpression)
			getChildrenPanel().add(new NodePanel(((AsExpression)expr).name(), this, visualizerPanel, fontSize));
		else if(expr instanceof GroupAsExpression)
			getChildrenPanel().add(new NodePanel(((GroupAsExpression)expr).name(), this, visualizerPanel, fontSize));
	}

	/**
	 * Visualizes {@link SimpleUnaryExpression} and its subclasses.
	 * 
	 * @param expr
	 *          expression to visualize
	 */
	private void visualizeSimpleUnaryExpression(SimpleUnaryExpression expr)
	{
		descriptionLabel.setBackground(UNARY_COLOR);

		getChildrenPanel().add(new NodePanel(expr.O, this, visualizerPanel, fontSize));
		getChildrenPanel().add(new NodePanel(expr.getExpression(), this, visualizerPanel, fontSize));
	}

	/**
	 * Visualizes {@link BinaryExpression} and its subclasses.
	 * 
	 * @param expr
	 *          expression to visualize
	 */
	private void visualizeBinaryExpression(BinaryExpression expr)
	{
		descriptionLabel.setBackground(BINARY_COLOR);

		getChildrenPanel().add(new NodePanel(expr.getLeftExpression(), this, visualizerPanel, fontSize));
		if(expr instanceof EqualityExpression)
			childrenPanel.add(new NodePanel(((EqualityExpression)expr).O, this, visualizerPanel, fontSize));
		getChildrenPanel().add(new NodePanel(expr.getRightExpression(), this, visualizerPanel, fontSize));
	}

	/**
	 * Visualizes {@link NonAlgebraicExpression} and its subclasses.
	 * 
	 * @param stmt
	 *          statement to visualize
	 */
	private void visualizeNonalgebraicExpression(NonAlgebraicExpression expr)
	{
		descriptionLabel.setBackground(BINARY_COLOR);

		getChildrenPanel().add(new NodePanel(expr.getLeftExpression(), this, visualizerPanel, fontSize));
		getChildrenPanel().add(new NodePanel(expr.getRightExpression(), this, visualizerPanel, fontSize));

		if(expr instanceof DotExpression)
			value += "<b>.</b>";
		else if(expr instanceof WhereExpression)
			value += "<b>where</b>";
		else if(expr instanceof JoinExpression)
			value += "<b>join</b>";
		else if(expr instanceof OrderByExpression)
			value += "<b>orderby</b>";
		else if(expr instanceof TransitiveClosureExpression)
			value += "<b>closeby</b>";
		else if(expr instanceof ForAllExpression)
			value += "<b>forall</b>";
		else if(expr instanceof ForSomeExpression)
			value += "<b>forsome</b>";
	}

	/**
	 * Visualizes {@link SimpleBinaryExpression} and its subclasses.
	 * 
	 * @param expr
	 *          expression to visualize
	 */
	private void visualizeSimpleBinaryExpression(SimpleBinaryExpression expr)
	{
		descriptionLabel.setBackground(BINARY_COLOR);

		getChildrenPanel().add(new NodePanel(expr.getLeftExpression(), this, visualizerPanel, fontSize));
		childrenPanel.add(new NodePanel(expr.O, this, visualizerPanel, fontSize));
		getChildrenPanel().add(new NodePanel(expr.getRightExpression(), this, visualizerPanel, fontSize));
	}

	/**
	 * Visualizes {@link ParallelExpression} and its subclasses.
	 * 
	 * @param expr
	 *          expression to visualize
	 */
	private void visualizeParallelExpression(ParallelExpression expr)
	{
		descriptionLabel.setBackground(OTHER_COLOR);

		for(Expression subexpr: expr.getParallelExpressions()) 
			getChildrenPanel().add(new NodePanel(subexpr, this, visualizerPanel, fontSize));

	}
	
	/**
	 * Visualizes other expressions.
	 * 
	 * @param expr
	 *          expression to visualize
	 */
	private void visualizeOtherExpression(Expression expr)
	{
		descriptionLabel.setBackground(OTHER_COLOR);

		if(expr instanceof CreateExpression)
		{
			CreateExpression createExpression = (CreateExpression)expr;
			getChildrenPanel().add(new NodePanel(createExpression.name(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(createExpression.getExpression(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof DeleteExpression)
		{
			DeleteExpression deleteExpression = (DeleteExpression)expr;
			getChildrenPanel().add(new NodePanel(deleteExpression.getExpression(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof ExecSqlExpression)
		{
			ExecSqlExpression execSqlExpression = (ExecSqlExpression)expr;
			getChildrenPanel().add(new NodePanel(execSqlExpression.query, this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(execSqlExpression.pattern, this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(execSqlExpression.module, this, visualizerPanel, fontSize));
		}
		else if(expr instanceof ExternalNameExpression)
		{
			ExternalNameExpression externalNameExpression = (ExternalNameExpression)expr;
			getChildrenPanel().add(new NodePanel(externalNameExpression.name(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof ExternalProcedureCallExpression)
		{
			ExternalProcedureCallExpression externalProcedureCallExpression = (ExternalProcedureCallExpression)expr;
			getChildrenPanel().add(new NodePanel(externalProcedureCallExpression.getLeftExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(externalProcedureCallExpression.getRightExpression(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof IfThenExpression)
		{
			IfThenExpression ifThenExpression = (IfThenExpression)expr;
			getChildrenPanel().add(new NodePanel(ifThenExpression.getConditionExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(ifThenExpression.getThenExpression(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof IfThenElseExpression)
		{
			IfThenElseExpression ifThenElseExpression = (IfThenElseExpression)expr;
			getChildrenPanel().add(new NodePanel(ifThenElseExpression.getConditionExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(ifThenElseExpression.getThenExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(ifThenElseExpression.getElseExpression(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof InsertExpression)
		{
			InsertExpression insertExpression = (InsertExpression)expr;
			getChildrenPanel().add(new NodePanel(insertExpression.getLeftExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(insertExpression.getRightExpression(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof InsertCopyExpression)
		{
			InsertCopyExpression insertCopyExpression = (InsertCopyExpression)expr;
			getChildrenPanel().add(new NodePanel(insertCopyExpression.name(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(insertCopyExpression.getLeftExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(insertCopyExpression.getRightExpression(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof NameExpression)
		{
			NameExpression nameExpression = (NameExpression)expr;
			nameExpression.name().addLinkDecoration(expr.getLinksDecoration());
			getChildrenPanel().add(new NodePanel(nameExpression.name(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof ProcedureCallExpression)
		{
			ProcedureCallExpression procedureCallExpression = (ProcedureCallExpression)expr;
			getChildrenPanel().add(new NodePanel(procedureCallExpression.getProcedureSelectorExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(procedureCallExpression.getArgumentsExpression(), this, visualizerPanel, fontSize));
		}
		else if(expr instanceof SequentialExpression)
		{
			SequentialExpression sequentialExpression = (SequentialExpression)expr;
			getChildrenPanel().add(new NodePanel(sequentialExpression.getFirstExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(sequentialExpression.getSecondExpression(), this, visualizerPanel, fontSize));
		}
	}

	/**
	 * Visualizes {@link Statement} and its subclasses.
	 * 
	 * @param stmt
	 *          statement to visualize
	 */
	private void visualizeStatement(Statement stmt)
	{
		descriptionLabel.setBackground(STATEMENT_COLOR);

		if(stmt instanceof ExpressionStatement)
		{
			ExpressionStatement expressionStatement = (ExpressionStatement)stmt;
			getChildrenPanel().add(new NodePanel(expressionStatement.getExpression(), this, visualizerPanel, fontSize));
		}
		else if(stmt instanceof BlockStatement)
		{
			BlockStatement blockStatement = (BlockStatement)stmt;
			getChildrenPanel().add(new NodePanel(blockStatement.getStatement(), this, visualizerPanel, fontSize));
		}
		else if(stmt instanceof DoWhileStatement)
		{
			DoWhileStatement doWhileStatement = (DoWhileStatement)stmt;
			getChildrenPanel().add(new NodePanel(doWhileStatement.getStatement(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(doWhileStatement.getExpression(), this, visualizerPanel, fontSize));
		}
		else if(stmt instanceof ForEachStatement)
		{
			ForEachStatement forEachStatement = (ForEachStatement)stmt;
			getChildrenPanel().add(new NodePanel(forEachStatement.getStatement(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(forEachStatement.getExpression(), this, visualizerPanel, fontSize));
		}
		else if(stmt instanceof ForStatement)
		{
			ForStatement forStatement = (ForStatement)stmt;
			getChildrenPanel().add(new NodePanel(forStatement.getInitExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(forStatement.getConditionalExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(forStatement.getIncrementExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(forStatement.getStatement(), this, visualizerPanel, fontSize));
		}
		else if(stmt instanceof IfElseStatement)
		{
			IfElseStatement ifElseStatement = (IfElseStatement)stmt;
			getChildrenPanel().add(new NodePanel(ifElseStatement.getIfStatement(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(ifElseStatement.getElseStatement(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(ifElseStatement.getExpression(), this, visualizerPanel, fontSize));
		}
		else if(stmt instanceof IfStatement)
		{
			IfStatement ifStatement = (IfStatement)stmt;
			getChildrenPanel().add(new NodePanel(ifStatement.getExpression(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(ifStatement.getStatement(), this, visualizerPanel, fontSize));
		}
		else if(stmt instanceof SequentialStatement)
		{
			SequentialStatement sequentialStatement = (SequentialStatement)stmt;
			getChildrenPanel().add(new NodePanel(sequentialStatement.getFirstStatement(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(sequentialStatement.getSecondStatement(), this, visualizerPanel, fontSize));
		}
		else if(stmt instanceof WhileStatement)
		{
			WhileStatement whileStatement = (WhileStatement)stmt;
			getChildrenPanel().add(new NodePanel(whileStatement.getStatement(), this, visualizerPanel, fontSize));
			getChildrenPanel().add(new NodePanel(whileStatement.getExpression(), this, visualizerPanel, fontSize));
		}
	}

	private void visualizeTerminal(Terminal terminal)
	{
		descriptionLabel.setBackground(TERMINAL_COLOR);
		centerSpanPanel.setVisible(false);
		childrenPanel.setVisible(false);

		if(terminal instanceof Name)
			value += "<b>" + formatValue(((Name)terminal).value(), MAX_LINE_LENGTH) + "</b>";
		else if(terminal instanceof StringLiteral)
			value += "<b>&quot;" + formatValue(((StringLiteral)terminal).value(), MAX_LINE_LENGTH) + "&quot;</b>";
		else if(terminal instanceof IntegerLiteral)
			value += "<b>" + formatValue(((IntegerLiteral)terminal).value(), MAX_LINE_LENGTH) + "</b>";
		else if(terminal instanceof RealLiteral)
			value += "<b>" + formatValue(((RealLiteral)terminal).value(), MAX_LINE_LENGTH) + "</b>";
		else if(terminal instanceof BooleanLiteral)
			value += "<b>" + formatValue(((BooleanLiteral)terminal).value(), MAX_LINE_LENGTH) + "</b>";
		else if(terminal instanceof DateLiteral)
			value += "<b>" + formatValue(new DateResult(((DateLiteral)terminal).value()).format(), MAX_LINE_LENGTH) + "</b>";
		else if(terminal instanceof Operator)
			value += "<b>" + formatValue(((Operator)terminal).spell(), MAX_LINE_LENGTH) + "</b>";
	}


	public Object visitTransactionAbortStatement(TransactionAbortStatement stmt, @SuppressWarnings("unused") 	Object attr) throws SBQLException
	{
		visualizeStatement(stmt);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitTryCatchFinallyStatement(odra.sbql.ast.statements.TryCatchFinallyStatement, java.lang.Object)
	 */
	public Object visitTryCatchFinallyStatement(
		TryCatchFinallyStatement stmt, Object attr) throws SBQLException
	{
	    assert false: "try-catch-finally unimplemented";
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitAtMostExpression(odra.sbql.ast.expressions.AtMostExpression, java.lang.Object)
	 */
	public Object visitAtMostExpression(AtMostExpression expr, Object attr)
		throws SBQLException {
	    this.visualizeUnaryExpression(expr);
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitAtLeastExpression(odra.sbql.ast.expressions.AtLeastExpression, java.lang.Object)
	 */
	public Object visitAtLeastExpression(AtLeastExpression expr, Object attr)
		throws SBQLException {
	    this.visualizeUnaryExpression(expr);
	    return null;
	}

	/**
	 * Decorates the node with links.
	 *
	 */
	private void decorateNodeWithLinks()
	{
		StringBuilder str = new StringBuilder();
		if (node.getLinksDecorationCount() > 0)
		{
			str.append("<br/>");
			str.append("<i>at { ");

			for (String s : node.getLinksDecoration())
			{
				str.append(s + "; ");
			}

			str.delete(str.length() - 2, str.length());
			str.append(" }</i>");
			
			value += str;
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

	/**
	 * This method initializes southPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSouthPanel()
	{
		if(southPanel == null)
		{
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setHgap(0);
			flowLayout1.setVgap(0);
			southPanel = new JPanel();
			southPanel.setLayout(flowLayout1);
			southPanel.setBackground(BACKGROUND_COLOR);
			southPanel.add(getChildrenPanel(), null);
		}
		return southPanel;
	}
	
	private void decorateWithBindings(NonAlgebraicExpression node)
	{
		if (node.getEnvsInfo() != null)
		{
			StringBuilder str = new StringBuilder();

			str.append("</br>");
			str.append("<i>");
			str.append("(" + node.getEnvsInfo().baseEnvsSize + "," + node.getEnvsInfo().framesOpened + ")");
			str.append("</i>");

			value += str;
		}
	}

	private void decorateWithBindings(NameExpression node)
	{
		if (node.getBindingInfo()!= null)
		{
			StringBuilder str = new StringBuilder();
			
			str.append("</br>");
			str.append("<i>");
			str.append("(" + node.getBindingInfo().boundat + ")");
			str.append("</i>");

			value += str;
		}
	}

	private void decorateWithBindingLevels()
	{		
		if (node instanceof NameExpression)
		{
			decorateWithBindings((NameExpression) node);
		}
		else if (node instanceof NonAlgebraicExpression)
		{
			decorateWithBindings((NonAlgebraicExpression) node);
		}
	}
	
	public boolean isBindingLevel()
	{
		return bindingLevel;
	}

	public void setBindingLevel(boolean isBindingLevel)
	{
		this.bindingLevel = isBindingLevel;
	}
	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitThrowStatement(odra.sbql.ast.statements.ThrowStatement, java.lang.Object)
	 */
	public Object visitThrowStatement(ThrowStatement stmt, @SuppressWarnings("unused") Object attr)
		throws SBQLException
	{
	    visualizeStatement(stmt);
	    return null;
	}

	public Object visitParallelUnionExpression(ParallelUnionExpression expr, @SuppressWarnings("unused") Object attr) throws SBQLException
	{
		visualizeParallelExpression(expr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitDbLinkDeclaration(odra.sbql.ast.declarations.DbLinkDeclaration, java.lang.Object)
	 */
	public Object visitExternalSchemaDefDeclaration(ExternalSchemaDefDeclaration decl, Object attr)
		throws SBQLException {
	    // TODO Auto-generated method stub
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitDbLinkFieldDeclaration(odra.sbql.ast.declarations.DbLinkFieldDeclaration, java.lang.Object)
	 */
	public Object visitExternalSchemaDefFieldDeclaration(ExternalSchemaDefFieldDeclaration decl,
		Object attr) throws SBQLException {
	    // TODO Auto-generated method stub
	    return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitRangeOfExpression(odra.sbql.ast.expressions.RangeOfExpression, java.lang.Object)
	 */
	@Override
	public Object visitRangeAsExpression(RangeAsExpression expr, Object attr) {
		visualizeUnaryExpression(expr);
		return null;
	}

	/**
	 * Returns the font size.
	 * 
	 * @return font size
	 */
	protected int getFontSize()
	{
		return fontSize;
	}
	
	/**
	 * Cycles the states forward and sets the next one.
	 */
	private void nextState()
	{
		stateIndex++;
		if(stateIndex >= State.values().length)
			stateIndex = 0;
		
		state = State.values()[stateIndex];
	}
	
	/**
	 * Cycles the states backward and sets the next one.
	 */
	private void prevState()
	{
		stateIndex--;
		if(stateIndex < 0)
			stateIndex = State.values().length - 1;
		
		state = State.values()[stateIndex];
	}
	
	/**
	 * Returns the node of this panel.
	 * 
	 * @return {@link ASTNode}
	 */
	protected ASTNode getNode()
	{
		return node;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#getSourceModuleName()
	 */
	public String getSourceModuleName() {
	    return this.modName;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#setSourceModuleName(java.lang.String)
	 */
	public void setSourceModuleName(String name) {
	    this.modName = name;
	    
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitOidExpression(odra.sbql.ast.expressions.OidExpression, java.lang.Object)
	 */
	@Override
	public Object visitSerializeOidExpression(SerializeOidExpression expr, Object attr) {
		visualizeUnaryExpression(expr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitDeserializeOidExpression(odra.sbql.ast.expressions.DeserializeOidExpression, java.lang.Object)
	 */
	@Override
	public Object visitDeserializeOidExpression(DeserializeOidExpression expr,
			Object attr) throws SBQLException {
		visualizeBinaryExpression(expr);
		return null;
	}

	/* (non-Javadoc)
	 * @see odra.sbql.ast.ASTVisitor#visitRenameExpression(odra.sbql.ast.expressions.RenameExpression, java.lang.Object)
	 */
	@Override
	public Object visitRenameExpression(RenameExpression expr, Object attr)
			throws SBQLException {
		visualizeUnaryExpression(expr);
		return null;
	}

	
	
	
}