package felix.parser.glr.grammar;

import java.io.IOException;
import java.util.Collection;

import felix.parser.glr.Parser.StackEntry;
import felix.parser.glr.automaton.Automaton;
import felix.parser.glr.automaton.Automaton.BuildQueueItem;
import felix.parser.glr.automaton.State;
import felix.parser.glr.grammar.Priority.Requirement;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.ParserReader;

public class SymbolWithPriorityRequirement extends Symbol {
	public Symbol symbol;
	final Priority.Requirement req;
	
	public SymbolWithPriorityRequirement(Symbol symbol, Priority.Requirement req) {
		super(symbol.id+"["+req+"]");
		this.symbol = symbol;
		this.req = req;
	}

	@Override
	public void computeActions(State prevState, State leftState,
			Requirement req, Collection<BuildQueueItem> queue, Automaton automaton) {
		if(req != null) throw new IllegalStateException();
		symbol.computeActions(prevState, leftState, req, queue, automaton);
	}

	@Override
	public Node match(ParserReader input, StackEntry head)
			throws IOException {
		final Node node = symbol.match(input, head);
		return node;
	}

	@Override
	public void resolveRefs(Automaton automaton) {
		if(symbol instanceof SymbolRef) {
			symbol = ((SymbolRef) symbol).getRealSym(automaton);
		}
	}
	
	public Symbol getSymbol() {
		return symbol;
	}

	public Priority.Requirement getReq() {
		return req;
	}
	
	public boolean compatibleWith(Symbol other, Priority priority) {
		final boolean ok = symbol.equals(other) && req.check(priority);
		System.out.println("Checking "+other.id+" == "+symbol.id+" && "+priority+" matches "+req+" : "+ok);
		return ok;
	}
}
