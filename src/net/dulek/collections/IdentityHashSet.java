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
import java.util.Collection;
import java.util.List;

/**
 * This class represents a set of elements, implemented using a hash table. The
 * hash table uses the identity hash ({@code System.identityHashCode(Object)})
 * rather than the element's own hash code, to determine the element's identity.
 * A set is an unordered collection of elements, containing no duplicate
 * elements. Being unordered, the set makes no guarantees as to the order of its
 * elements. This set is unsynchronised, and permits the {@code null} element.
 * <p>This implementation is heavily based on the default
 * {@link java.util.HashMap HashMap} implementation of Java 7, but some
 * improvements were made to better facilitate sets rather than key-value
 * mappings, since the default implementation of {@link HashSet} maintains a
 * heavy-weight {@code HashMap} to implement the set. Additionally, the default
 * implementation provides two ways of hashing its keys: the standard
 * {@code hashCode()} method of the keys or a cryptographically more secure
 * variant for {@code String}s that uses a random seed. This implementation
 * foregoes these, and uses the identity hash code that is based on the object's
 * location in memory. This may result in elements that are actually equivalent
 * being placed multiple times in the set (if they are different instances).
 * Lastly, the default implementation uses the separate chaining method for
 * preventing collisions. In an effort to rid the implementation from additional
 * "entry" instances, separate chaining is infeasable. Instead, open addressing
 * with quadratic probing was chosen. Quadratic probing avoids the clustering
 * problem better than linear probing and in Java the locality of reference
 * makes no difference. It was chosen over more complex collision resolution
 * techniques for how it requires less memory overhead.</p>
 * <p>This implementation provides expected constant-time performance for all
 * basic operations of the set: {@link #add(E)}, {@link #remove(Object)} and
 * {@link #contains(Object)}. This expected performance expects that the
 * identity hash function disperses the elements properly among the buckets,
 * which may not be the case, for instance if the constructor of the elements
 * creates a constant number of other objects and elements are instantiated in
 * sequence. Iteration over the set requires time proportional to the "capacity"
 * of the hash table (the number of buckets) plus its size (the number of
 * elements). Thus, it's very important not to set the initial capacity too high
 * (or the load factor too low) if insertion performance is important.</p>
 * <p>An instance of {@code IdentityHashSet} has two parameters that affect its
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
 * <p>If many elements are to be stored in a {@code IdentityHashSet} instance,
 * creating it with a sufficiently large capacity will allow the elements to be
 * stored more efficiently than letting it perform automatic rehashing as needed
 * to grow the table.</p>
 * <p>Note that this implementation is not synchronised. If multiple threads
 * access the {@code IdentityHashSet} concurrently, and at least one of these
 * threads modifies the map structurally, it must be synchronised externally. A
 * structural modification is any operation that adds or removes one or more
 * elements. This is typically accomplished by synchronising on some object that
 * naturally encapsulates the set. If no such object exists, the set should be
 * "wrapped" using the
 * {@link java.util.Collections#synchronizedSet Collections.synchronizedSet}
 * methods. This is best done at creation time, to prevent accidental
 * unsynchronised access to the set:
 * {@code Set<...> s = Collections.synchronizedSet(new IdentityHashSet<>(...));}
 * </p>
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
 * @see HashSet
 * @see java.util.HashMap
 * @see Set
 * @version 1.0
 */
public class IdentityHashSet<E> extends HashSet<E> {
	/**
	 * The version of the serialised format of the set. This identifies a
	 * serialisation as being the serialised format of this class.
	 * <p>The serialised format of this set is compatible with that of
	 * {@link HashSet}. Therefore, it should be the same.</p>
	 */
	private static final long serialVersionUID = 4882436748081726100L;

	/**
	 * Constructs an empty {@code IdentityHashSet} with the default initial
	 * capacity (16) and the default load factor (0.7). Note that it is good
	 * practice to always specify an initial capacity if there is any idea of
	 * approximately how many elements the set will hold.
	 */
	public IdentityHashSet() {
		super();
	}

