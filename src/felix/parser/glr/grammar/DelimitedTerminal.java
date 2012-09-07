package felix.parser.glr.grammar;

import java.io.IOException;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.parsetree.Node;
import felix.parser.glr.parsetree.Token;
import felix.parser.util.FilePos;
import felix.parser.util.ParserReader;

/**
 * Create a terminal that accepts characters starting at a certain delimiter and ending
 * at another delimiter.
 */
public class DelimitedTerminal extends Terminal {

	private String startDelim;
	private String endDelim;
	private char escapeChar;
	private boolean nestingAllowed;

	/**
	 * 
	 * @param id
	 * @param priority
	 * @param startDelim Prefix to check for
	 * @param endDelim String that ends the token
	 * @param escapeChar Escape character; if this precedes the end delimiter, the end will not be matched.  Use zero for no escaping.
	 * @param nestingAllowed If true, occurrences of the start delimiter will skip over nested pairs of the start/end delimeter in the
	 *                       input.
	 */
	public DelimitedTerminal(String id, Priority priority, String startDelim, String endDelim, char escapeChar, boolean nestingAllowed) {
		super(id, priority);
		this.startDelim = startDelim;
		this.endDelim = endDelim;
		this.escapeChar = escapeChar;
		this.nestingAllowed = nestingAllowed;
	}

	@Override
	public Node match(ParserReader input, StackHead head, String ignored)
			throws IOException {
		if(input.remaining() < startDelim.length()+endDelim.length())
			return null; // Not enough input remaining for even the delimiters
		
		FilePos start = input.getFilePos();
		for(int i=0; i < startDelim.length(); i++) {
			char expected = startDelim.charAt(i);
			final int actual = input.read();
			if(expected != actual) {
				input.seek(start);
				return null;
			}
		}
		StringBuffer text = new StringBuffer();
		text.append(startDelim);
		int endCharsMatched=0;
		int startCharsMatched=0;
		boolean escaped=false;
		int nestDepth=0;
		for(;;) {
			final int ch = input.read();
			text.append((char)ch);
			
			if(escaped) {
				escaped = false;
			} else if(ch == escapeChar) {
				escaped = true;
			} else if(ch == endDelim.charAt(endCharsMatched)) {
				endCharsMatched += 1;
				if(endCharsMatched == endDelim.length()) {
					if(nestDepth == 0) {
						break;
					} else {
						nestDepth -= 1;
						startCharsMatched = 0;
						endCharsMatched = 0;
					}
				}
			} else if(nestingAllowed && ch == startDelim.charAt(startCharsMatched)) {
				startCharsMatched += 1;
				if(startCharsMatched == startDelim.length()) {
					startCharsMatched = 0;
					endCharsMatched = 0;
					nestDepth += 1;
				}
			}
		}
		return new Token(input.getFileRange(start), this, text.toString(), ignored);
	}

}
