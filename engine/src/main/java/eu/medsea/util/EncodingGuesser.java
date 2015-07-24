/*
 * Copyright 2007-2009 Medsea Business Solutions S.L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.medsea.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * This class contains a list of known encodings used by TextMimeType.
 * It is used by the TextMimeDetector but can be used as a stand alone utility class
 * in other parts of your program if you want.
 * <p>
 * The getPossibleEncodings() method takes a byte [] as its source and the bigger the
 * array the better the detection ratio will be.
 * </p>
 * <p>
 * The class is initialised with an empty list of encodings so it is effectively disabled by
 * default. You can set the supported encodings to ALL of the encodings supported by your JVM at
 * any point during your program execution using the following method
 * EncodingGuesser.setSupportedEncodings(EncodingGuesser.getCanonicalEncodingNamesSupportedByJVM());
 * You can also clear the encodings and disable the detector at any point by calling
 * EncodingGuesser.setSupportedEncodings(new ArrayList()). If later on you dynamically
 * add more encodings they will NOT be detected automatically by this class but you can recall the
 * above method.
 * </p>
 * <p>
 * As the JVM can have a large number of encodings and each one is checked against the
 * byte array it may be wise to remove all encodings you are sure you will not use
 * to trim down on the number of tests. It will not stop at the first match but will try to
 * match as many encodings as possible and return this as a Collection.
 * </p>
 * <p>
 * A common scenario is where an application can handle only a small set of text encodings such as UTF-8
 * and windows-1252. If this is your case you can use the setSupportedEncodings() method so that
 * these are the only encodings in the supported encodings Collection.
 * This will dramatically improve the performance of this class.
 * </p>
 * <p>
 * It's possible that small byte arrays that should contain binary data are considered
 * possible text matches but generally binary data, such as images, should return no matches.
 * </p>
 * <p>
 * There are some optimisations that are applicable to text files containing BOM's (Byte Order Marks) such as
 * UTF-8, UTF-16LE, UTF-16BE, UTF-32LE and UTF-32BE. These are not required but if present will greatly improve
 * the resultant possible matches returned from the getPossibleEncodings() method.
 * </p>
 */
public class EncodingGuesser {

    private static Logger log = LoggerFactory.getLogger(EncodingGuesser.class);

