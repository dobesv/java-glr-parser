package felix.parser.glr.grammar;

import java.util.Set;

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
	
}
