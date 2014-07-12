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

import java.util.*;

/**
 * This class implements a Treap, which is a probabilistic search tree with a
 * high probability to balance to a logarithmic height for optimal performance.
 * It maintains a dynamic set of ordered keys and allows binary searches among
 * the keys.
 * @param <K> The type of the keys in the Treap.
 * @param <V> The type of the values in the Treap.
 * @author Ruben Dulek
 * @version 1.0
 */
public class Treap<K extends Comparable<K>,V> implements Map<K,V> {
	/**
	 * This is an entry in the Treap. It represents one element, a key-value
	 * pair. The entry contains a priority, which is a random number, that
	 * determines its position in the tree to a certain extent.
	 * @param <K> The type of the key of this entry.
	 * @param <V> The type of the value of this entry.
	 */
	public class Entry<K extends Comparable<K>,V> implements Map.Entry<K,V> {
		/**
		 * The key that this entry is identified by. This is used to compare
		 * with other keys to find the entry quickly.
		 */
		protected final K key;

		/**
		 * The value of this entry.
		 */
		protected V value;

		/**
		 * The priority of this entry. This is determined randomly when the
		 * entry is created. It decides the position of the nodes in the search
		 * tree if that is possible, and through randomness aims to generate a
		 * tree with minimal (logarithmic) height.
		 */
		protected final long priority;

		/**
		 * The root node of the left sub-tree, or null if there are no entries
		 * in the left sub-tree.
		 */
		protected Entry<K,V> left;

		/**
		 * The root node of the right sub-tree, or null if there are no entries
		 * in the right sub-tree.
		 */
		protected Entry<K,V> right;

		/**
		 * The parent node of this entry, or null if it is the global root node.
		 */
		protected Entry<K,V> parent;

		/**
		 * Creates a new entry in the Treap with the indicated key and value.
		 * The entry is not immediately added to the Treap.
		 * @param key The key of the new entry.
		 * @param value The value of the new entry.
		 * @param priority The priority of this node. It is determined randomly
		 * by the treap with a pseudo-random number generator.
		 */
		protected Entry(final K key,final V value,final long priority) {
			this.key = key;
			this.value = value;
			this.priority = priority;
			left = null;
			right = null;
			parent = null;
		}

		/**
		 * Returns whether this entry has the same value as the other entry.
		 * @param other The other entry to compare this entry with.
		 * @return Whether this entry has the same value as the other entry.
		 */
		@Override
		@SuppressWarnings("unchecked") //We handle this with a try-catch block.
		public boolean equals(final Object other) {
			if(other instanceof Entry) {
				try {
					return value.equals(((Entry<K,V>)other).value); //Try casting. This may give an exception, but that's caught below. Only compare the values.
				} catch(ClassCastException e) {
					return false; //The other object is an entry, but with the wrong type parameters.
				}
			}
			return false; //The other object is not an entry.
		}

		/**
		 * Gets the key of this entry.
		 * @return The key of this entry.
		 */
		@Override
		public K getKey() {
			return key;
		}

		/**
		 * Gets the value of this entry.
		 * @return The value of this entry.
		 */
		@Override
		public V getValue() {
			return value;
		}

		/**
		 * Gets the hash code of this entry. The hash code of an entry is the
		 * hash code of the key XORed with the hash code of the value.
		 * @return The hash code of this entry.
		 */
		@Override
		public int hashCode() {
			return (key == null ? 0 : 1337 * key.hashCode()) ^ (value == null ? 0 : value.hashCode()); //As taken from the Map.Entry interface.
		}

		/**
		 * Replaces the value corresponding to this entry with the specified
		 * value.
		 * @param value The new value of this entry.
		 * @return The old value of this entry.
		 */
		@Override
		public V setValue(final V value) {
			final V old = value;
			this.value = value;
			return old;
		}

		/**
		 * Adds an entry to this node's (sub)tree. The entry is added left if
		 * the key is lower than this entry's key, or right if it was higher.
		 * Afterwards, this node is rotated the other way in an attempt to make
		 * the tree balance out to compensate for the new node. If the keys were
		 * equal, this entry's value is replaced and returned.
		 * @param entry The new entry to arrange into the search tree.
		 * @return If there is an entry in this entry's (sub)tree with the same
		 * key, that entry's value is returned. Otherwise, returns
		 * <code>null</code>.
		 */
		protected V add(final Entry<K,V> entry) {
			final int comparison = key.compareTo(entry.key); //Compare the keys.
			if(comparison > 0) { //Our key is greater than the entry's key.
				final V result;
				if(left != null) { //There is already an entry left of us.
					result = left.add(entry); //Recursively add it to the left subtree.
				} else {
					left = entry; //Otherwise, this is our left leaf.
					result = null; //So no entry had that key; return null.
				}
				if(priority < left.priority) { //If this node's priority is lower, for the randomness.
					rotateRight(); //Since we're going left, we are allowed a right-rotation.
				}
				return result;
			} else if(comparison < 0) { //Our key is less than the entry's key.
				final V result;
				if(right != null) { //There is already an entry right of us.
					result = right.add(entry); //Recursively add it to the right subtree.
				} else {
					right = entry; //Otherwise, this is our right leaf.
					result = null; //So no entry had that key; return null.
				}
				if(priority < right.priority) { //If this node's priority is lower, for the randomness.
					rotateLeft(); //Since we're going right, we are allowed a left-rotation.
				}
				return result;
			} else { //Keys are equal.
				final V oldValue = value; //Store the old value. We need to return it.
				value = entry.value; //Overwrite the value.
				return oldValue;
			}
		}

		/**
		 * Searches for the value associated with the specified key. If the key
		 * does not exist in this node's subtree, returns <code>null</code>.
		 * @param key The key to search for in the subtree.
		 * @return The value of the entry with the specified key if it is in the
		 * subtree of this node, or <code>null</code> otherwise.
		 */
		protected Entry<K,V> get(final K key) {
			final int comparison = this.key.compareTo(key); //Compare the keys.
			if(comparison > 0) { //Our key is greater than the input key.
				if(left != null)
					return left.get(key); //Search left.
				return null; //No, it's not here.
			} else if(comparison < 0) { //Our key is less than the input key.
				if(right != null)
					return right.get(key); //Search right.
				return null; //No, it's not here.
			}
			return this; //Otherwise the keys must be equal. Found it!
		}

