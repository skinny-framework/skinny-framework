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

import eu.medsea.mimeutil.detector.MimeDetector;
import eu.medsea.util.StringUtil;
import eu.medsea.util.ZipJarUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipException;

/**
 * <p>
 * The <code>MimeUtil2</code> is a utility class that allows applications to detect, work with and manipulate MIME types.
 * </p>
 * <p>
 * A MIME or "Multipurpose Internet Mail Extension" type is an Internet standard that is important outside of just e-mail use.
 * MIME is used extensively in other communications protocols such as HTTP for web communications.
 * IANA "Internet Assigned Numbers Authority" is responsible for the standardisation and publication of MIME types. Basically any
 * resource on any computer that can be located via a URL can be assigned a MIME type. So for instance, JPEG images have a MIME type
 * of image/jpg. Some resources can have multiple MIME types associated with them such as files with an XML extension have the MIME types
 * text/xml and application/xml and even specialised versions of xml such as image/svg+xml for SVG image files.
 * </p>
 * <p>
 * To do this <code>MimeUtil2</code> uses registered <code>MimeDetector</code>(s) that are delegated too in sequence to actually
 * perform the detection. There are several <code>MimeDetector</code> implementations that come with the utility and
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
 * New <code>MimeDetector</code>(s) can easily be created and registered with <code>MimeUtil2</code> to extend it's
 * functionality beyond these initial detection strategies by extending the <code>AbstractMimeDetector</code> class.
 * To see how to implement your own <code>MimeDetector</code> take a look
 * at the java doc and source code for the ExtensionMimeDetector, MagicMimeMimeDetector and
 * OpendesktopMimeDetector classes. To register and unregister MimeDetector(s) use the
 * [un]registerMimeDetector(...) methods of this class.
 * </p>
 * <p>
 * The order that the <code>MimeDetector</code>(s) are executed is defined by the order each <code>MimeDetector</code> is registered.
 * </p>
 * <p>
 * The resulting <code>Collection</code> of mime types returned in response to a getMimeTypes(...) call is a normalised list of the
 * accumulation of MIME types returned by each of the registered <code>MimeDetector</code>(s) that implement the specified getMimeTypesXXX(...)
 * methods.
 * </p>
 * <p>
 * All methods in this class that return a Collection object containing MimeType(s) actually return a {@link MimeTypeHashSet}
 * that implements both the {@link Set} and {@link Collection} interfaces.
 * </p>
 *
 * @author Steven McArdle.
 * @since 2.1
 */
public class MimeUtil2 {

    private static Logger log = LoggerFactory.getLogger(MimeUtil2.class);

    /**
     * Mime type used to identify a directory
     */
    public static final MimeType DIRECTORY_MIME_TYPE = new MimeType("application/directory");
    /**
     * Mime type used to identify an unknown MIME type
     */
    // All mime types known to the utility. This is synchronised for multi-threaded use
    // and ALL instances of MimeUtil2 share this list.
    private static Map mimeTypes = Collections.synchronizedMap(new HashMap());

    private MimeDetectorRegistry mimeDetectorRegistry = new MimeDetectorRegistry();

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
     * @param mimeType a MIME type you want to add to the known MIME types.
     *                 Duplicates are ignored.
     */
    public static void addKnownMimeType(final MimeType mimeType) {
        addKnownMimeType(mimeType.toString());
    }


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
     * @param mimeType a MIME type you want to add to the known MIME types.
     *                 Duplicates are ignored.
     */
    public static void addKnownMimeType(final String mimeType) {
        try {

            String key = getMediaType(mimeType);
            Set s = (Set) mimeTypes.get(key);
            if (s == null) {
                s = new TreeSet();
            }
            s.add(getSubType(mimeType));
            mimeTypes.put(key, s);
        } catch (MimeException ignore) {
            // A couple of entries in the magic mime file don't follow the rules
            // so ignore them
        }
    }

    /**
     * Register a MimeDetector and add it to the MimeDetector registry.
     * MimeDetector(s) are effectively singletons as they are keyed against their
     * fully qualified class name.
     *
     * @param mimeDetector This must be the fully qualified name of a concrete instance of an
     *                     AbstractMimeDetector class.
     *                     This enforces that all custom MimeDetector(s) extend the AbstractMimeDetector.
     * @see MimeDetector
     */
    public MimeDetector registerMimeDetector(final String mimeDetector) {
        return mimeDetectorRegistry.registerMimeDetector(mimeDetector);
    }

