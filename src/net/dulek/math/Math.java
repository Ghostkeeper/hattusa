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

package net.dulek.math;

/**
 * A collection of extra mathematical functions. Included in this package are:
 * <br />
 * - <code>intSqrt(x)</code>: Computes the integer square root of
 * <code>x</code>.<br />
 * - <code>roundUpPower2(x)</code>: Rounds x upwards to the next power of 2.
 * @author Ruben Dulek
 */
public class Math {
	/**
	 * This is a look-up table to calculate the square root of some integer
	 * numbers extremely quickly. The table consists of pre-calculated results
	 * of some square roots, computed as follows:<br />
	 * <code>squaresTable[x] = sqrt(256x)</code><br />
	 * The table contains 256 entries, enough to get the roots within a range of
	 * 8 bits.
	 * <p>Warning: This method is not yet optimised enough to actually be faster
	 * on all machines. The default <code>Math.sqrt()</code> implementation is
	 * native and so depends on the JRE implementation. Most likely, on new
	 * machines, simply using <code>Math.sqrt()</code> and casting is faster.
	 * </p>
	 */
	private final static int[] rootsTable = {0,16,22,27,32,35,39,42,45,48,50,53,55,57,59,61,64,65,67,69,71,73,75,76,78,80,81,83,84,86,87,89,90,91,93,94,96,97,98,99,101,102,103,104,106,107,108,109,110,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,128,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,144,145,146,147,148,149,150,150,151,152,153,154,155,155,156,157,158,159,160,160,161,162,163,163,164,165,166,167,167,168,169,170,170,171,172,173,173,174,175,176,176,177,178,178,179,180,181,181,182,183,183,184,185,185,186,187,187,188,189,189,190,191,192,192,193,193,194,195,195,196,197,197,198,199,199,200,201,201,202,203,203,204,204,205,206,206,207,208,208,209,209,210,211,211,212,212,213,214,214,215,215,216,217,217,218,218,219,219,220,221,221,222,222,223,224,224,225,225,226,226,227,227,228,229,229,230,230,231,231,232,232,233,234,234,235,235,236,236,237,237,238,238,239,240,240,241,241,242,242,243,243,244,244,245,245,246,246,247,247,248,248,249,249,250,250,251,251,252,252,253,253,254,254,255};

	/**
	 * This would create an instance of the Math class. That is not intended, so
	 * it is a private constructor.
	 */
	private Math() {
		//Prevent this helper class from being constructed.
	}

