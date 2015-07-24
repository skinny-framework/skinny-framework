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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * <p>
 * The extension mime mappings are loaded in the following way.
 * <ol>
 * <li>Load the properties file from the mime utility jar named
 * <code>eu.medsea.mimeutil.mime-types.properties</code>.</li>
 * <li>Locate and load a file named <code>.mime-types.properties</code> from the
 * users home directory if one exists.</li>
 * <li>Locate and load a file named <code>mime-types.properties</code> from the
 * classpath if one exists</li>
 * <li>locate and load a file named by the JVM property
 * <code>mime-mappings</code> i.e.
 * <code>-Dmime-mappings=../my-mime-types.properties</code></li>
 * </ol>
 * Each property file loaded will add to the list of extensions understood by MimeUtil.
 * If there is a clash of extension names then the last one loaded wins, i.e they are not adative, this makes it
 * possible to completely change the mime types associated to a file extension declared in previously loaded property files.
 * The extensions are also case sensitive meaning that bat, bAt, BAT and Bat can all be recognised individually. If however,
 * no match is found using case sensitive matching then it will perform an insensitive match by lower casing the extension
 * of the file to be matched first.
 * </p>
 * <p>
 * Fortunately, we have compiled a relatively large list of mappings into a java properties file from information gleaned from many sites on the Internet.
 * This file resides in the eu.medsea.util.mime-types.properties file and is not guaranteed to be correct or contain all the known mappings for a file
 * extension type. This is not a complete or exhaustive list as that would have proven too difficult to compile for this project.
 * So instead we give you the opportunity to extend and override these mappings for yourself as defined above.
 * Obviously, to use this method you don't actually need a file object, you just need a file name with an extension. Also, if you have given or renamed a
 * file using a different extension than the one that it would normally be associated with then this mapping will return the wrong mime-type and
 * if the file has no extension at all, such as Make, then it's not going to be possible to determine a mime type using this technique
 * </p>
 * <p>
 * We acquired many mappings from many different sources on the net for the
 * extension mappings. The internal list is quite large and there can be many
 * associated mime types. These may not match what you are expecting so you can
 * add the mapping you want to change to your own property file following the
 * rules above. If you provide a mapping for an extension then any previously
 * loaded mappings will be removed and only the mappings you define will be
 * returned. This can be used to map certain extensions that are incorrectly
 * returned for our environment defined in the internal property file.
 * </p>
 * <p>
 * If we have not provided a mapping for a file extension that you know the mime
 * type for you can add this to your custom property files so that a correct mime
 * type is returned for you.
 * <p>
 * <p>
 * We use the <code>application/directory</code> mime type to identify
 * directories. Even though this is not an official mime type it seems to be
 * well accepted on the net as an unofficial mime type so we thought it was OK
 * for us to use as well.
 * </p>
 * <p>
 * This class is auto loaded by MimeUtil as it has an entry in the file called MimeDetectors.
 * MimeUtil reads this file at startup and calls Class.forName() on each entry found. This mean
 * the MimeDetector must have a no arg constructor.
 * </p>
 *
 * @author Steven McArdle.
 */
public class ExtensionMimeDetector extends MimeDetector {

    private static Logger log = LoggerFactory.getLogger(ExtensionMimeDetector.class);

    // Extension MimeTypes
    private static Map extMimeTypes;

    public ExtensionMimeDetector() {
        ExtensionMimeDetector.initMimeTypes();
    }

    public String getDescription() {
        return "Get the mime types of file extensions";
    }

    /**
     * Get the mime type of a file using extension mappings. The file path
     * can be a relative or absolute path or can refer to a completely non-existent file as
     * only the extension is important here.
     *
     * @param file points to a file or directory. May not actually exist
     * @return collection of the matched mime types.
     * @throws MimeException if errors occur.
     */
    public Collection getMimeTypesFile(final File file) throws MimeException {
        return getMimeTypesFileName(file.getName());
    }

    /**
     * Get the mime type of a URL using extension mappings. Only the extension is important here.
     *
     * @param url is a valid URL
     * @return collection of the matched mime types.
     * @throws MimeException if errors occur.
     */
    public Collection getMimeTypesURL(final URL url) throws MimeException {
        return getMimeTypesFileName(url.getPath());
    }

    /**
     * Get the mime type of a file name using file name extension mappings. The file name path
     * can be a relative or absolute path or can refer to a completely non-existent file as
     * only the extension is important here.
     *
     * @param fileName points to a file or directory. May not actually exist
     * @return collection of the matched mime types.
     * @throws MimeException if errors occur.
     */
    public Collection getMimeTypesFileName(final String fileName) throws MimeException {
        Collection mimeTypes = new HashSet();

        String fileExtension = MimeUtil.getExtension(fileName);
        while (fileExtension.length() != 0) {
            String types = null;
            // First try case sensitive
            types = (String) extMimeTypes.get(fileExtension);
            if (types != null) {
                String[] mimeTypeArray = types.split(",");
                for (int i = 0; i < mimeTypeArray.length; i++) {
                    mimeTypes.add(new MimeType(mimeTypeArray[i]));
                }
                return mimeTypes;
            }
            if (mimeTypes.isEmpty()) {
                // Failed to find case insensitive extension so lets try again with
                // lowercase
                types = (String) extMimeTypes.get(fileExtension.toLowerCase());
                if (types != null) {
                    String[] mimeTypeArray = types.split(",");
                    for (int i = 0; i < mimeTypeArray.length; i++) {
                        mimeTypes.add(new MimeType(mimeTypeArray[i]));
                    }
                    return mimeTypes;
                }
            }
            fileExtension = MimeUtil.getExtension(fileExtension);
        }
        return mimeTypes;
    }

