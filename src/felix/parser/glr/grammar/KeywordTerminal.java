package felix.parser.glr.grammar;

import java.io.IOException;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.parsetree.Node;
import felix.parser.glr.parsetree.Token;
import felix.parser.util.FilePos;
import felix.parser.util.ParserReader;

/**
 * This matches an exact piece of text (not a regular expression) in the input stream.
 */
public class KeywordTerminal extends Terminal {
	public final String text;

	public KeywordTerminal(String id, String text, Priority priority) {
		super(id, priority);
		this.text = text;
	}
	
	public KeywordTerminal(String id, String text) {
		this(id, text, Priority.DEFAULT);
	}
	
	public KeywordTerminal(String patAndId) {
		this(patAndId, patAndId);
	}
	
	@Override
	public Node match(ParserReader input, StackHead head, String ignored) throws IOException {
		FilePos start = input.getFilePos();
		if(!input.startsWith(text))
			return null;
		return new Token(input.getFileRange(start), this, text, ignored);
	}
	
	@Override
	public String toString() {
		return id+" : '"+text+"';";
	}
}
