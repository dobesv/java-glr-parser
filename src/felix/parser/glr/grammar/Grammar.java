package felix.parser.glr.grammar;

import java.util.Set;
import java.util.TreeSet;

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
	
}
