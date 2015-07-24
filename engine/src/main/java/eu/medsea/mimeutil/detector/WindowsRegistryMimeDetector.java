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

import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Get the content type for a file extension as stored in the Windows Registry
 * The extensions are stored at "HKEY_CLASSES_ROOT"
 * <p>
 * This MimeDetector will only operate on Windows machines. On any other platform
 * the methods throw a UnsupportedOperationException (These are swallowed by the MimeUtil class)
 * Therefore, it is perfectly acceptable to register this MimeDetector with MimeUtil and it
 * will only be used on a Windows Platform. On all other platforms it will just be ignored.
 * </p>
 * <p>
 * To register this MimeDetector use<br/>
 * <code>MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");</code>
 * </p>
 * <p>
 * The Collection returned from the getMimeTypesXXX(...) Methods with contain either a single MimeType
 * or the collection will be empty.
 * </p>
 * <p>
 * This MimeDetector only performs file extension mapping, so the methods taking an InputStream and byte array
 * throw UnsupportedOperationException
 * </p>
 *
 * @author Steven McArdle
 */
public class WindowsRegistryMimeDetector extends MimeDetector {

    private static final String REG_QUERY = "reg query ";
    private static final String CONTENT_TYPE = "\"Content Type\"";

    private static final boolean isWindows = System.getProperty("os.name").startsWith("Windows");

    public String getDescription() {
        return "Get the MIME types of file extensions from the Windows Registry. Will be inafective on non-Windows machines.";
    }

    public Collection getMimeTypesFile(File file)
            throws UnsupportedOperationException {
        try {
            return getMimeTypesURL(file.toURI().toURL());
        } catch (Exception e) {
            throw new MimeException(e);
        }
    }

    public Collection getMimeTypesFileName(String fileName)
            throws UnsupportedOperationException {
        return getMimeTypesFile(new File(fileName));
    }

    public Collection getMimeTypesURL(URL url)
            throws UnsupportedOperationException {
        Collection mimeTypes = new ArrayList();
        if (!isWindows) {
            return mimeTypes;
        }

        String contentType = getContentType(MimeUtil.getExtension(url.getPath()));
        if (contentType != null && contentType.length() > 0) {
            mimeTypes.add(new MimeType(contentType));
        }
        return mimeTypes;
    }

    /**
     * Content detection not supported
     */
    public Collection getMimeTypesByteArray(byte[] data)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "WindowsRegistryMimeDetector does not support detection from byte arrays.");
    }

    /**
     * Content detection not supported
     */
    public Collection getMimeTypesInputStream(InputStream in)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "WindowsRegistryMimeDetector does not support detection from InputStreams.");
    }

    private String getContentType(String extension) {
        if (extension == null || extension.length() < 1) {
            return null;
        }
        try {
            String query = REG_QUERY + "\"HKEY_CLASSES_ROOT\\." + extension + "\" /v " + CONTENT_TYPE;

            Process process = Runtime.getRuntime().exec(query);
            StreamReader reader = new StreamReader(process.getInputStream());

            reader.start();
            process.waitFor();
            reader.join();

            String result = reader.getResult();
            int p = result.indexOf("REG_SZ");

            if (p == -1)
                return null;

            return result.substring(p + "REG_SZ".length()).trim();
        } catch (Exception e) {
            return null;
        }
    }

    static class StreamReader extends Thread {
        private InputStream is;
        private StringWriter sw;

        StreamReader(InputStream is) {
            this.is = is;
            sw = new StringWriter();
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            } catch (IOException e) {
                ;
            }
        }

        String getResult() {
            return sw.toString();
        }
    }

}