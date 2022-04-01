package crawler.interiordesign;

import crawler.main.Globals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author VTPlusAKnauer
 * @param <E> Element type
 */
public class RandomIterableList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 617823734407953226L;

	public RandomIterableList(int initialCapacity) {
		super(initialCapacity);
	}

	public RandomIterableList() {
	}

	public RandomIterableList(Collection<? extends E> c) {
		super(c);
	}

	public E random() {
		return get(Globals.getRandom().nextInt(size()));
	}
	
	@Override
	public Iterator<E> iterator() {
		return new RandomIterator();
	}	
	
	private class RandomIterator implements Iterator<E> {		
		int startIndex;
		int nextElement;
		boolean oneElementListAndNextNotCalled;

		public RandomIterator() {
			if (!isEmpty()) {
				oneElementListAndNextNotCalled = (size() == 1);
				startIndex = Globals.getRandom().nextInt(size());
				nextElement = (startIndex + 1) % size();
			}
		}
		

		@Override
		public boolean hasNext() {			
			if (isEmpty())
				return false;
			else if (oneElementListAndNextNotCalled)
				return true;
			
			else return nextElement != startIndex;
		}

		@Override
		public E next() {
			if (isEmpty()) return null;
			
			oneElementListAndNextNotCalled = false;
			
			E element = get(nextElement);
			nextElement = (nextElement + 1) % size();			
			return element;
		}		
	}
}
