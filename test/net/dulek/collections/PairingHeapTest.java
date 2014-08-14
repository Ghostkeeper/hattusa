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

import java.util.*;
import net.dulek.collections.HashSet;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the correctness and speed of the {@link PairingHeap}, and compares it
 * to the default heap implementation of Java, {@link PriorityQueue}.
 * @author Ruben Dulek
 * @version 1.0
 */
@Ignore
public class PairingHeapTest {
	/**
	 * Number of times to repeat the tests. All results are averaged.
	 */
	private static final int numTests = 1000;

	/**
	 * The numbers {@code 0} up to {@code testSize - 1}, ordered randomly. This
	 * can be used, for instance, to make random extractions from the heaps.
	 */
	private static final ArrayList<Integer> randomOrder;

	/**
	 * A random test case of {@code testSize} long. The elements are pre-wrapped
	 * in Double instances.
	 */
	private static final ArrayList<Double> randomTest;

	/**
	 * A random test case of {@code testSize} long. These elements are wrapped
	 * in {@link Elem} instances, which are simple key-value pairs. Of these
	 * {@code Elem}s, the keys can be decreased separately, such that they can
	 * still be identified in a {@code PriorityQueue} even though their keys
	 * were changed.
	 */
	private static final ArrayList<Elem> randomTestElems;

	/**
	 * A random number generator with fixed seed to be able to repeat tests.
	 */
	private static final Random rng = new Random(0x12345678);

	/**
	 * Additional spins (made in an as-fast-as-possible countdown) to perform
	 * after the garbage collection and before each time measurement. This makes
	 * measurements slightly more reliable by allowing processes in other
	 * threads (such as the garbage collector) to finish.
	 */
	private static final int spins = 10_000;

	/**
	 * The size of the heaps to create, or if this is not applicable, anything
	 * else that would be a variable in a complexity analysis of a test.
	 */
	private static final int testSize = 1000;

	/**
	 * A pre-generated test value.
	 */
	private static final String value = "test";

	static { //Fill the randomTest with a random test case, and randomOrder with a random order.
		randomTest = new ArrayList<>(testSize);
		randomOrder = new ArrayList<>(testSize);
		randomTestElems = new ArrayList<>(testSize);
		for(int i = testSize - 1;i >= 0;i--) {
			randomTest.add(rng.nextDouble() * testSize);
			randomOrder.add(i);
			randomTestElems.add(new Elem(rng.nextDouble() * testSize,value));
		}
		Collections.shuffle(randomOrder,rng);
	}

