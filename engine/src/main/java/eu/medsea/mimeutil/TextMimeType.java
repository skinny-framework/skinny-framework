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
package eu.medsea.mimeutil;

import eu.medsea.util.EncodingGuesser;

/**
 * This class can be used to represent a mime type for a text file.
 * This should only be returned by MimeDetector(s) that use magic number
 * type matching. It allows for an encoding to be associated to a text type
 * mime type such as text/plain.
 *
 * @author Steven McArdle
 */
public class TextMimeType extends MimeType {

    private static final long serialVersionUID = -4798584119063522367L;

    // The default encoding is always set Unknown
    private String encoding = "Unknown";

    /**
     * Construct a TextMimeType from a string representation of a MimeType and
     * an encoding that should be one of the known encodings.
     *
     * @param mimeType
     * @param encoding
     */
    public TextMimeType(final String mimeType, final String encoding) {
        super(mimeType);
        this.encoding = getValidEncoding(encoding);
    }

    /**
     * Get the encoding currently set for this TextMimeType.
     *
     * @return the encoding as a string
     * @see #setEncoding(String)
     */
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String toString() {
        return super.toString() + ";charset=" + getEncoding();
    }

    /**
     * Utility method to see if the passed in encoding is known to this class.
     *
     * @param encoding
     * @return true if encoding passed in is one of the known encodings else false
     */
    private boolean isKnownEncoding(String encoding) {
        return EncodingGuesser.isKnownEncoding(encoding);
    }

    private String getValidEncoding(String encoding) {
        if (isKnownEncoding(encoding)) {
            return encoding;
        } else {
            return "Unknown";
        }
    }

}