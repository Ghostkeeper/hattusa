/*
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
import java.lang.reflect.Array;
import java.util.*;
import net.dulek.collections.HashSet; //To differentiate between java.util.HashSet and net.dulek.collections.HashSet.

/**
 * A pairing heap implementation, which is a self-organising free-form heap that
 * performs well in practice yet supports the {@code decreaseKey} operation.
 * This heap is designed for being a good candidate for applications that
 * require extensive use of the {@code decreaseKey} operation. For that reason,
 * the heap is comprised of a tree of key-value pairs (each represented by an
 * {@link Element} instance). Because of the free structure of a pairing heap,
 * and the frequent rearrangement of groups of nodes, an object-based
 * representation was used rather than an array-based implementation with index
 * pointers. This introduces a natural overhead in Java. For that reason,
 * array-based implementations of heaps will likely be faster if the
 * {@code decreaseKey} operation is not used often.
 * <p>Proofs of the run-time complexities of the operations on a pairing heap
 * can be found in the original paper by Fredman et al titled <i>The Pairing
 * Heap: A New Form of Self-Adjusting Heap</i> (Algorithmica, 1986). Note that
 * the run-time complexity of the {@code decreaseKey} operation itself is
 * unknown at the time of this writing (August 2014), but proven to be somewhere
 * between {@code O(2^(2*sqrt(log(log(n)))))} and {@code O(log(log(n)))} (see
 * <i>Towards a Final Analysis of Pairing Heaps</i> by Seth Pettie, in
 * Foundations of Computer Science, 2005). This data structure was chosen
 * because, though results vary across the scientific literature, most
 * experiments conclude that pairing heaps are the best, or one of the best,
 * known options for a medium to large number of nodes and extensive use of
 * {@code decreaseKey}. For a thorough and recent comparison, see for instance
 * <i>A Back-to-Basis Empirical Study of Priority Queues</i> by Larkin et al
 * (ALENEX, 2014).</p>
 * <p>The elements of the heap are ordered according to the natural ordering of
 * their keys, or by a {@link Comparator} provided at queue construction time,
 * if such a {@code Comparator} is given. If no {@code Comparator} is given, the
 * keys must implement the {@link Comparable} interface. If they don't, a
 * {@link ClassCastException} may be thrown when the keys are first compared to
 * each other. The {@code null} key is not allowed.</p>
 * <p>The heap is ordered as a "min-heap", meaning that it allows easy access to
 * the smallest element in the heap (through the {@link #findMin()} operation),
 * but not to the largest element. If a "max-heap" is desired, a
 * {@code Comparator} can be provided in the constructor that reverses the
 * ordering of the original comparator or the original natural ordering. Ties
 * are broken arbitrarily.</p>
 * <p>This class provides two iterators. The default method,
 * {@link #iterator()}, iterates over the elements in the heap in an arbitrary
 * order (a depth-first traversal of the tree) and will likely <b>not</b>
 * iterate over the elements in their order. The second iterator,
 * {@link #iteratorSorted()} will return the elements in order, from small to
 * large keys. However, the ordered iterator is significantly slower. Both
 * iterators are <i>fail-fast</i>, meaning that they will throw a
 * {@link ConcurrentModificationException} if the heap is modified during their
 * iteration. To be precise, the exception is thrown if any modification is made
 * between the construction of the iterator and the most recent call to the
 * {@link Iterator#next()} method. This fail-fast behaviour prevents
 * unpredictable behaviour when faced with concurrent modifications. Rather than
 * producing nondeterministic behaviour, skipping parts of the heap or returning
 * some elements more than once, the iterator always throws an exception
 * allowing the developer to more easily prevent this behaviour.</p>
 * <p>Note that this implementation is not synchronised. Multiple threads should
 * not access a {@code PairingHeap} concurrently if any of the threads modifies
 * the heap. The heap should be locked if the danger exists that the heap could
 * be modified by another thread while the local thread is performing any
 * operation on it.</p>
 * <p><b>On the implementation.</b> A pairing heap is a general tree where the
 * only constraint is the heap invariant: The key of a parent must always be
 * less than the keys of all its children. There are no restrictions on the
 * number of children or the depth of a node. This free-form structure makes
 * many operations very efficient, since it requires little rearrangements.</p>
 * <p>This implementation is based on a C++ implementation that can be found on
 * <a href="http://www.sanfoundry.com/cpp-program-implement-pairing-heap/">
 * Sanfoundry</a>. Each element in the heap is represented by a node in the
 * tree. An element has three references to other nodes, to maintain the
 * structure efficiently:
 * <ul><li>A {@code child} reference, pointing to the leftmost child of the
 * node.</li>
 * <li>A {@code next} reference, pointing to the sibling on the right, if any.
 * If it is the rightmost sibling, this reference contains {@code null}.</li>
 * <li>A {@code previous} reference, pointing to the sibling on the left, if
 * any. If this is the leftmost sibling, this reference points to the parent
 * instead. The parent can be differentiated from a sibling by checking whether
 * {@code previous.child == this}. If this holds true, {@code this} is the
 * leftmost sibling and the {@code previous} reference points to its parent.
 * Since the parent reference is only needed for the leftmost child, this
 * dual-purpose reference reduces the overhead that comes with constructing a
 * node.</li></ul>
 * <p>A helper method {@code join} merges two heaps in {@code O(1)} time by
 * taking the root with the the larger key and attaching it as a child of the
 * root with the smaller key.</p>
 * <p>The most important operations on the heap then work as follows:
 * <ul><li>The {@link #findMin()} operation returns the root of the heap as per
 * the heap invariant. This runs in {@code O(1)} worst-case time.</li>
 * <li>The {@link #insert(K,V)} operation introduces a new entry with the
 * specified key and value and {@code join}s it with the root of the heap. This
 * takes {@code O(1)} time in the worst case.</li>
 * <li>The {@link #merge(PairingHeap)} operation {@code join}s the roots of the
 * two heaps and uses the result as the new heap. This takes {@code O(1)} time
 * in the worst case.</li>
 * <li>The {@link #decreaseKey(Element,K)} operation decreases the key of the
 * specified element if the specified new key is lower than the original key.
 * This method removes the element and its subtree from the heap, reduces the
 * key (which is always allowed, since it has no parent any more), and then
 * {@code join}s it with the root of the original heap. The operation itself is
 * in actuality {@code O(1)} in the worst case, but adds at most
 * {@code O(2^(2*sqrt(log(log(n)))))} time potential to the next call to the
 * {@link #deleteMin()} operation. Since the operation is meaningless without
 * {@code deleteMin()}, this runtime potential is commonly considered to be
 * inherent in the {@code decreaseKey} operation.</li>
 * <li>The {@link #changeKey(Element,K)} operation modifies the key of the
 * specified element. It is a more general version of {@code decreaseKey} that
 * is also able to increase the key. If the new key is lower, the
 * {@code decreaseKey} operation is called. If the new key is higher such that
 * the heap invariant would be broken for its children, the element is removed
 * and re-inserted with a higher key in {@code O(log(n))} amortized time and
 * {@code O(n)} worst-case time (see {@code delete} below and {@code insert}
 * above.</li>
 * <li>The {@link #deleteMin()} operation removes the root and {@code join}s all
 * its children together. The order in which these children are {@code join}ed
 * is chosen such that the amortized runtime complexity can be proven to be
 * {@code O(log(n))}. The worst-case runtime complexity of this operation is
 * still {@code O(n)}.</li>
 * <li>The {@link #delete(Element)} operation {@code join}s all children of the
 * specified element similar to the {@code deleteMin()} operation, and inserts
 * the resulting tree in place of the specified element. This operation also
 * requires {@code O(log(n))} amortized time and {@code O(n)} worst-case time.
 * </li>
 * <li>The {@link #containsKey(K)} and {@link #containsValue(V)} operations
 * perform Eulerian traversals through the tree to find the specified key or
 * value. By the heap invariant, the search for keys might be terminated early
 * in certain subtrees, but both operations still require {@code O(n)} time in
 * the worst case.</li>
 * <li>The {@link #clear()} operation simply lets go of the root, requiring
 * {@code O(1)} time in itself but in acutality adding an {@code O(n)} runtime
 * potential to the next garbage collection of Java.</li>
 * <li>The other methods are implemented trivially.</li></ul></p>
 * @author Ruben Dulek
 * @param <K> The type of the keys used in this heap. Unless a
 * {@link Comparator} is provided in the constructor of the {@code PairingHeap},
 * these keys <b>must</b> implement the {@link Comparable} interface.
 * @param <V> The type of the values used in this heap.
 * @version 1.0
 */
public class PairingHeap<K,V> implements Iterable<PairingHeap<K,V>.Element>,Serializable {
	/*
	 * To any testers: If you'd like to change the merging strategy for siblings
	 * with deleted parents, the following four methods need to be modified:
	 *  - changeKey, twice in (comparator == null) and twice in (else).
	 *  - delete, twice in (else).
	 *  - deleteMin, in (else if comparator == null) and in (else).
	 *  - Itr.remove, twice in (else).
	 */

	/**
	 * The comparator to compare keys with, if any. If no comparator is given at
	 * construction, the keys are compared with
	 * {@code ((Comparable)x).compareTo(y)}.
	 */
	private final Comparator<? super K> comparator;

	/**
	 * The comparator to compare elements with. This uses the comparator by key
	 * given at construction to compare two elements by their keys, if any. If
	 * no comparator is given at construction, it uses a comparator that casts
	 * the keys to {@link Comparable} and then compares them.
	 */
	private final Comparator<Object> comparatorByKey;

	/**
	 * The number of times this heap has been modified. Every modification to
	 * the heap increments this count by one. When an iterator is constructed,
	 * this value is copied. When the {@link Iterator#next()} method is then
	 * called and the {@code modCount} does not match that of the heap, it
	 * recognises that the heap was modified during the iteration and a
	 * {@link ConcurrentModificationException} is thrown. This is how the
	 * fail-fast behaviour of iterators is achieved.
	 */
	private transient int modCount;

	/**
	 * The root of the heap's tree. If the heap is empty, this will be
	 * {@code null}. If the heap is not empty, this will contain the element
	 * with the lowest key. This element also provides access to all other
	 * elements of the heap, if any, through its child reference. It has no
	 * siblings and no parent, so {@code root.previous} and {@code root.next}
	 * must always be {@code null}.
	 */
	private transient Element root;

	/**
	 * The total number of elements in the heap. Counting this whenever the size
	 * is requested would be cumbersome, therefore this size is simply kept
	 * track of. It should be increased when the {@link #insert(K,V)} method is
	 * called and decreased when the {@link #delete(Element)} or
	 * {@link #deleteMin()} method is called.
	 */
	private int size;

