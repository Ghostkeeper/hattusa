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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class implements a Fibonacci Heap, which is a special type of heap that
 * organizes the elements in such a way that extraction can be done in amortized
 * constant time.
 * @author Ruben Dulek
 * @version 1.0
 * @param <K> The keys of the elements in the heap.
 * @param <V> The values of the elements in the heap.
 */
public class FibonacciHeap<K extends Comparable<K>,V> extends java.util.AbstractQueue<V> {
	/**
	 * The number of elements in this heap.
	 */
	private int size;

	/**
	 * The element with the lowest value.
	 */
	private Element<K,V> minimum;

	/**
	 * The entries in the Fibonacci Heap.
	 * @param <K> The keys of the elements in the heap.
	 * @param <V> The values of the elements in the heap.
	 */
	public class Element<K extends Comparable<K>,V> {
		/**
		 * The key of this element. This is used to compare two elements.
		 */
		protected K key;

		/**
		 * The value of this element.
		 */
		protected V value;

		/**
		 * The branching degree at this element.
		 */
		protected int degree;

		/**
		 * A flag that is used to indicate whether an element has already been
		 * handled by the <code>cut()</code> and <code>extractMin()</code>
		 * methods of the Fibonacci Heap.
		 */
		protected boolean flag;

		/**
		 * The next element in the circular linked list of nodes at this layer.
		 */
		protected Element<K,V> next;

		/**
		 * The previous element in the circular linked list of elements at this
		 * layer.
		 */
		protected Element<K,V> previous;

		/**
		 * The parent element in the tree, if any.
		 */
		protected Element<K,V> parent;

		/**
		 * One of the child elements in this tree, if any. The rest of the child
		 * elements can be accessed through the circular linked list that
		 * contains this child element.
		 */
		protected Element<K,V> child;

		/**
		 * Creates the element with the desired key and value.
		 * @param key The key by which to compare this element.
		 * @param value The value that this element must contain.
		 */
		@SuppressWarnings("LeakingThisInConstructor") //These are the last two commands of the constructor, and don't call any outside method.
		public Element(final K key,final V value) {
			this.key = key;
			this.value = value;
			next = this;
			previous = this;
		}

		/**
		 * Gets the key that this element is sorted by.
		 * @return The key that this element is sorted by.
		 */
		public K getKey() {
			return key;
		}

		/**
		 * Gets the value of this element.
		 * @return The value of this element.
		 */
		public V getValue() {
			return value;
		}

		/**
		 * Changes the value of this element.
		 * @param value The new value of this element.
		 */
		public void setValue(V value) {
			this.value = value;
		}

		/**
		 * Makes a deep clone of this element and all its children.
		 * @return A clone of this element, containing in its child nodes clones
		 * of the original child nodes.
		 */
		@Override
		public Element<K,V> clone() {
			final Element<K,V> result = new Element<>(key,value); //Create a simple clone of myself.
			result.next = result; //Make it a singleton for now. We don't know whether the siblings have also been cloned yet.
			result.previous = result;
			if(child != null) { //If we have siblings to clone.
				Element<K,V> element = child;
				Element<K,V> newElement = element.clone(); //Clone the first child.
				result.child = newElement; //Link that first child as my clone's child.
				newElement.parent = result; //My clone is the parent.
				do { //Iterate over all children and do a deep clone of them all.
					newElement.next = element.next.clone(); //Clone the next child.
					newElement.next.parent = result; //My clone is the parent.
					newElement.next.previous = newElement; //Link the siblings together.
					element = element.next; //And go on with the next child.
					newElement = newElement.next;
				} while(element != child);
				newElement.next = result.child; //Complete the loop of children.
				result.child.previous = newElement;
			}
			return result;
		}
	}

	/**
	 * Special iterator to iterate over elements in a Fibonacci Heap.
	 * @param <V> The values of the elements in the heap.
	 */
	public class FibonacciHeapIterator<V> implements Iterator<V> {
		/**
		 * A clone of the heap to iterate over.
		 */
		final private FibonacciHeap<K,V> heap;

