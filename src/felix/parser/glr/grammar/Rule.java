package felix.parser.glr.grammar;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.Automaton.BuildQueueItem;
import felix.parser.glr.automaton.Reduce;
import felix.parser.glr.automaton.Shift;
import felix.parser.glr.automaton.State;

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
		
		// For a rule like A B C D => S
		// And a prefix X Y Z
		// Actions:
		//   X Y Z, A => Shift (A)
		//   X Y Z A, B => Shift (A B)
		//   X Y Z A B, C => Shift (A B C)
		//   X Y Z A B C, D => Reduce (A B C D => S) 
		
		
		// Only the first part of the pattern is relative to the given previous state, the
		// rest should be generic for any use of the same rule
		State partStatePrefix = statePrefix;
		State partPrevState = prevState;
		State finalState = statePrefix;
		boolean first = true;
		for(int i=0; i < parts.length; i++) {
			Symbol part = parts[i];
			queue.add(new BuildQueueItem(part, partPrevState, partStatePrefix));
			
			if(first) {
				partStatePrefix = new State(null, part);
				partPrevState = new State(null, part);
				first = false;
			} else {
				partStatePrefix = new State(partStatePrefix, part);
				partPrevState = new State(partPrevState, part);
				
				if(!Objects.equals(partStatePrefix, finalState)) {
					automaton.addAction(finalState, new Shift(partStatePrefix, part, priority));
				}
			}
			
			finalState = new State(finalState, part);
			
		}
		
		// Add our action now.
		automaton.addAction(finalState, new Reduce(symbol, parts, priority));
	}

	public void collectSymbols(TreeSet<Symbol> set) {
		for(Symbol part : parts) {
			part.collectSymbols(set);
		}
	}
}