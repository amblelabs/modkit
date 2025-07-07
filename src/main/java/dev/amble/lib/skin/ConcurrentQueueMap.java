package dev.amble.lib.skin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mojang.datafixers.util.Pair;

public class ConcurrentQueueMap<K, V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<K> queue = new ConcurrentLinkedQueue<>();

    public void put(K key, V value) {
        if (map.put(key, value) == null) {
            queue.add(key);
        }
    }

    public V get(K key) {
        return map.get(key);
    }

    public Pair<K, V> remove() {
        K key = queue.poll();
        if (key != null) {
            V val = map.remove(key);

            if (val != null) {
                return Pair.of(key, val);
            }
        }
        return Pair.of(null, null);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}