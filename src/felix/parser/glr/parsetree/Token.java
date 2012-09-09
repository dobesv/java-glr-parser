package felix.parser.glr.parsetree;

import java.util.Collections;
import java.util.List;

import felix.parser.glr.grammar.Symbol;
import felix.parser.util.FilePos;
import felix.parser.util.FileRange;

public class Token extends Node {
	private final String text;
	private final FileRange fileRange;
	private final String ignoredPrefix;
	
	public Token(FileRange fileRange, Symbol symbol, String text, String ignoredPrefix) {
		super(symbol);
		if(fileRange == null) throw new NullPointerException();
		if(text == null) throw new NullPointerException();
		if(text.length() != fileRange.length()) throw new IllegalStateException();
		this.fileRange = fileRange;
		this.text = text;
		this.ignoredPrefix = ignoredPrefix;
	}
	
	public Token(FileRange fileRange, Symbol symbol, String text) {
		this(fileRange, symbol, text, "");
	}

	@Override
	public FileRange getFileRange() {
		return fileRange;
	}
	
	@Override
	public String toString() {
		return ((getText().equals(symbol.id) || getText().isEmpty()) ? symbol.id :
		 symbol.id+"("+getText().replace("\n", "\\n").replace("\r", "\\r")+")"); //+"@("+fileRange+")";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((getFileRange() == null) ? 0 : getFileRange().hashCode());
		result = prime * result + ((getText() == null) ? 0 : getText().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		if (!getFileRange().equals(other.getFileRange()))
			return false;
		if (!getText().equals(other.getText()))
			return false;
		return true;
	}

	@Override
	public List<Token> getTokens() {
		return Collections.singletonList(this);
	}

	public FilePos getEndPos() {
		return getFileRange().getEnd();
	}

	public String getText() {
		return text;
	}

	/**
	 * Any text that was consumed immediately before this token but not used as part of parsing.  Typically
	 * this is whitespace and comments.
	 */
	public String getIgnoredPrefix() {
		return ignoredPrefix;
	}

	@Override
	public Node getChild(int position) {
		throw new IndexOutOfBoundsException();
	}
	
	@Override
	public int getChildCount() {
		return 0;
	}
	
}
