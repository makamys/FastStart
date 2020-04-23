package makamys.faststart;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class WrappedMap<K, V> implements Map<K, V> {
    
    private Map<K, V> o;
    
    private Set<String> blacklist;
    
    public void setBlackList(Set<String> blacklist) {
        this.blacklist = blacklist;
    }
    
    public WrappedMap(Map<K, V> original) {
        this.o = original;
    }
    
    @Override
    public void clear() {
        o.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return o.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return o.containsValue(value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return o.entrySet();
    }

    @Override
    public V get(Object key) {
        return o.get(key);
    }

    @Override
    public boolean isEmpty() {
        return o.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return o.keySet();
    }

    @Override
    public V put(K key, V value) {
        /* Explanation: When we're loading cached classes, mixin gives an error because it thinks it's going to
           have to run on already transformed classes. This doesn't actually happen, since CacheTransformer
           steals the transformation right from all other classes, including Mixin. But we need to bypass
           this error somehow, since it results in a crash. This is how we do it. (see MixinInfo.readTargets
           to see why it works)*/ 
        if(blacklist != null && blacklist.contains(key)) {
            return null;
        } else {
            // For some reason mixin gives a different error if we always refuse to put, so we should only
            // do it when necessary.
            return o.put(key, value);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        o.putAll(m);
    }

    @Override
    public V remove(Object key) {
        return o.remove(key);
    }

    @Override
    public int size() {
        return o.size();
    }

    @Override
    public Collection<V> values() {
        return o.values();
    }
    
    
    
}