		/**
		 * Gets the entry with the specified value from the node's subtree. A
		 * recursive depth-first search is performed to find this value, since
		 * the entries are sorted by key and not by value.
		 * @param value The value to search for in the subtree.
		 * @return The first entry encountered that has the specified value.
		 */
		protected Entry<K,V> get(final V value) {
			if(this.value.equals(value)) //We found the value!
				return this;
			if(left != null) { //Only search the subtree if there is a subtree.
				final Entry<K,V> leftResult = left.get(value);
				if(leftResult != null) {
					return leftResult;
				}
			}
			if(right != null) { //Only search the subtree if there is a subtree.
				final Entry<K,V> rightResult = right.get(value);
				if(rightResult != null) {
					return rightResult;
				}
			}
			return null;
		}

		/**
		 * Gets whether this node's subtree contains an entry with the specified
		 * key. Since it is indistinguishable, if <code>get(key)</code> returns
		 * <code>null</code>, whether that was because the subtree didn't have
		 * the key, or because the key had a <code>null</code>-value attached,
		 * this method is needed to know whether the subtree contains a key.
		 * @param key The key to search for in the subtree.
		 * @return <code>True</code> if the subtree contains an entry with the
		 * specified key, or <code>false</code> otherwise.
		 */
		protected boolean contains(final K key) {
			final int comparison = this.key.compareTo(key); //Compare the keys.
			if(comparison > 0) { //Our key is greater than the input key.
				if(left != null)
					return left.contains(key); //Search left.
				return false; //No, it's not here.
			} else if(comparison < 0) { //Our key is less than the input key.
				if(right != null)
					return right.contains(key); //Search right.
				return false; //No, it's not here.
			}
			return true; //Otherwise the keys must be equal. Found it!
		}

		/**
		 * Gets whether this node's subtree contains an entry with the specified
		 * value. A recursive depth-first search is performed to find this
		 * value, since the entries are sorted by key and not by value.
		 * @param value The value to search for in the subtree.
		 * @return <code>True</code> if the subtree contains an entry with the
		 * specified value, or <code>false</code> otherwise.
		 */
		protected boolean contains(V value) {
			if(this.value.equals(value)) //We found the value!
				return true;
			return (left == null ? false : left.contains(value)) || (right == null ? false : right.contains(value)); //Only search a subtree if there is a subtree.
		}

		/**
		 * Removes an entry from the Treap, by key. The entry is found by using
		 * a dictionary search, then removed from the tree while keeping the
		 * tree balanced.
		 * @param key The key of the entry to remove.
		 * @return The entry that was removed from the Treap, or
		 * <code>null</code> if there was no such key in this node's subtree.
		 */
		protected Entry<K,V> remove(K key) {
			//First, find the key in the tree.
			final int comparison = this.key.compareTo(key); //Compare the keys.
			if(comparison > 0) { //Our key is greater than the input key.
				if(left != null)
					return left.remove(key); //Search left.
				return null; //No, it's not here.
			} else if(comparison < 0) { //Our key is less than the input key.
				if(right != null)
					return right.remove(key); //Search right.
				return null; //No, it's not here.
			}
			//Otherwise the keys must be equal. Found it!
			while(true) {
				if(left == null && right == null) { //No subtrees. Just remove this leaf and be done with it.
					if(parent != null) { //Can't remove the root from here.
						if(parent.left == this) { //We're the left child.
							parent.left = null; //Delete ourselves.
						} else { //We're the right child.
							parent.right = null; //Delete ourselves.
						}
					}
					return this; //Return the deleted node (me).
				} else if(left == null) { //There's nothing on our left. Link the right subtree to our parent.
					if(parent != null) { //Can't remove the root from here.
						if(parent.left == this) { //We're the left child.
							parent.left = right; //Take us out from between.
						} else { //We're the right child.
							parent.right = right; //Take us out from between.
						}
					}
					return this; //Return the deleted node (me).
				} else if(right == null) { //There's nothing on our right. Link the left subtree to our parent.
					if(parent != null) { //Can't remove the root from here.
						if(parent.left == this) { //We're the left child.
							parent.left = left; //Take us out from between.
						} else { //We're the right child.
							parent.right = left; //Take us out from between.
						}
					}
				} else { //We have both a left and a right subtree.
					//Rotate towards the node with highest priority.
					if(left.priority > right.priority) {
						rotateLeft();
					} else {
						rotateRight();
					}
					//This moves this node further down the tree, eventually hitting the bottom and returning a value.
				}
			}
		}

		/**
		 * Gets the entry after this one. The resulting entry's key will be
		 * greater than our key.
		 * @param direction Whether we should be traversing up or down. You
		 * could also see this boolean as answering the question "Did we just
		 * come from my right-child?".
		 * @return The entry after this one, or <code>null</code> if this is the
		 * right-most entry of the Treap.
		 */
		protected Entry<K,V> after(boolean direction) {
			if(right != null && !direction) //If we have a right child and we didn't just get from there, that is our answer.
				return right;
			if(parent == null) //No parent either.
				return null; //Then we're the rightmost one.
			if(parent.right == this) //Going up-left.
				return parent.after(true);
			return parent; //Going up-right, so the parent is the next one.
		}

		/**
		 * Gets the entry before this one. The resulting entry's key will be
		 * lesser than our key.
		 * @param direction Whether we should be traversing up or down. You
		 * could also see this boolean as answering the question "Did we just
		 * come from my left-child?".
		 * @return The entry before this one, or <code>null</code> if this is
		 * the left-most entry of the Treap.
		 */
		protected Entry<K,V> before(boolean direction) {
			if(left != null && !direction) //If we have a left child and we didn't just get from there, that is our answer.
				return left;
			if(parent == null) //No parent either.
				return null; //Then we're the leftmost one.
			if(parent.left == this) //Going up-right.
				return parent.before(true);
			return parent.left; //Going up-left, so the parent is the previous one.
		}

