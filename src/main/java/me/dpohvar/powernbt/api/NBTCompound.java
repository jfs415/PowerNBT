package me.dpohvar.powernbt.api;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.AbstractSet;
import java.util.AbstractCollection;

/**
 * Represent net.minecraft.server.NBTTagCompound.<br>
 * Allows you to work with NBTTagCompound as with Map.<br>
 * values of this map will be converted to java primitive types if it possible.<br>
 * net.minecraft.server.NBTTagList converted to NBTList<br>
 * net.minecraft.server.NBTTagCompound converted to NBTCompound<br>
 * types allowed to put:<br>
 * * all primitive types (boolean as NBTTagByte 0 or 1)<br>
 * * Object[] as NBTTagList<br>
 * * java.util.Collection as NBTTagList<br>
 * * java.util.Map as NBTTagCompound<br>
 * arrays, collections and maps must contains only the allowed values.<br>
 * Difference from {@link java.util.Map}:<br>
 * {@link me.dpohvar.powernbt.api.NBTCompound#put(String, Object)} creates a clone of NBT tag before put:<br><pre>
 *   NBTCompound cmp = new NBTCompound(); // cmp = {}
 *   cmp.put("foo", "bar"); // cmp = {foo=bar}
 *   cmp.put("self", cmp); // cloning cmp before put, cmp = {foo=bar, self={foo=bar}}
 *   cmp.get("self"); // result = {foo=bar}
 * </pre><br>
 * {@link me.dpohvar.powernbt.api.NBTCompound} can not contain empty keys or values (null)<br>
 * {@link me.dpohvar.powernbt.api.NBTCompound} can not contain cross-references.
 */
@SuppressWarnings("UnusedDeclaration")
public class NBTCompound implements Map<String, Object>, NBTBox {

	private static NBTBridge nbtBridge = NBTBridge.getInstance();
	private final Map<String, Object> handleMap;
	private final Object handle;

	/**
	 * Create new instance of NBTCompound by NBTTagCompound
	 *
	 * @param tag
	 * 			  instance of net.minecraft.server.NBTTagCompound
	 * @return NBTCompound
	 */
	public static NBTCompound forNBT(Object tag) {
		if (tag == null) {
			return null;
		}
		return new NBTCompound(tag);
	}

	/**
	 * Create new instance NBTCompound by copy of NBTTagCompound
	 *
	 * @param tag
	 * 			  instance of net.minecraft.server.NBTTagCompound
	 * @return NBTCompound
	 */
	public static NBTCompound forNBTCopy(Object tag) {
		if (tag == null) {
			return null;
		}
		return forNBT(nbtBridge.cloneTag(tag));
	}

	/**
	 * Convert NBT compound to java {@link java.util.Map}
	 *
	 * @param map
	 * 			  map to fill
	 * @param <T>
	 * 			  T
	 * @return map
	 */
	public <T extends Map<String, Object>> T toMap(T map) {
		map.clear();
		for (Map.Entry<String, Object> e : handleMap.entrySet()) {
			String key = e.getKey();
			Object nbtTag = e.getValue();
			byte type = nbtBridge.getTagType(nbtTag);
			if (type == 9) {
				map.put(key, NBTList.forNBT(nbtTag).toArrayList());
			} else if (type == 10) {
				map.put(key, forNBT(nbtTag).toHashMap());
			} else {
				map.put(key, nbtBridge.getPrimitiveValue(nbtTag));
			}
		}
		return map;
	}

	/**
	 * Convert nbt compound to {@link java.util.HashMap}
	 *
	 * @return HashMap
	 */
	public HashMap<String, Object> toHashMap() {
		return toMap(new HashMap<>());
	}

	NBTCompound(Object tag) {
		assert nbtBridge.getTagType(tag) == 10;
		this.handle = tag;
		this.handleMap = nbtBridge.getNbtInnerMap(tag);
	}

	/**
	 * Get original NBTTagCompound.
	 *
	 * @return NBTTagCompound
	 */
	public Object getHandle() {
		return handle;
	}

