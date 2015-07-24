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

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

/**
 * <p>
 * NOTE: Since version 2.1 this class delegates ALL calls to a static instance of the new MimeUtil2 implementation.
 *
 * @author Steven McArdle.
 * @see eu.medsea.mimeutil.MimeUtil2
 * </p>
 * <p>
 * The <code>MimeUtil</code> utility is a utility class that allows applications to detect, work with and manipulate MIME types.
 * </p>
 * <p>
 * A MIME or "Multipurpose Internet Mail Extension" type is an Internet standard that is important outside of just e-mail use.
 * MIME is used extensively in other communications protocols such as HTTP for web communications.
 * IANA "Internet Assigned Numbers Authority" is responsible for the standardisation and publication of MIME types. Basically any
 * resource on any computer that can be located via a URI can be assigned a mime type. So for instance, JPEG images have a MIME type
 * of image/jpg. Some resources can have multiple MIME types associated with them such as files with an XML extension have the MIME types
 * text/xml and application/xml and even specialised versions of xml such as image/svg+xml for SVG image files.
 * </p>
 * <p>
 * To do this <code>MimeUtil</code> uses registered <code>MimeDetector</code>(s) that are delegated to in sequence to actually
 * perform the detection. There a several <code>MimeDetector</code> implementations that come with the utility and
 * you can register and unregister them to perform detection based on file extensions, file globing and magic number detection.<br/>
 * Their is also a fourth MimeDetector that is registered by default that detects text files and encodings. Unlike the other
 * MimeDetector(s) or any MimeDetector(s) you may choose to implement, the TextMimeDetector cannot be registered or
 * unregistered by your code. It is advisable that you read the java doc for the TextMimeDetector as it can be modified in
 * several ways to make it perform better and or detect more specific types.<br/>
 * <p>
 * Please refer to the java doc for each of these <code>MimeDetector</code>(s) for a description of how they
 * actually perform their particular detection process.
 * </p>
 * <p>
 * It is important to note that MIME matching is not an exact science, meaning
 * that a positive match does not guarantee that the returned MIME type is actually correct.
 * It is a best guess method of matching and the matched MIME types should be used with this in
 * mind.
 * </p>
 * <p>
 * New <code>MimeDetector</code>(s) can easily be created and registered with <code>MimeUtil</code> to extend it's
 * functionality beyond these initial detection strategies by extending the <code>AbstractMimeDetector</code> class.
 * To see how to implement your own <code>MimeDetector</code> take a look
 * at the java doc and source code for the ExtensionMimeDetector,MagicMimeMimeDetector and OpendesktopMimeDetector classes.
 * To register and unregister MimeDetector(s) use the [un]registerMimeDetector(...) methods of this class.
 * </p>
 * <p>
 * The order that the <code>MimeDetector</code>(s) are executed is defined by the order each <code>MimeDetector</code> is registered.
 * </p>
 * <p>
 * The resulting <code>Collection</code> of MIME types returned in response to a getMimeTypes(...) call is a normalised list of the
 * accumulation of MIME types returned by each of the registered <code>MimeDetector</code>(s) that implement the specified getMimeTypesXXX(...)
 * methods.
 * </p>
 * <p>
 * All methods in this class that return a Collection object containing MimeType(s) actually return a {@link MimeTypeHashSet}
 * that implements both the {@link Set} and {@link Collection} interfaces.
 * </p>
 */
public class MimeUtil {

    /**
     * While MimeType(s) are being loaded by the MimeDetector(s) they should be
     * added to the list of known MIME types. It is not mandatory for MimeDetector(s)
     * to do so but they should where possible so that the list is as complete as possible.
     * You can add other MIME types to this list using this method. You can then use the
     * isMimeTypeKnown(...) utility methods to see if a MIME type you have
     * matches one that the utility has already seen.
     * <p>
     * This can be used to limit the mime types you work with i.e. if its not been loaded
     * then don't bother using it as it won't match. This is no guarantee that a match will not
     * be found as it is possible that a particular MimeDetector does not have an initialisation
     * phase that loads all of the MIME types it will match.
     * </p>
     * <p>
     * For instance if you had a MIME type of abc/xyz and passed this to
     * isMimeTypeKnown(...) it would return false unless you specifically add
     * this to the know MIME types using this method.
     * </p>
     *
     * @param mimeType a MIME type you want to add to the known MIME types. Duplicates are ignored.
     */
    public static void addKnownMimeType(final MimeType mimeType) {
        MimeUtil2.addKnownMimeType(mimeType);
    }

    /**
     * Utility method to get the InputStream from a URL. Handles several schemes, for instance, if the URL points to a jar
     * entry it will get a proper usable stream from the URL
     *
     * @param url
     * @return
     */
    public static InputStream getInputStreamForURL(URL url) throws Exception {
        return MimeUtil2.getInputStreamForURL(url);
    }

}