		/**
		 * The original heap that was passed in the constructor, from where we
		 * must remove elements if <code>remove()</code> is called.
		 */
		final private FibonacciHeap<K,V> original;

		/**
		 * The last element that was removed from the heap by
		 * <code>next()</code>, and the element that should be removed from the
		 * original if we should call <code>remove()</code>.
		 */
		private FibonacciHeap<K,V>.Element<K,V> last;

		/**
		 * Creates a new iterator that iterates over the specified Fibonacci
		 * Heap.
		 * @param heap The Fibonacci Heap to iterate over.
		 */
		public FibonacciHeapIterator(final FibonacciHeap<K,V> heap) {
			this.heap = heap.clone();
			original = heap;
			last = null;
		}

		/**
		 * Returns <code>true</code> if the iteration has more elements. In
		 * other words, returns <code>true</code> if <code>next()</code> would
		 * return an element rather than throwing an exception.
		 * @return <code>True</code> if the iterator has more elements,
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean hasNext() {
			return !heap.isEmpty();
		}

		/**
		 * Returns the next element in the iteration. Calling this method
		 * repeatedly until the <code>hasNext()</code> method returns false will
		 * return each element in the Fibonacci Heap exactly once.
		 * @return The next element in the iteration.
		 * @throws NoSuchElementException The iteration has no more elements.
		 */
		@Override
		public V next() throws NoSuchElementException {
			last = heap.extractMin(); //Store it as the last element taken.
			return last.value;
		}

		/**
		 * Removes from the Fibonacci Heap the last element returned by the
		 * iterator. This method can be called only once per call to
		 * <code>next()</code>. The behaviour is unspecified if the heap is
		 * modified while the iteration is in progress in any way other than by
		 * calling this method.
		 * @throws IllegalStateException The <code>next()</code> method has not
		 * yet been called, or the <code>remove()</code> method has already been
		 * called after the last call to the <code>next()</code> method.
		 */
		@Override
		public void remove() throws IllegalStateException {
			if(last == null) {
				throw new IllegalStateException("Trying to remove the last element while the next() method has not yet been called, or the element has already been removed.");
			}
			try {
				original.extract(last);
				last = null;
			} catch(Exception e) {
				throw new IllegalStateException("Some elements in the heap could not be compared with eachother to update the heap for extracting the element.",e);
			}
		}
	}

	/**
	 * Creates an empty <code>FibonacciHeap</code>.
	 */
	public FibonacciHeap() {
		//Nothing to be done, but we should allow for a constructor with 0 arguments.
	}

	/**
	 * Creates a <code>FibonacciHeap</code> when the elements are already in the
	 * appropriate order and all fields are already known.
	 * @param minimum The minimum element of the heap.
	 * @param size The size of the heap.
	 */
	private FibonacciHeap(Element<K,V> minimum,int size) {
		this.minimum = minimum;
		this.size = size;
	}

	/**
	 * Gets the number of elements in the Fibonacci Heap.
	 * @return The number of elements in the Fibonacci Heap.
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns whether the heap is empty or not.
	 * @return <code>True</code> if the heap is empty, or <code>false</code>
	 * otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return minimum == null;
	}

	/**
	 * Inserts a new element into the Fibonacci Heap.
	 * @param key The key of the element to insert.
	 * @param value The value of the element to insert.
	 */
	public void insert(K key,V value) {
		final Element<K,V> element = new Element<>(key,value); //Put the key and value in an element.
		minimum = join(minimum,element); //Join it with the heap, updating the minimum as we go.
		size++; //A new element, so we're growing.
	}

	/**
	 * Gets the value of the element with the lowest key from the heap. This
	 * doesn't alter the heap in any way.
	 * @return The value of the element with the lowest key in the heap.
	 * @throws NoSuchElementException The heap is empty.
	 */
	public Element<K,V> min() throws NoSuchElementException {
		if(minimum == null) {
			throw new NoSuchElementException("No element to extract from the heap.");
		}
		return minimum;
	}