	/**
	 * Get copy of original NBTTagCompound.
	 *
	 * @return NBTTagCompound
	 */
	public Object getHandleCopy() {
		return nbtBridge.cloneTag(handle);
	}

	/**
	 * get Map stored in original NBTTagCompound.
	 *
	 * @return Map
	 */
	public Map<String, Object> getHandleMap() {
		return handleMap;
	}

	/**
	 * Create new empty NBTCompound
	 */
	public NBTCompound() {
		this(new HashMap());
	}

	@Override
	public boolean equals(Object t) {
		return t instanceof NBTCompound && handle.equals(((NBTCompound) t).handle);
	}

	/**
	 * Convert java {@link java.util.Map} to NBTCompound.<br>
	 * map should not contain cross-references!
	 *
	 * @param map
	 * 			  map to convert
	 */
	public NBTCompound(Map<?, ?> map) {
		this(nbtBridge.createNBTTagCompound());
		for (var key : map.keySet()) {
			put(String.valueOf(key), map.get(key));
		}
	}

	/**
	 * Create clone of this NBT compouns
	 *
	 * @return cloned {@link me.dpohvar.powernbt.api.NBTCompound}
	 */
	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public NBTCompound clone() {
		return new NBTCompound(nbtBridge.cloneTag(handle));
	}

	@Override
	public int size() {
		return handleMap.size();
	}

	@Override
	public boolean isEmpty() {
		return handleMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return handleMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		Object tag;
		if (value instanceof Map map) {
			tag = new NBTCompound(map).getHandle();
		} else if (value instanceof Collection col) {
			tag = new NBTList(col).getHandle();
		} else if (value instanceof Object[] col) {
			tag = new NBTList(col).getHandle();
		} else {
			tag = nbtBridge.getTagValueByPrimitive(value);
		}
		return handleMap.containsValue(tag);
	}

	@Override
	public Object get(Object key) {
		return NBTManager.getInstance().getValueOfTag(handleMap.get(key));
	}

	/**
	 * Put the <code>copy</code> of value to NBTTagCompound
	 *
	 * @param key
	 * 			  Key with which the value is to be associated
	 * @param value
	 * 			  Value to be associated with the specified key
	 * @return The copy of previous value associated with key
	 */
	@Override
	public Object put(String key, Object value) {
		if (key == null) {
			return null;
		}
		if (value == null) {
			return remove(key);
		}
		Object tag;
		if (value instanceof Map map) {
			tag = new NBTCompound(map).getHandle();
		} else if (value instanceof Collection col) {
			tag = new NBTList(col).getHandle();
		} else if (value instanceof Object[] col) {
			tag = new NBTList(col).getHandle();
		} else {
			tag = nbtBridge.getTagValueByPrimitive(value);
		}
		Object oldTag = put_handle(key, tag);
		return NBTManager.getInstance().getValueOfTag(oldTag);
	}

	private Object put_handle(String key, Object tag) {
		return handleMap.put(key, tag);
	}

	@Override
	public Object remove(Object key) {
		Object oldTag = handleMap.remove(key);
		return NBTManager.getInstance().getValueOfTag(oldTag);
	}

	/**
	 * Copies all of the mappings from the map to this NBTTagCompound
	 *
	 * @param map
	 * 			  Mappings to be stored in this map
	 */
	@Override
	public void putAll(@NotNull Map<? extends String, ?> map) {
		for (Entry<? extends String, ?> e : map.entrySet()) {
			String key = e.getKey();
			if (key == null) {
				continue;
			}
			put(key, e.getValue());
		}
	}

	@Override
	public void clear() {
		handleMap.clear();
	}

	@Override
	public Set<String> keySet() {
		return handleMap.keySet();
	}

	@Override
	public Collection<Object> values() {
		return new NBTValues(handleMap.values());
	}

	@Override
	public NBTEntrySet entrySet() {
		return new NBTEntrySet(handleMap.entrySet());
	}

