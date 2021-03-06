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
import net.dulek.collections.HashSet;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Ruben Dulek
 */
//@Ignore
public class PairingHeapAnalysisTest {
	/**
	 * The size of the largest tests to try.
	 */
	private final int maxSize = 100_000;

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
	@Ignore
	@Test
	public void run() {
		prepare();
		try {
			//All the tests to conduct.
			changeKeyPairingHeap();
			changeKeyPriorityQueue();
			//clearPairingHeap();
			//clearPriorityQueue();
			//clonePairingHeap();
			//clonePriorityQueue();
			//containsKeyPairingHeap();
			//containsValuePairingHeap();
			//containsPriorityQueue();
			//decreaseKeyPairingHeap();
			//decreaseKeyPriorityQueue();
			//deletePairingHeap();
			//deletePriorityQueue();
			//deleteMinPairingHeap();
			//deleteMinPriorityQueue();
			//entrySetPairingHeap();
			//keySetPairingHeap();
			//valuesPairingHeap();
			//entrySetPriorityQueue();
			//findMinPairingHeap();
			//findMinPriorityQueue();
			//insertPairingHeap();
			//insertPriorityQueue();
			//isEmptyPairingHeap();
			//isEmptyPriorityQueue();
			//iteratorPairingHeap();
			//iteratorSortedPairingHeap();
			//iteratorPriorityQueue();
			//mergePairingHeap();
			//mergePriorityQueue();
			//sizePairingHeap();
			//sizePriorityQueue();
			//toArrayPairingHeap();
			//toArrayPriorityQueue();
			//dijkstraSparsePairingHeap();
			//dijkstraSparsePriorityQueue();
			//dijkstraMediumPairingHeap();
			//dijkstraMediumPriorityQueue();
			//dijkstraDensePairingHeap();
			//dijkstraDensePriorityQueue();
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
	@Ignore
	@Test
	public void changeKeyPairingHeap() {
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
		writeDirect("C:/temp/changeKeyPairingHeap.csv","PairingHeap.changeKey",result);
	}

	/**
	 * Tests changing the key of elements in a {@code PriorityQueue}. The keys
	 * are <i>always increased</i>.
	 * <p>The {@code PriorityQueue} requires that the sorting order of its
	 * elements stay the same as long as they are in the queue, so we will have
	 * to pull them out of the queue, change the key, and then put them back in.
	 * </p>
	 */
	@Ignore
	@Test
	public void changeKeyPriorityQueue() {
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
		writeDirect("C:/temp/changeKeyPriorityQueue.csv","PriorityQueue.changeKey",result);
	}

	/**
	 * Tests the {@link PairingHeap#clear()} method.
	 * <p>For fair comparison, the next garbage collection should also be
	 * included in the time measurement, but that would influence the
	 * measurements so greatly that the signal is indistinguishable from the
	 * noise.</p>
	 */
	@Ignore
	@Test
	public void clearPairingHeap() {
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
		writeDirect("C:/temp/clearPairingHeap.csv","PairingHeap.clear",result);
	}

	/**
	 * Tests the {@link PriorityQueue#clear()} method.
	 * <p>For fair comparison, the next garbage collection should also be
	 * included in the time measurement, but that would influence the
	 * measurements so greatly that the signal is indistinguishable from the
	 * noise.</p>
	 */
	@Ignore
	@Test
	public void clearPriorityQueue() {
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
		writeDirect("C:/temp/clearPriorityQueue.csv","PriorityQueue.clear",result);
	}

	/**
	 * Tests the {@link PairingHeap#clone()} method.
	 */
	@Ignore
	@Test
	public void clonePairingHeap() {
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
		writeDirect("C:/temp/clonePairingHeap.csv","PairingHeap.clone",result);
	}

	/**
	 * Tests the {@link PriorityQueue#PriorityQueue(PriorityQueue)} method.
	 */
	@Ignore
	@Test
	public void clonePriorityQueue() {
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
		writeDirect("C:/temp/clonePriorityQueue.csv","PriorityQueue.clone",result);
	}

	/**
	 * Tests the {@link PairingHeap#containsKey(K)} method.
	 * <p>The method is tested on a random key. The key is most likely not in
	 * the heap, but there is a slim chance that the random number generator
	 * generates a key that is in the heap, theoretically. Don't count on it;
	 * this method should not be considered to test the early-termination
	 * behaviour of the method.</p>
	 */
	@Ignore
	@Test
	public void containsKeyPairingHeap() {
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
		writeDirect("C:/temp/containsKeyPairingHeap.csv","PairingHeap.containsKey",result);
	}

	/**
	 * Tests the {@link PriorityQueue#contains(Object)} method.
	 * <p>The method is tested on a random key. The key is most likely not in
	 * the heap, but there is a slim chance that the random number generator
	 * generates a key that is in the heap, theoretically. Don't count on it;
	 * this method should not be considered to test the early-termination
	 * behaviour of the method.</p>
	 */
	@Ignore
	@Test
	public void containsPriorityQueue() {
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
		resultsHeaders.add("Contains PriorityQueue");
		writeDirect("C:/temp/containsPriorityQueue.csv","PriorityQueue.contains",result);
	}

	/**
	 * Tests the {@link PairingHeap#containsValue(V)} method.
	 * <p>The method is tested on value that is not in the heap. No early
	 * termination will occur.</p>
	 */
	@Ignore
	@Test
	public void containsValuePairingHeap() {
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
		writeDirect("C:/temp/containsValuePairingHeap.csv","PairingHeap.containsValue",result);
	}

	/**
	 * Tests the {@link PairingHeap#decreaseKey(PairingHeap.Element,K)} method.
	 * <p>Only the {@code decreaseKey} method is tested. It should be noted that
	 * this method increases the time taken by the next
	 * {@link PairingHeap#deleteMin()} method, but that method is not tested.
	 * For a better comparison, please see the tests on Dijkstra's Algorithm and
	 * such.</p>
	 */
	@Ignore
	@Test
	public void decreaseKeyPairingHeap() {
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
		writeDirect("C:/temp/decreaseKeyPairingHeap.csv","PairingHeap.decreaseKey",result);
	}

	/**
	 * Tests decreasing the key of elements in a {@code PriorityQueue}.
	 * <p>The {@code PriorityQueue} requires that the sorting order of its
	 * elements stay the same as long as they are in the queue, so we will have
	 * to pull them out of the queue, decrease the key, and then put them back
	 * in.</p>
	 */
	@Ignore
	@Test
	public void decreaseKeyPriorityQueue() {
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
		writeDirect("C:/temp/decreaseKeyPriorityQueue.csv","PriorityQueue.decreaseKey",result);
	}

	/**
	 * Tests the {@link PairingHeap#delete(PairingHeap.Element)} method. Because
	 * of the amortized nature of this method, the method is executed multiple
	 * times on each heap ({@code sizeIncrement} times). This should average out
	 * the amortisation sufficiently.
	 */
	@Ignore
	@Test
	public void deletePairingHeap() {
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
		writeDirect("C:/temp/deletePairingHeap.csv","PairingHeap.delete",result);
	}

	/**
	 * Tests the {@link PriorityQueue#remove(Object)} method. Because of the
	 * amortized nature of {@code PairingHeap}'s version of this method, the
	 * method is executed multiple times on each heap ({@code sizeIncrement}
	 * times). This should average out the amortisation sufficiently.
	 */
	@Ignore
	@Test
	public void deletePriorityQueue() {
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
		writeDirect("C:/temp/deletePriorityQueue.csv","PriorityQueue.delete",result);
	}

	/**
	 * Tests the {@link PairingHeap#deleteMin(PairingHeap.Element)} method.
	 * Because of the amortized nature of this method, the method is executed
	 * multiple times on each heap ({@code sizeIncrement} times). This should
	 * average out the amortisation sufficiently.
	 */
	@Test
	public void deleteMinPairingHeap() {
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
		writeDirect("C:/temp/deleteMinPairingHeap.csv","PairingHeap.deleteMin",result);
	}

	/**
	 * Tests the {@link PriorityQueue#poll()} method. Because of the amortized
	 * nature of {@code PairingHeap}'s version of this method, the method is
	 * executed multiple times on each heap ({@code sizeIncrement} times). This
	 * should average out the amortisation sufficiently.
	 */
	@Ignore
	@Test
	public void deleteMinPriorityQueue() {
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
		writeDirect("C:/temp/deleteMinPriorityQueue.csv","PriorityQueue.deleteMin",result);
	}

	/**
	 * Tests the {@code PairingHeap} on multiple attributes at once by
	 * performing Dijkstra's Algorithm for shortest paths on random graphs. The
	 * graphs will be sparse in this case. Every vertex is connected to
	 * {@code 0.5n} other vertices (asymetrically). Dijkstra's algorithm will
	 * compute the shortest paths from a random vertex to every other vertex in
	 * the graph (single-source, multiple-destination).
	 * <p>This test will analyse the efficiency of the heap mostly on the
	 * efficiency of its {@code insert} and {@code deleteMin} methods, but also
	 * a bit on the {@code decreaseKey} method.</p>
	 */
	@Test
	public void dijkstraDensePairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,Vertex> heap = new PairingHeap<>();
				final Vertex[] graph = graph(size,size >> 1);
				final Vertex start = graph[rng.nextInt(size)]; //Pick a random starting vertex.
				System.gc();
				spin();
				final long before = System.nanoTime();
				start.element = heap.insert(0.0,start); //Distance to starting vertex is zero.
				while(!heap.isEmpty()) {
					final PairingHeap<Double,Vertex>.Element current = heap.deleteMin();
					//Here is where you would report the distance to the vertex (current.getKey()).
					final double distance = current.getKey();
					final Vertex currentVertex = current.getValue();
					final Vertex[] neighbours = currentVertex.neighbours;
					final double[] neighbourDistances = currentVertex.neighbourDistances;
					for(int i = neighbours.length - 1;i >= 0;i--) {
						final Vertex neighbour = neighbours[i];
						final double neighbourDistance = neighbourDistances[i];
						final PairingHeap<Double,Vertex>.Element neighbourElement = neighbour.element;
						if(neighbourElement == null) { //Haven't seen this vertex yet.
							neighbour.element = heap.insert(distance + neighbourDistance,neighbour);
						} else { //Already seen it. Only need to decrease the key, perhaps.
							if(distance + neighbourDistance < neighbourElement.getKey()) {
								heap.decreaseKey(neighbourElement,distance + neighbourDistance);
							}
						}
					}
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") Dijkstra dense: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("DijkstraDense PairingHeap");
		writeDirect("C:/temp/dijkstraDensePairingHeap.csv","PairingHeap.dijkstraDense",result);
	}

	/**
	 * Tests the {@code PriorityQueue} on multiple attributes at once by
	 * performing Dijkstra's Algorithm for shortest paths on random graphs. The
	 * graphs will be sparse in this case. Every vertex is connected to
	 * {@code 0.5n} other vertices (asymetrically). Dijkstra's algorithm will
	 * compute the shortest paths from a random vertex to every other vertex in
	 * the graph (single-source, multiple-destination).
	 * <p>This test will analyse the efficiency of the heap mostly on the
	 * efficiency of its {@code insert} and {@code deleteMin} methods. Since the
	 * {@code PriorityQueue} has no {@code decreaseKey} method, this algorithm
	 * simply adds a new element every time a key needs decreasing.</p>
	 */
	@Test
	public void dijkstraDensePriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<VertexElem> heap = new PriorityQueue<>();
				final PriorityQueueVertex[] graph = priorityQueueGraph(size,size >> 1);
				final PriorityQueueVertex start = graph[rng.nextInt(size)]; //Pick a random starting vertex.
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.add(new VertexElem(0.0,start)); //Distance to starting vertex is zero.
				while(!heap.isEmpty()) {
					final VertexElem current = heap.poll();
					current.value.visited = true;
					//Here is where you would report the distance to the vertex (current.getKey()).
					final double distance = current.key;
					final PriorityQueueVertex currentVertex = current.value;
					final PriorityQueueVertex[] neighbours = currentVertex.neighbours;
					final double[] neighbourDistances = currentVertex.neighbourDistances;
					for(int i = neighbours.length - 1;i >= 0;i--) {
						final PriorityQueueVertex neighbour = neighbours[i];
						final double neighbourDistance = neighbourDistances[i];
						final VertexElem neighbourElement = neighbour.element;
						if(!neighbour.visited && (neighbourElement == null || distance + neighbourDistance < neighbourElement.key)) { //Haven't visited this vertex yet.
							final VertexElem elem = new VertexElem(distance + neighbourDistance,neighbour);
							neighbour.element = elem;
							heap.add(elem);
						}
					}
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") Dijkstra dense: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("DijkstraDense PriorityQueue");
		writeDirect("C:/temp/dijkstraDensePriorityQueue.csv","PriorityQueue.dijkstraDense",result);
	}

	/**
	 * Tests the {@code PairingHeap} on multiple attributes at once by
	 * performing Dijkstra's Algorithm for shortest paths on random graphs. The
	 * graphs will be sparse in this case. Every vertex is connected to
	 * {@code sqrt(n)} other vertices (asymetrically). Dijkstra's algorithm will
	 * compute the shortest paths from a random vertex to every other vertex in
	 * the graph (single-source, multiple-destination).
	 * <p>This test will analyse the efficiency of the heap mostly on the
	 * efficiency of its {@code insert} and {@code deleteMin} methods, but also
	 * a bit on the {@code decreaseKey} method.</p>
	 */
	@Test
	public void dijkstraMediumPairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,Vertex> heap = new PairingHeap<>();
				final Vertex[] graph = graph(size,(int)Math.sqrt(size));
				final Vertex start = graph[rng.nextInt(size)]; //Pick a random starting vertex.
				System.gc();
				spin();
				final long before = System.nanoTime();
				start.element = heap.insert(0.0,start); //Distance to starting vertex is zero.
				while(!heap.isEmpty()) {
					final PairingHeap<Double,Vertex>.Element current = heap.deleteMin();
					//Here is where you would report the distance to the vertex (current.getKey()).
					final double distance = current.getKey();
					final Vertex currentVertex = current.getValue();
					final Vertex[] neighbours = currentVertex.neighbours;
					final double[] neighbourDistances = currentVertex.neighbourDistances;
					for(int i = neighbours.length - 1;i >= 0;i--) {
						final Vertex neighbour = neighbours[i];
						final double neighbourDistance = neighbourDistances[i];
						final PairingHeap<Double,Vertex>.Element neighbourElement = neighbour.element;
						if(neighbourElement == null) { //Haven't seen this vertex yet.
							neighbour.element = heap.insert(distance + neighbourDistance,neighbour);
						} else { //Already seen it. Only need to decrease the key, perhaps.
							if(distance + neighbourDistance < neighbourElement.getKey()) {
								heap.decreaseKey(neighbourElement,distance + neighbourDistance);
							}
						}
					}
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") Dijkstra medium: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("DijkstraMedium PairingHeap");
		writeDirect("C:/temp/dijkstraMediumPairingHeap.csv","PairingHeap.dijkstraMedium",result);
	}

	/**
	 * Tests the {@code PriorityQueue} on multiple attributes at once by
	 * performing Dijkstra's Algorithm for shortest paths on random graphs. The
	 * graphs will be sparse in this case. Every vertex is connected to
	 * {@code sqrt(n)} other vertices (asymetrically). Dijkstra's algorithm will
	 * compute the shortest paths from a random vertex to every other vertex in
	 * the graph (single-source, multiple-destination).
	 * <p>This test will analyse the efficiency of the heap mostly on the
	 * efficiency of its {@code insert} and {@code deleteMin} methods. Since the
	 * {@code PriorityQueue} has no {@code decreaseKey} method, this algorithm
	 * simply adds a new element every time a key needs decreasing.</p>
	 */
	@Test
	public void dijkstraMediumPriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<VertexElem> heap = new PriorityQueue<>();
				final PriorityQueueVertex[] graph = priorityQueueGraph(size,(int)Math.sqrt(size));
				final PriorityQueueVertex start = graph[rng.nextInt(size)]; //Pick a random starting vertex.
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.add(new VertexElem(0.0,start)); //Distance to starting vertex is zero.
				while(!heap.isEmpty()) {
					final VertexElem current = heap.poll();
					current.value.visited = true;
					//Here is where you would report the distance to the vertex (current.getKey()).
					final double distance = current.key;
					final PriorityQueueVertex currentVertex = current.value;
					final PriorityQueueVertex[] neighbours = currentVertex.neighbours;
					final double[] neighbourDistances = currentVertex.neighbourDistances;
					for(int i = neighbours.length - 1;i >= 0;i--) {
						final PriorityQueueVertex neighbour = neighbours[i];
						final double neighbourDistance = neighbourDistances[i];
						final VertexElem neighbourElement = neighbour.element;
						if(!neighbour.visited && (neighbourElement == null || distance + neighbourDistance < neighbourElement.key)) { //Haven't visited this vertex yet.
							final VertexElem elem = new VertexElem(distance + neighbourDistance,neighbour);
							neighbour.element = elem;
							heap.add(elem);
						}
					}
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") Dijkstra medium: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("DijkstraMedium PriorityQueue");
		writeDirect("C:/temp/dijkstraMediumPriorityQueue.csv","PriorityQueue.dijkstraMedium",result);
	}

	/**
	 * Tests the {@code PairingHeap} on multiple attributes at once by
	 * performing Dijkstra's Algorithm for shortest paths on random graphs. The
	 * graphs will be sparse in this case. Every vertex is connected to
	 * {@code 10} other vertices (asymetrically). Dijkstra's algorithm will
	 * compute the shortest paths from a random vertex to every other vertex in
	 * the graph (single-source, multiple-destination).
	 * <p>This test will analyse the efficiency of the heap mostly on the
	 * efficiency of its {@code insert} and {@code deleteMin} methods.</p>
	 */
	@Ignore
	@Test
	public void dijkstraSparsePairingHeap() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PairingHeap<Double,Vertex> heap = new PairingHeap<>();
				final Vertex[] graph = graph(size,10);
				final Vertex start = graph[rng.nextInt(size)]; //Pick a random starting vertex.
				System.gc();
				spin();
				final long before = System.nanoTime();
				start.element = heap.insert(0.0,start); //Distance to starting vertex is zero.
				while(!heap.isEmpty()) {
					final PairingHeap<Double,Vertex>.Element current = heap.deleteMin();
					//Here is where you would report the distance to the vertex (current.getKey()).
					final double distance = current.getKey();
					final Vertex currentVertex = current.getValue();
					final Vertex[] neighbours = currentVertex.neighbours;
					final double[] neighbourDistances = currentVertex.neighbourDistances;
					for(int i = neighbours.length - 1;i >= 0;i--) {
						final Vertex neighbour = neighbours[i];
						final double neighbourDistance = neighbourDistances[i];
						final PairingHeap<Double,Vertex>.Element neighbourElement = neighbour.element;
						if(neighbourElement == null) { //Haven't seen this vertex yet.
							neighbour.element = heap.insert(distance + neighbourDistance,neighbour);
						} else { //Already seen it. Only need to decrease the key, perhaps.
							if(distance + neighbourDistance < neighbourElement.getKey()) {
								heap.decreaseKey(neighbourElement,distance + neighbourDistance);
							}
						}
					}
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PairingHeap(" + size + ") Dijkstra sparse: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("DijkstraSparse PairingHeap");
		writeDirect("C:/temp/dijkstraSparsePairingHeap.csv","PairingHeap.dijkstraSparse",result);
	}

	/**
	 * Tests the {@code PriorityQueue} on multiple attributes at once by
	 * performing Dijkstra's Algorithm for shortest paths on random graphs. The
	 * graphs will be sparse in this case. Every vertex is connected to
	 * {@code 10} other vertices (asymetrically). Dijkstra's algorithm will
	 * compute the shortest paths from a random vertex to every other vertex in
	 * the graph (single-source, multiple-destination).
	 * <p>This test will analyse the efficiency of the heap mostly on the
	 * efficiency of its {@code insert} and {@code deleteMin} methods. Since the
	 * {@code PriorityQueue} has no {@code decreaseKey} method, this algorithm
	 * simply adds a new element every time a key needs decreasing.</p>
	 */
	@Ignore
	@Test
	public void dijkstraSparsePriorityQueue() {
		final double[] result = new double[maxSize / sizeIncrement];
		for(int size = sizeIncrement;size < maxSize;size += sizeIncrement) {
			long time = 0;
			for(int r = 0;r < repeats;r++) {
				final PriorityQueue<VertexElem> heap = new PriorityQueue<>();
				final PriorityQueueVertex[] graph = priorityQueueGraph(size,10);
				final PriorityQueueVertex start = graph[rng.nextInt(size)]; //Pick a random starting vertex.
				System.gc();
				spin();
				final long before = System.nanoTime();
				heap.add(new VertexElem(0.0,start)); //Distance to starting vertex is zero.
				while(!heap.isEmpty()) {
					final VertexElem current = heap.poll();
					current.value.visited = true;
					//Here is where you would report the distance to the vertex (current.getKey()).
					final double distance = current.key;
					final PriorityQueueVertex currentVertex = current.value;
					final PriorityQueueVertex[] neighbours = currentVertex.neighbours;
					final double[] neighbourDistances = currentVertex.neighbourDistances;
					for(int i = neighbours.length - 1;i >= 0;i--) {
						final PriorityQueueVertex neighbour = neighbours[i];
						final double neighbourDistance = neighbourDistances[i];
						final VertexElem neighbourElement = neighbour.element;
						if(!neighbour.visited && (neighbourElement == null || distance + neighbourDistance < neighbourElement.key)) { //Haven't visited this vertex yet.
							final VertexElem elem = new VertexElem(distance + neighbourDistance,neighbour);
							neighbour.element = elem;
							heap.add(elem);
						}
					}
				}
				final long after = System.nanoTime();
				time += after - before;
			}
			result[size / sizeIncrement] = (double)time / repeats;
			System.out.println("PriorityQueue(" + size + ") Dijkstra sparse: " + ((double)time / repeats));
		}
		results.add(result);
		resultsHeaders.add("DijkstraSparse PriorityQueue");
		writeDirect("C:/temp/dijkstraSparsePriorityQueue.csv","PriorityQueue.dijkstraSparse",result);
	}

	/**
	 * Tests the {@link PairingHeap#entrySet()} method.
	 */
	@Ignore
	@Test
	public void entrySetPairingHeap() {
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
		writeDirect("C:/temp/entrySetPairingHeap.csv","PairingHeap.entrySet",result);
	}

	/**
	 * Tests generating a set of all elements in a {@code PriorityQueue}. Since
	 * the {@code PriorityQueue} has no such equivalent to {@code PairingHeap}'s
	 * version, a set is generated by iterating over the heap and adding the
	 * elements one by one. This comparison is likely not fair.
	 */
	@Ignore
	@Test
	public void entrySetPriorityQueue() {
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
		writeDirect("C:/temp/entrySetPriorityQueue.csv","PriorityQueue.entrySet",result);
	}

	/**
	 * Tests the {@link PairingHeap#findMin()} method.
	 */
	@Ignore
	@Test
	public void findMinPairingHeap() {
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
		writeDirect("C:/temp/findMinPairingHeap.csv","PairingHeap.findMin",result);
	}

	/**
	 * Tests the {@link PriorityQueue#peek()} method.
	 */
	@Ignore
	@Test
	public void findMinPriorityQueue() {
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
		writeDirect("C:/temp/findMinPriorityQueue.csv","PriorityQueue.findMin",result);
	}

	/**
	 * Tests the {@link PairingHeap#insert(K,V)} method. Because of the
	 * amortized nature of the {@code PriorityQueue}'s version of this method,
	 * the method is tested on every different size, even those between the
	 * increment, and then averaged. This should clearly show the break points
	 * where the {@code PriorityQueue} expands its array.
	 */
	@Ignore
	@Test
	public void insertPairingHeap() {
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
		writeDirect("C:/temp/insertPairingHeap.csv","PairingHeap.insert",result);
	}

	/**
	 * Tests the {@link PriorityQueue#add(E)} method. Because of the amortized
	 * nature of this method, the method is tested on every different size, even
	 * those between the increment, and then averaged. This should clearly show
	 * the break points where the {@code PriorityQueue} expands its array.
	 */
	@Ignore
	@Test
	public void insertPriorityQueue() {
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
		writeDirect("C:/temp/insertPriorityQueue.csv","PriorityQueue.insert",result);
	}

	/**
	 * Tests the {@link PairingHeap#isEmpty()} method.
	 * <p>The heap will not be empty, except for the first iteration when
	 * {@code size == 0}.</p>
	 */
	@Ignore
	@Test
	public void isEmptyPairingHeap() {
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
		writeDirect("C:/temp/isEmptyPairingHeap.csv","PairingHeap.isEmpty",result);
	}

	/**
	 * Tests the {@link PriorityQueue#isEmpty()} method.
	 * <p>The heap will not be empty, except for the first iteration when
	 * {@code size == 0}.</p>
	 */
	@Ignore
	@Test
	public void isEmptyPriorityQueue() {
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
		writeDirect("C:/temp/isEmptyPriorityQueue.csv","PriorityQueue.isEmpty",result);
	}

	/**
	 * Tests the {@code PairingHeap}'s iterator. The time of a complete
	 * iteration of the heap is measured.
	 */
	@Ignore
	@Test
	public void iteratorPairingHeap() {
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
		writeDirect("C:/temp/iteratorPairingHeap.csv","PairingHeap.iterator",result);
	}

	/**
	 * Tests the {@code PriorityQueue}'s iterator. The time of a complete
	 * iteration of the heap is measured.
	 */
	@Ignore
	@Test
	public void iteratorPriorityQueue() {
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
		writeDirect("C:/temp/iteratorPriorityQueue.csv","PriorityQueue.iterator",result);
	}

	/**
	 * Tests the {@code PairingHeap}'s sorted iterator. The time of a complete
	 * (ordered) iteration of the heap is measured.
	 */
	@Ignore
	@Test
	public void iteratorSortedPairingHeap() {
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
		writeDirect("C:/temp/iteratorSortedPairingHeap.csv","PairingHeap.iteratorSorted",result);
	}

	/**
	 * Tests the {@link PairingHeap#keySet()} method.
	 */
	@Ignore
	@Test
	public void keySetPairingHeap() {
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
		writeDirect("C:/temp/keySetPairingHeap.csv","PairingHeap.keySet",result);
	}

	/**
	 * Tests the {@link PairingHeap#merge(PairingHeap)} method. The heap is
	 * merged with a different random heap of the same size.
	 */
	@Ignore
	@Test
	public void mergePairingHeap() {
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
		writeDirect("C:/temp/mergePairingHeap.csv","PairingHeap.merge",result);
	}

	/**
	 * Tests merging two {@code PriorityQueue}s. Since no such method is
	 * available in {@code PriorityQueue}, the heaps are merged by adding all
	 * elements of one heap to the other. The two heaps have the same size.
	 */
	@Ignore
	@Test
	public void mergePriorityQueue() {
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
		writeDirect("C:/temp/mergePriorityQueue.csv","PriorityQueue.merge",result);
	}

	/**
	 * Tests the {@link PairingHeap#values()} method.
	 */
	@Ignore
	@Test
	public void valuesPairingHeap() {
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
		writeDirect("C:/temp/valuesPairingHeap.csv","PairingHeap.values",result);
	}

	/**
	 * Tests the {@link PairingHeap#size()} method.
	 */
	@Ignore
	@Test
	public void sizePairingHeap() {
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
		writeDirect("C:/temp/sizePairingHeap.csv","PairingHeap.size",result);
	}

	/**
	 * Tests the {@link PriorityQueue#size()} method.
	 */
	@Ignore
	@Test
	public void sizePriorityQueue() {
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
		writeDirect("C:/temp/sizePriorityQueue.csv","PriorityQueue.size",result);
	}

	/**
	 * Tests the {@link PairingHeap#toArray()} method.
	 */
	@Ignore
	@Test
	public void toArrayPairingHeap() {
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
		writeDirect("C:/temp/toArrayPairingHeap.csv","PairingHeap.toArray",result);
	}

	/**
	 * Tests the {@link PriorityQueue#toArray()} method.
	 * <p>It is assumed that the {@link PriorityQueue#toArray(T[])} method is
	 * more or less the same, so no test is created for that.</p>
	 */
	@Ignore
	@Test
	public void toArrayPriorityQueue() {
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
		writeDirect("C:/temp/toArrayPriorityQueue.csv","PriorityQueue.toArray",result);
	}

/////////////////////////////////HELPER METHODS/////////////////////////////////

	/**
	 * Generates a new random graph, composed of {@code Vertex} instances. Each
	 * vertex will be randomly connected to {@code degree} other vertices. The
	 * arcs in the graph will be weighted with a random double between 0 and 1.
	 * Note that the graph is not symmetrical: If there is an edge from vertex
	 * {@code v} to vertex {@code w}, that doesn't mean there is an edge from
	 * {@code w} to {@code v}, and if there is, it doesn't necessarily have the
	 * same weight.
	 * @param size The number of vertices in the graph.
	 * @param degree The number of edges for every vertex.
	 * @return A new random graph.
	 */
	private Vertex[] graph(final int size,final int degree) {
		if(degree >= size) {
			throw new IllegalArgumentException("The degree for the graph is too high. It would require every vertex to be connected to " + degree + " other vertices, but there are only " + size + " vertices in total.");
		}
		final Vertex[] result = new Vertex[size];
		for(int i = size - 1;i >= 0;i--) { //Create vertices and allocate memory for them.
			result[i] = new Vertex(degree);
		}
		for(int i = size - 1;i >= 0;i--) { //Connect vertices randomly.
			result[i].connect(result);
		}
		return result;
	}

	/**
	 * Generates a new random graph, composed of {@code PriorityQueueVertex}
	 * instances. Each vertex will be randomly connected to {@code degree} other
	 * vertices. The arcs in the graph will be weighted with a random double
	 * between 0 and 1. Note that the graph is not symmetrical: If there is an
	 * edge from vertex {@code v} to vertex {@code w}, that doesn't mean there
	 * is an edge from {@code w} to {@code v}, and if there is, it doesn't
	 * necessarily have the same weight.
	 * @param size The number of vertices in the graph.
	 * @param degree The number of edges for every vertex.
	 * @return A new random graph.
	 */
	private PriorityQueueVertex[] priorityQueueGraph(final int size,final int degree) {
		if(degree >= size) {
			throw new IllegalArgumentException("The degree for the graph is too high. It would require every vertex to be connected to " + degree + " other vertices, but there are only " + size + " vertices in total.");
		}
		final PriorityQueueVertex[] result = new PriorityQueueVertex[size];
		for(int i = size - 1;i >= 0;i--) { //Create vertices and allocate memory for them.
			result[i] = new PriorityQueueVertex(degree);
		}
		for(int i = size - 1;i >= 0;i--) { //Connect vertices randomly.
			result[i].connect(result);
		}
		return result;
	}

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
	 * Writes a single test directly to the specified file.
	 * @param filename The filename to write the file to.
	 * @param title The title of the new file.
	 * @param result The recorded test results to write to the file.
	 */
	private void writeDirect(final String filename,final String title,final double result[]) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(filename);
			try (BufferedWriter bw = new BufferedWriter(fw)) {
				bw.write("size;");
				bw.write(title);
				bw.write("\n");
				for(int i = 0;i * sizeIncrement < maxSize;i++) {
					bw.write("" + (i * sizeIncrement));
					bw.write(";" + result[i]);
					bw.write("\n");
				}
				bw.flush();
			}
		} catch(final IOException e) { //Couldn't write to file.
			e.printStackTrace();
			return;
		}
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
			bw.flush();
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
		 * The supposed number of neighbours of this vertex.
		 */
		final int capacity;

		/**
		 * A link to the {@code PairingHeap} element that represents this
		 * vertex. This allows Dijkstra's Algorithm to find the heap element to
		 * decrease the key of when it explores the neighbours of a vertex.
		 * <p>When the vertex is first explored, this field is set to an
		 * element. Once it has been set, it shouldn't be changed.</p>
		 */
		PairingHeap<Double,Vertex>.Element element;

		/**
		 * The distances to every neighbouring vertex.
		 */
		double[] neighbourDistances;

		/**
		 * All neighbouring vertices of this vertex.
		 */
		Vertex[] neighbours;

		/**
		 * Constructs a new vertex. The adjacency lists of the vertex will have
		 * the specified capacity.
		 * @param capacity The capacity of the adjacency lists of the vertex.
		 */
		Vertex(final int capacity) {
			this.capacity = capacity;
			neighbourDistances = new double[capacity];
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
			neighbours = selection.toArray(new Vertex[capacity]);
			for(int i = capacity - 1;i >= 0;i--) {
				neighbourDistances[i] = rng.nextDouble();
			}
		}
	}