	/**
	 * Constructs a new empty pairing heap. The elements in the heap are ordered
	 * according to the {@link Comparable} natural ordering of their keys.
	 */
	public PairingHeap() {
		comparator = null; //No comparator given.
		comparatorByKey = new Comparator<Object>() {
			/**
			 * Compares to elements of a pairing heap by their keys. This
			 * comparator uses the natural ordering of the keys to make the
			 * comparison.
			 * @param one The element to compare with {@code two}.
			 * @param two The element to compare with {@code one}.
			 * @return A positive number if {@code one > two}, a
			 * negative number if {@code two > one}, or {@code 0}
			 * otherwise.
			 * @throws ClassCastException One of the keys does not implement the
			 * {@link Comparable} interface.
			 */
			@Override
			@SuppressWarnings("unchecked")
			public int compare(final Object one,final Object two) {
				return ((Comparable<K>)((Element)one).key).compareTo(((Element)two).key);
			}
		}; //Make a comparator that extracts the keys.
	}

	/**
	 * Constructs a new empty pairing heap. The elements in the heap are ordered
	 * according to the specified comparator of their keys.
	 * @param comparator The comparator to order the keys in this heap by. If
	 * this is {@code null}, the natural ordering of the keys will be used.
	 */
	public PairingHeap(final Comparator<? super K> comparator) {
		this.comparator = comparator;
		comparatorByKey = new Comparator<Object>() {
			/**
			 * Compares to elements of a pairing heap by their keys. The
			 * heap must have a comparator to compare the keys by. This
			 * comparator is simply a wrapper for that comparator that
			 * takes the keys of two elements to compare them by.
			 * @param one The element to compare with {@code two}.
			 * @param two The element to compare with {@code one}.
			 * @return A positive number if {@code one > two}, a
			 * negative number if {@code two > one}, or {@code 0}
			 * otherwise.
			 */
			@Override
			@SuppressWarnings("unchecked")
			public int compare(final Object one,final Object two) {
				return PairingHeap.this.comparator.compare(((Element)one).key,((Element)two).key);
			}
		}; //Make a comparator that extracts the keys.
	}

	/**
	 * Changes the key of this element to the specified new key. The key it had
	 * before the modification will be returned. If the key is lower than the
	 * previous key, the {@link #decreaseKey(Element,K)} operation will be used
	 * to decrease the key. If the key is higher than the previous key, the
	 * element will be removed with the {@link #delete(Element)} operation and
	 * re-inserted into the heap with its new key. This method takes
	 * {@code O(log(n))} time in the worst case if the key is increased, or
	 * {@code O(2^(2*sqrt(log(log(n)))))} time if the key is decreased.
	 * <p>Please beware that the specified element must be in this heap. The
	 * method does <b>not</b> check for this. If this is not the case, the
	 * specified element could become linked to this one, while the number of
	 * elements returned by {@link #size()} on both heaps would be unchanged,
	 * rendering that size false.</p>
	 * @param element The element of which the key is to be changed.
	 * @param newKey The new key of this element.
	 * @return The previous key of this element.
	 * @throws ClassCastException The heap was not constructed with a specified
	 * {@code Comparator} and the specified key or the previous key does not
	 * implement {@code Comparable}.
	 */
	@SuppressWarnings("unchecked") //Caused by casting keys to Comparable if we have no comparator.
	public K changeKey(final Element element,final K newKey) {
		final K oldKey = element.key; //Make a copy before this is overwritten, in order to return it.
		element.key = newKey;
		if(comparator == null) { //No comparator. Use comparable.
			final Comparable<K> comparableKey = (Comparable<K>)newKey; //Causes ClassCastException if the key is not comparable.
			final int comparison = comparableKey.compareTo(oldKey); //Causes ClassCastException if the previous key is not comparable.
			if(comparison < 0) { //This is a decrease of key.
				decreaseKey(element,newKey);
			} else if(comparison > 0) { //This is an increase of key.
				//Remove the element from its environment and put its children into its place.
				if(element == root) { //The root is different: To put its children into its place we'd need to make the children the new root.
					if(element.child != null) {
						final Element children = combineTwoPassComparable(element.child);
						element.child = null;
						root = joinComparable(element,children);
						return oldKey;
					}
				} else if(element.child == null) { //This element has no children, so nothing will replace it.
					if(element.previous.child == element) { //This is the leftmost child.
						if(element.next != null) { //And not the rightmost child.
							element.next.previous = element.previous;
							element.previous.child = element.next;
						} else { //It was the only child.
							element.previous.child = null;
						}
					} else { //Not the leftmost child.
						if(element.next != null) { //And not the rightmost child.
							element.next.previous = element.previous;
							element.previous.next = element.next;
						} else { //It was the rightmost child.
							element.previous.next = null;
						}
					}
				} else { //It has children and they'll need to be combined.
					final Element children = combineTwoPassComparable(element.child);
					children.previous = element.previous;
					children.next = element.next;
					if(element.previous.child == element) { //This is the leftmost child.
						element.previous.child = children;
					} else { //Not the leftmost child.
						element.previous.next = children;
					}
					if(element.next != null) { //Not the rightmost child.
						element.next.previous = children;
					}
				}
				root = joinComparable(element,root); //Insert the element back in the heap.
				root.next = null; //Put the combined element in the context of the root.
				root.previous = null;
			} //Otherwise, the keys are equal according to their natural ordering and nothing needs to be changed.
		} else { //A comparator is given. Use it.
			final int comparison = comparator.compare(newKey,oldKey);
			if(comparison < 0) { //This is a decrease of key.
				decreaseKey(element,newKey);
			} else if(comparison > 0) { //This is an increase of key.
				//Remove the element from its environment and put its children into its place.
				if(element == root) { //The root is different: To put its children into its place we'd need to make the children the new root.
					if(element.child != null) {
						final Element children = combineTwoPassComparator(element.child);
						element.child = null;
						root = joinComparator(element,children);
						return oldKey;
					}
				} else if(element.child == null) { //This element has no children, so nothing will replace it.
					if(element.previous.child == element) { //This is the leftmost child.
						if(element.next != null) { //And not the rightmost child.
							element.next.previous = element.previous;
							element.previous.child = element.next;
						} else { //It was the only child.
							element.previous.child = null;
						}
					} else { //Not the leftmost child.
						if(element.next != null) { //And not the rightmost child.
							element.next.previous = element.previous;
							element.previous.next = element.next;
						} else { //It was the rightmost child.
							element.previous.next = null;
						}
					}
				} else { //It has children and they'll need to be combined.
					final Element children = combineTwoPassComparator(element.child);
					children.previous = element.previous;
					children.next = element.next;
					if(element.previous.child == element) { //This is the leftmost child.
						element.previous.child = children;
					} else { //Not the leftmost child.
						element.previous.next = children;
					}
					if(element.next != null) { //Not the rightmost child.
						element.next.previous = children;
					}
				}
				root = joinComparator(element,root); //Insert the element back in the heap.
				root.next = null; //Put the combined element in the context of the root.
				root.previous = null;
			}
		}
		return oldKey;
	}

	/**
	 * Removes all elements from the heap. This requires {@code O(1)} time by
	 * itself, but leaves {@code O(n)} memory to free by the garbage collection.
	 */
	public void clear() {
		modCount++; //Don't forget, this is a real modification to the heap.
		size = 0;
		root = null;
	}

	/**
	 * Creates a deep copy of this pairing heap. To this end, a depth-first
	 * traversal is made and the elements are then cloned one by one. The keys
	 * and values themselves are not cloned; only their reference is. Therefore,
	 * if a key or value were changed internally, the change would be reflected
	 * in both heaps.
	 * <p>This operation takes {@code O(n)} time, since it performs a
	 * constant-time operation for every element.</p>
	 * @return A copy of this heap.
	 * @throws CloneNotSupportedException If any subclass of {@code PairingHeap}
	 * doesn't support cloning, it might throw this exception, but this
	 * implementation does support cloning.
	 */
	@Override
	public PairingHeap<K,V> clone() throws CloneNotSupportedException {
		final PairingHeap<K,V> clone = new PairingHeap<>(comparator); //The resulting clone.

		//Process the root separately since we need to store it in the root-field of the clone.
		Element current = root; //The node we're currently processing.
		if(current == null) { //Heap is empty.
			return clone; //Then we're already done.
		}
		Element currentClone = current.clone(); //The clone of the node we're currently processing.
		clone.root = currentClone;

		//Perform a depth-first search.
		final Deque<Element> todo = new ArrayDeque<>(net.dulek.math.Math.log2(size) << 1); //Backtracking trail with the unexplored branches. Has two elements per step: both the original and the clone.
		while(true) { //Until we've explored all unexplored branches.
			if(current.child != null) { //It has a child.
				if(current.next != null) { //And it has a right sibling. Two branches then, so put one branch on the stack.
					final Element nextClone = current.next.clone(); //Clone it.
					currentClone.next = nextClone; //Link it.
					nextClone.previous = currentClone;
					todo.push(current.next); //Stack it.
					todo.push(nextClone);
				}
				final Element childClone = current.child.clone(); //Clone it.
				currentClone.child = childClone; //Link it.
				childClone.previous = currentClone;
				current = current.child; //Move to it.
				currentClone = childClone;
				continue;
			}
			//It has no child.
			if(current.next != null) { //But it has a right sibling.
				final Element nextClone = current.next.clone(); //Clone it.
				currentClone.next = nextClone; //Link it.
				nextClone.previous = currentClone;
				current = current.next; //Move to it.
				currentClone = nextClone;
				continue;
			}
			//It has neither child nor right sibling. Backtrack to a previously unexplored branch then.
			if(todo.isEmpty()) { //But there are no more unexplored branches.
				break; //Done then.
			}
			currentClone = todo.pop(); //Reverse order, mind you.
			current = todo.pop();
		}
		clone.size = size; //Update the size too.
		return clone;
	}

