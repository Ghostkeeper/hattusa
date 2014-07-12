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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Ruben
 */
@Ignore
public class ArrayListIntTest {
	/**
	 * An instance of the default {@code ArrayList&lt;Integer&gt;} class.
	 */
	private ArrayList<Integer> instDef;

	/**
	 * An instance of the specialised {@link ArrayListInt} class.
	 */
	private ArrayListInt instInt;

	/**
	 * The number of repeated tests to perform. This is not microbenchmarking!
	 */
	private final int tests = 100_000;

	/**
	 * The size of the variable-length tests. This generally indicates the size
	 * of the array to work with in tests that have a variable length.
	 */
	private final int size = 100_000;

	/**
	 * The size of the variable-length tests that should run in quadratic time.
	 * This generally indicates the size of the array to work with in tests that
	 * have a variable length.
	 */
	private final int sizeQuadratic = 5000;

	/**
	 * An arbitrary collection to help test linear methods that require a
	 * collection input. Its size will be equal to {@code size}.
	 */
	private final Collection<Integer> collection;

	/**
	 * An arbitrary collection to help test quadratic methods that require a
	 * collection input. Its size will be equal to {@code sizeQuadratic}.
	 */
	private final Collection<Integer> collectionQuadratic;

	/**
	 * Creates an instance of both ArrayLists.
	 */
	public ArrayListIntTest() {
		collection = new LinkedHashSet<>(size); //HashSet to reduce the overhead time of lookup methods.
		for(int i = size - 1;i >= 0;i--) {
			collection.add(i);
		}
		collectionQuadratic = new LinkedHashSet<>(sizeQuadratic);
		for(int i = sizeQuadratic - 1;i >= 0;i--) {
			collectionQuadratic.add(i);
		}
	}

