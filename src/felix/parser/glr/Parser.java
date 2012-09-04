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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import felix.parser.glr.automaton.Action;
import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.State;
import felix.parser.glr.grammar.Grammar;
import felix.parser.glr.grammar.Marker;
import felix.parser.glr.grammar.Priority;
import felix.parser.glr.grammar.Terminal;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.FilePos;
import felix.parser.util.ParserReader;

public class Parser {
	public static class StackHead {
		// The parent state we based this action on
		public final StackHead left;
		
		// Current state
		public final State state;
		
		// The parse tree node we generated when we moved into this state
		public final Node node;
		
		// The priority of the rule that produced the node
		public final Priority priority;
		
		// Parse table, rules, and symbols
		public final Automaton automaton;
		
		public StackHead(StackHead left, State state, Node node, Priority priority, Automaton automaton) {
			super();
			this.left = left;
			this.state = state;
			this.node = node;
			this.priority = priority;
			this.automaton = automaton;
		}
		
		public StackHead(StackHead left, State state, Node node, Priority priority) {
			this(left, state, node, priority, left.automaton);
		}

		public StringBuffer toString(StringBuffer buf, int n) {
			if(state == null && node == null && left == null) return buf.append("ROOT");
			if(buf.length() > 0) buf.append("\n");
			buf.append(n).append(": ").append(state==null?State.START_OF_FILE:state).append(" => ").append(node);
			if(left != null) left.toString(buf, n+1);
			return buf;
		}
		
		@Override
		public String toString() {
			if(state == null && node == null && left == null) return "ROOT";
			return toString(new StringBuffer(), 0).toString();
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
	 * @throws ParseException 
	 */
	static Node parse(Grammar grammar, ParserReader input) throws IOException, ParseException {
		Automaton automaton = new Automaton().build(grammar);
		LinkedList<StackHead> stacks = new LinkedList<>();
		stacks.add(new StackHead(null, null, Marker.START_OF_FILE.match(input, null), Priority.DEFAULT, automaton));
		ArrayList<Node> completed = new ArrayList<>();
		try {
			while(!stacks.isEmpty()) {
				StackHead stack = stacks.removeFirst();
				if(stack.state == State.ACCEPT) {
					completed.add(stack.node);
					continue;
				}
				State state = stack.state;
				Set<Action> actions = automaton.getActions(state);
				if(actions == null || actions.isEmpty()) {
					System.out.println("No successor to state "+state);
					// Ran out of steam on this alternative...
					continue;
				}
				
				// Seek to the end of the last token we read
				input.seek(stack.getParsePosition());
				
				// Skip over whitespace and comments
				ignoreTokens(input, grammar.ignore, automaton);
				
				System.out.println("Stack:\n"+stack);
				
				// Compute our next state(s)
				boolean matched = false;
				for(Action action : actions) {
					final StackHead newHead = action.apply(stack, input);
					if(newHead != null) {
						// We have a match!
						matched = true;
						System.out.println(stack.state + " "+action+" -> "+newHead.state+" => "+newHead.node);
						stacks.add(newHead);
					}
				}
				if(!matched) {
					System.out.println(input.getFilePos()+" in state "+stack.state+" nothing matched "+actions);
				}
			}
		} catch(EOFException e) {
			throw new SyntaxError("Passed EOF during parse. (BUG?)", input.getFileRange(input.getFilePos()));
		}
		if(completed.size() == 1) {
			return completed.get(0);
		} else if(completed.size() > 1){
			throw new AmbiguousInputException(completed.toArray(new Node[completed.size()]));
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
	public static Node parse(Grammar grammar, String input, String filename) throws IOException, ParseException {
		return parse(grammar, new ParserReader(new StringReader(input), filename, input.length()));
	}
	
	/**
	 * Parse a file as an input, using the given symbol as the expected format of the input.
	 * 
	 * This buffers the entire file into memory to allow seeking to and to count the number
	 * of characters in the file.
	 */
	public static Node parse(Grammar grammar, File input, String charsetName) throws IOException, ParseException {
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
	public static Node parse(Grammar grammar, String filename, int maxChars, Reader in) throws IOException, ParseException {
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
