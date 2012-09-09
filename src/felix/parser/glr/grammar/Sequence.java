package felix.parser.glr.grammar;

import java.util.Arrays;

import felix.parser.glr.parsetree.Element;
import felix.parser.glr.parsetree.Node;

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
	
	public final Mode mode;
	public final Symbol separator;
	public final Symbol item;
	
	public Sequence(String id, Symbol item, Mode mode) {
		this(id, item, mode, null);
	}
	public Sequence(String id, Symbol item, Mode mode, Symbol separator) {
		super(id, buildRules(new SymbolRef(id), item, mode, separator));
		this.item = item;
		this.mode = mode;
		this.separator = separator;
	}
	private static Rule[] optionalRule(Symbol delegate) {
		// X? = Nil | X
		return new Rule[] {Rule.EPSILON, new Rule(delegate)};
	}
	private static Rule[] oneOrMoreRule(Symbol self, Symbol delegate, Symbol separator) {
		if(separator == null) {
			// X+ = X | X X+
			return new Rule[] {
					new Rule(delegate),
					new Rule(self, delegate)
			};
		} else {
			// X+[S] = X | X S X+[S]
			return new Rule[] {
					new Rule(delegate),
					new Rule(self, separator, delegate)
			};
		}
	}
	private static Rule[] zeroOrMoreRule(Symbol self, Symbol delegate, Symbol separator) {
		if(separator == null) {
			// X* = Nil | X X*
			return new Rule[] {
					Rule.EPSILON,
					new Rule(self, delegate)
			};
		} else {
			// X*[S] = Nil | X | X*[S] S X 
			return new Rule[] {
					Rule.EPSILON,
					new Rule(delegate),
					new Rule(self, separator, delegate)
			};
		}
	}
	private static Rule[] buildRules(Symbol self, Symbol delegate, Mode mode, Symbol separator) {
		switch(mode) {
		case ZERO_OR_ONE: 
			if(separator != null)
				throw new IllegalArgumentException("separator is not supported for mode "+mode);
			return optionalRule(delegate);
		case ONE_OR_MORE:
			return oneOrMoreRule(self, delegate, separator);
		case ZERO_OR_MORE:
			return zeroOrMoreRule(self, delegate, separator);
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
	
	/**
	 * Override build so that we flatten out the matches into one element rather than a sort of linked list
	 */
	@Override
	public Node build(Node... nodes) {
		if(nodes.length == 1) {
			if(nodes[0].symbol.equals(Marker.NIL)) {
				return new Element(this, nodes[0].getFileRange());
			} else {
				return super.build(nodes);
			}
		} else {
			Element head = (Element)nodes[0];
			int newLength = head.children.length + nodes.length - 1;
			final Node[] newNodes = Arrays.copyOf(head.children, newLength);
			System.arraycopy(nodes, 1, newNodes, head.children.length, nodes.length-1);
			return super.build(newNodes);
		}
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
