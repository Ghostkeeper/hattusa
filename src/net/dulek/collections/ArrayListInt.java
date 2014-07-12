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
 * A version of {@link ArrayList} that stores raw integers instead of some
 * object version. The {@code ArrayListInt} should be faster and use less
 * memory. Note that this implementation does not extend from
 * {@link AbstractList} or {@link AbstractCollection}. It does implement
 * {@link List} and {@link Collection}. This is so that it can be used in
 * conjunction with other {@code List}s and {@code Collection}s. Using the
 * methods inherited from those interfaces however requires the use of the
 * reference-type {@link Integer}, which defeats the purpose of using
 * {@code ArrayListInt} over just {@link ArrayList}. Those methods provide no
 * speed advantage and may even be slightly slower.
 * <p>A benchmark was performed on an Intel i7-2600K CPU of 3.4GHz for various
 * common usages of the {@code ArrayListInt}, using Java 7.0 v13 as virtual
 * machine. The following percentile improvements over
 * {@code ArrayList&lt;Integer&gt;} were found. A percentile improvement of 100%
 * means that the {@code ArrayListInt} performed the task in half the time of
 * {@code ArrayList&lt;Integer&gt;}'s equivalent task. A percentile improvement
 * of -100% means that the {@code ArrayList&lt;Integer&gt;} performed the task
 * in half the time of {@code ArrayListInt}'s equivalent task.
 * <ul><li>{@link add(int)}: 73.1%</li>
 * <li>{@link add(Integer)}: 25.7%</li>
 * <li>{@link add(int,int)}: 5.7%</li>
 * <li>{@link add(int,Integer)}: 5.5%</li>
 * <li>{@link addAll(Collection)}: 40.8%</li>
 * <li>{@link addAll(int,Collection)}: 40.4%</li>
 * <li>{@link clear()}: -3.0%</li>
 * <li>{@link clone()}: 0.4%</li>
 * <li>{@link contains(int)}: 182.6%</li>
 * <li>{@link contains(Integer)}: 176.4%</li>
 * <li>{@link containsAll(Collection)}: 219.7%</li>
 * <li>{@link equals(Object)}: -91.6%</li>
 * <li>{@link getInt(int)}: -0.3%</li>
 * <li>{@link get(int)}: -99900.0%</li>
 * <li>{@link hashCode()}: 2709.6%</li>
 * <li>{@link indexOf(int)}: 173.5%</li>
 * <li>{@link indexOf(Integer)}: 172.6%</li>
 * <li>{@link isEmpty()}: 0.0%</li>
 * <li>{@link iterator()}, iterating normally: -6150.0%</li>
 * <li>{@link iterator()}, removing during iterating: 7.6%</li>
 * <li>{@link lastIndexOf(int)}: 175.9%</li>
 * <li>{@link lastIndexOf(Integer)}: 173.8%</li>
 * <li>{@link listIterator()}, iterating normally: -4445.5%</li>
 * <li>{@link listIterator()}, removing during iterating: 3.1%</li>
 * <li>{@link removeInt(int)}: 214.8%</li>
 * <li>{@link remove(Object)}: 227.3%</li>
 * <li>{@link removeIndex(int)}: 2.1%</li>
 * <li>{@link remove(int)}: 5.2%</li>
 * <li>{@link removeAll(Collection)}: 16.1%</li>
 * <li>{@link retainAll(Collection)}: 18.9%</li>
 * <li>{@link set(int,int)}: 2209.1%</li>
 * <li>{@link set(int,Integer)}: 13.3%</li>
 * <li>{@link size()}: 1500.0%</li>
 * <li>{@link subList()}: 6.6%</li>
 * <li>{@link toArray()}: -348.4%</li>
 * <li>{@link toArray(int[])}: 313.9%</li>
 * <li>{@link toArray(Integer[])}: -135.8%</li>
 * <li>{@link toString()}: 202.8%</li></ul>
 * Note that the iterator is notoriously slower in normal usage. This makes
 * {@code ArrayListInt} slower when it is used as a collection rather than a
 * list.</p>
 * <p>Resizable-array implementation of the {@link List} interface. Implements
 * all optional list operations, and permits all integers. In addition to
 * implementing the {@link List} interface, this class provides methods to
 * manipulate the size of the array that is used to internally store the list.
 * (This class is roughly equivalent to {@link Vector}, except that it is
 * unsynchronized.)</p>
 * <p>The {@link size()}, {@link isEmpty()}, {@link get(int)},
 * {@link set(int,int)}, {@link iterator()} and {@link listIterator()}
 * operations run in constant time. The {@code add(int)} operation runs in
 * <i>amortized constant time</i>, that is, adding {@code n} elements requires
 * {@code O(n)} time. All of the other operations run in linear time (roughly
 * speaking). The constant factor is low compared to that for the
 * {@link LinkedList} implementation.</p>
 * <p>Each {@code ArrayListInt} instance has a <i>capacity</i>. The capacity is
 * the size of the array used to store the elements in the list. It is always at
 * least as large as the list size. As elements are added to an
 * {@code ArrayListInt}, its capacity grows automatically. The details of the
 * growth policy are not specified beyond the fact that adding an element has
 * constant amortized time cost.</p>
 * <p>An application can increase the capacity of an {@code ArrayListInt}
 * instance before adding a large number of elements using the
 * {@code ensureCapacity()} operation. This may reduce the amount of incremental
 * reallocation.</p>
 * <p><strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access an {@code ArrayListInt} instance concurrently, and at
 * least one of the threads modifies the list structurally, it <i>must</i> be
 * synchronized externally. (A structural modification is any operation that
 * adds or deletes one or more elements, or explicitly resizes the backing
 * array; merely setting the value of an element is not a structural
 * modification.) This is typically accomplished by synchronizing on some object
 * that naturally encapsulates the list. If no such object exists, the list
 * should be "wrapped" using the {@link Collections.synchronizedList(List)}
 * method. This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:</p>
 * <p>{@code     List list = Collections.synchronizedList(new ArrayList(...));}
 * </p>
 * <p>The iterators returned by this class's {@link iterator} and
 * {@link listIterator} methods are fail-fast: if the list is structurally
 * modified at any time after the iterator is created, in any way except through
 * the iterator's own {@link Iterator.remove()} or {@link ListIterator.add(E)}
 * methods, the iterator will throw a {@link ConcurrentModificationException}.
 * Thus, in the face of concurrent modification, the iterator fails quickly and
 * cleanly, rather than risking arbitrary, non-deterministic behaviour at an
 * undetermined time in the future.</p>
 * <p>Note that the fail-fast behaviour of an iterator cannot be guaranteed as
 * it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification. Fail-fast iterators throw
 * {@link ConcurrentModificationException} on a best-effort basis. Therefore, it
 * would be wrong to write a program that depended on this exception for its
 * correctness: <i>the fail-fast behaviour of iterators should be used only to
 * detect bugs.</i></p>
 * @author Ruben Dulek
 * @version 1.0
 * @see java.util.ArrayList
 * @see java.util.Collection
 * @see java.util.List
 * @see java.util.LinkedList
 * @see java.util.Vector
 */
public class ArrayListInt extends AbstractList<Integer> implements Serializable,Cloneable,Iterable<Integer>,Collection<Integer>,List<Integer>,RandomAccess {
	/**
	 * The serial version of this {@code ArrayListInt} implementation. This
	 * serial version really is only the number of times the serialisation was
	 * changed.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of times this list has been <i>structurally modified</i>.
	 * Structural modifications are those that change the size of the list, or
	 * otherwise perturb it in such a fashion that iterations in progress may
	 * yield incorrect results.
	 * <p>This field is used by the iterator and list iterator implementation
	 * returned by the {@link iterator()} and {@link listIterator()} methods. If
	 * the value of this field changes unexpectedly, the iterator (or list
	 * iterator) will throw a {@link ConcurrentModificationException} in
	 * response to the {@link Iterator.next()}, {@link Iterator.remove()},
	 * {@link ListIterator.previous()}, {@link ListIterator.set(int)} or
	 * {@link ListIterator.add(int)} operations. This provides <i>fail-fast</i>
	 * behaviour, rather than non-deterministic behaviour in the face of
	 * concurrent modification during iteration.</p>
	 */
	protected transient int modCount;

	/**
	 * The array buffer into which the elements of the {@code ArrayListInt} are
	 * stored. The capacity of the {@code ArrayListInt} is the length of this
	 * array buffer.
	 */
	private transient int[] elementData;

	/**
	 * The size of the {@code ArrayListInt} (the number of elements it
	 * contains).
	 * @serial The number of elements in the list is emitted ({@code int}).
	 */
	private int size;

	/**
	 * The maximum size of array to allocate. Some VMs reserve some header words
	 * in an array, hence the {@code - 8}. Attempts to allocate larger arrays
	 * may result in {@code OutOfMemoryError: Requested array size exceeds VM
	 * limit}.
	 */
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 * <p>Note that it is always good practice to provide an initial capacity
	 * for {@code ArrayListInt}. This causes it to perform better since it will
	 * require less reallocations to a bigger internal array.</p>
	 */
	public ArrayListInt() {
		elementData = new int[10]; //Construct the list with a default initial capacity of 10.
	}