		/**
		 * Makes a left rotation. In essence, this just swaps our position in
		 * the tree with that of our right child (let's call it node
		 * <code>X</code>), but makes us the left child of <code>X</code>
		 * instead of right. Since we'd be overwriting the left child of
		 * <code>X</code>, we're taking over the left child of <code>X</code> as
		 * our new right child (where <code>X</code> used to be).
		 */
		private void rotateLeft() {
			final Entry<K,V> temp = right.left; //Save this grandchild for now.
			//Change the child of the parent to X.
			if(parent != null) { //Only if we have a parent.
				if(parent.right != null && parent.right.key.equals(key)) { //We're a right child.
					parent.right = right; //Save X as the right child of our parent.
				} else if(parent.left != null && parent.left.key.equals(key)) { //We're a left child.
					parent.left = right; //Save X as the left child of our parent.
				}
			}
			if(right.left != null) //If we have a grandchild left of X.
				right.left.parent = this; //That grandchild needs to become our child, so we're his parent now.
			right.left = this; //We become X's child.
			right.parent = parent; //Our original parent is X's parent now. This will be null if we're the root.
			//Change our own variables lastly, so we don't confuse them!
			parent = right; //And X is our new parent.
			right = temp; //The original grandchild is now our child.
		}

		/**
		 * Makes a right rotation. In essence, this just swaps our position in
		 * the tree with that of our left child (let's call it node
		 * <code>X</code>), but makes us the right child of <code>X</code>
		 * instead of left. Since we'd be overwriting the right child of
		 * <code>X</code>, we're taking over the right child of <code>X</code>
		 * as our new left child (where <code>X</code> used to be).
		 */
		private void rotateRight() {
			final Entry<K,V> temp = left.right; //Save this grandchild for now.
			//Change the child of the parent to X.
			if(parent != null) { //Only if we have a parent.
				if(parent.right != null && parent.right.key.equals(key)) { //We're a right child.
					parent.right = left; //Save X as the right child of our parent.
				} else if(parent.left != null && parent.left.key.equals(key)) { //We're a left child.
					parent.left = left; //Save X as the left child of our parent.
				}
			}
			if(left.right != null) //If we have a grandchild right of X.
				left.right.parent = this; //That grandchild needs to become our child, so we're his parent now.
			left.right = this; //We become X's child.
			left.parent = parent; //Our original parent is X's parent now. This will be null if we're the root.
			//Change our own variables lastly, so we don't confuse them!
			parent = left; //And x is our new parent.
			left = temp; //The original grandchild is now our child.
		}
	}

	/**
	 * A collection of entries from a Treap. This set is just a wrapper of a
	 * <code>LinkedHashSet</code> and behaves nearly the same. The key
	 * difference between the <code>EntrySet</code> and the
	 * <code>LinkedHashSet</code> is that the <code>EntrySet</code> contains a
	 * field that links it to a Treap, and removing elements from the
	 * <code>EntrySet</code> also removes those elements from the corresponding
	 * Treap and vice-versa.
	 */
	public class EntrySet extends LinkedHashSet<Map.Entry<K,V>> {
		/**
		 * The Treap that this Set got its entries from. If we have to remove
		 * entries from this set, they must also be removed from this Treap.
		 */
		private final Treap<K,V> treap;

		/**
		 * Class version number. Required for serialization.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>EntrySet</code> from a Treap. The Set will
		 * contain all entries that are in the Treap at the time the Set is
		 * created.
		 * @param input The Treap to create the Set from.
		 */
		protected EntrySet(final Treap<K,V> input) {
			super(); //Create the LinkedHashSet.
			treap = input; //Link the Treap to the set.
			final Deque<Entry<K,V>> todo = new ArrayDeque<>(net.dulek.math.Math.log2(treap.size) + 4); //We're going to do an exhaustive Depth-First Search to find all nodes.
			todo.add(treap.root); //Start the DFS from the root.
			while(!todo.isEmpty()) {
				final Entry<K,V> next = todo.removeFirst(); //Expand the next node.
				super.add(next); //Put it in our set.
				if(next.left != null) {
					todo.addFirst(next.left); //Explore left.
				}
				if(next.right != null) {
					todo.addFirst(next.right); //Explore right.
				}
			}
		}

		/**
		 * Removes the specified entry from this Set if it is present, as well
		 * as from the corresponding Treap that this Set was created by. Returns
		 * <code>true</code> if the Set contained the specified element.
		 * The input object is cast to an Entry, and only removed from the Treap
		 * if it could be cast.
		 * @param o The entry to remove from the Set and the Treap.
		 * @return <code>True</code> if the Set contained the specified entry,
		 * or <code>false</code> otherwise.
		 */
		@Override
		@SuppressWarnings("unchecked") //We handle this with a try-catch block.
		public boolean remove(final Object o) {
			if(o instanceof Entry) {
				try {
					treap.remove(((Entry<K,V>)o).key); //Remove it from the Treap.
				} catch(ClassCastException e) { //Couldn't be cast.
					//Don't remove from the Treap. Try removing it from the Set anyways.
				}
				return super.remove(o); //Also remove it from the Set.
			}
			return false; //The input object was not an entry.
		}

		/**
		 * Removes all of the elements from this Set as well as from the
		 * corresponding Treap. The Set and the Treap will be empty after this
		 * call returns.
		 */
		@Override
		public void clear() {
			treap.clear();
			super.clear();
		}

		/**
		 * The add operation is not supported by this set.
		 * @param e The entry you would want to add. Tough luck.
		 * @return This method never returns anything. It throws an
		 * <code>UnsupportedOperationException</code>.
		 * @throws UnsupportedOperationException The add operation is not
		 * supported by this set.
		 */
		@Override
		public boolean add(final Map.Entry<K,V> e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("The add operation is not supported by this set.");
		}
	}

	/**
	 * A collection of keys from a Treap. This set is just a wrapper of a
	 * <code>LinkedHashSet</code> and behaves nearly the same. The key
	 * difference between the <code>KeySet</code> and the
	 * <code>LinkedHashSet</code> is that the <code>KeySet</code> contains a
	 * field that links it to a Treap, and removing elements from the
	 * <code>KeySet</code> also removes those elements from the corresponding
	 * Treap.
	 */
	public class KeySet extends LinkedHashSet<K> {
		/**
		 * The treap that this Set got its keys from. If we have to remove keys
		 * from this set, they must also be removed from this Treap.
		 */
		private final Treap<K,V> treap;