	/**
	 * Returns {@code true} if the specified key is the key of at least one of
	 * the elements in this heap, or {@code false} if it isn't.
	 * <p>This method takes {@code O(n)} time, as it performs a depth-first
	 * search through the tree.</p>
	 * @param key The key to search for in the heap.
	 * @return {@code true} if the key is in this heap, or {@code false} if it
	 * isn't.
	 * @throws ClassCastException The specified key, or any key in the heap, was
	 * not an implementation of Comparable, but the heap has no Comparator.
	 */
	public boolean containsKey(final K key) {
		if(key == null || root == null) { //This heap can't contain null-keys. If the heap is empty it can't contain any key
			return false;
		}
		final Deque<Element> todo = new ArrayDeque<>(net.dulek.math.Math.log2(size)); //Stack of unexplored paths.
		Element current = root;
		if(comparator == null) {
			@SuppressWarnings("unchecked") //Caused by casting keys to Comparable if they aren't Comparable.
			final Comparable<K> comparableKey = (Comparable<K>)key; //Throws ClassCastException if the specified key is not comparable.
			while(true) { //Until we've explored all paths.
				if(current.key == key) { //Found it!
					return true;
				}
				if(current.child != null && comparableKey.compareTo(current.child.key) > 0) { //It has a child, and the key might be in its subtree.
					if(current.next != null) { //But it also has a right sibling.
						todo.push(current.next); //Store the sibling to do later.
					}
					current = current.child;
				} else if(current.next != null) { //No child, but a right sibling.
					current = current.next;
				} else if(!todo.isEmpty()) { //No child, no right sibling, but an unexplored branch is still left.
					current = todo.pop(); //Take the next unexplored branch.
				} else {
					break; //Nothing left to explore.
				}
			}
		} else {
			while(true) { //Until we've explored all paths.
				if(current.key == key) { //Found it!
					return true;
				}
				if(current.child != null && comparator.compare(key,current.child.key) > 0) { //It has a child, and the key might be in its subtree.
					if(current.next != null) { //But it also has a right sibling.
						todo.push(current.next); //Store the sibling to do later.
					}
					current = current.child;
				} else if(current.next != null) { //No child, but a right sibling.
					current = current.next;
				} else if(!todo.isEmpty()) { //No child, no right sibling, but an unexplored branch is still left.
					current = todo.pop(); //Take the next unexplored branch.
				} else {
					break; //Nothing left to explore.
				}
			}
		}
		return false; //Searched through the tree, but found nothing.
	}

	/**
	 * Returns {@code true} if the specified value is the value of at least one
	 * of the elements in this heap, or {@code false} if it isn't.
	 * <p>This method takes {@code O(n)} time, as it performs a depth-first
	 * search through the tree.</p>
	 * @param value The value to search for in the heap.
	 * @return {@code true} if the value is in this heap, or {@code false} if it
	 * isn't.
	 */
	public boolean containsValue(final V value) {
		final Deque<Element> todo = new ArrayDeque<>(net.dulek.math.Math.log2(size)); //Stack of unexplored branches of the heap.
		Element current = root;
		if(current == null) { //Heap is empty. It can't contain the value then.
			return false;
		}
		//Perform a depth-first search.
		while(true) { //Until we've explored all branches.
			if(current.value == value) { //Found it!
				return true;
			}
			if(current.child != null) { //It has a child.
				if(current.next != null) { //But it also has a right sibling.
					todo.push(current.next); //Store the sibling to do later.
				}
				current = current.child;
			} else if(current.next != null) { //No child, but a right sibling.
				current = current.next;
			} else if(!todo.isEmpty()) { //No child, no right sibling, but an unexplored branch is still left.
				current = todo.pop(); //Take the next unexplored branch.
			} else {
				break; //Nothing left to explore.
			}
		}
		return false; //Searched through the tree, but found nothing.
	}

	/**
	 * Decreases the key of the specified element to the specified new key. The
	 * key it has before the change will be returned.
	 * <p>If the specified key is higher than the current key of the element,
	 * this method will throw an {@link IllegalArgumentException}. In order to
	 * increase the key of an element, use {@link Element#setKey(K)} instead.
	 * This method is separate because in this heap, decreasing keys can be done
	 * more efficiently than increasing them.</p>
	 * <p>This method takes {@code O(2^(2*sqrt(log(log(n)))))} time. The method
	 * takes {@code O(1)} time in itself, but since it causes the root to have
	 * more children, the next call to {@link #deleteMin()} will take longer.
	 * Since this method is useless without calling {@code deleteMin()}, the
	 * time complexity of that effect is added to this method. The time
	 * complexity of that effect is subject to research, so it may actually be
	 * lower. Note that the specified element is unlinked even if the reduction
	 * of key wouldn't violate the heap invariant (since searching for the
	 * parent would increase the runtime complexity more).</p>
	 * @param element The element to reduce the key of.
	 * @param newKey The new key for {@code element}.
	 * @return The key of the specified element before the change.
	 * @see Element#setKey(K)
	 * @throws ClassCastException The specified key or the original key is not
	 * comparable and no comparator is available.
	 * @throws IllegalArgumentException The specified key is larger than the
	 * original key of the specified element.
	 * @throws NullPointerException The specified element or the specified key
	 * was {@code null}.
	 */
	@SuppressWarnings("unchecked") //Caused by casting keys to Comparable if no comparator is available.
	public K decreaseKey(final Element element,final K newKey) {
		final K oldKey = element.key; //Store the old key so we can return it afterwards.
		if(element == root) { //Of the root, we can always decrease the key and won't need to detach or re-attach.
			element.key = newKey; //Update the key.
			return oldKey;
		}
		if(comparator == null) {
			if(((Comparable<K>)newKey).compareTo(element.key) > 0) { //New key is bigger. Throws ClassCastException if either key is not comparable.
				throw new IllegalArgumentException("The new key for the element is larger than the original key.");
			}
			element.key = newKey; //Update the key.
			modCount++; //We'll move stuff around now.
			//Remove the element from its environment.
			if(element.next != null) {
				element.next.previous = element.previous;
			}
			if(element.previous != null) {
				if(element.previous.child == element) { //This is the leftmost sibling, so we're talking to a parent now.
					element.previous.child = element.next;
				} else { //Not the leftmost sibling, so we're talking to another sibling now.
					element.previous.next = element.next;
				}
			}
			element.next = null;
			element.previous = null;
			root = joinComparable(element,root);
		} else {
			if(comparator.compare(newKey,element.key) > 0) { //New key is bigger.
				throw new IllegalArgumentException("The new key for the element is larger than the original key.");
			}
			element.key = newKey; //Update the key.
			modCount++; //We'll move stuff around now.
			//Remove the element from its environment.
			if(element.next != null) {
				element.next.previous = element.previous;
			}
			if(element.previous != null) {
				if(element.previous.child == element) { //This is the leftmost sibling, so we're talking to a parent now.
					element.previous.child = element.next;
				} else { //Not the leftmost sibling, so we're talking to another sibling now.
					element.previous.next = element.next;
				}
			}
			element.next = null;
			element.previous = null;
			root = joinComparator(element,root);
		}
		return oldKey;
	}

	/**
	 * Removes the specifies element from the heap.
	 * <p>This operation takes {@code O(log(n))} amortized time, and
	 * {@code O(n)} time in the worst case. This is because the element has at
	 * most {@code O(n)} children (as is the case for the root if there have
	 * only been insertions), and every time this method is called, these
	 * children are rearranged in such a way as to reduce this amount, making
	 * the next call more efficient. For an actual complexity analysis, please
	 * refer to the original paper introducing pairing heaps as cited in the
	 * documentation of the {@code PairingHeap} class itself.</p>
	 * @param element The element to remove from the heap.
	 * @throws ClassCastException No comparator is available, but some keys in
	 * the heap don't implement the {@code Comparable} interface.
	 * @throws NullPointerException The specified element is {@code null}.
	 */
	public void delete(final Element element) {
		if(element == null) {
			throw new NullPointerException("The element to delete is null.");
		}
		if(element == root) { //Root needs to be replaced and stuff. We already have a method for this.
			deleteMin();
			return;
		}
		modCount++; //From here we'll modify the heap.
		if(element.child == null) { //This element has no children, so nothing will replace it.
			if(element.previous.child == element) { //This is the leftmost child.
				if(element.next != null) { //And not the rightmost child.
					element.next.previous = element.previous;
					element.previous.child = element.next;
				} else { //It was the only child.
					element.previous.child = null;
				}
			} else { //Not the leftmost child.
				if(element.next != null) { //And not the rightmost child.
					element.next.previous = element.previous;
					element.previous.next = element.next;
				} else { //It was the rightmost child.
					element.previous.next = null;
				}
			}
		} else { //It has children, and they'll need to be combined.
			final Element children;
			if(comparator == null) { //Merge children using Comparable.
				children = combineTwoPassComparable(element.child);
			} else { //Merge children using Comparator.
				children = combineTwoPassComparator(element.child);
			}
			children.previous = element.previous;
			children.next = element.next;
			if(element.previous.child == element) { //This is the leftmost child.
				element.previous.child = children;
			} else { //Not the leftmost child.
				element.previous.next = children;
			}
			if(element.next != null) { //Not the rightmost child.
				element.next.previous = children;
			}
		}
		size--;
	}

	/**
	 * Removes the element with the lowest key from the heap, if any, and
	 * returns it. If this heap has no elements, a
	 * {@link NoSuchElementException} will be thrown.
	 * <p>This operation takes {@code O(log(n))} amortized time, and
	 * {@code O(n)} time in the worst case. This is because the root has at most
	 * {@code O(n)} children (as is the case if there have only been
	 * insertions), and every time this method is called, these children are
	 * rearranged in such a way as to reduce this amount, making the next call
	 * more efficient. For an actual complexity analysis, please refer to the
	 * original paper introducing pairing heaps as cited in the documentation of
	 * the {@code PairingHeap} class itself.</p>
	 * @return The element with the lowest key, before it was removed.
	 * @throws ClassCastException No comparator is available, but some keys in
	 * the heap don't implement the {@code Comparable} interface.
	 * @throws NoSuchElementException The heap is empty.
	 */
	public Element deleteMin() {
		if(root == null) { //Heap is empty.
			throw new NoSuchElementException("The heap is empty.");
		}
		final Element result = root; //The minimum, the element we're deleting.
		modCount++; //From here we'll modify the heap.
		if(root.child == null) { //Only the root exists, so there's nothing to merge.
			root = null;
			size--;
			return result;
		} else if(comparator == null) { //We're merging. Use the Comparable version.
			root = combineTwoPassComparable(root.child);
		} else { //We're merging. Use the Comparator version.
			root = combineTwoPassComparator(root.child);
		}
		root.previous = null;
		root.next = null;
		size--;
		return result;
	}

