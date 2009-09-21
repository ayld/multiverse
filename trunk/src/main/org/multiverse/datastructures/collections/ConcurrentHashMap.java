package org.multiverse.datastructures.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.datastructures.refs.Ref;

@AtomicObject
public class ConcurrentHashMap<K,V> implements ConcurrentMap<K, V> {

	private static class MapEntry<K,V> implements Map.Entry<K, V> {

		private final K _key;
		private V _value;
		
		MapEntry(K key, V value) {
			_key = key;
			_value = value;
		}
		
		@Override
		public K getKey() {
			return _key;
		}

		@Override
		public V getValue() {
			return _value;
		}

		@Override
		public V setValue(V value) {
			V old = _value;
			_value = value;
			return old;
		}
		
		public String toString() {
			return String.format("MapEntry(key=%s, value=%s)", _key, _value);
		}
		
	}
	
	private static class BucketList<K,V> {
		
		private final List<MapEntry<K,V>> _bucketEntries;
		
		BucketList() {
			_bucketEntries = new LinkedList<MapEntry<K,V>>();
		}
		
		void add(K key, V value) {
			if (key == null) {
				throw new NullPointerException("The keys of the ConcurrentHashMap may not be null.");
			}
			_bucketEntries.add(new MapEntry<K,V>(key, value));
		}
		
		MapEntry<K,V> getEntryForKey(Object key) {
			if(key == null) {
				throw new NullPointerException("The keys of the ConcurrentHashMap may not be null.");
			}
			for (MapEntry<K,V> entry : _bucketEntries) {
				Object k = entry.getKey();
				if ((k == key) || (key != null && key.equals(k))) {
					return entry;
				}
			}
			return null;
		}
		
		MapEntry<K,V> getEntryForValue(Object value) {
			for (MapEntry<K,V> entry : _bucketEntries) {
				Object v = entry.getValue();
				if ((v == value) || (value != null && value.equals(v))) {
					return entry;
				}
			}
			return null;
		}
		
		boolean remove(MapEntry<K,V> entry) {
			return _bucketEntries.remove(entry);
		}
		
		public String toString() {
			return _bucketEntries.toString();
		}
	}
	
	private class MapEntryIterator implements Iterator<MapEntry<K,V>> {

		private int _currentBucket = 0;
		private Iterator<MapEntry<K,V>> _currentBucketIterator = null;
		
		@Override
		public boolean hasNext() {
			if (_currentBucketIterator == null) {
				nextIterator();
			}
			
			if (_currentBucketIterator == null) {
				return false;
			} else {
				return _currentBucketIterator.hasNext();
			}
		}

		private void nextIterator() {
			_currentBucketIterator = null;
			while (_currentBucketIterator != null && _currentBucket < _capacity) {
				BucketList<K,V> bucketList = _entries[_currentBucket].get();
				if (bucketList != null) {
					_currentBucketIterator = bucketList._bucketEntries.iterator();
					++_currentBucket;
				}
			}
		}

		@Override
		public MapEntry<K, V> next() {
			if (_currentBucketIterator == null) {
				nextIterator();
			}
			
			if (_currentBucketIterator == null) {
				throw new NoSuchElementException("No more elements in the iteration.");
			} else {
				return _currentBucketIterator.next();
			}
		}

		@Override
		public void remove() {
			if (_currentBucketIterator == null) {
				nextIterator();
			}	
			if (_currentBucketIterator == null) {
				throw new IllegalStateException("Already beyond the last element.");
			} else {
				_currentBucketIterator.remove();
				--_size;
			}
		}
		
	}
	
	private class EntrySet extends AbstractSet<Entry<K,V>> {

		@Override
		public Iterator<java.util.Map.Entry<K, V>> iterator() {
			return new Iterator<Entry<K, V>>() {

				final MapEntryIterator _delegate = new MapEntryIterator();
				
				@Override
				public boolean hasNext() {
					return _delegate.hasNext();
				}

				@Override
				public java.util.Map.Entry<K, V> next() {
					return _delegate.next();
				}

				@Override
				public void remove() {
					_delegate.remove();
				}		
			};
		}

		@Override
		public int size() {
			return _size;
		}
		
	}
	
	private class KeySet extends AbstractSet<K> {

		@Override
		public Iterator<K> iterator() {
			return new Iterator<K>() {

				final MapEntryIterator _delegate = new MapEntryIterator();
				
				@Override
				public boolean hasNext() {
					return _delegate.hasNext();
				}

				@Override
				public K next() {
					return _delegate.next().getKey();
				}

				@Override
				public void remove() {
					_delegate.remove();
				}		
			};
		}

		@Override
		public int size() {
			return _size;
		}	
	}
	
