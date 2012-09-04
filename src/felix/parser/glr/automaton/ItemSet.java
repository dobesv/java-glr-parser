package felix.parser.glr.automaton;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import felix.parser.glr.grammar.Rule;
import felix.parser.glr.grammar.Symbol;

public class ItemSet {
	final HashSet<Item> items = new HashSet<Item>();
	
	public ItemSet() {
		
	}
	public ItemSet(Collection<Item> items) {
		items.addAll(items);
	}

	boolean add(Item item) {
		return this.items.add(item);
	}

	void addClosure(Automaton automaton) {
		LinkedList<Item> queue = new LinkedList<Item>(items);
		while(!queue.isEmpty()) {
			Item item = queue.remove();
			final Symbol nextSym = item.nextSym();
			for(Rule rule : nextSym.calculateRules(automaton)) {
				final Item newItem = new Item(nextSym, rule, 0);
				if(this.add(newItem)) {
					queue.add(newItem);
				}
			}
		}
	}
	public ItemSet calculateClosure(Automaton automaton) {
		ItemSet result = new ItemSet();
		result.addClosure(automaton);
		return result;
	}
	
	public Set<ItemSet> calculateNextSets(Automaton automaton) {
		HashSet<ItemSet> result = new HashSet<>();
		return calculateNextSets(automaton, result);
	}
	private Set<ItemSet> calculateNextSets(Automaton automaton,
			HashSet<ItemSet> result) {
		HashMap<Symbol,Set<Item>> nextSyms = new HashMap<>();
		for(Item item : items) {
			if(item.hasNextSym()) {
				Symbol nextSym = item.nextSym();
				Set<Item> nextItems = nextSyms.get(nextSym);
				if(nextItems == null) nextSyms.put(nextSym, nextItems = new HashSet<Item>());
				nextItems.add(item);
			}
		}
		
		LinkedList<ItemSet> queue = new LinkedList<ItemSet>();
		for(Map.Entry<Symbol, Set<Item>> nextItem : nextSyms.entrySet()) {
			ItemSet nextClosure = new ItemSet(nextItem.getValue());
			nextClosure.addClosure(automaton);
			if(result.add(nextClosure)) {
				queue.add(nextClosure);
			}
		}
		while(!queue.isEmpty()) {
			final ItemSet set = queue.removeFirst();
			set.calculateNextSets(automaton, result);
		}
		return result;
	}
}