	/**
	 * A vertex that, when combined with other vertices, forms a simple graph.
	 * The edges are implemented using an adjacency list.
	 * <p>This version is meant for the {@code PriorityQueue}, so it stores
	 * {@code VertexElem}s for quick access, rather than
	 * {@code PairingHeap.Element}s.</p>
	 */
	private class PriorityQueueVertex {
		/**
		 * The supposed number of neighbours of this vertex.
		 */
		final int capacity;

		/**
		 * A link to the {@code VertexElem} that represents this vertex. This
		 * allows Dijkstra's Algorithm to find the heap element to decrease the
		 * key of when it explores the neighbours of a vertex.
		 * <p>When the vertex is first explored, this field is set to an
		 * element. Once it has been set, it shouldn't be changed.</p>
		 */
		VertexElem element;

		/**
		 * The distances to every neighbouring vertex.
		 */
		double[] neighbourDistances;

		/**
		 * All neighbouring vertices of this vertex.
		 */
		PriorityQueueVertex[] neighbours;

		/**
		 * Whether Dijkstra's Algorithm has already visited this vertex. This is
		 * required since the heap may contain this vertex multiple times
		 * through different {@code VertexElem}s.
		 */
		boolean visited;

		/**
		 * Constructs a new vertex. The adjacency lists of the vertex will have
		 * the specified capacity.
		 * @param capacity The capacity of the adjacency lists of the vertex.
		 */
		PriorityQueueVertex(final int capacity) {
			this.capacity = capacity;
			neighbourDistances = new double[capacity];
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
		void connect(final PriorityQueueVertex[] candidates) {
			final int numCandidates = candidates.length;
			if(numCandidates - 1 < capacity) { //Throws NullPointerException if candidates is null.
				throw new IllegalArgumentException("There must be enough unique candidates to fill the neighbours list.");
			}
			final IdentityHashSet<PriorityQueueVertex> selection = new IdentityHashSet<>(capacity);
			while(selection.size() < capacity) {
				final PriorityQueueVertex candidate = candidates[rng.nextInt(numCandidates)];
				if(candidate != this) {
					selection.add(candidate);
				}
			}
			neighbours = selection.toArray(new PriorityQueueVertex[capacity]);
			for(int i = capacity - 1;i >= 0;i--) {
				neighbourDistances[i] = rng.nextDouble();
			}
		}
	}

