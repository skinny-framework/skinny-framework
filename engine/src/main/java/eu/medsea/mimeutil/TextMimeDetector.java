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
/**
 * <p>
 * </p>
 *
 * @author Steven McArdle
 */
package eu.medsea.mimeutil;

import eu.medsea.mimeutil.detector.MimeDetector;
import eu.medsea.mimeutil.handler.TextMimeHandler;
import eu.medsea.util.EncodingGuesser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This MimeDetector cannot be registered, unregistered or subclassed.
 * It is a default MimeDetector that is pre-installed into the mime-util utility and
 * is used as the FIRST MimeDetector.
 * <p>
 * You can influence this MimeDetector in several ways.
 * <ul>
 * <li>Specify a different list of preferred encodings using the static TextMimeDetector.setPreferredEncodings(...) method.</li>
 * <li>Change the list of supported encodings using the static EncodingGuesser.setSupportedEncodings(...) method.</li>
 * <li>Register TextMimeHandler(s) using the static TextMimeDetector.registerTextMimeHandler(...) method (very, VERY powerful).</li>
 * </ul>
 * <p>
 * The TextMimeDetector.setPreferredEncodings(...) method is used to provide a preferred list of encodings. The final encoding for the MimeType
 * will be the first one in this list that is also contained in the possible encodings returned from the EncodingGuesser class. If none of
 * these match then the first entry in the possible encodings collection is used.
 * </p>
 * <p>
 * The EncodingGuesser.setSupportedEncodings(...) method is used to set the list of encodings that will be considered when trying to guess the
 * encoding. If you provide encodings that are not supported by your JVM an error is logged and the next encoding is tried. If you set this to an
 * empty Collection then you will effectively turn this MimeDetector OFF (the default). This is the recommended way to disable this MimeDetector.
 * The most common usage scenario for this method is when your application is designed to support only a limited set of encodings such as
 * UTF-8 and UTF-16 encoded text files. You can set the supported encodings list to this sub set of encodings and improve the performance
 * of this MimeDetector greatly.
 * </p>
 * <p>
 * The TextMimeDetector.registerTextMimeHandler(...) method can be used to register special TextMimeHandler(s). These MimeHandler(s) are
 * delegated to when once valid encodings have been found for the content contained in File, InputStream or byte []. The handlers can influence
 * both the returned MimeType and encoding of any matched content. For instance, the default behavior is to return a MimeType of text/plain and
 * encoding set according to the rules above. The Handler(s) allow you to further process the content and decide that it is in fact a text/xml
 * or application/svg-xml or even mytype/mysubtype. You can also change the assigned encoding as it may be wrong for your new MimeType.
 * For instance, if you decide the MimeType is really an XML file and not just a standard text/plain file and the detector calculated that the
 * best encoding is UTF-8 but you detect and encoding attribute in the XML content for ISO-8859-1, you can set this as well thus returning
 * a TextMimeType of application/xml with an encoding or ISO-8859-1 instead of a TextMimeType of text/plain and an encoding of UTF-8.<br/><br/>
 * IMPORTANT: Your handler(s) will only get to see and act on content that this MimeDetector thinks is text in the first place. So if your
 * restrictions on supported encodings will no longer detect a file as text then your handler(s) will never be called.
 * </p>
 * </p>
 * <p>
 * The methods will do their best to eliminate any binary files before trying to detect an encoding.
 * However, if a binary file contains only a few bytes of data or you are very unlucky it could be
 * mistakenly recognised as a text file and processed by this MimeDetector.
 * </p>
 * <p>
 * The Collection(s) returned from the methods in this class will contain either 0 or 1 MimeType entry
 * of type TextMimeType with a mime type of "text/plain" or whatever matching registered TextMimeHandler(s) decide to return.
 * You can test for matches from this MimeDetector by using the instanceof operator on the Collection of returned MimeType(s) to your code
 * (remember, the returned Collection to you is the accumulated collection from ALL registered MimeDetectors. You can retrieve the
 * encoding using the getEncoding() method of TextMimeType after casting the MimeType to a TextMimeType.
 * </p>
 * <p>
 * You should also remember that if this MimeDetector puts a TextMimeType into the eventual Collection of MimeType(s) returned to your code
 * of say "text/plain" and one or more of the other registered MimeDetector(s) also add an instance of "text/plain" in accordance with their
 * detection rules, the type will not be changed from TextMimeType to MimeType. Only the specificity value of the MimeType will be increased
 * thus improving the likelihood that this MimeType will be returned from the MimeUtil.getMostSpecificMimeType(Collection mimeTypes) method.
 * </p>
 *
 * @author Steven McArdle
 */
public final class TextMimeDetector extends MimeDetector {

    private static Logger log = LoggerFactory.getLogger(TextMimeDetector.class);

    // The maximum amount of data to retrieve from a stream
    private static final int BUFFER_SIZE = 1024;

    // No text file should have 2 or more consecutive NULL values
    private static final int MAX_NULL_VALUES = 1;

    private static Collection preferredEncodings = new ArrayList();

    static {
        TextMimeDetector.setPreferredEncodings(new String[]{"UTF-16", "UTF-8", "ISO-8859-1", "windows-1252", "US-ASCII"});
    }

    // Registered list of TextMimeHandler(s)
    private static Collection handlers = new ArrayList();

    // Private so nobody can register one using the MimeUtil.registerMimeDetector(...) method
    private TextMimeDetector() {
    }

    // Package scoped so that the class can still be create for use by mime-util without resorting to a singleton approach
    // Could change this in the future !!!
    TextMimeDetector(int dummy) {
        this();
    }

    public String getDescription() {
        return "Determine if a file or stream contains a text mime type. If so then return TextMimeType with text/plain and the best guess encoding.";
    }

    /**
     * This MimeDetector requires content so defer to the file method
     */
    public Collection getMimeTypesFileName(String fileName) throws UnsupportedOperationException {
        return getMimeTypesFile(new File(fileName));
    }

    /**
     * We only want to deal with the stream from the URL
     */
    public Collection getMimeTypesURL(URL url) throws UnsupportedOperationException {
        InputStream in = null;
        try {
            return getMimeTypesInputStream(in = new BufferedInputStream(MimeUtil.getInputStreamForURL(url)));
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new MimeException(e);
        } finally {
            try {
                in.close();
            } catch (Exception ignore) {
                log.error(ignore.getLocalizedMessage());
            }
        }
    }

    /**
     * We only want to deal with the stream for the file
     */
    public Collection getMimeTypesFile(File file) throws UnsupportedOperationException {
        if (!file.exists()) {
            throw new UnsupportedOperationException("This MimeDetector requires actual content.");
        }
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            return getMimeTypesInputStream(in);
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new MimeException(e);
        } finally {
            try {
                in.close();
            } catch (Exception ignore) {
                log.error(ignore.getLocalizedMessage());
            }
        }
    }

    public Collection getMimeTypesInputStream(InputStream in) throws UnsupportedOperationException {
        int offset = 0;
        int len = TextMimeDetector.BUFFER_SIZE;
        byte[] data = new byte[len];
        byte[] copy = null;
        // Mark the input stream
        in.mark(len);

        try {
            // Since an InputStream might return only some data (not all
            // requested), we have to read in a loop until
            // either EOF is reached or the desired number of bytes have been
            // read.
            int restBytesToRead = len;
            while (restBytesToRead > 0) {
                int bytesRead = in.read(data, offset, restBytesToRead);
                if (bytesRead < 0)
                    break; // EOF

                offset += bytesRead;
                restBytesToRead -= bytesRead;
            }
            if (offset < len) {
                copy = new byte[offset];
                System.arraycopy(data, 0, copy, 0, offset);
            } else {
                copy = data;
            }
        } catch (IOException ioe) {
            throw new MimeException(ioe);
        } finally {
            try {
                // Reset the input stream to where it was marked.
                in.reset();
            } catch (Exception e) {
                throw new MimeException(e);
            }
        }
        return getMimeTypesByteArray(copy);
    }

    public Collection getMimeTypesByteArray(byte[] data) throws UnsupportedOperationException {

        // Check if the array contains binary data
        if (EncodingGuesser.getSupportedEncodings().isEmpty() || isBinary(data)) {
            throw new UnsupportedOperationException();
        }

        Collection mimeTypes = new ArrayList();

        Collection possibleEncodings = EncodingGuesser.getPossibleEncodings(data);
        if (log.isDebugEnabled()) {
            log.debug("Possible encodings [" + possibleEncodings.size() + "] " + possibleEncodings);
        }

        if (possibleEncodings.isEmpty()) {
            // Is not a text file understood by this JVM
            throw new UnsupportedOperationException();
        }

        String encoding = null;
        // Iterate over the preferedEncodings array in the order defined and return the first one found
        for (Iterator it = TextMimeDetector.preferredEncodings.iterator(); it.hasNext(); ) {
            encoding = (String) it.next();
            if (possibleEncodings.contains(encoding)) {
                mimeTypes.add(new TextMimeType("text/plain", encoding));
                break;
            }
        }
        // If none of the preferred encodings were acceptable lets see if the default encoding can be used.
        if (mimeTypes.isEmpty() && possibleEncodings.contains(EncodingGuesser.getDefaultEncoding())) {
            encoding = EncodingGuesser.getDefaultEncoding();
            mimeTypes.add(new TextMimeType("text/plain", encoding));
        }

        // If none of our preferredEncodings or the default encoding are in the possible encodings list we return the first possibleEncoding;
        if (mimeTypes.isEmpty()) {
            Iterator it = possibleEncodings.iterator();
            encoding = (String) it.next();
            mimeTypes.add(new TextMimeType("text/plain", encoding));
        }

        if (mimeTypes.isEmpty() || handlers.isEmpty()) {
            // Nothing to handle
            return mimeTypes;
        }

        // String will be passed in as  is currently in the encoding defined by encoding
        try {
            int lengthBOM = EncodingGuesser.getLengthBOM(encoding, data);
            String content = new String(EncodingGuesser.getByteArraySubArray(data, lengthBOM, data.length - lengthBOM), encoding);
            return fireMimeHandlers(mimeTypes, content);
        } catch (UnsupportedEncodingException ignore) {
            // This should never, never, never happen
        }
        return mimeTypes;
    }

    /**
     * Change the list of preferred encodings.
     * This list is used where multiple possible encodings are identified to refer to
     * the contents in a byte array passed in or read in from a Stream or File object.
     * <p>
     * This list is iterated over in order and the first match is set as the encoding for
     * the text/plain TextMimeType ONLY if the JVM default encoding is not in the list.
     * <p>
     * If the neither the defaultEncoding or any of these preferred encodings are in
     * the list of possible encodings then the first possible encoding will be used.
     *
     * @param encodings String array of canonical encoding names.
     */
    public static void setPreferredEncodings(String[] encodings) {
        TextMimeDetector.preferredEncodings = EncodingGuesser.getValidEncodings(encodings);
        if (log.isDebugEnabled()) {
            log.debug("Preferred Encodings set to " + TextMimeDetector.preferredEncodings);
        }
    }

    /**
     * Give registered TextMimeHandler(s) the opportunity to influence the
     * actual mime type before returning from the getMimeTypesXXX(...) methods
     *
     * @param mimeTypes
     * @param content
     * @return
     */
    private Collection fireMimeHandlers(Collection mimeTypes, String content) {
        // We only have one entry in the mimeTypes Collection due to the way
        // this MimeDetector works.
        TextMimeType mimeType = (TextMimeType) mimeTypes.iterator().next();

        for (Iterator it = handlers.iterator(); it.hasNext(); ) {
            TextMimeHandler tmh = (TextMimeHandler) it.next();
            if (tmh.handle(mimeType, content)) {
                // The first handler to return true will short circuit the rest of the handlers
                break;
            }
        }
        return mimeTypes;
    }

    /*
     * This is a quick check for the byte array to see if it contains binary data.
     *
     * As no known text encoding can have more than MAX_NULL_VALUES consecutive null values the
     * method does a quick and dirty elimination of what are probably binary files but should never eliminate possible text files.
     *
     * It is possible that some binary files will not have MAX_NULL_VALUES consecutive byte
     * values especially if it's a small file and will slip through here. Later tests should eliminate these.
     *
     * We will modify this method to include other known sequences as and when we discover them
     */
    private boolean isBinary(byte[] data) {
        int negCount = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                negCount++;
            } else {
                negCount = 0;
            }
            if (negCount == MAX_NULL_VALUES) {
                return true;
            }
        }
        return false;
    }

}