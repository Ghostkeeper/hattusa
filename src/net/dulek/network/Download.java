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

package net.dulek.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is a utility function that downloads a file at an URL and returns its
 * contents as a string.
 * @author Ruben Dulek
 * @version 1.0
 */
public class Download {
	/**
	 * Downloads a file from an URL, if a connection is available, and returns
	 * its contents as a String.
	 * <p>Note that this halts the thread until the download is complete. If
	 * this is done in a program that is to be published, always execute this in
	 * a separate thread.
	 * @param url The URL of the file to download.
	 * @param encoding The byte encoding of the remote resource. If unknown, try
	 * UTF-8 or ANSI.
	 * @return The contents of the file as far as it could be downloaded.
	 * @throws MalformedURLException The provided URL is unknown.
	 * @throws IOException The resource could not be read for some reason.
	 * @throws UnsupportedEncodingException The encoding provided is unsupported
	 * or unknown.
	 */
	private static String download(String url,String encoding) throws MalformedURLException,IOException {
		StringBuilder output = new StringBuilder(4096); //Make a StringBuilder to store the result in.
		try(InputStream reader = new URL(url).openStream()) { //Open connection with the URL.
			byte[] buffer = new byte[1024]; //A buffer of 1kb.
			int bytes = 0; //The number of bytes read in this iteration (maximum 1kb).
			while((bytes = reader.read(buffer)) > 0) { //Keep reading from the file.
				output.append(new String(buffer,0,bytes,encoding)); //Convert to a string using the encoding, then append to the output.
			}
		}
		return output.toString(); //Convert to ordinary string, and output.
	}

	/**
	 * Prevents the class from being constructed.
	 */
	private Download() {
		//Unreachable.
	}
}