	/**
	 * Returns a set of all entries in this heap. The entries are not copied, so
	 * changing them will change the heap itself as well.
	 * <p>Constructing the set takes {@code O(n)} expected time. The resulting
	 * set is actually an {@link IdentityHashSet}, and {@code O(n)} elements
	 * will be inserted into that.</p>
	 * @return A set of all entries in this heap.
	 * @see #keySet()
	 * @see #toArray()
	 * @see #values()
	 */
	public Set<Element> entrySet() {
		final Set<Element> result = new IdentityHashSet<>(size); //The resulting set of elements.
		final Deque<Element> todo = new ArrayDeque<>(net.dulek.math.Math.log2(size)); //Stack of unexplored branches of the heap.
		Element current = root;
		if(current == null) { //Heap is empty.
			return result; //We're already done then.
		}
		//Perform a depth-first search.
		while(true) { //Until we've explored all branches.
			result.add(current);
			if(current.child != null) { //It has a child.
				if(current.next != null) { //But it also has a right sibling.
					todo.push(current.next); //Store the sibling to do later.
				}
				current = current.child;
			} else if(current.next != null) { //No child, but a right sibling.
				current = current.next;
			} else if(!todo.isEmpty()) { //No child, no right sibling, but an unexplored branch is still left.
				current = todo.pop(); //Take the next unexplored branch.
			} else {
				break; //Nothing left to explore.
			}
		}
		return result;
	}

	/**
	 * Returns the element with the lowest key. If the heap is empty,
	 * {@code null} will be returned.
	 * <p>This method takes {@code O(1)} time, as the element with the lowest
	 * key is always at the root of the heap.</p>
	 * @return The element with the lowest key, or {@code null} if the heap has
	 * no elements at all.
	 */
	public Element findMin() {
		return root;
	}

	/**
	 * Adds a new element to the heap with the specified key and value. The new
	 * element will be returned.
	 * <p>This method takes {@code O(1)} time, since the new entry will be
	 * joined with the root.</p>
	 * @param key The key of the new element.
	 * @param value The value of the new element.
	 * @return The newly created element.
	 * @throws ClassCastException The specified key doesn't implement
	 * {@code Comparable} and no {@code Comparator} is available.
	 * @throws NullPointerException The specified key is {@code null}.
	 */
	public Element insert(final K key,final V value) {
		final Element element = new Element(key,value); //The new element.
		modCount++; //We're making a modification.
		if(comparator == null) {
			root = joinComparable(element,root);
		} else {
			root = joinComparator(element,root);
		}
		size++;
		return element;
	}

	/**
	 * Returns whether the heap is empty or not.
	 * <p>This method takes {@code O(1)} time, as it only needs to check the
	 * existence of a root element.</p>
	 * @return {@code true} if the heap is empty, or {@code false} if it isn't.
	 */
	public boolean isEmpty() {
		return root == null;
	}

	/**
	 * Returns an iterator over the elements in this heap. The iterator does not
	 * return the elements of the heap in order of their keys. If a sorted order
	 * is required, please refer to the {@link #iteratorSorted()} method.
	 * <p>In the face of concurrent modification of the heap, this iterator is
	 * <i>fail-fast</i>, meaning that if any modification is made to the heap,
	 * the iterator will immediately throw a
	 * {@link ConcurrentModificationException} on the next call to
	 * {@link #next()}. This fail-fast behaviour prevents nondeterministic
	 * results if changes are made between the call of this method and the last
	 * call to {@link Iterator#next()}. Rather than being unpredictable, it will
	 * always throw an exception. This fail-fast behaviour is on a best-effort
	 * basis, and makes no guarantees that the iteration will be correct when
	 * the heap is modified during the iteration.</p>
	 * <p>The iterator supports the {@link Iterator#remove()} operation. The
	 * {@link Iterator#hasNext()} operation performs in {@code O(1)} time. The
	 * {@link Iterator#next()} operation performs in {@code O(1)} amortized time
	 * but {@code O(n)} worst-case time. The {@link Iterator#remove()} operation
	 * performs in {@code O(log(n))} amortized time, as it calls the heap to
	 * remove the element.</p>
	 * @return An iterator over the elements in this heap.
	 */
	@Override
	public Iterator<Element> iterator() {
		return new Itr();
	}

	/**
	 * Returns an iterator that enumerates the elements in this heap in order of
	 * increasing keys.
	 * <p>In the face of concurrent modification of the heap, this iterator is
	 * <i>fail-fast</i>, meaning that if any modification is made to the heap,
	 * the iterator will immediately throw a
	 * {@link ConcurrentModificationException} on the next call to
	 * {@link #next()}. This fail-fast behaviour prevents nondeterministic
	 * results if changes are made between the call of this method and the last
	 * call to {@link Iterator#next()}. Rather than being unpredictable, it will
	 * always throw an exception. This fail-fast behaviour is on a best-effort
	 * basis, and makes no guarantees that the iteration will be correct when
	 * the heap is modified during the iteration.</p>
	 * <p>The iterator supports the {@link Iterator#remove()} operation. The
	 * {@link Iterator#hasNext()} and {@link Iterator#next()} operations both
	 * perform in {@code O(1)} time. The {@link Iterator#remove()} operation
	 * performs in {@code O(log(n))} amortized time, as it calls the heap to
	 * remove the element. The constructing of the iterator itself actually
	 * sorts the elements, and takes {@code O(n*log(n))} time.</p>
	 * @return An iterator over the elements in this heap in sorted order.
	 */
	public Iterator<Element> iteratorSorted() {
		return new ItrSorted();
	}

	/**
	 * Returns a set of all keys in this heap. Note that if multiple elements
	 * use the same key, the set will only contain one of them. To get all keys
	 * including duplicates, the heap should be traversed using the
	 * {@link #iterator()}, gathering all keys from those elements.
	 * <p>Constructing the set takes {@code O(n)} expected time. The resulting
	 * set is actually a {@link HashSet}, and {@code O(n)} keys will be inserted
	 * into that.</p>
	 * @return A set of all keys in this heap.
	 * @see #entrySet()
	 * @see #values()
	 */
	public Set<K> keySet() {
		final Set<K> result = new HashSet<>(size); //The resulting set of elements.
		final Deque<Element> todo = new ArrayDeque<>(net.dulek.math.Math.log2(size)); //Stack of unexplored branches of the heap.
		Element current = root;
		if(current == null) { //The heap was empty.
			return result; //Then we're already done.
		}
		//Perform a depth-first search.
		while(true) { //Until we've explored all branches.
			result.add(current.key); //The hash set filters out the duplicate keys.
			if(current.child != null) { //It has a child.
				if(current.next != null) { //But it also has a right sibling.
					todo.push(current.next); //Store the sibling to do later.
				}
				current = current.child;
			} else if(current.next != null) { //No child, but a right sibling.
				current = current.next;
			} else if(!todo.isEmpty()) { //No child, no right sibling, but an unexplored branch is still left.
				current = todo.pop(); //Take the next unexplored branch.
			} else {
				break; //Nothing left to explore.
			}
		}
		return result;
	}

	/**
	 * Merges this heap with another {@code PairingHeap} instance. The result
	 * will be stored in this heap. If the result should be placed in a new
	 * {@code PairingHeap} instance, create a new instance manually and merge
	 * both heaps into that. The specified heap will be emptied, in order to
	 * prevent modifications to that heap leaking into this heap.
	 * <p>This method takes {@code O(1)} time. The two roots are simply merged
	 * and the specified heap will be cleared.</p>
	 * @param other The heap to merge into this one.
	 * @throws IllegalArgumentException Trying to merge this heap with itself.
	 * @throws NullPointerException The specified heap is {@code null}.
	 */
	public void merge(final PairingHeap<K,V> other) {
		if(other == this) { //We're merging with ourselves?
			throw new IllegalArgumentException("Merging PairingHeap with itself.");
		}
		modCount++; //We'll make some big changes.
		if(other.root == null) { //The specified heap may be empty.
			return; //Then we won't need to do anything!
		}
		if(comparator == null) {
			root = joinComparable(other.root,root); //Join the two roots.
		} else {
			root = joinComparator(other.root,root); //Join the two roots.
		}
		size += other.size; //The two are now merged.
		other.clear(); //Clear the other heap, to prevent unwarranted modifications.
	}

	/**
	 * Returns the number of elements in this heap.
	 * <p>This method takes {@code O(1)} time, since this is stored in a cached
	 * field.</p>
	 * @return The number of elements in this heap.
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns an array of all elements in this heap. The returned array will be
	 * of type {@code Object[]} due to the prohibition on generic array
	 * creation. Therefore, the elements would have to be cast back to
	 * {@code PairingHeap<K,V>.Element} individually.
	 * <p>Constructing the array takes {@code O(n)} time, as the result of the
	 * Eulerian tour it takes through the heap to collect all elements.</p>
	 * @return An array of all elements in this heap.
	 * @see #entrySet()
	 */
	public Object[] toArray() {
		final Object[] result = new Object[size];
		if(size == 0) { //The heap was empty.
			return result; //Then we're already done.
		}
		int index = 0; //The index to put the next element at.
		final Deque<Element> todo = new ArrayDeque<>(net.dulek.math.Math.log2(size)); //Stack of unexplored branches of the heap.
		Element current = root;
		//Perform a depth-first search.
		while(true) { //Until we've explored all branches.
			result[index++] = current;
			if(current.child != null) { //It has a child.
				if(current.next != null) { //But it also has a right sibling.
					todo.push(current.next); //Store the sibling to do later.
				}
				current = current.child;
			} else if(current.next != null) { //No child, but a right sibling.
				current = current.next;
			} else if(!todo.isEmpty()) { //No child, no right sibling, but an unexplored branch is still left.
				current = todo.pop(); //Take the next unexplored branch.
			} else {
				break; //Nothing left to explore.
			}
		}
		return result;
	}

	/**
	 * Returns a set of all values in this heap. Note that if multiple elements
	 * have the same value, the set will only contain one of them. To get all
	 * values including duplicates, the heap should be traversed using the
	 * {@link #iterator()}, gathering all values from those elements.
	 * <p>Constructing the set takes {@code O(n)} expected time. The resulting
	 * set is actually a {@link HashSet}, and {@code O(n)} values will be
	 * inserted into that.</p>
	 * @return A set of all values in this heap.
	 * @see #entrySet()
	 * @see #keySet()
	 */
	public Set<V> values() {
		final Set<V> result = new HashSet<>(size); //The resulting set of elements.
		final Deque<Element> todo = new ArrayDeque<>(net.dulek.math.Math.log2(size)); //Stack of unexplored branches of the heap.
		Element current = root;
		if(current == null) { //Heap is empty.
			return result; //Then we're already done.
		}
		//Perform a depth-first search.
		while(true) { //Until we've explored all branches.
			result.add(current.value);
			if(current.child != null) { //It has a child.
				if(current.next != null) { //But it also has a right sibling.
					todo.push(current.next); //Store the sibling to do later.
				}
				current = current.child;
			} else if(current.next != null) { //No child, but a right sibling.
				current = current.next;
			} else if(!todo.isEmpty()) { //No child, no right sibling, but an unexplored branch is still left.
				current = todo.pop(); //Take the next unexplored branch.
			} else {
				break; //Nothing left to explore.
			}
		}
		return result;
	}

