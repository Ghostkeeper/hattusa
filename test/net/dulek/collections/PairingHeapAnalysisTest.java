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

import java.io.*;
import java.util.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Ruben Dulek
 */
public class PairingHeapAnalysisTest {
	/**
	 * The size of the largest tests to try.
	 */
	private final int maxSize = 10_000;

	/**
	 * The file the test results should be written to.
	 */
	private final String outputFile = "C:/temp/pairingheap.txt";

	/**
	 * The number of times to repeat each test. The results are averaged.
	 */
	private final int repeats = 100;

	/**
	 * A list of the results for each test. Each 'result' in this case is an
	 * array of doubles, listing the time taken for each size increment of a
	 * test. This list coincides with {@link resultsHeaders}, which lists the
	 * title of each test.
	 */
	private final List<double[]> results = new ArrayList<>(12);

	/**
	 * A list of the names of the tests performed, for the results table. This
	 * list coincides with {@link results}, which provides the gathered data for
	 * each test.
	 */
	private final List<String> resultsHeaders = new ArrayList<>(12);

	/**
	 * A random number generator with a fixed seed. This makes the results
	 * repeatable.
	 */
	private final Random rng = new Random(0x12345678);

	/**
	 * With how much to increment the test size after each test. The test size
	 * is incremented until {@link maxSize} is reached.
	 */
	private final int sizeIncrement = 1000;

	/**
	 * How long to spin the current thread before each test. Spinning allows
	 * other threads of the program (such as the garbage collector) to finish so
	 * they cannot influence the results.
	 */
	private final int spins = 10_000;

	/**
	 * A test value to stick into the pairing heap.
	 */
	private final String value = "test";

	/**
	 * Performs the test.
	 */
	@Test
	public void run() {
		prepare();
		try {
			//All the tests to conduct.
			changeKeyPairingHeap();
			changeKeyPriorityQueue();
			clearPairingHeap();
			clearPriorityQueue();
			clonePairingHeap();
			clonePriorityQueue();
			containsKeyPairingHeap();
			containsValuePairingHeap();
			containsPriorityQueue();
			decreaseKeyPairingHeap();
			decreaseKeyPriorityQueue();
			deletePairingHeap();
			deletePriorityQueue();
			deleteMinPairingHeap();
			deleteMinPriorityQueue();
			entrySetPairingHeap();
			keySetPairingHeap();
			valuesPairingHeap();
			entrySetPriorityQueue();
			findMinPairingHeap();
			findMinPriorityQueue();
			insertPairingHeap();
			insertPriorityQueue();
			isEmptyPairingHeap();
			isEmptyPriorityQueue();
			iteratorPairingHeap();
			iteratorSortedPairingHeap();
			iteratorPriorityQueue();
			mergePairingHeap();
			mergePriorityQueue();
			sizePairingHeap();
			sizePriorityQueue();
			toArrayPairingHeap();
			toArrayPriorityQueue();
		} catch(final Exception e) { //Catches all exceptions (such as out of memory) and writes intermediate results to the file.
			e.printStackTrace();
			writeToFile();
			return;
		}

		writeToFile();
	}

