package makamys.faststart;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class ExtraIterator<T> implements ListIterator<T> {

	ListIterator<T> original;
	T secret;
	List<T> list;
	
	int index;
	
	public ExtraIterator(ListIterator<T> original, int index, T extra, List<T> list) {
		this.original = original;
		this.index = index;
		this.secret = extra;
	}
	
	@Override
	public boolean hasNext() {
		return index == 0 || original.hasNext();
	}

	
	
	@Override
	public T next() {
		if(index++ == 0) {
			return secret;
		} else {
			index++;
			return original.next();
		}
	}

	@Override
	public void add(T arg0) {
		list.add(index, arg0);
	}

	@Override
	public boolean hasPrevious() {
		return index > 0;
	}

	@Override
	public int nextIndex() {
		return index;
	}

	@Override
	public T previous() {
		index--;
		if(index > 0) {
			return original.previous();
		} else {
			return secret;
		}
	}

	@Override
	public int previousIndex() {
		return index - 1;
	}

	@Override
	public void remove() {
		if(index == 0) {
			System.err.println("tried to remove extra element in ExtraIterator");
		} else {
			original.remove();
		}
	}

	@Override
	public void set(T arg0) {
		if(index == 0) {
			System.err.println("tried to set extra element in ExtraIterator");
		} else {
			original.set(arg0);
		}
	}
	
}
