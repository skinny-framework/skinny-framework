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
import eu.medsea.mimeutil.MimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The magic mime rules files are loaded in the following way.
 * <ol>
 * <li>From a JVM system property <code>magic-mime</code> i.e
 * <code>-Dmagic-mime=../my/magic/mime/rules</code></li>
 * <li>From any file named <code>magic.mime</code> that can be found on the
 * classpath</li>
 * <li>From a file named <code>.magic.mime</code> in the users home directory</li>
 * <li>From the normal Unix locations <code>/usr/share/file/magic.mime</code>
 * and <code>/etc/magic.mime</code> (in that order)</li>
 * <li>From the internal <code>magic.mime</code> file
 * <code>eu.medsea.mimeutil.magic.mime</code> if, and only if, no files are
 * located in step 4 above.</li>
 * </ol>
 * Each rule file is appended to the end of the existing rules so the earlier in
 * the sequence you define a rule means this will take precedence over rules
 * loaded later. </p>
 * <p>
 * You can add new mime mapping rules using the syntax defined for the Unix
 * magic.mime file by placing these rules in any of the files or locations
 * listed above. You can also change an existing mapping rule by redefining the
 * existing rule in one of the files listed above. This is handy for some of the
 * more sketchy rules defined in the existing Unix magic.mime files.
 * <p>
 * We extended the string type rule which allows you to match strings in a file
 * where you do not know the actual offset of the string containing magic file
 * information it goes something like �what I am looking for will be �somewhere�
 * within the next n characters� from this location. This is an important
 * improvement to the string matching rules especially for text based documents
 * such as HTML and XML formats. The reasoning for this was that the rules for
 * matching SVG images defined in the original 'magic.mime' file hardly ever
 * worked, this is because of the fixed offset definitions within the magic rule
 * format. As XML documents generally have an XML declaration that can contain
 * various optional attributes the length of this header often cannot be
 * determined, therefore we cannot know that the DOCTYPE declaration for an SVG
 * xml file starts at �this� location, all we can say is that, if this is an SVG
 * xml file then it will have an SVG DOCTYPE somewhere near the beginning of the
 * file and probably within the first 1024 characters. So we test for the xml
 * declaration and then we test for the DOCTYPE within a specified number of
 * characters and if found then we match this rule. This extension can be used
 * to better identify ALL of the XML type mime mappings in the current
 * 'magic.mime' file. Remember though, as we stated earlier mime type matching
 * using any of the mechanisms supported is not an exact science and should
 * always be viewed as a 'best guess' and not as a 'definite match'.
 * </p>
 * <p>
 * An example of overriding the PNG and SVG rules can be found in our internal
 * 'magic.mime' file located in the test_files directory (this file is NOT used
 * when locating rules and is used for testing purposes only). This PNG rule
 * overrides the original PNG rule defined in the 'magic.mime' file we took from
 * the Internet, and the SVG rule overrides the SVG detection also defined in
 * the original 'magic.mime' file
 * </p>
 * <p>
 * <p>
 * <pre>
 * #PNG Image Format
 * 0		string		\211PNG\r\n\032\n		image/png
 *
 * #SVG Image Format
 * #	We know its an XML file so it should start with an XML declaration.
 * 0	string	\&lt;?xml\ version=	text/xml
 * #	As the XML declaration in an XML file can be short or extended we cannot know
 * #	exactly where the declaration ends i.e. how long it is,
 * #	also it could be terminated by a new line(s) or a space(s).
 * #	So the next line states that somewhere after the 15th character position we should find the DOCTYPE declaration.
 * #	This DOCTYPE declaration should be within 1024 characters from the 15th character
 * &gt;15	string&gt;1024&lt;	\&lt;!DOCTYPE\ svg\ PUBLIC\ &quot;-//W3C//DTD\ SVG 	image/svg+xml
 * </pre>
 * <p>
 * </p>
 * <p>
 * As you can see the extension is defined using the syntax string>bufsize<. It
 * can only be used on a string type and basically means match this within
 * bufsize character from the position defined at the beginning of the line.
 * This rule is much more verbose than required as we really only need to check
 * for the presence of SVG. As we said earlier, this is a test case file and not
 * used by the utility under normal circumstances.
 * <p>
 * The test mime-types.properties and magic.mime files we use can be located in
 * the test_files directory of this distribution.
 * </p>
 * <p>
 * We use the <code>application/directory</code> mime type to identify
 * directories. Even though this is not an official mime type it seems to be
 * well accepted on the net as an unofficial mime type so we thought it was OK
 * for us to use as well.
 * </p>
 * <p>
 * This class is auto loaded by MimeUtil as it has an entry in the file called
 * MimeDetectors. MimeUtil reads this file at startup and calls Class.forName()
 * on each entry found. This mean the MimeDetector must have a no arg
 * constructor.
 * </p>
 *
 * @author Steven McArdle.
 */