	/**
	 * Constructs an empty list containing the elements of the specified
	 * collection, in the order they are returned by the collection's iterator.
	 * @param c The collection whose elements are to be placed into this list.
	 * @throws NullPointerException The specified collection is {@code null} or
	 * contains elements which are {@code null}.
	 */
	public ArrayListInt(final Collection<? extends Integer> c) {
		elementData = new int[c.size()];
		for(Integer e : c) { //Have to cast and copy one by one. Implicit cast from ? to Integer.
			elementData[size++] = e; //Implicit unboxing to int. Throws NullPointerException if an element is null.
		}
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 * @param initialCapacity The initial capacity of the list.
	 * @throws IllegalArgumentException The specified initial capacity is
	 * negative.
	 */
	public ArrayListInt(final int initialCapacity) {
		if(initialCapacity < 0) {
			throw new IllegalArgumentException("The specified initial capacity was negative: " + initialCapacity);
		}
		elementData = new int[initialCapacity]; //Allocate the required amount of memory for the initial capacity.
	}

	/**
	 * Appends the specified element to the end of this list.
	 * @param e The element to be appended to this list.
	 * @return {@code true} (as specified by {@link Collection.add(E)}).
	 */
	public boolean add(final int e) {
		ensureCapacityInternal(size + 1); //Make sure we can store the element.
		elementData[size++] = e; //Store it and increment the size.
		return true;
	}

	/**
	 * Inserts the specified element at the specified position in this list.
	 * Shifts the element currently at that position (if any) and any subsequent
	 * elements to the right (adds one to their indices).
	 * @param index The index at which the specified element is to be inserted.
	 * @param element The element to be inserted.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt; size()}).
	 */
	public void add(final int index,final int element) {
		if(index > size || index < 0) {
			throw new IndexOutOfBoundsException("Tried to add an integer at index " + index + " (current size is " + size + ").");
		}
		ensureCapacityInternal(size + 1); //Make sure we can store the element.
		System.arraycopy(elementData,index,elementData,index + 1,size++ - index); //Shift all elements one further to make room.
		elementData[index] = element; //Store it.
	}

	/**
	 * Appends the specified element to the end of this list. The {@code null}
	 * value is not allowed.
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link add(int)} method. Please
	 * use the {@link add(int)} method when possible.</p>
	 * @param e The element to be appended to this list.
	 * @return {@code true} (as specified by {@link Collection.add(E)}).
	 * @throws NullPointerException The specified element to add was
	 * {@code null}.
	 */
	@Override
	public boolean add(final Integer e) {
		ensureCapacityInternal(size + 1); //Make sure we can store the element.
		elementData[size++] = e; //Implicit cast. Store it and increment the size.
		return true;
	}

	/**
	 * Inserts the specified element at the specified position in this list.
	 * Shifts the element currently at that position (if any) and any subsequent
	 * elements to the right (adds one to their indices).
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link add(int,int)} method.
	 * Please use the {@link add(int,int)} method when possible.</p>
	 * @param index The index at which the specified element is to be inserted.
	 * @param element The element to be inserted.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt; size()}).
	 * @throws NullPointerException The specified element to add was
	 * {@code null}.
	 */
	@Override
	public void add(final int index,final Integer element) {
		if(index > size || index < 0) {
			throw new IndexOutOfBoundsException("Tried to add an integer at index " + index + " (current size is " + size + ").");
		}
		ensureCapacityInternal(size + 1); //Make sure we can store the element.
		System.arraycopy(elementData,index,elementData,index + 1,size++ - index); //Shift all elements one further to make room.
		elementData[index] = element; //Implicit cast.
	}

	/**
	 * Appends all of the elements in the specified collection to the end of
	 * this list, in the order that they are returned by the specified
	 * collection's {@code Iterator}. The behaviour of this operation is
	 * undefined if the specified collection is modified while the operation is
	 * in progress. (This implies that the behaviour of this call is undefined
	 * if the specified collection is this list, and this list is nonempty.)
	 * @param c The collection containing elements to be added to this list.
	 * @return {@code true} if this list changed as a result of the call.
	 * @throws NullPointerException The specified collection is {@code null} or
	 * contains elements which are {@code null}.
	 */
	@Override
	public boolean addAll(final Collection<? extends Integer> c) {
		final int numNew = c.size(); //The amount of new elements.
		ensureCapacityInternal(size + numNew); //Make sure we can store all the new data.
		for(Integer e : c) { //Have to cast and copy one by one. Implicit cast from ? to Integer.
			elementData[size++] = e; //Implicit unboxing to int.
		}
		return numNew != 0;
	}

	/**
	 * Inserts all of the elements in the specified collection into this list,
	 * starting at the specified position. Shifts the element currently at that
	 * position (if any) and any subsequent elements to the right (increases
	 * their indices). The new elements will appear in the list in the order
	 * that they are returned by the specified collection's {@code Iterator}.
	 * @param index The index at which to insert the first element from the
	 * specified collection.
	 * @param c The collection containing elements to be added to this list.
	 * @return {@code true} if this list changed as a result of the call.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt; size()}).
	 * @throws NullPointerException The specified collection is {@code null} or
	 * contains elements which are {@code null}.
	 */
	@Override
	public boolean addAll(int index,final Collection<? extends Integer> c) {
		if(index > size || index < 0) {
			throw new IndexOutOfBoundsException("Tried to add a collection of integers at index " + index + " (current size is " + size + ").");
		}
		final int numNew = c.size(); //The amount of new elements.
		ensureCapacityInternal(size + numNew); //Make sure we can store all the new data.
		final int numToMove = size - index; //The amount of elements from the old array that have to be moved.
		if(numToMove > 0) {
			System.arraycopy(elementData,index,elementData,index + numNew,numToMove);
		}
		for(Integer e : c) { //Have to cast and copy one by one. Implicit cast from ? to Integer.
			elementData[index++] = e; //Implicit unboxing to int.
		}
		size += numNew;
		return numNew != 0;
	}

	/**
	 * Removes all of the elements from this list. The list will be empty after
	 * this call returns.
	 * <p>The memory used by the {@code ArrayListInt} is not freed. In order to
	 * do that, use the {@link trimToSize()} method.</p>
	 */
	@Override
	public void clear() {
		modCount++;
		size = 0; //Simply setting the size to 0 lets the range checks to the job. Memory is not released.
	}

	/**
	 * Returns a copy of this {@code ArrayListInt} instance. Since the elements
	 * are raw types, they are also copied.
	 * @return A clone of this {@code ArrayListInt} instance.
	 * @see Cloneable
	 */
	@Override
	public ArrayListInt clone() {
		final ArrayListInt result = new ArrayListInt(size); //A new ArrayListInt to return.
		System.arraycopy(elementData,0,result.elementData,0,size); //Copy our values to it.
		result.size = size; //Including our size. But not the modCount.
		return result;
	}

	/**
	 * Returns {@code true} if this list contains the specified element. More
	 * formally, returns {@code true} if and only if this list contains at least
	 * one element {@code e} such that {@code o == e}.
	 * @param o The element whose presence in this list is to be tested.
	 * @return {@code true} if this list contains the specified element, or
	 * {@code false} otherwise.
	 */
	public boolean contains(final int o) {
		for(int i = size - 1;i >= 0;i--) { //Loop backwards for speed. Assume no bias in where the element may be.
			if(elementData[i] == o) { //We have a match.
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns {@code true} if this list contains the specified element. More
	 * formally, returns {@code true} if and only if this list contains at least
	 * one element {@code e} such that {@code o == e}. If the specified element
	 * is {@code null}, {@code false} is returned, since this list cannot
	 * contain {@code null} elements.
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link contains(int)} method.
	 * Please use the {@link contains(int)} method when possible.</p>
	 * @param o The element whose presence in this list is to be tested.
	 * @return {@code true} if this list contains the specified element, or
	 * {@code false} otherwise.
	 */
	@Override
	public boolean contains(final Object o) {
		if(o == null) { //We don't store null elements here.
			return false;
		}
		if(!(o instanceof Integer)) { //We don't store anything else than Integers.
			return false;
		}
		final int value = ((Integer)o).intValue(); //Then we know we can cast safely.
		for(int i = size - 1;i >= 0;i--) { //Loop backwards for speed. Assume no bias in where the element may be.
			if(elementData[i] == value) { //We have a match.
				return true;
			}
		}
		return false; //No match.
	}

	/**
	 * Returns {@code true} if this list contains all of the elements of the
	 * specified collection.
	 * @param c The collection to be checked for containment in this list.
	 * @return {@code true} if this list contains all of the elements of the
	 * specified collection, or {@code false} otherwise.
	 * @throws ClassCastException The types of one or more elements in the
	 * specified collection are incompatible with this list.
	 * @throws NullPointerException The specified collection contains one or
	 * more {@code null} elements, or the specified collection is {@code null}.
	 * @see contains(int)
	 * @see contains(Object)
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
		for(Object e : c) {
			if(e == null) { //We don't store null elements here.
				return false;
			}
			if(!(e instanceof Integer)) { //We don't store anything else than Integers.
				return false;
			}
			final int value = ((Integer)e).intValue(); //Then we know we can cast safely.
			if(!contains(value)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Increases the capacity of this {@code ArrayListInt} instance, if
	 * necessary, to ensure that it can hold at least the number of elements
	 * specified by the minimum capacity argument.
	 * @param minCapacity The desired minimum capacity.
	 */
	public void ensureCapacity(final int minCapacity) {
		if(minCapacity > 0) { //Negative capacity doesn't make sense. Zero can be skipped too, since the array is always at least zero length.
			ensureCapacityInternal(minCapacity);
		}
	}

	/**
	 * Increases the capacity of this {@code ArrayListInt} instance, if
	 * necessary, to ensure that it can hold at least the number of elements
	 * specified by the minimum capacity argument. This internal version doesn't
	 * check for negative or zero capacity.
	 * @param minCapacity The desired minimum capacity.
	 */
	private void ensureCapacityInternal(final int minCapacity) {
		modCount++; //This is a structural modification.
		if(minCapacity - elementData.length > 0) { //minCapacity > elementData.length (overflow-conscious)
			final int oldCapacity = elementData.length;
			int newCapacity = oldCapacity + (oldCapacity >> 1); //Thrice the oldCapacity.
			if(newCapacity - minCapacity < 0) { //Still too small.
				newCapacity = minCapacity;
			}
			if(newCapacity - MAX_ARRAY_SIZE > 0) { //newCapacity > MAX_ARRAY_SIZE
				if(minCapacity < 0) { //Overflow.
					throw new OutOfMemoryError("The ArrayListInt tried to grow to a size larger than the maximum array size.");
				}
				newCapacity = MAX_ARRAY_SIZE;
			}
			elementData = Arrays.copyOf(elementData,newCapacity);
		}
	}

	/**
	 * Compares the specified object with this list for equality. Returns
	 * {@code true} if and only if the specified object is also a list, both
	 * lists have the same size, and all corresponding pairs of elements in the
	 * two lists are <i>equal</i>. (Two elements {@code e1} and {@code e2} are
	 * <i>equal</i> if {@code e1.equals(e2)}.) In other words, two lists are
	 * defined to be equal if they contain the same elements in the same order.
	 * This definition ensures that the {@code equals()} method works properly
	 * across different implementations of the {@link List} interface.
	 * @param o The object to be compared for equality with this list.
	 * @return {@code true} if the specified object is equal to this list.
	 * @see Object.hashCode()
	 * @see HashMap
	 */
	@Override
	public boolean equals(final Object o) {
		if(o == this) { //Hey, that's me!
			return true;
		}
		if(!(o instanceof List) || ((List)o).size() != size) { //Not a list at all or not the same size.
			return false;
		}
		final ListIterator iterator = ((List)o).listIterator();
		for(int i = 0;i < size;i++) { //Check every element.
			final Object other = iterator.next();
			if(other == null || !other.equals(elementData[i])) { //Null or not the same.
				return false;
			}
		}
		return true; //Same type. Same size. Same elements.
	}

	/**
	 * Returns the element at the specified position in this list as a reference
	 * type {@link Integer}. This version is compatible with the {@link List}
	 * and {@link Collection} interfaces.
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link getInt(int)} method.
	 * Please use the {@link getInt(int)} method when possible.</p>
	 * @param index The index of the element to return.
	 * @return The element at the specified position in this list, as
	 * {@link Integer}.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt;= size()}).
	 */
	@Override
	public Integer get(final int index) {
		if(index >= size) {
			throw new IndexOutOfBoundsException("Tried to get the value at index " + index + " (current size is " + size + ").");
		}
		return Integer.valueOf(elementData[index]);
	}

	/**
	 * Returns the element at the specified position in this list.
	 * @param index The index of the element to return.
	 * @return The element at the specified position in this list.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt;= size()}).
	 */
	public int getInt(final int index) {
		if(index >= size) {
			throw new IndexOutOfBoundsException("Tried to get the value at index " + index + " (current size is " + size + ").");
		}
		return elementData[index];
	}

	/**
	 * Returns the hash code value for this list. The hash code of a list is
	 * defined to be the result of the following calculation:
	 * <p><code>int hashCode = 1;
	 * for(int e : list) {
	 *     hashCode = 31 * hashCode + e;
	 * }</code></p>
	 * <p>This ensures that {@code list1.equals(list2)} implies that
	 * {@code list1.hashCode() == list2.hashCode()} for any two lists
	 * {@code list1} and {@code list2}, as required by the general contract of
	 * {@link Object.hashCode()}.</p>
	 * @return The hash code value for this list.
	 * @see Object.equals(Object)
	 * @see equals(Object)
	 */
	@Override
	public int hashCode() {
		int hashCode = 1; //This algorithm is literally in the Javadoc!
		for(int i = 0;i < size;i++) { //Instead of using the iterator, we'll just iterate ourselves though.
			hashCode = 31 * hashCode + elementData[i]; //The 31 is to reduce collisions. Any prime will work.
		}
		return hashCode;
	}

	/**
	 * Returns the index of the first occurrence of the specified element in
	 * this list, or {@code -1} if this list does not contain the element. More
	 * formally, returns the lowest index {@code i} such that
	 * {@code get(i) == o}, or {@code -1} if there is no such index.
	 * @param o The element to search for.
	 * @return The index of the first occurrence of the specified element in
	 * this list, or {@code -1} if this list does not contain the element.
	 */
	public int indexOf(final int o) {
		for(int i = 0;i < size;i++) { //Forward loop required, since we need the first index.
			if(o == elementData[i]) { //We have a match!
				return i;
			}
		}
		return -1; //No match in the entire list.
	}

	/**
	 * Returns the index of the first occurrence of the specified element in
	 * this list, or {@code -1} if this list does not contain the element. More
	 * formally, returns the lowest index {@code i} such that
	 * {@code get(i) == o}, or {@code -1} if there is no such index. Searching
	 * for the {@code null} element will always return {@code -1}, since this
	 * list cannot contain {@code null} elements.
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link indexOf(int)} method.
	 * Please use the {@link indexOf(int)} method when possible.</p>
	 * @param o The element to search for.
	 * @return The index of the first occurrence of the specified element in
	 * this list, or {@code -1} if this list does not contain the element.
	 */
	@Override
	public int indexOf(final Object o) {
		if(o == null) { //This list contains no null elements.
			return -1;
		}
		if(!(o instanceof Integer)) { //We store only integers.
			return -1;
		}
		final int value = (int)o; //Cast this once.
		for(int i = 0;i < size;i++) { //Forward loop required, since we need the first index.
			if(value == elementData[i]) { //We have a match!
				return i;
			}
		}
		return -1; //No match in the entire list.
	}

	/**
	 * Returns {@code true} if this list contains no elements.
	 * @return {@code true} if this list contains no elements, or {@code false}
	 * otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Returns an iterator over the elements in this list in proper sequence.
	 * <p>The returned iterator is <i>fail-fast</i>.</p>
	 * @return An iterator over the elements in this list in proper sequence.
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new Itr();
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this
	 * list, or {@code -1} if this list does not contain the element. More
	 * formally, returns the highest index {@code i} such that
	 * {@code get(i) == o}, or {@code -1} if there is no such index.
	 * @param o The element to search for.
	 * @return The index of the last occurrence of the specified element in this
	 * list, or {@code -1} if this list does not contain the element.
	 */
	public int lastIndexOf(final int o) {
		for(int i = size - 1;i >= 0;i--) { //Walk backwards through the list.
			if(o == elementData[i]) { //We have a match!
				return i;
			}
		}
		return -1; //No match in the entire list.
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this
	 * list, or {@code -1} if this list does not contain the element. More
	 * formally, returns the highest index {@code i} such that
	 * {@code get(i) == o}, or {@code -1} if there is no such index. Searching
	 * for the {@code null} element will always return {@code -1}, since this
	 * list cannot contain {@code null} elements.
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link lastIndexOf(int)} method.
	 * Please use the {@link lastIndexOf(int)} method when possible.</p>
	 * @param o The element to search for.
	 * @return The index of the last occurrence of the specified element in this
	 * list, or {@code -1} if this list does not contain the element.
	 */
	@Override
	public int lastIndexOf(final Object o) {
		if(o == null) { //This list contains no null elements.
			return -1;
		}
		if(!(o instanceof Integer)) { //We store only integers.
			return -1;
		}
		final int value = (int)o; //Cast this once.
		for(int i = size - 1;i >= 0;i--) { //Walk backwards through the list.
			if(value == elementData[i]) {
				return i;
			}
		}
		return -1; //No match in the entire list.
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence).
	 * <p>The returned iterator is <i>fail-fast</i>.</p>
	 * @return A list iterator over the elements in this list (in proper
	 * sequence).
	 * @see listIterator(int)
	 */
	@Override
	public ListIterator<Integer> listIterator() {
		return new ListItr();
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence), starting at the specified position in the list. The specified
	 * index indicates the first element that would be returned by an initial
	 * call to {@link ListIterator.next()}. An initial call to
	 * {@link ListIterator.previous()} would return the element with the
	 * specified index minus one.
	 * @param index The index of the first element to be returned from the list
	 * iterator (by a call to {@link ListIterator.next()}).
	 * @return A list iterator over the elements in this list (in proper
	 * sequence), starting at the specified position in the list.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt; size()}).
	 */
	@Override
	public ListIterator<Integer> listIterator(final int index) {
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException("Tried to create a list iterator that starts at index " + index + " (current size is " + size + ").");
		}
		return new ListItr(index);
	}