		/**
		 * Class version number. Required for serialization.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>KeySet</code> from a Treap. The
		 * <code>KeySet</code> will contain all keys that are in the Treap at
		 * the time the Set is created.
		 * @param input The Treap to create the Set from.
		 */
		protected KeySet(final Treap<K,V> input) {
			super(); //Create the LinkedHashSet.
			treap = input; //Link the Treap to the set.
			final Deque<Entry<K,V>> todo = new ArrayDeque<>(net.dulek.math.Math.log2(treap.size) + 4); //We're going to do an exhaustive Depth-First Search to find all nodes.
			todo.add(treap.root); //Start the DFS from the root.
			while(!todo.isEmpty()) {
				final Entry<K,V> next = todo.removeFirst(); //Expand the next node.
				super.add(next.key); //Put its key in our set.
				if(next.left != null) {
					todo.addFirst(next.left); //Explore left.
				}
				if(next.right != null) {
					todo.addFirst(next.right); //Explore right.
				}
			}
		}

		/**
		 * Removes the specified key from this Set if it is present, as well as
		 * from the corresponding Treap that this Set was created by. Returns
		 * <code>true</code> if the Set contained the specified element.
		 * The input object is cast to a Key, and only removed from the Treap if
		 * it could be cast.
		 * @param o The key of the entry to remove from the Set and the Treap.
		 * @return <code>True</code> if the Set contained the specified entry,
		 * or <code>false</code> otherwise.
		 */
		@Override
		@SuppressWarnings("unchecked") //We handle this with a try-catch block.
		public boolean remove(final Object o) {
			try { //Try casting it to a key.
				final K key = (K) o; //Try to cast it to a key.
				treap.remove(key); //Remove it from the Treap.
				return super.remove(o); //Also remove it from the Set.
			} catch(ClassCastException e) {
				return false; //Don't remove it if it could not be cast.
			}
		}

		/**
		 * Removes all of the elements from this Set as well as from the
		 * corresponding Treap. The Set and the Treap will be empty after this
		 * call returns.
		 */
		@Override
		public void clear() {
			treap.clear();
			super.clear();
		}

		/**
		 * The add operation is not supported by this set.
		 * @param e The key you would want to add. Tough luck.
		 * @return This method never returns anything. It throws an
		 * <code>UnsupportedOperationException</code>.
		 * @throws UnsupportedOperationException The add operation is not
		 * supported by this Set.
		 */
		@Override
		public boolean add(final K e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("The add operation is not supported by this set.");
		}
	}

	/**
	 * A collection of values from a Treap. This <code>Collection</code> is
	 * backed by the Treap it was made from, so changes made to this
	 * <code>Collection</code> will be reflected by the Treap and vice versa.
	 * @param <V> The type of values stored in this collection.
	 */
	public class ValuesCollection<V> implements Collection<V> {
		/**
		 * The treap that this Collection got its entries from. Changes made to
		 * the Collection will be reflected in the Treap and vice versa.
		 */
		private final Treap<K,V> treap;

		/**
		 * Creates a new <code>ValuesCollection</code> from a Treap. The
		 * <code>ValuesCollection</code> will contain the same values as the
		 * Treap.
		 * @param input The Treap to create the Collection from.
		 */
		protected ValuesCollection(final Treap<K,V> input) {
			treap = input; //Link the Treap to the Collection.
		}

		/**
		 * Returns the number of elements in this Collection. If this Collection
		 * contains more than <code>Integer.MAX_VALUE</code> elements, returns
		 * <code>Integer.MAX_VALUE</code>.
		 * This simply returns the size of the Treap.
		 * @return The number of elements in this Collection.
		 */
		@Override
		public int size() {
			return treap.size;
		}

		/**
		 * Returns <code>true</code> if this Collection contains no elements.
		 * @return <code>True</code> if this Collection contains no elements, or
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean isEmpty() {
			return treap.isEmpty();
		}

		/**
		 * Returns <code>true</code> if this Collection contains the specified
		 * value. More formally, returns <code>true</code> if and only if this
		 * Collection contains at least one element <code>e</code> such that
		 * <code>(o == null ? e == null : o.equals(e))</code>.
		 * @param o The element whose presence in this Collection is to be
		 * tested.
		 * @return <code>True</code> if this Collection contains the specified
		 * element, or <code>false</code> otherwise.
		 */
		@Override
		@SuppressWarnings("unchecked") //We handle this with a try-catch block.
		public boolean contains(Object o) {
			try { //Try to cast.
				return treap.containsValue((V)o); //Look for it in the Treap.
			} catch(ClassCastException e) { //Couldn't cast!
				return false; //Then it's not a value, so it's not there.
			}
		}

		/**
		 * Returns an iterator over the values in this Collection. This method
		 * takes linear time, due to the nature in which the values in this
		 * Collection are linked to the entries in the Treap. Iterating over all
		 * values with the returned iterator may take linear time again.
		 * @return An iterator over the values of the Treap.
		 */
		@Override
		public Iterator<V> iterator() {
			final ArrayList<V> valueList = new ArrayList<>(size()); //We're going to create a copy that contains the entries and return the Iterator for that.
			final Iterator<Map.Entry<K,V>> entryIterator = treap.entrySet().iterator(); //Iterate over all entries.
			while(entryIterator.hasNext()) //Add them all!
				valueList.add(entryIterator.next().getValue()); //Extract the value from it.
			return valueList.iterator();
		}

		/**
		 * Returns an array containing all of the elements in this Collection.
		 * The returned array will be "safe" in that no references to it are
		 * maintained by this Collection. The caller is thus free to modify the
		 * returned array.
		 * This method acts as bridge between array-based and collection-based
		 * APIs.
		 * @return An array containing all of the elements in this collection.
		 */
		@Override
		public Object[] toArray() {
			final Object[] result = new Object[size()]; //Allocate an array of the correct size.
			final Iterator<Map.Entry<K,V>> entryIterator = treap.entrySet().iterator(); //Iterate over all entries.
			for(int i = 0;i < size() && entryIterator.hasNext();i++) { //The size of the entry iterator should be the same as our size, but you never know.
				result[i] = entryIterator.next().getValue(); //Extract the value from it.
			}
			return result;
		}

