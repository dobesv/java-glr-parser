package felix.parser.glr.parsetree;

import java.util.List;

import felix.parser.glr.grammar.Symbol;
import felix.parser.util.FileRange;

public abstract class Node {
	public final Symbol symbol;
	
	public Node(Symbol symbol) {
		super();
		if(symbol == null) throw new NullPointerException();
		this.symbol = symbol;
	}

	/**
	 * The part of the input used to parse this node.
	 */
	public abstract FileRange getFileRange();

	/**
	 * The list of tokens actuallY used to parse this node.  It may include
	 * whitespace and comment tokens.
	 */
	public abstract List<Token> getTokens();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		Node other = (Node) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	
}
