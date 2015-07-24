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
package eu.medsea.mimeutil.handler;

import eu.medsea.mimeutil.TextMimeType;

/**
 * This interface is to be implemented by all TextMmeDetector(s)
 * that are registered with the TextMimeDetector
 * <p>
 * A scenario in which you would want to use this feature is when
 * handling text files that are ultimately XML type files.
 * </p>
 * <p>
 * These handlers are given a chance to influence the returned
 * MimeType present in the Collection returned from the TextMimeDetector
 * that is pre-registered with mime-util. Each TextMimeHandler will
 * be called in the order they are registered. If the handle(...) method
 * returns true, no more handlers will be called but if handle(...) returns false
 * the next handler in the chain will be called and given a chance to change the
 * information contained in the passed in TextMimeType such as the mediaType, subType
 * and encoding.
 * </p>
 * <p>
 * As these operate in a chain like fashion you can create generic handlers for say
 * XML files that checks the content for the presence of the xml declaration and set
 * the media and sub types of the MimeType to text/xml instead of the default text/plain.
 * You can also change the encoding from the guessed encoding to the encoding defined
 * in the XML file. The next handler could be configured to only execute it's logic
 * if the sub type of the TextMimeType is or contains xml. This handler could then look to see if
 * the content is actually and SVG file and change the media type to application, the sub type to svg+xml
 * and return true from the handle method so that no more handlers in the chain are called because
 * we now know we have the correct information.
 * </p>
 * <p>
 * For some VERY basic implementations of TextMimeHandler(s) using string functions see the unit tests for
 * the TextMimeDetector. For your implementations you will probably want to use regular expressions
 * to determine content or even to decide if this handler is interested in the content.
 * </p>
 * <p>
 * This is one area that you can contribute back to the community. If you have a first class TexMimeHandler
 * implementation for a specific type of text file content then please consider donating it back to
 * the project and we will release this in a future contributed library. You could even sell these as
 * commercial add ons if it's the bees knees for a specific, hard to detect, type of text content.
 * </p>
 *
 * @author Steven McArdle
 */
public interface TextMimeHandler {

    /**
     * All TextMimeHandler(s) will have this method that has a chance
     * to re-handle what the TextMimeDetector has decided
     *
     * @param mimeType what the current TextMimeType looks like i.e. it's
     *                 current MimeType and encoding
     * @param content  is the actual text you can use to better determine what this text really is
     * @return if true is returned then no more registered TextMimeHandler(s) will fire after this.
     * false means that the next registered TextMimeHandler in the list gets a chance to also change
     * the MimeType and encoding.
     */
    boolean handle(final TextMimeType mimeType, final String content);

}