	/**
	 * An element in this {@code PairingHeap}. The element will be enclosed
	 * inside the {@code PairingHeap} instance (it is not static), so it will
	 * inherit the parent heap's key and value types and cause the heap instance
	 * to remain in memory as long as the element is (which might be an issue if
	 * the {@link #merge(PairingHeap)} operation is used often).
	 * <p>An element contains a {@code key} and a {@code value} field, to store
	 * the element's key and value. It contains three fields to maintain the
	 * structure of the heap:
	 * <ul><li>The {@code child} field references the leftmost child of the
	 * element, or {@code null} if it has no child.</li>
	 * <li>The {@code next} field references the sibling to the right of the
	 * element, or {@code null} if it is the rightmost sibling.</li>
	 * <li>The {@code previous} field references the sibling to the left of the
	 * element, or the parent element if it is the leftmost sibling.</li></ul>
	 * </p>
	 * <p>The element's key and value can be retrieved with respectively the
	 * {@link #getKey()} and {@link #getValue()} methods. The value can also be
	 * changed with the {@link #setValue(V)} method. The key cannot be changed
	 * from within the element, since it doesn't keep track of what heap it is
	 * in. If the key needs to be changed, use the {@link #changeKey(Element,K)}
	 * method of the heap the element is in. If multiple values are required for
	 * every key, it is recommended that you extend this class rather than
	 * putting tuple-like objects or constant-size arrays in the value field.
	 * </p>
	 * <p>The elements are intended to be manipulated through the encompassing
	 * {@code PairingHeap} instantiation, so please refer to {@link PairingHeap}
	 * to delete, create and insert elements.</p>
	 */
	public class Element {
		/**
		 * The key of this element. This key specifies the order in which the
		 * elements will appear (as seen from the outside) in the heap. Elements
		 * with a low key can be obtained quickly, via the {@link #findMin()}
		 * and {@link #deleteMin()} operations. The heap is ordered in order to
		 * make this possible.
		 */
		private K key;

		/**
		 * The value of this element. This value is not used in the heap, but it
		 * is stored alongside the keys in order to make the
		 * {@link #decreaseKey} operation be more meaningful. That operation
		 * does not modify the value, so the identity of the element is not
		 * completely changed.
		 */
		protected V value;

		/**
		 * The leftmost child of this element, if any. The leftmost child will
		 * be the child that is most recently added to the list of children of
		 * the element. The other children can be accessed by calling the
		 * {@code previous} and {@code next} attributes of the children. If this
		 * element has no child, this will be {@code null}.
		 */
		private Element child;

		/**
		 * The sibling to the right of this element, if any. This sibling will
		 * be the sibling that was added before this element to the parent
		 * element's children. If this is the rightmost sibling, this will be
		 * {@code null}.
		 */
		private Element next;

		/**
		 * The sibling to the left of this element, if any, or otherwise the
		 * parent of this element. This sibling will be the sibling that was
		 * added after this element to the parent element's children. If this is
		 * the leftmost sibling, this will contain the parent element instead,
		 * if any. If this is the root, this will be {@code null}, since that
		 * element is always the leftmost sibling and has no parent.
		 */
		private Element previous;

		/**
		 * Constructs a new Pairing Heap Element instance. The specified key and
		 * value are given to the element.
		 * @param key The key of this element.
		 * @param value The value of this element.
		 */
		private Element(final K key,final V value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * Creates a copy of this element. A new element is constructed with the
		 * same key and value as this element. The {@code child}, {@code next}
		 * and {@code previous} fields are not copied.
		 * @return A copy of this element.
		 * @throws CloneNotSupportedException If any subclass of {@code Element}
		 * doesn't support cloning for some reason, it may throw this exception.
		 * This implementation does support cloning however.
		 */
		@Override
		public Element clone() throws CloneNotSupportedException {
			return new Element(key,value);
		}

		/**
		 * Returns the key of this element.
		 * @return The key of this element.
		 */
		public K getKey() {
			return key;
		}

		/**
		 * Returns the value associated with this element.
		 * @return The value of this element.
		 */
		public V getValue() {
			return value;
		}

		/**
		 * Changes the value associated with this element to the specified new
		 * value. The value it had before the modification will be returned.
		 * @param value The new value for this element.
		 * @return The previous value of this element.
		 */
		public V setValue(final V value) {
			final V oldValue = this.value; //Make a copy before this is overwritten, in order to return it.
			this.value = value;
			return oldValue;
		}

		public String toString() {
			return key.toString();
		}
	}

	/**
	 * An iterator for a pairing heap. This iterator will return the elements of
	 * the heap in depth-first order. That order is not guaranteed to match any
	 * order of the keys. If a sorted order is required, please refer to the
	 * {@link ItrSorted} class inside {@code PairingHeap}.
	 * <p>In the face of concurrent modification of the encompassing heap, this
	 * iterator is <i>fail-fast</i>, meaning that if any modification to the
	 * heap is made, the iterator will immediately throw a
	 * {@link ConcurrentModificationException} on the next call to
	 * {@link #next()}. This fail-fast behaviour prevents nondeterministic
	 * results if changes are made between the constructing of the iterator and
	 * the last call to {@code next()}. Rather than being unpredictable, it will
	 * always throw an exception. This fail-fast behaviour is on a best-effort
	 * basis, and makes no guarantees that the iteration will be correct when
	 * the heap is modified during the iteration.</p>
	 * <p>The iterator supports the {@link #remove()} operation. The
	 * {@link #hasNext()} operation performs in {@code O(1)} time. The
	 * {@link #next()} operation performs in {@code O(1)} amortized time but
	 * {@code O(n)} worst-case time. The {@link #remove()} operation performs in
	 * {@code O(log(n))} amortized time, as it calls the heap to remove the
	 * element.</p>
	 */
	private class Itr implements Iterator<Element> {
		/**
		 * The most recent element returned with a call to {@link #next()}. This
		 * is the element that has to be removed if {@link #remove()} would be
		 * called.
		 */
		private Element current;

		/**
		 * The {@code modCount} of the {@code PairingHeap} instance at the time
		 * of this iterator's creation or its most recent call to
		 * {@link #remove()}. If this is different from the current
		 * {@code modCount}, that means that the heap was modified such that the
		 * traversal order of this iterator was changed, and we should throw a
		 * {@link ConcurrentModificationException} if the user tries to continue
		 * iterating through the heap.
		 */
		private int originalModCount;

		/**
		 * A stack of some elements that have not yet been iterated over. An
		 * element will be added at every branch point, when the current element
		 * has both a child and a right sibling. The right sibling will then be
		 * added. When an element has neither a child nor a right sibling, the
		 * most recent element will be popped from the stack and the iteration
		 * continues from there.
		 */
		private final Deque<Element> todo;

		/**
		 * Constructs a new iterator for the parent {@code PairingHeap}
		 * instance.
		 * <p>This iterator reserves {@code log(n)} memory for a call stack.
		 * This causes the constructor to take {@code O(log(n))} time.</p>
		 */
		Itr() {
			originalModCount = modCount; //Make a copy of modCount.
			todo = new ArrayDeque<>(net.dulek.math.Math.log2(size));
			todo.push(root); //Start the iteration with the root.
		}

		/**
		 * Returns {@code true} if the iteration has more elements. In other
		 * words, returns {@code true} if {@link #next()} would return an
		 * element rather than throwing an exception.
		 * @return {@code true} if the iteration has more elements, or
		 * {@code false} otherwise.
		 */
		@Override
		public boolean hasNext() {
			return !(todo.isEmpty() && (current == null || (current.child == null && current.next == null))); //Done if we're at a leaf, and there's no unexplored branch left (or if the entire heap is empty).
			//return !((childDone && current == root) || root == null); //(!childDone || current != root) && root != null; //We're done if we went back at the root and would go up to its parent next.
		}

		/**
		 * Returns the next element in the iteration. If there are no more
		 * elements, a {@link NoSuchElementException} will be thrown.
		 * <p>This operation takes {@code O(1)} amortized time and {@code O(n)}
		 * time in the worst case. That is because to find the next element, the
		 * iteration might have to go all the way up from a leaf until it finds
		 * a node with an unprocessed child (and the tree is {@code O(n)} high
		 * in the worst case). Every node is visited at most twice, leading to a
		 * {@code O(1)} amortized time complexity.</p>
		 * @return The next element in the iteration.
		 * @throws ConcurrentModificationException The pairing heap was modified
		 * since this iterator was constructed.
		 * @throws NoSuchElementException The iteration has no more elements.
		 */
		@Override
		public Element next() {
			if(originalModCount != modCount) { //The heap was modified!
				throw new ConcurrentModificationException("The pairing heap was modified during iteration.");
			}
			if(current == null) { //The first iteration, or after a remove operation.
				if(todo.isEmpty()) { //Nothing left to do though.
					throw new NoSuchElementException("The iteration has no more elements.");
				}
				current = todo.pop();
				return root;
			}
			if(current.child != null) { //Since we're going depth-first, first go to the child.
				if(current.next != null) { //There's not only a child, but also a sibling. It's a split point.
					todo.push(current.next); //Store that one for later work.
				}
				current = current.child;
				return current;
			}
			if(current.next != null) { //No child, then the sibling is our second-best choice.
				current = current.next;
				return current;
			}
			//No child, no sibling. See if there are any unexplored branches left.
			if(todo.isEmpty()) { //No unexplored branches either.
				throw new NoSuchElementException("The iteration has no more elements.");
			}
			current = todo.pop(); //Take one of these unexplored branches.
			return current;
		}