	/**
	 * Tests the {@link PairingHeap#changeKey(PairingHeap.Element,K)} method.
	 * The keys are <i>always increased</i>.
	 */
	private void changeKeyPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				final Object[] elements = heap.toArray();
				final PairingHeap<Double,String>.Element element = (PairingHeap<Double,String>.Element) elements[rng.nextInt(elements.length)]; //Pick a random element to change the key of.
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.changeKey(element,2.0);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") changeKey: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("ChangeKey PairingHeap");
	}

	/**
	 * Tests changing the key of elements in a {@code PriorityQueue}. The keys
	 * are <i>always increased</i>.
	 * <p>The {@code PriorityQueue} requires that the sorting order of its
	 * elements stay the same as long as they are in the queue, so we will have
	 * to pull them out of the queue, change the key, and then put them back in.
	 * </p>
	 */
	private void changeKeyPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Elem> heap = priorityQueueKeyed(size);
				Elem[] elements = new Elem[size];
				elements = heap.toArray(elements);
				final Elem element = elements[rng.nextInt(elements.length)]; //Pick a random element to change the key of.
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.remove(element);
				element.key = 2.0;
				heap.add(element);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") changeKey: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("ChangeKey PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#clear()} method.
	 * <p>For fair comparison, the next garbage collection should also be
	 * included in the time measurement, but that would influence the
	 * measurements so greatly that the signal is indistinguishable from the
	 * noise.</p>
	 */
	private void clearPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.clear();
				//System.gc();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") clear: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Clear PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#clear()} method.
	 * <p>For fair comparison, the next garbage collection should also be
	 * included in the time measurement, but that would influence the
	 * measurements so greatly that the signal is indistinguishable from the
	 * noise.</p>
	 */
	private void clearPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.clear();
				//System.gc();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") clear: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Clear PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#clone()} method.
	 */
	private void clonePairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				try {
					heap.clone();
				} catch(final CloneNotSupportedException e) { //Never happens.
					throw new InternalError(e);
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") clone: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Clone PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#PriorityQueue(PriorityQueue)} method.
	 */
	private void clonePriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				final PriorityQueue<Double> clone = new PriorityQueue<>(heap);
				final long after = System.nanoTime();
				clone.peek();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") clone: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Clone PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#containsKey(K)} method.
	 * <p>The method is tested on a random key. The key is most likely not in
	 * the heap, but there is a slim chance that the random number generator
	 * generates a key that is in the heap, theoretically. Don't count on it;
	 * this method should not be considered to test the early-termination
	 * behaviour of the method.</p>
	 */
	private void containsKeyPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				final Double key = rng.nextDouble();
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.containsKey(key);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") containsKey: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("ContainsKey PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#contains(Object)} method.
	 * <p>The method is tested on a random key. The key is most likely not in
	 * the heap, but there is a slim chance that the random number generator
	 * generates a key that is in the heap, theoretically. Don't count on it;
	 * this method should not be considered to test the early-termination
	 * behaviour of the method.</p>
	 */
	private void containsPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				final Double key = rng.nextDouble();
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.contains(key);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") contains: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Clone Contains");
	}

	/**
	 * Tests the {@link PairingHeap#containsValue(V)} method.
	 * <p>The method is tested on value that is not in the heap. No early
	 * termination will occur.</p>
	 */
	private void containsValuePairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				final String value = "I'm not in the heap, dummy.";
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.containsValue(value);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") containsValue: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("ContainsValue PairingHeap");
	}

	/**
	 * Tests the {@link PairingHeap#decreaseKey(PairingHeap.Element,K)} method.
	 * <p>Only the {@code decreaseKey} method is tested. It should be noted that
	 * this method increases the time taken by the next
	 * {@link PairingHeap#deleteMin()} method, but that method is not tested.
	 * For a better comparison, please see the tests on Dijkstra's Algorithm and
	 * such.</p>
	 */
	private void decreaseKeyPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				final Object[] elements = heap.toArray();
				final PairingHeap<Double,String>.Element element = (PairingHeap<Double,String>.Element) elements[rng.nextInt(elements.length)]; //Pick a random element to change the key of.
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.decreaseKey(element,0.0);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") decreaseKey: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("DecreaseKey PairingHeap");
	}

	/**
	 * Tests decreasing the key of elements in a {@code PriorityQueue}.
	 * <p>The {@code PriorityQueue} requires that the sorting order of its
	 * elements stay the same as long as they are in the queue, so we will have
	 * to pull them out of the queue, decrease the key, and then put them back
	 * in.</p>
	 */
	private void decreaseKeyPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Elem> heap = priorityQueueKeyed(size);
				Elem[] elements = new Elem[size];
				elements = heap.toArray(elements);
				final Elem element = elements[rng.nextInt(elements.length)]; //Pick a random element to change the key of.
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.remove(element);
				element.key = 0.0;
				heap.add(element);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") decreaseKey: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("DecreaseKey PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#delete(PairingHeap.Element)} method. Because
	 * of the amortized nature of this method, the method is executed multiple
	 * times on each heap ({@code sizeIncrement} times). This should average out
	 * the amortisation sufficiently.
	 */
	private void deletePairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				final Object[] objects = heap.toArray();
				final List<PairingHeap<Double,String>.Element> elements = new ArrayList<>(size);
				for(int i = size - 1;i >= 0;i--) {
					elements.add((PairingHeap<Double,String>.Element)objects[i]);
				}
				Collections.shuffle(elements); //We'll delete the first sizeIncrement of these.
				System.gc();
				int i = sizeIncrement - 1;
				spin();
				final long before = System.nanoTime();
				for(;i >= 0;i--) {
					heap.delete(elements.get(i));
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats / sizeIncrement;
			System.out.println("PairingHeap(" + size + ") delete: " + ((double)time / repeats / sizeIncrement));
		}
		results.add(result);
		resultsHeaders.add("Delete PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#remove(Object)} method. Because of the
	 * amortized nature of {@code PairingHeap}'s version of this method, the
	 * method is executed multiple times on each heap ({@code sizeIncrement}
	 * times). This should average out the amortisation sufficiently.
	 */
	private void deletePriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				Double[] elements = new Double[size];
				elements = heap.toArray(elements);
				List<Double> elementsList = Arrays.asList(elements);
				Collections.shuffle(elementsList); //We'll delete the first sizeIncrement of these.
				System.gc();
				int i = sizeIncrement - 1;
				spin();
				final long before = System.nanoTime();
				for(;i >= 0;i--) {
					heap.remove(elementsList.get(i));
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats / sizeIncrement;
			System.out.println("PriorityQueue(" + size + ") delete: " + ((double)time / repeats / sizeIncrement));
		}
		results.add(result);
		resultsHeaders.add("Delete PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#deleteMin(PairingHeap.Element)} method.
	 * Because of the amortized nature of this method, the method is executed
	 * multiple times on each heap ({@code sizeIncrement} times). This should
	 * average out the amortisation sufficiently.
	 */
	private void deleteMinPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				int i = sizeIncrement - 1;
				spin();
				final long before = System.nanoTime();
				for(;i >= 0;i--) {
					heap.deleteMin();
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats / sizeIncrement;
			System.out.println("PairingHeap(" + size + ") deleteMin: " + ((double)time / repeats / sizeIncrement));
		}
		results.add(result);
		resultsHeaders.add("DeleteMin PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#poll()} method. Because of the amortized
	 * nature of {@code PairingHeap}'s version of this method, the method is
	 * executed multiple times on each heap ({@code sizeIncrement} times). This
	 * should average out the amortisation sufficiently.
	 */
	private void deleteMinPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				System.gc();
				int i = sizeIncrement - 1;
				spin();
				final long before = System.nanoTime();
				for(;i >= 0;i--) {
					heap.poll();
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats / sizeIncrement;
			System.out.println("PriorityQueue(" + size + ") deleteMin: " + ((double)time / repeats / sizeIncrement));
		}
		results.add(result);
		resultsHeaders.add("DeleteMin PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#entrySet()} method.
	 */
	private void entrySetPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.entrySet();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") entrySet: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("EntrySet PairingHeap");
	}

	/**
	 * Tests generating a set of all elements in a {@code PriorityQueue}. Since
	 * the {@code PriorityQueue} has no such equivalent to {@code PairingHeap}'s
	 * version, a set is generated by iterating over the heap and adding the
	 * elements one by one. This comparison is likely not fair.
	 */
	private void entrySetPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				final Set<Double> set;
				final int setCapacity = size < 1 ? 10 : size;
				System.gc();
				spin();
				final long before = System.nanoTime();
				set = new HashSet<>(setCapacity);
				for(final Double element : heap) {
					set.add(element);
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") entrySet: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("EntrySet PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#findMin()} method.
	 */
	private void findMinPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.findMin();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") findMin: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("FindMin PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#peek()} method.
	 */
	private void findMinPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.peek();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") findMin: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("FindMin PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#insert(K,V)} method. Because of the
	 * amortized nature of the {@code PriorityQueue}'s version of this method,
	 * the method is tested on every different size, even those between the
	 * increment, and then averaged. This should clearly show the break points
	 * where the {@code PriorityQueue} expands its array.
	 */
	private void insertPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				double[] keys = new double[sizeIncrement];
				for(int j = sizeIncrement - 1;j >= 0;j--) { //Pre-generate the keys too.
					keys[j] = rng.nextDouble();
				}
				int i = sizeIncrement - 1;
				spin();
				final long before = System.nanoTime();
				for(;i >= 0;i--) {
					heap.insert(keys[i],value);
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats / sizeIncrement;
			System.out.println("PairingHeap(" + size + ") insert: " + ((double)time / repeats / sizeIncrement));
		}
		results.add(result);
		resultsHeaders.add("Insert PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#add(E)} method. Because of the amortized
	 * nature of this method, the method is tested on every different size, even
	 * those between the increment, and then averaged. This should clearly show
	 * the break points where the {@code PriorityQueue} expands its array.
	 */
	private void insertPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				System.gc();
				int i = sizeIncrement - 1;
				final double[] keys = new double[sizeIncrement];
				for(int j = sizeIncrement - 1;j >= 0;j--) { //Pre-generate the keys too.
					keys[j] = rng.nextDouble();
				}
				spin();
				final long before = System.nanoTime();
				for(;i >= 0;i--) {
					heap.add(keys[i]);
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats / sizeIncrement;
			System.out.println("PriorityQueue(" + size + ") insert: " + ((double)time / repeats / sizeIncrement));
		}
		results.add(result);
		resultsHeaders.add("Insert PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#isEmpty()} method.
	 * <p>The heap will not be empty, except for the first iteration when
	 * {@code size == 0}.</p>
	 */
	private void isEmptyPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.isEmpty();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") isEmpty: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("IsEmpty PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#isEmpty()} method.
	 * <p>The heap will not be empty, except for the first iteration when
	 * {@code size == 0}.</p>
	 */
	private void isEmptyPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.isEmpty();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") isEmpty: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("IsEmpty PriorityQueue");
	}

	/**
	 * Tests the {@code PairingHeap}'s iterator. The time of a complete
	 * iteration of the heap is measured.
	 */
	private void iteratorPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				for(final PairingHeap<Double,String>.Element element : heap);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") iterator: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Iterator PairingHeap");
	}

	/**
	 * Tests the {@code PriorityQueue}'s iterator. The time of a complete
	 * iteration of the heap is measured.
	 */
	private void iteratorPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				for(final Double element : heap);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") iterator: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Iterator PriorityQueue");
	}

	/**
	 * Tests the {@code PairingHeap}'s sorted iterator. The time of a complete
	 * (ordered) iteration of the heap is measured.
	 */
	private void iteratorSortedPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				Iterator<PairingHeap<Double,String>.Element> iterator = heap.iteratorSorted();
				while(iterator.hasNext()) {
					iterator.next();
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") iteratorSorted: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("IteratorSorted PairingHeap");
	}

	/**
	 * Tests the {@link PairingHeap#keySet()} method.
	 */
	private void keySetPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.keySet();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") keySet: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("KeySet PairingHeap");
	}

	/**
	 * Tests the {@link PairingHeap#merge(PairingHeap)} method. The heap is
	 * merged with a different random heap of the same size.
	 */
	private void mergePairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				final PairingHeap<Double,String> otherHeap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.merge(otherHeap);
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") merge: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Merge PairingHeap");
	}

	/**
	 * Tests merging two {@code PriorityQueue}s. Since no such method is
	 * available in {@code PriorityQueue}, the heaps are merged by adding all
	 * elements of one heap to the other. The two heaps have the same size.
	 */
	private void mergePriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				final PriorityQueue<Double> otherHeap = priorityQueue(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				for(final Double element : otherHeap) {
					heap.add(element);
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") merge: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Merge PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#values()} method.
	 */
	private void valuesPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.values();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") values: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Values PairingHeap");
	}

	/**
	 * Tests the {@link PairingHeap#size()} method.
	 */
	private void sizePairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.size();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") size: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Size PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#size()} method.
	 */
	private void sizePriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.size();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") size: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("Size PriorityQueue");
	}

	/**
	 * Tests the {@link PairingHeap#toArray()} method.
	 */
	private void toArrayPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,String> heap = pairingHeap(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.toArray();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") toArray: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("ToArray PairingHeap");
	}

	/**
	 * Tests the {@link PriorityQueue#toArray()} method.
	 * <p>It is assumed that the {@link PriorityQueue#toArray(T[])} method is
	 * more or less the same, so no test is created for that.</p>
	 */
	private void toArrayPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = 0;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<Double> heap = priorityQueue(size);
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.toArray();
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") toArray: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("ToArray PriorityQueue");
	}