	/**
	 * Merge this compound with map.<br>
	 * Merging occurs recursively for inner maps
	 *
	 * @param map
	 * 			  map to merge
	 */
	public void merge(Map<?, ?> map) {
		for (Object key : map.keySet()) {
			if (key == null) {
				continue;
			}
			if (!containsKey(key)) {
				put(key.toString(), map.get(key));
				continue;
			}
			Object val = get(key);
			Object value = map.get(key);
			if (val instanceof NBTCompound cmp && value instanceof Map<?, ?> vMap) {
				cmp.merge(vMap);
			} else {
				put(key.toString(), value);
			}
		}
	}

	public String toString() {
		return handle.toString();
	}

	/**
	 * Try to get value and convert to boolean
	 *
	 * @param key
	 * 			  key
	 * @return value, false by default
	 */
	public boolean getBoolean(String key) {
		Object val = get(key);
		if (val instanceof Float f) {
			return f != 0.f;
		}
		if (val instanceof Double d) {
			return d != 0.d;
		}
		if (val instanceof Number num) {
			return num.longValue() != 0;
		}
		if (val instanceof CharSequence cs) {
			return cs.length() != 0;
		}
		if (val instanceof int[] ints) {
			return ints.length != 0;
		}
		if (val instanceof byte[] bytes) {
			return bytes.length != 0;
		}
		if (val instanceof Collection col) {
			return col.isEmpty();
		}
		if (val instanceof Map map) {
			return !map.isEmpty();
		}
		return false;
	}

	/**
	 * Try to get byte value or convert to byte
	 *
	 * @param key
	 * 			  key
	 * @return value, 0 by default
	 */
	public byte getByte(String key) {
		Object val = get(key);
		if (val instanceof Number) {
			return ((Number) val).byteValue();
		}
		if (val instanceof CharSequence) {
			try {
				return (byte) Long.parseLong(val.toString());
			} catch (Exception e) {
				try {
					return (byte) Double.parseDouble(val.toString());
				} catch (Exception ignored) {
				}
			}
		}
		return 0;
	}

	/**
	 * Try to get short value or convert to short
	 *
	 * @param key
	 * 			  key
	 * @return value, 0 by default
	 */
	public short getShort(String key) {
		Object val = get(key);
		if (val instanceof Number) {
			return ((Number) val).shortValue();
		}
		if (val instanceof CharSequence) {
			try {
				return (short) Long.parseLong(val.toString());
			} catch (Exception e) {
				try {
					return (short) Double.parseDouble(val.toString());
				} catch (Exception ignored) {
				}
			}
		}
		return 0;
	}

	/**
	 * Try to get int value or convert to int
	 *
	 * @param key
	 * 			  key
	 * @return value, 0 by default
	 */
	public int getInt(String key) {
		Object val = get(key);
		if (val instanceof Number num) {
			return num.intValue();
		}
		if (val instanceof CharSequence) {
			try {
				return (int) Long.parseLong(val.toString());
			} catch (Exception e) {
				try {
					return (int) Double.parseDouble(val.toString());
				} catch (Exception ignored) {
				}
			}
		}
		return 0;
	}

	/**
	 * Try to get long value or convert to long
	 *
	 * @param key
	 * 			  key
	 * @return value, 0 by default
	 */
	public long getLong(String key) {
		Object val = get(key);
		if (val instanceof Number num) {
			return num.longValue();
		}
		if (val instanceof CharSequence) {
			try {
				return Long.parseLong(val.toString());
			} catch (Exception e) {
				try {
					return (long) Double.parseDouble(val.toString());
				} catch (Exception ignored) {
				}
			}
		}
		return 0;
	}

	/**
	 * Try to get float value or convert to float
	 *
	 * @param key
	 * 			  key
	 * @return value, 0 by default
	 */
	public float getFloat(String key) {
		Object val = get(key);
		if (val instanceof Number) {
			return ((Number) val).floatValue();
		}
		if (val instanceof CharSequence) {
			try {
				return (float) Double.parseDouble(val.toString());
			} catch (Exception ignored) {
			}
		}
		return 0;
	}

