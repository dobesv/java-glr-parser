package felix.parser.grammar;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import felix.parser.util.ParserReader;

public class FelixString {
	// Regular expression version:
	// http://code.activestate.com/recipes/475109-regular-expression-for-python-string-literals/
	static final int S = '\'';
	static final int D = '"';
	static final int ESCAPE = '\\';
	
	public final String contents;
	public final int quote;
	public boolean triple;
	
	public FelixString(String contents, int quote, boolean triple) {
		super();
		this.contents = contents;
		this.quote = quote;
		this.triple = triple;
	}

	static HashMap<Character,Character> specialCharMap = new HashMap<Character,Character>() {
		private static final long serialVersionUID = -1333536331963507839L;
		{
			put('n', '\n');
			put('r', '\r');
			put('t', '\t');
		};
	};
	static int mapSpecialChar(int ch) {
		switch(ch) {
		case 'n': return '\n';
		case 'r': return '\r';
		case 't': return '\t';
		case '\\': 
		case '\'':
		case '"': return ch;
		default: return ch; // TODO Report invalid escapes, maybe?
		}
	}
	public static FelixString tryParse(ParserReader in) throws IOException {
		in.mark(1); // Prepare to roll back if we don't see a quote character. 
		int quote = in.read();
		boolean isQuote = quote == S || quote == D;
		if(!isQuote) {
			in.reset();
			return null; // Not a string
		}
		
		StringBuffer buf = new StringBuffer(1000);
		boolean triple = checkTriple(in, quote);
		for(;;) {
			int ch = in.read();
			if(ch == -1) throw new EOFException("Reached EOF parsing string");
			if(ch == quote && (!triple || checkTriple(in, quote))) {
				return new FelixString(buf.toString(), quote, triple);
			} else if(ch == ESCAPE) {
				buf.append(mapSpecialChar(in.read()));
			}
		}
		
	}

	/**
	 * Returns true if the next two characters are the same quote character we just got (passed in).
	 * 
	 * The position of the stream will be after the two quote characters if this returns true; otherwise
	 * this is reset to the same position where it started.
	 */
	private static boolean checkTriple(Reader in, int quote) throws IOException {
		in.mark(2);
		boolean triple = (in.read() == quote && in.read() == quote);
		if(!triple) in.reset();
		return triple;
	}
}