    /**
     * Utility method to get the major or media part of a mime type i.e. the bit before
     * the '/' character
     *
     * @param mimeType you want to get the media part from
     * @return media type of the mime type
     * @throws MimeException if you pass in an invalid mime type structure
     */
    public static String getMediaType(final String mimeType)
            throws MimeException {
        return new MimeType(mimeType).getMediaType();
    }

    /**
     * Get a Collection of possible MimeType(s) that this byte array could represent
     * according to the registered MimeDetector(s). If no MimeType(s) are detected
     * then the returned Collection will contain only the passed in unknownMimeType
     *
     * @param data
     * @param unknownMimeType used if the registered MimeDetector(s) fail to match any MimeType(s)
     * @return all matching MimeType(s)
     * @throws MimeException
     */
    public final Collection getMimeTypes(final byte[] data, final MimeType unknownMimeType) throws MimeException {
        Collection mimeTypes = new MimeTypeHashSet();
        if (data == null) {
            log.error("byte array cannot be null.");
        } else {
            if (log.isDebugEnabled()) {
                try {
                    log.debug("Getting MIME types for byte array [" + StringUtil.getHexString(data) + "].");
                } catch (UnsupportedEncodingException e) {
                    throw new MimeException(e);
                }
            }
            mimeTypes.addAll(mimeDetectorRegistry.getMimeTypes(data));

            // We don't want the unknownMimeType added to the collection by MimeDetector(s)
            mimeTypes.remove(unknownMimeType);
        }

        // If the collection is empty we want to add the unknownMimetype
        if (mimeTypes.isEmpty()) {
            mimeTypes.add(unknownMimeType);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieved MIME types [" + mimeTypes.toString() + "]");
        }
        return mimeTypes;
    }

    /**
     * Get all of the matching mime types for this file object.
     * The method delegates down to each of the registered MimeHandler(s) and returns a
     * normalised list of all matching mime types. If no matching mime types are found the returned
     * Collection will contain the unknownMimeType passed in.
     *
     * @param file            the File object to detect.
     * @param unknownMimeType
     * @return the Collection of matching mime types. If the collection would be empty i.e. no matches then this will
     * contain the passed in parameter unknownMimeType
     * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
     *                       executed.
     */
    public final Collection getMimeTypes(final File file, final MimeType unknownMimeType) throws MimeException {
        Collection mimeTypes = new MimeTypeHashSet();

        if (file == null) {
            log.error("File reference cannot be null.");
        } else {

            if (log.isDebugEnabled()) {
                log.debug("Getting MIME types for file [" + file.getAbsolutePath() + "].");
            }

            if (file.isDirectory()) {
                mimeTypes.add(MimeUtil2.DIRECTORY_MIME_TYPE);
            } else {
                // Defer this call to the file name and stream methods
                mimeTypes.addAll(mimeDetectorRegistry.getMimeTypes(file));

                // We don't want the unknownMimeType added to the collection by MimeDetector(s)
                mimeTypes.remove(unknownMimeType);
            }
        }
        // If the collection is empty we want to add the unknownMimetype
        if (mimeTypes.isEmpty()) {
            mimeTypes.add(unknownMimeType);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieved MIME types [" + mimeTypes.toString() + "]");
        }
        return mimeTypes;
    }

    /**
     * Get all of the matching mime types for this InputStream object.
     * The method delegates down to each of the registered MimeHandler(s) and returns a
     * normalised list of all matching mime types. If no matching mime types are found the returned
     * Collection will contain the unknownMimeType passed in.
     *
     * @param in              the InputStream object to detect.
     * @param unknownMimeType
     * @return the Collection of matching mime types. If the collection would be empty i.e. no matches then this will
     * contain the passed in parameter unknownMimeType
     * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
     *                       executed.
     */
    public final Collection getMimeTypes(final InputStream in, final MimeType unknownMimeType) throws MimeException {
        Collection mimeTypes = new MimeTypeHashSet();

        if (in == null) {
            log.error("InputStream reference cannot be null.");
        } else {
            if (!in.markSupported()) {
                throw new MimeException("InputStream must support the mark() and reset() methods.");
            }
            if (log.isDebugEnabled()) {
                log.debug("Getting MIME types for InputSteam [" + in + "].");
            }
            mimeTypes.addAll(mimeDetectorRegistry.getMimeTypes(in));

            // We don't want the unknownMimeType added to the collection by MimeDetector(s)
            mimeTypes.remove(unknownMimeType);
        }
        // If the collection is empty we want to add the unknownMimetype
        if (mimeTypes.isEmpty()) {
            mimeTypes.add(unknownMimeType);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieved MIME types [" + mimeTypes.toString() + "]");
        }
        return mimeTypes;
    }