	private class ValueSet extends AbstractSet<V> {

		@Override
		public Iterator<V> iterator() {
			return new Iterator<V>() {

				final MapEntryIterator _delegate = new MapEntryIterator();
				
				@Override
				public boolean hasNext() {
					return _delegate.hasNext();
				}

				@Override
				public V next() {
					return _delegate.next().getValue();
				}

				@Override
				public void remove() {
					_delegate.remove();
				}		
			};
		}

		@Override
		public int size() {
			return _size;
		}	
	}	
	
	private static final int DEFAULT_CAPACITY = 16;
	
	private final Ref<BucketList<K,V>>[] _entries;
	private final int _capacity;
	private int _size;
	
	public ConcurrentHashMap() {
		this(DEFAULT_CAPACITY);
	}
	
	public ConcurrentHashMap(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Capacity must be >= 1");
		}
		_size = 0;
		_capacity = capacity;
		_entries = new Ref[_capacity];
        for (int i = 0; i < _capacity; ++i) {
        	_entries[i] = new Ref<BucketList<K,V>>();
        }
	}
	
	@Override
	public void clear() {
		for (Ref<BucketList<K,V>> bucketListRef : _entries) {
			BucketList<K,V> bucketList = bucketListRef.get();
			if (bucketList != null) {
				bucketListRef.set(null);
			}
		}
		_size = 0;
	}

	@Override
    @AtomicMethod(readonly = true)
	public boolean containsKey(Object key) {
		return getEntryForKey(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		for (Ref<BucketList<K,V>> bucketListRef : _entries) {
			BucketList<K,V> bucketList = bucketListRef.get();
			if (bucketList != null) {
				MapEntry<K,V> entry = bucketList.getEntryForValue(value);
				if (entry != null) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
    @AtomicMethod(readonly = true)
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	@Override
    @AtomicMethod(readonly = true)
	public V get(Object key) {
		MapEntry<K,V> entry = getEntryForKey(key);
		if (entry != null) {
			return entry.getValue();
		} else {
			return null;
		}
	}

	@Override
    @AtomicMethod(readonly = true)
    public boolean isEmpty() {
		return _size == 0;
	}

	@Override
    @AtomicMethod(readonly = true)
	public Set<K> keySet() {
		return new KeySet();
	}

	@Override
	public V put(K key, V value) {
		BucketList<K,V> bucketList = getBucketForKey(key);
		MapEntry<K,V> entry = bucketList.getEntryForKey(key);
		if (entry == null) {
			bucketList.add(key, value);
			++_size;
			return null;
		} else {
			V old = entry.getValue();
			entry.setValue(value);
			return old;
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V putIfAbsent(K key, V value) {
		MapEntry<K,V> entry = getEntryForKey(key);
		if (entry == null) {
			put(key, value);
			return value;
		} else {
			return entry.getValue();
		}
	}

	@Override
	public V remove(Object key) {
		BucketList<K,V> bucketList = getBucketForKey(key);
		MapEntry<K,V> entry = bucketList.getEntryForKey(key);
		if (entry == null) {
			return null;
		} else {
			bucketList.remove(entry);
			--_size;
			return entry.getValue();
		}
	}

	@Override
	public boolean remove(Object key, Object value) {
		BucketList<K,V> bucketList = getBucketForKey(key);
		MapEntry<K,V> entry = bucketList.getEntryForKey(key);
		if (entry != null && entry.getValue() != null && entry.getValue().equals(value)) {
			--_size;
			return bucketList.remove(entry);
		}
		return false;
	}

	@Override
	public V replace(K key, V value) {
		MapEntry<K,V> entry = getEntryForKey(key);
		if (entry == null) {
			return null;
		} else {
			V old = entry.getValue();
			entry.setValue(value);
			return old;
		}
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		MapEntry<K,V> entry = getEntryForKey(key);
		if (entry != null && entry.getValue() != null && entry.getValue().equals(oldValue)) {
			entry.setValue(newValue);
			return true;
		}
		return false;
	}

	@Override
    @AtomicMethod(readonly = true)
	public int size() {
		return _size;
	}

	@Override
    @AtomicMethod(readonly = true)
	public Collection<V> values() {
		return new ValueSet();
	}
	
	private BucketList<K,V> getBucketForKey(Object key) {
		int bucket = key.hashCode() % _capacity;
		BucketList<K,V> bucketList = _entries[bucket].get();
		
		if (bucketList == null) {
			bucketList = new BucketList<K,V>();
			_entries[bucket].set(bucketList);
		}
		
		return bucketList;
	}

	private MapEntry<K,V> getEntryForKey(Object key) {
		return getBucketForKey(key).getEntryForKey(key);
	}
}