	/**
	 * Try to get double value or convert to double
	 *
	 * @param key
	 * 			  key
	 * @return value, 0 by default
	 */
	public double getDouble(String key) {
		Object val = get(key);
		if (val instanceof Number) {
			return ((Number) val).doubleValue();
		}
		if (val instanceof CharSequence) {
			try {
				return Double.parseDouble(val.toString());
			} catch (Exception ignored) {
			}
		}
		return 0;
	}

	/**
	 * Try to get string value or convert string
	 *
	 * @param key
	 * 			  key
	 * @return value, empty string by default
	 */
	public String getString(String key) {
		Object val = get(key);
		if (val == null) {
			return "";
		} else {
			return val.toString();
		}
	}

	/**
	 * Try to get int[]
	 *
	 * @param key
	 * 			  key
	 * @return array, empty array by default
	 */
	public int[] getIntArray(String key) {
		Object val = get(key);
		if (val instanceof int[]) {
			return (int[]) val;
		}
		if (val instanceof byte[] bytes) {
			int[] result = new int[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				result[i] = bytes[i];
			}
			return result;
		}
		return new int[0];
	}

	/**
	 * Try to get byte[]
	 *
	 * @param key
	 * 			  key
	 * @return array, empty array by default
	 */
	public byte[] getByteArray(String key) { // sorry for typo
		Object val = get(key);
		if (val instanceof byte[]) {
			return (byte[]) val;
		}
		if (val instanceof int[] ints) {
			byte[] result = new byte[ints.length];
			for (int i = 0; i < ints.length; i++) {
				result[i] = (byte) ints[i];
			}
			return result;
		}
		return new byte[0];
	}

	/**
	 * Try to get NBTCompound
	 *
	 * @param key
	 * 			  key
	 * @return NBTCompound value, or null if there is no compound
	 */
	public NBTCompound getCompound(String key) {
		Object val = get(key);
		if (val instanceof NBTCompound cmp) {
			return cmp;
		}
		return null;
	}

	/**
	 * Try to get NBTList
	 *
	 * @param key
	 * 			  key
	 * @return NBTList value, or null if there is no list
	 */
	public NBTList getList(String key) {
		Object val = get(key);
		if (val instanceof NBTList list) {
			return list;
		}
		return null;
	}

	/**
	 * Get NBTCompound or create new one<br>
	 * Example: <br><pre>
	 *     NBTCompound cmp = new NBTCompound().compound("display").list("Lore").add("lore1");
	 *     // cmp = {display:{Lore:["lore1"]}}
	 * </pre>
	 *
	 * @param key
	 * 			  Key
	 * @return Existing or created compound
	 */
	public NBTCompound compound(String key) {
		Object val = get(key);
		if (val instanceof NBTCompound cmp) {
			return cmp;
		}
		NBTCompound compound = new NBTCompound();
		put_handle(key, compound.getHandle());
		return compound;
	}

	/**
	 * get NBTList or create new one<br>
	 * Example: <br><pre>
	 *     NBTCompound cmp = new NBTCompound().compound("display").list("Lore").add("lore1");
	 *     // cmp = {display:{Lore:["lore1"]}}
	 * </pre>
	 *
	 * @param key
	 * 			  Key
	 * @return Existing or created list
	 */
	public NBTList list(String key) {
		Object val = get(key);
		if (val instanceof NBTList list) {
			return list;
		}
		NBTList list = new NBTList();
		put_handle(key, list.getHandle());
		return list;
	}

	/**
	 * Put NBTCompound to handle without using cloning.<br>
	 * Be sure that you do not have cross-reference.<br>
	 * Do not bind NBTCompound to itself!
	 *
	 * @param key
	 * 			  key with which the NBTCompound is to be associated
	 * @param value
	 * 			  NBTCompound to be associated with key
	 * @return the previous value associated with key
	 */
	public Object bind(String key, NBTCompound value) {
		Object val = get(key);
		put_handle(key, value.getHandle());
		return val;
	}

