package makamys.faststart;

public interface ListAddListener<T> {
    
    void onElementAdded(int index, T addedElement);
    
    void beforeIterator();

}