	/**
	 * Merges two heaps together into one big heap. The elements are not copied,
	 * so if you would change them internally in the original heaps, they will
	 * also change in the new heap.
	 * @param a The heap to merge with b.
	 * @param b The heap to merge with a.
	 * @return A new Fibonacci Heap with all elements from a and b.
	 */
	public FibonacciHeap<K,V> merge(FibonacciHeap<K,V> a,FibonacciHeap<K,V> b) {
		final FibonacciHeap<K,V> result = new FibonacciHeap<>(); //Create a new heap to contain the elements of the old ones.
		result.minimum = join(a.minimum,b.minimum); //Join the heaps together.
		result.size = a.size + b.size; //Add up the sizes of the original heaps.
		return result;
	}

	/**
	 * Extracts the value of the element with the lowest key from the heap. This
	 * removes it from the heap.
	 * @return The value of the element with the lowest key in the heap.
	 * @throws NoSuchElementException The heap is empty.
	 */
	public Element<K,V> extractMin() throws NoSuchElementException {
		if(minimum == null)
			throw new NoSuchElementException("No element to extract from the heap.");

		//Phase 1: Extraction
		size--; //We're going to remove one.
		final Element<K,V> min = minimum; //This is the one we're removing.

		if(minimum.next == minimum) { //If this was the last one.
			minimum = null; //Then the Heap is now empty.
		} else { //Otherwise, remove it from the circular linked list.
			minimum.next.previous = minimum.previous;
			minimum.previous.next = minimum.next;
			minimum = minimum.next;
		}

		//Phase 2: Restructuring
		if(min.child != null) { //If the removed child still had children...
			Element<K,V> element = min.child;
			do { //Iterate over the children.
				element.parent = null; //Remove their parent, it's gone now.
				element = element.next;
			} while(element != min.child);
		}

		minimum = join(minimum,min.child); //Merge the children with the root.

		if(minimum == null) //No elements in the heap any more, so we're done.
			return min;

		final int maxSize = log2(size) + 1; //There are at most log(n) different degrees (ceiled).
		final ArrayList<Element<K,V>> table = new ArrayList<>(maxSize); //This functions as a generic array. It'll always have maxSize elements.
		for(int i = 0;i < maxSize;i++) //Fill the table with null elements.
			table.add(null);
		final List<Element<K,V>> todo = new ArrayList<>(16);
		for(Element<K,V> element = minimum;todo.isEmpty() || todo.get(0) != element;element = element.next) //For every entry, until we loop around to the start.
			todo.add(element); //Add it to the list of elements to do.
		for(Element<K,V> element : todo) { //For every element...
			Element<K,V> current = element;
			while(true) { //We're merging trees, until we find a spot in the table that is empty.
				if(table.get(current.degree) == null) { //Found a spot.
					table.set(current.degree,current);
					break; //And we're done.
				}
				final Element<K,V> other = table.get(current.degree); //The spot wasn't empty.
				table.set(current.degree,null); //Take it out of the table. We're merging it with this one.
				final Element<K,V> low;
				final Element<K,V> high;

				if(other.key.compareTo(current.key) < 0) { //Which element should be removed?
					low = other;
					high = current;
				} else {
					low = current;
					high = other;
				}
				high.next.previous = high.previous; //Remove the highest one from the linked list, since we're adding it to the children of the other.
				high.previous.next = high.next;
				high.next = high; //Make a singleton out of it.
				high.previous = high;
				low.child = join(low.child,high); //Merge the two.
				high.parent = low;
				high.flag = false; //We can cut this one again if we like.
				low.degree++; //This element just got an extra child.
				current = low; //Continue with the lower element until we found a spot.
			}
			//Phase 3: Update the minimum.
			if(current.key.compareTo(minimum.key) <= 0)
				minimum = current;
		}
		return min;
	}

	/**
	 * Decreases the key of a specified element.
	 * @param element The element to decrease the key of.
	 * @param newKey The new key. If this is higher than the current key,
	 * nothing will happen.
	 */
	public void decreaseKey(Element<K,V> element,K newKey) {
		if(newKey.compareTo(element.key) < 0) { //If the new key is actually lower than the original key.
			element.key = newKey; //Update it.
			if(element.parent != null && element.key.compareTo(element.parent.key) <= 0)
				cut(element);
			if(element.key.compareTo(minimum.key) <= 0) { //The new key is lower than the minimum.
				minimum = element;
			}
		}
	}

