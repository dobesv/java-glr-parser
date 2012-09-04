package felix.parser.glr.automaton;

import java.util.Set;

import felix.parser.glr.grammar.Rule;
import felix.parser.glr.grammar.Symbol;

public class Item {
	public final Symbol symbol;
	public final Rule rule;
	public final int position;
	
	public Item(Symbol symbol, Rule rule, int position) {
		super();
		this.symbol = symbol;
		this.rule = rule;
		this.position = position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + position;
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
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
		Item other = (Item) obj;
		if (position != other.position)
			return false;
		if (rule == null) {
			if (other.rule != null)
				return false;
		} else if (!rule.equals(other.rule))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	public boolean hasNextSym() {
		return this.position < rule.parts.length;
	}
	public Symbol nextSym() {
		return rule.parts[this.position];
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(symbol.id).append(" ->");
		for(int i=0; i < rule.parts.length; i++) {
			if(i == position) sb.append(" .");
			else sb.append(" ");
			sb.append(rule.parts[i].id);
		}
		return sb.toString();
	}
}
