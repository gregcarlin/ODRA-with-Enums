package odra.cli.gui.editor;

import java.awt.Color;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * 
 * @author raist
 */

public class SBQLDocument extends DefaultStyledDocument {
	private Element rootElement;

	private HashMap<String, Color> keywords;

	private MutableAttributeSet style;

	private Color keywordColor = new Color(24, 27, 120);
	private Color commentColor = new Color(35, 110, 37);
	private Color stringColor = new Color(137, 19, 21);
	
	private Pattern singleLineCommentDelimter = Pattern.compile("//");
	private Pattern multiLineCommentDelimiterStart = Pattern.compile("/\\*");
	private Pattern multiLineCommentDelimiterEnd = Pattern.compile("\\*/");

	public SBQLDocument() {
		putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

		rootElement = getDefaultRootElement();
		style = new SimpleAttributeSet();
		
		keywords = new HashMap<String, Color>();
		for (String k : keys)
			keywords.put(k, keywordColor);
	}

	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		super.insertString(offset, str, attr);
		processChangedLines(offset, str.length());
	}

	public void remove(int offset, int length) throws BadLocationException {
		super.remove(offset, length);
		processChangedLines(offset, length);
	}

	private void processChangedLines(int offset, int length) throws BadLocationException {
		String text = getText(0, getLength());
		highlightString(Color.black, 0, getLength(), true, false);

		Set<String> keyw = keywords.keySet();

		for (String keyword : keyw) {
			Color col = keywords.get(keyword);

			Pattern p = Pattern.compile("\\b" + keyword + "\\b");
			Matcher m = p.matcher(text);

			while (m.find()) 
				highlightString(col, m.start(), keyword.length(), true, true);
		}

		Matcher mlcStart = multiLineCommentDelimiterStart.matcher(text);
		Matcher mlcEnd = multiLineCommentDelimiterEnd.matcher(text);

		while (mlcStart.find()) {
			if (mlcEnd.find(mlcStart.end()))
				highlightString(commentColor, mlcStart.start(), (mlcEnd.end() - mlcStart.start()), true, true);
			else
				highlightString(commentColor, mlcStart.start(), getLength(), true, true);
		}

		Matcher slc = singleLineCommentDelimter.matcher(text);

		while (slc.find()) {
			int line = rootElement.getElementIndex(slc.start());
			int endOffset = rootElement.getElement(line).getEndOffset() - 1;

			highlightString(commentColor, slc.start(), (endOffset - slc.start()), true, true);
		}

		String newline = System.getProperty("line.separator");
		int inside = -1;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\"' && inside == -1)
				inside = i;
			else if (text.charAt(i) == '\"' && inside != -1) {
				highlightString(stringColor, inside, i - inside + 1, true, true);

				inside = -1;
			}
			else if (text.indexOf(newline, i) == i)
				inside = -1;
		}
	}

	private void highlightString(Color col, int begin, int length, boolean flag, boolean bold) {
		StyleConstants.setForeground(style, col);
		StyleConstants.setBold(style, bold);

		setCharacterAttributes(begin, length, style, flag);
	}
	
	private String[] keys = {
			"abstract",
			"intersect",
			"not",
			"and",		
			"or",		
			"where",		
			"join",		
			"groupas",	
			"as",	
			"forall",	
			"forsome",	
			"minus",		
			"union",	
			"intersect",	
			"count",	
			"in",		
			"orderby",
			"avg",		
			"min",		
			"max",		
			"unique",	
			"exists",	
			"true",		
			"false",		
			"sum",		
			"module",	
			"import",	
			"record",	
			"type",		
			"is",		
			"return",	
			"deref",	
			"do",		
			"view",		
			"virtual",	
			"objects",	
			"on_retrieve",
			"on_update", 
			"on_delete",	
			"on_new",	
			"on_navigate",	
			"foreach",
			"else",		
			"if",		
			"external", 
			"bag",
			"struct",
			"integer",
			"string",
			"real",
			"boolean"
	};	
}
