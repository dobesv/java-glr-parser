package felix.parser.glr.parsetree;

import java.util.Collections;
import java.util.List;

import felix.parser.glr.grammar.Symbol;
import felix.parser.util.FilePos;
import felix.parser.util.FileRange;

public class Token extends Node {
	public final String text;
	public final FileRange fileRange;
	
	public Token(FileRange fileRange, Symbol symbol, String text) {
		super(symbol);
		this.fileRange = fileRange;
		this.text = text;
	}

	@Override
	public FileRange getFileRange() {
		return fileRange;
	}
	
	@Override
	public String toString() {
		return ((text.equals(symbol.id) || text.isEmpty()) ? symbol.id : // +"@"+fileRange.start.offset;
		 symbol.id+"("+text.replace("\n", "\\n").replace("\r", "\\r")+")"); //+"@"+fileRange.start.offset;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((fileRange == null) ? 0 : fileRange.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		if (fileRange == null) {
			if (other.fileRange != null)
				return false;
		} else if (!fileRange.equals(other.fileRange))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
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

	
	
}