		/**
		 * Removes from the heap the last element returned by this iterator.
		 * This method can be called only once per call to {@link #next()}. The
		 * iterator will not throw a {@link ConcurrentModificationException} as
		 * result of the modification caused by this method.
		 * <p>This method takes {@code O(log(n))} amortized time to execute,
		 * since it has to delete an element from the heap.</p>
		 * @throws ClassCastException No comparator is available, and a key was
		 * encountered that does not implement {@link Comparable}.
		 * @throws ConcurrentModificationException The pairing heap was modified
		 * since the construction of this iterator.
		 * @throws IllegalStateException The {@link #next()} method has not yet
		 * been called, or the {@link #remove()} method has already been called
		 * after the last call to the {@code next()} method.
		 */
		@Override
		public void remove() {
			if(originalModCount != modCount) { //The heap was modified!
				throw new ConcurrentModificationException("The pairing heap was modified during iteration.");
			}
			if(current == null) { //Remove() makes current = null. Or it was still null from the beginning.
				throw new IllegalStateException("The next() method has not yet been called, or the remove() method has already been called since the last call to next().");
			}
			if(current == root) { //We're removing the root. We'll need to mark a new root.
				deleteMin();
				todo.push(root); //Start from the new root.
				current = null;
				return;
			}

			modCount++; //From here we'll modify the heap ourselves.
			if(current.child == null) { //This element has no children, so nothing will replace it.
				if(current.previous.child == current) { //This is the leftmost child.
					if(current.next != null) { //And not the rightmost child.
						current.next.previous = current.previous;
						current.previous.child = current.next;
						todo.push(current.next); //Continue with the right sibling.
					} else { //It was the only child.
						current.previous.child = null;
						//Continue with the next unexplored branch.
					}
				} else { //Not the leftmost child.
					if(current.next != null) { //And not the rightmost child.
						current.next.previous = current.previous;
						current.previous.next = current.next;
						todo.push(current.next); //Continue with the right sibling.
					} else { //It was the rightmost child.
						current.previous.next = null;
						//Continue with the next unexplored branch.
					}
				}
			} else { //It has children, and they'll need to be combined.
				final Element children;
				if(comparator == null) { //Merge children using Comparable.
					children = combineTwoPassComparable(current.child);
				} else { //Merge children using Comparator.
					children = combineTwoPassComparator(current.child);
				}
				children.previous = current.previous;
				children.next = current.next;
				if(current.previous.child == current) { //This is the leftmost child.
					current.previous.child = children;
				} else { //Not the leftmost child.
					current.previous.next = children;
				}
				if(current.next != null) { //Not the rightmost child.
					current.next.previous = children;
				}
				todo.push(children); //Continue with the children.
			}
			size--;
			current = null; //Make sure we continue iterating at the next todo-element.
		}
	}

	/**
	 * An iterator for a pairing heap that returns the elements of the heap in
	 * order of increasing key. This iterator internally creates an array of all
	 * elements in the heap, sorts it, and iterates through it.
	 * <p>In the face of concurrent modification of the encompassing heap, this
	 * iterator is <i>fail-fast</i>, meaning that if any modification to the
	 * heap is made, the iterator will immediately throw a
	 * {@link ConcurrentModificationException} on the next call to
	 * {@link #next()}. This fail-fast behaviour prevents nondeterministic
	 * results if changes are made between the constructing of the iterator and
	 * the last call to {@code next()}. Rather than being unpredictable, it will
	 * always throw an exception. This fail-fast behaviour is on a best-effort
	 * basis, and makes no guarantees that the iteration will be correct when
	 * the heap is modified during the iteration.</p>
	 * <p>The iterator supports the {@link #remove()} operation. The
	 * {@link #hasNext()} and {@link #next()} operations both perform in
	 * {@code O(1)} time. The {@link #remove()} operation performs in
	 * {@code O(log(n)} amortized time, as it calls the heap to remove the
	 * element. The bulk of the time required to use this heap is in the
	 * constructor, which takes {@code O(n*log(n))} time in the worst case.</p>
	 * <p>Perhaps in a future version of this class, the iterator may guarantee
	 * constant time to construct, and {@code O(log(n))} time per call to
	 * {@link #next()}. The best alternative I could come up with was to make a
	 * lazy copy of the heap during the iteration, which took {@code O(n)} time
	 * per iteration in the worst case and {@code O(log(n))} amortized time, but
	 * was much slower in practice. This might have been slightly faster if only
	 * the first few elements of the iteration are requested, especially if some
	 * elements had been deleted from the heap beforehand, and the heap is huge
	 * enough. But that is a very narrow use case. Therefore, this
	 * implementation (which is really just {@code Arrays.sort(toArray())}) was
	 * chosen.</p>
	 */
	private class ItrSorted implements Iterator<Element> {
		/**
		 * An array of the elements of this heap, sorted by their keys at the
		 * construction of this iterator.
		 */
		private final Object[] sortedElements;

		/**
		 * The index of the next element to return in the iteration through the
		 * sorted array of elements.
		 */
		private int index;

		/**
		 * The {@code modCount} of the {@code PairingHeap} instance at the time
		 * of this iterator's creation or its most recent call to
		 * {@link #remove()}. If this is different from the current
		 * {@code modCount}, that means that the heap was modified such that the
		 * traversal order of this iterator was changed, and we should throw a
		 * {@link ConcurrentModificationException} if the user tries to continue
		 * iterating through the heap.
		 */
		private int originalModCount;

		/**
		 * Constructs a new iterator for the parent {@code PairingHeap}
		 * instance. This immediately gathers all elements in an array and sorts
		 * them by their key, to be ready to iterate over them.
		 */
		@SuppressWarnings("unchecked") //Caused by casting an Object[] array back to Element[], but it has only Elements so it's okay.
		ItrSorted() {
			originalModCount = modCount; //Make a copy of the modCount.
			sortedElements = toArray(); //Gather all elements.
			Arrays.sort(sortedElements,comparatorByKey); //Compare by keys.
		}

		/**
		 * Returns {@code true} if the iteration has more elements. In other
		 * words, returns {@code true} if {@link #next()} would return an
		 * element rather than throwing an exception.
		 * @return {@code true} if the iteration has more elements, or
		 * {@code false} otherwise.
		 */
		@Override
		public boolean hasNext() {
			return index < sortedElements.length; //Not out of bounds of the array yet.
		}

		/**
		 * Returns the next element in the iteration. If there are no more
		 * elements, a {@link NoSuchElementException} will be thrown.
		 * @return The next element in the iteration.
		 * @throws ConcurrentModificationException The pairing heap was modified
		 * since this iterator was constructed.
		 * @throws NoSuchElementException The iteration has no more elements.
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Element next() {
			if(originalModCount != modCount) { //The heap was modified!
				throw new ConcurrentModificationException("The pairing heap was modified during iteration.");
			}
			if(index >= sortedElements.length) { //Past the end of the array.
				throw new NoSuchElementException("The iteration has no more elements.");
			}
			return (Element)sortedElements[index++];
		}

		/**
		 * Removes from the heap the last element returned by this iterator.
		 * This method can be called only once per call to {@link #next()}. The
		 * iterator will not throw a {@link ConcurrentModificationException} as
		 * result of the modification caused by this method.
		 * <p>This method takes {@code O(log(n))} amortized time to execute,
		 * since it has to delete an element from the heap.</p>
		 * @throws ClassCastException No comparator is available, and a key was
		 * encountered that does not implement {@link Comparable}.
		 * @throws ConcurrentModificationException The pairing heap was modified
		 * since the construction of this iterator.
		 */
		@Override
		public void remove() {
			if(originalModCount != modCount) { //The heap was modified!
				throw new ConcurrentModificationException("The pairing heap was modified during iteration.");
			}
			if(index <= 0) { //Haven't yet called next().
				throw new IllegalStateException("The next() method has not yet been called.");
			}
			@SuppressWarnings("unchecked")
			final Element removeThis = (Element)sortedElements[index - 1]; //The element that needs to be removed.
			if(removeThis == null) { //Has already been removed.
				throw new IllegalStateException("The remove() method has already been called since the last call to next().");
			}
			delete(removeThis); //Remove the element.
			originalModCount = modCount; //This made a modification, but not a concurrent one, so update our expected modCount.
			sortedElements[index - 1] = null; //Indicate that this element has now been removed, for our next call to remove().
		}
	}

	/**
	 * When their parent has been removed, this helper method merges its
	 * children together. Out comes the element with the lowest key of these
	 * children, with the others linked below it.
	 * <p>This method implements the two-pass strategy as described in the paper
	 * by Fredman et al. First, it goes through the children from left to right,
	 * merging every second sibling with the one before it. Then, it linearly
	 * combines the remaining trees into one, going in reverse order (from right
	 * to left). This strategy is seen as the 'default' strategy for the pairing
	 * heap, and is the one from which it derives its name.</p>
	 * <p>This version assumes that no comparator is available, and keys must be
	 * compared using their natural ordering.</p>
	 * @param element The leftmost sibling of the siblings that must be combined
	 * into one.
	 * @return The root of the subtree that should be placed in the place of the
	 * specified element's removed parent.
	 * @throws ArrayIndexOutOfBoundsException The specified element is
	 * {@code null}. The method doesn't check for this.
	 */
	private Element combineTwoPassComparable(final Element element) {
		//First pass: Combine adjacent pairs.
		//final List<Element> pairs = new ArrayList<>(net.dulek.math.Math.log2(size)); //List of pairs after the first pass.
		Element previousPair = null; //In order to go in backwards order, we'll link each pair to the previous pair in advance.
		Element current = element;
		while(current != null) {
			final Element even = current; //The two elements we'll join.
			final Element odd = current.next;
			if(odd == null) { //There was an odd number of siblings.
				//pairs.add(even); //Add the last one singularly.
				even.previous = previousPair; //Add the last one as singleton.
				previousPair = even;
				break;
			}
			current = odd.next; //Move the index before we mess up the next-pointer of odd.
			Element newPair = joinComparable(even,odd); //Merge these two.
			newPair.previous = previousPair; //Link it so we can find them back.
			previousPair = newPair;
			//pairs.add(joinComparable(even,odd)); //Merge these two.
		}

		//Second pass: Combine linearly from back to front.
		Element result = null;
		/*
		for(int i = pairs.size() - 1;i >= 0;i--) { //Iterate linearly from back to front.
			result = joinComparable(pairs.get(i),result); //And combine one by one.
		}
		*/
		while(previousPair != null) { //Traverse lineraly from back to front.
			current = previousPair;
			previousPair = previousPair.previous;
			result = joinComparable(current,result); //Combine one by one.
		}

		return result;
	}

