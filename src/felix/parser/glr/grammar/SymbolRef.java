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

/**
 * Class to allow cycles to be created in the tree; this delegates all
 * operations to the "canonical" symbol of the same name.
 * @author dobes
 *
 */
public class SymbolRef extends Symbol {
	public SymbolRef(String id) {
		super(id);
	}

	@Override
	public void computeActions(State prevState, State leftState, Requirement req, Collection<BuildQueueItem> queue, Automaton automaton) {
		Symbol realSym = getRealSym(automaton);
		realSym.computeActions(prevState, leftState, null, queue, automaton);
	}
	
	@Override
	public Node match(ParserReader input, StackEntry head)
			throws IOException {
		Symbol realSym = getRealSym(head.automaton);
		return realSym.match(input, head);
	}

	public Symbol getRealSym(Automaton automaton) {
		Symbol realSym = automaton.getSymbol(id);
		if(realSym == this) throw new IllegalStateException(); // Shouldn't be registered as the offical symbol!
		if(realSym == null) throw new NullPointerException("Symbol not registered in automaton: "+id+"; symbols: "+automaton.symbols.keySet());
		return realSym;
	}
	
	@Override
	public void resolveRefs(Automaton automaton) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean compatibleWith(Symbol symbol, Priority priority) {
		return id.equals(symbol.id);
	}
}