    // We want the CANONICAL name of the default Charset for the JVM.
    private static String defaultJVMEncoding = Charset.forName(
            new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream()).getEncoding()).name();

    private static Collection supportedEncodings = new TreeSet();

    private static Map boms = new HashMap();

    /**
     * Initialise the supported encodings to be those supported by the JVM.
     * This will NOT be updated should you later add encodings dynamically to your
     * running code.
     *
     * You can also remove some of these later if you know they will not be used.
     * The more you remove the more performant the it will be.
     */
    static {
        // We have this switched off by default. If you want to initialise with all encodings
        // supported by your JVM the just un-comment the following line
        // EncodingGuesser.supportedEncodings = getCanonicalEncodingNamesSupportedByJVM();

        // Initialise some known BOM (s) keyed by their canonical encoding name.
        boms.put("UTF-32BE", new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF});
        boms.put("UTF-32LE", new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00});
        boms.put("UTF-16BE", new byte[]{(byte) 0xFE, (byte) 0xFF});
        boms.put("UTF-16LE", new byte[]{(byte) 0xFF, (byte) 0xFE});
        boms.put("UTF-8", new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        boms.put("UTF-7", new byte[]{(byte) 0x2B, (byte) 0x2F, (byte) 0x76}); // We may need to cater for the next char as well which can be one of [38 | 39 | 2B | 2F]
        boms.put("UTF-1", new byte[]{(byte) 0xF7, (byte) 0x64, (byte) 0x4C});
        boms.put("UTF-EBCDIC", new byte[]{(byte) 0xDD, (byte) 0x73, (byte) 0x66, (byte) 0x73});
        boms.put("SCSU", new byte[]{(byte) 0x0E, (byte) 0xFE, (byte) 0xFF});
        boms.put("BOCU-1", new byte[]{(byte) 0xFB, (byte) 0xEE, (byte) 0x28}); // optionally followed by 0xFF

    }

    /**
     * Check if the encoding String is one of the encodings supported.
     *
     * @param encoding
     * @return true if encoding is understood by this class
     */
    public static boolean isKnownEncoding(String encoding) {
        return supportedEncodings.contains(encoding);
    }

    /**
     * Get a Collection of all the possible encodings this byte array could be used to represent.
     *
     * @param data
     * @return the Collection of possible encodings from the supported encodings
     */
    public static Collection getPossibleEncodings(byte[] data) {

        Collection possibleEncodings = new TreeSet();
        if (data == null || data.length == 0) {
            return possibleEncodings;
        }

        // We may have to take account of a BOM (Byte Order Mark) as this could be present at the beginning of
        // the source byte array. These sequences may match valid bytes at the beginning of binary data but this shouldn't
        // match any encodings anyway.

        String encoding = null;
        for (Iterator it = supportedEncodings.iterator(); it.hasNext(); ) {
            // This will eliminate encodings it can't possibly be from the supported encodings
            // by converting the source byte array to a String using each encoding in turn and
            // then getting the resultant byte array and checking it against the passed in data.

            try {
                // One problem to overcome is that the passed in data may be terminated by an
                // incomplete character for the current encoding so we need to remove the last character
                // then get the resulting bytes and only match this against the source byte array.

                encoding = (String) it.next();

                // Check if this encoding has a known bom and if so does it match the beginning of the data array ?
                // returns either 0 or the length of the bom
                int lengthBOM = getLengthBOM(encoding, data);

                // Don't use the BOM when constructing the String
                String test = new String(getByteArraySubArray(data, lengthBOM, data.length - lengthBOM), encoding);

                // Only remove the last character if the String is more than 1 character long
                if (test.length() > 1) {
                    // Remove last character from the test string.
                    test = test.substring(0, test.length() - 2);
                }

                // This is the byte array we will compare with the passed in source array copy
                byte[] compare = null;
                try {
                    compare = test.getBytes(encoding);
                } catch (UnsupportedOperationException ignore) {
                    continue;
                }

                // Check if source and destination byte arrays are equal
                if (!compareByteArrays(data, lengthBOM, compare, 0, compare.length)) {
                    // dosn't match so ignore this encoding as it is unlikely to be correct
                    // even if it does contain valid text data.
                    continue;
                }

                // If we get this far and the lengthBOM is not 0 then we have a match for this encoding.
                if (lengthBOM != 0) {
                    // We know we have a perfect match for this encoding so ditch the rest and return just this one
                    possibleEncodings.clear();
                    possibleEncodings.add(encoding);
                    return possibleEncodings;
                }

                // This is a possible match.
                possibleEncodings.add(encoding);
            } catch (UnsupportedEncodingException uee) {
                log.error("The encoding [" + encoding + "] is not supported by your JVM.");
            } catch (Exception e) {
                // Log the error but carry on with the next encoding
                log.error(e.getLocalizedMessage(), e);
            }
        }
        return possibleEncodings;
    }

    /**
     * Get a Collection containing entries in both the supported encodings
     * and the passed in String [] of encodings.
     * <p>
     * This is used by TextMimeDetector to get a valid list of the preferred encodings.
     *
     * @param encodings
     * @return a Collection containing all valid encodings contained in the passed in encodings array
     */
    public static Collection getValidEncodings(String[] encodings) {
        Collection c = new ArrayList();
        for (int i = 0; i < encodings.length; i++) {
            if (supportedEncodings.contains(encodings[i])) {
                c.add(encodings[i]);
            }
        }
        return c;
    }


    /**
     * Get the JVM default canonical encoding. For instance the canonical encoding for cp1252 is windows-1252
     *
     * @return the default canonical encoding name for the JVM
     */
    public static String getDefaultEncoding() {
        return EncodingGuesser.defaultJVMEncoding;
    }

    /**
     * Get the Collection of currently supported encodings
     *
     * @return the supported encodings.
     */
    public static Collection getSupportedEncodings() {
        return supportedEncodings;
    }

    /**
     * Set the supported encodings
     *
     * @param encodings If this is null the supported encodings are left unchanged.
     * @return a copy of the currently supported encodings
     */
    public static Collection setSupportedEncodings(Collection encodings) {
        Collection current = new TreeSet();
        for (Iterator it = supportedEncodings.iterator(); it.hasNext(); ) {
            current.add(it.next());
        }
        if (encodings != null) {
            supportedEncodings.clear();
            for (Iterator it = encodings.iterator(); it.hasNext(); ) {
                supportedEncodings.add(it.next());
            }
        }
        return current;
    }

    /**
     * Get the length of a BOM for this this encoding and byte array
     *
     * @param encoding
     * @param data
     * @return length of BOM if the data contains a BOM else returns 0
     */
    public static int getLengthBOM(String encoding, byte[] data) {
        if (!boms.containsKey(encoding)) {
            return 0;
        }
        byte[] bom = (byte[]) boms.get(encoding);
        if (compareByteArrays(bom, 0, data, 0, bom.length)) {
            return bom.length;
        } else {
            return 0;
        }
    }

    /**
     * Get a sub array of this byte array starting at offset until length
     *
     * @param a
     * @param offset
     * @param length
     * @return new byte array unless is would replicate or increase the original array in which case it returns the original
     */
    public static byte[] getByteArraySubArray(byte[] a, int offset, int length) {
        if ((offset + length > a.length)) {
            return a;
        }
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = a[offset + i];
        }
        return data;
    }

    /**
     * Utility method to compare a region of two byte arrays for equality
     *
     * @param a
     * @param aOffset
     * @param b
     * @param bOffset
     * @param length
     * @return true is the two regions contain the same byte values else false
     */
    public static boolean compareByteArrays(byte[] a, int aOffset, byte[] b, int bOffset, int length) {
        if ((a.length < aOffset + length) || (b.length < bOffset + length)) {
            // would match beyond one of the arrays
            return false;
        }

        for (int i = 0; i < length; i++) {
            if (a[aOffset + i] != b[bOffset + i]) {
                return false;
            }
        }
        return true;
    }

}