    /*
     * This loads the mime-types.properties files that define mime types based
     * on file extensions using the following load sequence 1. Loads the
     * property file from the mime utility jar named
     * eu.medsea.mime.mime-types.properties. 2. Locates and loads a file named
     * .mime-types.properties from the users home directory if one exists. 3.
     * Locates and loads a file named mime-types.properties from the classpath
     * if one exists 4. locates and loads a file named by the JVM property
     * mime-mappings i.e. -Dmime-mappings=../my-mime-types.properties
     */
    private static void initMimeTypes() {
        InputStream is = null;
        extMimeTypes = new Properties();
        try {
            // Load the file extension mappings from the internal property file and
            // then
            // from the custom property files if they can be found
            try {
                // Load the default supplied mime types
                is = MimeUtil.class.getClassLoader().getResourceAsStream(
                        "eu/medsea/mimeutil/mime-types.properties");
                if (is != null) {
                    ((Properties) extMimeTypes).load(is);
                }
            } catch (Exception e) {
                // log the error but don't throw the exception up the stack
                log.error("Error loading internal mime-types.properties", e);
            } finally {
                is = closeStream(is);
            }

            // Load any .mime-types.properties from the users home directory
            try {
                File f = new File(System.getProperty("user.home")
                        + File.separator + ".mime-types.properties");
                if (f.exists()) {
                    is = new FileInputStream(f);
                    if (is != null) {
                        log.debug("Found a custom .mime-types.properties file in the users home directory.");
                        Properties props = new Properties();
                        props.load(is);
                        if (props.size() > 0) {
                            extMimeTypes.putAll(props);
                        }
                        log.debug("Successfully parsed .mime-types.properties from users home directory.");
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse .magic.mime file from users home directory. File will be ignored.", e);
            } finally {
                is = closeStream(is);
            }

            // Load any classpath provided mime types that either extend or
            // override the default mime type entries. Could also be in jar files.
            // Get an enumeration of all files on the classpath with this name. They could be in jar files as well
            try {
                Enumeration e = MimeUtil.class.getClassLoader().getResources("mime-types.properties");
                while (e.hasMoreElements()) {
                    URL url = (URL) e.nextElement();
                    if (log.isDebugEnabled()) {
                        log.debug("Found custom mime-types.properties file on the classpath [" + url + "].");
                    }
                    Properties props = new Properties();
                    try {
                        is = url.openStream();
                        if (is != null) {
                            props.load(is);
                            if (props.size() > 0) {
                                extMimeTypes.putAll(props);
                                if (log.isDebugEnabled()) {
                                    log.debug("Successfully loaded custome mime-type.properties file [" + url + "] from classpath.");
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Failed while loading custom mime-type.properties file [" + url + "] from classpath. File will be ignored.");
                    }
                }
            } catch (Exception e) {
                log.error("Problem while processing mime-types.properties files(s) from classpath. Files will be ignored.", e);
            } finally {
                is = closeStream(is);
            }

            try {
                // Load any mime extension mappings file defined with the JVM
                // property -Dmime-mappings=../my/custom/mappings.properties
                String fname = System.getProperty("mime-mappings");
                if (fname != null && fname.length() != 0) {
                    is = new FileInputStream(fname);
                    if (is != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found a custom mime-mappings property defined by the property -Dmime-mappings ["
                                    + System.getProperty("mime-mappings") + "].");
                        }
                        Properties props = new Properties();
                        props.load(is);
                        if (props.size() > 0) {
                            extMimeTypes.putAll(props);
                        }
                        log.debug("Successfully loaded the mime mappings file from property -Dmime-mappings ["
                                + System.getProperty("mime-mappings") + "].");
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to load the mime-mappings file defined by the property -Dmime-mappings ["
                        + System.getProperty("mime-mappings") + "].");
            } finally {
                is = closeStream(is);
            }
        } finally {
            // Load the mime types into the known mime types map of MimeUtil
            Iterator it = extMimeTypes.values().iterator();
            while (it.hasNext()) {
                String[] types = ((String) it.next()).split(",");
                for (int i = 0; i < types.length; i++) {
                    MimeUtil.addKnownMimeType(types[i]);
                }
            }
        }
    }

    /**
     * This method is required by the abstract MimeDetector class. As we do not support extension mapping of streams
     * we just throw an {@link UnsupportedOperationException}. This ensures that the getMimeTypes(...) methods ignore this
     * method. We could also have just returned an empty collection.
     */
    public Collection getMimeTypesInputStream(InputStream in)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This MimeDetector does not support detection from streams.");
    }

    /**
     * This method is required by the abstract MimeDetector class. As we do not support extension mapping of byte arrays
     * we just throw an {@link UnsupportedOperationException}. This ensures that the getMimeTypes(...) methods ignore this
     * method. We could also have just returned an empty collection.
     */
    public Collection getMimeTypesByteArray(byte[] data)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This MimeDetector does not support detection from byte arrays.");
    }

}
