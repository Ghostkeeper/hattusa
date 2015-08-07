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

import java.util.Objects;

/**
 * A general-purpose pair class. This class holds exactly two values of any
 * generic type. The pair is order-dependent, meaning that the order in which
 * the elements are entered makes a difference in getting the elements back as
 * well as equality and hash code checks on the pair.
 * <p>The pair is immutable, allowing only read-access to its values. However,
 * no guarantee can be made for the internal (deep) content of the classes
 * stored in this pair to be immutable too.</p>
 * @author Ruben Dulek
 * @param <A> The type of the first element of the pair.
 * @param <B> the type of the second element of the pair.
 * @version 1.0
 */
public class Pair<A,B> {
	/**
	 * The first element of the pair.
	 */
	public final A first;

	/**
	 * The second element of the pair.
	 */
	public final B second;

	/**
	 * Creates a new pair with the specified pair of values.
	 * @param first The first element of the pair.
	 * @param second The second element of the pair.
	 */
	public Pair(final A first,final B second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Indicates whether the specified object is equal to this pair. It is only
	 * equal if the specified object is also a pair and both elements are equal
	 * to the respective elements of this pair.
	 * @param obj The object to compare with for equality.
	 * @return {@code true} if the specified object is equal to this pair, or
	 * {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Pair)) { //Other element is not a pair.
			return false;
		}
		@SuppressWarnings("unchecked") //Caused by the generic cast, which is always possible because it's cast to Object.
		final Pair<Object,Object> other = (Pair<Object,Object>)obj;
		return first.equals(other.first) && second.equals(other.second); //Both elements must be equal.
	}

	/**
	 * Computes a hash code for this pair. The hash codes of two pairs are
	 * guaranteed to be equal if their {@link #equals} methods return
	 * {@code true} when compared with each other. If they are not equal, no
	 * guarantee is made that their hash codes will be different.
	 * @return A hash code for this pair.
	 */
	@Override
	public int hashCode() {
		//This uses multiplication by primes in order to create orthogonal prime factorisations, which results in better distribution across most hash tables.
		int hash = 7 + first.hashCode();
		return 31 * hash + second.hashCode();
	}
}
