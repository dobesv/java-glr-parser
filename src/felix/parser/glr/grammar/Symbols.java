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
	public static NonTerminal nt(String id, Rule ... rules) {
		return new NonTerminal(id, rules);
	}
	public static NonTerminal nt(String id, Symbol ... parts) {
		return nt(id, rule(parts));
	}
	

}
