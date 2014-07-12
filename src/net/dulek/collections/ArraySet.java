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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class represents a set of elements, implemented using an array. A set is
 * an unordered collection of elements, containing no duplicate elements. More
 * formally, sets contain no pair of elements {@code e1} and {@code e2} such
 * that {@code e1.equals(e2)}, and at most one {@code null}-element. As implied
 * by its name, this collection models the mathematical <emph>set</emph>
 * abstraction. Being unordered, the set makes no guarantees as to the order of
 * its elements. The set is unsynchronised, and permits the {@code null}
 * element.
 * <p>This implementation is based on a simple array. Its elements are put in
 * this array in no particular order. Adding an element puts it at the far end
 * of the array. Removing an element will iterate over the array until the
 * element is found, remove it, and place the last element of the array at its
 * position. Checking for containment in the set will iterate over the array and
 * return whether it is found there. This provides extremely efficient
 * {@link #add(E)} operations and iteration, but provides linear time complexity
 * for the {@link #contains(Object)} and {@link #remove(Object)} operations.
 * This makes the {@code ArraySet} a good choice for small sets and for sets
 * that will only be iterated over.</p>
 * <p>The internal array of the set will have a limited capacity, that will have
 * to be extended when the set grows. If many elements are to be stored in an
 * {@code ArraySet} instance, creating it with a sufficiently large capacity
 * will allow the elements to be stored more efficiently than letting the
 * internal array expand automatically and copying over the elements to the new
 * array.</p>
 * <p>Note that this implementation is not synchronised. If multiple threads
 * access the {@code ArraySet} concurrently, and at least one of these threads
 * removes an element or has to expand the capacity of the array, it must be
 * synchronised externally. This is typically accomplished by synchronising on
 * some object that naturally encapsulates the set. If no such object exists,
 * the set should be "wrapped" using the
 * {@link java.util.Collections#synchronizedSet Collections.synchronizedSet}
 * methods. This is best done at creation time, to prevent accidental
 * unsynchronised access to the set:
 * {@code Set s = Collections.synchronizedSet(new ArraySet(...));}</p>
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
 * <p>This implementation of {@link Set} is primarily designed for applications
 * where small sets are used, but performance and memory are still critical.
 * Performance tests have been conducted (thoroughly, yet unscientifically), to
 * compare the performance of the {@code ArraySet} compared to the default
 * {@link java.util.HashSet} implementation that would otherwise be used. The
 * performance analysis per method is shown below.<p>
 * <ul><li>{@link #add} without initial capacity: {@code ArraySet} is faster
 * with up to 113 elements. For bigger sets, {@code HashSet} is faster and has
 * better complexity.</li>
 * <li>{@link #add} with an initial capacity: {@code ArraySet} is faster with up
 * to 144 elements. For bigger sets, {@code HashSet} is faster and has better
 * complexity.</li>
 * <li>{@link #addAll}: {@code HashSet} is always faster.</li>
 * <li>{@link #clear}: {@code ArraySet} is always faster, and has constant
 * complexity rather than linear.</li>
 * <li>{@link #clone}: {@code ArraySet} is always faster.</li>
 * <li>{@link #contains}: {@code HashSet} is always faster and has better
 * complexity.</li>
 * <li>{@link #containsAll}: {@code ArraySet} is faster with up to 45 elements.
 * For bigger sets, {@code HashSet} is faster and has better complexity.</li>
 * <li>{@link #hashCode}: {@code ArraySet} is always faster.</li>
 * <li>{@link #iterator}: {@code ArraySet} is always much faster.</li>
 * <li>{@link #remove}: {@code HashSet} is always faster and has better
 * complexity, but under 43 elements the difference is within the margin of
 * error.</li>
 * <li>{@link #removeAll}: {@code ArraySet} is faster with up to 63 elements.
 * For bigger sets, {@code HashSet} is faster and has better complexity.</li>
 * <li>{@link #retainAll}: {@code ArraySet} is always faster, but the difference
 * is very small at large sets.</li>
 * <li>{@link #toArray}: {@code ArraySet} is always faster.</li></ul>
 * @author Ruben Dulek
 * @param <E> The types of elements stored in this set.
 * @see Collection
 * @see Set
 * @version 1.0
 */
public class ArraySet<E> implements Set<E>,Cloneable,Serializable {
	/**
	 * The default initial capacity of the internal array. This will be the
	 * initial capacity of the array if no initial capacity is specified in the
	 * constructing of the set.
	 */
	private static final int defaultInitialCapacity = 8;

	/**
	 * The number of times this {@code ArraySet} has been structurally modified.
	 * Structural modifications are those that change the positions of elements,
	 * such as removing an element or expanding the internal array's capacity.
	 * This field is used to make iterators over the set fail-fast.
	 */
	protected transient int modCount;

	/**
	 * The version of the serialised format of the set. This identifies a
	 * serialisation as being the serialised format of this class.
	 */
	private static final long serialVersionUID = 837711974059991793L;

	/**
	 * The number of elements in this set.
	 */
	protected transient int size;

	/**
	 * The actual array to store the elements of the set. The array will be
	 * resized as necessary.
	 */
	protected transient Object[] table;

	/**
	 * Constructs an empty {@code ArraySet} with the default initial capacity
	 * (*). Note that it is good practice to always specify an initial capacity
	 * if there is any idea of approximately how many elements the set will
	 * hold.
	 */
	public ArraySet() {
		table = new Object[defaultInitialCapacity]; //Reserve the actual table.
	}

	/**
	 * Constructs an empty {@code ArraySet} with the specified initial capacity.
	 * @param initialCapacity The initial capacity of the internal array.
	 * @throws IllegalArgumentException The initial capacity is negative.
	 */
	public ArraySet(final int initialCapacity) {
		if(initialCapacity <= 0) {
			throw new IllegalArgumentException("The initial capacity of " + initialCapacity + " is negative.");
		}
		table = new Object[initialCapacity];
	}

	/**
	 * Constructs a new set containing the elements in the specified collection.
	 * The set is created with an initial capacity sufficient to contain the
	 * elements in the specified collection.
	 * @param c The collection whose elements are to be placed into this set.
	 * @throws NullPointerException The specified collection is {@code null}.
	 */
	public ArraySet(final Collection<? extends E> c) {
		table = new Object[Math.max(c.size(),defaultInitialCapacity)]; //Make the capacity enough for all elements but no lower than the default initial capacity.
		addAll(c); //Add all elements of the collection.
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
		if(!contains(element)) {
			if(size >= table.length) { //We're at capacity.
				resize(table.length << 1);
			}
			table[size++] = element; //Store the element here.
			return true;
		}
		return false; //The set already contained the element. Don't add it again.
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
		if(size + c.size() >= table.length) {
			resize(size + c.size());
		}
		boolean modified = false;
		for(E element : c) { //Add all elements of the collection.
			modified |= add(element);
		}
		return modified;
	}

	/**
	 * Removes all of the elements from this set. The set will be empty after
	 * this call returns.
	 * <p>Note that the memory used by the elements may not actually be freed
	 * immediately. This method simply puts all elements out of bounds in the
	 * table, and will only free the memory of its elements once they have been
	 * overwritten (or the set itself is picked up by the garbage collection).
	 * </p>
	 */
	@Override
	public void clear() {
		modCount++;
		size = 0; //No need to actually remove the elements.
	}

	/**
	 * Returns a shallow copy of this {@code ArraySet} instance: The elements
	 * themselves are not cloned.
	 * @return A shallow copy of this set.
	 */
	@Override
	@SuppressWarnings("unchecked") //Caused by cloning the super-object and casting it back to ArraySet.
	public ArraySet<E> clone() {
		try {
			final ArraySet<E> result = (ArraySet<E>)super.clone();
			result.table = new Object[table.length];
			System.arraycopy(table,0,result.table,0,size);
			result.size = size;
			return result;
		} catch(final CloneNotSupportedException e) {
			throw new InternalError("Clone should be supported by the supertype (Object), but apparently it isn't.");
		}
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
	public boolean contains(final Object o) {
		final Object[] t = table; //Cache locally for faster access without JIT compiler.
		if(o == null) { //Can't use equals() if this is null.
			for(int i = size - 1;i >= 0;i--) { //So search for null then.
				if(t[i] == null) {
					return true;
				}
			}
			return false;
		}
		for(int i = size - 1;i >= 0;i--) {
			if(o.equals(t[i])) {
				return true;
			}
		}
		return false;
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
	 * same size, and every member of this set is contained in the specified
	 * set. This ensures that the {@code equals(Object)} method works properly
	 * across different implementations of the {@code Set} interface.
	 * <p>This implementation first checks if the specified object is this set;
	 * if so it returns {@code true}. Then, it checks if the specified object is
	 * a set whose size is identical to the size of this set; if not, it returns
	 * {@code false}. If so, it returns {@code ((Set)o).containsAll(this)}.
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
			return set.containsAll(this);
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
		for(int i = size - 1;i >= 0;i--) { //Iterate over the elements of the array.
			if(t[i] != null) { //Don't try to hash null.
				result += t[i].hashCode();
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
	 * which they are stored in the array, and this is unspecified.
	 * <p>The iterator is fail-fast, which means that if a structural
	 * modification is made to the set after this method call, the iterator
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
	public boolean remove(final Object element) {
		final Object[] t = table; //Local cache for speed without JIT.

		//Find the element, and then place the last element at its position if it's in the set.
		if(element == null) { //Can't use equals() if this is null.
			for(int i = size - 1;i >= 0;i--) { //So search for null then.
				if(t[i] == null) {
					t[i] = t[--size]; //Overwrite this with the last element of the array, and remove the last element.
					modCount++;
					return true;
				}
			}
			return false; //Not found.
		}
		for(int i = size - 1;i >= 0;i--) { //Search for the element then.
			if(element.equals(t[i])) {
				t[i] = t[--size]; //Overwrite this with the last element of the array, and remove the last element.
				modCount++;
				return true;
			}
		}
		return false; //Not found.
	}

	/**
	 * Removes from this set all of its elements that are contained in the
	 * specified collection. If the specified collection is also a set, this
	 * operation effectively modifies this set so that its value is the
	 * asymmetric set difference of the two sets.
	 * <p>This implementation iterates over either the specified collection or
	 * over the set, based on which is smaller: the collection or the set. When
	 * iterating over the collection, each element of the collection is removed
	 * from the set. When iterating over the set, each element of the set is
	 * removed from the set if it is contained over the collection.</p>
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

		//Pick whichever method is fastest:
		if(Math.sqrt(table.length) > c.size() || (c instanceof List && table.length > c.size())) { //Iterate over c, removing all elements from this set. Lists have linear contains() methods like me, so they get special treatment.
			boolean changed = false;
			for(Object element : c) { //Iterate over the collection.
				changed |= remove(element); //Remove every element of the collection from this set.
			}
			return changed;
		}
		//Otherwise, iterate over the set, removing all elements that are in c.
		final Object[] t = table; //Local cache for faster access without JIT compiler.
		boolean changed = false;
		for(int i = size - 1;i >= 0;i--) {
			if(c.contains(t[i])) { //If it's in the collection, remove it.
				t[i] = t[--size]; //Overwrite this with the last element of the array, and remove the last element.
				modCount++;
				changed = true;
			}
		}
		return changed;
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
		final Object[] t = table; //Local cache for faster access without JIT compiler.

		boolean changed = false;
		for(int i = size - 1;i >= 0;i--) {
			if(!c.contains(t[i])) { //This element must be removed.
				t[i] = t[--size]; //Overwrite this with the last element of the array, and remove the last element.
				modCount++;
				changed = true;
			}
		}
		return changed;
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
	 * <p>This implementation copies the active portion of the internal array to
	 * the resulting array.
	 * @return An array containing all of the elements in this set.
	 */
	@Override
	public Object[] toArray() {
		final Object[] result = new Object[size];
		System.arraycopy(table,0,result,0,size); //Copy the active portion (0 to size) to the result.
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
	@SuppressWarnings("unchecked") //The reflection array creation.
	public <E> E[] toArray(E[] a) {
		final E[] result;
		if(a.length >= size) { //Array suffices.
			result = a;
		} else { //Too many elements for the array.
			result = (E[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(),size); //Allocate a new array.
		}
		System.arraycopy(table,0,result,0,size); //Copy the active portion (0 to size) to the result.
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

		final StringBuilder result = new StringBuilder(2 + 5 * size); //Reserve 2 chars for brackets, 4 for each element and 1 for the commas after each element.
		result.append('{');
		for(int i = size - 1;i >= 0;i--) {
			final Object elem = t[i];
			if(elem == this) { //Don't go into infinite recursion if it contains itself!
				result.append("(this Set)");
			} else if(elem == null) { //This element has no toString() method.
				result.append("null");
			} else {
				result.append(elem); //Calls the toString() method.
			}
			if(i > 0) { //That was not the last element. Add a comma.
				result.append(',');
			}
		}
		result.append('}');
		return result.toString(); //Finalise and return.
	}

	/**
	 * Expands the capacity of the internal array for this set. This allocates a
	 * new array and copies all elements to it.
	 * @param newCapacity The capacity of the new array.
	 */
	protected void resize(final int newCapacity) {
		final Object[] newTable = new Object[newCapacity];
		System.arraycopy(table,0,newTable,0,size);
		modCount++; //This invalidates all lookups to the previous table, so it's a structural modification.
		table = newTable;
	}

	/**
	 * Reconstitute the {@code ArraySet} instance from a stream (that is,
	 * deserialise it).
	 * @param s The stream to read the state of an {@code ArraySet} from.
	 * @throws ClassNotFoundException The SerialVerionUID of the class does not
	 * match any known version of {@code ArraySet}.
	 * @throws IOException Something went wrong reading from the specified
	 * stream.
	 */
	private void readObject(final ObjectInputStream s) throws IOException,ClassNotFoundException {
		s.defaultReadObject(); //Read the default serialisation magic.

		size = s.readInt(); //Read size.
		final Object[] t = new Object[size]; //Create an array just big enough for everything.
		for(int i = size - 1;i >= 0;i--) { //Read each element.
			t[i] = s.readObject();
		}
		table = t; //Store the array in the object.
	}

	/**
	 * Save the state of this {@code ArraySet} instance to a stream (that is,
	 * serialise it).
	 * @serialData The number of elements in the set ({@code int}) is emitted,
	 * followed by all elements ({@code Object}s) in no particular order.
	 * @param s The stream to write the state of this {@code ArraySet} to.
	 * @throws IOException Something went wrong writing to the specified stream.
	 */
	private void writeObject(final ObjectOutputStream s) throws IOException {
		s.defaultWriteObject(); //Write the default serialisation magic.

		s.writeInt(size); //Write size.
		Object[] t = table; //Cache a local copy for faster access without JIT compiler.
		for(int i = size - 1;i >= 0;i--) {
			s.writeObject(t[i]); //Write out each element.
		}
	}

	/**
	 * This is a custom implementation of {@link Iterator} that iterates over
	 * the elements of an array set. The iterator gives the elements of the set
	 * in no particular order. The actual order will be the reverse order of the
	 * elements in the set as they are ordered in the internal array of the set.
	 * <p>The iterator is fail-fast, which means that if a structural
	 * modification is made to the array set after this method call, the
	 * iterator will throw a {@link ConcurrentModificationException} when
	 * {@link #next()} or {@link #remove()} is called. This behaviour prevents
	 * nondeterministic or unexpected behaviour caused by the concurrent
	 * modification of the set while it is being iterated over. Rather than
	 * giving inconsequent results, it always fails to give a result.</p>
	 */
	protected class Itr implements Iterator<E> {
		/**
		 * The expected modifiction count of the accompanying {@code ArraySet}.
		 * If this is different from the actual modification count of the set, a
		 * concurrent modification on the set has occurred and every method
		 * should throw a {@link ConcurrentModificationException}.
		 */
		protected int expectedModCount;

		/**
		 * The current position in the iteration. If this index is negative, the
		 * iteration is complete.
		 */
		protected int index;

		/**
		 * Indicates whether the {@link #remove()} method would be successful in
		 * removing an element. This is set to true after a successful call to
		 * {@link #next()}, and to false after a successful call to
		 * {@link #remove()}.
		 * <p>This will initially be {@code false}.</p>
		 */
		protected boolean canRemove;

		/**
		 * Creates a new iterator over the {@code ArraySet}. The iterator will
		 * start at the last active element in the array, iterating towards
		 * index {@code 0}.
		 */
		protected Itr() {
			expectedModCount = modCount;
			index = size - 1;
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
		@SuppressWarnings("unchecked") //Caused by the casting of an array element to the returned E.
		public E next() {
			if(modCount != expectedModCount) { //Some concurrent modification has been made on the set!
				throw new ConcurrentModificationException();
			}
			if(index < 0) { //We're through the array.
				canRemove = false;
				throw new NoSuchElementException();
			}
			canRemove = true;
			return (E)table[index--];
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
			if(!canRemove) { //Nothing to remove.
				throw new IllegalStateException();
			}
			if(modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			//Delete the element at the previous index.
			table[index + 1] = table[--size]; //By overwriting it with the last element, and removing the last element.
			modCount++;
			expectedModCount = modCount; //This modification is expected since the iterator made it.
			canRemove = false; //Can't remove another one until next() is called.
		}
	}
}
