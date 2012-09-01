package felix.parser.glr;

import java.io.CharArrayReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import felix.parser.glr.automaton.Action;
import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.State;
import felix.parser.glr.grammar.Grammar;
import felix.parser.glr.grammar.Marker;
import felix.parser.glr.grammar.PatternTerminal;
import felix.parser.glr.grammar.Priority;
import felix.parser.glr.grammar.Terminal;
import felix.parser.glr.parsetree.Node;
import felix.parser.glr.parsetree.Token;
import felix.parser.util.FilePos;
import felix.parser.util.ParserReader;

public class Parser {
	public static class StackEntry {
		// The parent state we based this action on
		public final StackEntry left;
		
		// Current state
		public final State state;
		
		// The parse tree node we generated when we moved into this state
		public final Node node;
		
		// The priority of the rule that produced the node
		public final Priority priority;
		
		// Parse table, rules, and symbols
		public final Automaton automaton;
		
		public StackEntry(StackEntry left, State state, Node node, Priority priority, Automaton automaton) {
			super();
			this.left = left;
			this.state = state;
			this.node = node;
			this.priority = priority;
			this.automaton = automaton;
		}
		
		public StackEntry(StackEntry left, State state, Node node, Priority priority) {
			this(left, state, node, priority, left.automaton);
		}

		@Override
		public String toString() {
			if(state == null && node == null) return "ROOT";
			return left+" "+state+" => "+node;
		}

		public FilePos getParsePosition() {
			return node == null ? FilePos.START : node.getFileRange().getEnd();
		}
	}
	
	/**
	 * Parse using the given symbol as the "root" symbol.  This symbol should
	 * match the entire input given by the reader.
	 * @param symbols TODO
	 * @throws IOException 
	 * @throws SyntaxError 
	 */
	static Node parse(Grammar grammar, ParserReader input) throws IOException, SyntaxError {
		Automaton automaton = new Automaton().build(grammar);
		LinkedList<StackEntry> heads = new LinkedList<>();
		heads.add(new StackEntry(null, null, Marker.START_OF_FILE.match(input, null), Priority.DEFAULT, automaton));
		ArrayList<StackEntry> completed = new ArrayList<>();
		try {
			while(!heads.isEmpty()) {
				StackEntry head = heads.removeFirst();
				if(head.state == State.ACCEPT) {
					System.out.println("Complete parse: "+head.node);
					completed.add(head);
					continue;
				}
				State state = head.state;
				Set<Action> actions = automaton.getActions(state);
				if(actions == null || actions.isEmpty()) {
					System.out.println("No successor to state "+state);
					// Ran out of steam on this alternative...
					continue;
				}
				
				// Seek to the end of the last token we read
				input.seek(head.getParsePosition());
				
				// Skip over whitespace and comments
				ignoreTokens(input, grammar.ignore, automaton);
				
				// Compute our next state(s)
				boolean matched = false;
				for(Action action : actions) {
					final StackEntry newHead = action.apply(head, input);
					if(newHead != null) {
						// We have a match!
						matched = true;
						System.out.println(head.state + " "+action+" -> "+newHead.state+" => "+newHead.node);
						heads.add(newHead);
					}
				}
				if(!matched) {
					System.out.println(input.getFilePos()+" in state "+head.state+" nothing matched "+actions);
				}
			}
		} catch(EOFException e) {
			throw new SyntaxError("Passed EOF during parse. (BUG?)", input.getFileRange(input.getFilePos()));
		}
		if(completed.size() == 1) {
			return completed.get(0).node;
		} else if(completed.size() > 1){
			throw new SyntaxError("Ambiguous parse.", input.getFileRange(input.getFilePos()));
		} else {
			// No successful parses
			throw new SyntaxError("Failed to parse", input.getFileRange(input.getFilePos()));
		}
			
	}

	/**
	 * Advance the given token in the stream past any whitespace or comment type
	 * tokens (the "skip" tokens).
	 * @param automaton TODO
	 */
	static void ignoreTokens(ParserReader reader, Set<Terminal> ignore, Automaton automaton) throws IOException {
		FilePos startPos = reader.getFilePos();
		for(;;) {
			for(Terminal term : ignore) {
				term.match(reader, null);
			}
			FilePos endPos = reader.getFilePos();
			if(endPos.equals(startPos)) // No forward movement, we're done
				break;
			startPos = endPos;
		}
	}

	/**
	 * Parse a string as an input, using the given symbol as the expected format of the input.
	 */
	public static Node parse(Grammar grammar, String input, String filename) throws IOException, SyntaxError {
		return parse(grammar, new ParserReader(new StringReader(input), filename, input.length()));
	}
	
	/**
	 * Parse a file as an input, using the given symbol as the expected format of the input.
	 * 
	 * This buffers the entire file into memory to allow seeking to and to count the number
	 * of characters in the file.
	 */
	public static Node parse(Grammar grammar, File input, String charsetName) throws IOException, SyntaxError {
		Reader in = new InputStreamReader(new FileInputStream(input), charsetName);
		return parse(grammar, input.getPath(), (int) input.length(), in);
	}

	/**
	 * Parse from a reader.  The reader is first buffered fully into memory to count characters and to allow
	 * seeking.  After reading, the provided reader is closed.
	 * 
	 * @param maxChars The maximum expected number of characters in the file; used to allocate a buffer to contain
	 *                 the file.  If the file has more characters than expected, the remaining characters will be
	 *                 ignored.  The number of bytes in a file may be OK for this as long as the file is exected
	 *                 to have fewer characters than bytes (i.e. for UTF-8 or any 8-bit encoding).
	 */
	public static Node parse(Grammar grammar, String filename, int maxChars, Reader in) throws IOException, SyntaxError {
		CharBuffer buf = CharBuffer.allocate(maxChars);
		try {
			while(in.read(buf) > 0) {
				// Work done in the conditional here...
			}
		} finally {
			in.close();
		}
		buf.flip();
		return parse(grammar, new ParserReader(new CharArrayReader(buf.array()), filename, buf.remaining()));
	}
	
}
