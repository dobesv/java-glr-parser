package felix.parser.glr.grammar;

import java.util.Collection;

import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.Automaton.BuildQueueItem;
import felix.parser.glr.automaton.Shift;
import felix.parser.glr.automaton.State;
import felix.parser.glr.grammar.Priority.Requirement;
import felix.parser.glr.parsetree.Token;
import felix.parser.util.FileRange;


public abstract class Terminal extends Symbol {
	final Priority priority;
	
	public Terminal(String id, Priority priority) {
		super(id);
		this.priority = priority;
	}
	
	public Terminal(String id) {
		this(id, Priority.DEFAULT);
	}

	@Override
	public void computeActions(State prevState, State leftState, Requirement req, Collection<BuildQueueItem> queue, Automaton automaton) {
		if(req == null || req.check(priority)) {
			automaton.addAction(prevState, new Shift(new State(leftState,this), this, priority));
		}
	}
	
	public Token build(FileRange fileRange, String text) {
		return new Token(fileRange, this, text);
	}
	
	@Override
	public void resolveRefs(Automaton automaton) {
	}
}
