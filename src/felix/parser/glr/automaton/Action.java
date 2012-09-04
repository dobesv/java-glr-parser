package felix.parser.glr.automaton;

import java.io.IOException;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.grammar.Priority;
import felix.parser.util.ParserReader;

public abstract class Action implements Comparable<Action> {
	final Priority priority;
	
	@Override
	public int compareTo(Action o) {
		return priority.compareTo(o.priority);
	}

	public abstract StackHead apply(StackHead head, ParserReader reader) throws IOException;

	public Action(Priority priority) {
		super();
		this.priority = priority;
	}
}