	//63.222
	@Ignore
	@Test
	public void defAdd() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>();
			for(int j = size - 1;j >= 0;j--) {
				instDef.add(j);
			}
		}
	}

	//36.517
	@Ignore
	@Test
	public void intAdd() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt();
			for(int j = size - 1;j >= 0;j--) {
				instInt.add(j);
			}
		}
	}

	//50.284
	@Ignore
	@Test
	public void intAddBad() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt();
			for(int j = size - 1;j >= 0;j--) {
				instInt.add(Integer.valueOf(j));
			}
		}
	}

	//152.118
	@Ignore
	@Test
	public void defInsert() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>();
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instDef.add(0,j);
			}
		}
	}

	//143.898
	@Ignore
	@Test
	public void intInsert() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt();
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.add(0,j);
			}
		}
	}

	//144.242
	@Ignore
	@Test
	public void intInsertBad() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt();
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.add(0,Integer.valueOf(j));
			}
		}
	}

	//55.488
	@Ignore
	@Test
	public void defAddAll() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>();
			instDef.addAll(collection);
		}
	}

	//39.408
	@Ignore
	@Test
	public void intAddAll() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt();
			instInt.addAll(collection);
		}
	}

	//141.101
	@Ignore
	@Test
	public void defInsertAll() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>();
			instDef.addAll(collection);
			instDef.addAll(0,collection);
		}
	}

	//100.505
	@Ignore
	@Test
	public void intInsertAll() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt();
			instInt.addAll(collection);
			instInt.addAll(0,collection);
		}
	}

	//45.737
	@Ignore
	@Test
	public void defClear() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>(collection);
			instDef.clear();
		}
	}

	//47.096
	@Ignore
	@Test
	public void intClear() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt(collection);
			instInt.clear();
		}
	}

	//7.109
	@Ignore
	@Test
	@SuppressWarnings("unchecked")
	public void defClone() {
		instDef = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			ArrayList<Integer> clone = (ArrayList<Integer>)instDef.clone();
		}
	}

	//7.078
	@Ignore
	@Test
	public void intClone() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			ArrayListInt clone = instInt.clone();
		}
	}

	//1069.291
	@Ignore
	@Test
	public void defContains() {
		instDef = new ArrayList<>(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instDef.contains(j);
			}
		}
	}

	//378.395
	@Ignore
	@Test
	public void intContains() {
		instInt = new ArrayListInt(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.contains(j);
			}
		}
	}

	//386.864
	@Ignore
	@Test
	public void intContainsBad() {
		instInt = new ArrayListInt(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.contains(Integer.valueOf(j));
			}
		}
	}

	//1219.908
	@Ignore
	@Test
	public void defContainsAll() {
		instDef = new ArrayList<>(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			instDef.containsAll(collectionQuadratic);
		}
	}

	//381.508
	@Ignore
	@Test
	public void intContainsAll() {
		instInt = new ArrayListInt(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			instInt.containsAll(collectionQuadratic);
		}
	}

	//36.424
	@Ignore
	@Test
	public void defEquals() {
		instDef = new ArrayList<>(collection);
		ArrayList<Integer> other = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instDef.equals(other);
		}
	}

	//69.785
	@Ignore
	@Test
	public void intEquals() {
		instInt = new ArrayListInt(collection);
		ArrayListInt other = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instInt.equals(other);
		}
	}

	//0.004
	@Ignore
	@Test
	public void defGet() {
		instDef = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = size - 1;j >= 0;j--) {
				instDef.get(j);
			}
		}
	}

	//0.005
	@Ignore
	@Test
	public void intGet() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = size - 1;j >= 0;j--) {
				instInt.getInt(j);
			}
		}
	}

	//16.914
	@Ignore
	@Test
	public void intGetBad() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = size - 1;j >= 0;j--) {
				instInt.get(j);
			}
		}
	}

	//2.641
	@Ignore
	@Test
	public void defHashCode() {
		instDef = new ArrayList<>(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			instDef.hashCode();
		}
	}

	//0.094
	@Ignore
	@Test
	public void intHashCode() {
		instInt = new ArrayListInt(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			instInt.hashCode();
		}
	}

	//1035.117
	@Ignore
	@Test
	public void defIndexOf() {
		instDef = new ArrayList<>(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instDef.indexOf(j);
			}
		}
	}

	//378.426
	@Ignore
	@Test
	public void intIndexOf() {
		instInt = new ArrayListInt(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.indexOf(j);
			}
		}
	}

	//379.692
	@Ignore
	@Test
	public void intIndexOfBad() {
		instInt = new ArrayListInt(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.indexOf(Integer.valueOf(j));
			}
		}
	}

	//0.0
	@Ignore
	@Test
	public void defIsEmpty() {
		instDef = new ArrayList<>();
		for(int i = tests - 1;i >= 0;i--) {
			instDef.isEmpty();
		}
	}

	//0.0
	@Ignore
	@Test
	public void intIsEmpty() {
		instInt = new ArrayListInt();
		for(int i = tests - 1;i >= 0;i--) {
			instInt.isEmpty();
		}
	}

	//0.344
	//@Ignore
	@Test
	public void defIterator() {
		instDef = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			Iterator<Integer> it = instDef.iterator();
			while(it.hasNext()) {
				it.next();
			}
		}
	}

	//149.273
	@Ignore
	@Test
	public void defIteratorRemove() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>(collectionQuadratic);
			Iterator<Integer> it = instDef.iterator();
			while(it.hasNext()) {
				it.next();
				it.remove();
			}
		}
	}

	//21.47
	//@Ignore
	@Test
	public void intIterator() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			Iterator<Integer> it = instInt.iterator();
			while(it.hasNext()) {
				it.next();
			}
		}
	}

	//138.773
	@Ignore
	@Test
	public void intIteratorRemove() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt(collectionQuadratic);
			Iterator<Integer> it = instInt.iterator();
			while(it.hasNext()) {
				it.next();
				it.remove();
			}
		}
	}

	//1045.227
	@Ignore
	@Test
	public void defLastIndexOf() {
		instDef = new ArrayList<>(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instDef.lastIndexOf(j);
			}
		}
	}

	//378.864
	@Ignore
	@Test
	public void intLastIndexOf() {
		instInt = new ArrayListInt(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.lastIndexOf(j);
			}
		}
	}

	//381.755
	@Ignore
	@Test
	public void intLastIndexOfBad() {
		instInt = new ArrayListInt(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.lastIndexOf(Integer.valueOf(j));
			}
		}
	}

	//0.484
	@Ignore
	@Test
	public void defListIterator() {
		instDef = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			ListIterator<Integer> it = instDef.listIterator(instDef.size() - 1);
			while(it.hasPrevious()) {
				it.previous();
			}
		}
	}

	//79.317
	@Ignore
	@Test
	public void defListIteratorRemove() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>(collectionQuadratic);
			ListIterator<Integer> it = instDef.listIterator(instDef.size() >> 1);
			while(it.hasNext()) {
				it.next();
				it.remove();
				if(it.hasPrevious()) {
					it.previous();
					it.remove();
				}
			}
		}
	}

	//21.892
	@Ignore
	@Test
	public void intListIterator() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			ListIterator<Integer> it = instInt.listIterator(instInt.size() - 1);
			while(it.hasPrevious()) {
				it.previous();
			}
		}
	}

	//76.941
	@Ignore
	@Test
	public void intListIteratorRemove() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt(collectionQuadratic);
			ListIterator<Integer> it = instInt.listIterator(instInt.size() >> 1);
			while(it.hasNext()) {
				it.next();
				it.remove();
				if(it.hasPrevious()) {
					it.previous();
					it.remove();
				}
			}
		}
	}

	//1364.085
	@Ignore
	@Test
	public void defRemove() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>(collectionQuadratic);
			for(int j = 0;j < size;j++) { //Forward loop to make it quadratic.
				instDef.remove(Integer.valueOf(j));
			}
		}
	}

	//433.35
	@Ignore
	@Test
	public void intRemove() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt(collectionQuadratic);
			for(int j = 0;j < size;j++) { //Forward loop to make it quadratic.
				instInt.removeInt(j);
			}
		}
	}

	//416.787
	@Ignore
	@Test
	public void intRemoveBad() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt(collectionQuadratic);
			for(int j = 0;j < size;j++) { //Forward loop to make it quadratic.
				instInt.remove(Integer.valueOf(j));
			}
		}
	}

	//144.523
	@Ignore
	@Test
	public void defRemoveIndex() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>(collectionQuadratic);
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instDef.remove(0);
			}
		}
	}

	//141.569
	@Ignore
	@Test
	public void intRemoveIndex() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt(collectionQuadratic);
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.removeIndex(0);
			}
		}
	}

	//137.398
	@Ignore
	@Test
	public void intRemoveIndexBad() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt(collectionQuadratic);
			for(int j = sizeQuadratic - 1;j >= 0;j--) {
				instInt.remove(0);
			}
		}
	}

	//6.078
	@Ignore
	@Test
	public void defRemoveAll() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>(collectionQuadratic);
			instDef.removeAll(collectionQuadratic);
		}
	}

	//5.234
	@Ignore
	@Test
	public void intRemoveAll() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt(collectionQuadratic);
			instInt.removeAll(collectionQuadratic);
		}
	}

	//4.531
	@Ignore
	@Test
	public void defRetainAll() {
		instDef = new ArrayList<>(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			instDef.retainAll(collectionQuadratic);
		}
	}

	//3.813
	@Ignore
	@Test
	public void intRetainAll() {
		instInt = new ArrayListInt(collectionQuadratic);
		for(int i = tests - 1;i >= 0;i--) {
			instInt.retainAll(collectionQuadratic);
		}
	}

	//37.893
	@Ignore
	@Test
	public void defSet() {
		instDef = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = size - 1;j >= 0;j--) {
				instDef.set(j,i);
			}
		}
	}

	//1.641
	@Ignore
	@Test
	public void intSet() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = size - 1;j >= 0;j--) {
				instInt.set(j,i);
			}
		}
	}

	//33.439
	@Ignore
	@Test
	public void intSetBad() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			for(int j = size - 1;j >= 0;j--) {
				instInt.set(j,Integer.valueOf(i));
			}
		}
	}

	//0.016
	@Ignore
	@Test
	public void defSize() {
		instDef = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instDef.size();
		}
	}

	//0.0
	@Ignore
	@Test
	public void intSize() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instInt.size();
		}
	}

	//29.096
	@Ignore
	@Test
	public void defSubList() {
		for(int i = tests - 1;i >= 0;i--) {
			instDef = new ArrayList<>(collectionQuadratic);
			List<Integer> subList = instDef.subList(sizeQuadratic >> 2,sizeQuadratic - (sizeQuadratic >> 2));
			for(int j = (subList.size() >> 1) - 1;j >= 0;j--) {
				subList.remove(sizeQuadratic >> 2);
			}
		}
	}

	//27.282
	@Ignore
	@Test
	public void intSubList() {
		for(int i = tests - 1;i >= 0;i--) {
			instInt = new ArrayListInt(collectionQuadratic);
			List<Integer> subList = instInt.subList(sizeQuadratic >> 2,sizeQuadratic - (sizeQuadratic >> 2));
			for(int j = (subList.size() >> 1) - 1;j >= 0;j--) {
				subList.remove(sizeQuadratic >> 2);
			}
		}
	}

	//7.313
	@Ignore
	@Test
	public void defToArray() {
		instDef = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instDef.toArray();
		}
	}

	//32.798
	@Ignore
	@Test
	public void intToArray() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instInt.toArray();
		}
	}

	//22.438
	@Ignore
	@Test
	public void defToIntArray() {
		instDef = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instDef.toArray(new Integer[instDef.size()]);
		}
	}

	//7.125
	@Ignore
	@Test
	public void intToIntArray() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instInt.toArray(new int[instInt.size()]);
		}
	}

	//52.971
	@Ignore
	@Test
	public void intToIntArrayBad() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instInt.toArray(new Integer[instInt.size()]);
		}
	}

	//500.151
	@Ignore
	@Test
	public void defToString() {
		instDef = new ArrayList<>(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instDef.toString();
		}
	}

	//245.451
	@Ignore
	@Test
	public void intToString() {
		instInt = new ArrayListInt(collection);
		for(int i = tests - 1;i >= 0;i--) {
			instInt.toString();
		}
	}
}