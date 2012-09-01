package felix.parser.util;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public final class ListChain<E> extends AbstractSequentialList<E> implements List<E> {
	List<List<E>> lists;

	@Override
	public int size() {
		int size = 0;
		for(List<E> list : lists) size += list.size();
		return size;
	}

	@Override
	public boolean isEmpty() {
		for(List<E> list : lists) {
			if(!list.isEmpty())
				return false;
		}
		return true;
	}

	@Override
	public boolean contains(Object o) {
		for(List<E> list : lists) {
			if(list.contains(o))
				return true;
		}
		return false;
	}

	final static class ChainListIterator<E> implements ListIterator<E> {
		final ListIterator<List<E>> iter;
		ListIterator<E> current;
		int index;
		
		public ChainListIterator(List<List<E>> lists, int index) {
			super();
			this.iter = lists.listIterator();
			
			// Advance to the correct index
			while(this.index < index) {
				if(!iter.hasNext())
					throw new IndexOutOfBoundsException();
				List<E> list = iter.next();
				
				int remaining = index - this.index;
				if(list.size() <= remaining) {
					this.index += list.size();
				} else {
					this.index = index;
					current = list.listIterator(remaining);
				}
			}
		}

		@Override
		public boolean hasNext() {
			// Advance until we find an iterator that returns true for hasNext(),
			// or we run out of lists.
			while(current == null || !current.hasNext()) {
				if(!iter.hasNext())
					return false;
				List<E> list = iter.next();
				if(list.isEmpty())
					continue;
				current = list.listIterator();
			}
			return current.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			while(current == null || !current.hasPrevious()) {
				if(!iter.hasPrevious())
					return false;
				List<E> list = iter.previous();
				if(list.isEmpty())
					continue;
				current = list.listIterator(list.size()-1);
			}
			return false;
		}

		@Override
		public E next() {
			if(!hasNext()) throw new NoSuchElementException();
			return current.next();
		}

		@Override
		public E previous() {
			if(!hasPrevious()) throw new NoSuchElementException();
			return current.previous();
		}

		@Override
		public void remove() {
			if(current != null) current.remove();
			else throw new IllegalStateException();
		}

		@Override
		public int nextIndex() {
			return index;
		}

		@Override
		public int previousIndex() {
			return index-1;
		}

		@Override
		public void set(E e) {
			if(current != null) current.set(e);
			else throw new IllegalStateException();
		}

		@Override
		public void add(E e) {
			if(hasNext()) current.add(e);
			else iter.add(Collections.singletonList(e));
		}
		
	}

	/**
	 * Add a new list to the chain containing only the given element.
	 */
	@Override
	public boolean add(E e) {
		return lists.add(Collections.singletonList(e));
	}

	public boolean add(List<E> e) {
		return lists.add(e);
	}
	
	@Override
	public boolean remove(Object o) {
		for(List<E> list : lists) {
			if(list.remove(o))
				return true;
		}
		return false;
	}

	public boolean remove(List<E> list) {
		return lists.remove(list);
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		return lists.add(new ArrayList<>(c));
	}

	@Override
	public void clear() {
		lists.clear();
	}


	@Override
	public ListIterator<E> listIterator() {
		return new ChainListIterator<E>(lists, 0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ChainListIterator<E>(lists, index);
	}
}
