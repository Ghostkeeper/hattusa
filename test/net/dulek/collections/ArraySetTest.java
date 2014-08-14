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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 * Tests the correctness and speed of the custom {@link Set} implementation:
 * {@link ArraySet}.
 * @author Ruben Dulek
 * @version 1.0
 */
@Ignore
public class ArraySetTest {
	/**
	 * The number of times the test cases are repeated.
	 */
	private static final int numTests = 10_000;

	/**
	 * The number of elements in the sets used in the tests.
	 */
	private static final int testSize = 10_000;

	/**
	 * The number of characters to leave open for number data.
	 */
	private static final int spacing = 11;

	/**
	 * Random number generator with a pre-specified seed so tests are
	 * repeatable. Science!
	 */
	private static final Random rng = new Random(0x12345678);

	/**
	 * A randomly generated testcase of {@code testSize} length.
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
	 * specifying the correct initial capacity, so this also tests the resizing.
	 */
	@Ignore
	@Test
	public void testAdd() {
		ArraySet<Integer> me = new ArraySet<>();
		HashSet<Integer> them = new HashSet<>();
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
			ArraySet<Integer> inst = new ArraySet<>();
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
			HashSet<Integer> inst = new HashSet<>();
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
	 * Test of adding elements. Tests adding of elements to a set and specifying
	 * the correct initial capacity.
	 */
	@Ignore
	@Test
	public void testAdd2() {
		ArraySet<Integer> me = new ArraySet<>(testSize);
		HashSet<Integer> them = new HashSet<>(testSize);
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
			ArraySet<Integer> inst = new ArraySet<>(testSize);
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
			HashSet<Integer> inst = new HashSet<>(testSize);
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
	 * Tests adding collections to the set.
	 */
	@Ignore
	@Test
	public void testAddAll() {
		ArraySet<Integer> me = new ArraySet<>(testSize);
		HashSet<Integer> them = new HashSet<>(testSize);
		me.addAll(randomTest);
		them.addAll(randomTest);
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("AddAll:                       ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			ArraySet<Integer> inst = new ArraySet<>(testSize);
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
			HashSet<Integer> inst = new HashSet<>(testSize);
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
	 * Tests clearing the sets of all elements.
	 */
	@Ignore
	@Test
	public void testClear() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
		me.clear();
		them.clear();
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("Clear:                        ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			ArraySet<Integer> inst = new ArraySet<>(randomTest);
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
			HashSet<Integer> inst = new HashSet<>(randomTest);
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
	 * Tests making a clone of the set.
	 */
	@Ignore
	@SuppressWarnings("unchecked") //Caused by casting the cloned HashSet back to HashSet.
	@Test
	public void testClone() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
		ArraySet<Integer> meClone = me.clone();
		HashSet<Integer> themClone = (HashSet<Integer>)them.clone();
		assertEquals(meClone,themClone);

		//////////////////------------------------------///
		System.out.print("Clone:                        ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			me.clone();
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
	 * Tests checking the set for containment of elements.
	 */
	@Ignore
	@Test
	public void testContains() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
		for(int i = testSize - 1;i >= 0;i--) {
			assertEquals(me.contains(randomTest.get(i)),them.contains(randomTest.get(i)));
			assertEquals(me.contains(randomTest.get(i) + 1),them.contains(randomTest.get(i) + 1));
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
	 * Tests checking the set for containment of subsets.
	 */
	@Ignore
	@Test
	public void testContainsAll() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
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
	 * Tests computing the hash code of sets.
	 */
	@Ignore
	@Test
	public void testHashCode() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
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
	 * Tests running through the iterator of the sets.
	 */
	@Ignore
	@Test
	public void testIterator() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
		ArrayList<Integer> meList = new ArrayList<>(testSize);
		ArrayList<Integer> themList = new ArrayList<>(testSize);
		Iterator<Integer> meIt = me.iterator();
		while(meIt.hasNext()) {
			meList.add(meIt.next());
		}
		Iterator<Integer> themIt = them.iterator();
		while(themIt.hasNext()) {
			themList.add(themIt.next());
		}
		Collections.sort(meList);
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
	 * Tests removing elements from the sets.
	 */
	@Ignore
	@Test
	public void testRemove() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
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
			ArraySet<Integer> inst = new ArraySet<>(randomTest);
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
			HashSet<Integer> inst = new HashSet<>(randomTest);
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
	 * Tests removing subsets from the sets.
	 */
	@Ignore
	@Test
	public void testRemoveAll() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
		List<Integer> halfRandomTest = randomTest.subList(0,(int)(testSize * 0.4));
		me.removeAll(halfRandomTest);
		them.removeAll(halfRandomTest);
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("RemoveAll:                    ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			ArraySet<Integer> inst = new ArraySet<>(randomTest);
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
			HashSet<Integer> inst = new HashSet<>(randomTest);
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
	 * Tests retaining subsets of the sets.
	 */
	@Ignore
	@Test
	public void testRetainAll() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
		List<Integer> halfRandomTest = randomTest.subList(0,(int)(testSize * 0.4));
		me.retainAll(halfRandomTest);
		them.retainAll(halfRandomTest);
		assertEquals(me,them);

		//////////////////------------------------------///
		System.out.print("RetainAll:                    ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			ArraySet<Integer> inst = new ArraySet<>(randomTest);
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
			HashSet<Integer> inst = new HashSet<>(randomTest);
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
	 * Tests listing all elements of the set into an array.
	 */
	@Ignore
	@Test
	public void testToArray() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
		Object[] meArr = me.toArray();
		Object[] themArr = them.toArray();
		Arrays.sort(meArr);
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
	 * Tests listing all elements of the set into an array. The generic type
	 * version of {@link ArraySet#toArray} will be used, but an empty array is
	 * provided (so the set will have to allocate a new array).
	 */
	@Ignore
	@Test
	public void testToArrayGeneric() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
		Integer[] meArr = me.toArray(new Integer[0]);
		Integer[] themArr = them.toArray(new Integer[0]);
		Arrays.sort(meArr);
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
	 * Tests listing all elements of the set into an array. The generic type
	 * version of {@link ArraySet#toArray} will be used, and the array provided
	 * will have sufficient size so that no new array need be allocated.
	 */
	@Ignore
	@Test
	public void testToArrayGenericSized() {
		ArraySet<Integer> me = new ArraySet<>(randomTest);
		HashSet<Integer> them = new HashSet<>(randomTest);
		Integer[] meArr = me.toArray(new Integer[me.size()]);
		Integer[] themArr = them.toArray(new Integer[them.size()]);
		Arrays.sort(meArr);
		Arrays.sort(themArr);
		assertArrayEquals(meArr,themArr);

		//////////////////------------------------------///
		System.out.print("ToArray with correct array:   ");
		long meTime = 0;
		long themTime = 0;
		for(int r = numTests - 1;r >= 0;r--) {
			System.gc();
			long before = System.nanoTime();
			me.toArray(new Integer[randomTest.size()]);
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
			them.toArray(new Integer[randomTest.size()]);
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