		/**
		 * Returns an array containing all of the elements in this Collection.
		 * The runtime type of the returned array is that of the specified
		 * array. If the Collection fits in the specified array, it is returned
		 * therein. Otherwise, a new array is allocated with the runtime type of
		 * the specified array and the size of this Collection.
		 * If this Collection fits in the specified array with room to spare
		 * (i.e., the array has more elements than this Collection), the rest of
		 * the elements in the array are set to <code>null</code>. This is
		 * useful in determining the length of this Collection only if the
		 * caller knows that this Collection does not contain any
		 * <code>null</code> elements.
		 * <p>This method acts as a bridge between array-based and collection-
		 * based APIs. Further, this method allows precise control over the
		 * runtime type of the output array, and may, under certain
		 * circumstances, be used to save allocation costs.</p>
		 * @param a The array into which the elements of this Collection are to
		 * be stored, if it is big enough; otherwise, a new array of the same
		 * runtime type is allocated for this purpose.
		 * @return An array containing all of the elements in this Collection.
		 * @throws ArrayStoreException The runtime type of the specified array
		 * is not a supertype of the runtime type of every element in this
		 * Collection, or every element in this Collection was
		 * <code>null</code>.
		 * @throws NullPointerException The specified array is
		 * <code>null</code>.
		 */
		@Override
		@SuppressWarnings("unchecked") //Everywhere we have a cast, we checked if it was a safe cast, except if the Collection is empty.
		public V[] toArray(final Object[] a) throws ArrayStoreException,NullPointerException {
			if(a.length < size()) { //Doesn't fit in the array.
				final ArrayList<V> result = new ArrayList<>(treap.size); //Needed a generic array, but this will do. Stupid erasure.
				final Iterator<Map.Entry<K,V>> entryIterator = treap.entrySet().iterator(); //Iterate over all entries.
				V typeVar = null;
				for(int i = 0;i < size() && entryIterator.hasNext();i++) { //The size of the entry iterator should be the same as our size, but you never know.
					final V value = entryIterator.next().getValue();
					result.add(value); //Add all values.
					if(typeVar == null) { //Maybe we've found the type?
						typeVar = value;
					}
				}
				if(typeVar != null)
					return result.toArray((V[]) java.lang.reflect.Array.newInstance(typeVar.getClass(),0)); //Safe cast. We checked.
				throw new ArrayStoreException("The collection only has null elements. Unable to find the class of the collection."); //No elements to get the class from.
			}
			final Iterator<Map.Entry<K,V>> entryIterator = treap.entrySet().iterator(); //Iterate over all entries.
			int i;
			for(i = 0;i < size() && entryIterator.hasNext();i++) {
				a[i] = entryIterator.next().getValue(); //Add all values to the array.
			}
			while(i < size()) { //For the rest of the elements.
				a[i] = null; //Just set them to null, as required.
				i++;
			}
			return (V[]) a; //Cast it to V[]. We know it contains only values.
		}

		/**
		 * The add operation is not supported by this Collection. This is
		 * because the Collection interface does not allow us to specify a key
		 * as well.
		 * @param e The value you would want to add. Tough luck.
		 * @return This method never returns anything. It throws an
		 * <code>UnsupportedOperationException</code>.
		 * @throws UnsupportedOperationException The add operation is not
		 * supported by this Collection.
		 */
		@Override
		public boolean add(final V e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("The add operation is not supported by this Collection.");
		}

		/**
		 * Removes a single instance of the specified value from this Collection
		 * if it is present, as well as from the corresponding Treap that this
		 * Collection was created by. More formally, removes an element
		 * <code>e</code> such that
		 * <code>(o == null ? e == null : o.equals(e))</code>, if this
		 * collection contains one or more such elements. Returns
		 * <code>true</code> if this collection contained the specified element
		 * (or equivalently, if this collection changed as a result of the
		 * call).
		 * @param o The value to remove from the Collection and the Treap, if
		 * present.
		 * @return <code>True</code> if an element was removed as a result of
		 * this call, or <code>false</code> otherwise.
		 */
		@Override
		@SuppressWarnings("unchecked") //We handle this with a try-catch block.
		public boolean remove(final Object o) {
			try { //Try to cast.
				final V value = (V) o;
				final Iterator<Map.Entry<K,V>> entryIterator = treap.entrySet().iterator(); //Iterate over all entries to find this particular value.
				while(entryIterator.hasNext()) {
					final Map.Entry<K,V> next = entryIterator.next();
					if(next.getValue().equals(value)) { //Found it?
						treap.remove(next.getKey()); //Remove it from the Treap, and thus from this Collection.
						return true;
					}
				}
				return false; //Didn't find it.
			} catch(ClassCastException e) { //Couldn't cast.
				return false; //So it's not a value, and it won't be in here.
			}
		}

		/**
		 * Returns <code>true</code> if this Collection contains all of the
		 * elements in the specified collection.
		 * @param c The collection to be checked for being a subset of this
		 * Collection.
		 * @return <code>True</code> if this Collection contains all of the
		 * elements in the specified collection, or </code>false</code>
		 * otherwise.
		 */
		@Override
		@SuppressWarnings("unchecked") //We handle this with a try-catch block.
		public boolean containsAll(final Collection<?> c) {
			final Iterator<?> iterator = c.iterator(); //Iterate over the collection.
			while(iterator.hasNext()) {
				final Object next = iterator.next();
				try { //Try to cast.
					V value = (V) next;
					if(!contains((V)next)) //Nop, not here.
						return false;
				} catch(ClassCastException e) { //If it couldn't be cast, this is not a V collection and it doesn't contain the elements.
					return false;
				}
			}
			return true; //We didn't find any elements that weren't contained here.
		}

		/**
		 * Adding values is not supported by this Collection. This is because
		 * the Collection interface does not allow us to specify a key as well.
		 * @param c The collection with values you would want to add. Tough
		 * luck.
		 * @return This method never returns anything. It throws an
		 * <code>UnsupportedOperationException</code>.
		 * @throws UnsupportedOperationException Adding values is not supported
		 * by this Collection.
		 */
		@Override
		public boolean addAll(final Collection<? extends V> c) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Adding values is not supported by this Collection.");
		}

