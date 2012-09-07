package felix.parser.glr.grammar;

import java.util.regex.Pattern;

/**
 * Helper class to make it more readable to define a grammar in code.
 */
public class Symbols {
	public static KeywordTerminal kw(String idAndText) {
		return new KeywordTerminal(idAndText);
	}
	public static KeywordTerminal kw(String id, String pattern) {
		return new KeywordTerminal(id, pattern);
	}
	public static Terminal re(String id, String pattern) {
		return new PatternTerminal(id, pattern);
	}
	public static Terminal re(String id, Pattern pattern) {
		return new PatternTerminal(id, pattern);
	}
	public static Rule rule(Priority p, Symbol ... parts) {
		return new Rule(p, parts);
	}
	public static Rule rule(Symbol ... parts) {
		return rule(Priority.DEFAULT, parts);
	}
	
	/**
	 * Create a non-terminal with multiple production
	 * rules.
	 */
	public static NonTerminal nt(String id, Rule ... rules) {
		return new NonTerminal(id, rules);
	}
	
	/**
	 * Create a non-terminal with a single production rule.
	 */
	public static NonTerminal nt(String id, Symbol ... parts) {
		return nt(id, rule(parts));
	}
	
	/**
	 * Create a nested rule with the given pattern.
	 */
	public static NestedRule nestedRule(Symbol ... parts) {
		return new NestedRule(parts);
	}

	/**
	 * Create a nested rule with the given patterns.
	 */
	public static NestedRule nestedRule(Rule ... rules) {
		return new NestedRule(rules);
	}
	
	/**
	 * Create a nested rule that allows zero or more repetitions
	 * of the same pattern.
	 */
	public static Symbol zeroOrMore(Symbol ... parts) {
		return Sequence.zeroOrMore(maybeNest(parts));
	}

	/**
	 * Create a nested rule that allows zero or more repetitions
	 * of the same nested rules.
	 */
	public static Symbol zeroOrMore(Rule ... rules) {
		return Sequence.zeroOrMore(nestedRule(rules));
	}
	
	/**
	 * Create a nested rule that allows zero or more repetitions
	 * of the same pattern.
	 */
	public static Symbol zeroOrMore(String id, Symbol ... parts) {
		return Sequence.zeroOrMore(id, maybeNest(parts));
	}
	
	/**
	 * Create a nested rule that allows one or more repetitions
	 * of the same pattern.
	 */
	public static Symbol oneOrMore(Symbol ... parts) {
		return Sequence.oneOrMore(maybeNest(parts));
	}

	/**
	 * Create a non-terminal that allows one or more repetitions
	 * of the same pattern.
	 */
	public static Symbol oneOrMore(String id, Symbol ... parts) {
		return Sequence.oneOrMore(id, maybeNest(parts));
	}

	/**
	 * If the given list of symbols is length 1, return
	 * the single element.  Otherwise create a nested
	 * rule.
	 */
	private static Symbol maybeNest(Symbol[] parts) {
		if(parts.length == 1)
			return parts[0];
		return nestedRule(parts);
	}
	
	/**
	 * Create a non-terminal rule that allows the pattern to
	 * match optionally (i.e. zero or one matches).
	 */
	public static Symbol opt(String id, Symbol ... parts) {
		return Sequence.optional(id, maybeNest(parts));
	}
	/**
	 * Create a nested rule that allows the pattern to
	 * match optionally (i.e. zero or one matches).
	 */
	public static Symbol opt(Symbol ... parts) {
		return Sequence.optional(maybeNest(parts));
	}
	
	/**
	 * Parse a list of elements separated by a particular symbol.
	 */
	public static Symbol oneOrMoreSeparatedBy(Symbol element, Symbol separator) {
		return Sequence.oneOrMoreSeperatedBy(element, separator);
	}

	/**
	 * Parse a list of elements separated by a particular symbol.
	 */
	public static Symbol zeroOrMoreSeparatedBy(Symbol element, Symbol separator) {
		return Sequence.zeroOrMoreSeperatedBy(element, separator);
	}
	
	public static Symbol optOneOf(Symbol ... alternatives) {
		if(alternatives.length == 1)
			return opt(alternatives);
		Rule[] rules = new Rule[alternatives.length];
		for(int i=0; i < rules.length; i++) {
			rules[i] = rule(alternatives[i]);
		}
		return opt(nestedRule(rules));
	}
	
	/**
	 * Forward reference to a symbol not yet defined.
	 */
	public static SymbolRef ref(String name) {
		return new SymbolRef(name);
	}
}
