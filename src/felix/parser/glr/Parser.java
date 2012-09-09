package felix.parser.glr;


import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.State;
import felix.parser.glr.grammar.Priority;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.FilePos;

public class Parser {
	public static boolean debug;
	
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
	
}
