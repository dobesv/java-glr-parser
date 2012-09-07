package felix.parser.glr.grammar;

import java.util.Collection;

import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.Automaton.BuildQueueItem;
import felix.parser.glr.automaton.State;
import felix.parser.glr.grammar.Priority.Requirement;

public abstract class SymbolWrapper extends Symbol {
	Symbol delegate;

	public SymbolWrapper(String id, Symbol delegate) {
		super(id);
		this.delegate = delegate;
	}
	@Override
	public void resolveRefs(Automaton automaton) {
		if(delegate instanceof SymbolRef) {
			delegate = ((SymbolRef) delegate).getRealSym(automaton);
		}
	}

	@Override
	public void computeActions(State prevState, State leftState,
			Requirement req, Collection<BuildQueueItem> queue,
			Automaton automaton) {
		delegate.computeActions(prevState, leftState, req, queue, automaton);
	}

	
}
