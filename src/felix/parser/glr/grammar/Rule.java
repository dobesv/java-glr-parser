package felix.parser.glr.grammar;

import java.util.Arrays;
import java.util.Collection;

import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.Reduce;
import felix.parser.glr.automaton.State;
import felix.parser.glr.automaton.Automaton.BuildQueueItem;

public class Rule {
	public final Symbol[] parts;
	public final Priority priority;
	
	public Rule(Priority priority, Symbol... parts) {
		super();
		this.parts = parts;
		this.priority = priority;
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
		System.out.println("  "+prevState+" "+statePrefix+" "+this+" => "+symbol.id);
		
		// Only the first part of the pattern is relative to the given previous state, the
		// rest should be generic for any use of the same rule
		State partStatePrefix = statePrefix;
		State partPrevState = prevState;
		State finalState = statePrefix;
		boolean first = true;
		for(Symbol part : parts) {
			queue.add(new BuildQueueItem(part, partPrevState, partStatePrefix));
			finalState = new State(partStatePrefix, part);
			if(first) {
				partStatePrefix = new State(null, part);
				partPrevState = new State(null, part);
				first = false;
			} else {
				partStatePrefix = new State(partStatePrefix, part);
				partPrevState = new State(partPrevState, part);
			}
		}
		
		// Add our action now.
		automaton.addAction(finalState, new Reduce(symbol, parts, priority));
	}
}