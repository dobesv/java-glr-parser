package felix.parser.glr.grammar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.parsetree.Element;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.ParserReader;

/**
 * Matches some number of repetitions of the given symbol and
 * returns an Element whose children are the matched nodes.
 */
public class Sequence extends NonTerminal {
	public enum Mode { 
		ZERO_OR_ONE('?', true, false),
		ZERO_OR_MORE('*', true, true), 
		ONE_OR_MORE('+', false, true);
		final char ch;
		final boolean matchZeroOccurrences;
		final boolean matchMultipleOccurrences;
		
		private Mode(char ch, boolean matchZero, boolean matchMultipleOccurrences) {
			this.ch = ch;
			this.matchZeroOccurrences = matchZero;
			this.matchMultipleOccurrences = matchMultipleOccurrences;
		}
	};
	
	final Mode mode;
	final Symbol separator;
	public Sequence(String id, Symbol delegate, Mode mode) {
		this(id, delegate, mode, null);
	}
	public Sequence(String id, Symbol delegate, Mode mode, Symbol separator) {
		super(id, buildRules(new SymbolRef(id), delegate, mode, separator));
		this.mode = mode;
		this.separator = separator;
	}
	private static Rule[] optionalRule(Symbol delegate) {
		return new Rule[] {Rule.EPSILON, new Rule(delegate)};
	}
	private static Rule oneOrMoreRule(Symbol self, Symbol delegate, Symbol separator) {
		return new Rule(delegate, 
						optional(separator == null ? self :
									new NestedRule(separator, self)
								));
	}
	private static Rule[] buildRules(Symbol self, Symbol delegate, Mode mode, Symbol separator) {
		switch(mode) {
		case ZERO_OR_ONE: 
			if(separator != null)
				throw new IllegalArgumentException("separator is not supported for mode "+mode);
			return optionalRule(delegate);
		case ONE_OR_MORE:
			return new Rule[] { oneOrMoreRule(self, delegate, separator) };
		case ZERO_OR_MORE:
			return optionalRule(new NestedRule(oneOrMoreRule(self, delegate, separator)));
		default:
			throw new IllegalStateException();
		}
	}
	
	private static String genId(Symbol delegate, Mode mode, Symbol separator) {
		return delegate.id+(separator==null?"":"["+separator.id+"]")+mode.ch;
	}
	public Sequence(Symbol delegate, Mode mode) {
		this(genId(delegate, mode, null), delegate, mode, null);
	}
	public Sequence(Symbol delegate, Mode mode, Symbol separator) {
		this(genId(delegate, mode, separator), delegate, mode, separator);
	}
	
	public static Sequence optional(Symbol s) {
		return new Sequence(s, Mode.ZERO_OR_ONE);
	}
	public static Sequence zeroOrMore(Symbol s) {
		return new Sequence(s, Mode.ZERO_OR_MORE);
	}
	public static Sequence oneOrMore(Symbol s) {
		return new Sequence(s, Mode.ONE_OR_MORE);
	}

	public static Sequence zeroOrMoreSeperatedBy(Symbol element, Symbol separator) {
		return new Sequence(element, Mode.ZERO_OR_MORE, separator);
	}
	public static Sequence oneOrMoreSeperatedBy(Symbol element, Symbol seperator) {
		return new Sequence(element, Mode.ONE_OR_MORE, seperator);
	}
	
	public static Sequence optional(String id, Symbol s) {
		return new Sequence(id, s, Mode.ZERO_OR_ONE, null);
	}
	public static Sequence zeroOrMore(String id, Symbol s) {
		return new Sequence(id, s, Mode.ZERO_OR_MORE, null);
	}
	public static Sequence oneOrMore(String id, Symbol s) {
		return new Sequence(id, s, Mode.ONE_OR_MORE, null);
	}
}
