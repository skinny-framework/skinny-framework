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

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * <p>
 * The Opendesktop shared mime database contains glob rules and magic number
 * lookup information to enable applications to detect the mime types of files.
 * </p>
 * <p>
 * This class uses the mime.cache file which is one of the files created by the
 * update-mime-database application. This file is a memory mapped file that
 * enables the database to be updated and copied without interrupting
 * applications.
 * </p>
 * <p>
 * This implementation follows the memory mapped spec so it is not required to
 * restart an application using this mime detector should the underlying
 * mime.cache database change.
 * </p>
 * <p>
 * For a complete description of the information contained in this file please
 * see: http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-
 * spec-latest.html
 * </p>
 * <p>
 * This class also follows, where possible, the RECOMENDED order of detection as
 * detailed in this spec. Thanks go to Mathias Clasen at Red Hat for pointing me
 * to the original xdgmime implementation
 * http://svn.gnome.org/viewvc/glib/trunk/
 * gio/xdgmime/xdgmimecache.c?revision=7784&view=markup
 * </p>
 *
 * @author Steven McArdle
 */
public class OpendesktopMimeDetector extends MimeDetector {

    private static Logger log = LoggerFactory.getLogger(OpendesktopMimeDetector.class);

    private static String mimeCacheFile = "/usr/share/mime/mime.cache";
    private static String internalMimeCacheFile = "src/main/resources/mime.cache";

    private ByteBuffer content;

    private Timer timer;

    public OpendesktopMimeDetector(final String mimeCacheFile) {
        init(mimeCacheFile);
    }

    public OpendesktopMimeDetector() {
        init(mimeCacheFile);
    }