	/**
	 * Extracts a specified element from the heap, by reducing the key to the
	 * lowest key in the heap, thus putting it at the top of the heap, and
	 * allowing us to remove it with <code>extractMin()</code>.
	 * @param element The element to remove from the heap.
	 * @return The element that was removed.
	 */
	public Element<K,V> extract(Element<K,V> element) {
		decreaseKey(element,minimum.key);
		return extractMin();
	}

	/**
	 * Insert the specified element into this Fibonacci Heap.
	 * @param key The key of the element to insert.
	 * @param value The value of the element to insert.
	 * @return Always returns <code>true</code>.
	 */
	public boolean offer(K key,V value) {
		insert(key,value);
		return true;
	}

	/**
	 * Insert the specified element into this Fibonacci Heap, if possible. The
	 * key associated with the value will be the same as the lowest key in the
	 * heap, so it will be put right in front as a stack (in stead of a queue).
	 * If you wish to put it back at the end of the queue, give it a key that is
	 * higher than all others.
	 * @param value The value to add to the heap.
	 * @return <code>True</code> if it was possible to add the element to this
	 * Fibonacci Heap, else <code>false</code>.
	 */
	@Override
	public boolean offer(V value) {
		if(!isEmpty()) { //We need to have a key to insert it with.
			return offer(minimum.key,value); //Returns true.
		}
		return false;
	}

	/**
	 * Retrieves and removes the lowest element of this Fibonacci Heap, or
	 * <code>null</code> if the heap is empty.
	 * @return The lowest element of this Fibonacci Heap, or <code>null</code>
	 * if the heap is empty.
	 */
	@Override
	public V poll() {
		if(minimum == null) { //No elements.
			return null;
		}
		return extractMin().value;
	}

	/**
	 * Retrieves and removes the lowest element of this Fibonacci Heap.
	 * @return The lowest element of this Fibonacci Heap.
	 * @throws NoSuchElementException The heap is empty.
	 */
	@Override
	public V remove() throws NoSuchElementException {
		return extractMin().value;
	}

	/**
	 * Removes a single instance of an object from this Fibonacci Heap, if such
	 * an entry exists. This implementation obtains an iterator over the
	 * Fibonacci Heap and iterates over it, testing each element for deep
	 * equality with the given object. If it is equal, it is removed by the
	 * iterator's <code>remove()</code> method. After the first element has been
	 * removed, <code>true</code> is returned; if the end of the collection is
	 * reached, <code>false</code> is returned.
	 * @param obj The value of the entry to remove from this collection.
	 * @return <code>True</code> if the remove operation caused the Fibonacci
	 * Heap to change, or equivalently if the collection did contain the
	 * provided object.
	 */
	@Override
	public boolean remove(Object obj) {
		Iterator<V> iterator = iterator();
		while(iterator.hasNext()) { //Iterate over all values.
			if(obj.equals(iterator.next())) { //Deep equality check.
				iterator.remove(); //Remove from the iterator and thus from the Fibonacci Heap.
				return true;
			}
		}
		return false; //Not found.
	}

	/**
	 * Removes all elements from the heap.
	 */
	@Override
	public void clear() {
		minimum = null;
		size = 0;
	}

	/**
	 * Retrieves, but does not remove, the lowest element of this Fibonacci
	 * Heap, returning <code>null</code> if the heap is empty.
	 * @return The lowest element of this Fibonacci Heap, or <code>null</code>
	 * if the heap is empty.
	 */
	@Override
	public V peek() {
		if(minimum == null) { //No value to return.
			return null;
		}
		return min().value;
	}

	/**
	 * Retrieves, but does not remove, the lowest element of this Fibonacci
	 * Heap.
	 * @return The lowest element of this Fibonacci Heap.
	 * @throws NoSuchElementException The heap is empty.
	 */
	@Override
	public V element() throws NoSuchElementException {
		return min().value;
	}

