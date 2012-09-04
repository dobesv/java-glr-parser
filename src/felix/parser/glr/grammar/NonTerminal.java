package felix.parser.glr.grammar;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.Automaton.BuildQueueItem;
import felix.parser.glr.automaton.State;
import felix.parser.glr.grammar.Priority.Requirement;
import felix.parser.glr.parsetree.Element;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.ParserReader;

public class NonTerminal extends Symbol {

	public final Rule[] rules;

	public NonTerminal(String id, Rule ... rules) {
		super(id);
		this.rules = rules;
	}
	
	@Override
	public void computeActions(final State prevState, State leftState, Requirement req, Collection<BuildQueueItem> queue, Automaton automaton) {
		System.out.println("computeActions ( "+prevState+" -> "+this+" )");
		
		for(Rule r : rules) {
			if(req == null || req.check(r.priority)) {
				r.computeActions(this, prevState, leftState, queue, automaton);
			}
		}
	}
	/**
	 * If the top of the parse stack is a reduction of this non-terminal, that's a match
	 * of this non-terminal.
	 */
	@Override
	public Node match(ParserReader input, StackHead head, String ignored) {
		if(head != null && head.node != null && this.equals(head.node.symbol)) {
			return head.node;
		} else {
			
			return null;
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		sb.append(id).append(" : ");
		for(Rule r : rules) {
			if(first) first = false; else sb.append(" | ");
			sb.append(r);
		}
		sb.append(';');
		return sb.toString();
	}
	
	public Element build(Node ... nodes) {
		return new Element(this, nodes);
	}
	
	@Override
	public void resolveRefs(Automaton automaton) {
		for(Rule r : rules) {
			r.resolveRefs(automaton);
		}
	}
	
	public Collection<Rule> calculateRules(Automaton automaton) {
		return Arrays.asList(rules);
	}
	
	@Override
	public boolean isNonTerminal() {
		return true;
	}
	
	@Override
	public void collectSymbols(TreeSet<Symbol> set) {
		super.collectSymbols(set);
		for(Rule r : rules) {
			r.collectSymbols(set);
		}
	}
}
