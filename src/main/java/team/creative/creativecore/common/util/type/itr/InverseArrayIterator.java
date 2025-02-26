package team.creative.creativecore.common.util.type.itr;

import java.util.Iterator;

public class InverseArrayIterator<T> implements Iterator<T> {
    
    public final T[] content;
    private int index;
    
    public InverseArrayIterator(T[] content) {
        this.content = content;
        this.index = content.length - 1;
    }
    
    @Override
    public boolean hasNext() {
        return index >= 0;
    }
    
    @Override
    public T next() {
        T result = content[index];
        index--;
        return result;
    }
    
}