	/**
	 * Put NBTList to handle without using cloning.<br>
	 * Be sure that you do not have cross-reference.<br>
	 *
	 * @param key
	 * 			  Key with which the NBTList is to be associated
	 * @param value
	 * 			  NBTList to be associated with key
	 * @return the previous value associated with key
	 */
	public Object bind(String key, NBTList value) {
		Object val = get(key);
		put_handle(key, value.getHandle());
		return val;
	}

	/**
	 * Check if compound contains key with value of specific type
	 *
	 * @param key
	 * 			  key
	 * @param type
	 * 			  type of value
	 * @return true if compound has key with specific value
	 */
	public boolean containsKey(String key, Class<?> type) {
		Object t = get(key);
		return type.isInstance(t);
	}

	/**
	 * Check if compound contains key with value of specific type
	 *
	 * @param key
	 * 			  key
	 * @param type
	 * 			  byte type of NBT tag
	 * @return true if compound has key with specific value
	 */
	public boolean containsKey(String key, byte type) {
		Object tag = handleMap.get(key);
		return tag != null && nbtBridge.getTagType(tag) == type;
	}

	public static class NBTValues extends AbstractCollection<Object> {

		Collection<Object> handle;

		private NBTValues(Collection<Object> values) {
			this.handle = values;
		}

		@Override
		public Iterator<Object> iterator() {
			return new NBTValuesIterator(handle.iterator());
		}

		@Override
		public int size() {
			return handle.size();
		}

		public static class NBTValuesIterator implements Iterator<Object> {

			private final Iterator<Object> handle;

			private NBTValuesIterator(Iterator<Object> iterator) {
				this.handle = iterator;
			}

			@Override
			public boolean hasNext() {
				return handle.hasNext();
			}

			@Override
			public Object next() {
				return NBTManager.getInstance().getValueOfTag(handle.next());
			}

			@Override
			public void remove() {
				handle.remove();
			}

		}

	}

	public static class NBTEntrySet extends AbstractSet<Entry<String, Object>> {

		private final Set<Entry<String, Object>> entries;

		private NBTEntrySet(Set<Entry<String, Object>> entries) {
			this.entries = entries;
		}

		@Override
		public NBTIterator iterator() {
			return new NBTIterator(entries.iterator());
		}

		@Override
		public int size() {
			return entries.size();
		}

		public static class NBTIterator implements Iterator<Entry<String, Object>> {

			private final Iterator<Entry<String, Object>> iterator;

			private NBTIterator(Iterator<Entry<String, Object>> iterator) {
				this.iterator = iterator;
			}

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public NBTEntry next() {
				return new NBTEntry(iterator.next());
			}

			@Override
			public void remove() {
				iterator.remove();
			}

			public class NBTEntry implements Entry<String, Object> {

				private final Entry<String, Object> entry;

				NBTEntry(Entry<String, Object> entry) {
					this.entry = entry;
				}

				@Override
				public String getKey() {
					return entry.getKey();
				}

				@Override
				public Object getValue() {
					return NBTManager.getInstance().getValueOfTag(entry.getValue());
				}

				@Override
				public Object setValue(Object value) {
					if (value == null) {
						Object val = getValue();
						remove();
						return val;
					} else {
						Object tag;
						if (value instanceof Map map) {
							tag = new NBTCompound(map).getHandle();
						} else if (value instanceof Collection col) {
							tag = new NBTList(col).getHandle();
						} else if (value instanceof Object[] col) {
							tag = new NBTList(col).getHandle();
						} else {
							tag = nbtBridge.getTagValueByPrimitive(value);
						}
						Object oldTag = entry.setValue(tag);
						return NBTManager.getInstance().getValueOfTag(oldTag);
					}
				}

			}

		}

	}

}










