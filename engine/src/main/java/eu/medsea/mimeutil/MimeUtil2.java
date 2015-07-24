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
import java.nio.ByteOrder;
import java.util.*;
import java.util.regex.Pattern;
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
 * at the java doc and source code for the {@link ExtensionMimeDetector}, {@link MagicMimeMimeDetector} and
 * {@link OpendesktopMimeDetector} classes. To register and unregister MimeDetector(s) use the
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
    public static final MimeType UNKNOWN_MIME_TYPE = new MimeType("application/octet-stream");

    private static final Pattern mimeSplitter = Pattern.compile("[/;]++");

    // All mime types known to the utility. This is synchronised for multi-threaded use
    // and ALL instances of MimeUtil2 share this list.
    private static Map mimeTypes = Collections.synchronizedMap(new HashMap());

    // the native byte order of the underlying OS. "BIG" or "little" Endian
    private static ByteOrder nativeByteOrder = ByteOrder.nativeOrder();

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
     * @see #isMimeTypeKnown(String mimeType)
     * @see #isMimeTypeKnown(MimeType mimetType)
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
     * @see #isMimeTypeKnown(String mimetype)
     * @see #isMimeTypeKnown(MimeType mimetType)
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
     * Returns a copy of the Collection of currently known MIME types as strings that have been
     * registered either by the initialisation methods of the MimeDetector(s) or by the user.
     */
    public static Collection getKnownMimeTypes() {
        Collection mimeTypes = new ArrayList();
        Iterator i = MimeUtil2.mimeTypes.keySet().iterator();
        while (i.hasNext()) {
            // Iterate through each set and compose the MIME types
            String mediaType = (String) i.next();
            Iterator it = ((Set) MimeUtil2.mimeTypes.get(mediaType)).iterator();
            while (it.hasNext()) {
                mimeTypes.add(mediaType + "/" + (String) it.next());
            }
        }
        return mimeTypes;
    }

    /**
     * Register a MimeDetector and add it to the MimeDetector registry.
     * MimeDetector(s) are effectively singletons as they are keyed against their
     * fully qualified class name.
     *
     * @param mimeDetector. This must be the fully qualified name of a concrete instance of an
     *                      AbstractMimeDetector class.
     *                      This enforces that all custom MimeDetector(s) extend the AbstractMimeDetector.
     * @see MimeDetector
     */
    public MimeDetector registerMimeDetector(final String mimeDetector) {
        return mimeDetectorRegistry.registerMimeDetector(mimeDetector);
    }

    /**
     * Get the extension part of a file name defined by the file parameter.
     *
     * @param file a file object
     * @return the file extension or null if it does not have one.
     */
    public static String getExtension(final File file) {
        return getExtension(file.getName());
    }

    /**
     * Get the extension part of a file name defined by the fileName parameter.
     * There may be no extension or it could be a single part extension such as
     * .bat or a multi-part extension such as .tar.gz
     *
     * @param fileName a relative or absolute path to a file
     * @return the file extension or null if it does not have one.
     */
    public static String getExtension(final String fileName) {
        if (fileName == null || fileName.length() == 0) {
            return "";
        }
        int index = fileName.indexOf(".");
        return index < 0 ? "" : fileName.substring(index + 1);
    }

    /**
     * Get the first in a comma separated list of mime types. Useful when using
     * extension mapping that can return multiple mime types separate by commas
     * and you only want the first one.
     *
     * @param mimeTypes comma separated list of mime types
     * @return first in a comma separated list of mime types or null if the mimeTypes string is null or empty
     */
    public static MimeType getFirstMimeType(final String mimeTypes) {
        if (mimeTypes != null && mimeTypes.trim().length() != 0) {
            return new MimeType(mimeTypes.split(",")[0].trim());
        }
        return null;
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
     * Utility method to get the quality part of a mime type. If it does not
     * exist then it is always set to q=1.0 unless it's a wild card. For the
     * major component wild card the value is set to 0.01 For the minor
     * component wild card the value is set to 0.02
     * <p>
     * Thanks to the Apache organisation for these settings.
     *
     * @param mimeType a valid mime type string with or without a valid q parameter
     * @return the quality value of the mime type either calculated from the
     * rules above or the actual value defined.
     * @throws MimeException this is thrown if the mime type pattern is invalid.
     */
    public static double getMimeQuality(final String mimeType) throws MimeException {
        if (mimeType == null) {
            throw new MimeException("Invalid MimeType [" + mimeType + "].");
        }
        String[] parts = mimeSplitter.split(mimeType);
        if (parts.length < 2) {
            throw new MimeException("Invalid MimeType [" + mimeType + "].");
        }
        if (parts.length > 2) {
            for (int i = 2; i < parts.length; i++) {
                if (parts[i].trim().startsWith("q=")) {
                    // Get the number part
                    try {
                        // Get the quality factor
                        double d = Double.parseDouble(parts[i].split("=")[1]
                                .trim());
                        return d > 1.0 ? 1.0 : d;
                    } catch (NumberFormatException e) {
                        throw new MimeException(
                                "Invalid MIME quality indicator ["
                                        + parts[i].trim()
                                        + "]. Must be a valid double between 0 and 1");
                    } catch (Exception e) {
                        throw new MimeException(
                                "Error parsing MIME quality indicator.", e);
                    }
                }
            }
        }
        // No quality indicator so always assume its 1 unless a wild card is used
        if (StringUtil.contains(parts[0], "*")) {
            return 0.01;
        } else if (StringUtil.contains(parts[1], "*")) {
            return 0.02;
        } else {
            // Assume q value of 1
            return 1.0;
        }
    }

    /**
     * Get a registered MimeDetector by name.
     *
     * @param name the name of a registered MimeDetector. This is always the fully qualified
     *             name of the class implementing the MimeDetector.
     * @return
     */
    public MimeDetector getMimeDetector(final String name) {
        return mimeDetectorRegistry.getMimeDetector(name);
    }

    /**
     * Get a Collection of possible MimeType(s) that this byte array could represent
     * according to the registered MimeDetector(s). If no MimeType(s) are detected
     * then the returned Collection will contain only the UNKNOWN_MIME_TYPE
     *
     * @param data
     * @return all matching MimeType(s)
     * @throws MimeException
     */
    public final Collection getMimeTypes(final byte[] data) throws MimeException {
        return getMimeTypes(data, UNKNOWN_MIME_TYPE);
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
     * Collection will contain the default UNKNOWN_MIME_TYPE
     *
     * @param file the File object to detect.
     * @return collection of matching MimeType(s)
     * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
     *                       executed.
     */
    public final Collection getMimeTypes(final File file) throws MimeException {
        return getMimeTypes(file, UNKNOWN_MIME_TYPE);
    }

    /**
     * Get all of the matching mime types for this file object.
     * The method delegates down to each of the registered MimeHandler(s) and returns a
     * normalised list of all matching mime types. If no matching mime types are found the returned
     * Collection will contain the unknownMimeType passed in.
     *
     * @param file             the File object to detect.
     * @param unknownMimeType.
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
     * Collection will contain the default UNKNOWN_MIME_TYPE
     *
     * @param in InputStream to detect.
     * @return
     * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
     *                       executed.
     */
    public final Collection getMimeTypes(final InputStream in) throws MimeException {
        return getMimeTypes(in, UNKNOWN_MIME_TYPE);
    }

    /**
     * Get all of the matching mime types for this InputStream object.
     * The method delegates down to each of the registered MimeHandler(s) and returns a
     * normalised list of all matching mime types. If no matching mime types are found the returned
     * Collection will contain the unknownMimeType passed in.
     *
     * @param in               the InputStream object to detect.
     * @param unknownMimeType.
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
     * Get all of the matching mime types for this file name.
     * The method delegates down to each of the registered MimeHandler(s) and returns a
     * normalised list of all matching mime types. If no matching mime types are found the returned
     * Collection will contain the default UNKNOWN_MIME_TYPE
     *
     * @param fileName the name of a file to detect.
     * @return collection of matching MimeType(s)
     * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
     *                       executed.
     */
    public final Collection getMimeTypes(final String fileName) throws MimeException {
        return getMimeTypes(fileName, UNKNOWN_MIME_TYPE);
    }

    /**
     * Get all of the matching mime types for this file name .
     * The method delegates down to each of the registered MimeHandler(s) and returns a
     * normalised list of all matching mime types. If no matching mime types are found the returned
     * Collection will contain the unknownMimeType passed in.
     *
     * @param fileName         the name of a file to detect.
     * @param unknownMimeType.
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

    /**
     * Get all of the matching mime types for this URL object.
     * The method delegates down to each of the registered MimeHandler(s) and returns a
     * normalised list of all matching mime types. If no matching mime types are found the returned
     * Collection will contain the default UNKNOWN_MIME_TYPE
     *
     * @param url a URL to detect.
     * @return Collection of matching MimeType(s)
     * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
     *                       executed.
     */
    public final Collection getMimeTypes(final URL url) throws MimeException {
        return getMimeTypes(url, UNKNOWN_MIME_TYPE);
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
     * Get the native byte order of the OS on which you are running. It will be
     * either big or little endian. This is used internally for the magic mime
     * rules mapping.
     *
     * @return ByteOrder
     */
    public static ByteOrder getNativeOrder() {
        return MimeUtil2.nativeByteOrder;
    }

    /**
     * Gives you the best match for your requirements.
     * <p>
     * You can pass the accept header from a browser request to this method
     * along with a comma separated list of possible mime types returned from
     * say getExtensionMimeTypes(...) and the best match according to the accept
     * header will be returned.
     * </p>
     * <p>
     * The following is typical of what may be specified in an HTTP Accept
     * header:
     * </p>
     * <p>
     * Accept: text/xml, application/xml, application/xhtml+xml,
     * text/html;q=0.9, text/plain;q=0.8, video/x-mng, image/png, image/jpeg,
     * image/gif;q=0.2, text/css, *&#47;*;q=0.1
     * </p>
     * <p>
     * The quality parameter (q) indicates how well the user agent handles the
     * MIME type. A value of 1 indicates the MIME type is understood perfectly,
     * and a value of 0 indicates the MIME type isn't understood at all.
     * </p>
     * <p>
     * The reason the image/gif MIME type contains a quality parameter of 0.2,
     * is to indicate that PNG & JPEG are preferred over GIF if the server is
     * using content negotiation to deliver either a PNG or a GIF to user
     * agents. Similarly, the text/html quality parameter has been lowered a
     * little, to ensure that the XML MIME types are given in preference if
     * content negotiation is being used to serve an XHTML document.
     * </p>
     *
     * @param accept     is a comma separated list of mime types you can accept
     *                   including QoS parameters. Can pass the Accept: header
     *                   directly.
     * @param canProvide is a comma separated list of mime types that can be provided
     *                   such as that returned from a call to
     *                   getExtensionMimeTypes(...)
     * @return the best matching mime type possible.
     */
    public static MimeType getPreferedMimeType(String accept, final String canProvide) {
        if (canProvide == null || canProvide.trim().length() == 0) {
            throw new MimeException(
                    "Must specify at least one MIME type that can be provided.");
        }
        if (accept == null || accept.trim().length() == 0) {
            accept = "*/*";
        }

        // If an accept header is passed in then lets remove the Accept part
        if (accept.indexOf(":") > 0) {
            accept = accept.substring(accept.indexOf(":") + 1);
        }

        // Remove any unwanted spaces from the wanted mime types for instance
        // text/html; q=0.4
        accept = accept.replaceAll(" ", "");

        return getBestMatch(accept, getList(canProvide));
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
     * Check to see if this mime type is one of the types seen during
     * initialisation or has been added at some later stage using
     * addKnownMimeType(...)
     *
     * @param mimeType
     * @return true if the mimeType is in the list else false is returned
     * @see #addKnownMimeType(String mimetype)
     */
    public static boolean isMimeTypeKnown(final MimeType mimeType) {
        try {
            Set s = (Set) mimeTypes.get(mimeType.getMediaType());
            if (s == null) {
                return false;
            }
            return s.contains(mimeType.getSubType());
        } catch (MimeException e) {
            return false;
        }
    }

    /**
     * Check to see if this mime type is one of the types seen during
     * initialisation or has been added at some later stage using
     * addKnownMimeType(...)
     *
     * @param mimeType
     * @return true if the mimeType is in the list else false is returned
     * @see #addKnownMimeType(String mimetype)
     */
    public static boolean isMimeTypeKnown(final String mimeType) {
        return isMimeTypeKnown(new MimeType(mimeType));
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
     * Remove a previously registered MimeDetector
     *
     * @param mimeDetector
     * @return the MimeDetector that was removed from the registry else null.
     */
    public MimeDetector unregisterMimeDetector(final MimeDetector mimeDetector) {
        return mimeDetectorRegistry.unregisterMimeDetector(mimeDetector);
    }

    /**
     * Remove a previously registered MimeDetector
     *
     * @param mimeDetector
     * @return the MimeDetector that was removed from the registry else null.
     */
    public MimeDetector unregisterMimeDetector(final String mimeDetector) {
        return mimeDetectorRegistry.unregisterMimeDetector(mimeDetector);
    }

    /**
     * Get the quality parameter of this mime type i.e. the <code>q=</code> property.
     * This method implements a value system similar to that used by the apache server i.e.
     * if the media type is a * then it's <code>q</code> value is set to 0.01 and if the sub type is
     * a * then the <code>q</code> value is set to 0.02 unless a specific <code>q</code>
     * value is specified. If a <code>q</code> property is set it is limited to a max value of 1.0
     *
     * @param mimeType
     * @return the quality value as a double between 0.0 and 1.0
     * @throws MimeException
     */
    public static double getQuality(final String mimeType) throws MimeException {
        return getMimeQuality(mimeType);
    }

    // Check each entry in each of the wanted lists against the entries in the
    // can provide list.
    // We take into consideration the QoS indicator
    private static MimeType getBestMatch(final String accept, final List canProvideList) {

        if (canProvideList.size() == 1) {
            // If we only have one mime type that can be provided then thats
            // what we provide even if
            // the wanted list does not contain this entry or it's the worst
            // QoS.
            // This will cover the majority of cases
            return new MimeType((String) canProvideList.get(0));
        }

        Map wantedMap = normaliseWantedMap(accept, canProvideList);

        MimeType bestMatch = null;
        double qos = 0.0;
        Iterator it = wantedMap.keySet().iterator();
        while (it.hasNext()) {
            List wantedList = (List) wantedMap.get(it.next());
            Iterator it2 = wantedList.iterator();
            while (it2.hasNext()) {
                String mimeType = (String) it2.next();
                double q = getMimeQuality(mimeType);
                String majorComponent = getMediaType(mimeType);
                String minorComponent = getSubType(mimeType);
                if (q > qos) {
                    qos = q;
                    bestMatch = new MimeType(majorComponent + "/" + minorComponent);
                }
            }
        }
        // Gone through all the wanted list and found the best match possible
        return bestMatch;
    }

    // Turn a comma separated string into a list
    private static List getList(final String options) {
        List list = new ArrayList();
        String[] array = options.split(",");
        for (int i = 0; i < array.length; i++) {
            list.add(array[i].trim());
        }
        return list;
    }

    // Turn a comma separated string of accepted mime types into a Map
    // based on the list of mime types that can be provided
    private static Map normaliseWantedMap(final String accept, final List canProvide) {
        Map map = new LinkedHashMap();
        String[] array = accept.split(",");

        for (int i = 0; i < array.length; i++) {
            String mimeType = array[i].trim();
            String major = getMediaType(mimeType);
            String minor = getSubType(mimeType);
            double qos = getMimeQuality(mimeType);

            if (StringUtil.contains(major, "*")) {
                // All canProvide types are acceptable with the qos defined OR
                // 0.01 if not defined
                Iterator it = canProvide.iterator();
                while (it.hasNext()) {
                    String mt = (String) it.next();
                    List list = (List) map.get(getMediaType(mt));
                    if (list == null) {
                        list = new ArrayList();
                    }
                    list.add(mt + ";q=" + qos);
                    map.put(getMediaType(mt), list);
                }
            } else if (StringUtil.contains(minor, "*")) {
                Iterator it = canProvide.iterator();
                while (it.hasNext()) {
                    String mt = (String) it.next();
                    if (getMediaType(mt).equals(major)) {
                        List list = (List) map.get(major);
                        if (list == null) {
                            list = new ArrayList();
                        }
                        list.add(major + "/" + getSubType(mt) + ";q="
                                + qos);
                        map.put(major, list);
                    }
                }

            } else {
                if (canProvide.contains(major + "/" + minor)) {
                    List list = (List) map.get(major);
                    if (list == null) {
                        list = new ArrayList();
                    }
                    list.add(major + "/" + minor + ";q=" + qos);
                    map.put(major, list);
                }
            }
        }
        return map;
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