	/**
	 * Calculates the integer square root of x. The result is rounded down to
	 * the nearest integer. Input numbers below 289 are calculated extremely
	 * quickly (about 5 times as fast as Math.sqrt()). Higher numbers are
	 * calculated slightly slower (about 3 times as fast).
	 * @param x The input number to take the square root of.
	 * @return The square root of x, rounded down to the nearest integer. If x
	 * was negative, returns -1.
	 */
	public static int intSqrt(int x) {
		if(x < 0) { //Taking a square root of a negative number would result in an irreal number.
			return -1; //Keeping it real.
		}
		if(x < 289) { //For low numbers of 8 bits and lower (and to a certain extent in the 9th bit), we can use the look-up table directly.
			if(x >= 256) { //Extending above our look-up table.
				return rootsTable[x >> 2] >> 3; //Approximate the answer by looking up x / 4 and divide by 8 in stead of 16. Accurate up to 289.
			} //Else
			return rootsTable[x] >> 4; //Since sqrt(x) = sqrt(256x) / sqrt(256). Look up sqrt(256x) in the table. Sqrt(256) = 16, so we can do bitwise division.
		}
		int estimate;
		//The if-statements below will reduce the input number (32-bit integer) to a smaller number of at most 8 bits so it can be looked up in the table.
		//The bit-shifts after looking up in the table are to restore the answer to the look-up table's data range of sqrt(0) to sqrt(65536).
		if(x >= 0x10000) { //More than 16 bits.
			if(x >= 0x1000000) { //More than 24 bits.
				if(x >= 0x10000000) { //More than 28 bits.
					if(x >= 0x40000000) { //31 or 32 bits.
						estimate = rootsTable[x >> 24] << 8; //32 bits - 24 bits = 8 bits, restore by shifting 8 bits left.
					} else { //29 or 30 bits.
						estimate = rootsTable[x >> 22] << 7; //30 bits - 22 bits = 8 bits, restore by shifting 7 bits left.
					}
				} else { //25 to 28 bits.
					if(x >= 4000000) { //27 or 28 bits.
						estimate = rootsTable[x >> 20] << 6; //28 bits - 20 bits = 8 bits, restore by shifting 6 bits left.
					} else { //25 or 26 bits.
						estimate = rootsTable[x >> 18] << 5; //26 bits - 18 bits = 8 bits, restore by shifting 5 bits left.
					}
				}
				//The following convergence formula is derived as follows from Newton's method:
				// - We are 5 to 8 bits away from integer precision. The last 5 to 8 bits are 0 due to the left-bitshifts.
				// - This means estimate < sqrt(x).
				// - Thus x / estimate > sqrt(x).
				// - Thus we can get a new estimate by taking the average of estimate and x / estimate.
				// - Do this twice and we should gain 7 bits of precision. Get the last bit with a boolean check.
				estimate = (estimate + 1 + (x / estimate)) >> 1; //Add the estimate to x / estimate and divide by 2.
				estimate = (estimate + 1 + (x / estimate)) >> 1; //Twice to give us (almost) enough precision.
				return ((estimate * estimate) > x) ? estimate - 1 : estimate; //Are we too high? Then subtract one.
			}
			//17 to 14 bits.
			if(x >= 100000) { //21 to 24 bits.
				if(x >= 400000) { //23 or 24 bits.
					estimate = rootsTable[x >> 16] << 4; //24 bits - 16 bits = 8 bits, restore by shifting 4 bits left.
				} else { //21 or 22 bits.
					estimate = rootsTable[x >> 14] << 3; //22 bits - 14 bits = 8 bits, restore by shifting 3 bits left.
				}
			} else { //17 to 20 bits.
				if(x >= 0x40000) { //19 or 20 bits.
					estimate = rootsTable[x >> 12] << 2; //20 bits - 12 bits = 8 bits, restore by shifting 2 bits left.
				} else { //17 or 18 bits.
					estimate = rootsTable[x >> 10] << 1; //18 bits - 10 bits = 8 bits, restore by shifting 1 bit left.
				}
			}
			//We are 1 to 4 bits away from integer precision. One convergence iteration is enough.
			estimate = (estimate + 1 + (x / estimate)) >> 1;
			return ((estimate * estimate) > x) ? estimate - 1 : estimate;
		}
		//9 to 16 bits.
		//The "+ 1" below is to ensure we are at least as high as the answer. The last bit is done with a boolean check.
		if(x >= 0x1000) { //13 to 16 bits.
			if(x >= 0x4000) { //15 or 16 bits.
				estimate = rootsTable[x >> 8] + 1; //16 bits - 8 bits = 8 bits. No restoration needed!
			} else { //13 or 14 bits.
				estimate = (rootsTable[x >> 6] >> 1) + 1; //14 bits - 6 bits = 8 bits, restore by shifting 1 bit right.
			}
		} else { //9 to 12 bits.
			if(x >= 0x400) { //11 or 12 bits.
				estimate = (rootsTable[x >> 4] >> 2) + 1; //12 bits - 4 bits = 8 bits, restore by shifting 2 bits right.
			} else { //9 or 10 bits.
				estimate = (rootsTable[x >> 2] >> 3) + 1; //10 bits - 2 bits = 8 bits, restore by shifting 3 bits right.
			}
		}
		//The last bit of precision can be obtained with a simple boolean check.
		return ((estimate * estimate) > x) ? estimate - 1 : estimate;
	}

	/**
	 * Raises the {@code base} to the {@code exponent}'s power. The result must
	 * be exactly equal to {@code Math.pow(base,exponent)}, except where integer
	 * overflow has occured. These are the special cases:
	 * <ul><li>If the exponent is {@code 0}, the result is {@code 1}.</li>
	 * <li>If the exponent is {@code 1}, the result is {@code base}.</li>
	 * <li>If the base is {@code 0}, the result is {@code 0}.</li>
	 * <li>If the exponent is negative, the result is {@code 0}.</li></ul>
	 * This method uses exponentation by squaring.
	 * @param base The base of the exponentation.
	 * @param exponent The exponent, i.e. to the how manyeth power to raise the
	 * base.
	 * @return {@code base} raised to the {@code exponent}th power.
	 */
	public static int power(int base,int exponent) {
		if(exponent < 0) { //Negative exponents should be giving a fraction, but since this is ints, we return 0.
			return 0;
		}
		int result = 1;
		while(exponent > 0) {
			if((exponent & 1) > 0) { //If there is still an exponent to raise.
				result *= base;
			}
			base *= base; //Square the base, halve the exponent.
			exponent >>= 1;
		}
		return result;
	}