	/**
	 * Returns an iterator over the elements contained in this Fibonacci Heap.
	 * @return An iterator over the elements contained in this Fibonacci Heap.
	 */
	@Override
	public Iterator<V> iterator() {
		return new FibonacciHeapIterator<>(this);
	}

	/**
	 * Makes a deep clone of the Fibonacci Heap. All elements are copied. The
	 * keys and values themselves are not deep-copied, but passed by reference.
	 * @return A clone of the Fibonacci Heap.
	 */
	@Override
	public FibonacciHeap<K,V> clone() {
		Element<K,V> newMinimum = null;
		if(!isEmpty()) { //Since we're doing a deep clone, we have to clone the internal elements if it's not empty.
			newMinimum = minimum.clone();
			Element<K,V> newElement = newMinimum;
			Element<K,V> element = minimum;
			do { //Iterate over all roots to clone them.
				newElement.next = element.next.clone(); //Clone the next one.
				newElement.next.previous = newElement; //Link the siblings together.
				element = element.next; //And go on with the next sibling.
				newElement = newElement.next;
			} while(element != minimum);
			newElement.next = newMinimum; //Complete the loop of siblings.
			newMinimum.previous = newElement;
		}
		FibonacciHeap<K,V> result = new FibonacciHeap<>(newMinimum,size); //Clone the encompassing heap as well.
		return result;
	}

	/**
	 * Cuts an element away from a tree and puts it in its own tree back in the
	 * heap.
	 * @param element The element to cut away from the tree.
	 */
	private void cut(Element<K,V> element) {
		element.flag = false; //We're cutting this element. Don't cut it again.
		if(element.parent == null) //Nothing to cut. This is the root.
			return;
		if(element.next != element) { //This element has siblings.
			element.next.previous = element.previous; //Cut it out from that linked list.
			element.previous.next = element.next;
		}
		if(element.parent.child == element) { //This was the main child of the parent.
			if(element.next != element) { //If it has siblings.
				element.parent.child = element.next; //Make some sibling the main child.
			} else {
				element.parent.child = null; //No other children, so no children now.
			}
		}
		element.parent.degree--; //The parent has one less child.
		element.previous = element; //Make this element a singleton.
		element.next = element;
		minimum = join(minimum,element); //Merge it back in the heap as a separate tree.
		if(element.parent.flag) { //The parent needs to be cut as well.
			cut(element.parent);
		} else {
			element.parent.flag = true; //Well, cut it next time then, to create the fibonacci-structure we like.
		}
		element.parent = null; //It's now a root, so no parent any more.
	}

	/**
	 * Joins two trees with eachother.
	 * @param a The element at the root of a tree that should be joined with
	 * <code>b</code>.
	 * @param b The element at the root of a tree that should be joined with
	 * <code>a</code>.
	 * @return An element that is the root of a tree containing all elements of
	 * both <code>a</code> and <code>b</code>.
	 */
	private Element<K,V> join(Element<K,V> a,Element<K,V> b) {
		if(a == null && b == null) //Both trees are empty.
			return null; //The result will be empty as well.
		if(b == null) //B is empty. A is not.
			return a;
		if(a == null) //A is empty. B is not.
			return b;

		//Cross-link a with b.next and b with a.next.
		final Element<K,V> temp = a.next;
		a.next = b.next;
		a.next.previous = a;
		b.next = temp;
		temp.previous = b;

		//Now find the minimum of the two, and return that one.
		if(a.key.compareTo(b.key) < 0) {
			return a;
		}
		return b;
	}

	/**
	 * Fast calculation for the integer log base 2 with integers.
	 * @param x The integer to take the integer logarithm of.
	 * @return The integer logarithm base 2 of x.
	 */
	private static int log2(int x) {
		int log = 0;
		if((x & 0xFFFF0000) != 0) { //If x >= 65536.
			x >>>= 16;
			log = 16;
		}
		if(x >= 256) {
			x >>>= 8;
			log += 8;
		}
		if(x >= 16) {
			x >>>= 4;
			log += 4;
		}
		if(x >= 4) {
			x >>>= 2;
			log += 2;
		}
		return log + (x >>> 1);
	}
}