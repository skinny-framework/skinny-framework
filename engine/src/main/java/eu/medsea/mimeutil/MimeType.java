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

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * This class represents a simple MimeType object. A mime type is made up of
 * two parts <code>&lt;media type&gt;/&lt;sub type&gt;</code>.
 * The media type can be something like <code>application</code> or <code>text</code> and
 * the the sub type can be something like <code>xml</code> or <code>plain</code>.
 * <p>
 * Both the media type and sub type can also be the wild card <code>*</code> such as
 * <code>*&#47;*</code> and <code>text&#47;*</code>. Note, if the media type is the wild card
 * then the sub type must also be a wild card.
 *
 * @author Steven McArdle
 */
public class MimeType implements Comparable, Serializable {

    private static final long serialVersionUID = -1324243127744494894L;

    private static final Pattern mimeSplitter = Pattern.compile("[/;]++");

    protected String mediaType = "*";
    protected String subType = "*";

    //This is a estimate of how specific this mime type is
    private int specificity = 1;

    /**
     * Construct a mime type from a String such as <code>text/plain</code>.
     * It tries to ensure that the mime type pattern passed in is correctly
     * formatted.
     *
     * @param mimeType
     * @throws MimeException
     */
    public MimeType(final String mimeType) throws MimeException {
        if (mimeType == null || mimeType.trim().length() == 0) {
            throw new MimeException("Invalid MimeType [" + mimeType + "]");
        }
        String[] parts = mimeSplitter.split(mimeType.trim());

        if (parts.length > 0) {
            // Treat as the mediaType
            mediaType = getValidMediaType(parts[0]);
        }
        if (parts.length > 1) {
            subType = getValidSubType(parts[1]);
        }
    }

    /**
     * Get the media type part of the mime type.
     *
     * @return media type
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Get the sub type of the mime type
     *
     * @return sub type
     */
    public String getSubType() {
        return subType;
    }


    /**
     * See if this MimeType is the same as the passed in mime type string
     *
     * @param mimeType as a String
     * @return true if the MimeType passed in has the same media and sub types, else returns false.
     */
    private boolean match(final String mimeType) {
        return toString().equals(mimeType);
    }

    /**
     * Get the hashCode of this MimeType.
     * The hashCode is calculate as (31 * mediaType.hashCode()) + subType.hashCode()
     *
     * @return calculated hashCode
     * @see Object#hashCode()
     */
    public int hashCode() {
        return (31 * mediaType.hashCode()) + subType.hashCode();
    }

    /**
     * Overrides the equals method of <code>java.lang.Object</code>. This is able to compare
     * against another MimeType instance or a string representation of a mime type.
     *
     * @return true if the types match else false.
     * @see Object#equals(Object o)
     */
    public boolean equals(Object o) {
        if (o instanceof MimeType) {
            if (this.mediaType.equals(((MimeType) o).mediaType) && this.subType.equals(((MimeType) o).subType)) {
                return true;
            }
        } else if (o instanceof String) {
            return match((String) o);
        }
        return false;
    }

    /**
     * Overrides the toString method of <code>java.lang.Object</code>.
     *
     * @return String representation i.e. <code>&lt;media type&gt;/&lt;sub type&gt;.
     * @see Object#toString()
     */
    public String toString() {
        return mediaType + "/" + subType;
    }

    /**
     * This indicates how specific the mime types is i.e. how good a match
     * the mime type is when returned from the getMimeTypes(...) calls.
     * <p>
     * This is calculated by the number of times this MimeType would be returned
     * if the Collection was not normalised. The higher the count the more MimeDetectors
     * have matched this type. As this can be a false positive for types such as application/octect-stream
     * and text/plain where they would be returned by multiple MimeDetector(s). These types are referred to as root
     * mime types where ALL mime types derive from application/octet-stream and all text/* types derive from text/plan
     * so in these cases we set the specificity to 0 no matter how many times they match. This ensures they are regarded
     * as the least specific in the returned Collection.
     * </p>
     *
     * @return how specific this MimeType is according to the rest of the MimeTypes in a Collection.
     */
    public int getSpecificity() {
        return specificity;
    }

    /*
     * Set the value of the specificity. The higher the value the more specific a MimeType is.
     */
    void setSpecificity(final int specificity) {
        this.specificity = specificity;
    }

    /*
     * Check the media type at least looks valid.
     * TODO: Enforce more rigorous checking of valid media types.
     */
    private String getValidMediaType(final String mediaType) {
        if (mediaType == null || mediaType.trim().length() == 0) {
            return "*";
        }
        return mediaType;
    }

    /*
     * Check the sub type at least looks valid.
     * TODO: Enforce more rigorous checking of valid sub types.
     */
    private String getValidSubType(final String subType) {
        if (subType == null || subType.trim().length() == 0 || "*".equals(mediaType)) {
            // If the mediaType is a wild card then the sub type must also be a wild card
            return "*";
        }
        return subType;
    }

    /**
     * Allows us to use MimeType(s) in Sortable Set's such as the TreeSet.
     */
    public int compareTo(Object arg0) {
        if (arg0 instanceof MimeType) {
            return toString().compareTo(((MimeType) arg0).toString());
        }
        return 0;
    }

}