	/**
	 * When their parent has been removed, this helper method merges its
	 * children together. Out comes the element with the lowest key of these
	 * children, with the others linked below it.
	 * <p>This method implements the two-pass strategy as described in the paper
	 * by Fredman et al. First, it goes through the children from left to right,
	 * merging every second sibling with the one before it. Then it linearly
	 * combines the remaining trees into one, going in reverse order (from right
	 * to left). This strategy is seen as the 'default' strategy for the pairing
	 * heap, and is the one from which it derives its name.</p>
	 * <p>This version assumes that a comparator is available and keys must be
	 * compared using that comparator.</p>
	 * @param element The leftmost sibling of the siblings that must be combined
	 * into one.
	 * @return The root of the subtree that should be placed in the place of the
	 * specified element's removed parent.
	 * @throws ArrayIndexOutOfBoundsException The specified element is
	 * {@code null}. The method doesn't check for this.
	 */
	private Element combineTwoPassComparator(final Element element) {
		//First pass: Combine adjacent pairs.
		final List<Element> pairs = new ArrayList<>(net.dulek.math.Math.log2(size)); //List of pairs after the first pass.
		Element current = element;
		while(current != null) {
			final Element even = current; //The two elements we'll join.
			final Element odd = current.next;
			if(odd == null) { //There was an odd number of siblings.
				pairs.add(even); //Add the last one singularly.
				break;
			}
			current = odd.next; //Move the index before we mess up the next-pointer of odd.
			pairs.add(joinComparator(even,odd)); //Merge these two.
		}

		//Second pass: Combine linearly from back to front.
		Element result = null;
		for(int i = pairs.size() - 1;i > 0;i--) { //Iterate linearly from back to front.
			result = joinComparator(pairs.get(i),result); //And combine one by one.
		}

		return result;
	}

	/**
	 * Joins the two specified entries together. The entry with the smallest key
	 * is returned. The entry with the largest key is attached as the first
	 * child of the entry with the smallest key. This helper method assumes that
	 * no comparator is available, so it compares keys with the
	 * {@code Comparable} interface.
	 * <p>Care should be taken that {@code one} cannot be {@code null}, but
	 * {@code two} may be {@code null}. This allows for joining an element with
	 * the root node, which may be {@code null} if the heap is empty, and it
	 * allows for joining an element with its right sibling which it might not
	 * have. Both are common operations, and for this reason it will check first
	 * whether {@code two == null}.</p>
	 * <p>The environments of these two elements are not modified. This means
	 * that the lower of the two (the element returned) will still be
	 * double linked to its siblings and/or parent, and the other will have its
	 * original siblings and/or parent point to him but won't point back. This
	 * means that before this method is called, it should be made sure that no
	 * other elements point to the specified elements other than their children.
	 * </p>
	 * @param one The element to be joined together with {@code two}.
	 * @param two The element to be joined together with {@code one}, or
	 * {@code null} if there is no second element.
	 * @return The combined tree of the two elements.
	 * @throws ClassCastException A key of either element does not implement the
	 * {@code Comparable} interface but no comparator is given.
	 * @throws NullPointerException The {@code one} parameter is {@code null}.
	 */
	@SuppressWarnings("unchecked") //Thrown when either key does not implement the Comparable interface but no comparator is available.
	private Element joinComparable(final Element one,final Element two) {
		if(two == null) { //No second element exists.
			return one;
		}
		if(((Comparable<K>)one.key).compareTo(two.key) < 0) { //One has the lesser key. Throws a ClassCastException if either key is not comparable. Throws NullPointerException if One is null.
			//Make Two a child of One.
			final Element oneChild = one.child;
			two.next = oneChild;
			if(oneChild != null) {
				oneChild.previous = two;
			}
			one.child = two;
			two.previous = one;
			one.next = null;
			return one;
		} else { //Two has the lesser key.
			//Make One a child of Two.
			final Element twoChild = two.child;
			one.next = twoChild;
			if(twoChild != null) {
				twoChild.previous = one;
			}
			two.child = one;
			one.previous = two;
			two.next = null;
			return two;
		}
	}

	/**
	 * Joins the two specified entries together. The entry with the smallest key
	 * is returned. The entry with the largest key is attached as the first
	 * child of the entry with the smallest key. This helper method assumes that
	 * a comparator is available, and compares keys with that comparator.
	 * <p>Care should be taken that {@code one} cannot be {@code null}, but
	 * {@code two} may be {@code null}. This allows for joining an element with
	 * the root node, which may be {@code null} if the heap is empty, and it
	 * allows for joining an element with its right sibling which it might not
	 * have. Both are common operations, and for this reason it will check first
	 * whether {@code two == null}.</p>
	 * @param one The element to be joined together with {@code two}.
	 * @param two The element to be joined together with {@code one}, or
	 * {@code null} if there is no second element.
	 * @return The combined tree of the two elements.
	 * @throws NullPointerException The {@code one} parameter is {@code null},
	 * or there is no comparator in this heap.
	 */
	private Element joinComparator(final Element one,final Element two) {
		if(two == null) { //No second element exists.
			return one;
		}
		if(comparator.compare(one.key,two.key) < 0) { //One has the lesser key. Throws NullPointerException if One is null or there is no comparator.
			//Make Two a child of One.
			final Element oneChild = one.child;
			two.next = oneChild;
			if(oneChild != null) {
				oneChild.previous = two;
			}
			one.child = two;
			two.previous = one;
			one.next = null;
			return one;
		} else { //Two has the lesser key.
			//Make One a child of Two.
			final Element twoChild = two.child;
			one.next = twoChild;
			if(twoChild != null) {
				twoChild.previous = one;
			}
			two.child = one;
			one.previous = two;
			two.next = null;
			return two;
		}
	}

	/**
	 * Reconstitutes the {@code PairingHeap} instance from a stream (that is,
	 * deserialises it).
	 * <p>This method takes {@code O(n)} time, since the stream contains
	 * {@code O(n)} elements and for each element {@code O(1)} additional data
	 * to indicate the direction of traversal to reconstruct the heap.</p>
	 * @param ois The stream from which to read data.
	 * @throws ClassCastException The stream is corrupted and contained the
	 * wrong classes at the wrong locations.
	 * @throws ClassNotFoundException The stream contained an unknown class.
	 * @throws IOException The stream couldn't properly be read or is no
	 * serialised instance of {@code PairingHeap}.
	 */
	@SuppressWarnings("unchecked") //Caused by casting objects found in the stream to keys or values, but shouldn't happen unless there is a serialVerionID clash or a corrupt stream.
	private void readObject(final ObjectInputStream ois) throws IOException,ClassNotFoundException {
		modCount++; //We're re-constructing the entire heap, so yeah we could call that a significant modification I guess.
		ois.defaultReadObject(); //Read non-transient fields (comparator, comparatorByKey and size).
		if(size == 0) { //Empty.
			return;
		}

		//Process the root separately, since we need to store it in a field.
		root = new Element((K)ois.readObject(),(V)ois.readObject());

		//Make a depth-first traversal of the tree, guided by the direction flags in the stream.
		final Deque<Element> stack = new ArrayDeque<>(12); //Don't know any size yet to guide initial capacity by. Guess 12.
		Element current = root;
		while(true) {
			byte direction = ois.readByte(); //What direction should we go?
			switch(direction) {
				case 0: { //Towards child.
					final Element child = new Element((K)ois.readObject(),(V)ois.readObject()); //Read the child element.
					current.child = child; //Link it properly.
					child.previous = current;
					stack.push(current); //And keep track of the trail back up.
					current = child;
					break;
				}
				case 1: { //Towards right sibling.
					final Element sibling = new Element((K)ois.readObject(),(V)ois.readObject()); //Read the sibling element.
					current.next = sibling; //Link it properly.
					sibling.previous = current;
					current = sibling;
					break;
				}
				case 2: { //Towards parent.
					//No new element then.
					if(current == root) { //Can't go towards parent, since it has no parent.
						return; //Then we're done.
					}
					current = stack.pop(); //Go one level up.
				}
			}
		}
	}

	/**
	 * Saves this {@code PairingHeap} to a stream (that is, serialises it).
	 * <p>This method takes {@code O(n)} time, since it makes a depth-first
	 * traversal through the heap and writes every element it encounters along
	 * with a linear number of directional flags to indicate the direction of
	 * the traversal.</p>
	 * @param oos The stream to which to write data.
	 * @serialData To emit an element, first the key, then the value is emitted.
	 * To serialise the heap, first the root is emitted. Then, doing a
	 * depth-first traversal over the tree, first a directional flag is emitted
	 * and then optionally an element in that direction. The directional flag is
	 * a byte. A flag value of 0 indicates that a child element will follow. A
	 * flag value of 1 indicates that a right sibling element will follow. A
	 * value of 2 indicates that no element will follow, but the traversal
	 * should go up to the current element's parent. These are repeated until
	 * all elements are serialised and the traversal is back in the root.
	 * @throws IOException The stream could not be written to.
	 * @throws NotSerializableException A key or value of this heap could not be
	 * serialised.
	 */
	private void writeObject(final ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject(); //Write non-transient fields (comparator, comparatorByKey and size).

		//Make a depth-first traversal of the tree, and write additional direction flags to indicate which way we went.
		final Deque<Element> stack = new ArrayDeque<>(net.dulek.math.Math.log2(size)); //Breadcrumb trail to find the parents quickly.
		Element current = root;
		oos.writeObject(current.key); //Write the root to the stream first.
		oos.writeObject(current.value);
		boolean childDone = false; //To indicate a direction. Have we already processed the child?
		while(true) {
			if(!childDone && current.child != null) { //There is a child to process.
				oos.writeByte(0); //Direction flag 0: Move to the child.
				stack.push(current); //Store this node on the stack so we can backtrack easily.
				current = current.child; //Move to the child.
				oos.writeObject(current.key); //Write the child.
				oos.writeObject(current.value);
			} else if(current.next != null) { //There is a sibling to process.
				oos.writeByte(1); //Direction flag 1: Move to the sibling.
				current = current.next;
				childDone = false;
				oos.writeObject(current.key); //Write the sibling.
				oos.writeObject(current.value);
			} else { //No child, no sibling, then go back to the parent.
				if(current == root) { //But we're back at the root, so there is no more parent.
					break;
				}
				oos.writeByte(2);
				current = stack.pop();
				childDone = true;
				//But don't write the parent again!
			}
		}
	}

	/**
	 * Testing routine to print the heap to System.out.
	 */
	public void print() {
		if(root == null) {
			System.out.println("Empty heap.");
			return;
		}
		System.out.println("Root: " + root.key);
		System.out.print("Traversal: ");
		for(Element elem : this) {
			System.out.print(elem.key + " ");
		}
		System.out.println(" (" + size + ")");
		for(Element elem : this) {
			if(elem.child != null) {
				System.out.print(elem.key + ": ");
				Element current = elem.child;
				while(current != null) {
					System.out.print(current.key + ", ");
					current = current.next;
				}
				System.out.println();
			}
		}
		System.out.println();
	}