/////////////////////////////////HELPER METHODS/////////////////////////////////

	/**
	 * Generates a new random {@code PairingHeap} of the specified size.
	 * @param size The size of the {@code PairingHeap}.
	 * @return A new random {@code PairingHeap}.
	 */
	private PairingHeap<Double,String> pairingHeap(final int size) {
		final PairingHeap<Double,String> result = new PairingHeap<>();
		for(int i = size;i >= 0;i--) {
			result.insert(rng.nextDouble(),value);
		}
		return result;
	}

	/**
	 * Initial spin. This method loads up a full {@code PairingHeap} and a full
	 * {@code PriorityQueue}. This allows the JVM to expand its memory
	 * allocation to allow for the biggest data structures. Expanding memory
	 * takes extra time, and we don't want that to influence our measurements
	 * for the first test we're conducting.
	 */
	private void prepare() {
		final PairingHeap<Double,String> pairingHeap = pairingHeap(maxSize);
		final PriorityQueue<Double> priorityQueue = priorityQueue(maxSize);
		pairingHeap.clear();
		priorityQueue.poll();
	}

	/**
	 * Generates a new random {@code PriorityQueue} of the specified size.
	 * @param size The size of the {@code PriorityQueue}.
	 * @return A new random {@code PriorityQueue}.
	 */
	private PriorityQueue<Double> priorityQueue(int size) {
		final PriorityQueue<Double> result = new PriorityQueue<>(size < 1 ? 1 : size);
		for(int i = size;i >= 0;i--) {
			result.add(rng.nextDouble());
		}
		return result;
	}

	/**
	 * Generates a new random {@code PriorityQueue} of the specified size. The
	 * queue will contain {@code Elem}s with random keys. This allows for
	 * changing the keys of its elements separately from the actual elements, to
	 * test operations such as {@code decreaseKey} and {@code changeKey}.
	 * @param size The size of the {@code PriorityQueue}.
	 * @return A new random {@code PriorityQueue}.
	 */
	private PriorityQueue<Elem> priorityQueueKeyed(int size) {
		final PriorityQueue<Elem> result = new PriorityQueue<>(size < 1 ? 1 : size);
		for(int i = size;i >= 0;i--) {
			result.add(new Elem(rng.nextDouble(),value));
		}
		return result;
	}

	/**
	 * Spins the thread around for a loop. This allows other tasks to finish,
	 * such as the garbage collector, printing to the console, or out-of-order
	 * execution. It should be called right before every time measurement.
	 */
	private void spin() {
		for(int i = spins;i >= 0;i--);
	}

	/**
	 * Writes the current results to the file specified in {@code outputFile}.
	 */
	private void writeToFile() {
		try {
			final BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for(int i = 0;i < resultsHeaders.size() - 1;i++) {
				bw.write(resultsHeaders.get(i) + ";");
			}
			bw.write(resultsHeaders.get(resultsHeaders.size() - 1) + "\n");
			for(int i = 0;i * sizeIncrement < maxSize;i++) {
				for(int j = 0;j < results.size() - 1;j++) {
					bw.write(results.get(j)[i] + ";");
				}
				bw.write(results.get(results.size() - 1)[i] + "\n");
			}
			bw.close();
		} catch(final FileNotFoundException e) { //Sucks.
			e.printStackTrace();
			return;
		} catch(final IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * A simple key-value pair that can be compared by its key. The purpose of
	 * this helper class is to provide the {@code PriorityQueue} with elements
	 * of which the key may change. To change the key of a simple
	 * {@code Double}, for instance, would change the identity of the
	 * {@code Double}. Not so for this {@code Elem}. This enables the
	 * {@code PriorityQueue} to perform operations like {@code decreaseKey} or
	 * {@code changeKey}.
	 */
	private class Elem implements Comparable<Elem> {
		/**
		 * The key of the element. This may be changed.
		 */
		Double key;

		/**
		 * The value of the element. This doesn't need to be changed.
		 */
		final String value;

		/**
		 * Constructs a new {@code Elem} with the specified key and value.
		 * @param key The key for the new {@code Elem}.
		 * @param value The value for the new {@code Elem}.
		 */
		Elem(final Double key,final String value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * Compares this {@code Elem} with another {@code Elem}. Only the keys
		 * are compared and the result is returned.
		 * @param other The {@code Elem} to compare this element with.
		 * @return The result of comparing the keys of the elements.
		 */
		@Override
		public int compareTo(final Elem other) {
			return Double.compare(key,other.key);
		}
	}

	/**
	 * A vertex that, when combined with other vertices, forms a simple graph.
	 * The edges are implemented using an adjacency list.
	 */
	private class Vertex {
		/**
		 * The distances to every neighbouring vertex.
		 */
		double[] neighbourDistances;

		/**
		 * All neighbouring vertices of this vertex.
		 */
		Vertex[] neighbours;

		/**
		 * The supposed number of neighbours of this vertex.
		 */
		final int capacity;

		/**
		 * Constructs a new vertex. The adjacency lists of the vertex will have
		 * the specified capacity.
		 * @param capacity The capacity of the adjacency lists of the vertex.
		 */
		Vertex(final int capacity) {
			this.capacity = capacity;
		}

		/**
		 * Connects this vertex to other vertices up to the capacity of the
		 * adjacency list. Random neighbours will be selected from the specified
		 * candidate vertex array. No duplicates will be selected, nor will the
		 * vertex be connected to itself.
		 * <p>Note that the vertices will not be connected symmetrically, nor
		 * will the graph be Euclidean.</p>
		 * @param candidates An array of candidate vertices to connect to.
		 * @throws IllegalArgumentException The candidate list is too short.
		 * @throws NullPointerException The specified candidate list was
		 * {@code null}.
		 */
		void connect(final Vertex[] candidates) {
			final int numCandidates = candidates.length;
			if(numCandidates - 1 < capacity) { //Throws NullPointerException if candidates is null.
				throw new IllegalArgumentException("There must be enough unique candidates to fill the neighbours list.");
			}
			final IdentityHashSet<Vertex> selection = new IdentityHashSet<>(capacity);
			while(selection.size() < capacity) {
				final Vertex candidate = candidates[rng.nextInt(numCandidates)];
				if(candidate != this) {
					selection.add(candidate);
				}
			}
			neighbours = selection.toArray(new Vertex[neighbours.length]);
			for(int i = capacity - 1;i >= 0;i--) {
				neighbourDistances[i] = rng.nextDouble();
			}
		}
	}
}