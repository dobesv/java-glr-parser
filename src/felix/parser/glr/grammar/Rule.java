package felix.parser.glr.grammar;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.Automaton.BuildQueueItem;
import felix.parser.glr.automaton.Reduce;
import felix.parser.glr.automaton.State;

public class Rule {
	/** Rule that always matches */
	public static final Rule EPSILON = new Rule();
	
	public final Symbol[] parts;
	public final Priority priority;
	
	public Rule(Priority priority, Symbol... parts) {
		super();
		if(parts.length == 0) parts = new Symbol[]{Marker.NIL};
		this.parts = parts;
		this.priority = priority;
	}
	public Rule(Symbol... parts) {
		this(Priority.DEFAULT, parts);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(parts);
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
		Rule other = (Rule) obj;
		if (!Arrays.equals(parts, other.parts))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for(Symbol sym : parts) {
			if(first) first = false; else sb.append(" ");
			sb.append(sym.id);
		}
		return sb.toString();
	}

	public void resolveRefs(Automaton automaton) {
		for(int i=0; i < parts.length; i++) {
			Symbol sym = parts[i];
			if(sym instanceof SymbolRef) {
				parts[i] = ((SymbolRef)sym).getRealSym(automaton);
			}
		}
	}

	void computeActions(Symbol symbol, final State prevState, State statePrefix, Collection<BuildQueueItem> queue, Automaton automaton) {
		//System.out.println("  "+prevState+" "+statePrefix+" "+this+" => "+symbol.id);
		
		if(parts.length == 0) throw new IllegalStateException(); // Empty rule not allowed, stick NIL in there if necessary
		
		final Reduce reduceAction = new Reduce(symbol, parts, priority);
		
		// When the rule has just a single part / sub-rule, simply issue a reduce
		// after that is matched against our current prefix
		// Pass along the rest to the target rule
		if(parts.length == 1) {
			Symbol part = parts[0];
			queue.add(new BuildQueueItem(part, prevState, statePrefix));
			automaton.addAction(new State(prevState, part), reduceAction);
			return;
		}
		
		// Only the first part of the pattern is relative to the given previous state, the
		// rest should be generic for any use of the same rule
		State partStatePrefix = null;
		//State finalState = null;
		State partPrevState = prevState;
		State jumpPrevState = prevState;
		boolean first = true;
		for(int i=0; i < parts.length; i++) {
			Symbol part = parts[i];
			queue.add(new BuildQueueItem(part, partPrevState, partStatePrefix));
			
			if(first) {
				partPrevState = new State(null, part);
				first = false;
			} else {
				partPrevState = new State(partPrevState, part);
			}
			
			if(part.isTerminal() && !Objects.equals(jumpPrevState, partPrevState)) {
				queue.add(new BuildQueueItem(part, jumpPrevState, partPrevState.left));
				//automaton.addAction(jumpPrevState, new Shift(part, partPrevState, Priority.DEFAULT));
			}
			
			partStatePrefix = new State(partStatePrefix, part);
			jumpPrevState = new State(jumpPrevState, part);
		}
		
		// Add our final reduce action now.
		automaton.addAction(partStatePrefix, reduceAction);
	}

	public void collectSymbols(TreeSet<Symbol> set) {
		for(Symbol part : parts) {
			part.collectSymbols(set);
		}
	}
}