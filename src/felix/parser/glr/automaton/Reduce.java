package felix.parser.glr.automaton;

import java.util.Arrays;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.grammar.Priority;
import felix.parser.glr.grammar.Symbol;
import felix.parser.glr.parsetree.Node;
import felix.parser.glr.parsetree.Element;
import felix.parser.util.ParserReader;

public class Reduce extends Action {
	public final Symbol symbol;
	public final Symbol[] parts;
	public Reduce(Symbol symbol, Symbol[] parts, Priority priority) {
		super(priority);
		this.symbol = symbol;
		this.parts = parts;
	}
	
	@Override
	public int compareTo(Action o) {
		int cmp = super.compareTo(o);
		if(cmp != 0) return cmp;
		if(!(o instanceof Reduce)) {
			return getClass().getName().compareTo(o.getClass().getName());
		}
		Reduce x = (Reduce)o;
		cmp = symbol.compareTo(x.symbol);
		if(cmp != 0) return cmp;
		cmp = Integer.compare(parts.length, x.parts.length);
		if(cmp != 0) return cmp;
		for(int i=0; i < parts.length; i++) {
			cmp = parts[i].compareTo(x.parts[i]);
			if(cmp != 0) return cmp;
		}
		return 0;
	}
	
	@Override
	public StackHead apply(StackHead head, ParserReader reader, String ignored) {
		Node[] nodes = new Node[parts.length];
		// Match against the nodes on the stack; if we match the whole pattern then we can reduce.
		State state = head.state;
		for(int i=parts.length-1; i >= 0; i--) {
			Symbol sym = parts[i];
			if(head == null) {
				return null; // Not enough nodes available
			}
			Node node = head.node;
			if(!sym.compatibleWith(node.symbol, head.priority)) {
				return null; // Should be the same symbol in the stack as we had in this pattern
			}
			nodes[i] = node;
			head = head.left;
			state = state.left;
		}
		final Node newNode = symbol.build(nodes);
		final State newState = new State(head.state, symbol);
		final StackHead newStack = new StackHead(head, newState, newNode, priority);
		//System.out.println("Reduce "+this+" head.state="+(head==null?null:head.state)+" state="+state+" new stack:\n"+newStack);
		return newStack;
		
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean first = true;
		for(Symbol sym : parts) {
			if(first) first = false; else sb.append(" ");
			sb.append(sym.id);
		}
		sb.append(" => ").append(symbol.id);
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(parts);
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Reduce other = (Reduce) obj;
		if (!Arrays.equals(parts, other.parts))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
	
}