	/**
	 * Reconstitute the {@code ArrayListInt} instance from a stream (that is,
	 * deserialise it).
	 * @param s The stream to read the {@code ArrayListInt} from.
	 * @throws IOException Failed to read from the stream.
	 * @throws ClassNotFoundException The class of the serialised object could
	 * not be found.
	 */
	private void readObject(final ObjectInputStream s) throws IOException,ClassNotFoundException {
		s.defaultReadObject(); //Read the size and any object global stuff.
		final int arrayLength = s.readInt(); //Read the array length.
		final int[] array = new int[arrayLength]; //Allocate the required array.
		for(int i = 0;i < arrayLength;i++) { //Read all elements in the proper order.
			array[i] = s.readInt();
		}
		elementData = array;
	}

	/**
	 * Removes the element at the specified position in this list. Shifts any
	 * subsequent elements to the left (subtracts one from their indices).
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link removeIndex(int)} method.
	 * Please use the {@link removeIndex(int)} method when possible.</p>
	 * @param index The index of the element to be removed.
	 * @return The element that was removed from the list.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt;= size()}).
	 */
	@Override
	public Integer remove(final int index) {
		if(index >= size) {
			throw new IndexOutOfBoundsException("Tried to remove the value at index " + index + " (current size is " + size + ").");
		}
		modCount++;
		final int oldValue = elementData[index]; //The old value is about to be removed, but we have to return it.
		final int numMoved = --size - index; //The number of items that have to be shifted back a position.
		if(numMoved > 0) {
			System.arraycopy(elementData,index + 1,elementData,index,numMoved);
		}
		return Integer.valueOf(oldValue); //Implicit cast.
	}

	/**
	 * Removes the first occurrence of the specified element from this list, if
	 * it is present. If the list does not contain the element, it is unchanged.
	 * More formally, removes the element with the lowest index {@code i} such
	 * that {@code o.equals(get(i))} (if such an element exists). Returns
	 * {@code true} if this list contained the specified element (or
	 * equivalently, if this list changed as a result of the call).
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link removeInt(int)} method.
	 * Please use the {@link removeInt(int)} method when possible.</p>
	 * @param o The element to be removed from this list, if present.
	 * @return {@code true} if this list contained the specified element, or
	 * {@code false} otherwise.
	 */
	@Override
	public boolean remove(final Object o) {
		if(o == null) { //This list contains no null elements.
			return false;
		}
		if(!(o instanceof Integer)) { //We store only integers.
			return false;
		}
		final int value = (int)o; //Cast this once.
		for(int i = 0;i < size;i++) { //Do a forward loop, since we have to remove the first occurrence.
			if(value == elementData[i]) { //We have a match! Remove the item.
				modCount++;
				final int numMoved = --size - i; //The number of items that have to be shifted back a position.
				if(numMoved > 0) {
					System.arraycopy(elementData,i + 1,elementData,i,numMoved);
				}
				return true;
			}
		}
		return false; //Not found in the list.
	}

	/**
	 * Removes from this list all of its elements that are contained in the
	 * specified collection.
	 * @param c The collection containing elements to be removed from this list.
	 * @return {@code true} if this list changed as a result of the call, or
	 * {@code false} otherwise.
	 * @throws ClassCastException The class of an element of this list is
	 * incompatible with the specified collection.
	 * @throws NullPointerException The specified collection contains a
	 * {@code null} element, or the specified collection is {@code null}.
	 * @see Collection.contains(Object)
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		final int[] elementData = this.elementData; //Make a local copy of this pointer for faster access.
		int pos = 0; //Current size of our result.
		for(int i = 0;i < size;i++) { //Forward loop, since we want to retain our present order.
			if(!c.contains(Integer.valueOf(i))) { //We should retain this item.
				elementData[pos++] = elementData[i];
			}
		}
		if(pos != size) { //Not every item was retained.
			modCount += size - pos;
			size = pos;
			return true;
		}
		return false;
	}

	/**
	 * Removes the element at the specified position in this list. Shifts any
	 * subsequent elements to the left (subtracts one from their indices).
	 * @param index The index of the element to be removed.
	 * @return The element that was removed from the list.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt;= size()}).
	 */
	public int removeIndex(final int index) {
		if(index >= size) {
			throw new IndexOutOfBoundsException("Tried to remove the value at index " + index + " (current size is " + size + ").");
		}
		modCount++;
		int oldValue = elementData[index]; //The old value is about to be removed, but we have to return it.
		int numMoved = --size - index; //The number of items that have to be shifted back a position.
		if(numMoved > 0) {
			System.arraycopy(elementData,index + 1,elementData,index,numMoved);
		}
		return oldValue;
	}

	/**
	 * Removes the first occurrence of the specified element from this list, if
	 * it is present. If the list does not contain the element, it is unchanged.
	 * More formally, removes the element with the lowest index {@code i} such
	 * that {@code o.equals(get(i))} (if such an element exists). Returns
	 * {@code true} if this list contained the specified element (or
	 * equivalently, if this list changed as a result of the call).
	 * @param o The element to be removed from this list, if present.
	 * @return {@code true} if this list contained the specified element, or
	 * {@code false} otherwise.
	 */
	public boolean removeInt(final int o) {
		for(int i = 0;i < size;i++) { //Do a forward loop, since we have to remove the first occurrence.
			if(o == elementData[i]) { //We have a match! Remove the item.
				modCount++;
				final int numMoved = --size - i; //The number of items that have to be shifted back a position.
				if(numMoved > 0) {
					System.arraycopy(elementData,i + 1,elementData,i,numMoved);
				}
				return true;
			}
		}
		return false; //Not found in the list.
	}

	/**
	 * Removes from this list all of the elements whose index is between
	 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive. Shifts any
	 * succeeding elements to the left (reduces their index). This call shortens
	 * the list by {@code (toIndex - fromIndex)} elements. If
	 * {@code toIndex == fromIndex}, this operation has no effect.
	 * @param fromIndex The index of the first element to be removed.
	 * @param toIndex The index after the last element to be removed.
	 * @throws IndexOutOfBoundsException An endpoint was out of bounds
	 * ({@code fromIndex &lt; 0 || toIndex &gt; size || fromIndex &gt;
	 * toIndex}).
	 */
	@Override
	public void removeRange(final int fromIndex,final int toIndex) {
		if(fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
			throw new IndexOutOfBoundsException("Tried to remove values from the range " + fromIndex + " to " + toIndex + " (current size is " + size + ").");
		}
		final int toRemove = toIndex - fromIndex; //The number of items that have to be removed.
		if(toRemove == 0) { //No structural modification, and no superfluous empty arraycopy.
			return;
		}
		modCount++;
		final int numMoved = size - toIndex; //The number of items that have to be shifted back.
		System.arraycopy(elementData,toIndex,elementData,fromIndex,numMoved);
		size -= toRemove; //Forget about the extra elements after the end.
	}

