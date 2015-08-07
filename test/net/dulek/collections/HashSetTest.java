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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 * Tests the correctness and speed of the custom {@link Set} implementation
 * {@link HashSet}.
 * @author Ruben Dulek
 * @version 1.0
 */
@Ignore
public class HashSetTest {
	/**
	 * Number of times to repeat the tests.
	 */
	private static final int numTests = 1000;

	/**
	 * How big the tests are supposed to be. This often indicates the number of
	 * elements put into the sets.
	 */
	private static final int testSize = 100_000;

	/**
	 * Space for the me-time.
	 */
	private static final int spacing = 11;

	/**
	 * A random number generator with fixed seed to test.
	 */
	private static final Random rng = new Random(0x12345678);

	/**
	 * A random test case of {@code testSize} long.
	 */
	private static final ArrayList<Integer> randomTest;

	static {
		randomTest = new ArrayList<>(testSize);
		for(int i = testSize - 1;i >= 0;i--) {
			randomTest.add(rng.nextInt() % testSize);
		}
	}

	/**
	 * Test of adding elements. Tests adding of elements to a set without
	 * specifying the correct initial capacity, so this also tests the resizing
	 * and rehashing.
	 */
	@Ignore
	@Test
	public void testAdd() {
		HashSet<Integer> me = new HashSet<>();
		java.util.HashSet<Integer> them = new java.util.HashSet<>();
		for(int i = testSize - 1;i >= 0;i--) {
			me.add(randomTest.get(i));
			them.add(randomTest.get(i));
		}
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("Add without initial capacity: ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			HashSet<Integer> inst = new HashSet<>();
			System.gc();
			long before = System.nanoTime();
			for(int i = testSize - 1;i >= 0;i--) {
				inst.add(randomTest.get(i));
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			java.util.HashSet<Integer> inst = new java.util.HashSet<>();
			System.gc();
			long before = System.nanoTime();
			for(int i = testSize - 1;i >= 0;i--) {
				inst.add(randomTest.get(i));
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Test of add method, of class HashSet. Tests adding of elements to a set
	 * and specifying the correct initial capacity.
	 */
	@Ignore
	@Test
	public void testAdd2() {
		HashSet<Integer> me = new HashSet<>(testSize);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(testSize);
		for(int i = testSize - 1;i >= 0;i--) {
			me.add(randomTest.get(i));
			them.add(randomTest.get(i));
		}
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("Add with initial capacity:    ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			HashSet<Integer> inst = new HashSet<>(testSize);
			System.gc();
			long before = System.nanoTime();
			for(int i = testSize - 1;i >= 0;i--) {
				inst.add(randomTest.get(i));
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			java.util.HashSet<Integer> inst = new java.util.HashSet<>(testSize);
			System.gc();
			long before = System.nanoTime();
			for(int i = testSize - 1;i >= 0;i--) {
				inst.add(randomTest.get(i));
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests adding entire collections to the set.
	 */
	@Ignore
	@Test
	public void testAddAll() {
		HashSet<Integer> me = new HashSet<>(testSize);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(testSize);
		me.addAll(randomTest);
		them.addAll(randomTest);
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("AddAll:                       ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			HashSet<Integer> inst = new HashSet<>(testSize);
			System.gc();
			long before = System.nanoTime();
			inst.addAll(randomTest);
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			java.util.HashSet<Integer> inst = new java.util.HashSet<>(testSize);
			System.gc();
			long before = System.nanoTime();
			inst.addAll(randomTest);
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests clearing the set.
	 */
	@Ignore
	@Test
	public void testClear() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		me.clear();
		them.clear();
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("Clear:                        ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			HashSet<Integer> inst = new HashSet<>(randomTest);
			System.gc();
			long before = System.nanoTime();
			inst.clear();
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			java.util.HashSet<Integer> inst = new java.util.HashSet<>(randomTest);
			System.gc();
			long before = System.nanoTime();
			inst.clear();
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests cloning the set.
	 */
	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void testClone() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		HashSet<Integer> meClone;
		try {
			meClone = me.clone();
		} catch(CloneNotSupportedException e) {
			assertTrue(false);
			return;
		}
		java.util.HashSet<Integer> themClone = (java.util.HashSet<Integer>)them.clone();
		assertEquals(meClone,themClone);

		//////////////////------------------------------///
		System.out.print("Clone:                        ");
		long meTime = 0;
		long themTime = 0;
		try {
			for(int r = numTests - 1;r >= 0;r--) {
				System.gc();
				long before = System.nanoTime();
				me.clone();
				long after = System.nanoTime();
				meTime += after - before;
			}
		} catch(CloneNotSupportedException e) { //Can't happen. If it's not supported, it should already be returned above.
			return;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			them.clone();
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests checking the set for the containment of an element.
	 */
	@Ignore
	@Test
	public void testContains() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		for(int i = testSize - 1;i >= 0;i--) {
			assertEquals(me.contains(randomTest.get(i)),them.contains(randomTest.get(i)));
		}

		//////////////////------------------------------///
		System.out.print("Contains:                     ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			for(int i = testSize - 1;i >= 0;i--) {
				me.contains(randomTest.get(i));
				me.contains(randomTest.get(i) + 1);
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			for(int i = testSize - 1;i >= 0;i--) {
				them.contains(randomTest.get(i));
				them.contains(randomTest.get(i) + 1);
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests checking whether a set contains all elements of a collection.
	 */
	@Ignore
	@Test
	public void testContainsAll() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		assertEquals(me.containsAll(randomTest),them.containsAll(randomTest));

		//////////////////------------------------------///
		System.out.print("ContainsAll:                  ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			me.containsAll(randomTest);
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			them.containsAll(randomTest);
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests the computing of the hash code.
	 */
	@Ignore
	@Test
	public void testHashCode() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		assertEquals(me.hashCode(),them.hashCode());

		//////////////////------------------------------///
		System.out.print("HashCode:                     ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			me.hashCode();
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			them.hashCode();
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests working with the iterator. This test simply iterates over all
	 * elements in the set.
	 */
	@Ignore
	@Test
	public void testIterator() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		ArrayList<Integer> meList = new ArrayList<>(testSize);
		ArrayList<Integer> themList = new ArrayList<>(testSize);
		Iterator<Integer> meIt = me.iterator();
		Iterator<Integer> themIt = them.iterator();
		while(meIt.hasNext()) {
			meList.add(meIt.next());
		}
		while(themIt.hasNext()) {
			themList.add(themIt.next());
		}
		Collections.sort(meList); //Take the order out of the equation
		Collections.sort(themList);
		assertEquals(meList,themList);

		//////////////////------------------------------///
		System.out.print("Iterator:                     ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			Iterator<Integer> it = me.iterator();
			System.gc();
			long before = System.nanoTime();
			while(it.hasNext()) {
				it.next();
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			Iterator<Integer> it = them.iterator();
			System.gc();
			long before = System.nanoTime();
			while(it.hasNext()) {
				it.next();
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests removing elements from the set.
	 */
	@Ignore
	@Test
	public void testRemove() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		for(int i = testSize >> 1;i >= 0;i--) {
			me.remove(randomTest.get(i));
			them.remove(randomTest.get(i));
		}
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("Remove:                       ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			HashSet<Integer> inst = new HashSet<>(randomTest);
			System.gc();
			long before = System.nanoTime();
			for(int i = testSize - 1;i >= 0;i--) {
				inst.remove(randomTest.get(i));
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			java.util.HashSet<Integer> inst = new java.util.HashSet<>(randomTest);
			System.gc();
			long before = System.nanoTime();
			for(int i = testSize - 1;i >= 0;i--) {
				inst.remove(randomTest.get(i));
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests removing all items of a collection from the set.
	 */
	@Ignore
	@Test
	public void testRemoveAll() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		ArrayList<Integer> halfRandomTest = new ArrayList<>();
		for(int i = (int)(testSize * 0.4);i >= 0;i--) {
			halfRandomTest.add(randomTest.get(i));
		}
		me.removeAll(halfRandomTest);
		them.removeAll(halfRandomTest);
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("RemoveAll:                    ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			HashSet<Integer> inst = new HashSet<>(randomTest);
			System.gc();
			long before = System.nanoTime();
			inst.removeAll(halfRandomTest);
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			java.util.HashSet<Integer> inst = new java.util.HashSet<>(randomTest);
			System.gc();
			long before = System.nanoTime();
			inst.removeAll(halfRandomTest);
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests removing all elements except those in a collection.
	 */
	@Ignore
	@Test
	public void testRetainAll() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		ArrayList<Integer> halfRandomTest = new ArrayList<>();
		for(int i = (int)(testSize * 0.4);i >= 0;i--) {
			halfRandomTest.add(randomTest.get(i));
		}
		me.retainAll(halfRandomTest);
		them.retainAll(halfRandomTest);
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("RetainAll:                    ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			HashSet<Integer> inst = new HashSet<>(randomTest);
			System.gc();
			long before = System.nanoTime();
			inst.retainAll(halfRandomTest);
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			java.util.HashSet<Integer> inst = new java.util.HashSet<>(randomTest);
			System.gc();
			long before = System.nanoTime();
			inst.retainAll(halfRandomTest);
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests converting the set to an array.
	 */
	@Ignore
	@Test
	public void testToArray() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		Object[] meArr = me.toArray();
		Object[] themArr = them.toArray();
		Arrays.sort(meArr); //Take the order out of the equation.
		Arrays.sort(themArr);
		assertArrayEquals(meArr,themArr);

		//////////////////------------------------------///
		System.out.print("ToArray:                      ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			me.toArray();
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			them.toArray();
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests converting the set to an array with the generic type parameter.
	 */
	@Ignore
	@Test
	public void testToArrayGeneric() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		Integer[] meArr = me.toArray(new Integer[0]);
		Integer[] themArr = them.toArray(new Integer[0]);
		Arrays.sort(meArr); //Take the order out of the equation.
		Arrays.sort(themArr);
		assertArrayEquals(meArr,themArr);

		//////////////////------------------------------///
		System.out.print("ToArray with generics:        ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			me.toArray(new Integer[0]);
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			them.toArray(new Integer[0]);
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * Tests converting the set to an array with the generic type parameter and
	 * providing an array of the correct size.
	 */
	@Ignore
	@Test
	public void testToArrayGenericSized() {
		HashSet<Integer> me = new HashSet<>(randomTest);
		java.util.HashSet<Integer> them = new java.util.HashSet<>(randomTest);
		Integer[] meArr = me.toArray(new Integer[me.size()]);
		Integer[] themArr = them.toArray(new Integer[them.size()]);
		Arrays.sort(meArr); //Take the order out of the equation.
		Arrays.sort(themArr);
		assertArrayEquals(meArr,themArr);

		//////////////////------------------------------///
		System.out.print("ToArray with correct array:   ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			me.toArray(new Integer[testSize]);
			long after = System.nanoTime();
			meTime += after - before;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			them.toArray(new Integer[testSize]);
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}

	/**
	 * A bigger test to test generic usage of the set, by adding and removing
	 * elements sequentially. This is supposed to test all functions of the set.
	 * It is also the only test that tests whether methods other than remove()
	 * can actually handle tombstones.
	 */
	@Ignore
	@Test
	public void testUsage() {
		HashSet<Integer> me = new HashSet<>();
		java.util.HashSet<Integer> them = new java.util.HashSet<>();
		for(int i = testSize - 1;i >= 0;i--) {
			me.add(randomTest.get(i));
			them.add(randomTest.get(i));
		}
		for(int i = testSize - 1;i >= testSize >> 1;i--) { //Remove half of the items.
			assertEquals(me.remove(randomTest.get(i)),them.remove(randomTest.get(i)));
		}
		assertEquals(me,them);
		for(int i = testSize - 1;i > 0;i--) { //Test if the contains() method still says they're the same.
			assertEquals(me.contains(randomTest.get(i)),them.contains(randomTest.get(i)));
		}
		for(int i = testSize - 1;i > 0;i--) { //Add all of them (including the ones already there).
			assertEquals(me.add(randomTest.get(i)),them.add(randomTest.get(i)));
		}
		assertEquals(me,them);
		assertEquals(me.addAll(randomTest),them.addAll(randomTest)); //Shouldn't add any more items (test if false == false).
		me.clear();
		them.clear();
		assertEquals(me.isEmpty(),them.isEmpty());
		assertEquals(me.addAll(randomTest),them.addAll(randomTest)); //Re-add all items (test if true == true).
		Iterator<Integer> meIt = me.iterator();
		Iterator<Integer> themIt = them.iterator();
		while(meIt.hasNext()) {
			int n = meIt.next();
			if(n % 2 == 0) { //Remove all even numbers.
				meIt.remove();
			}
		}
		while(themIt.hasNext()) {
			int n = themIt.next();
			if(n % 2 == 0) { //Remove all even numbers.
				themIt.remove();
			}
		}
		assertEquals(me,them);
		ArrayList<Integer> meList = new ArrayList<>(testSize);
		ArrayList<Integer> themList = new ArrayList<>(testSize);
		meIt = me.iterator();
		themIt = them.iterator();
		while(meIt.hasNext()) {
			meList.add(meIt.next());
		}
		while(themIt.hasNext()) {
			themList.add(themIt.next());
		}
		Collections.sort(meList);
		Collections.sort(themList);
		assertEquals(meList,themList);
		try {
			assertEquals(me.clone(),them.clone());
		} catch(CloneNotSupportedException e) {
			assertTrue(false);
		}
		assertEquals(me.containsAll(randomTest),them.containsAll(randomTest)); //false == false
		assertEquals(me.hashCode(),them.hashCode());
		Object[] meArrObj = me.toArray();
		Object[] themArrObj = them.toArray();
		Arrays.sort(meArrObj);
		Arrays.sort(themArrObj);
		assertArrayEquals(meArrObj,themArrObj);
		Integer[] meArrInt = me.toArray(new Integer[0]);
		Integer[] themArrInt = them.toArray(new Integer[0]);
		Arrays.sort(meArrInt);
		Arrays.sort(themArrInt);
		assertArrayEquals(meArrInt,themArrInt);
		meArrInt = me.toArray(new Integer[me.size()]);
		themArrInt = them.toArray(new Integer[them.size()]);
		Arrays.sort(meArrInt);
		Arrays.sort(themArrInt);
		assertArrayEquals(meArrInt,themArrInt);
		assertEquals(me.removeAll(randomTest),them.removeAll(randomTest));
		assertEquals(me,them);
		me.addAll(randomTest);
		them.addAll(randomTest);
		assertEquals(me,them);
		assertEquals(me.retainAll(randomTest),them.retainAll(randomTest));
		java.util.HashSet<Integer> randomSet = new java.util.HashSet<>(randomTest);
		assertEquals(me.retainAll(randomSet),them.retainAll(randomSet));
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("Huge usage test:              ");
		long meTime = 0;
		long themTime = 0;
		try {
			for(int r = numTests - 1;r >= 0;r--) {
				System.gc();
				long before = System.nanoTime();
				me = new HashSet<>();
				for(int i = testSize - 1;i >= 0;i--) {
					me.add(randomTest.get(i));
				}
				for(int i = testSize - 1;i >= testSize >> 1;i--) { //Remove half of the items.
					me.remove(randomTest.get(i));
				}
				for(int i = testSize - 1;i > 0;i--) { //Call contains() on all of them.
					me.contains(randomTest.get(i));
				}
				for(int i = testSize - 1;i > 0;i--) { //Add all of them (including the ones already there).
					me.add(randomTest.get(i));
				}
				me.addAll(randomTest); //Shouldn't add any more items.
				me.clear();
				me.isEmpty();
				me.addAll(randomTest); //Re-add all items.
				meIt = me.iterator();
				while(meIt.hasNext()) {
					int n = meIt.next();
					if(n % 2 == 0) { //Remove all even numbers.
						meIt.remove();
					}
				}
				meIt = me.iterator();
				while(meIt.hasNext()) {
					meIt.next();
				}
				me.clone();
				me.containsAll(randomTest);
				me.hashCode();
				me.toArray();
				me.toArray(new Integer[0]);
				me.toArray(new Integer[me.size()]);
				me.removeAll(randomTest);
				me.addAll(randomTest);
				me.retainAll(randomTest);
				me.retainAll(randomSet);
				long after = System.nanoTime();
				meTime += after - before;
			}
		} catch(CloneNotSupportedException e) { //Shouldn't happen. It would've returned earlier.
			return;
		}
		String meTimeStr = Double.toString(meTime / 1E9);
		System.out.print(meTimeStr);
		System.out.print("s (me)");
		for(int i = spacing - meTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.print(" vs ");
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			them = new java.util.HashSet<>();
			for(int i = testSize - 1;i >= 0;i--) {
				them.add(randomTest.get(i));
			}
			for(int i = testSize - 1;i >= testSize >> 1;i--) { //Remove half of the items.
				them.remove(randomTest.get(i));
			}
			for(int i = testSize - 1;i > 0;i--) { //Call contains() on all of them.
				them.contains(randomTest.get(i));
			}
			for(int i = testSize - 1;i > 0;i--) { //Add all of them (including the ones already there).
				them.add(randomTest.get(i));
			}
			them.addAll(randomTest); //Shouldn't add any more items.
			them.clear();
			them.isEmpty();
			them.addAll(randomTest); //Re-add all items.
			themIt = them.iterator();
			while(themIt.hasNext()) {
				int n = themIt.next();
				if(n % 2 == 0) { //Remove all even numbers.
					themIt.remove();
				}
			}
			themIt = me.iterator();
			while(themIt.hasNext()) {
				themIt.next();
			}
			them.clone();
			them.containsAll(randomTest);
			them.hashCode();
			them.toArray();
			them.toArray(new Integer[0]);
			them.toArray(new Integer[them.size()]);
			them.removeAll(randomTest);
			them.addAll(randomTest);
			them.retainAll(randomTest);
			them.retainAll(randomSet);
			long after = System.nanoTime();
			themTime += after - before;
		}
		String themTimeStr = Double.toString(themTime / 1E9);
		System.out.print(themTimeStr);
		System.out.print("s (them)");
		for(int i = spacing + 2 - themTimeStr.length();i >= 0;i--) System.out.print(' ');
		System.out.println(" FACTOR: " + (1.0 * themTime / meTime));
	}
}