package felix.parser.glr.grammar;

import java.util.Collection;
import java.util.Collections;

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
	public void computeActions(State prevState, State statePrefix, Requirement req, Collection<BuildQueueItem> queue, Automaton automaton) {
		if(req == null || req.check(priority)) {
			automaton.addAction(prevState, new Shift(this, new State(statePrefix,this), priority));
		}
	}
	
	public Token build(FileRange fileRange, String text, String ignored) {
		return new Token(fileRange, this, text, ignored);
	}
	
	@Override
	public void resolveRefs(Automaton automaton) {
	}
	
	@Override
	public Collection<Rule> calculateRules(Automaton automaton) {
		return Collections.singleton(new Rule(this.priority, this));
	}
	
	@Override
	public boolean isNonTerminal() {
		return false;
	}
}
