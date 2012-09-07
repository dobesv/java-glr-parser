package felix.parser.glr.automaton;

import java.io.IOException;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.grammar.Priority;
import felix.parser.glr.grammar.Symbol;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.ParserReader;

public class Shift extends Action {
	public final State state;
	public final Symbol symbol;
	
	public Shift(State state, Symbol symbol, Priority priority) {
		this(symbol, state, priority);
	}

	public Shift(Symbol symbol, State state, Priority priority) {
		super(priority);
		this.state = state;
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return "{"+symbol.id+" --> "+state+"}";
	}
	
	@Override
	public StackHead apply(StackHead head, ParserReader reader, String ignored) throws IOException {
		Node node = symbol.match(reader, head, ignored);
		if(node != null)
			return new StackHead(head, this.state, node, priority);
		else
			return null;
	}

	@Override
	public int compareTo(Action o) {
		int cmp = super.compareTo(o);
		if(cmp != 0) return cmp;
		if(!(o instanceof Shift)) {
			return getClass().getName().compareTo(o.getClass().getName());
		}
		return state.compareTo(((Shift)o).state);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		Shift other = (Shift) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
	
}