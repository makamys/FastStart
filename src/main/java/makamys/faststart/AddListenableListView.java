package makamys.faststart;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import net.minecraft.launchwrapper.LaunchClassLoader;

/** A wrapper around a list that emits events when items are added to the list. */

public class AddListenableListView<T> implements List<T> {
    List<T> original;
    
    private List<ListAddListener<T>> listeners = new LinkedList<>();
    
    public T alt;
    
    public AddListenableListView(List<T> original){
        this.original = original;
    }
    
    public void addListener(ListAddListener<T> l) {
        listeners.add(l);
    }
    
    public void removeListener(ListAddListener<T> l) {
        listeners.remove(l);
    }
    
    private void emit(int index, T element) {
        for(ListAddListener<T> l : listeners) {
            //l.onElementAdded(index, element);
        }
    }
    
    private void emitBeforeIterator() {
    	for(ListAddListener<T> l : listeners) {
            //l.beforeIterator();
        }
    }
    
    @Override
    public boolean add(T e) {
        /*if(original.add(e)) {
            emit(size() - 1, e);
            return true;
        }
        return false;*/
        
        return original.add(e);
    }

    @Override
    public void add(int index, T element) {
        original.add(index, element);
        
        //emit(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return addAll(size(), c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        /*if(original.addAll(c)) {
            int cIndex = 0;
            for(T e : c) {
                emit(index + cIndex, e);
                cIndex++;
            }
            return true;
        }
        return false;*/
    	return original.addAll(c);
    }

    @Override
    public void clear() {
        original.clear();
    }

    @Override
    public boolean contains(Object o) {
    	return true||alt == null ? original.contains(o) : o.equals(alt);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if(true||alt == null) {
        	return original.containsAll(c);
        } else {
        	return c.size() == 1 && c.iterator().next().equals(alt);
        }
    }

    @Override
    public T get(int index) throws IndexOutOfBoundsException {
    	if(true||alt == null) {
    		return original.get(index);
    	} else {
    		if(index != 0) {
    			throw new IndexOutOfBoundsException();
    		}
    		return alt;
    	}
    }

    @Override
    public int indexOf(Object o) {
    	if(true||alt == null) {
    		return original.indexOf(o);
    	} else {
    		return o.equals(alt) ? 0 : -1;
    	}
    }

    @Override
    public boolean isEmpty() {
    	if(true||alt == null) {
    		return original.isEmpty();
    	} else {
    		return false;
    	}
    }

    @Override
    public Iterator<T> iterator() {
    	return listIterator();
    }
    


    @Override
    public int lastIndexOf(Object o) {
        if(true||alt == null) {
        	return original.lastIndexOf(o); 
        } else {
        	return o.equals(alt) ? 0 : -1;
        }
    }

    @Override
    public ListIterator<T> listIterator() {
    	return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        boolean first = true;
        
        for(StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if(!first && !e.getClassName().equals(getClass().getName())) {
                if(!e.getClassName().equals(LaunchClassLoader.class.getName())) {
                    
                    System.out.println("iterator called by " + String.join(" > ", Arrays.stream(Thread.currentThread().getStackTrace()).map(x -> x.getClassName()).collect(Collectors.toList())));
                }
                break;
            }
            first = false;
        }
    	if(alt == null) {
    		return original.listIterator(index);
    	} else {
    		List<T> list = new ArrayList<T>(1);
    		list.add(alt);
    		return list.listIterator(index);
    	}
    }

    @Override
    public boolean remove(Object o) {
    	if(false&&alt != null && o.equals(alt)) {
    		System.err.println("tried to remove alt");
    		return false;
    	} else {
    		return original.remove(o);
    	}
    }

    @Override
    public T remove(int index) {
    	if(true||alt == null && index==0) {
    		return original.remove(index);
    		
    	} else {
    		if(index != 0) {
    			throw new IndexOutOfBoundsException();
    		}
    		System.err.println("tried to remove alt (index 0)");
    		return null;
    	}
    }

    @Override
    public boolean removeAll(Collection<?> c) {
    	if(true||alt == null && c.contains(alt)) {
    		return original.removeAll(c);
    		
    	} else {
    		if(c.contains(alt)) {
    			System.err.println("tried to remove alt (removeAll)");
    		}
    		return false;
    	}
    }

    @Override
    public boolean retainAll(Collection<?> c) {
    	if(true||alt == null) {
    		return original.retainAll(c);
    		
    	} else {
    		if(!c.contains(alt)) {
    			System.err.println("tried to remove alt (retainAll)");
    		}
    		
    		return false;
    	}
    }

    @Override
    public T set(int index, T element) {
    	if(true||alt == null) {
    		return original.set(index, element);
    	} else {
    		if(index == 0) {
    			System.err.println("tried to set secret");
    			return null;
    		} else {
    			throw new ArrayIndexOutOfBoundsException();
    		}
    	}
        
    }

    @Override
    public int size() {
    	return true||alt == null ? original.size() : 1;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
    	if(alt != null) {
    		System.err.println("subList unimplemented");
    	}
        return original.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
    	if(true||alt == null) {
    		return original.toArray();
    	} else {
    		Object[] array = new Object[1];
    		array[0] = alt;
    		return array;
    	}
    }

    @Override
    public <T> T[] toArray(T[] a) {
    	System.err.println("toArray, probably buggy");
    	if(true||alt == null) {
    		return original.toArray(a);
    	} else {
    		a[0] = (T)alt;
    	}
    	return a;
    }
}
