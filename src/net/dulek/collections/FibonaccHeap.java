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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import net.dulek.collections.FibonaccHeap.Element;

/**
 * A priority queue implementation based on a Fibonacci Heap. The elements of
 * the queue are ordered by the natural ordering of their keys, or by a
 * {@link Comparator} provided in the constructor, if any. The heap permits
 * {@code null} only in the element's value, not in its key.
 * <p>The head of this queue is the lowest element with respect to the specified
 * ordering of the keys. If multiple elements are tied for the lowest value, the
 * head may be any of these elements. No order can be guaranteed in those cases.
 * The operations {@link #add(Element)}, {@link #element()},
 * {@link #offer(Element)}, {@link #peek()}, {@link #poll()} and
 * {@link #remove()} all apply at the head of the queue.</p>
 * <p>Note that this implementation is not synchronized. Multiple threads should
 * not access a {@code FibonacciHeap} instance concurrently if any of the
 * threads modifies the queue.</p>
 * <p>This implementation, due to it being a Fibonacci Heap, allows for constant
 * insertion, constant peeking, amortised constant reduction of keys, and
 * amortised logarithmic dequeueing.</p>
 * @author Ruben Dulek
 * @version 2.0
 * @param <K>
 * @param <V>
 */
public class FibonaccHeap<K extends Comparable<K>,V> implements Queue<Element<K,V>>,Serializable {

	public static class Element<K extends Comparable<K>,V> implements Comparable<K> {

		@Override
		public int compareTo(K o) {
			throw new UnsupportedOperationException("Not implemented yet.");
		}

	}

	@Override
	public boolean add(Element<K,V> e) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean offer(Element<K,V> e) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public Element<K,V> remove() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public Element<K,V> poll() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public Element<K,V> element() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public Element<K,V> peek() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public Iterator<Element<K,V>> iterator() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean addAll(Collection<? extends Element<K,V>> c) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}
}