	/**
	 * Another testing routine. This one prints the tree on an image.
	 */
	public void visualise() {
		java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(1000,1000,java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
		for(int x = 0;x < 1000;x++) {
			for(int y = 0;y < 1000;y++) {
				image.setRGB(x,y,0);
			}
		}
		Map<Element,Integer> elemX = new IdentityHashMap<>(size);
		Map<Element,Integer> elemY = new IdentityHashMap<>(size);
		Element current = root;
		int x = 1;
		int y = 1;
		Deque<Element> todo = new ArrayDeque<>();
		while(true) {
			if(!elemX.containsKey(current)) {
				elemX.put(current,x);
				elemY.put(current,y);
			}
			visualiseElement(image,x,y,current);
			if(current.previous != null) {
				int prevX = elemX.get(current.previous);
				int prevY = elemY.get(current.previous);
				if(current.previous.next == current) {
					visualiseArrow(image,x,y + 6,prevX + 10,prevY + 6);
				} else if(current.previous.child == current) {
					visualiseArrow(image,x + 6,y,prevX + 6,prevY + 10);
				} else {
					visualiseArrow(image,x,y,prevX + 10,prevY + 10);
				}
			}
			if(current.child != null && !elemX.containsKey(current.child)) {
				visualiseArrow(image,x + 3,y + 10,x + 3,y + 20);
				if(current.next != null) {
					int width = visualiseWidth(current) * 20;
					visualiseArrow(image,x + 10,y + 3,x + width,y + 3);
					if(!elemX.containsKey(current.next)) {
						todo.push(current.next);
						elemX.put(current.next,x + width);
						elemY.put(current.next,y);
					} else {
						System.out.println("Already seen " + current.next.key);
					}
				}
				current = current.child;
				y += 20;
			} else if(current.next != null && !elemX.containsKey(current.next)) {
				int width = visualiseWidth(current) * 20;
				visualiseArrow(image,x + 10,y + 3,x + width,y + 3);
				current = current.next;
				x += width;
			} else {
				if(todo.isEmpty()) {
					break;
				}
				current = todo.pop();
				x = elemX.get(current);
				y = elemY.get(current);
			}
		}
		try {
			javax.imageio.ImageIO.write(image,"PNG",new java.io.File("C:/temp/visualisation.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private int visualiseWidth(Element element) {
		int width = 1;
		if(element.child != null) {
			width = 0;
			for(Element current = element.child;current != null;current = current.next) {
				width += visualiseWidth(current);
			}
		}
		return width;
	}

	private void visualiseLine(java.awt.image.BufferedImage image,int x1,int y1,int x2,int y2) {
		if(x1 < 0 || x1 >= 1000 || x2 < 0 || x2 >= 1000 || y1 < 0 || y1 >= 1000 || y2 < 0 || y2 >= 1000) {
			System.out.println("Out of bounds: (" + x1 + "," + y1 + ") - (" + x2 + "," + y2 + ")");
		}
		for(double i = 0;i < 1;i += 0.01) {
			image.setRGB((int)(x1 + (x2 - x1) * i),(int)(y1 + (y2 - y1) * i),Integer.MAX_VALUE);
		}
	}

	private void visualiseArrow(java.awt.image.BufferedImage image,int x1,int y1,int x2,int y2) {
		visualiseLine(image,x1,y1,x2,y2);
		image.setRGB(x2,y2,Integer.MAX_VALUE);
		image.setRGB(x2 + 1,y2 + 1,Integer.MAX_VALUE);
		image.setRGB(x2 - 1,y2 - 1,Integer.MAX_VALUE);
		image.setRGB(x2 + 1,y2 - 1,Integer.MAX_VALUE);
		image.setRGB(x2 - 1,y2 + 1,Integer.MAX_VALUE);
	}

	private void visualiseElement(java.awt.image.BufferedImage image,int x,int y,Element element) {
		visualiseLine(image,x,y,x + 10,y);
		visualiseLine(image,x + 10,y,x + 10,y + 10);
		visualiseLine(image,x + 10,y + 10,x,y + 10);
		visualiseLine(image,x,y + 10,x,y);
		int key = (Integer)element.getKey(); //Yes, this only works for integer keys. I didn't put in the whole alphabet or something.
		int digitx = x + 5;
		while(key > 0) {
			visualiseDigit(image,digitx,y + 3,key % 10);
			key /= 10;
			digitx -= 4;
		}
	}

	private void visualiseDigit(java.awt.image.BufferedImage image,int x,int y,int digit) {
		int colour = Integer.MAX_VALUE << 14;
		//Sorry. I have no internet and couldn't remember how to print text... Haha.
		switch(digit) {
			case 0:
				image.setRGB(x + 1,y,colour);
				image.setRGB(x + 2,y,colour);
				image.setRGB(x + 3,y + 1,colour);
				image.setRGB(x + 3,y + 2,colour);
				image.setRGB(x + 3,y + 3,colour);
				image.setRGB(x + 2,y + 4,colour);
				image.setRGB(x + 1,y + 4,colour);
				image.setRGB(x,y + 3,colour);
				image.setRGB(x,y + 2,colour);
				image.setRGB(x,y + 1,colour);
				break;
			case 1:
				image.setRGB(x,y + 1,colour);
				image.setRGB(x + 1,y,colour);
				image.setRGB(x + 1,y + 1,colour);
				image.setRGB(x + 1,y + 2,colour);
				image.setRGB(x + 1,y + 3,colour);
				image.setRGB(x + 1,y + 4,colour);
				break;
			case 2:
				image.setRGB(x,y + 1,colour);
				image.setRGB(x + 1,y,colour);
				image.setRGB(x + 2,y,colour);
				image.setRGB(x + 3,y + 1,colour);
				image.setRGB(x + 3,y + 2,colour);
				image.setRGB(x + 2,y + 3,colour);
				image.setRGB(x + 1,y + 4,colour);
				image.setRGB(x,y + 4,colour);
				image.setRGB(x + 2,y + 4,colour);
				image.setRGB(x + 3,y + 4,colour);
				break;
			case 3:
				image.setRGB(x,y,colour);
				image.setRGB(x + 1,y,colour);
				image.setRGB(x + 2,y,colour);
				image.setRGB(x + 3,y,colour);
				image.setRGB(x + 3,y + 1,colour);
				image.setRGB(x + 3,y + 2,colour);
				image.setRGB(x + 2,y + 2,colour);
				image.setRGB(x + 3,y + 3,colour);
				image.setRGB(x + 3,y + 4,colour);
				image.setRGB(x + 2,y + 4,colour);
				image.setRGB(x + 1,y + 4,colour);
				image.setRGB(x,y + 4,colour);
				break;
			case 4:
				image.setRGB(x,y,colour);
				image.setRGB(x,y + 1,colour);
				image.setRGB(x,y + 2,colour);
				image.setRGB(x,y + 3,colour);
				image.setRGB(x + 1,y + 3,colour);
				image.setRGB(x + 2,y + 3,colour);
				image.setRGB(x + 3,y + 3,colour);
				image.setRGB(x + 3,y + 2,colour);
				image.setRGB(x + 3,y + 1,colour);
				image.setRGB(x + 3,y,colour);
				image.setRGB(x + 3,y + 4,colour);
				break;
			case 5:
				image.setRGB(x + 3,y,colour);
				image.setRGB(x + 2,y,colour);
				image.setRGB(x + 1,y,colour);
				image.setRGB(x,y,colour);
				image.setRGB(x,y + 1,colour);
				image.setRGB(x,y + 2,colour);
				image.setRGB(x + 1,y + 2,colour);
				image.setRGB(x + 2,y + 2,colour);
				image.setRGB(x + 3,y + 2,colour);
				image.setRGB(x + 3,y + 3,colour);
				image.setRGB(x + 3,y + 4,colour);
				image.setRGB(x + 2,y + 4,colour);
				image.setRGB(x + 1,y + 4,colour);
				image.setRGB(x,y + 4,colour);
				break;
			case 6:
				image.setRGB(x + 3,y,colour);
				image.setRGB(x + 2,y,colour);
				image.setRGB(x + 1,y,colour);
				image.setRGB(x,y,colour);
				image.setRGB(x,y + 1,colour);
				image.setRGB(x,y + 2,colour);
				image.setRGB(x,y + 3,colour);
				image.setRGB(x,y + 4,colour);
				image.setRGB(x + 1,y + 4,colour);
				image.setRGB(x + 2,y + 4,colour);
				image.setRGB(x + 3,y + 4,colour);
				image.setRGB(x + 3,y + 3,colour);
				image.setRGB(x + 3,y + 2,colour);
				image.setRGB(x + 2,y + 2,colour);
				image.setRGB(x + 1,y + 2,colour);
				break;
			case 7:
				image.setRGB(x,y,colour);
				image.setRGB(x + 1,y,colour);
				image.setRGB(x + 2,y,colour);
				image.setRGB(x + 3,y,colour);
				image.setRGB(x + 3,y + 1,colour);
				image.setRGB(x + 3,y + 2,colour);
				image.setRGB(x + 3,y + 3,colour);
				image.setRGB(x + 3,y + 4,colour);
				break;
			case 8:
				image.setRGB(x,y,colour);
				image.setRGB(x + 1,y,colour);
				image.setRGB(x + 2,y,colour);
				image.setRGB(x + 3,y,colour);
				image.setRGB(x + 3,y + 1,colour);
				image.setRGB(x + 3,y + 2,colour);
				image.setRGB(x + 3,y + 3,colour);
				image.setRGB(x + 3,y + 4,colour);
				image.setRGB(x + 2,y + 4,colour);
				image.setRGB(x + 1,y + 4,colour);
				image.setRGB(x,y + 4,colour);
				image.setRGB(x,y + 3,colour);
				image.setRGB(x,y + 2,colour);
				image.setRGB(x,y + 1,colour);
				image.setRGB(x + 1,y + 2,colour);
				image.setRGB(x + 2,y + 2,colour);
				break;
			case 9:
				image.setRGB(x,y,colour);
				image.setRGB(x + 1,y,colour);
				image.setRGB(x + 2,y,colour);
				image.setRGB(x + 3,y,colour);
				image.setRGB(x + 3,y + 1,colour);
				image.setRGB(x + 3,y + 2,colour);
				image.setRGB(x + 3,y + 3,colour);
				image.setRGB(x + 3,y + 4,colour);
				image.setRGB(x + 2,y + 4,colour);
				image.setRGB(x + 1,y + 4,colour);
				image.setRGB(x,y + 4,colour);
				image.setRGB(x,y + 2,colour);
				image.setRGB(x,y + 1,colour);
				image.setRGB(x + 1,y + 2,colour);
				image.setRGB(x + 2,y + 2,colour);
				break;
			default:
				throw new InternalError("Unknown digit " + digit);
		}
	}
}
