package felix.parser.glr.grammar;


import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import felix.parser.glr.Parser;
import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.Automaton.BuildQueueItem;
import felix.parser.glr.automaton.State;
import felix.parser.glr.grammar.Priority.Requirement;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.ParserReader;

public abstract class Symbol implements Comparable<Symbol> {
	public final String id;
	
	public Symbol(String id) {
		super();
		this.id = id;
	}

	@Override
	public String toString() {
		return id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Symbol other = (Symbol) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(Symbol o) {
		return id.compareTo(o.id);
	}
	
	
	/**
	 * Resolve named references to direct references and report any errors.
	 */
	public abstract void resolveRefs(Automaton automaton);
	
	/**
	 * Compute actions based on the "previous" state and "left" state.
	 * 
	 * @param prevState This is the state the parser would be in when it may encounter this symbol
	 * @param leftState TODO
	 * @param req TODO
	 * @param queue TODO
	 * @param automaton Target to add actions to
	 */
	public abstract void computeActions(State prevState, State leftState, Requirement req, Collection<BuildQueueItem> queue, Automaton automaton);
	
	/**
	 * Attempt to match this symbol against the current parser state.  For a terminal, this checks
	 * whether the next piece of input would match the regular expression.  For a non-terminal,
	 * it just looks at the state stack to decide whether it can reduce the previously matched
	 * tokens.
	 * 
	 * If the match is unsuccessful, the input position is left as it was when the method was called.  If
	 * the parse is successful, the input position is moved to the end of the part of the input that matched.
	 * @param ignored TODO
	 */
	public abstract Node match(ParserReader input, StackHead head, String ignored) throws IOException;

	private Symbol withPriorityRequirement(Requirement req) {
		return new SymbolWithPriorityRequirement(this, req);
	}

	public Symbol gt(Priority pp) {
		return withPriorityRequirement(pp.requireGreaterThan());
	}

	public Symbol ge(Priority pp) {
		return withPriorityRequirement(pp.requireGreaterThanOrEqualTo());
	}
	public Symbol lt(Priority pp) {
		return withPriorityRequirement(pp.requireLessThan());
	}

	public Symbol le(Priority pp) {
		return withPriorityRequirement(pp.requireLessThanOrEqualTo());
	}

	public Symbol eq(Priority pp) {
		return withPriorityRequirement(pp.requireEqualTo());
	}
	
	public boolean compatibleWith(Symbol symbol, Priority priority) {
		return equals(symbol);
	}

	public abstract Collection<Rule> calculateRules(Automaton automaton);

	public abstract boolean isNonTerminal();

	/**
	 * Add all referenced symbols to the given set recursively.
	 */
	public void collectSymbols(TreeSet<Symbol> set) {
		set.add(this);
	}
	
	
	/**
	 * Parse a string using this symbol as the root symbol.  Useful
	 * for simple usages and test cases.
	 */
	public Node parse(String input) throws IOException, ParseException {
		final Set<Terminal> ignore = Collections.<Terminal>emptySet();
		return this.parse(input, ignore);
	}
	
	/**
	 * Parse a string using this symbol as the root symbol.  Useful
	 * for simple usages and test cases.
	 */
	public Node parse(String input, Set<Terminal> ignore) throws IOException, ParseException {
		final Grammar g = new Grammar(this, ignore);
		return Parser.parse(g, input, "<string>");
	}

	public boolean isTerminal() {
		return ! isNonTerminal();
	}
}