	/**
	 * Constructs an empty {@code IdentityHashSet} with the specified initial
	 * capacity and the default load factor (0.7).
	 * @param initialCapacity The initial capacity of the hash table.
	 * @throws IllegalArgumentException The initial capacity is negative.
	 */
	public IdentityHashSet(final int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Constructs an empty {@code IdentityHashSet} with the specified initial
	 * capacity and load factor.
	 * @param initialCapacity The initial capacity of the hash table.
	 * @param loadFactor The load factor.
	 * @throws IllegalArgumentException The initial capacity is negative or the
	 * load factor is not positive.
	 */
	public IdentityHashSet(final int initialCapacity,final float loadFactor) {
		super(initialCapacity,loadFactor);
	}

	/**
	 * Constructs a new set containing the elements in the specified collection.
	 * The set is created with a default load factor (0.7) and an initial
	 * capacity sufficient to contain the elements in the specified collection.
	 * @param c The collection whose elements are to be placed into this set.
	 * @throws NullPointerException The specified collection is {@code null}.
	 */
	public IdentityHashSet(final Collection<? extends E> c) {
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
			int index = System.identityHashCode(object) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

			int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
			Object elem;
			//Search for the element.
			while((elem = table[index]) != null) {
				if(elem == object) { //The element is already in the table.
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
	 * Adds the specified element to this set if it is not already present. More
	 * formally, adds the specified element {@code e} to this set if this set
	 * contains no element {@code f} such that {@code e == f}. If this set
	 * already contains the element, the call leaves the set unchanged and
	 * returns {@code false}.
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
		int index = System.identityHashCode(object); //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

		int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
		Object elem;
		//Search for the element.
		while((elem = table[index]) != null) {
			if(elem == tombstone) { //There was an object here...
				//Continue the search, but no more gravedigging.
				int searchIndex = (index + offset++) & tMax;
				while((elem = table[searchIndex]) != null) {
					if(elem == object) { //The element is already in the table.
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
			if(elem == object) { //The element is already in the table.
				return false;
			}
			index = (index + offset++) & tMax;
		}
		//table[index] is now null (since the while loop ended) so this is a free spot.
		table[index] = object;
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
			if(table.length == maximumCapacity) { //We're already at the maximum capacity. Don't trigger this again.
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
					int index = System.identityHashCode(element) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

					int offset = 1; //By how much we must grow the index while searching. Index grows quadratically.
					Object elem;
					//Search for the element.
					while((elem = table[index]) != null) {
						if(elem == element) { //The element is already in the table.
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
	 * Returns {@code true} if this set contains the specified element. More
	 * formally, returns {@code true} if and only if this set contains an
	 * element {@code e} such that {@code o == e}.
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
		int index = System.identityHashCode(o) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

		int offset = 1;
		Object elem;
		while((elem = t[index]) != null) { //Search until we hit an empty spot.
			if(elem == o) {
				return true;
			}
			index = (index + offset++) & tMax;
		}
		return false; //Reached an empty spot and haven't found it yet.
	}

	/**
	 * Removes the specified element from this set if it is present. More
	 * formally, removes an element {@code e} such that {@code element == e}, if
	 * this set contains such an element. Returns {@code true} if this set
	 * contained the element (or equivalently, if this set changed as a result
	 * of the call). This set will not contain the element once the call
	 * returns.
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
		int index = System.identityHashCode(element) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

		Object elem;
		int offset = 1;
		while((elem = t[index]) != null) { //Look for the object.
			if(elem == element) { //This is the element we seek.
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
	 * assymetric set difference of the two sets.
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
				if(element == null) { //Don't try to remove null. Remove the element representing null instead.
					element = nullElement;
				}

				//Compute the desired bucket for the element.
				int index = System.identityHashCode(element) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

				Object elem;
				int offset = 1;
				while((elem = t[index]) != null) { //Look for the object.
					if(elem == element) { //This is the element we seek.
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
				int index = System.identityHashCode(element) & tMax; //Keep only the lower bits (length - 1 has all bits on, since length is a power of 2). Same as modulo length.

				Object elem;
				int offset = 1;
				while((elem = t[index]) != null) { //Look for the object.
					if(elem == tombstone) { //Copy over tombstones as well, or we'd have to rehash everything.
						newTable[index] = elem;
					} else if(elem == element) { //Found it!
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
	 * Helper method to rehash the table to a new size. A new array will be
	 * allocated for the hash table with the specified capacity. All elements in
	 * the old table will be rehashed and placed in the new table.
	 * @param newCapacity The capacity of the new hash table. This must be a
	 * power of {@code 2}!
	 */
	@Override
	protected void resize(final int newCapacity) {
		final Object[] newTable = new Object[newCapacity];
		final int tMax = newCapacity - 1;
		for(int i = table.length - 1;i >= 0;i--) {
			final Object element = table[i];
			if(element == null || element == tombstone) { //This bucket was empty. And don't put tombstones in the new table either.
				continue;
			}

			int index = System.identityHashCode(element) & tMax; //Keep only the lower bits (newCapacity - 1 has all bits on, since newCapacity is a power of 2). Same as modulo length.

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
	 * Reconstitute the {@code IdentityHashSet} instance from a stream (that is,
	 * deserialise it).
	 * @param s The stream to read the state of an {@code IdentityHashSet} from.
	 * @throws ClassNotFoundException The serialVersionUID of the class does not
	 * match any known version of {@code IdentityHashSet}.
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
			t[System.identityHashCode(element) & (capacity - 1)] = element; //Hash to find the index at which to place it, and place it there.
		}
		table = t; //Store the hash table in the object.
		treshold = (int)(table.length * loadFactor);
	}

	/**
	 * Save the state of this {@code IdentityHashSet} instance to a stream (that
	 * is, serialise it).
	 * @serialData The capacity ({@code int}) and load factor ({@code float}) of
	 * the hash table are emitted, followed by the number of elements in the set
	 * ({@code int}), followed by all elements ({@code Object}s) in no
	 * particular order.
	 * @param s The stream to write the state of this {@code IdentityHashSet}
	 * to.
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
}