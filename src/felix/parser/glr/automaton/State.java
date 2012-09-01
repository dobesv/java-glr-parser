package felix.parser.glr.automaton;

import felix.parser.glr.grammar.Marker;
import felix.parser.glr.grammar.Symbol;
import felix.parser.glr.grammar.SymbolWithPriorityRequirement;


public class State implements Comparable<State> {
	public final State left;
	public final Symbol symbol;
	private final int hash;
	public static final State START_OF_FILE = new State(null, Marker.START_OF_FILE);
	public static final State ACCEPT = new State(null, Marker.END_OF_FILE);
	
	public State(State state, Symbol symbol) {
		super();
		this.left = state;
		this.symbol = symbol instanceof SymbolWithPriorityRequirement ? ((SymbolWithPriorityRequirement)symbol).getSymbol() : symbol;
		this.hash = calcHash();
	}
	private int calcHash() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public String toString() {
		return append(new StringBuffer().append("(State: ")).append(')').toString();

	}
	
	public StringBuffer append(StringBuffer sb) {
		if(left != null)
			left.append(sb).append(" ");
		return sb.append(symbol.id);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
	public int compareTo(State other) {
		if(this == other) return 0;
		if(other == null) return -1;
		if(getClass() != other.getClass()) return getClass().getName().compareTo(other.getClass().getName());
		if(left == null) {
			if(other.left != null)
				return 1;
		} else if(other.left == null) {
			return -1;
		} else { 
			int cmp = left.compareTo(other.left);
			if(cmp != 0) return cmp;
		}
		if(symbol == null) {
			if(other.symbol != null)
				return 1;
		} else if(other.symbol == null) {
			return -1;
		} else {
			int cmp = symbol.compareTo(other.symbol);
			if(cmp != 0) return cmp;
		}
		return 0;
	}
}