    private void init(final String mimeCacheFile) {
        String cacheFile = mimeCacheFile;
        if (!new File(cacheFile).exists()) {
            cacheFile = internalMimeCacheFile;

        }
        // Map the mime.cache file as a memory mapped file
        FileChannel rCh = null;
        try {
            RandomAccessFile raf = null;
            raf = new RandomAccessFile(cacheFile, "r");
            rCh = (raf).getChannel();
            content = rCh.map(FileChannel.MapMode.READ_ONLY, 0, rCh.size());

            // Read all of the MIME type from the Alias list
            initMimeTypes();

            if (log.isDebugEnabled()) {
                log.debug("Registering a FileWatcher for [" + cacheFile + "]");
            }
            TimerTask task = new FileWatcher(new File(cacheFile)) {
                protected void onChange(File file) {
                    initMimeTypes();
                }
            };

            timer = new Timer();
            // repeat the check every 10 seconds
            timer.schedule(task, new Date(), 10000);

        } catch (Exception e) {
            throw new MimeException(e);
        } finally {
            if (rCh != null) {
                try {
                    rCh.close();
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    public void delete() {
        // Cancel this timer
        timer.cancel();
    }

    public String getDescription() {
        return "Resolve mime types for files and streams using the Opendesktop shared mime.cache file. Version ["
                + getMajorVersion() + "." + getMinorVersion() + "].";
    }

    /**
     * This method resolves mime types closely in accordance with the RECOMENDED
     * order of detection detailed in the Opendesktop shared mime database
     * specification
     * http://standards.freedesktop.org/shared-mime-info-spec/shared
     * -mime-info-spec-latest.html See the Recommended checking order.
     */
    public Collection getMimeTypesFileName(String fileName) {
        Collection mimeTypes = new ArrayList();
        // Lookup the globbing methods first
        lookupMimeTypesForGlobFileName(fileName, mimeTypes);

        if (!mimeTypes.isEmpty()) {
            mimeTypes = normalizeWeightedMimeList((List) mimeTypes);
        }

        return mimeTypes;
    }

    /**
     * This method resolves mime types closely in accordance with the RECOMENDED
     * order of detection detailed in the Opendesktop shared mime database
     * specification
     * http://standards.freedesktop.org/shared-mime-info-spec/shared
     * -mime-info-spec-latest.html See the Recommended checking order.
     */
    public Collection getMimeTypesURL(URL url) {

        Collection mimeTypes = getMimeTypesFileName(url.getPath());
        return _getMimeTypes(mimeTypes, getInputStream(url));
    }

    /**
     * This method resolves mime types closely in accordance with the RECOMENDED
     * order of detection detailed in the Opendesktop shared mime database
     * specification
     * http://standards.freedesktop.org/shared-mime-info-spec/shared
     * -mime-info-spec-latest.html See the Recommended checking order.
     */
    public Collection getMimeTypesFile(File file)
            throws UnsupportedOperationException {

        Collection mimeTypes = getMimeTypesFileName(file.getName());
        if (!file.exists()) {
            return mimeTypes;
        }
        return _getMimeTypes(mimeTypes, getInputStream(file));
    }

    /**
     * This method is unable to perform glob matching as no name is available.
     * This means that it does not follow the recommended order of detection
     * defined in the shared mime database spec
     * http://standards.freedesktop.org/
     * shared-mime-info-spec/shared-mime-info-spec-latest.html
     */
    public Collection getMimeTypesInputStream(InputStream in)
            throws UnsupportedOperationException {
        return lookupMimeTypesForMagicData(in);
    }

    /**
     * This method is unable to perform glob matching as no name is available.
     * This means that it does not follow the recommended order of detection
     * defined in the shared mime database spec
     * http://standards.freedesktop.org/
     * shared-mime-info-spec/shared-mime-info-spec-latest.html
     */
    public Collection getMimeTypesByteArray(byte[] data)
            throws UnsupportedOperationException {
        return lookupMagicData(data);
    }

    public String dump() {
        return "{MAJOR_VERSION=" + getMajorVersion() + " MINOR_VERSION="
                + getMinorVersion() + " ALIAS_LIST_OFFSET="
                + getAliasListOffset() + " PARENT_LIST_OFFSET="
                + getParentListOffset() + " LITERAL_LIST_OFFSET="
                + getLiteralListOffset() + " REVERSE_SUFFIX_TREE_OFFSET="
                + getReverseSuffixTreeOffset() + " GLOB_LIST_OFFSET="
                + getGlobListOffset() + " MAGIC_LIST_OFFSET="
                + getMagicListOffset() + " NAMESPACE_LIST_OFFSET="
                + getNameSpaceListOffset() + " ICONS_LIST_OFFSET="
                + getIconListOffset() + " GENERIC_ICONS_LIST_OFFSET="
                + getGenericIconListOffset() + "}";
    }

    private Collection lookupMimeTypesForMagicData(InputStream in) {

        int offset = 0;
        int len = getMaxExtents();
        byte[] data = new byte[len];
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
        return lookupMagicData(data);
    }

    private Collection lookupMagicData(byte[] data) {

        Collection mimeTypes = new ArrayList();

        int listOffset = getMagicListOffset();
        int numEntries = content.getInt(listOffset);
        int offset = content.getInt(listOffset + 8);

        for (int i = 0; i < numEntries; i++) {
            String mimeType = compareToMagicData(offset + (16 * i), data);
            if (mimeType != null) {
                mimeTypes.add(mimeType);
            } else {
                String nonMatch = getMimeType(content.getInt(offset + (16 * i)
                        + 4));
                mimeTypes.remove(nonMatch);
            }
        }

        return mimeTypes;
    }

    private String compareToMagicData(int offset, byte[] data) {
        // int priority = content.getInt(offset);
        int mimeOffset = content.getInt(offset + 4);
        int numMatches = content.getInt(offset + 8);
        int matchletOffset = content.getInt(offset + 12);

        for (int i = 0; i < numMatches; i++) {
            if (matchletMagicCompare(matchletOffset + (i * 32), data)) {
                return getMimeType(mimeOffset);
            }
        }
        return null;
    }

    private boolean matchletMagicCompare(int offset, byte[] data) {
        int rangeStart = content.getInt(offset);
        int rangeLength = content.getInt(offset + 4);
        int dataLength = content.getInt(offset + 12);
        int dataOffset = content.getInt(offset + 16);
        int maskOffset = content.getInt(offset + 20);

        for (int i = rangeStart; i <= rangeStart + rangeLength; i++) {
            boolean validMatch = true;
            if (i + dataLength > data.length) {
                return false;
            }
            if (maskOffset != 0) {
                for (int j = 0; j < dataLength; j++) {
                    if ((content.get(dataOffset + j) & content.get(maskOffset
                            + j)) != (data[j + i] & content.get(maskOffset + j))) {
                        validMatch = false;
                        break;
                    }
                }
            } else {
                for (int j = 0; j < dataLength; j++) {
                    if (content.get(dataOffset + j) != data[j + i]) {
                        validMatch = false;
                        break;
                    }
                }
            }

            if (validMatch) {
                return true;
            }
        }
        return false;
    }

    private void lookupGlobLiteral(String fileName, Collection mimeTypes) {
        int listOffset = getLiteralListOffset();
        int numEntries = content.getInt(listOffset);

        int min = 0;
        int max = numEntries - 1;
        while (max >= min) {
            int mid = (min + max) / 2;
            String literal = getString(content.getInt((listOffset + 4)
                    + (12 * mid)));
            int cmp = literal.compareTo(fileName);
            if (cmp < 0) {
                min = mid + 1;
            } else if (cmp > 0) {
                max = mid - 1;
            } else {
                String mimeType = getMimeType(content.getInt((listOffset + 4)
                        + (12 * mid) + 4));
                int weight = content.getInt((listOffset + 4) + (12 * mid) + 8);
                mimeTypes.add(new WeightedMimeType(mimeType, literal, weight));
                return;
            }
        }
    }

    private void lookupGlobFileNameMatch(String fileName, Collection mimeTypes) {
        int listOffset = getGlobListOffset();
        int numEntries = content.getInt(listOffset);

        for (int i = 0; i < numEntries; i++) {
            int offset = content.getInt((listOffset + 4) + (12 * i));
            int mimeTypeOffset = content
                    .getInt((listOffset + 4) + (12 * i) + 4);
            int weight = content.getInt((listOffset + 4) + (12 * i) + 8);

            String pattern = getString(offset, true);
            String mimeType = getMimeType(mimeTypeOffset);

            if (fileName.matches(pattern)) {
                mimeTypes.add(new WeightedMimeType(mimeType, pattern, weight));
            }
        }
    }

    private Collection normalizeWeightedMimeList(Collection weightedMimeTypes) {
        Collection mimeTypes = new LinkedHashSet();

        // Sort the weightedMimeTypes
        Collections.sort((List) weightedMimeTypes, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                return ((WeightedMimeType) obj1).weight
                        - ((WeightedMimeType) obj2).weight;
            }
        });

        // Keep only globs with the biggest weight. They are in weight order at
        // this point
        int weight = 0;
        int patternLen = 0;
        for (Iterator it = weightedMimeTypes.iterator(); it.hasNext(); ) {
            WeightedMimeType mw = (WeightedMimeType) it.next();
            if (weight < mw.weight) {
                weight = mw.weight;
            }
            if (weight >= mw.weight) {
                if (mw.pattern.length() > patternLen) {
                    patternLen = mw.pattern.length();
                }
                mimeTypes.add(mw);
            }
        }

        // Now keep only the longest patterns
        for (Iterator it = weightedMimeTypes.iterator(); it.hasNext(); ) {
            WeightedMimeType mw = (WeightedMimeType) it.next();
            if (mw.pattern.length() < patternLen) {
                mimeTypes.remove(mw);
            }
        }

        // Could possibly have multiple mimeTypes here with the same weight and
        // pattern length. Can even have multiple entries for the same type so
        // lets remove
        // any duplicates by copying entries to a HashSet that can only have a
        // single instance
        // of each type
        Collection _mimeTypes = new HashSet();
        for (Iterator it = mimeTypes.iterator(); it.hasNext(); ) {
            _mimeTypes.add(((WeightedMimeType) it.next()).toString());
        }
        return _mimeTypes;
    }

    private void lookupMimeTypesForGlobFileName(String fileName,
                                                Collection mimeTypes) {
        if (fileName == null) {
            return;
        }

        lookupGlobLiteral(fileName, mimeTypes);
        if (!mimeTypes.isEmpty()) {
            return;
        }

        int len = fileName.length();
        lookupGlobSuffix(fileName, false, len, mimeTypes);

        if (mimeTypes.isEmpty()) {
            lookupGlobSuffix(fileName, true, len, mimeTypes);
        }
        if (mimeTypes.isEmpty()) {
            lookupGlobFileNameMatch(fileName, mimeTypes);
        }
    }

    private void lookupGlobSuffix(String fileName, boolean ignoreCase, int len,
                                  Collection mimeTypes) {

        int listOffset = getReverseSuffixTreeOffset();
        int numEntries = content.getInt(listOffset);
        int offset = content.getInt(listOffset + 4);

        lookupGlobNodeSuffix(fileName, numEntries, offset, ignoreCase, len,
                mimeTypes, new StringBuffer());
    }

    private void lookupGlobNodeSuffix(String fileName, int numEntries,
                                      int offset, boolean ignoreCase, int len, Collection mimeTypes,
                                      StringBuffer pattern) {
        char character = ignoreCase ? fileName.toLowerCase().charAt(len - 1)
                : fileName.charAt(len - 1);

        if (character == 0) {
            return;
        }

        int min = 0;
        int max = numEntries - 1;
        while (max >= min && len >= 0) {
            int mid = (min + max) / 2;

            char matchChar = (char) content.getInt(offset + (12 * mid));
            if (matchChar < character) {
                min = mid + 1;
            } else if (matchChar > character) {
                max = mid - 1;
            } else {
                len--;
                int numChildren = content.getInt(offset + (12 * mid) + 4);
                int childOffset = content.getInt(offset + (12 * mid) + 8);
                if (len > 0) {
                    pattern.append(matchChar);
                    lookupGlobNodeSuffix(fileName, numChildren, childOffset,
                            ignoreCase, len, mimeTypes, pattern);
                }
                if (mimeTypes.isEmpty()) {
                    for (int i = 0; i < numChildren; i++) {
                        matchChar = (char) content.getInt(childOffset
                                + (12 * i));
                        if (matchChar != 0) {
                            break;
                        }

                        int mimeOffset = content.getInt(childOffset + (12 * i)
                                + 4);
                        int weight = content.getInt(childOffset + (12 * i) + 8);
                        mimeTypes.add(new WeightedMimeType(
                                getMimeType(mimeOffset), pattern.toString(),
                                weight));
                    }
                }
                return;
            }
        }
    }

    class WeightedMimeType extends MimeType {

        private static final long serialVersionUID = 1L;
        String pattern;
        int weight;

        WeightedMimeType(String mimeType, String pattern, int weight) {
            super(mimeType);
            this.pattern = pattern;
            this.weight = weight;
        }
    }

    private int getMaxExtents() {
        return content.getInt(getMagicListOffset() + 4);
    }

    private String aliasLookup(String alias) {
        int aliasListOffset = getAliasListOffset();
        int min = 0;
        int max = content.getInt(aliasListOffset) - 1;

        while (max >= min) {
            int mid = (min + max) / 2;
            // content.position((aliasListOffset + 4) + (mid * 8));

            int aliasOffset = content.getInt((aliasListOffset + 4) + (mid * 8));
            int mimeOffset = content.getInt((aliasListOffset + 4) + (mid * 8)
                    + 4);

            int cmp = getMimeType(aliasOffset).compareTo(alias);
            if (cmp < 0) {
                min = mid + 1;
            } else if (cmp > 0) {
                max = mid - 1;
            } else {
                return getMimeType(mimeOffset);
            }
        }
        return null;
    }

    private String unaliasMimeType(String mimeType) {
        String lookup = aliasLookup(mimeType);
        return lookup == null ? mimeType : lookup;
    }

    private boolean isMimeTypeSubclass(String mimeType, String subClass) {
        String umimeType = unaliasMimeType(mimeType);
        String usubClass = unaliasMimeType(subClass);
        MimeType _mimeType = new MimeType(umimeType);
        MimeType _subClass = new MimeType(usubClass);

        if (umimeType.compareTo(usubClass) == 0) {
            return true;
        }

        if (isSuperType(usubClass)
                && (_mimeType.getMediaType().equals(_subClass.getMediaType()))) {
            return true;
        }

        // Handle special cases text/plain and application/octet-stream
        if (usubClass.equals("text/plain")
                && _mimeType.getMediaType().equals("text")) {
            return true;
        }

        if (usubClass.equals("application/octet-stream")) {
            return true;
        }
        int parentListOffset = getParentListOffset();
        int numParents = content.getInt(parentListOffset);
        int min = 0;
        int max = numParents - 1;
        while (max >= min) {
            int med = (min + max) / 2;
            int offset = content.getInt((parentListOffset + 4) + (8 * med));
            String parentMime = getMimeType(offset);
            int cmp = parentMime.compareTo(umimeType);
            if (cmp < 0) {
                min = med + 1;
            } else if (cmp > 0) {
                max = med - 1;
            } else {
                offset = content.getInt((parentListOffset + 4) + (8 * med) + 4);
                int _numParents = content.getInt(offset);
                for (int i = 0; i < _numParents; i++) {
                    int parentOffset = content.getInt((offset + 4) + (4 * i));
                    if (isMimeTypeSubclass(getMimeType(parentOffset), usubClass)) {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    private boolean isSuperType(String mimeType) {
        String type = mimeType.substring(mimeType.length() - 2);
        if (type.equals("/*")) {
            return true;
        }
        return false;
    }

    private int getGenericIconListOffset() {
        return content.getInt(36);
    }

    private int getIconListOffset() {
        return content.getInt(32);
    }

    private int getNameSpaceListOffset() {
        return content.getInt(28);
    }

    private int getMagicListOffset() {
        return content.getInt(24);
    }

    private int getGlobListOffset() {
        return content.getInt(20);
    }

    private int getReverseSuffixTreeOffset() {
        return content.getInt(16);
    }

    private int getLiteralListOffset() {
        return content.getInt(12);
    }

    private int getParentListOffset() {
        return content.getInt(8);
    }

    private int getAliasListOffset() {
        return content.getInt(4);
    }

    private short getMinorVersion() {
        return content.getShort(2);
    }

    private short getMajorVersion() {
        return content.getShort(0);
    }

    private String getMimeType(int offset) {
        return getString(offset);
    }

    private String getString(int offset) {
        return getString(offset, false);
    }

    private String getString(int offset, boolean regularExpression) {
        int position = content.position();
        content.position(offset);
        StringBuffer buf = new StringBuffer();
        char c = 0;
        while ((c = (char) content.get()) != 0) {
            if (regularExpression) {
                switch (c) {
                    case '.':
                        buf.append("\\");
                        break;
                    case '*':
                    case '+':
                    case '?':
                        buf.append(".");
                }
            }
            buf.append(c);
        }
        // Reset position
        content.position(position + 4);

        if (regularExpression) {
            buf.insert(0, '^');
            buf.append('$');
        }
        return buf.toString();
    }

    private InputStream getInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (Exception e) {
            log.error("Error getting InputStream for file ["
                    + file.getAbsolutePath() + "]", e);
        }
        return null;
    }

    private InputStream getInputStream(URL url) {
        try {
            return MimeUtil.getInputStreamForURL(url);
        } catch (Exception e) {
            throw new MimeException("Error getting InputStream for URL ["
                    + url.getPath() + "]", e);
        }
    }

    private Collection _getMimeTypes(Collection mimeTypes, InputStream in) {

        try {
            if (mimeTypes.isEmpty() || mimeTypes.size() > 1) {
                Collection _mimeTypes = getMimeTypesInputStream(in = new BufferedInputStream(
                        in));

                if (!_mimeTypes.isEmpty()) {
                    if (!mimeTypes.isEmpty()) {
                        // more than one glob matched

                        // Check for same mime type
                        for (Iterator it = mimeTypes.iterator(); it.hasNext(); ) {
                            String mimeType = (String) it.next();
                            if (_mimeTypes.contains(mimeType)) {
                                // mimeTypes = new ArrayList();
                                mimeTypes.add(mimeType);
                                // return mimeTypes;
                            }
                            // Check for mime type subtype
                            for (Iterator _it = _mimeTypes.iterator(); _it
                                    .hasNext(); ) {
                                String _mimeType = (String) _it.next();
                                if (isMimeTypeSubclass(mimeType, _mimeType)) {
                                    // mimeTypes = new ArrayList();
                                    mimeTypes.add(mimeType);
                                    // return mimeTypes;
                                }
                            }
                        }
                    } else {
                        // No globs matched but we have magic matches
                        return _mimeTypes;
                    }
                }
            }
        } catch (Exception e) {
            throw new MimeException(e);
        } finally {
            closeStream(in);
        }
        return mimeTypes;

    }

    // The Alias list should contain just about all the mime types used by
    // this MimeDetector so we will be content with these entries
    private void initMimeTypes() {

        int listOffset = getAliasListOffset();
        int numAliases = content.getInt(listOffset);

        for (int i = 0; i < numAliases; i++) {
            MimeUtil.addKnownMimeType(getString(content.getInt((listOffset + 4)
                    + (i * 8)))); //
            MimeUtil.addKnownMimeType(getString(content.getInt((listOffset + 8)
                    + (i * 8))));
        }
    }

}