/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or distribute
 * this software, either in source code form or as a compiled binary, for any
 * purpose, commercial or non-commercial, and by any means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors of this
 * software dedicate any and all copyright interest in the software to the
 * public domain. We make this dedication for the benefit of the public at large
 * and to the detriment of our heirs and successors. We intend this dedication
 * to be an overt act of relinquishment in perpetuity of all present and future
 * rights to this software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dulek.collections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * This class represents a set of elements, implemented using a hash table. A
 * set is an unordered collection of elements, containing no duplicate elements.
 * Being unordered, the set makes no guarantees as to the order of its elements.
 * This set is unsynchronised, and permits the {@code null} element.
 * <p>This implementation is heavily based on the default
 * {@link java.util.HashMap HashMap} implementation of Java 7, but some
 * improvements were made to better facilitate sets rather than key-value
 * mappings, since the default implementation of {@link HashSet} maintains a
 * heavy-weight {@code HashMap} to implement the set. Additionally, the default
 * implementation provides two ways of hashing its keys: the standard
 * {@code hashCode()} method of the keys or a cryptographically more secure
 * variant for {@code String}s that uses a random seed. This implementation
 * foregoes the latter hashing method, making it potentially more vulnerable to
 * collision attacks. Lastly, the default implementation uses the separate
 * chaining method for preventing collisions. In an effort to rid the
 * implementation from additional "entry" instances, separate chaining is
 * infeasable. Instead, open addressing with quadratic probing was chosen.
 * Quadratic probing avoids the clustering problem better than linear probing
 * and in Java the locality of reference makes no difference. It was chosen over
 * more complex collision resolution techniques for how it requires less memory
 * overhead.</p>
 * <p>This implementation provides expected constant-time performance for all
 * basic operations of the set: {@link #add(E)}, {@link #remove(Object)} and
 * {@link #contains(Object)}. This expected performance expects that the hash
 * function disperses the elements properly among the buckets. Iteration over
 * the set requires time proportional to the "capacity" of the hash table (the
 * number of buckets) plus its size (the number of elements). Thus, it's very
 * important not to set the initial capacity too high (or the load factor too
 * low) if iteration performance is important.</p>
 * <p>An instance of {@code HashSet} has two parameters that affect its
 * performance: the initial capacity and the load factor. The capacity is the
 * number of buckets in the hash table, and the initial capacity is simply the
 * capacity at the time the hash table is created. The load factor is a measure
 * of how full the hash table is allowed to get before its capacity is
 * automatically increased. When the number of entries in the hash table exceeds
 * the product of the load factor and the current capacity, the hash table is
 * rehashed, meaning all elements will be distributed over a new table. The new
 * table will be approximately twice the size of the old table.</p>
 * <p>As a general rule, the default load factor offers a good tradeoff between
 * time and space costs. Higher values reduce the space overhead but increase
 * the time cost of the basic operations. Lower values increase the memory
 * requirement but reduce the time cost. The expected number of entries in the
 * set and its load factor should be taken into account when setting its initial
 * capacity, so as to minimise the number of rehash operations. If the initial
 * capacity is greater than the maximum number of entries divided by the load
 * factor, no rehash operations will ever occur. The load factor cannot exceed
 * {@code 1} in this implementation, since there can be only one element in each
 * bucket.</p>
 * <p>If many elements are to be stored in a {@code HashSet} instance, creating
 * it with a sufficiently large capacity will allow the elements to be stored
 * more efficiently than letting it perform automatic rehashing as needed to
 * grow the table.</p>
 * <p>Note that this implementation is not synchronised. If multiple threads
 * access the {@code HashSet} concurrently, and at least one of these threads
 * modifies the map structurally, it must be synchronised externally. A
 * structural modification is any operation that adds or removes one or more
 * elements. This is typically accomplished by synchronising on some object that
 * naturally encapsulates the set. If no such object exists, the set should be
 * "wrapped" using the
 * {@link java.util.Collections#synchronizedSet Collections.synchronizedSet}
 * methods. This is best done at creation time, to prevent accidental
 * unsynchronised access to the set:
 * {@code Set<...> s = Collections.synchronizedSet(new HashSet<>(...));}</p>
 * <p>The iterators returned by this class's {@link #iterator()} method are
 * fail-fast: if the set is structurally modified at any time after the iterator
 * is created, in any way except through the iterator's own
 * {@link Iterator#remove()} method, the {@code Iterator} throws a
 * {@link ConcurrentModificationException}. Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behaviour at an undetermined time in the future.
 * </p>
 * <p>Note that the fail-fast behaviour of an iterator cannot be guaranteed as
 * it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronised concurrent modification. Fail-fast iterators throw
 * {@code ConcurrentModificationException}s on a best-effort basis. Therefore,
 * it would be wrong to write a program that depended on this exception for its
 * correctness: the fail-fast behaviour of iterators should only be used to
 * detect bugs.</p>
 * @author Ruben Dulek
 * @param <E> The types of elements stored in this set.
 * @see Collection
 * @see java.util.HashMap
 * @see Set
 * @version 1.0
 */
public class HashSet<E> implements Set<E>,Cloneable,Serializable {
	/**
	 * The default initial capacity of the hash table. This will be the initial
	 * capacity of the hash table if no initial capacity is specified in the
	 * constructing of the set. It must be a power of two, so keep the bitshift
	 * operator in this value, please.
	 */
	protected static final int defaultInitialCapacity = 1 << 4;

	/**
	 * The default load factor. This will be the load factor if none is
	 * specified in the constructing of the set. A value of 0.7 is a good
	 * trade-off between memory usage and time cost. Must be between {@code 0}
	 * and {@code 1}.
	 */
	protected static final float defaultLoadFactor = 0.7f;

	/**
	 * The load factor for the hash table. This indicates the maximum fraction
	 * of buckets that can be filled before the table is rehashed to a bigger
	 * table.
	 * @serial The factor is converted to a {@code String} via
	 * {@code Float.toString(loadFactor)}.
	 */
	protected float loadFactor;

	/**
	 * The maximum capacity of the table. This is used if a higher value is
	 * implicity specified by either of the constructors with arguments. It must
	 * be a power of two, so keep the bitshift operator in this value, please.
	 */
	protected static final int maximumCapacity = 1 << 30;

	/**
	 * The number of times this {@code HashSet} has been structurally modified.
	 * Structural modifications are those that change the number of elements in
	 * the set or otherwise modify its internal structure (e.g. a rehash). This
	 * field is used to make iterators over the set fail-fast.
	 */
	protected transient int modCount;

	/**
	 * This object represents the {@code null}-element. If it is in the set, the
	 * set contains the {@code null}-element. Methods that allow access to the
	 * actual elements of the set (such as {@link Iterator#next()}) should mind
	 * not to return this object, but return {@code null} instead.
	 */
	protected static final Object nullElement = new Object();

	/**
	 * The version of the serialised format of the set. This identifies a
	 * serialisation as being the serialised format of this class.
	 */
	private static final long serialVersionUID = 4882436748081726100L;

	/**
	 * The number of elements in this set.
	 */
	protected transient int size;

	/**
	 * The actual hash table. Its length must always be a power of two. The
	 * table will be resized as necessary, rehashing every element in it.
	 */
	protected transient Object[] table;

	/**
	 * When an element is removed, it will be replaced by a tombstone, marking
	 * that the bucket used to contain an element, and that there might still be
	 * elements after the tombstone belonging to buckets before the tombstone.
	 */
	protected static final Object tombstone = new Object();

	/**
	 * The next size value at which to resize and rehash the table. It will be
	 * kept at {@code table.length * loadFactor}.
	 */
	protected int treshold;

	/**
	 * Constructs an empty {@code HashSet} with the default initial capacity
	 * (16) and the default load factor (0.7). Note that it is good practice to
	 * always specify an initial capacity if there is any idea of approximately
	 * how many elements the set will hold.
	 */
	public HashSet() {
		loadFactor = defaultLoadFactor;
		treshold = (int)(defaultInitialCapacity * defaultLoadFactor); //Pre-compute the treshold.
		table = new Object[defaultInitialCapacity]; //Reserve the actual table.
	}

	/**
	 * Constructs an empty {@code HashSet} with the specified initial capacity
	 * and the default load factor (0.7).
	 * @param initialCapacity The initial capacity of the hash table.
	 * @throws IllegalArgumentException The initial capacity is negative.
	 */
	public HashSet(int initialCapacity) {
		if(initialCapacity <= 0) {
			throw new IllegalArgumentException("The initial capacity of " + initialCapacity + " is negative.");
		}
		if(initialCapacity > maximumCapacity) {
			initialCapacity = maximumCapacity;
		}
		initialCapacity = net.dulek.math.Math.roundUpPower2(initialCapacity); //Find the next power of 2 greater than or equal to initialCapacity.
		loadFactor = defaultLoadFactor;
		treshold = Math.min((int)(initialCapacity * loadFactor),maximumCapacity + 1); //Pre-compute the treshold.
		table = new Object[initialCapacity]; //Reserve the actual table.
	}

	/**
	 * Constructs an empty {@code HashSet} with the specified initial capacity
	 * and load factor.
	 * @param initialCapacity The initial capacity of the hash table.
	 * @param loadFactor The load factor.
	 * @throws IllegalArgumentException The initial capacity is negative or the
	 * load factor is not positive.
	 */
	public HashSet(int initialCapacity,final float loadFactor) {
		if(initialCapacity < 0) {
			throw new IllegalArgumentException("The initial capacity of " + initialCapacity + " is negative.");
		}
		if(initialCapacity > maximumCapacity) {
			initialCapacity = maximumCapacity;
		}
		if(loadFactor <= 0 || Float.isNaN(loadFactor)) {
			throw new IllegalArgumentException("The load factor of " + loadFactor + " is not positive.");
		}
		if(loadFactor >= 1) {
			throw new IllegalArgumentException("The load factor of " + loadFactor + " is too high.");
		}
		initialCapacity = net.dulek.math.Math.roundUpPower2(initialCapacity); //Find the next power of 2 greater than or equal to initialCapacity.
		this.loadFactor = loadFactor;
		treshold = Math.min((int)(initialCapacity * loadFactor),maximumCapacity + 1); //Pre-compute the treshold.
		table = new Object[initialCapacity]; //Reserve the actual table.
	}

	/**
	 * Constructs a new set containing the elements in the specified collection.
	 * The set is created with a default load factor (0.7) and an initial
	 * capacity sufficient to contain the elements in the specified collection.
	 * @param c The collection whose elements are to be placed into this set.
	 * @throws NullPointerException The specified collection is {@code null}.
	 */
	public HashSet(final Collection<? extends E> c) {
		if(c == null) {
			throw new NullPointerException("The specified collection to initialise the set with is null.");
		}
		loadFactor = defaultLoadFactor;
		final int initialCapacity = net.dulek.math.Math.roundUpPower2(Math.max((int)(c.size() / defaultLoadFactor),defaultInitialCapacity)); //Make the capacity enough for all elements but no lower than the default initial capacity.
		treshold = Math.min((int)(initialCapacity * defaultLoadFactor),maximumCapacity + 1); //Pre-compute the treshold.
		table = new Object[initialCapacity]; //Reserve the actual table.

		//Add all elements of the collection.
		final int tMax = initialCapacity - 1;
		ADDALL:
		for(E element : c) { //Add all elements from the collection.
			Object object = element;
			if(object == null) { //Don't try to add null. Add the object representing null instead.
				object = nullElement;
			}
			//Compute the desired bucket for the element.
			final int hash = object.hashCode();
			int index = hash ^ ((hash >>> 20) ^ (hash >>> 12)); //These shifts ensure that hash codes that differ only by constant multiples are at each bit position have a bounded number of collisions.
			index = (index ^ (index >>> 7) ^ (index >>> 4)) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

			int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
			Object elem;
			//Search for the element.
			while((elem = table[index]) != null) {
				if(elem.hashCode() == hash && elem.equals(object)) { //The element is already in the table.
					continue ADDALL; //Skip this element. Continue with the next one.
				}
				index = (index + offset++) & tMax;
			}
			//table[index] is now null (since the while loop ended) so this is a free spot.
			table[index] = object; //Place it at our free spot.
			modCount++;
		}
	}

	/**
	 * Creates a hash set from a clone operation. The provided parameters are
	 * stored literally in the fields.
	 * @param loadFactor The load factor.
	 * @param size The number of elements in the set.
	 * @param table The hash table for the set.
	 */
	private HashSet(final float loadFactor,final int size,final Object[] table) {
		this.loadFactor = loadFactor;
		this.size = size;
		this.table = table;
		treshold = Math.min((int)(table.length * loadFactor),maximumCapacity + 1);
	}

	/**
	 * Adds the specified element to this set if it is not already present. More
	 * formally, adds the specified element {@code e} to this set if this set
	 * contains no element {@code f} such that
	 * {@code (e == null ? f == null : e.equals(f))}. If this set already
	 * contains the element, the call leaves the set unchanged and returns
	 * {@code false}.
	 * @param element The element to be added to this set.
	 * @return {@code true} if this set did not already contain the specified
	 * element, or {@code false} otherwise.
	 */
	@Override
	public boolean add(final E element) {
		Object object = element;
		if(object == null) { //Don't try to add null. Add the object representing null instead.
			object = nullElement;
		}
		final int tMax = table.length - 1;
		//Compute the desired bucket for the element.
		final int hash = object.hashCode();
		int index = hash ^ ((hash >>> 20) ^ (hash >>> 12)); //These shifts ensure that hash codes that differ only by constant multiples are at each bit position have a bounded number of collisions.
		index = (index ^ (index >>> 7) ^ (index >>> 4)) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

		int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
		Object elem;
		//Search for the element.
		while((elem = table[index]) != null) {
			if(elem == tombstone) { //There was an object here...
				//Continue the search, but no more gravedigging.
				int searchIndex = (index + offset++) & tMax;
				while((elem = table[searchIndex]) != null) {
					if(elem.hashCode() == hash && elem.equals(object)) { //The element is already in the table.
						return false;
					}
					searchIndex = (searchIndex + offset++) & tMax;
				}
				table[index] = object; //Place it at the tombstone.
				modCount++;
				if(++size > treshold) { //Getting too big.
					resize(table.length << 1);
				}
				return true;
			}
			if(elem.hashCode() == hash && elem.equals(object)) { //The element is already in the table.
				return false;
			}
			index = (index + offset++) & tMax;
		}
		//table[index] is now null (since the while loop ended) so this is a free spot.
		table[index] = object; //Place it at our free spot.
		modCount++;
		if(++size > treshold) { //Getting too big.
			resize(table.length << 1);
		}
		return true;
	}

	/**
	 * Adds all of the elements in the specified collection to this set. If the
	 * specified collection is this set, a
	 * {@link ConcurrentModificationException} is thrown.
	 * <p>This implementation iterates over the specified collection, and adds
	 * each object returned by the iterator to this collection, in turn.</p>
	 * @param c The collection containing elements to be added to this set.
	 * @return {@code true} if this set changed as a result of the call.
	 * @throws NullPointerException The specified collection is {@code null}.
	 */
	@Override
	public boolean addAll(final Collection<? extends E> c) {
		if(c == null) {
			throw new NullPointerException("The specified collection to add to the set is null.");
		}
		//Make sure we have enough capacity, even if they're all new elements.
		if(size + c.size() >= treshold) {
			if(table.length == maximumCapacity) { //We're already at maximum capacity. Don't trigger this again.
				treshold = Integer.MAX_VALUE;
			} else {
				final int newCapacity = net.dulek.math.Math.roundUpPower2((int)((size + c.size()) / loadFactor));
				resize(newCapacity);
				//Add all elements, but don't check for tombstones or the treshold (since we just rehashed everything anyways).
				boolean modified = false;
				final int tMax = newCapacity - 1;
				ADDINGALL:
				for(Object element : c) {
					if(element == null) { //Don't try to add null. Add the object representing null instead.
						element = nullElement;
					}

					//Compute the desired bucket for the element.
					final int hash = element.hashCode();
					int index = hash ^ ((hash >>> 20) ^ (hash >>> 12)); //These shifts ensure that hash codes that differ only by constant multiples are at each bit position have a bounded number of collisions.
					index = (index ^ (index >>> 7) ^ (index >>> 4)) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

					int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
					Object elem;
					//Search for the element.
					while((elem = table[index]) != null) {
						if(elem.hashCode() == hash && elem.equals(element)) { //The element is already in the table.
							continue ADDINGALL; //Continue with the next element.
						}
						index = (index + offset++) & tMax;
					}
					//table[index] is now null (since the while loop ended) so this is a free spot.
					table[index] = element; //Place it at our free spot.
					size++;
					modified = true;
				}
				if(modified) {
					modCount++;
					return true;
				}
				return false;
			}
		}

		//Add all elements, but keep checking for tombstones.
		boolean modified = false;
		for(E element : c) { //Add all elements from the collection.
			modified |= add(element);
		}
		return modified;
	}

	/**
	 * Removes all of the elements from this set. The set will be empty after
	 * this call returns.
	 */
	@Override
	public void clear() {
		modCount++;
		final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
		for(int i = t.length - 1;i >= 0;i--) {
			t[i] = null; //Erase the table.
		}
		size = 0;
	}

	/**
	 * Returns a shallow copy of this {@code HashSet} instance: The elements
	 * themselves are not cloned.
	 * @return A shallow copy of this set.
	 * @throws CloneNotSupportedException The {@code clone()} operation is not
	 * supported by the {@code Object} class. This also allows extensions of
	 * this implementation to not support {@code clone()}.
	 */
	@Override
	@SuppressWarnings("unchecked") //Caused by cloning the super-object and casting it back to HashSet.
	public HashSet<E> clone() throws CloneNotSupportedException {
		final HashSet<E> result = (HashSet<E>)super.clone();
		result.table = new Object[table.length];
		System.arraycopy(table,0,result.table,0,table.length);
		result.loadFactor = loadFactor;
		result.size = size;
		result.treshold = treshold;
		return result;
	}

	/**
	 * Returns {@code true} if this set contains the specified element. More
	 * formally, returns {@code true} if and only if this set contains an
	 * element {@code e} such that
	 * {@code (o == null ? e == null : o.equals(e))}.
	 * @param o The element whose presence in this set is to be tested.
	 * @return {@code true} if this set contains the specified element, or
	 * {@code false} otherwise.
	 */
	@Override
	public boolean contains(Object o) {
		if(o == null) { //For null-elements, search for the object representing a null-element.
			o = nullElement;
		}

		final Object[] t = table; //Local cache for speed without JIT.
		final int tMax = t.length - 1;

		//Compute the desired bucket for the element.
		final int hash = o.hashCode();
		int index = hash ^ ((hash >>> 20) ^ (hash >>> 12)); //These shifts ensure that hash codes that differ only by constant multiples at each bit position have a bounded number of collisions.
		index = (index ^ (index >>> 7) ^ (index >>> 4)) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

		int offset = 1;
		Object elem;
		while((elem = t[index]) != null) { //Search until we hit an empty spot.
			if(elem.hashCode() == hash && elem.equals(o)) { //There is the element we seek.
				return true;
			}
			index = (index + offset++) & tMax;
		}
		return false; //Reached an empty spot and haven't found it yet.
	}

	/**
	 * Returns {@code true} if this set contains all of the elements in the
	 * specified collection.
	 * <p>This implementation iterates over the specified collection, checking
	 * each element returned by the iterator in turn to see if it's contained in
	 * this set. If all elements are so contained, {@code true} is returned, and
	 * otherwise {@code false} is returned.</p>
	 * @param c The collection to be checked for containment in this set.
	 * @return {@code true} if this set contains all of the elements in the
	 * specified collection.
	 * @throws ClassCastException The types of one or more elements in the
	 * specified collection are not subclasses of the type of elements in this
	 * set.
	 * @throws NullPointerException The specified collection is {@code null}.
	 */
	@Override
	@SuppressWarnings("element-type-mismatch") //Caused by checking elements of the collection for containment in this set.
	public boolean containsAll(final Collection<?> c) {
		if(c == null) {
			throw new NullPointerException("The specified collection to check for containment in this set is null.");
		}
		for(final Object element : c) { //Check for every element in the collection whether it is contained in the set.
			if(!contains(element)) { //Throws ClassCastException if not a subclass of E.
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares the specified object with this set for equality. Returns
	 * {@code true} if the given object is also a set, the two sets have the
	 * same size, and every member of the specified set is contained in this
	 * set. This ensures that the {@code equals(Object)} method works properly
	 * across different implementations of the {@code Set} interface.
	 * <p>This implementation first checks if the specified object is this set;
	 * if so it returns {@code true}. Then, it checks if the specified object is
	 * a set whose size is identical to the size of this set; if not, it returns
	 * {@code false}. If so, it returns {@code containsAll((Set)o)}.
	 * @param o The object to be compared for equality with this set.
	 * @return {@code true} if the specified object is equal to this set, or
	 * {@code false} otherwise.
	 */
	@Override
	public boolean equals(final Object o) {
		if(o == this) { //It's the same object.
			return true;
		}
		if(o == null) { //It's not even an object.
			return false;
		}
		if(!(o instanceof Set)) { //It's not even a set.
			return false;
		}
		final Set<?> set = (Set)o;
		if(set.size() != size) { //The cardinalities of the sets are not equal.
			return false;
		}
		try {
			return containsAll(set);
		} catch(final ClassCastException e) { //Generic type arguments are not E.
			return false;
		}
	}

	/**
	 * Returns the hash code for this set. The hash code of a set is defined to
	 * be the sum of the hash codes of the elements in the set, where the hash
	 * code of a {@code null}-element is defined to be zero. This ensures that
	 * {@code s1.equals(s2)} implies that {@code s1.hashCode() == s2.hashCode()}
	 * for any two sets {@code s1} and {@code s2}, as required by the general
	 * contract of {@link Object#hashCode()}.
	 * @return The hash code for this set.
	 * @see Set#hashCode()
	 */
	@Override
	public int hashCode() {
		final Object[] t = table; //Cache a local copy for faster access without JIT compiler.

		int result = 0;
		for(int i = t.length - 1;i >= 0;i--) { //Iterate over the buckets of the hash table.
			final Object elem;
			if((elem = t[i]) != null && elem != tombstone && elem != nullElement) { //Don't count empty buckets, tombstones or the null-element in the hash.
				result += elem.hashCode(); //Take the sum of all hashcodes.
			}
		}
		return result;
	}

	/**
	 * Returns {@code true} if this set contains no elements.
	 * @return {@code true} if this set contains no elements, or {@code false}
	 * otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Returns an iterator over the elements in this set. The elements are
	 * returned in no particular order, as the order depends on the order in
	 * which they are stored in the hash table, and this is unspecified.
	 * <p>Note that the iterator will search through all buckets of the hash
	 * table to search for the elements of the set. The time complexity of a
	 * complete iteration of the set scales with the capacity of the hash table
	 * plus the number of elements in the set. Also, the time between two
	 * consecutive calls to {@link Iterator#next()} may vary.</p>
	 * <p>The iterator is fail-fast, which means that if a structural
	 * modification is made to the hash set after this method call, the iterator
	 * returned by this method call will throw a
	 * {@link ConcurrentModificationException} when {@link Iterator#next()} or
	 * {@link Iterator#remove()} is called. This behaviour prevents
	 * nondeterministic or unexpected behaviour caused by the concurrent
	 * modification of the set while it is being iterated over. Rather than
	 * giving inconsequent results, it always fails to give a result.</p>
	 * @return An iterator over the elements in this set.
	 */
	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}

	/**
	 * Removes the specified element from this set if it is present. More
	 * formally, removes an element {@code e} such that
	 * {@code (element == null ? e == null : element.equals(e))}, if this set
	 * contains such an element. Returns {@code true} if this set contained the
	 * element (or equivalently, if this set changed as a result of the call).
	 * This set will not contain the element once the call returns.
	 * @param element The object to be removed from this set, if present.
	 * @return {@code true} if the set contained the specified element, or
	 * {@code false} otherwise.
     */
	@Override
	public boolean remove(Object element) {
		if(element == null) { //Don't try to remove null. Remove the object representing null instead.
			element = nullElement;
		}

		final Object[] t = table; //Local cache for speed without JIT.
		final int tMax = t.length - 1;

		//Compute the desired bucket for the element.
		final int hash = element.hashCode();
		int index = hash ^ ((hash >>> 20) ^ (hash >>> 12)); //These shifts ensure that hash codes that differ only by constant multiples at each bit position have a bounded number of collisions.
		index = (index ^ (index >>> 7) ^ (index >>> 4)) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

		Object elem;
		int offset = 1;
		while((elem = t[index]) != null) { //Look for the object.
			if(elem.hashCode() == hash && elem.equals(element)) { //This is the element we seek.
				t[index] = tombstone; //R.I.P.
				modCount++;
				size--;
				return true;
			}
			index = (index + offset++) & tMax;
		}
		return false; //Reached an empty spot without any hit. Not found.
	}

	/**
	 * Removes from this set all of its elements that are contained in the
	 * specified collection. If the specified collection is also a set, this
	 * operation effectively modifies this set so that its value is the
	 * asymmetric set difference of the two sets.
	 * <p>This implementation iterates over either the specified collection or
	 * over the set, based on which is smaller: the size of the collection or
	 * the capacity of the hash table. Since the hash table iterates over the
	 * total size of the table rather than just the elements, its table capacity
	 * is compared rather than the cardinality of the set. When iterating over
	 * the collection, each element of the collection is removed from the set if
	 * it is present. When iterating over the set, each element of the set is
	 * removed from the set if it is contained in the collection.</p>
	 * @param c The collection containing elements to be removed from this set.
	 * @return {@code true} if this set changed as a result of the call, or
	 * {@code false} otherwise.
	 * @throws ClassCastException The class of an element of the specified
	 * collection is not a subclass of the types of elements in this set.
	 * @throws NullPointerException The specified collection is {@code null}.
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		if(c == null) {
			throw new NullPointerException("The specified collection to remove all elements from is null.");
		}
		final Object[] t = table; //Local cache for speed without JIT.
		int removedElements = 0;

		//Pick whichever method is fastest:
		if(table.length > c.size() || (c instanceof List && table.length > Math.sqrt(c.size()))) { //Iterate over c, removing all elements from this set. Lists have linear contains() methods, so they get special treatment.
			final int tMax = t.length - 1;

			for(Object element : c) { //Iterate over the collection.
				if(element == null) { //Don't try to remove null. Remove the object representing null instead.
					element = nullElement;
				}

				//Compute the desired bucket for the element.
				final int hash = element.hashCode();
				int index = hash ^ ((hash >>> 20) ^ (hash >>> 12)); //These shifts ensure that hash codes that differ only by constant multiples at each bit position have a bounded number of collisions.
				index = (index ^ (index >>> 7) ^ (index >>> 4)) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

				Object elem;
				int offset = 1;
				while((elem = t[index]) != null) { //Look for the object.
					if(elem.hashCode() == hash && elem.equals(element)) { //This is the element we seek.
						t[index] = tombstone; //R.I.P.
						removedElements++;
						break;
					}
					index = (index + offset++) & tMax;
				}
				//Element not found. Continue with the next element.
			}
			if(removedElements >= 0) { //That actually removed something.
				modCount++;
				size -= removedElements;
				return true;
			}
			return false;
		}
		//Otherwise, iterate over the set, removing all elements that are in c.
		for(int i = t.length - 1;i >= 0;i--) {
			final Object elem;
			if((elem = t[i]) != null && elem != tombstone && ((elem == nullElement && c.contains(null)) || c.contains(elem))) { //If it's the null-element, check if c has a null-element. Otherwise, just check for the element itself.
				t[i] = tombstone; //R.I.P.
				removedElements++;
			}
		}
		if(removedElements >= 0) { //That actually removed something.
			modCount++;
			size -= removedElements;
			return true;
		}
		return false;
	}

	/**
	 * Retains only the elements in this set that are contained in the specified
	 * collection. In other words, removes from this set all of its elements
	 * that are not contained in the specified collection. When the method call
	 * returns, the set will contain the intersection of the original elements
	 * of the set and the elements of the collection.
	 * <p>This implementation iterates over the elements of the set and checks
	 * for each element whether it is contained in the specified collection,
	 * removing it if it is not contained.</p>
	 * <p>List collections get special treatment. Their
	 * {@link List#contains(Object)} method is generally linear-time and their
	 * iterator constant per element. Therefore, when encountered with a list of
	 * reasonable size, this method will instead clear the set and then iterate
	 * over the list, re-adding those elements that were in the original set.
	 * </p>
	 * @param c The collection to retain the elements from.
	 * @return {@code true} if this set changed as a result of the call, or
	 * {@code false} otherwise.
	 * @throws ClassCastException The class of an element of the specified
	 * collection is not a subclass of the elements of this set.
	 * @throws NullPointerException The specified collection is {@code null}.
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
		if(c == null) {
			throw new NullPointerException("The specified collection to retain all elements from is null.");
		}
		final Object[] t = table; //Cache a local copy for faster access without JIT compiler.

		if(c instanceof List && t.length > Math.sqrt(c.size())) { //Lists have linear contains() methods and will get special treatment.
			//Iterate over the list and add all elements that are both in the list and in the original table.
			final Object[] newTable = new Object[t.length];
			final int tMax = t.length - 1;
			int retainedElements = 0;

			for(Object element : c) { //See if it's in the original hash table.
				if(element == null) { //Don't try to find null. Find the object representing null instead.
					element = nullElement;
				}

				//Compute the desired bucket for the element.
				final int hash = element.hashCode();
				int index = hash ^ ((hash >>> 20) ^ (hash >>> 12)); //These shifts ensure that hash codes that differ only by constant multiples at each bit position have a bounded number of collisions.
				index = (index ^ (index >>> 7) ^ (index >>> 4)) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

				Object elem;
				int offset = 1;
				while((elem = t[index]) != null) { //Look for the object.
					if(elem == tombstone) { //Copy over tombstones as well, or we'd have to rehash everything.
						newTable[index] = elem;
					} else if(elem.hashCode() == hash && elem.equals(element)) { //Found it!
						newTable[index] = elem; //Copy it to the new table.
						t[index] = tombstone; //Don't find it a second time.
						retainedElements++;
						break;
					}
					index = (index + offset++) & tMax;
				}
				//Element not found. Continue with the next element.
			}
			for(int i = tMax;i >= 0;i--) { //Place tombstones in the new table where elements have been removed.
				if(t[i] != null && newTable[i] == null) {
					newTable[i] = tombstone;
				}
			}
			table = newTable; //Use the new table. The old one was modified anyhow.
			if(retainedElements < size) { //That actually removed something.
				modCount++;
				size = retainedElements;
				return true;
			}
			return false;
		}

		int removedElements = 0;
		for(int i = t.length - 1;i >= 0;i--) {
			final Object elem;
			if((elem = t[i]) != null && elem != tombstone && ((elem == nullElement && !c.contains(null)) || !c.contains(elem))) { //If it's the null-element, check if c has a null-element. Otherwise, just check for the element itself.
				t[i] = tombstone; //R.I.P.
				removedElements++;
			}
		}
		if(removedElements > 0) { //That actually removed something.
			modCount++;
			size -= removedElements;
			return true;
		}
		return false;
	}

	/**
	 * Returns the number of elements in this set.
	 * @return The number of elements in this set.
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns an array containing all of the elements in this set. The elements
	 * are in no particular order.
	 * <p>The returned array will be "safe" in that no references to it are
	 * maintained by this set. The caller is thus free to modify the returned
	 * array; it will not have any effect on the set.</p>
	 * <p>This implementation searches through the hash table for active
	 * elements and copies every element it finds to the resulting array.
	 * @return An array containing all of the elements in this set.
	 */
	@Override
	public Object[] toArray() {
		final Object[] t = table; //Cache a local copy for faster access without JIT compiler.

		final Object[] result = new Object[size];
		int pos = 0; //Position in the result table to put the next element.
		for(int i = t.length - 1;i >= 0;i--) {
			final Object elem = t[i];
			if(elem != null && elem != tombstone) {
				if(elem == nullElement) {
					pos++; //This element in the result can remain null.
				} else {
					result[pos++] = elem;
				}
			}
		}

		return result;
	}

	/**
	 * Returns an array containing all of the elements in this set; the runtime
	 * type of the returned array is that of the specified array. If the set
	 * fits in the specified array, it is returned therein. Otherwise, a new
	 * array is allocated with the runtime type of the specified array and the
	 * size of this set.
	 * <p>If this set fits in the specified array with room to spare (i.e. the
	 * array has more elements than this set), the elements following the end of
	 * the set remain {@code null}.</p>
	 * <p>The elements are returned in no particular order.</p>
	 * @param <E> The type of elements in the new array.
	 * @param a The array into which the elements of this set are to be stored,
	 * if it is big enough; otherwise, a new array of the same runtime type is
	 * allocated for this purpose.
	 * @return An array containing all of the elements in this set.
	 * @throws ArrayStoreException The runtime type of the specified array is
	 * not a supertype of the runtime type of every element in this collection.
	 * @throws NullPointerException The specified array is {@code null}.
	 */
	@Override
	@SuppressWarnings("unchecked") //The reflection array creation, and casting Objects from the hash table to E's for the result.
	public <E> E[] toArray(final E[] a) {
		final Object[] t = table; //Cache a local copy for faster access without JIT compiler.

		final E[] result;
		if(a.length >= size) { //Array suffices.
			result = a;
		} else { //Too many elements for the array.
			result = (E[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(),size); //Allocate a new array.
		}
		int pos = 0; //Position in the result table to put the next element.
		for(int i = t.length - 1;i >= 0;i--) {
			final E elem = (E)t[i]; //Safe cast, since t[] contains only E's.
			if(elem != null && elem != tombstone) {
				if(elem == nullElement) {
					pos++; //This element in the result can remain null.
				} else {
					result[pos++] = elem;
				}
			}
		}

		return result;
	}

	/**
	 * Returns a {@code String} representation of this set. The {@code String}
	 * representation reflects the mathematical notation of a set. It consists
	 * of a list of the set's elements, in no particular order, enclosed in
	 * curly brackets ({@code "&#x7B;&#x7D;"}). Adjacent elements are separated
	 * by commas (","). Elements are converted to strings by their
	 * {@link Object#toString()} methods.
	 * @return A {@code String} representation of this set.
	 */
	@Override
	public String toString() {
		final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
		final int s = size;

		final StringBuilder result = new StringBuilder(2 + 5 * s); //Reserve 2 chars for brackets, 4 for each element and 1 for the commas after each element.
		result.append('{');
		int count = 0; //The number of elements found so far. This tells us whether this is the last element (to append a comma or not).
		for(int i = t.length - 1;i >= 0;i--) {
			final Object elem = t[i];
			if(elem != null && elem != tombstone) { //Found an element.
				if(elem == this) { //Don't go into infinite recursion if it contains itself!
					result.append("(this Set)");
				} else if(elem == nullElement) { //This one has a special string: null.
					result.append("null");
				} else {
					result.append(elem); //Calls the toString() method.
				}
				if(++count < s) { //That was not the last element. Add a comma.
					result.append(',');
				}
			}
		}
		result.append('}');
		return result.toString(); //Finalise and return.
	}

	/**
	 * Helper method to rehash the table to a new size. A new array will be
	 * allocated for the hash table with the specified capacity. All elements in
	 * the old table will be rehashed and placed in the new table.
	 * @param newCapacity The capacity of the new hash table. This must be a
	 * power of {@code 2}!
	 */
	protected void resize(final int newCapacity) {
		final Object[] newTable = new Object[newCapacity];
		final int tMax = newCapacity - 1;
		for(int i = table.length - 1;i >= 0;i--) {
			final Object element = table[i];
			if(element == null || element == tombstone) { //This bucket was empty. And don't put tombstones in the new table either.
				continue;
			}

			final int hash = element.hashCode();
			int index = hash ^ ((hash >>> 20) ^ (hash >>> 12)); //These shifts ensure that hash codes that differ only by constant multiples are at each bit position have a bounded number of collisions.
			index = (index ^ (index >>> 7) ^ (index >>> 4)) & tMax; //Keep only the lower bits (newCapacity - 1 has all bits on, since newCapacity is a power of 2). Same as modulo length.

			int offset = 1;
			while(newTable[index] != null) { //Search for a spot to place it.
				index = (index + offset++) & tMax;
			}
			//Since newTable[index] is now null due to the while loop, we have an empty spot for the element.
			newTable[index] = element;
		}
		table = newTable; //Use the new table from now on.
		treshold = Math.min((int)(newCapacity * loadFactor),maximumCapacity + 1); //Set a new treshold for the next rehash.
	}

	/**
	 * Reconstitute the {@code HashSet} instance from a stream (that is,
	 * deserialise it).
	 * @param s The stream to read the state of a {@code HashSet} from.
	 * @throws ClassNotFoundException The SerialVersionUID of the class does not
	 * match any known version of {@code HashSet}.
	 * @throws IOException Something went wrong reading from the specified
	 * stream.
	 */
	private void readObject(final ObjectInputStream s) throws IOException,ClassNotFoundException {
		s.defaultReadObject(); //Read the default serialisation magic.

		final int capacity = s.readInt(); //Read table capacity.
		final Object[] t = new Object[capacity]; //Create the actual hash table for the hash set.
		loadFactor = s.readFloat(); //Read load factor.
		size = s.readInt(); //Read size.
		for(int i = size - 1;i >= 0;i--) { //Read each element.
			final Object element = s.readObject(); //Read the element itself.
			final int hashCode = element.hashCode();
			int index = hashCode ^ ((hashCode >>> 20) ^ (hashCode >>> 12)); //Find the index at which to place it.
			index = (index ^ (index >>> 7) ^ (index >>> 4)) & (capacity - 1);
			t[index] = element; //Store it at that location.
		}
		table = t; //Store the hash table in the object.
		treshold = (int)(table.length * loadFactor);
	}

	/**
	 * Save the state of this {@code HashSet} instance to a stream (that is,
	 * serialise it).
	 * @serialData The capacity ({@code int}) and load factor ({@code float}) of
	 * the hash table are emitted, followed by the number of elements in the set
	 * ({@code int}), followed by all elements ({@code Object}s) in no
	 * particular order.
	 * @param s The stream to write the state of this {@code HashSet} to.
	 * @throws IOException Something went wrong writing to the specified stream.
	 */
	private void writeObject(final ObjectOutputStream s) throws IOException {
		Object[] t = table; //Cache a local copy for faster access without JIT compiler.

		s.defaultWriteObject(); //Write the default serialisation magic.

		s.writeInt(t.length); //Write table capacity.
		s.writeFloat(loadFactor); //Write load factor.
		s.writeInt(size); //Write size.
		for(int i = t.length - 1;i >= 0;i--) {
			if(t[i] != null) {
				s.writeObject(t[i]); //Write out each element.
			}
		}
	}

	/**
	 * This is a custom implementation of {@link Iterator} that iterates over
	 * the elements of a hash set. The iterator gives the elements of the set in
	 * no particular order. The actual order will be the reverse order of the
	 * elements in the set as they are ordered in the internal hash table of the
	 * set.
	 * <p>Note that the iterator will search through all buckets of the hash
	 * table to search for the elements of the set. The time complexity of a
	 * complete iteration of the set scales with the capacity of the hash table
	 * plus the number of elements in the set. Also, the time between two
	 * consecutive calls to {@link Iterator#next()} may vary.</p>
	 * <p>The iterator is fail-fast, which means that if a structural
	 * modification is made to the hash set after this method call, the iterator
	 * will throw a {@link ConcurrentModificationException} when {@link #next()}
	 * or {@link #remove()} is called. This behaviour prevents nondeterministic
	 * or unexpected behaviour caused by the concurrent modification of the set
	 * while it is being iterated over. Rather than giving inconsequent results,
	 * it always fails to give a result.</p>
	 */
	protected class Itr implements Iterator<E> {
		/**
		 * The expected modification count of the accompanying {@code HashSet}.
		 * If this is different from the actual modification count of the set, a
		 * concurrent modification on the set has occurred and every method
		 * should throw a {@link ConcurrentModificationException}.
		 */
		protected int expectedModCount;

		/**
		 * The current slot in the iteration. If this index is negative, the
		 * iteration is complete.
		 */
		protected int index;

		/**
		 * The index of the last element that was returned by a call to
		 * {@link #next()}. This element will be removed from the table if
		 * {@link #remove()} would be called. A value of -1 indicates that
		 * {@link #next()} has not yet been called or not since the last call to
		 * {@link #remove()} (or that {@link #next()} didn't return an element).
		 */
		protected int last = -1;

		/**
		 * Creates a new iterator over the {@code HashSet}. The iterator will
		 * start at the last element in the table, iterating towards index
		 * {@code 0}.
		 */
		protected Itr() {
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			for(index = t.length - 1;index >= 0 && (t[index] == null || t[index] == tombstone);index--); //Set index in advance to the index of the first element.
			expectedModCount = modCount; //Store the modCount of the HashSet. From here, concurrent modification will throw an exception.
		}

		/**
		 * Returns {@code true} if the iteration has more elements. In other
		 * words, returns {@code true} if {@link #next()} would return an
		 * element of the set rather than throwing a
		 * {@code NoSuchElementException}.
		 * @return {@code true} if the iteration has more elements, or
		 * {@code false} otherwise.
		 */
		@Override
		public boolean hasNext() {
			return index >= 0;
		}

		/**
		 * Returns the next element in the iteration.
		 * @return The next element in the iteration.
		 * @throws ConcurrentModificationException The set was structurally
		 * modified between the constructing of this iterator and the calling of
		 * this method.
		 * @throws NoSuchElementException The iteration has no more elements.
		 */
		@Override
		@SuppressWarnings("unchecked") //Caused by the casting of a table element to the returned E.
		public E next() {
			if(modCount != expectedModCount) { //Some concurrent modification has been made on the set!
				throw new ConcurrentModificationException();
			}
			if(index < 0) { //We're through the array.
				last = -1;
				throw new NoSuchElementException();
			}
			final Object[] t = table; //Cache a local copy for faster access without JIT compiler.
			last = index;
			for(index--;index >= 0 && (t[index] == null ||  t[index] == tombstone);index--); //Set the index on the next position.
			if(t[last] == nullElement) {
				return null; //Return null instead of the element representing null.
			}
			return (E)t[last]; //Safe cast, since the table contains only E's.
		}

		/**
		 * Removes from the set the last element returned by this iterator. This
		 * method can be called only once per call to {@link #next()}.
		 * @throws ConcurrentModificationException The set was structurally
		 * modified between the constructing of this iterator and the calling of
		 * this method.
		 * @throws IllegalStateException The {@link #next()} method has not yet
		 * been called, or the {@code remove()} method has already been called
		 * after the last call to the {@code next()} method.
		 */
		@Override
		public void remove() {
			if(last < 0) { //Nothing to remove.
				throw new IllegalStateException();
			}
			if(modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			//Delete the element at 'last'.
			if(table[last] != null && table[last] != tombstone) {
				table[last] = tombstone; //R.I.P.
				size--;
				modCount++;
				expectedModCount = modCount; //This modification is expected since the iterator made it.
				last = -1; //Set this to negative so the next call doesn't try to remove it again.
			}
		}
	}
}