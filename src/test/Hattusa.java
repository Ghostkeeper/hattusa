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

package test;

import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;
import net.dulek.collections.PairingHeap;

/**
 * This main class can be used to debug the Hattusa code library with
 * breakpoints enabled.
 * @author Ruben Dulek
 */
public class Hattusa implements Runnable {
	private static final Logger LOG = Logger.getLogger(Hattusa.class.getName());

	/**
	 * Makes an instance of itself and reads it.
	 * @param args Command line arguments. These are ignored.
	 */
	public static void main(String[] args) {
		Hattusa me = new Hattusa();
		me.run();
	}

	/**
	 * Runs the program itself in a non-static environment.
	 */
	@Override
	public void run() {
		PairingHeap<Integer,String> heap = new PairingHeap<>();
		String value = "test";
		PairingHeap<Integer,String>.Element elem6 = heap.insert(6,value);
		PairingHeap<Integer,String>.Element elem4 = heap.insert(4,value);
		PairingHeap<Integer,String>.Element elem10 = heap.insert(10,value);
		PairingHeap<Integer,String>.Element elem1 = heap.insert(1,value);
		PairingHeap<Integer,String>.Element elem5 = heap.insert(5,value);
		PairingHeap<Integer,String>.Element elem7 = heap.insert(7,value);
		heap.delete(elem7);
		Iterator<PairingHeap<Integer,String>.Element> iterator = heap.iteratorSorted();
		while(iterator.hasNext()) {
			System.out.println(iterator.next());
		}

		//Correctness.
		Random rng = new Random(0x12345678);
		PairingHeap<Integer,String> me = new PairingHeap<>();
		for(int i = 30 - 1;i >= 0;i--) {
			me.insert(rng.nextInt(97) + 2,value);
		}
		for(PairingHeap<Integer,String>.Element element : me.entrySet()) {
			System.out.println("Decreasing " + element.getKey() + ".");
			Integer originalKey = element.getKey();
			me.decreaseKey(element,1);
			System.out.println("Restoring " + originalKey + ".");
			me.changeKey(element,originalKey); //Change it back.
		}
	}
}
