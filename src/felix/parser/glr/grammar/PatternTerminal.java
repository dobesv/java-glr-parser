package felix.parser.glr.grammar;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.automaton.Automaton.BuildQueueItem;
import felix.parser.glr.automaton.Reduce;
import felix.parser.glr.automaton.Shift;
import felix.parser.glr.automaton.State;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.ParserReader;

/**
 * A terminal is not made up of other symbols, but is instead a piece of source
 * text.  It is matched using a regular expression.
 */
public class PatternTerminal extends Terminal {
	public final Pattern re;

	public PatternTerminal(String id, Pattern re, Priority priority) {
		super(id, priority);
		this.re = re;
	}
	
	public PatternTerminal(String id, Pattern re) {
		this(id, re, Priority.DEFAULT);
	}
	
	public PatternTerminal(String id, String pat) {
		this(id, Pattern.compile(pat), Priority.DEFAULT);
	}

	public PatternTerminal(String id, String pat, Priority pri) {
		this(id, Pattern.compile(pat), pri);
	}
	
	@Override
	public Node match(ParserReader input, StackHead head) throws IOException {
		return input.checkNextToken(re, this);
	}
	
	@Override
	public String toString() {
		return id+" : \""+re.pattern()+"\";";
	}
}