    /**
     * Get all of the matching mime types for this file name .
     * The method delegates down to each of the registered MimeHandler(s) and returns a
     * normalised list of all matching mime types. If no matching mime types are found the returned
     * Collection will contain the unknownMimeType passed in.
     *
     * @param fileName        the name of a file to detect.
     * @param unknownMimeType
     * @return the Collection of matching mime types. If the collection would be empty i.e. no matches then this will
     * contain the passed in parameter unknownMimeType
     * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
     *                       executed.
     */
    public final Collection getMimeTypes(final String fileName, final MimeType unknownMimeType) throws MimeException {
        Collection mimeTypes = new MimeTypeHashSet();

        if (fileName == null) {
            log.error("fileName cannot be null.");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Getting MIME types for file name [" + fileName + "].");
            }

            // Test if this is a directory
            File file = new File(fileName);

            if (file.isDirectory()) {
                mimeTypes.add(MimeUtil2.DIRECTORY_MIME_TYPE);
            } else {
                mimeTypes.addAll(mimeDetectorRegistry.getMimeTypes(fileName));

                // We don't want the unknownMimeType added to the collection by MimeDetector(s)
                mimeTypes.remove(unknownMimeType);
            }
        }
        // If the collection is empty we want to add the unknownMimetype
        if (mimeTypes.isEmpty()) {
            mimeTypes.add(unknownMimeType);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieved MIME types [" + mimeTypes.toString() + "]");
        }
        return mimeTypes;

    }

    public final Collection getMimeTypes(final URL url, final MimeType unknownMimeType) throws MimeException {
        Collection mimeTypes = new MimeTypeHashSet();

        if (url == null) {
            log.error("URL reference cannot be null.");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Getting MIME types for URL [" + url + "].");
            }

            // Test if this is a directory
            File file = new File(url.getPath());
            if (file.isDirectory()) {
                mimeTypes.add(MimeUtil2.DIRECTORY_MIME_TYPE);
            } else {
                // defer these calls to the file name and stream methods
                mimeTypes.addAll(mimeDetectorRegistry.getMimeTypes(url));

                // We don't want the unknownMimeType added to the collection by MimeDetector(s)
                mimeTypes.remove(unknownMimeType);
            }
        }
        // If the collection is empty we want to add the unknownMimetype
        if (mimeTypes.isEmpty()) {
            mimeTypes.add(unknownMimeType);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieved MIME types [" + mimeTypes.toString() + "]");
        }
        return mimeTypes;
    }

    /**
     * Get the most specific match of the Collection of mime types passed in.
     * The Collection
     *
     * @param mimeTypes this should be the Collection of mime types returned
     *                  from a getMimeTypes(...) call.
     * @return the most specific MimeType. If more than one of the mime types in the Collection
     * have the same value then the first one found with this value in the Collection is returned.
     */
    public static MimeType getMostSpecificMimeType(final Collection mimeTypes) {
        MimeType mimeType = null;
        int specificity = 0;
        for (Iterator it = mimeTypes.iterator(); it.hasNext(); ) {
            MimeType mt = (MimeType) it.next();
            if (mt.getSpecificity() > specificity) {
                mimeType = mt;
                specificity = mimeType.getSpecificity();
            }
        }
        return mimeType;
    }

    /**
     * Utility method to get the minor part of a mime type i.e. the bit after
     * the '/' character
     *
     * @param mimeType you want to get the minor part from
     * @return sub type of the mime type
     * @throws MimeException if you pass in an invalid mime type structure
     */
    public static String getSubType(final String mimeType)
            throws MimeException {
        return new MimeType(mimeType).getSubType();
    }

    /**
     * Utility convenience method to check if a particular MimeType instance is actually a TextMimeType.
     * Used when iterating over a collection of MimeType's to help with casting to enable access
     * the the TextMimeType methods not available to a standard MimeType. Can also use instanceof.
     *
     * @param mimeType
     * @return true if the passed in instance is a TextMimeType
     * @see MimeType
     * @see TextMimeType
     */
    public static boolean isTextMimeType(final MimeType mimeType) {
        return mimeType instanceof TextMimeType;
    }

    /**
     * Utility method to get the InputStream from a URL. Handles several schemes, for instance, if the URL points to a jar
     * entry it will get a proper usable stream from the URL
     *
     * @param url
     * @return
     */
    public static InputStream getInputStreamForURL(URL url) throws Exception {
        try {
            return url.openStream();
        } catch (ZipException e) {
            return ZipJarUtil.getInputStreamForURL(url);
        }
    }

}