	/**
	 * A simple key-value pair that can be compared by its key, but holds
	 * vertices. The purpose of this helper class is to provide the
	 * {@code PriorityQueue} with light-weight elements of which the key may
	 * change. The alternative is to create new {@code Vertex} elements for
	 * every decrease of key, but those vertices may (in dense graphs) take a
	 * linear amount of memory and would have a linear construction time. That's
	 * why this solution was chosen, albeit a little cumbersome.
	 */
	private class VertexElem implements Comparable<VertexElem> {
		/**
		 * The key of the element. This indicates the distance of the vertex to
		 * the source.
		 */
		Double key;

		/**
		 * The value of the element. This is the vertex this element represents.
		 */
		final PriorityQueueVertex value;

		/**
		 * Constructs a new {@code VertexElem} with the specified key and value.
		 * @param key The key for the new {@code VertexElem}.
		 * @param value The vertex for the new {@code VertexElem}.
		 */
		VertexElem(final Double key,final PriorityQueueVertex value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * Compares this {@code VertexElem} with another {@code VertexElem}.
		 * Only the keys are compared and the result is returned.
		 * @param other The {@code VertexElem} to compare this element with.
		 * @return The result of comparing the keys of the elements.
		 */
		@Override
		public int compareTo(final VertexElem other) {
			return Double.compare(key,other.key);
		}
	}
}