	/**
	 * Raises the {@code base} to the {@code exponent}'s power. The result must
	 * be exactly equal to {@code Math.pow(base,exponent)}, except where long
	 * overflow has occured. These are the special cases:
	 * <ul><li>If the exponent is {@code 0}, the result is {@code 1}.</li>
	 * <li>If the exponent is {@code 1}, the result is {@code base}.</li>
	 * <li>If the base is {@code 0}, the result is {@code 0}.</li>
	 * <li>If the exponent is negative, the result is {@code 0}.</li>
	 * This method uses exponentiation by squaring.
	 * @param base The base of the exponentiation.
	 * @param exponent The exponent, i.e. to the how manyeth power to raise the
	 * base.
	 * @return {@code base} raised to the {@code exponent}th power.
	 */
	public static long power(long base,int exponent) {
		if(exponent < 0) { //Negative exponents should be giving a fraction, but since this is longs, we return 0.
			return 0;
		}
		long result = 1;
		while(exponent > 0) {
			if((exponent & 1) > 0) { //If there is still an exponent to raise.
				result *= base;
			}
			base *= base; //Square the base, halve the exponent.
			exponent >>= 1;
		}
		return result;
	}

	/**
	 * Rounds a short upwards to the nearest power of 2. It works by filling all
	 * bits lower than the most significant bit with 1's, and then adding 1 to
	 * it, causing all bits to flip and resulting in a power of 2.
	 * @param x The value to round upwards from.
	 * @return The nearest power of two that is higher than or equal to x.
	 */
	public static short roundUpPower2(short x) {
		x--; //If x is already a power of 2, make it round to itself.
		x |= x >> 1;
		x |= x >> 2;
		x |= x >> 4;
		x |= x >> 8;
		return ++x; //Then add 1. All bits flip over and you've got 0b10000..., which is a power of 2.
	}

	/**
	 * Rounds an integer upwards to the nearest power of 2. It works by filling
	 * all bits lower than the most significant bit with 1's, and then adding 1
	 * to it, causing all bits to flip and resulting in a power of 2.
	 * @param x The value to round upwards from.
	 * @return The nearest power of two that is higher than or equal to x.
	 */
	public static int roundUpPower2(int x) {
		x--; //If x is already a power of 2, make it round to itself.
		x |= x >> 1; //These will fill all used bits in the sequence with 1's.
		x |= x >> 2;
		x |= x >> 4;
		x |= x >> 8;
		x |= x >> 16;
		return ++x; //Then add 1. All bits flip over and you've got 0b10000..., which is a power of 2.
	}

	/**
	 * Rounds a long upwards to the nearest power of 2. It works by filling all
	 * bits lower than the most significant bit with 1's, and then adding 1 to
	 * it, causing all bits to flip and resulting in a power of 2.
	 * @param x The value to round upwards from.
	 * @return The nearest power of two that is higher than or equal to x.
	 */
	public static long roundUpPower2(long x) {
		x--; //If x is already a power of 2, make it round to itself.
		x |= x >> 1; //These will fill all used bits in the sequence with 1's.
		x |= x >> 2;
		x |= x >> 4;
		x |= x >> 8;
		x |= x >> 16;
		x |= x >> 32;
		return ++x; //Then add 1. All bits flip over and you've got 0b10000..., which is a power of 2.
	}

	/**
	 * Computes the binary logarithm of an integer. That is, the logarithm base
	 * <code>2</code> of <code>x</code>. The result is rounded down to the
	 * nearest integer. This is one less than the number of bits required to
	 * store the number.
	 * <p>If <code>x</code> is zero, <code>0</code> will be returned. If
	 * <code>x</code> is negative, the result will be nonsense.</p>
	 * @param x The number to take the binary logarithm of.
	 * @return The binary logarithm of <code>x</code>.
	 */
	public static int log2(int x) {
		int log = 0;
		if(x > 0xFFFF) { //Are the most significant 16 bits set? Then the result is at least 16.
			x >>>= 16; //Shift 16 bits, so we'll find the next bits for the consecutive checks.
			log = 16;
		}
		if(x > 0xFF) { //Are the next 8 bits set?
			x >>>= 8;
			log += 8;
		}
		if(x > 0xF) { //Are the next 4 bits set?
			x >>>= 4;
			log += 4;
		}
		if(x > 4) { //Are the next 2 bits set?
			x >>>= 2;
			log += 2;
		}
		return log + (x >>> 1); //And the final bit.
	}
}
