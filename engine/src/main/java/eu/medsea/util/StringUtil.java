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

import java.io.UnsupportedEncodingException;

/**
 * A string utility class with various string manipulation functions
 */
public class StringUtil {

    static final byte[] HEX_CHAR_TABLE = {(byte) '0', (byte) '1', (byte) '2',
            (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c',
            (byte) 'd', (byte) 'e', (byte) 'f'};

    /**
     * Convert a byte array into a hex string representation
     *
     * @param raw the byte [] to convert to a hex string representation
     * @return the hex representation
     * @throws UnsupportedEncodingException
     */
    public static String getHexString(byte[] raw)
            throws UnsupportedEncodingException {
        if (raw == null) {
            return "";
        }
        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for (int i = 0; i < raw.length; i++) {
            byte b = raw[i];
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex, "ASCII");
    }

    public static boolean contains(String target, String content) {
        if (target.indexOf(content) != -1) {
            return true;
        }
        return false;
    }

}