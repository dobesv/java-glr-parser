package felix.parser.glr.automaton;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import felix.parser.glr.grammar.Grammar;
import felix.parser.glr.grammar.Priority;
import felix.parser.glr.grammar.Symbol;

/**
 * The automaton describes the action to take for each possible state.
 * 
 * A state is the pair of the previous state and the next symbol, and it
 * can be mapped to a list of actions.
 * 
 * When there are zero actions, the current subtree is dead.
 * 
 * When there is one action we have no ambiguity.
 * 
 * When there are more than one actions we have a conflict in the grammar so we'll
 * be adding some additional states to our parallel state tree.
 */
public class Automaton {

	public final LinkedHashMap<String,Symbol> symbols = new LinkedHashMap<>();
	public final LinkedHashMap<State,Set<Action>> table = new LinkedHashMap<>();
	
	public Automaton() {
		super();
	}

	
	public static class BuildQueueItem {
		public final Symbol symbol;
		public final State prevState;
		public final State leftState;
		public final Priority.Requirement req;
		public BuildQueueItem(Symbol symbol, State prevState, State leftState, Priority.Requirement req) {
			super();
			this.symbol = symbol;
			this.prevState = prevState;
			this.leftState = leftState;
			this.req = req;
		}
		public BuildQueueItem(Symbol symbol, State prevState, State leftState) {
			this(symbol, prevState, leftState, null);
		}
		public void invoke(LinkedList<BuildQueueItem> queue, Automaton automaton) {
			symbol.computeActions(prevState, leftState, req, queue, automaton);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((leftState == null) ? 0 : leftState.hashCode());
			result = prime * result
					+ ((prevState == null) ? 0 : prevState.hashCode());
			result = prime * result + ((req == null) ? 0 : req.hashCode());
			result = prime * result
					+ ((symbol == null) ? 0 : symbol.hashCode());
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
			BuildQueueItem other = (BuildQueueItem) obj;
			if (leftState == null) {
				if (other.leftState != null)
					return false;
			} else if (!leftState.equals(other.leftState))
				return false;
			if (prevState == null) {
				if (other.prevState != null)
					return false;
			} else if (!prevState.equals(other.prevState))
				return false;
			if (req == null) {
				if (other.req != null)
					return false;
			} else if (!req.equals(other.req))
				return false;
			if (symbol == null) {
				if (other.symbol != null)
					return false;
			} else if (!symbol.equals(other.symbol))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "BuildQueueItem [symbol=" + symbol + ", prevState="
					+ prevState + ", leftState=" + leftState + ", req=" + req
					+ "]";
		}
		
		
	}
	
	
	/**
	 * Build the parsing table using the given symbol as the root.
	 * 
	 * The symbol is expected to encompass the entire input (that is, the symbol
	 * plus EOF is a valid input).
	 */
	public Automaton build(Grammar grammar) {
		for(Symbol symbol : grammar.symbols) {
			addSymbol(symbol);
		}
		for(Symbol symbol : grammar.symbols) {
			symbol.resolveRefs(this);
		}
		LinkedList<BuildQueueItem> queue = new LinkedList<>();
		final State rootStatePrefix = null; //(grammar.root instanceof NonTerminal) ? null : State.START_OF_FILE; // Hmmm bit of a hack here
		queue.add(new BuildQueueItem(grammar.root, null, rootStatePrefix));
		addAction(new State(null, grammar.root), new Accept());
		
		HashSet<BuildQueueItem> itemsExecuted = new HashSet<>();
		while(!queue.isEmpty()) {
			final BuildQueueItem item = queue.removeLast();
			if(itemsExecuted.add(item)) {
				item.invoke(queue, this);
			}
		}
		
		//System.out.println(this.toString());
		return this;
	}
	
	public void addSymbol(Symbol sym) {
		symbols.put(sym.id, sym);
	}
	
	/**
	 * Add an action to the table.
	 * @param left State to the left of the current position (null for the initial state)
	 * @param action Action to take (shift or reduce)
	 * @return true if the given action wasn't already added for that state and symbol
	 */
	public boolean addAction(State left, Action action) {
		if(action == null) throw new NullPointerException();
		Set<Action> actionSet = getActions(left, true);
		return actionSet.add(action);
	}


	/**
	 * Get the list of actions for a given state and symbol combination.
	 * @param createIfMissing If true, this will add a new empty set and return it if no actions have been defined; otherwise,
	 *                        it may return null.
	 */
	public Set<Action> getActions(State state, boolean createIfMissing) {
		Set<Action> actionSet = table.get(state);
		if(actionSet == null && createIfMissing) table.put(state, actionSet = new TreeSet<>());
		return actionSet;
	}

	/**
	 * Get the list of possible next actions for a state and symbol combination.
	 */
	public Set<Action> getActions(State left) {
		return getActions(left, false);
	}

	/**
	 * Check if actions have been defined for the given state & symbol combination.
	 */
	public boolean hasActions(State left, Symbol symbol) {
		Set<Action> actions = getActions(left, false);
		return actions != null && !actions.isEmpty();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("match input with");
		for(Entry<State, Set<Action>> entry : table.entrySet()) {
			State state = entry.getKey();
			for(Action act : entry.getValue()) {
				sb.append("\n| ").append(state).append(" ").append(act);
			}
		}
		return sb.toString();
	}


	public Symbol getSymbol(String id) {
		return symbols.get(id);
	}
}