		/**
		 * Removes all of this Collection's elements that are also contained in
		 * the specified collection. After this call returns, this Collection
		 * will contain no elements in common with the specified collection.
		 * @param c The collection containing elements to be removed from this
		 * Collection.
		 * @return <code>True</code> if this Collection changed as a result of
		 * the call, or <code>false</code> otherwise.
		 */
		@Override
		@SuppressWarnings("unchecked") //We handle this with a try-catch block.
		public boolean removeAll(final Collection<?> c) {
			final Iterator<?> iterator = c.iterator(); //Iterate over the elements.
			boolean changed = false;
			while(iterator.hasNext()) { //Remove them one by one.
				final Object next = iterator.next();
				try { //Try to cast.
					changed = remove((V)next) || changed; //Actually remove it, if successful
				} catch(ClassCastException e) { //The collection has the wrong type.
					return false;
				}
			}
			return changed;
		}

		/**
		 * Retains only the elements in this collection that are contained in
		 * the specified collection. In other words, removes from this
		 * collection all of its elements that are not contained in the
		 * specified collection.
		 * Note that this is typically a rather expensive operation, since it
		 * iterates over this Collection and for every item checks if it is
		 * contained in the specified collection.
		 * @param c The collection containing elements to take the intersection
		 * of.
		 * @return <code>True</code> if this collection changed as a result of
		 * the call, or <code>false</code> otherwise.
		 */
		@Override
		public boolean retainAll(final Collection<?> c) {
			final Iterator<V> iterator = iterator(); //Get our own iterator.
			boolean changed = false;
			while(iterator.hasNext()) { //Iterate to see if every element is contained in the specified collection.
				final V next = iterator.next();
				if(!c.contains(next)) { //It's not in there. Remove it.
					changed = remove(next) || changed; //This is a safe operation, since our own iterator makes a copy of the collection and iterates over that.
				}
			}
			return changed;
		}

