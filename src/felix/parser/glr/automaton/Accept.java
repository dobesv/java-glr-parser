package felix.parser.glr.automaton;

import java.io.IOException;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.grammar.Marker;
import felix.parser.glr.grammar.Priority;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.ParserReader;

public class Accept extends Action {
	public Accept(Priority priority) {
		super(priority);
	}

	public Accept() {
		this(Priority.DEFAULT);
	}

	@Override
	public StackHead apply(StackHead head, ParserReader reader) throws IOException {
		// If we are at the top of the stack
		if(head.left != null && head.left.left != null)
			return null;
		
		// And we should have consumed everything up the end of the input
		final Node token = Marker.END_OF_FILE.match(reader, head);
		if(token == null)
			return null;

		// OK, looks like we parsed everything then!
		return new StackHead(head, State.ACCEPT, head.node, head.priority);
	}

	@Override
	public String toString() {
		return "<end> -> {accept}";
	}
}
