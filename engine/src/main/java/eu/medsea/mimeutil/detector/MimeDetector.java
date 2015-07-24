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
package eu.medsea.mimeutil.detector;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

/**
 * ALL MimeDetector(s) must extend this class.
 *
 * @author Steven McArdle
 */
public abstract class MimeDetector {

    /**
     * Gets the name of this MimeDetector
     *
     * @return name of MimeDetector as a fully qualified class name
     */
    public final String getName() {
        return getClass().getName();
    }

    /**
     * Called by MimeUtil.MimeDetectorRegistry.getMimeTypes(String fileName) {}
     *
     * @param fileName
     * @return
     * @throws UnsupportedOperationException
     */
    public final Collection getMimeTypes(final String fileName) throws UnsupportedOperationException {
        return getMimeTypesFileName(fileName);
    }

    /**
     * Called by MimeUtil.MimeDetectorRegistry.getMimeTypes(File file) {}
     *
     * @param file
     * @return
     * @throws UnsupportedOperationException
     */
    public final Collection getMimeTypes(final File file) throws UnsupportedOperationException {
        return getMimeTypesFile(file);
    }

    /**
     * Called by MimeUtil.MimeDetectorRegistry.getMimeTypes(URL url) {}
     *
     * @param url
     * @return
     * @throws UnsupportedOperationException
     */
    public final Collection getMimeTypes(final URL url) throws UnsupportedOperationException {
        return getMimeTypesURL(url);
    }

    /**
     * Called by MimeUtil.MimeDetectorRegistry.getMimeTypes(byte [] data) {}
     *
     * @param data
     * @return
     * @throws UnsupportedOperationException
     */
    public final Collection getMimeTypes(final byte[] data) throws UnsupportedOperationException {
        return getMimeTypesByteArray(data);
    }

    /**
     * Called by MimeUtil.MimeDetectorRegistry.getMimeTypes(InputStream in) {}
     * The InputStream must support the mark() and reset() methods.
     *
     * @param in
     * @return
     * @throws UnsupportedOperationException
     */
    public final Collection getMimeTypes(final InputStream in) throws UnsupportedOperationException {
        // Enforces that the InputStream supports the mark() and reset() methods
        if (!in.markSupported()) {
            throw new UnsupportedOperationException("The InputStream must support the mark() and reset() methods.");
        }
        return getMimeTypesInputStream(in);
    }

    /**
     * You can override this method if you have any special one off initialisation to perform
     * such as allocating resources etc.
     */
    public void init() {
    }

    /**
     * You can override this method if for instance you allocated any resources in the init() method
     * that need to be closed or deallocated specially.
     */
    public void delete() {
    }

    /**
     * Abstract method to be implement by concrete MimeDetector(s).
     *
     * @return description of this MimeDetector
     */
    public abstract String getDescription();

    /**
     * Abstract method that must be implemented by concrete MimeDetector(s). This takes a file name and is
     * called by the MimeUtil getMimeTypes(String fileName) getMimeTypes(File file) getMimeTypes(URL url) methods.
     * If your MimeDetector does not handle file names then either throw an UnsupportedOperationException or return an
     * empty collection.
     *
     * @param fileName
     * @return Collection of matched MimeType(s)
     * @throws UnsupportedOperationException
     */
    protected abstract Collection getMimeTypesFileName(final String fileName) throws UnsupportedOperationException;

    /**
     * Abstract method that must be implemented by concrete MimeDetector(s). This takes a file object and is
     * called by the MimeUtil getMimeTypes(File file) method.
     * If your MimeDetector does not handle file names then either throw an UnsupportedOperationException or return an
     * empty collection.
     *
     * @param file
     * @return Collection of matched MimeType(s)
     * @throws UnsupportedOperationException
     */
    protected abstract Collection getMimeTypesFile(final File file) throws UnsupportedOperationException;

    /**
     * Abstract method that must be implemented by concrete MimeDetector(s). This takes a URL object and is
     * called by the MimeUtil getMimeTypes(URL url) method.
     * If your MimeDetector does not handle file names then either throw an UnsupportedOperationException or return an
     * empty collection.
     *
     * @param url
     * @return Collection of matched MimeType(s)
     * @throws UnsupportedOperationException
     */
    protected abstract Collection getMimeTypesURL(final URL url) throws UnsupportedOperationException;

    /**
     * Abstract method that must be implemented by concrete MimeDetector(s). This takes an InputStream object and is
     * called by the MimeUtil getMimeTypes(URL url), getMimeTypes(File file) and getMimeTypes(InputStream in) methods.
     * If your MimeDetector does not handle InputStream objects then either throw an UnsupportedOperationException or return an
     * empty collection.
     * <p>
     * If the InputStream passed in does not support the mark() and reset() methods a MimeException will be thrown
     * before reaching this point. The implementation is responsible for the actual use of the mark() and reset() methods
     * as the amount of data to retrieve from the stream is implementation and even call by call dependent.
     * If you do not use the mark() and reset() methods on the Stream then the position in the Stream will have moved on when this method returns
     * and the next MimeDetector that handles the stream will either fail or be incorrect.
     * </p>
     * <p>
     * To allow the reuse of the Stream in other parts of your code and by further MimeDetector(s) in a way that it is unaware of
     * any data read via this method i.e. the Stream position will be returned to where it was when this method was called,
     * it is IMPORTANT to utilise the mark() and reset() methods within your implementing method.
     * </p>
     *
     * @param in InputStream.
     * @return Collection of matched MimeType(s)
     * @throws UnsupportedOperationException
     */
    protected abstract Collection getMimeTypesInputStream(final InputStream in) throws UnsupportedOperationException;

    /**
     * Abstract method that must be implemented by concrete MimeDetector(s). This takes a byte [] object and is
     * called by the MimeUtil getMimeTypes(byte []) method.
     * If your MimeDetector does not handle byte [] objects then either throw an UnsupportedOperationException or return an
     * empty collection.
     *
     * @param data byte []. Is a byte array that you want to parse for matching mime types.
     * @return Collection of matched MimeType(s)
     * @throws UnsupportedOperationException
     */
    protected abstract Collection getMimeTypesByteArray(final byte[] data) throws UnsupportedOperationException;

}