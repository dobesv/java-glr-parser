package felix.parser.glr.grammar;

import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.Set;
import java.util.TreeSet;

import felix.parser.glr.Parser;
import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.ParserReader;

public class Grammar {
	public final Set<Symbol> symbols;
	public final Symbol root;
	public final Set<Terminal> ignore;
	
	public Grammar(Set<Symbol> symbols, Symbol root, Set<Terminal> ignore) {
		super();
		this.symbols = symbols;
		this.root = root;
		this.ignore = ignore;
	}
	
	public static TreeSet<Symbol> buildSymbolSet(Symbol root, Set<Terminal> ignore) {
		TreeSet<Symbol> symbolSet = new TreeSet<>();
		symbolSet.addAll(ignore);
		root.collectSymbols(symbolSet);
		return symbolSet;
	}
	public Grammar(Symbol root, Set<Terminal> ignore) {
		this(buildSymbolSet(root,ignore), root, ignore);
	}

	/**
	 * Parse using the given symbol as the "root" symbol.  This symbol should
	 * match the entire input given by the reader.
	 * @param symbols TODO
	 * @param input TODO
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public Node parse(ParserReader input) throws IOException, ParseException {
		Automaton automaton = new Automaton().build(this);
		if(Parser.debug) System.out.println(automaton);
		return automaton.parse(input);
	}

	/**
	 * Parse a string as an input, using the given symbol as the expected format of the input.
	 * @param input TODO
	 * @param filename TODO
	 */
	public Node parse(String input, String filename) throws IOException, ParseException {
		return parse(new ParserReader(new StringReader(input), filename, input.length()));
	}

	/**
	 * Parse a file as an input, using the given symbol as the expected format of the input.
	 * 
	 * This buffers the entire file into memory to allow seeking to and to count the number
	 * of characters in the file.
	 * @param input TODO
	 * @param charsetName TODO
	 */
	public Node parse(File input, String charsetName) throws IOException, ParseException {
		Reader in = new InputStreamReader(new FileInputStream(input), charsetName);
		return parse(input.getPath(), (int) input.length(), in);
	}

	/**
	 * Parse from a reader.  The reader is first buffered fully into memory to count characters and to allow
	 * seeking.  After reading, the provided reader is closed.
	 * 
	 * @param filename TODO
	 * @param maxChars The maximum expected number of characters in the file; used to allocate a buffer to contain
	 *                 the file.  If the file has more characters than expected, the remaining characters will be
	 *                 ignored.  The number of bytes in a file may be OK for this as long as the file is exected
	 *                 to have fewer characters than bytes (i.e. for UTF-8 or any 8-bit encoding).
	 * @param in TODO
	 */
	public Node parse(String filename, int maxChars, Reader in) throws IOException, ParseException {
		CharBuffer buf = CharBuffer.allocate(maxChars);
		try {
			while(in.read(buf) > 0) {
				// Work done in the conditional here...
			}
		} finally {
			in.close();
		}
		buf.flip();
		return parse(new ParserReader(new CharArrayReader(buf.array()), filename, buf.remaining()));
	}
	
}