		/**
		 * Removes all of the elements from this Collection as well as its
		 * corresponding Treap. The Collection and the Treap will be empty after
		 * this method returns.
		 */
		@Override
		public void clear() {
			treap.clear();
		}
	}

	/**
	 * The root node of the Treap. Nearly all external method calls start their
	 * execution on this node.
	 */
	private Entry<K,V> root;

	/**
	 * The number of entries in the Treap.
	 */
	private int size;

	/**
	 * A pseudo-random number generator to generate new priorities for every
	 * node we add. The priorities are stored as <code>long</code>s, so this
	 * random number generator will only be used to create long integers.
	 */
	private final Random rng;

	/**
	 * A set containing all of the elements of this Treap. If this is set,
	 * changes to the Treap need to be reflected in this set.
	 */
	private EntrySet entrySet;

	/**
	 * A set containing all of the keys of this Treap. If this is set, changes
	 * to the Treap need to be reflected in this set.
	 */
	private KeySet keySet;

	/**
	 * Creates a new <code>Treap</code>. The <code>Treap</code> will be empty.
	 */
	public Treap() {
		rng = new Random();
	}

	/**
	 * Associates the specified value with the specified key in the Treap. If
	 * the Treap previously contained a value for this key, the old value is
	 * replaced and returned.
	 * @param key The key to which to identify the value by.
	 * @param value The value to store in the Treap.
	 * @return If the key was already in the Treap, the old value associated
	 * with the key is returned. Otherwise, <code>null</code> is returned. Note
	 * that a return value of <code>null</code> may indicate that the key didn't
	 * exist in the Treap, or that the key did exist but was associated with a
	 * <code>null</code> value.
	 * @throws NullPointerException The specified key is <code>null</code>.
	 */
	@Override
	public V put(final K key,final V value) throws NullPointerException {
		if(key == null) {
			throw new NullPointerException("A key of null was provided.");
		}
		final Entry<K,V> newEntry = new Entry<>(key,value,rng.nextLong()); //Create a new entry with a random priority.
		if(root == null) { //No item yet.
			root = newEntry;
			size++;
			if(entrySet != null) { //Add it to our two sets, if they are initialised.
				entrySet.add(newEntry);
			}
			if(keySet != null) {
				keySet.add(key);
			}
			return null; //No item associated with the key either.
		}
		final V result = root.add(newEntry); //Add this entry.
		if(result == null) {
			size++; //Increment the size, if this didn't overwrite any previous value.
		}

		//Add it to our two sets, if they are initialised.
		if(entrySet != null) {
			entrySet.add(newEntry);
		}
		if(keySet != null) {
			keySet.add(key);
		}

		return result;
	}

	/**
	 * Associates the specified value with the specified key in the Treap, and
	 * returns the resulting entry. If the Treap previously contained a value
	 * for this key, the old value is replaced.
	 * @param key The key to which to identify the value by.
	 * @param value The value to store in the Treap.
	 * @return An entry in the Treap that was created for you to store the
	 * specified key and value.
	 * @throws NullPointerException The specified key is <code>null</code>.
	 */
	public Entry<K,V> putAndGet(final K key,final V value) throws NullPointerException {
		if(key == null) {
			throw new NullPointerException("A key of null was provided.");
		}
		final Entry<K,V> newEntry = new Entry<>(key,value,rng.nextLong()); //Create a new entry with a random priority.
		if(root == null) { //No item yet.
			root = newEntry;
			size++;
		} else if(root.add(newEntry) == null) { //Add this entry. No returned value? Then it was new.
			size++; //Increment the size, if this didn't overwrite any previous value.
		}

		//Add it to our two sets, if they are initialised.
		if(entrySet != null) {
			entrySet.add(newEntry);
		}
		if(keySet != null) {
			keySet.add(key);
		}

		return newEntry;
	}

	/**
	 * Copies all of the entries from the specified map to this Treap. The
	 * effect of this call is equivalent to that of calling
	 * <code>put(k,v)</code> on this Treap once for each mapping from key
	 * <code>k</code> to value <code>v</code> in the specified map.
	 * <p>The behaviour of this operation is unspecified if the specified map is
	 * modified while the operation is in progress.</p>
	 * @param t The map to be copied to this Treap.
	 * @throws ClassCastException The class of a key or value in the specified
	 * map could not be cast to the class of the keys or the values in this
	 * Treap.
	 * @throws NullPointerException The specified map is <code>null</code>, or
	 * one of the keys in the map is <code>null</code>.
	 */
	@Override
	@SuppressWarnings("unchecked") //This exception is documented.
	public void putAll(final Map<? extends K,? extends V> t) throws ClassCastException,NullPointerException {
		if(t == null) {
			throw new NullPointerException("The specified map to copy to the Treap is null.");
		}
		for(final Map.Entry<? extends K,? extends V> object : t.entrySet()) {
			final Map.Entry<K,V> entry = (Map.Entry<K,V>)object; //Cast it to an Entry (throws ClassCastException if it can't).
			put(entry.getKey(),entry.getValue()); //Copy the entry to this Treap.

			//Add them to our two sets, if they are initialised.
			if(entrySet != null) {
				entrySet.add(entry);
			}
			if(keySet != null) {
				keySet.add(entry.getKey());
			}
		}
	}

	/**
	 * Returns the value to which this Treap maps the specified key. Returns
	 * <code>null</code> if the Treap does not have an entry for this key, but
	 * if the Treap returns <code>null</code> it may also mean that the key had
	 * a <code>null</code> as value attached.
	 * @param key The key to search for in the Treap.
	 * @return The value attached to the key, or <code>null</code> if the key
	 * was not found.
	 * @throws ClassCastException The key is of an inappropriate type for this
	 * map.
	 * @throws NullPointerException The specified key was <code>null</code>.
	 */
	@Override
	@SuppressWarnings("unchecked") //This exception is documented.
	public V get(final Object key) throws ClassCastException,NullPointerException {
		if(key == null) {
			throw new NullPointerException("Treaps can't contain null keys. Do not search for one.");
		}
		/*if(root == null) { //No items at all.
			return null;
		}*/

		//New, non-recursive version:
		final K k = (K)key; //Try to cast it to a key. Throws ClassCastException if this object is not a key.
		Entry<K,V> node = root;
		while(node != null) {
			final int cmp = k.compareTo(node.key);
			if(cmp < 0) {
				node = node.left;
			} else if(cmp > 0) {
				node = node.right;
			} else {
				return node.value;
			}
		}
		return null;

		/*original:
		final Entry<K,V> result = root.get((K)key); //Try to cast it to a key. Throws ClassCastException if this object is not a key.
		if(result == null) { //Not found.
			return null;
		}
		return result.getValue();*/
	}

	/**
	 * Returns the entry in the Treap that has the specified key. Returns
	 * <code>null</code> if the Treap does not have an entry for this key.
	 * @param key The key to search for in the Treap.
	 * @return The entry (key-value pair) that has the specified key, or
	 * <code>null</code> if the key was not found.
	 * @throws NullPointerException The specified key was <code>null</code>.
	 */
	public Entry<K,V> getEntry(final K key) throws NullPointerException {
		if(key == null) {
			throw new NullPointerException("Treaps can't contain null keys. Do not search for one.");
		}
		if(root == null) { //No items at all.
			return null;
		}
		return root.get(key);
	}

	/**
	 * Gets the entry after this one. The resulting entry will have a higher key
	 * than the provided entry.
	 * @param current The entry before the desired entry.
	 * @return The entry after the current entry. If this was the last entry,
	 * <code>null</code> is returned.
	 */
	public Entry<K,V> after(final Entry<K,V> current) {
		return current.after(false); //Going down, since we need to check children first.
	}

	/**
	 * Gets the entry before this one. The resulting entry will have a lower key
	 * than the provided entry.
	 * @param current The entry after the desired key.
	 * @return The entry before the current entry. If this was the last entry,
	 * <code>null</code> is returned.
	 */
	public Entry<K,V> before(final Entry<K,V> current) {
		return current.before(false); //Going down, since we need to check children first.
	}

	/**
	 * Gets the number of entries in the Treap.
	 * @return The number of entries in the Treap.
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Gets whether there are no entries in the Treap.
	 * @return <code>True</code> if there are no entries in the Treap, or
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Returns <code>true</code> if this Treap contains an entry for the
	 * specified key.
	 * @param key The key whose presence in this Treap is to be tested.
	 * @return <code>True</code> if this Treap contains an entry for the
	 * specified key, or <code>false</code> otherwise.
	 * @throws ClassCastException The key is of an inappropriate type for this
	 * map.
	 * @throws NullPointerException The specified key was <code>null</code>.
	 */
	@Override
	@SuppressWarnings("unchecked") //This exception is documented.
	public boolean containsKey(final Object key) throws ClassCastException,NullPointerException {
		if(key == null) {
			throw new NullPointerException("Treaps can't contain null keys. Do not search for one.");
		}
		if(root == null) { //No entries at all.
			return false;
		}
		return root.contains((K)key); //Try to cast it to a Key. Throws ClassCastException if this object is not a key.
	}

	/**
	 * Returns <code>true</code> if this Treap contains an entry with the
	 * specified value.
	 * @param value The value whose presence in this Treap is to be tested.
	 * @return <code>True</code> if this Treap contains an entry with the
	 * specified value, or <code>false</code> otherwise.
	 */
	@Override
	@SuppressWarnings("unchecked") //We handle this with a try-catch block.
	public boolean containsValue(final Object value) {
		if(root == null) { //No entries at all.
			return false;
		}
		try { //Try to cast.
			return root.contains((V)value);
		} catch(ClassCastException e) { //The specified object was not a value.
			return false;
		}
	}

	/**
	 * Removes the entry for this key from the Treap if it is present. It
	 * returns the value to which the Treap previously associated the key, or
	 * <code>null</code> if the Treap contained no entry for this key. A
	 * <code>null</code> return can also indicate that the Treap previously
	 * associated <code>null</code> with the specified key. The Treap will not
	 * contain a mapping for the specified key once the call returns.
	 * @param key The key whose mapping is to be removed from the Treap.
	 * @return The previous value associated with the key, or <code>null</code>
	 * if there was no entry for the key.
	 * @throws ClassCastException The key is of an inappropriate type for this
	 * map.
	 * @throws NullPointerException The specified key is <code>null</code>.
	 */
	@Override
	@SuppressWarnings("unchecked") //This exception is documented.
	public V remove(final Object key) throws ClassCastException,NullPointerException {
		if(key == null) {
			throw new NullPointerException("Treaps can't contain null keys. Do not try to remove an entry with null as key.");
		}
		if(root == null) { //No entries at all.
			return null;
		}
		final K key2 = (K)key; //Try to cast it to a key. Throws ClassCastException if this object is not a key.
		final Entry<K,V> result = root.remove(key2); //Remove the entry from the Treap.
		if(root.key.equals(key2)) //Edge case: The root can't remove itself from its parent (it has none).
			root = null;
		if(result != null)
			size--; //Reduce the size by 1 if we removed a node.

		//Remove the entries from our two sets, if they are initialized.
		if(entrySet != null && result != null)
			entrySet.remove(result);
		if(keySet != null)
			keySet.remove(key2);

		if(result == null) { //Wasn't found.
			return null;
		}
		return result.value; //Don't return an entry, but its value.
	}

	/**
	 * Removes all of the entries from this Treap. The Treap will be empty after
	 * this call returns. This method is very fast, since it doesn't have to
	 * remove all entries one-by-one, but can simply let go of the root.
	 * If there exists an Entry Set or a Key Set for this Treap however, they
	 * still have to be cleared.
	 */
	@Override
	public void clear() {
		root = null; //Removes the root. The nodes are now free to pick up by the garbage collection if no variables from outside link them any more.
		size = 0; //No entries any more in this set.
		//Clear our two sets, if they are initialized.
		if(entrySet != null) {
			entrySet.clear();
		}
		if(keySet != null) {
			keySet.clear();
		}
	}

	/**
	 * Returns a Set view of the entries contained in this Treap. The Set is
	 * backed by the Treap, so changes to the Treap are reflected in the Set,
	 * and vice-versa. If the Treap is modified while an iteration over the set
	 * is in progress (except through the iterator's own <code>remove()</code>
	 * operation, or through the <code>setValue()</code> operation on an entry
	 * returned by the iterator) the set will also be modified. The Set supports
	 * element removal, which removes the corresponding entry from the Treap,
	 * via the <code>Iterator.remove()</code>, <code>Set.remove()</code>,
	 * <code>removeAll()</code> and <code>clear()</code> operations. It does not
	 * support the <code>add()</code> or <code>addAll()</code> operations.
	 * @return A Set view of the entries contained in this Treap.
	 */
	@Override
	public Set<Map.Entry<K,V>> entrySet() {
		if(entrySet == null) { //We don't have one yet.
			entrySet = new EntrySet(this); //Create the Entry Set from this Treap.
		}
		return entrySet; //Create the Entry Set from this Treap.
	}

	/**
	 * Returns a Collection view of the values contained in this Treap. The
	 * Collection is backed by the Treap, so changes to the Treap are reflected
	 * in the Collection, and vice-versa. If the Treap is modified while an
	 * iteration over the Collection is in progress, the collection will be
	 * modified as well. The Collection supports element removal, which removes
	 * the corresponding entry from the Treap, via the
	 * <code>Iterator.remove()</code>, <code>Collection.remove()</code>,
	 * <code>removeAll()</code>, <code>retainAll()</code> and
	 * <code>clear()</code> operations. It does not support the
	 * <code>add()</code> or <code>addAll()</code> operations.
	 * @return A Collection view of the values contained in this Treap.
	 */
	@Override
	public Collection<V> values() {
		return (Collection<V>) new ValuesCollection<>(this); //Create the Values Collection from this Treap. Cast it to a Collection.
	}

	/**
	 * Returns a Set view of the keys contained in this Treap. The set is backed
	 * by the Treap, so changes to the Treap are reflected in the Set, and vice-
	 * versa. If the Treap is modified while an iteration over the set is in
	 * progress (except through the iterator's own <code>remove()</code>
	 * operation), set will also be modified. The Set supports element removal,
	 * which removes the corresponding mapping from the Treap, via the
	 * <code>Iterator.remove()</code>, <code>Set.remove()</code>,
	 * <code>removeAll()</code>, <code>retainAll()</code> and
	 * <code>clear()</code> operations. It does not support the
	 * <code>add()</code> or <code>addAll()</code> operations.
	 * @return A Set view of the keys contained in this Treap.
	 */
	@Override
	public Set<K> keySet() {
		if(keySet == null) {
			keySet = new KeySet(this); //Create the Key Set from this Treap.
		}
		return keySet; //Create the Key Set from this Treap.
	}

	/**
	 * Swaps two entries in the tree representation of the Treap. This is a
	 * dangerous operation, so be careful not to break the Treap. If you swap
	 * the keys, you MUST swap the keys' comparison values manually in order for
	 * them to maintain their sorted order!
	 * @param key1 The key of the first entry, to swap with the other entry.
	 * @param key2 The key of the second entry, to swap with the other entry.
	 * @param swapKeys <code>True</code> if you want to swap the keys in the
	 * Treap as well. If you do this, you MUST swap the keys' comparison values
	 * manually in order for them to maintain their sorted order!
	 * <code>False<code> if you don't want to swap their keys. Only the values
	 * are swapped, then.
	 * @return <code>True</code> if the swap was succesful, or
	 * <code>false</code> if either keys could not be found in the Treap.
	 */
	public boolean swap(final K key1,final K key2,final boolean swapKeys) {
		if(root == null) {
			return false;
		}
		final Entry<K,V> node1 = root.get(key1); //Get the entry of the first key.
		if(node1 == null) { //Doesn't exist? Stop here.
			return false;
		}
		final Entry<K,V> node2 = root.get(key2); //Get the entry of the second key.
		if(node2 == null) { //Doesn't exist? Stop here.
			return false;
		}
		if(swapKeys) { //If we should swap the keys too.
			//Swap the two entries entirely!
			final Entry<K,V> parent1 = node1.parent; //Save these three since they're about to be overwritten.
			final Entry<K,V> left1 = node1.left;
			final Entry<K,V> right1 = node1.right;
			node1.parent = node2.parent; //Swap the neighbours of both nodes.
			node1.left = node2.left;
			node1.right = node2.right;
			node2.parent = parent1; //Use the stored data for these three.
			node2.left = left1;
			node2.right = right1;
		} else { //Don't swap the keys. Just the values then.
			final V value1 = node1.value; //Save this since it's about to be overwritten.
			node1.value = node2.value; //Swap the values.
			node2.value = value1; //Use the stored data for this one.
		}
		return true;
	}
}