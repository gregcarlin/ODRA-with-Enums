package odra.cli.gui.editor;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

/**
 * 
 * @author raist
 */

public class LineNumber extends JComponent {
	private final static Color DEFAULT_BACKGROUND = new Color(240, 240, 240);
	private final static Color DEFAULT_FOREGROUND = new Color(100, 100, 100);
	private final static Font DEFAULT_FONT = new Font("helvetica", Font.PLAIN, 9);
	private final static int HEIGHT = Integer.MAX_VALUE - 1000000;
	private final static int RIGHTMARGIN = 5;
	private final static int LEFTMARGIN = 13;

	private FontMetrics fontMetrics;
	private int lineHeight;
	private int currentDigits;
	private JComponent component;
	private int componentFontHeight;
	private int componentFontAscent;
	private int parentComponentFontHeight = 12;
	private int parentComponentFontAscent;
	private int parentFontHeight;

	public LineNumber(JComponent component) {
		if (component == null) {
			this.component = this;
		}
		else {
			this.component = component;
		}

		super.setFont(DEFAULT_FONT);

		fontMetrics = getFontMetrics(component.getFont());
		componentFontHeight = fontMetrics.getHeight();

		fontMetrics = getFontMetrics(getFont());
		componentFontAscent = fontMetrics.getAscent();

		setBackground(DEFAULT_BACKGROUND);
		setForeground(DEFAULT_FOREGROUND);
		setPreferredWidth(99);
	}

	public void setPreferredWidth(int lines) {
		int digits = String.valueOf(lines).length();

		if (digits != currentDigits && digits > 1) {
			currentDigits = digits;
			int width = fontMetrics.charWidth('0') * digits;
			Dimension d = getPreferredSize();
			d.setSize(LEFTMARGIN + RIGHTMARGIN + width, HEIGHT);
			setPreferredSize(d);
			setSize(d);
		}
	}

	public int getLineHeight() {
		if (lineHeight == 0)
			return componentFontHeight;
		else
			return lineHeight;
	}

	public void setLineHeight(int lineHeight) {
		if (lineHeight > 0)
			this.lineHeight = lineHeight;
	}

	public int getStartOffset() {
		return component.getInsets().top + componentFontAscent;
	}

	public void paintComponent(Graphics g) {
		int lineHeight = getLineHeight();
		int startOffset = getStartOffset();
		Rectangle drawHere = g.getClipBounds();

		g.setColor(getBackground());
		g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

		g.setColor(getForeground());
		int startLineNumber = (drawHere.y / lineHeight) + 1;
		int endLineNumber = startLineNumber + (drawHere.height / lineHeight);

		int start = (drawHere.y / lineHeight) * lineHeight + startOffset + 2;

		for (int i = startLineNumber; i <= endLineNumber; i++) {
			String lineNumber = String.valueOf(i);
			int stringWidth = fontMetrics.stringWidth(lineNumber);
			int rowWidth = getSize().width;
			g.drawString(lineNumber, rowWidth - stringWidth - RIGHTMARGIN, start);
			start += lineHeight;
		}

		int rows = component.getSize().height / componentFontHeight;
		setPreferredWidth(rows);
	}
}