	/**
	 * Retains only the elements in this list that are contained in the
	 * specified collection. In other words, removes from this list all of its
	 * elements that are not contained in the specified collection.
	 * @param c The collection containing elements to be retained in this list.
	 * @return {@code true} if this list changed as a result of the call.
	 * @throws ClassCastException The class of an element of this list is
	 * incompatible with the specified collection.
	 * @throws NullPointerException The specified collection contains a
	 * {@code null} element, or the specified collection is {@code null}.
	 * @see Collection.contains(Object)
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
		final int[] elementData = this.elementData; //Make a local copy of this pointer for faster access.
		int pos = 0; //Current size of our result.
		for(int i = 0;i < size;i++) { //Forward loop, since we want to retain our present order.
			if(c.contains(Integer.valueOf(i))) { //We should retain this item.
				elementData[pos++] = elementData[i];
			}
		}
		if(pos != size) { //Not every item was retained.
			modCount += size - pos;
			size = pos;
			return true;
		}
		return false;
	}

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element.
	 * @param index The index of the element to replace.
	 * @param element The element to be stored at the specified position.
	 * @return The element previously at the specified position.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt;= size()}).
	 */
	public int set(final int index,final int element) {
		if(index >= size) {
			throw new IndexOutOfBoundsException("Tried to set the value at index " + index + " (current size is " + size + ").");
		}
		final int oldValue = elementData[index]; //The old value is about to be overwritten, but we must return it, so store it.
		elementData[index] = element;
		return oldValue;
	}

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element.
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link set(int,int)} method.
	 * Please use the {@link set(int,int)} method when possible.</p>
	 * @param index The index of the element to replace.
	 * @param element The element to be stored at the specified position.
	 * @return The element previously at the specified position, as a reference-
	 * type {@link Integer}.
	 * @throws IndexOutOfBoundsException The index is out of range ({@code index
	 * &lt; 0 || index &gt;= size()}).
	 */
	@Override
	public Integer set(final int index,final Integer element) {
		if(index >= size) {
			throw new IndexOutOfBoundsException("Tried to set the value at index " + index + " (current size is " + size + ").");
		}
		final int oldValue = elementData[index]; //The old value is about to be overwritten, but we must return it, so store it.
		elementData[index] = element; //Implicit cast.
		return Integer.valueOf(oldValue);
	}

	/**
	 * Returns the number of elements in this list.
	 * @return The number of elements in this list.
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns a view of the portion of this list between the specified
	 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive. (If
	 * {@code fromIndex} and {@code toIndex} are equal, the returned list is
	 * empty.) The returned list is backed by this list, so non-structural
	 * changes in the returned list are reflected in this list, and vice-versa.
	 * The returned list supports all of the optional list operations.
	 * <p>This method eliminates the need for explicit range operations (of the
	 * sort that commonly exist for arrays). Any operation that expects a list
	 * can be used as a range operation by passing a sub-list view instead of a
	 * whole list. For example, the following idiom removes a range of elements
	 * from a list:</p>
	 * <p>{@code     list.subList(from,to).clear();}</p>
	 * <p>Similar idioms may be constructed for {@link indexOf(int)},
	 * {@link indexOf(Object)}, {@link lastIndexOf(int)} and
	 * {@link lastIndexOf(Object)}, and all of the algorithms in the
	 * {@link Collections} class can be applied to a sub-list.</p>
	 * <p>The semantics of the list returned by this method become undefined if
	 * the backing list (i.e., this list) is <i>structurally modified</i> in any
	 * way other than via the returned list. (Structural modifications are those
	 * that change the size of the list, or otherwise perturb it in such a
	 * fashion that iterations in progress may yield incorrect results.)</p>
	 * @param fromIndex The low endpoint (inclusive) of the sub-list.
	 * @param toIndex The high endpoint (exclusive) of the sub-list.
	 * @return A view of the specified range within this list.
	 * @throws IndexOutOfBoundsException An endpoint index is out of range
	 * ({@code fromIndex &lt; 0 || toIndex &gt; size}).
	 * @throws IllegalArgumentException The endpoint indices are out of order
	 * ({@code fromIndex &gt; toIndex}).
	 */
	@Override
	public List<Integer> subList(final int fromIndex,final int toIndex) {
		if(fromIndex < 0 || toIndex > size) {
			throw new IndexOutOfBoundsException("Tried to create a sublist from index " + fromIndex + " to " + toIndex + " (current size is " + size + ").");
		}
		if(fromIndex > toIndex) {
			throw new IllegalArgumentException("Tried to create a reverse sublist: from index " + fromIndex + " to " + toIndex + ".");
		}
		return new SubList(this,fromIndex,toIndex);
	}

	/**
	 * Trims the capacity of this {@code ArrayListInt} instance to be the list's
	 * current size. An application can use this operation to minimize the
	 * storage of an {@code ArrayListInt} instance.
	 */
	public void trimToSize() {
		modCount++; //This is a structural modification.
		if(size < elementData.length) { //Don't trim if we're already at a tight fit.
			elementData = Arrays.copyOf(elementData,size);
		}
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence (from first to last element). If the list fits in the specified
	 * array, it is returned therein. Otherwise, a new array is allocated with
	 * the size of this list.
	 * <p>If the list fits in the specified array with room to spare (i.e. the
	 * array has more elements than the list), the rest of the elements are left
	 * unchanged.</p>
	 * @param a The array into which the elements of the list are to be stored,
	 * if it is big enough; otherwise, a new array is allocated for this
	 * purpose.
	 * @return An array containing the elements of the list.
	 * @throws NullPointerException The specified array is {@code null}.
	 */
	public int[] toArray(int[] a) {
		if(a.length < size) { //Not large enough.
			a = new int[size]; //Create a new array to store everything in.
		}
		System.arraycopy(elementData,0,a,0,size); //Copy the data into the result array.
		return a;
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence (from first to last element).
	 * <p>The returned array will be "safe" in that no references to it are
	 * maintained by this list. (In other words, this method must allocate a new
	 * array.) The caller is thus free to modify the returned array.</p>
	 * <p>This method acts as bridge between array-based and collection-based
	 * APIs.</p>
	 * @return An array containing all of the elements in this list in proper
	 * sequence.
	 * @see Arrays.asList(Object[])
	 */
	@Override
	public Object[] toArray() {
		Object[] result = new Object[size];
		for(int i = size - 1;i >= 0;i--) { //Cast the elements one by one. No autoboxing for System.arraycopy exists.
			result[i] = elementData[i]; //Implicit cast to Integer.
		}
		return result;
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence (from first to last element); the runtime type of the returned
	 * array is that of the specified array. If the list fits in the specified
	 * array, it is returned therein. Otherwise, a new array is allocated with
	 * the runtime type of the specified array and the size of this list.
	 * <p>If the list fits in the specified array with room to spare (i.e., the
	 * array has more elements than the list), the element in the array
	 * immediately following the end of the collection is set to {@code null}.
	 * (This is useful in determining the length of the list. The list does not
	 * contain any {@code null} elements.)
	 * <p>This method is compatible with the {@link List} and {@link Collection}
	 * interfaces, but is a bit slower than the {@link toArray(int[])} method.
	 * Please use the {@link toArray(int[])} method when possible.</p>
	 * @param <T> The runtime type of the array. This must be {@link Integer} or
	 * a supertype thereof.
	 * @param a The array into which the elements of the list are to be stored,
	 * if it is big enough; otherwise, a new array of the same runtime type is
	 * allocated for this purpose.
	 * @return An array containing the elements of the list.
	 * @throws ClassCastException The runtime type of the specified array is not
	 * a supertype of {@link Integer}.
	 * @throws NullPointerException The specified array is {@code null}.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(final T[] a) {
		if(a.length < size) { //Not large enough.
			return (T[])toArray(); //Throws ClassCastException if T is not of the right type for integers.
		}
		Integer[] arr = (Integer[])a; //Throws ClassCastException if T is not of the right type for integers.
		for(int i = size - 1;i >= 0;i--) { //Cast the elements one by one. No autoboxing for System.arraycopy exists.
			arr[i] = elementData[i]; //Implicit cast to Integer.
		}
		//System.arraycopy(elementData,0,a,0,size);
		if(arr.length > size) { //Too large.
			arr[size] = null; //Also make the element after it null then.
		}
		return a; //Changes to arr are reflected in a, since it's the same array.
	}

	/**
	 * Returns a string representation of this {@code ArrayListInt}. The string
	 * representation consists of the {@code ArrayListInt}'s elements in the
	 * order they are returned by its iterator, enclosed in square brackets
	 * ({@code "[]"}). Adjacent elements are separated by the characters
	 * {@code ", "} (comma and space). Elements are converted to strings as by
	 * {@link Integer.toString()}.
	 * @return A string representation of this {@code ArrayListInt}.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(2 + (size << 2)); //Predict a length of 2 * size per int, and allocate 2 extra per int for the comma and space, and 2 extra for brackets.
		sb.append('['); //Opening bracket.
		for(int i = 0;i < elementData.length;i++) { //A forward loop is required to put the elements in order.
			sb.append(elementData[i]);
			sb.append(',').append(' '); //Comma and space as separators.
		}
		return sb.append(']').toString(); //Append a closing bracket.
	}

	/**
	 * Save the state of the {@code ArrayListInt} instance to a stream (that is,
	 * serialise it).
	 * @serialData The length of the array backing the {@code ArrayListInt} is
	 * emitted ({@code int}), followed by all of its elements (each an
	 * {@code int}) in the proper order.
	 * @param s The stream to save the state of the {@code ArrayListInt} to.
	 * @throws IOException The stream could not be written to.
	 */
	private void writeObject(final ObjectOutputStream s) throws IOException {
		final int expectedModCount = modCount; //Make sure we're not modifying the array structurally while writing.
		s.defaultWriteObject(); //Write the element count, and any hidden stuff.
		s.write(elementData.length);
		for(int i = 0;i < size;i++) { //Write all elements in the proper order.
			s.writeInt(elementData[i]);
		}
		if(modCount != expectedModCount) { //The list was structurally modified!
			throw new ConcurrentModificationException();
		}
	}

	/**
	 * An optimised iterator backed by this {@code ArrayListInt}. This iterator
	 * allows the programmer to traverse a list in order.
	 */
	private class Itr implements Iterator<Integer> {
		/**
		 * The index of the next element to return.
		 */
		int cursor;

		/**
		 * The index of the last returned element. If no element has yet been
		 * returned, this will be {@code -1}.
		 */
		int lastReturned = -1;

		/**
		 * Stores the modCount of the ArrayListInt at construction. If at some
		 * point this is not equal to the modCount of the ArrayListInt any more,
		 * a {@code ConcurrentModificationException} will be thrown.
		 */
		int expectedModCount = modCount;

		/**
		 * Returns {@code true} if the iteration has more elements. (In other
		 * words, returns {@code true} if {@link next()} would return an element
		 * rather than throwing an exception.)
		 * @return {@code true} if the iteration has more elements.
		 */
		@Override
		public boolean hasNext() {
			return cursor < size;
		}

		/**
		 * Returns the next element in the iteration.
		 * @return The next element in the iteration.
		 * @throws NoSuchElementException The iteration has no more elements.
		 */
		@Override
		public Integer next() {
			if(modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
				throw new ConcurrentModificationException();
			}
			int i = cursor; //Store this value for concurrency. Multiple concurrent next() operations race for this read.
			if(i >= size) {
				throw new NoSuchElementException();
			}
			int[] elementData = ArrayListInt.this.elementData; //Copy in case the elementData is just being replaced with a structural modification.
			if(i >= elementData.length) {
				throw new ConcurrentModificationException();
			}
			cursor = i + 1;
			return elementData[lastReturned = i]; //Also update the last returned value.
		}

		/**
		 * Removes from the underlying collection the last element returned by
		 * this iterator. This method can be called only once per call to
		 * {@link next()}. The behaviour of an iterator is unspecified if the
		 * underlying collection is modified while the iteration is in progress
		 * in any way other than by calling this method.
		 * @throws IllegalStateException The {@link next()} method has not yet
		 * been called, or the {@code remove()} method has already been called
		 * after the last call to the {@link next()} method.
		 */
		@Override
		public void remove() {
			if(lastReturned < 0) { //Nothing returned yet. Nothing to remove.
				throw new IllegalStateException();
			}
			if(modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
				throw new ConcurrentModificationException();
			}
			try {
				ArrayListInt.this.removeIndex(lastReturned); //Try to remove the last returned element.
				cursor = lastReturned; //Set the cursor back.
				lastReturned = -1; //Only one item at a time can be removed.
				expectedModCount = modCount; //This changed the modCount. Update it.
			} catch(IndexOutOfBoundsException e) { //This was not due to an out of bounds index, but because the backing array changed before trying to remove this.
				throw new ConcurrentModificationException();
			}
		}
	}

	/**
	 * An optimised list iterator backed by this {@code ArrayListInt}. This
	 * iterator for lists allows the programmer to traverse the list in either
	 * direction, modify the list during iteration, and obtain the iterator's
	 * current position in the list. A {@code ListIterator} has no current
	 * element; its <i>cursor position</i> always lies between the element that
	 * would be returned by a call to {@link previous()} and the element that
	 * would be returned by a call to {@link next()}. An iterator for a list of
	 * length {@code n} has {@code n + 1} possible cursor positions.
	 * <p>Note that the {@link remove()} and {@link set(Integer)} methods are
	 * <i>not</i> defined in terms of the cursor position; they are defined to
	 * operate on the last element returned by a call to {@link next()} or
	 * {@link previous()}.</p>
	 */
	private class ListItr implements ListIterator<Integer> {
		/**
		 * The index of the next element to return.
		 */
		int cursor;

		/**
		 * The index of the last returned element. If no element has yet been
		 * returned, this will be {@code -1}.
		 */
		int lastReturned = -1;

		/**
		 * Stores the modCount of the ArrayListInt at construction. If at some
		 * point this is not equal to the modCount of the ArrayListInt any more,
		 * a {@code ConcurrentModificationException} will be thrown.
		 */
		int expectedModCount = modCount;

		/**
		 * Creates a new list iterator that starts iterating at the beginning of
		 * the list.
		 */
		ListItr() {
			//Nothing to do, but the constructor needs to exist without arguments.
		}

		/**
		 * Creates a new list iterator that starts iterating at the specified
		 * index in the list.
		 * @param index The index to start iterating at.
		 */
		ListItr(final int index) {
			cursor = index;
		}

		/**
		 * Inserts the specified element into the list. The element is inserted
		 * immediately before the element that would returned by {@link next()},
		 * if any, and after the element that would be returned by
		 * {@link previous()}, if any. If the list contains no elements, the new
		 * element becomes the sole element on the list. The new element is
		 * inserted before the implicit cursor: a subsequent call to
		 * {@link next()} would be unaffected, and a subsequent call to
		 * {@link previous()} would return the new element. This call increases
		 * by one the value that would be returned by a call to
		 * {@link nextIndex()} or {@link previousIndex()}.
		 * @param e The element to insert.
		 */
		@Override
		public void add(final Integer e) {
			if(modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
				throw new ConcurrentModificationException();
			}
			try {
				int i = cursor; //Save this here in case of a concurrent call changes the cursor position.
				ArrayListInt.this.add(i,e); //Actually add the element.
				cursor = i + 1;
				lastReturned = -1; //Disallow calls to remove(), set(), etc.
				expectedModCount = modCount;
			} catch(IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		/**
		 * Returns {@code true} if this list iterator has more elements when
		 * traversing the list in the forward direction. In other words, returns
		 * {@code true} if {@link next()} would return an element rather than
		 * throwing an exception.
		 * @return {@code true} if the list iterator has more elements when
		 * traversing the list in the forward direction, or {@code false} if
		 * there are no more elements in that direction.
		 */
		@Override
		public boolean hasNext() {
			return cursor < size;
		}

		/**
		 * Returns {@code true} if this list iterator has more elements when
		 * traversing the list in the reverse direction. In other words, returns
		 * {@code true} if {@link previous()} would return an element rather
		 * than throwing an exception.
		 * @return {@code true} if the list iterator has more elements when
		 * traversing the list in the reverse direction, or {@code false} if
		 * there are no more elements in that direction.
		 */
		@Override
		public boolean hasPrevious() {
			return cursor != 0;
		}

		/**
		 * Returns the next element in the list and advances the cursor
		 * position. This method may be called repeatedly to iterate through the
		 * list, or intermixed with calls to {@link previous()} to go back and
		 * forth. Note that alternating calls to {@code next()} and
		 * {@link previous()} will return the same element repeatedly.
		 * @return The next element in the list.
		 * @throws NoSuchElementException The iteration has no next element.
		 */
		@Override
		public Integer next() {
			if(modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
				throw new ConcurrentModificationException();
			}
			int i = cursor; //Store this value for concurrency. Multiple concurrent next() operations race for this read.
			if(i >= size) {
				throw new NoSuchElementException();
			}
			int[] elementData = ArrayListInt.this.elementData; //Copy in case the elementData is just being replaced with a structural modification.
			if(i >= elementData.length) {
				throw new ConcurrentModificationException();
			}
			cursor = i + 1;
			return elementData[lastReturned = i]; //Also update the last returned value.
		}

		/**
		 * Returns the index of the element that would be returned by a
		 * subsequent call to {@link next()}. Returns the list size if the list
		 * iterator is at the end of the list.
		 * @return The index of the element that would be returned by a
		 * subsequent call to {@link next()}, or the list size if the list
		 * iterator is at the end of the list.
		 */
		@Override
		public int nextIndex() {
			return cursor;
		}

		/**
		 * Returns the previous element in the list and moves the cursor
		 * position backwards. This method may be called repeatedly to iterate
		 * through the list backwards, or intermixed with calls to
		 * {@link next()} to go back and forth. Note that alternating calls to
		 * {@link next()} and {@code previous()} will return the same element
		 * repeatedly.
		 * @return The previous element in the list.
		 * @throws NoSuchElementException The iteration has no previous element.
		 */
		@Override
		public Integer previous() {
			if(modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			int i = cursor - 1; //Store this value for concurrency. Multiple concurrent next() operations race for this read.
			if(i < 0) {
				throw new NoSuchElementException();
			}
			int[] elementData = ArrayListInt.this.elementData; //Copy in case the elementData is just being replaced with a structural modification.
			if(i >= elementData.length) {
				throw new ConcurrentModificationException();
			}
			cursor = i;
			return elementData[lastReturned = i]; //Also update the last returned value.
		}

		/**
		 * Returns the index of the element that would be returned by a
		 * subsequent call to {@link previous()}. Returns {@code -1} if the list
		 * iterator is at the beginning of the list.
		 * @return The index of the element that would be returned by a
		 * subsequent call to {@link previous()}, or {@code -1} if the list
		 * iterator is at the beginning of the list.
		 */
		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		/**
		 * Removes from the list the last element that was returned by
		 * {@link next()} or {@link previous()}. This call can only be made once
		 * per call to {@link next()} or {@link previous()}. It can be made only
		 * if {@code add(Integer)} has not been called after the last call to
		 * {@link next()} or {@link previous()}.
		 * @throws IllegalStateException Neither {@link next()} nor
		 * {@link previous()} have been called, or {@code remove()} or
		 * {@link add(Integer)} have been called after the last call to
		 * {@link next()} or {@link previous()}.
		 */
		@Override
		public void remove() {
			if(lastReturned < 0) { //Nothing returned yet. Nothing to remove.
				throw new IllegalStateException();
			}
			if(modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
				throw new ConcurrentModificationException();
			}
			try {
				ArrayListInt.this.removeIndex(lastReturned); //Try to remove the last returned element.
				cursor = lastReturned; //Set the cursor back.
				lastReturned = -1; //Only one item at a time can be removed.
				expectedModCount = modCount; //This changed the modCount. Update it.
			} catch(IndexOutOfBoundsException e) { //This was not due to an out of bound index, but because the backing array changed before trying to remove this.
				throw new ConcurrentModificationException();
			}
		}

		/**
		 * Replaces the last element returned by {@link next()} or
		 * {@link previous()} with the specified element. This call can be made
		 * only if neither {@link remove()} nor {@link add(Integer)} have been
		 * called after the last call to {@link next()} or {@link previous()}.
		 * @param e The element with which to replace the last element returned
		 * by {@link next()} or {@link previous()}.
		 * @throws IllegalStateException Neither {@link next()} nor
		 * {@link previous()} have been called, or {@link remove()} or
		 * {@link add(Integer)} have been called after the last call to
		 * {@link next()} or {@link previous()}.
		 */
		@Override
		public void set(final Integer e) {
			if(lastReturned < 0) { //Nothing returned yet. Nothing to change.
				throw new IllegalStateException();
			}
			if(modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
				throw new ConcurrentModificationException();
			}
			try {
				ArrayListInt.this.set(lastReturned,e); //Actually try to set the last returned element to the given value.
			} catch(IndexOutOfBoundsException ex) { //This was not due to an out of bound index, but because the backing array changed before trying to set this element.
				throw new ConcurrentModificationException();
			}
		}
	}

	/**
	 * An optimised sublist backed by this {@code ArrayListInt}. This sublist
	 * provides a partial view of the backing list by narrowing the range of
	 * indices it is allowed to access from the backing {@code ArrayListInt}.
	 */
	private class SubList implements List<Integer>,RandomAccess {
		/**
		 * The backing list of which this list is a sublist. Changes in the
		 * sublist are reflected in the parent list and vice-versa.
		 */
		private final ArrayListInt parent;

		/**
		 * The position in the parent list where this sublist starts.
		 */
		private final int parentOffset;

		/**
		 * The number of times this list has been <i>structurally modified</i>.
		 * Structural modifications are those that change the size of the list,
		 * or otherwise perturb it in such a fashion that iterations in progress
		 * may yield incorrect results. This counter will be checked against the
		 * {@code modCount} of the parent list to determine whether the array
		 * has been structurally modified since the creation of this sublist.
		 */
		private int modCount;

		/**
		 * The number of elements in the sublist.
		 */
		int size;

		/**
		 * Constructs a new sublist within an {@code ArrayListInt}. The sublist
		 * will get the specified delimiters to determine its new range of
		 * allowed indices.
		 * @param parent The backing {@link List} upon which to build the
		 * sublist.
		 * @param offset The starting offset of the cursor within the sublist.
		 * @param fromIndex The index in the backing list where the sublist
		 * starts.
		 * @param toIndex The index in the backing list where the sublist ends.
		 */
		SubList(final ArrayListInt parent,final int fromIndex,final int toIndex) {
			this.parent = parent;
			this.parentOffset = fromIndex;
			this.size = toIndex - fromIndex;
			this.modCount = parent.modCount; //Copy modCount from the backing list so that we can detect structural changes.
		}

		/**
		 * Appends the specified element to the end of this sublist. The
		 * {@code null} value is not allowed. The list will be inserted in its
		 * parent list at the point where this sublist ends.
		 * @param e The element to be appended to this sublist and inserted into
		 * the parent list.
		 * @return Always returns {@code true}.
		 * @throws NullPointerException The specified element is {@code null}.
		 */
		@Override
		public boolean add(final Integer e) {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			parent.add(parentOffset + this.size,e); //Add at the end.
			this.modCount = parent.modCount; //The backing array may have changed.
			this.size++;
			return true;
		}

		/**
		 * Inserts the specified element at the specified position in this list,
		 * and into its parent list at the corresponding position. Shifts the
		 * element currently at that position (if any) and any subsequent
		 * elements to the right (adds one to their indices).
		 * @param index The index in the sublist at which the specified element
		 * is to be inserted.
		 * @param e The element to be inserted.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 * @throws IndexOutOfBoundsException The index is out of range
		 * ({@code index &lt; 0 || index &gt; size()}).
		 * @throws NullPointerException The specified element is {@code null}.
		 */
		@Override
		public void add(final int index,final Integer e) {
			if(index < 0 || index > this.size) {
				throw new IndexOutOfBoundsException("Tried to add a value at index " + index + " (current size is " + this.size + ").");
			}
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			parent.add(parentOffset + index,e);
			this.modCount = parent.modCount; //The backing array may have changed.
			this.size++;
		}

		/**
		 * Appends all of the elements in the specified collection to the end of
		 * this sublist and inserts them into the parent list at the end of this
		 * sublist, in the order that they are returned by the specified
		 * collection's iterator. The behaviour of this operation is undefined
		 * if the specified collection is modified while the operation is in
		 * progress. Note that this will occur if the specified collection is
		 * this list, or any parent- or sublist of it, and it's nonempty.
		 * @param c The collection containing elements to be added to this
		 * sublist.
		 * @return {@code true} if this list changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 * @throws NullPointerException The specified collection contains one or
		 * more {@code null} elements, or the specified collection is
		 * {@code null}.
		 */
		@Override
		public boolean addAll(final Collection<? extends Integer> c) {
			int collectionSize = c.size();
			if(collectionSize == 0) { //Nothing to add, so return false (since the list is unchanged).
				return false;
			}
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			parent.addAll(parentOffset + this.size,c); //Add at the end.
			this.modCount = parent.modCount; //The backing array may have changed.
			this.size += collectionSize;
			return true;
		}

		/**
		 * Inserts all of the elements in the specified collection into this
		 * list at the specified position, and into its parent list at the
		 * corresponding position. Shifts the element currently at that position
		 * (if any) and any subsequent elements to the right (increases their
		 * indices). The new elements will appear in this list in the order that
		 * they are returned by the specified collection's iterator. The
		 * behaviour of this operation is undefined if the specified collection
		 * is modified while the operation is in progress. Note that this will
		 * occur if the specified collection is this list, or its parent- or
		 * sublists, and it's nonempty.
		 * @param index The index at which to insert the first element from the
		 * specified collection.
		 * @param c The collection containing elements to be added to this list.
		 * @return {@code true} if this list changed as a result of the call, or
		 * {@code false} otherwise.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 * @throws IndexOutOfBoundsException The index is out of range
		 * ({@code index &lt; 0 || index &gt; size()}).
		 * @throws NullPointerException The specified collection contains one or
		 * more {@code null} elements, or the specified collection is
		 * {@code null}.
		 */
		@Override
		public boolean addAll(final int index,final Collection<? extends Integer> c) {
			if(index < 0 || index > this.size) {
				throw new IndexOutOfBoundsException("Tried to add a value at index " + index + " (current size is " + this.size + ").");
			}
			int collectionSize = c.size();
			if(collectionSize == 0) { //Nothing to add, so return false (since the list is unchanged).
				return false;
			}
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			parent.addAll(parentOffset + index,c);
			this.modCount = parent.modCount; //The backing array may have changed.
			this.size += collectionSize;
			return true;
		}

		/**
		 * Removes all of the elements from this sublist. The elements within
		 * the sublist will be removed from the parent list as well. The sublist
		 * will be empty after this call returns.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public void clear() {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			parent.removeRange(parentOffset,this.size); //Remove the range along which this sublist spans.
			this.modCount = parent.modCount; //The backing array may have changed.
			this.size = 0;
		}

		/**
		 * Returns {@code true} if this sublist contains the specified element.
		 * More formally, returns {@code true} if and only if the given object
		 * is an {@link Integer} and this sublist contains at least one element
		 * {@code e} such that {@code o == e}.
		 * @param o The element whose presence in this sublist is to be tested.
		 * @return {@code true} if this list contains the specified element, or
		 * {@code false} otherwise.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public boolean contains(final Object o) {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			if(o == null) { //This sublist cannot contain null elements.
				return false;
			}
			if(!(o instanceof Integer)) { //This sublist can only contain integers.
				return false;
			}
			final int value = ((Integer)o).intValue(); //Then we know we can cast safely.
			for(int i = parentOffset + this.size - 1;i >= parentOffset;i--) { //Loop backwards for speed. Assume no bias in where the element may be.
				if(parent.elementData[i] == value) { //We have a match.
					return true;
				}
			}
			return false; //No match.
		}

		/**
		 * Returns {@code true} if this sublist contains all of the elements of
		 * the specified collection.
		 * @param c The collection to be checked for containment in this list.
		 * @return {@code true} if this list contains all of the elements of the
		 * specified collection.
		 * @throws ClassCastException The types of one or more elements in the
		 * specified collection are not supertypes of {@link Integer}.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public boolean containsAll(final Collection<?> c) {
			OUTER:
			for(Object e : c) {
				if(e == null) { //This sublist cannot contain null elements.
					return false;
				}
				if(!(e instanceof Integer)) { //This sublist can only contain integers.
					return false;
				}
				final int value = ((Integer)e).intValue(); //Then we know we can cast safely.
				for(int i = parentOffset + this.size - 1;i >= parentOffset;i--) { //Loop backwards for speed. Assume no bias in where the element may be.
					if(parent.elementData[i] == value) { //We have a match.
						continue OUTER;
					}
				}
				return false; //No match found for this element.
			}
			return true; //Everything eventually found a match.
		}

		/**
		 * Compares the specified object with this sublist for equality. Returns
		 * {@code true} if and only if the specified object is also a
		 * {@link List}, both lists have the same size, and all corresponding
		 * pairs of elements in the two lists are <i>equal</i>. Two elements
		 * {@code e1} and {@code e2} are <i>equal</i> if {@code e1.equals(e2)}.
		 * In other words, two lists are defined to be equal if they contain the
		 * same elements in the same order.
		 * @param o The object to be compared for equality with this list.
		 * @return {@code true} if the specified object is equal to this list,
		 * or {@code false} otherwise.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public boolean equals(final Object o) {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			if(o == this) { //Hey, that's me!
				return true;
			}
			if(!(o instanceof List) || ((List)o).size() != this.size) { //Not a list at all or not the same size.
				return false;
			}
			final ListIterator iterator = ((List)o).listIterator();
			final int highEnd = parentOffset + this.size;
			for(int i = parentOffset;i < highEnd;i++) { //Check every element.
				final Object other = iterator.next();
				if(other == null || !other.equals(parent.elementData[i])) { //Null or not the same.
					return false;
				}
			}
			return true; //Same type. Same size. Same elements.
		}

		/**
		 * Returns the element at the specified position in this sublist.
		 * @param index The index of the element to return.
		 * @return The specified at the specified position in this sublist.
		 * @throws IndexOutOfBoundsException The index is out of range
		 * ({@code index &lt; 0 || index &gt;= size()}).
		 */
		@Override
		public Integer get(final int index) {
			if(index >= this.size) {
				throw new IndexOutOfBoundsException("Tried to get the value at index " + index + " (current size is " + this.size + ").");
			}
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			return parent.elementData[parentOffset + index]; //Implicit cast.
		}

		/**
		 * Returns the hash code for this sublist. The hash code of a list is
		 * defined to be the result of the following calculation:
		 * <p><code>int hashCode = 1;
		 * for(Integer e : list) {
		 *     hashCode = 31 * hashCode + e;
		 * }</code></p>
		 * <p>This ensures that {@code list1.equals(list2)} implies that
		 * {@code list1.hashCode() == list2.hashCode()} for any two lists
		 * {@code list1} and {@code list2}, as required by the general contract
		 * of {@link Object.hashCode()}.</p>
		 * @return The hash code for this list.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public int hashCode() {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			int hashCode = 1; //This algorithm is literally in the Javadoc!
			final int highEnd = parentOffset + this.size;
			for(int i = parentOffset;i < highEnd;i++) { //Instead of using the iterator, we'll just iterate ourselves though.
				hashCode = 31 * hashCode + parent.elementData[i]; //The 31 is to reduce collisions. Any prime will work.
			}
			return hashCode;
		}

		/**
		 * Returns the index of the first occurrence of the specified element in
		 * this sublist, or {@code -1} if this list does not contain the
		 * element. More formally, returns the lowest index {@code i} such that
		 * {@code o.equals(get(i))}, or {@code -1} if there is no such index.
		 * @param o The element to search for.
		 * @return The index of the first occurrence of the specified element in
		 * this sublist, or {@code -1} if this sublist does not contain the
		 * element.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public int indexOf(final Object o) {
			if(o == null) { //This list contains no null elements.
				return -1;
			}
			if(!(o instanceof Integer)) { //We store only integers.
				return -1;
			}
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			final int value = (int)o; //Cast this once.
			final int highEnd = parentOffset + this.size;
			for(int i = parentOffset;i < highEnd;i++) { //Forward loop required, since we need the first match.
				if(value == parent.elementData[i]) { //We have a match!
					return i;
				}
			}
			return -1; //No match in the entire list.
		}

		/**
		 * Returns {@code true} if this sublist contains no elements.
		 * @return {@code true} if this sublist contains no elements.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public boolean isEmpty() {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			return this.size == 0;
		}

		/**
		 * Returns an iterator over the elements in this sublist in proper
		 * sequence.
		 * @return An iterator over the elements in this list in proper
		 * sequence.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public Iterator<Integer> iterator() {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			return new SubItr(this);
		}

		/**
		 * Returns the index of the last occurrence of the specified element in
		 * this sublist, or {@code -1} if this sublist does not contain the
		 * element. More formally, returns the highest index {@code i} such that
		 * {@code o.equals(get(i))}, or {@code -1} if there is no such index.
		 * @param o The element to search for.
		 * @return The index of the last occurrence of the specified element in
		 * this sublist, or {@code -1} if this sublist does not contain the
		 * element.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public int lastIndexOf(final Object o) {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			if(o == null) { //This list contains no null elements.
				return -1;
			}
			if(!(o instanceof Integer)) { //We store only integers.
				return -1;
			}
			final int value = (int)o; //Cast this once.
			for(int i = parentOffset + this.size - 1;i >= parentOffset;i--) { //Walk backwards through the list.
				if(value == parent.elementData[i]) {
					return i;
				}
			}
			return -1; //No match in the entire list.
		}

		/**
		 * Returns a list iterator over the elements in this sublist (in proper
		 * sequence).
		 * @return A list iterator over the elements in this sublist (in proper
		 * sequence).
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public ListIterator<Integer> listIterator() {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			return new SubListItr(this);
		}

		/**
		 * Returns a list iterator over the elements in this sublist (in proper
		 * sequence), starting at the specified position in the sublist. The
		 * specified index indicates the first element that would be returned by
		 * an initial call to {@link Iterator.next()}. An initial call to
		 * {@link Iterator.previous()} would return the element with the
		 * specified index minus one.
		 * @param index The index of the first element to be returned from the
		 * list iterator (by a call to {@link Iterator.next()}.
		 * @return A list iterator over the elements in this sublist (in proper
		 * sequence), starting at the specified position in the list.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 * @throws IndexOutOfBoundsException The index is out of range
		 * ({@code index &lt; 0 || index &gt; size()}).
		 */
		@Override
		public ListIterator<Integer> listIterator(final int index) {
			if(index < 0 || index > this.size) {
				throw new IndexOutOfBoundsException("Tried to make a list iterator start at index " + index + " (current size is " + this.size + ").");
			}
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			return new SubListItr(this,index);
		}

		/**
		 * Removes the element at the specified position in this list and at the
		 * corresponding position in the parent list. Shifts any subsequent
		 * elements to the left (subtracts one from their indices). Returns the
		 * element that was removed from the list.
		 * @param index The index of the element to be removed.
		 * @return The element previously at the specified position.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 * @throws IndexOutOfBoundsException The index is out of range
		 * ({@code index &lt; 0 || index &gt;= size()}).
		 */
		@Override
		public Integer remove(final int index) {
			if(index < 0 || index >= this.size) {
				throw new IndexOutOfBoundsException("Tried to remove a value at index " + index + " (current size is " + this.size + ").");
			}
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			Integer result = parent.remove(parentOffset + index); //Remove from the parent.
			this.modCount = parent.modCount; //The backing array may have structurally changed.
			this.size--;
			return result;
		}

		/**
		 * Removes the first occurrence of the specified element from this
		 * sublist and its parent list, if it is present in the sublist. If this
		 * sublist does not contain the element, both are unchanged. More
		 * formally, removes the element with the lowest index {@code i} such
		 * that {@code o.equals(get(i))}.
		 * @param o The element to be removed from this list, if present.
		 * @return {@code true} if this list contained the specified element, or
		 * {@code false} otherwise.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public boolean remove(final Object o) {
			if(o == null) { //This list contains no null elements.
				return false;
			}
			if(!(o instanceof Integer)) { //We store only integers.
				return false;
			}
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			final int value = (int)o; //Cast this once.
			final int highEnd = parentOffset + size;
			for(int i = parentOffset;i < highEnd;i++) { //Do a forward loop, since we have to remove the first occurrence.
				if(value == parent.elementData[i]) { //We have a match! Remove the item.
					return parent.removeInt(i);
				}
			}
			return false; //Not found in the list.
		}

		/**
		 * Removes from this list and its parent list all of its elements that
		 * are contained in the specified collection. The elements are only
		 * removed if they are in the sublist; no elements from the parent list
		 * outside of this sublist are removed.
		 * @param c The collection containing elements to be removed from this
		 * list.
		 * @return {@code true} if this sublist changed as a result of the call,
		 * or {@code false} otherwise.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public boolean removeAll(final Collection<?> c) {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			final int[] elementData = ArrayListInt.this.elementData; //Make a local copy of this pointer for faster access.
			int pos = 0; //Current size of our result.
			final int highEnd = parentOffset + this.size;
			for(int i = parentOffset;i < highEnd;i++) { //Forward loop, since we want to retain our present order.
				if(!c.contains(Integer.valueOf(i))) { //We should retain this item.
					elementData[pos++] = elementData[i];
				}
			}
			if(pos != this.size) { //Not every item was retained.
				parent.removeRange(parentOffset + pos,highEnd); //Shift the rest outside of the sublist to the left.
				this.modCount = parent.modCount;
				this.size = pos;
				return true;
			}
			return false;
		}

		/**
		 * Retains only the elements in this list that are contained in the
		 * specified collection. In other words, removes from this list all of
		 * its elements that are not contained in the specified collection. The
		 * elements are only removed if they are in the sublist; no elements
		 * from the parent list outside of this sublist are removed. After this
		 * method has returned, the sublist will contain the intersection of its
		 * original contents and the contents of the specified collection.
		 * @param c The collection containing elements to be retained in this
		 * list.
		 * @return {@code true} if this list changed as a result of the call.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public boolean retainAll(final Collection<?> c) {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			final int[] elementData = ArrayListInt.this.elementData; //Make a local copy of this pointer for faster access.
			int pos = 0; //Current size of our result.
			final int highEnd = parentOffset + this.size;
			for(int i = parentOffset;i < highEnd;i++) { //Forward loop, since we want to retain our present order.
				if(c.contains(Integer.valueOf(i))) { //We should retain this item.
					elementData[pos++] = elementData[i];
				}
			}
			if(pos != this.size) { //Not every item was retained.
				parent.removeRange(parentOffset + pos,highEnd); //Shift the rest outside of the sublist to the left.
				this.modCount = parent.modCount;
				this.size = pos;
				return true;
			}
			return false;
		}

		/**
		 * Replaces the element at the specified position in this sublist with
		 * the specified element. The corresponding element in the parent list
		 * is replaced as well.
		 * @param index The index of the element to replace.
		 * @param e The element to be stored at the specified position.
		 * @return The element previously at the specified position.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 * @throws IndexOutOfBoundsException The index is out of range
		 * ({@code index &lt; 0 || index &gt;= size()}).
		 * @throws NullPointerException The specified element is {@code null}.
		 */
		@Override
		public Integer set(final int index,final Integer e) {
			if(index < 0 || index >= this.size) {
				throw new IndexOutOfBoundsException("Tried to change a value at index " + index + " (current size is " + this.size + ").");
			}
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			Integer oldValue = parent.elementData[parentOffset + index]; //Implicit cast.
			return oldValue;
		}

		/**
		 * Returns the number of elements in this sublist.
		 * @return The number of elements in this sublist.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public int size() {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			return this.size;
		}

		/**
		 * Returns a view of the portion of this sublist between the specified
		 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive. If
		 * {@code fromIndex} and {@code toIndex} are equal, the returned list is
		 * empty. The returned list is backed by this list, so non-structural
		 * changes in the returned list are reflected in this list, and vice-
		 * versa.
		 * <p>This method eliminates the need for explicit range operations (of
		 * the sort that commonly exist for arrays). Any operation that expects
		 * a list can be used as a range operation by passing a subList view
		 * instead of a whole list. For example, the following idiom removes a
		 * range of elements from a list:
		 * <p><code>list.subList(from,to).clear();</code></p>
		 * <p>Similar idioms may be constructed for {@code indexOf} and
		 * {@code lastIndexOf}, and all of the algorithms in the
		 * {@link Collections} class can be applied to a subList.</p>
		 * <p>If this list or its parents list is <i>structurally modified</i>
		 * in any way other than via the returned list, subsequent operations on
		 * the returned sublist will result in a
		 * {@link ConcurrentModificationException}. Structural modifications are
		 * those that change the size of this list, or otherwise perturb it in
		 * such a fashion that iterations in progress may yield incorrect
		 * results.</p>
		 * @param fromIndex The low endpoint (inclusive) of the subList.
		 * @param toIndex The high endpoint (exclusive) of the subList.
		 * @return A view of the specified range within this list.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 * @throws IndexOutOfBoundsException An endpoint index value is illegal
		 * ({@code fromIndex &lt; 0 || toIndex &gt; size || fromIndex &gt;
		 * toIndex}).
		 */
		@Override
		public List<Integer> subList(final int fromIndex,final int toIndex) {
			if(fromIndex < 0 || toIndex > this.size || fromIndex > toIndex) {
				throw new IndexOutOfBoundsException("Tried to take a sublist from index " + fromIndex + " to " + toIndex + " (current size is " + this.size + ").");
			}
			return new SubList(parent,parentOffset + fromIndex,parentOffset + toIndex);
		}

		/**
		 * Returns an array containing all of the elements in this sublist in
		 * proper sequence (from first to last element).
		 * <p>The returned array will be "safe" in that no references to it are
		 * maintained by this list. In other words, this method must allocate a
		 * new array. The caller is thus free to modify the returned array.</p>
		 * @return An array containing all of the elements in this sublist in
		 * proper sequence.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 */
		@Override
		public Object[] toArray() {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			Object[] result = new Object[this.size];
			System.arraycopy(ArrayListInt.this.elementData,parentOffset,result,0,this.size); //Copy the sublist's part into the new array.
			return result;
		}

		/**
		 * Returns an array containing all of the elements in this sublist in
		 * proper sequence (from first to last element); the runtime type of the
		 * returned array is that of the specified array. If the sublist fits in
		 * the specified array, it is returned therein. Otherwise, a new array
		 * is allocated with the runtime type of the specified array and the
		 * size of this sublist. If the list fits in the specified array with
		 * room to spare (i.e. the array has more elements than the sublist),
		 * the element in the array immediately following the end of the list is
		 * set to {@code null}. This is useful in determining the length of the
		 * list.
		 * @param <T> The runtime type of the elements in the array to return.
		 * This must be a supertype of {@link Integer} ({@link Object or
		 * {@link Integer}), otherwise an {@link ArrayStoreException} will be
		 * thrown.
		 * @param a The array into which the elements of this sublist are to be
		 * stored, if it is big enough; otherwise, a new array of the same
		 * runtime type is allocated for this purpose.
		 * @return An array containing the elements of this sublist.
		 * @throws ClassCastException The runtime type of the specified array is
		 * not a supertype of {@link Integer}.
		 * @throws ConcurrentModificationException The backing list was
		 * structurally modified since the constructing of this sublist.
		 * @throws NullPointerException The specified array is {@code null}.
		 */
		@Override
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(final T[] a) {
			if(parent.modCount != this.modCount) { //The backing array was structurally modified since the constructing of this sublist.
				throw new ConcurrentModificationException();
			}
			if(a.length < this.size) { //Not large enough.
				return (T[])toArray(); //Throws ClassCastException if T is not of the right type for integers.
			}
			System.arraycopy(ArrayListInt.this.elementData,parentOffset,a,0,this.size);
			if(a.length > this.size) { //Too large.
				a[this.size] = null; //Also make the element after it null then.
			}
			return a;
		}

		/**
		 * An optimised iterator backed by this {@code SubList}. This iterator
		 * allows the programmer to traverse the sublist in order.
		 */
		private class SubItr implements Iterator<Integer> {
			/**
			 * The index of the next element to return.
			 */
			int cursor;

			/**
			 * The index of the last returned element. If no element has yet
			 * been returned, this will be {@code -1}.
			 */
			int lastReturned = -1;

			/**
			 * Stores the modCount of the {@code ArrayListInt} at construction.
			 * If at some point this is not equal to the modCount of the
			 * {@code ArrayListInt} any more, a
			 * {@code ConcurrentModificationException} will be thrown.
			 */
			int expectedModCount = ArrayListInt.this.modCount;

			/**
			 * The parent {@code SubList} over which this {@code Iterator}
			 * iterates.
			 */
			final SubList parent;

			/**
			 * Constructs a new {@code Iterator} over the specified sublist.
			 * @param parent The {@code SubList} to iterate over.
			 */
			SubItr(final SubList parent) {
				this.parent = parent;
			}

			/**
			 * Returns {@code true} if the iteration has more elements. (In
			 * other words, returns {@code true} if {@link next()} would return
			 * an element rather than throwing an exception.)
			 * @return {@code true} if the iteration has more elements.
			 */
			@Override
			public boolean hasNext() {
				return cursor < this.parent.size;
			}

			/**
			 * Returns the next element in the iteration.
			 * @return The next element in the iteration.
			 * @throws NoSuchElementException The iteration has no more
			 * elements.
			 */
			@Override
			public Integer next() {
				if(ArrayListInt.this.modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
					throw new ConcurrentModificationException();
				}
				int i = cursor; //Store this value for concurrency. Multiple concurrent next() operations race for this read.
				if(i >= this.parent.size) {
					throw new NoSuchElementException();
				}
				int[] elementData = ArrayListInt.this.elementData; //Copy in case the elementData is just being replaced with a structural modification.
				if(i >= elementData.length) {
					throw new ConcurrentModificationException();
				}
				cursor = i + 1;
				return elementData[parent.parentOffset + (lastReturned = i)]; //Also update the last returned value.
			}

			/**
			 * Removes from the underlying collection the last element returned
			 * by this iterator. This method can be called only once per call to
			 * {@link next()}. The behaviour of an iterator is unspecified if
			 * the underlying collection is modified while the iteration is in
			 * progress in any way other than by calling this method.
			 * @throws IllegalStateException The {@link next()} method has not
			 * yet been called, or the {@code remove()} method has already been
			 * called after the last call to the {@link next()} method.
			 */
			@Override
			public void remove() {
				if(lastReturned < 0) { //Nothing returned yet. Nothing to remove.
					throw new IllegalStateException();
				}
				if(ArrayListInt.this.modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
					throw new ConcurrentModificationException();
				}
				try {
					parent.remove(lastReturned); //Try to remove the last returned element.
					cursor = lastReturned; //Set the cursor back.
					lastReturned = -1; //Only one item at a time can be removed.
					expectedModCount = ArrayListInt.this.modCount; //This changed the modCount. Update it.
				} catch(IndexOutOfBoundsException e) { //This was not due to an out of bounds index, but because the backing array changed before trying to remove this.
					throw new ConcurrentModificationException();
				}
			}
		}

		/**
		 * An optimised list iterator backed by this {@code SubList}. This
		 * iterator for lists allows the programmer to traverse the sublist in
		 * either direction, modify the list during iteration, and obtain the
		 * iterator's current position in the sublist. A {@code ListIterator}
		 * has no current element; its <i>cursor position</i> always lies
		 * between the element that would be returned by a call to
		 * {@link previous()} and the element that would be returned by a call
		 * to {@link next()}. An iterator for a list of length {@code n} has
		 * {@code n + 1} possible cursor positions.
		 * <p>Note that the {@link remove()} and {@link set(Integer)} methods
		 * are <i>not</i> defined in terms of the cursor position; they are
		 * defined to operate on the last element returned by a call to
		 * {@link next()} or {@link previous()}.</p>
		 */
		private class SubListItr implements ListIterator<Integer> {
			/**
			 * The index of the next element to return.
			 */
			int cursor;

			/**
			 * The index of the last returned element. If no element has yet
			 * been returned, this will be {@code -1}.
			 */
			int lastReturned = -1;

			/**
			 * Stores the modCount of the SubList at construction. If at some
			 * point this is not equal to the modCount of the SubList any more,
			 * a {@code ConcurrentModificationException} will be thrown.
			 */
			int expectedModCount = ArrayListInt.this.modCount;

			/**
			 * The parent {@code SubList} over which this {@code Iterator}
			 * iterates.
			 */
			final SubList parent;

			/**
			 * Creates a new list iterator that starts iterating at the
			 * beginning of the specified sublist.
			 * @param parent The {@code SubList} to iterate over.
			 */
			SubListItr(final SubList parent) {
				this.parent = parent;
			}

			/**
			 * Creates a new list iterator that starts iterating at the
			 * specified index in the specified sublist.
			 * @param parent The {@code SubList} to iterate over.
			 * @param index The index to start iterating at.
			 */
			SubListItr(final SubList parent,final int index) {
				this.parent = parent;
				cursor = index;
			}

			/**
			 * Inserts the specified element into the list. The element is
			 * inserted immediately before the element that would returned by
			 * {@link next()}, if any, and after the element that would be
			 * returned by {@link previous()}, if any. If the sublist contains
			 * no elements, the new element becomes the sole element in the
			 * sublist. The new element is inserted before the implicit cursor:
			 * a subsequent call to {@link next()} would be unaffected, and a
			 * subsequent call to {@link previous()} would return the new
			 * element. This call increases by one the value that would be
			 * returned by a call to {@link nextIndex()} or
			 * {@link previousIndex()}.
			 * @param e The element to insert.
			 */
			@Override
			public void add(final Integer e) {
				if(ArrayListInt.this.modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
					throw new ConcurrentModificationException();
				}
				try {
					int i = cursor; //Save this here in case of a concurrent call changes the cursor position.
					this.parent.add(i,e); //Actually add the element.
					cursor = i + 1;
					lastReturned = -1; //Disallow calls to remove(), set(), etc.
					expectedModCount = ArrayListInt.this.modCount;
				} catch(IndexOutOfBoundsException ex) {
					throw new ConcurrentModificationException();
				}
			}

			/**
			 * Returns {@code true} if this list iterator has more elements when
			 * traversing the list in the forward direction. In other words,
			 * returns {@code true} if {@link next()} would return an element
			 * rather than throwing an exception.
			 * @return {@code true} if the list iterator has more elements when
			 * traversing the list in the forward direction, or {@code false} if
			 * there are no more elements in that direction.
			 */
			@Override
			public boolean hasNext() {
				return cursor < this.parent.size;
			}

			/**
			 * Returns {@code true} if this list iterator has more elements when
			 * traversing the list in the reverse direction. In other words,
			 * returns {@code true} if {@link previous()} would return an
			 * element rather than throwing an exception.
			 * @return {@code true} if the list iterator has more elements when
			 * traversing the list in the reverse direction, or {@code false} if
			 * there are no more elements in that direction.
			 */
			@Override
			public boolean hasPrevious() {
				return cursor != 0;
			}

			/**
			 * Returns the next element in the sublist and advances the cursor
			 * position. This method may be called repeatedly to iterate through
			 * the sublist, or intermixed with calls to {@link previous()} to go
			 * back and forth. Note that alternating calls to {@code next()} and
			 * {@link previous()} will return the same element repeatedly.
			 * @return The next element in the sublist.
			 * @throws NoSuchElementException The iteration has no next element.
			 */
			@Override
			public Integer next() {
				if(ArrayListInt.this.modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
					throw new ConcurrentModificationException();
				}
				int i = cursor; //Store this value for concurrency. Multiple concurrent next() operations race for this read.
				if(i >= this.parent.size) {
					throw new NoSuchElementException();
				}
				int[] elementData = ArrayListInt.this.elementData; //Copy in case the elementData is just being replaced with a structural modification.
				if(i >= elementData.length) {
					throw new ConcurrentModificationException();
				}
				cursor = i + 1;
				return elementData[this.parent.parentOffset + (lastReturned = i)]; //Also update the last returned value.
			}

			/**
			 * Returns the index of the element that would be returned by a
			 * subsequent call to {@link next()}. Returns the sublist size if
			 * the list iterator is at the end of the sublist.
			 * @return The index of the element that would be returned by a
			 * subsequent call to {@link next()}, or the sublist size if the
			 * list iterator is at the end of the sublist.
			 */
			@Override
			public int nextIndex() {
				return cursor;
			}

			/**
			 * Returns the previous element in the sublist and moves the cursor
			 * position backwards. This method may be called repeatedly to
			 * iterate through the sublist backwards, or intermixed with calls
			 * to {@link next()} to go back and forth. Note that alternating
			 * calls to {@link next()} and {@code previous()} will return the
			 * same element repeatedly.
			 * @return The previous element in the sublist.
			 * @throws NoSuchElementException The iteration has no previous
			 * element.
			 */
			@Override
			public Integer previous() {
				if(ArrayListInt.this.modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
				int i = cursor - 1; //Store this value for concurrency. Multiple concurrent next() operations race for this read.
				if(i < 0) {
					throw new NoSuchElementException();
				}
				int[] elementData = ArrayListInt.this.elementData; //Copy in case the elementData is just being replaced with a structural modification.
				if(i >= elementData.length) {
					throw new ConcurrentModificationException();
				}
				cursor = i;
				return elementData[this.parent.parentOffset + (lastReturned = i)]; //Also update the last returned value.
			}

			/**
			 * Returns the index of the element that would be returned by a
			 * subsequent call to {@link previous()}. Returns {@code -1} if the
			 * list iterator is at the beginning of the sublist.
			 * @return The index of the element that would be returned by a
			 * subsequent call to {@link previous()}, or {@code -1} if the list
			 * iterator is at the beginning of the sublist.
			 */
			@Override
			public int previousIndex() {
				return cursor - 1;
			}

			/**
			 * Removes from the sublist the last element that was returned by
			 * {@link next()} or {@link previous()}. This call can only be made
			 * once per call to {@link next()} or {@link previous()}. It can be
			 * made only if {@code add(Integer)} has not been called after the
			 * last call to {@link next()} or {@link previous()}.
			 * @throws IllegalStateException Neither {@link next()} nor
			 * {@link previous()} have been called, or {@code remove()} or
			 * {@link add(Integer)} have been called after the last call to
			 * {@link next()} or {@link previous()}.
			 */
			@Override
			public void remove() {
				if(lastReturned < 0) { //Nothing returned yet. Nothing to remove.
					throw new IllegalStateException();
				}
				if(ArrayListInt.this.modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
					throw new ConcurrentModificationException();
				}
				try {
					this.parent.remove(lastReturned); //Try to remove the last returned element.
					cursor = lastReturned; //Set the cursor back.
					lastReturned = -1; //Only one item at a time can be removed.
					expectedModCount = ArrayListInt.this.modCount; //This changed the modCount. Update it.
				} catch(IndexOutOfBoundsException e) { //This was not due to an out of bound index, but because the backing array changed before trying to remove this.
					throw new ConcurrentModificationException();
				}
			}

			/**
			 * Replaces the last element returned by {@link next()} or
			 * {@link previous()} with the specified element. This call can be
			 * made only if neither {@link remove()} nor {@link add(Integer)}
			 * have been called after the last call to {@link next()} or
			 * {@link previous()}.
			 * @param e The element with which to replace the last element
			 * returned by {@link next()} or {@link previous()}.
			 * @throws IllegalStateException Neither {@link next()} nor
			 * {@link previous()} have been called, or {@link remove()} or
			 * {@link add(Integer)} have been called after the last call to
			 * {@link next()} or {@link previous()}.
			 */
			@Override
			public void set(final Integer e) {
				if(lastReturned < 0) { //Nothing returned yet. Nothing to change.
					throw new IllegalStateException();
				}
				if(ArrayListInt.this.modCount != expectedModCount) { //Something structurally modified the backing ArrayListInt.
					throw new ConcurrentModificationException();
				}
				try {
					this.parent.set(lastReturned,e); //Actually try to set the last returned element to the given value.
				} catch(IndexOutOfBoundsException ex) { //This was not due to an out of bound index, but because the backing array changed before trying to set this element.
					throw new ConcurrentModificationException();
				}
			}
		}
	}
}