public class MagicMimeMimeDetector extends MimeDetector {

    private static Logger log = LoggerFactory.getLogger(MagicMimeMimeDetector.class);

    // Having the defaultLocations as protected allows you to subclass this class
    // and add different paths or remove them all so that the internal file is always used
    protected static String[] defaultLocations = {"/usr/share/mimelnk/magic",
            "/usr/share/file/magic.mime", "/etc/magic.mime"};
    private static List magicMimeFileLocations = Arrays
            .asList(defaultLocations);

    private static ArrayList mMagicMimeEntries = new ArrayList();

    public MagicMimeMimeDetector() {
        MagicMimeMimeDetector.initMagicRules();
    }

    public String getDescription() {
        return "Get the mime types of files or streams using the Unix file(5) magic.mime files";
    }

    /**
     * Get the mime types that may be contained in the data array.
     *
     * @param data The byte array that contains data we want to detect mime types from.
     * @return the mime types.
     * @throws MimeException if for instance we try to match beyond the end of the data.
     */
    public Collection getMimeTypesByteArray(final byte[] data)
            throws UnsupportedOperationException {
        Collection mimeTypes = new LinkedHashSet();
        int len = mMagicMimeEntries.size();
        try {
            for (int i = 0; i < len; i++) {
                MagicMimeEntry me = (MagicMimeEntry) mMagicMimeEntries.get(i);
                MagicMimeEntry matchingMagicMimeEntry = me.getMatch(data);
                if (matchingMagicMimeEntry != null) {
                    mimeTypes.add(matchingMagicMimeEntry.getMimeType());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return mimeTypes;
    }


    /**
     * Get the mime types of the data in the specified {@link InputStream}.
     * Therefore, the <code>InputStream</code> must support mark and reset (see
     * {@link InputStream#markSupported()}). If it does not support mark and
     * reset, an {@link MimeException} is thrown.
     *
     * @param in the stream from which to read the data.
     * @return the mime types.
     * @throws MimeException if the specified <code>InputStream</code> does not support
     *                       mark and reset (see {@link InputStream#markSupported()}).
     */
    public Collection getMimeTypesInputStream(final InputStream in)
            throws UnsupportedOperationException {
        Collection mimeTypes = new LinkedHashSet();
        int len = mMagicMimeEntries.size();
        try {
            for (int i = 0; i < len; i++) {
                MagicMimeEntry me = (MagicMimeEntry) mMagicMimeEntries.get(i);
                MagicMimeEntry matchingMagicMimeEntry = me.getMatch(in);
                if (matchingMagicMimeEntry != null) {
                    mimeTypes.add(matchingMagicMimeEntry.getMimeType());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return mimeTypes;
    }

    /**
     * Defer this call to the File method
     */
    public Collection getMimeTypesFileName(final String fileName) throws UnsupportedOperationException {
        return getMimeTypesFile(new File(fileName));
    }


    /**
     * Defer this call to the InputStream method
     */
    public Collection getMimeTypesURL(final URL url) throws UnsupportedOperationException {
        InputStream in = null;
        try {
            return getMimeTypesInputStream(in = new BufferedInputStream(MimeUtil.getInputStreamForURL(url)));
        } catch (Exception e) {
            throw new MimeException(e);
        } finally {
            closeStream(in);
        }
    }

    /**
     * Defer this call to the InputStream method
     */
    public Collection getMimeTypesFile(final File file) throws UnsupportedOperationException {
        InputStream in = null;
        try {
            return getMimeTypesInputStream(in = new BufferedInputStream(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new MimeException(e);
        } finally {
            closeStream(in);
        }
    }

    /*
     * This loads the magic.mime file rules into the internal parse tree in the
     * following order 1. From any magic.mime that can be located on the
     * classpath 2. From any magic.mime file that can be located using the
     * environment variable MAGIC 3. From any magic.mime located in the users
     * home directory ~/.magic.mime file if the MAGIC environment variable is
     * not set 4. From the locations defined in the magicMimeFileLocations and
     * the order defined 5. From the internally defined magic.mime file ONLY if
     * we are unable to locate any of the files in steps 2 - 5 above Thanks go
     * to Simon Pepping for his bug report
     */
    private static void initMagicRules() {
        InputStream in = null;

        // Try to locate a magic.mime file locate by system property magic-mime
        try {
            String fname = System.getProperty("magic-mime");
            if (fname != null && fname.length() != 0) {
                in = new FileInputStream(fname);
                if (in != null) {
                    parse("-Dmagic-mime=" + fname, new InputStreamReader(in));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse custom magic mime file defined by system property -Dmagic-mime ["
                    + System.getProperty("magic-mime")
                    + "]. File will be ignored.", e);
        } finally {
            in = closeStream(in);
        }

        // Try to locate a magic.mime file(s) on the classpath

        // Get an enumeration of all files on the classpath with this name. They could be in jar files as well
        try {
            Enumeration en = MimeUtil.class.getClassLoader().getResources("magic.mime");
            while (en.hasMoreElements()) {
                URL url = (URL) en.nextElement();
                in = url.openStream();
                if (in != null) {
                    try {
                        parse("classpath:[" + url + "]", new InputStreamReader(in));
                    } catch (Exception ex) {
                        log.error("Failed to parse magic.mime rule file [" + url + "] on the classpath. File will be ignored.",
                                ex);
                    }
                }

            }
        } catch (Exception e) {
            log.error("Problem while processing magic.mime files from classpath. Files will be ignored.", e);
        } finally {
            in = closeStream(in);
        }

        // Now lets see if we have one in the users home directory. This is
        // named .magic.mime as opposed to magic.mime
        try {
            File f = new File(System.getProperty("user.home") + File.separator
                    + ".magic.mime");
            if (f.exists()) {
                in = new FileInputStream(f);
                if (in != null) {
                    try {
                        parse(f.getAbsolutePath(), new InputStreamReader(in));
                    } catch (Exception ex) {
                        log.error("Failed to parse .magic.mime file from the users home directory. File will be ignored.", ex);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Problem while processing .magic.mime file from the users home directory. File will be ignored.", e);
        } finally {
            in = closeStream(in);
        }

        // Now lets see if we have an environment variable named MAGIC set. This
        // would normally point to a magic or magic.mgc file.
        // As we don't use these file types we will look to see if there is also
        // a magic.mime file at this location for us to use.
        try {
            String name = System.getProperty("MAGIC");
            if (name != null && name.length() != 0) {
                // Strip the .mgc from the end if it's there and add the .mime
                // extension
                if (name.indexOf('.') < 0) {
                    name = name + ".mime";
                } else {
                    // remove the mgc extension
                    name = name.substring(0, name.indexOf('.') - 1) + "mime";
                }
                File f = new File(name);
                if (f.exists()) {
                    in = new FileInputStream(f);
                    if (in != null) {
                        try {
                            parse(f.getAbsolutePath(),
                                    new InputStreamReader(in));
                        } catch (Exception ex) {
                            log.error("Failed to parse magic.mime file from directory located by environment variable MAGIC. File will be ignored.", ex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Problem while processing magic.mime file from directory located by environment variable MAGIC. File will be ignored.", e);
        } finally {
            in = closeStream(in);
        }

        // Parse the UNIX magic(5) magic.mime files. Since there can be
        // multiple, we have to load all of them.
        // We save, how many entries we have now, in order to fall back to our
        // default magic.mime that we ship,
        // if no entries were read from the OS.

        int mMagicMimeEntriesSizeBeforeReadingOS = mMagicMimeEntries.size();
        Iterator it = magicMimeFileLocations.iterator();
        while (it.hasNext()) {
            parseMagicMimeFileLocation((String) it.next());
        }

        if (mMagicMimeEntriesSizeBeforeReadingOS == mMagicMimeEntries.size()) {
            // Use the magic.mime that we ship
            try {
                String resource = "eu/medsea/mimeutil/magic.mime";
                in = MimeUtil.class.getClassLoader().getResourceAsStream(
                        resource);
                if (in != null) {
                    try {
                        parse("resource:" + resource, new InputStreamReader(in));
                    } catch (Exception ex) {
                        log.error("Failed to parse internal magic.mime file.", ex);
                    }
                }
            } catch (Exception e) {
                log.error("Problem while processing internal magic.mime file.", e);
            } finally {
                in = closeStream(in);
            }
        }
    }

    private static void parseMagicMimeFileLocation(final String location) {
        InputStream is = null;

        List magicMimeFiles = getMagicFilesFromMagicMimeFileLocation(location);

        for (Iterator itFile = magicMimeFiles.iterator(); itFile.hasNext(); ) {
            File f = (File) itFile.next();
            try {
                if (f.exists()) {
                    is = new FileInputStream(f);
                    try {
                        parse(f.getAbsolutePath(), new InputStreamReader(is));
                    } catch (Exception e) {
                        log.error("Failed to parse " + f.getName() + ". File will be ignored.");
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                is = closeStream(is);
            }
        }
    }

    private static List getMagicFilesFromMagicMimeFileLocation(
            final String magicMimeFileLocation) {
        List magicMimeFiles = new LinkedList();
        if (magicMimeFileLocation.indexOf('*') < 0) {
            magicMimeFiles.add(new File(magicMimeFileLocation));
        } else {
            int lastSlashPos = magicMimeFileLocation.lastIndexOf('/');
            File dir;
            String fileNameSimplePattern;
            if (lastSlashPos < 0) {
                dir = new File("someProbablyNotExistingFile").getAbsoluteFile()
                        .getParentFile();
                fileNameSimplePattern = magicMimeFileLocation;
            } else {
                String dirName = magicMimeFileLocation.substring(0,
                        lastSlashPos);
                if (dirName.indexOf('*') >= 0)
                    throw new UnsupportedOperationException(
                            "The wildcard '*' is not allowed in directory part of the location! Do you want to implement expressions like /path/**/*.mime for recursive search? Please do!");

                dir = new File(dirName);
                fileNameSimplePattern = magicMimeFileLocation
                        .substring(lastSlashPos + 1);
            }

            if (!dir.isDirectory())
                return Collections.EMPTY_LIST;

            String s = fileNameSimplePattern.replaceAll("\\.", "\\\\.");
            s = s.replaceAll("\\*", ".*");
            Pattern fileNamePattern = Pattern.compile(s);

            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];

                if (fileNamePattern.matcher(file.getName()).matches())
                    magicMimeFiles.add(file);
            }
        }
        return magicMimeFiles;
    }

    // Parse the magic.mime file
    private static void parse(final String magicFile, final Reader r)
            throws IOException {
        long start = System.currentTimeMillis();

        BufferedReader br = new BufferedReader(r);
        String line;
        ArrayList sequence = new ArrayList();

        long lineNumber = 0;
        line = br.readLine();
        if (line != null)
            ++lineNumber;
        while (true) {
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() == 0 || line.charAt(0) == '#') {
                line = br.readLine();
                if (line != null)
                    ++lineNumber;
                continue;
            }
            sequence.add(line);

            // read the following lines until a line does not begin with '>' or
            // EOF
            while (true) {
                line = br.readLine();
                if (line != null)
                    ++lineNumber;
                if (line == null) {
                    addEntry(magicFile, lineNumber, sequence);
                    sequence.clear();
                    break;
                }
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                if (line.charAt(0) != '>') {
                    addEntry(magicFile, lineNumber, sequence);
                    sequence.clear();
                    break;
                }
                sequence.add(line);
            }

        }
        if (!sequence.isEmpty()) {
            addEntry(magicFile, lineNumber, sequence);
        }

        if (log.isDebugEnabled())
            log.debug("Parsing \"" + magicFile + "\" took "
                    + (System.currentTimeMillis() - start) + " msec.");
    }

    private static void addEntry(final String magicFile, final long lineNumber,
                                 final ArrayList aStringArray) {
        try {
            MagicMimeEntry magicEntry = new MagicMimeEntry(aStringArray);
            mMagicMimeEntries.add(magicEntry);
            // Add this to the list of known mime types as well
            if (magicEntry.getMimeType() != null) {
                MimeUtil.addKnownMimeType(magicEntry.getMimeType());
            }
        } catch (InvalidMagicMimeEntryException e) {
            // Continue on but lets print an exception so people can see there
            // is a problem
            log.warn(e.getClass().getName() + ": " + e.getMessage()
                    + ": file \"" + magicFile + "\": before or at line "
                    + lineNumber, e);
        }
    }
}
