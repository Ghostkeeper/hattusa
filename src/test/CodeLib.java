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

import net.dulek.collections.ArrayListInt;
import net.dulek.collections.FibonacciHeap;
import net.dulek.collections.Treap;
import java.util.*;
import java.util.logging.Logger;

/**
 * This main class can be used as a sort of unit test for the code library.
 * @author Ruben Dulek
 */
public class CodeLib implements Runnable {
	private static final Logger LOG = Logger.getLogger(CodeLib.class.getName());

	/**
	 * Makes an instance of itself and reads it.
	 * @param args Command line arguments. These are ignored.
	 */
	public static void main(String[] args) {
		CodeLib me = new CodeLib();
		me.run();
	}

	/**
	 * Runs the program itself in a non-static environment.
	 */
	@Override
	public void run() {
		for(int i = Integer.MAX_VALUE;i > Integer.MIN_VALUE;i--) {
			//Spin to warm up.
		}

		final int tests = 1_000_000;
		final int tests2 = 1_000;

		for(int i = tests;i > 0;i--) {
			for(int j = tests2;j > 0;j--) {
				int me = net.dulek.math.Math.power(i,j);
				double them = Math.pow(i,j);
				if(!Double.isInfinite(them) && !Double.isNaN(them) && them < Integer.MAX_VALUE && them > 0 && me != them) {
					LOG.warning("Mistake! " + i + "^" + j + " was " + me + ", but should be " + them + "!");
				}
			}
		}

		long start = System.nanoTime();
		for(int i = tests;i > 0;i--) {
			for(int j = tests2;j > 0;j--) {
				net.dulek.math.Math.power(i,j);
			}
		}
		long end = System.nanoTime();
		LOG.info("Duration CodeLib: " + ((end - start) / 1E9) + "s");

		start = System.nanoTime();
		for(int i = tests;i > 0;i--) {
			for(int j = tests2;j > 0;j--) {
				Math.pow(i,j);
			}
		}
		end = System.nanoTime();
		LOG.info("Duration built-in: " + ((end - start) / 1E9) + "s");
	}
}