	/**
	 * Test of the clear method on its own. The speed is compared against
	 * clearing a {@code PriorityQueue} of equal size. For this speed
	 * comparison, the next garbage collection is also included. This could
	 * produce some pretty unreliable results, but otherwise the comparison is
	 * unfair. The unreliable results could be combatted by more repeats.
	 */
	@Ignore
	@Test
	public void clear() {
		System.out.print("Clear:                        ");

		//Correctness.
		PairingHeap<Double,String> me = new PairingHeap<>();
		for(int i = testSize - 1;i >= 0;i--) {
			me.insert(randomTest.get(i),value);
		}
		me.clear();
		assertEquals(me.size(),0);
		assertEquals(me.findMin(),null);

		long meTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			PairingHeap<Double,String> inst = new PairingHeap<>();
			for(int i = testSize - 1;i >= 0;i--) {
				inst.insert(randomTest.get(i),value);
			}
			System.gc();
			spin();
			long before = System.nanoTime();
			inst.clear();
			System.gc();
			long after = System.nanoTime();
			meTime += after - before;
		}
		printMe(meTime);
		long themTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			PriorityQueue<Double> inst = new PriorityQueue<>(randomTest);
			System.gc();
			spin();
			long before = System.nanoTime();
			inst.clear();
			System.gc();
			long after = System.nanoTime();
			themTime += after - before;
		}
		printThem(meTime,themTime);
	}

	/**
	 * Test of decreaseKey. The speed is compared against the only equivalent in
	 * {@code PriorityQueue}: Deleting the element and re-inserting it with a
	 * lower key. Note that this is quite an unfair comparison on its own as the
	 * only complexity for the {@code PairingHeap} here is in the next call to
	 * {@link delete(Element)} or {@link deleteMin()}, and these methods are not
	 * measured here. For a better comparison, test it with Dijkstra's algorithm
	 * or better yet, the actual algorithm you would use the data structure for.
	 */
	//@Ignore
	@Test
	public void decreaseKey() {
		System.out.println("DecreaseKey:                  ");

		//Correctness.
		PairingHeap<Double,String> me = new PairingHeap<>();
		for(int i = testSize - 1;i >= 0;i--) {
			me.insert(randomTest.get(i),value);
		}
		for(PairingHeap<Double,String>.Element element : me.entrySet()) {
			Double originalKey = element.getKey();
			me.decreaseKey(element,-1.0);
			assertEquals(me.findMin(),element);
			me.changeKey(element,originalKey); //Change it back.
		}

		System.out.println("Correctness done.");

		long meTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			PairingHeap<Double,String> inst = new PairingHeap<>();
			for(int i = testSize - 1;i >= 0;i--) {
				inst.insert(randomTest.get(i),value);
			}
			System.gc();
			spin();
			long before = System.nanoTime();
			for(PairingHeap<Double,String>.Element element : inst.entrySet()) {
				me.decreaseKey(element,-1.0);
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		printMe(meTime);
		long themTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			PriorityQueue<Elem> inst = new PriorityQueue<>(randomTestElems);
			System.gc();
			spin();
			long before = System.nanoTime();
			Elem[] elems = new Elem[testSize];
			for(Elem elem : inst.toArray(elems)) {
				inst.remove(elem);
				elem.key = -1.0;
				inst.add(elem);
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		printThem(meTime,themTime);
	}

	/**
	 * Test of the deleteMin method on its own. The speed is compared against
	 * the {@code PriorityQueue}'s poll method.
	 */
	@Ignore
	@Test
	public void deleteMin() {
		System.out.print("DeleteMin:                    ");

		//Correctness.
		PairingHeap<Double,String> me = new PairingHeap<>();
		for(int i = testSize - 1;i >= 0;i--) {
			me.insert(randomTest.get(i),value);
		}
		Double[] meSorted = new Double[testSize];
		for(int i = testSize - 1;i >= 0;i--) {
			meSorted[i] = me.deleteMin().getKey();
		}
		PriorityQueue<Double> them = new PriorityQueue<>(randomTest);
		Double[] themSorted = new Double[testSize];
		for(int i = testSize - 1;i >= 0;i--) {
			themSorted[i] = them.poll();
		}
		assertArrayEquals(meSorted,themSorted);

		long meTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			PairingHeap<Double,String> inst = new PairingHeap<>();
			for(int i = testSize - 1;i >= 0;i--) {
				inst.insert(randomTest.get(i),value);
			}
			System.gc();
			spin();
			long before = System.nanoTime();
			while(!inst.isEmpty()) {
				inst.deleteMin();
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		printMe(meTime);
		long themTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			PriorityQueue<Double> inst = new PriorityQueue<>(randomTest);
			System.gc();
			spin();
			long before = System.nanoTime();
			while(!inst.isEmpty()) {
				inst.poll();
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		printThem(meTime,themTime);
	}

	/**
	 * Test of the insert method on its own. The speed is compared against the
	 * {@code PriorityQueue} when inserting with an initial capacity specified.
	 * <p>Note that for fair comparison, the construction of the heaps is also
	 * included in the measurement.</p>
	 */
	@Ignore
	@Test
	public void insertInitialCapacity() {
		System.out.print("Insert (initial capacity):    ");

		//Correctness.
		PairingHeap<Double,String> me = new PairingHeap<>();
		for(int i = testSize - 1;i >= 0;i--) {
			me.insert(randomTest.get(i),value);
		}
		assertEquals(me.size(),testSize);

		long meTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			int i = testSize - 1;
			System.gc();
			spin();
			long before = System.nanoTime();
			PairingHeap<Double,String> inst = new PairingHeap<>();
			for(;i >= 0;i--) {
				inst.insert(randomTest.get(i),value);
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		printMe(meTime);
		long themTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			int i = testSize - 1;
			System.gc();
			spin();
			long before = System.nanoTime();
			PriorityQueue<Double> inst = new PriorityQueue<>(testSize);
			for(;i >= 0;i--) {
				inst.add(randomTest.get(i));
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		printThem(meTime,themTime);
	}

	/**
	 * Test of the insert method on its own. The speed is compared against the
	 * {@code PriorityQueue} when inserting without specifying an initial
	 * capacity.
	 * <p>Note that for fair comparison, the construction of the heaps is also
	 * included in the measurement.</p>
	 */
	@Ignore
	@Test
	public void insertNoInitialCapacity() {
		System.out.print("Insert (no initial capacity): ");

		//Correctness.
		PairingHeap<Double,String> me = new PairingHeap<>();
		for(int i = testSize - 1;i >= 0;i--) {
			me.insert(randomTest.get(i),value);
		}
		assertEquals(me.size(),testSize);

		long meTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			int i = testSize - 1;
			System.gc();
			spin();
			long before = System.nanoTime();
			PairingHeap<Double,String> inst = new PairingHeap<>();
			for(;i >= 0;i--) {
				inst.insert(randomTest.get(i),value);
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		printMe(meTime);
		long themTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			int i = testSize - 1;
			System.gc();
			spin();
			long before = System.nanoTime();
			PriorityQueue<Double> inst = new PriorityQueue<>();
			for(;i >= 0;i--) {
				inst.add(randomTest.get(i));
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		printThem(meTime,themTime);
	}

	/**
	 * Test of the iterator. The speed of an iteration is tested against the
	 * speed of an iteration of the {@code PriorityQueue}.
	 */
	@Ignore
	@Test
	public void iterator() {
		System.out.print("Iterator:                     ");

		//Correctness.
		PairingHeap<Double,String> me = new PairingHeap<>();
		for(int i = testSize - 1;i >= 0;i--) {
			me.insert(randomTest.get(i),value);
		}
		Set<Double> elements = new HashSet<>(testSize);
		Set<Double> control = new HashSet<>(randomTest);
		for(PairingHeap<Double,String>.Element element : me) {
			elements.add(element.getKey());
		}
		assertEquals(elements,control);

		long meTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			System.gc();
			spin();
			long before = System.nanoTime();
			Iterator<PairingHeap<Double,String>.Element> iterator = me.iterator();
			while(iterator.hasNext()) {
				iterator.next();
			}
			long after = System.nanoTime();
			meTime += after - before;
		}
		printMe(meTime);
		PriorityQueue<Double> them = new PriorityQueue<>(randomTest);
		long themTime = 0;
		for(int t = numTests - 1;t >= 0;t--) {
			System.gc();
			spin();
			long before = System.nanoTime();
			Iterator<Double> iterator = them.iterator();
			while(iterator.hasNext()) {
				iterator.next();
			}
			long after = System.nanoTime();
			themTime += after - before;
		}
		printThem(meTime,themTime);
	}

	/**
	 * Spins this thread, allowing other threads, such as the garbage collector,
	 * to finish. This makes results slightly more reliable.
	 */
	private void spin() {
		for(int i = spins;i >= 0;i--) {
			i *= 1;
		}
	}

	/**
	 * Prints the time a test on the pairing heap took, in seconds, and provides
	 * additional spacing to line out the time for the priority queue.
	 * @param meTime The time the test on the pairing heap took, in nanoseconds.
	 */
	private void printMe(long meTime) {
		String meStr = Double.toString(meTime / 1E9);
		System.out.print(meStr);
		System.out.print("s (me)");
		for(int i = 11 - meStr.length();i >= 0;i--) {
			System.out.print(' ');
		}
	}

	/**
	 * Prints the time a test on the priority queue took, in seconds, and the
	 * ratio between {@code themTime} and {@code meTime}. A higher ratio means
	 * that the pairing heap was fast, which is better!
	 * @param meTime The time the test on the pairing heap took, in nanoseconds.
	 * @param themTime The time the test on the priority queue took, in
	 * nanoseconds.
	 */
	private void printThem(long meTime,long themTime) {
		String themStr = Double.toString(themTime / 1E9);
		System.out.print(themStr);
		System.out.print("s (them)");
		for(int i = 13 - themStr.length();i >= 0;i--) {
			System.out.print(' ');
		}
		System.out.println(" RATIO: " + (1.0 * themTime / meTime));
	}

	static class Elem implements Comparable<Elem> {
		double key;
		String value;
		Elem(double key,String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public int compareTo(Elem o) {
			return Double.compare(key,o.key);
		}
	}
}