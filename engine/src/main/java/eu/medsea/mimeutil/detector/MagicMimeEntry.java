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

import eu.medsea.mimeutil.MimeType;
import eu.medsea.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * A single MagicMime entry from a magic.mime file. This entry can contain
 * sub-entries; so it recursively includes itself, if sub-entries are found.
 * Basically this class represents a node in a simple n-array tree
 *
 * @author Steven McArdle
 */
class MagicMimeEntry {

    public static final int STRING_TYPE = 1;
    public static final int BELONG_TYPE = 2;
    public static final int SHORT_TYPE = 3;
    public static final int LELONG_TYPE = 4;
    public static final int BESHORT_TYPE = 5;
    public static final int LESHORT_TYPE = 6;
    public static final int BYTE_TYPE = 7;
    public static final int UNKNOWN_TYPE = 20;

    private ArrayList subEntries = new ArrayList();
    private int checkBytesFrom;
    private int type;
    private String typeStr;
    private String content;
    private long contentNumber;
    private MimeType mimeType;
    private String mimeEnc;
    private MagicMimeEntry parent;

    private MagicMimeEntryOperation operation = MagicMimeEntryOperation.EQUALS;

    boolean isBetween; // used for range checking strings.

    public MagicMimeEntry(ArrayList entries)
            throws InvalidMagicMimeEntryException {
        this(0, null, entries);
    }

    private MagicMimeEntry(int level, MagicMimeEntry parent, ArrayList entries)
            throws InvalidMagicMimeEntryException {
        if (entries == null || entries.size() == 0) {
            return;
        }

        this.parent = parent;
        if (parent != null) {
            parent.subEntries.add(this);
        }

        try {
            addEntry((String) entries.get(0));
        } catch (Exception e) {
            throw new InvalidMagicMimeEntryException(entries, e);
        }
        entries.remove(0);

        while (entries.size() > 0) {
            int thisLevel = howManyGreaterThans((String) entries.get(0));
            if (thisLevel > level) {
                new MagicMimeEntry(thisLevel, this, entries);
            } else {
                break;
            }
        }
    }

    public String toString() {
        return "MimeMagicType: " + checkBytesFrom + ", " + type + ", "
                + content + ", " + mimeType + ", " + mimeEnc;
    }

    public void traverseAndPrint(String tabs) {
        System.out.println(tabs + toString());
        int len = subEntries.size();
        for (int i = 0; i < len; i++) {
            MagicMimeEntry me = (MagicMimeEntry) subEntries.get(i);
            me.traverseAndPrint(tabs + "\t");
        }
    }

    private int howManyGreaterThans(String aLine) {
        if (aLine == null) {
            return -1;
        }
        int i = 0;
        int len = aLine.length();
        while (i < len) {
            if (aLine.charAt(i) == '>') {
                i++;
            } else {
                break;
            }
        }
        return i;
    }

    // There are problems with the magic.mime file. It seems that some of the
    // fields
    // are space delimited and not tab delimited as defined in the spec.
    // We will attempt to handle the case for space delimiters here so that we
    // can parse
    // as much of the file as possible. Currently about 70 entries are incorrect
    void addEntry(String aLine) throws InvalidMagicMimeEntryException {
        // Try to handle all cases where spaces are used instead of tabs to
        // delineated fields. We must still allow escaped spaces in content so
        // we eliminate
        // these first with the <##> tag and then continue to replace all white
        // spaces remaining with tabs and then replace multiple tabs with a
        // single one. Finally
        // we put back any valid escaped spaces
        String trimmed = aLine.replaceAll("[\\\\][ ]", "<##>").replaceAll(
                "^>*", "").replaceAll("\\s+", "\t")
                .replaceAll("[\t]{2,}", "\t").replaceAll("<##>", "\\\\ ");
        String[] tokens = trimmed.split("\t");

        // Now strip the empty entries
        Vector v = new Vector();
        for (int i = 0; i < tokens.length; i++) {
            if (!"".equals(tokens[i])) {
                v.add(tokens[i]);
            }
        }
        tokens = new String[v.size()];
        tokens = (String[]) v.toArray(tokens);

        if (tokens.length > 0) {
            String tok = tokens[0].trim();
            try {
                if (tok.startsWith("0x")) {
                    checkBytesFrom = Integer.parseInt(tok.substring(2), 16);
                } else {
                    checkBytesFrom = Integer.parseInt(tok);
                }
            } catch (NumberFormatException e) {
                throw new InvalidMagicMimeEntryException(Collections
                        .singletonList(this), e);
            }
        }
        if (tokens.length > 1) {
            typeStr = tokens[1].trim();
            type = getType(typeStr);
        }
        if (tokens.length > 2) {
            // We don't trim the content
            content = ltrim(tokens[2]);

            // The content might begin with an operation, hence get it from the
            // content.
            switch (type) {
                case BYTE_TYPE:
                case SHORT_TYPE:
                case BESHORT_TYPE:
                case LESHORT_TYPE:
                case LELONG_TYPE:
                case BELONG_TYPE:
                    operation = MagicMimeEntryOperation
                            .getOperationForNumberField(content);
                    break;
                default:
                    operation = MagicMimeEntryOperation
                            .getOperationForStringField(content);
            }

            if (content.length() > 0
                    && content.charAt(0) == operation.getOperationID())
                content = content.substring(1);

            content = stringWithEscapeSubstitutions(content);
        } else
            content = ""; // prevent NullPointerException happening later in
        // readBuffer(...)

        if (tokens.length > 3) {
            mimeType = new MimeType(tokens[3].trim());
        }
        if (tokens.length > 4) {
            mimeEnc = tokens[4].trim();
        }

        initContentNumber();
    }

    /**
     * Numeric values may be preceded by a character indicating the operation to
     * be performed. It may be =, to specify that the value from the file must
     * equal the specified value, &lt;, to specify that the value from the file
     * must be less than the specified value, &gt;, to specify that the value
     * from the file must be greater than the specified value, &amp;, to specify
     * that the value from the file must have set all of the bits that are set
     * in the specified value, ^, to specify that the value from the file must
     * have clear any of the bits that are set in the specified value, or ~, the
     * value specified after is negated before tested. x, to specify that any
     * value will match. If the character is omitted, it is assumed to be =. For
     * all tests except string and regex, operation ! specifies that the line
     * matches if the test does not succeed.
     * <p>
     * Numeric values are specified in C form; e.g. 13 is decimal, 013 is octal,
     * and 0x13 is hexadecimal.
     */
    private void initContentNumber() {
        contentNumber = 0;
        if (content.length() == 0)
            return;

        // check to already get exception during parsing rather than during
        // checking (later). we keep these
        // values already in long form rather than executing Long.parseLong(...)
        // many times (during check).
        switch (type) {
            case BYTE_TYPE:
            case SHORT_TYPE:
            case BESHORT_TYPE:
            case LESHORT_TYPE:
            case LELONG_TYPE:
            case BELONG_TYPE: {
                if (content.startsWith("0x")) {
                    contentNumber = Long.parseLong(content.substring(2).trim(), 16); // Without
                    // the
                    // trim(),
                    // I
                    // got
                    // some
                    // NumberFormatExceptions
                    // here.
                    // Added
                    // trim()
                    // below,
                    // as
                    // well.
                    // Marco.
                } else if (content.startsWith("0")) {
                    contentNumber = Long.parseLong(content.trim(), 8);
                } else {
                    contentNumber = Long.parseLong(content.trim());
                }
            }
        }
    }

    private String ltrim(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ') {
                return s.substring(i);
            }
        }
        return s;
    }

    private int getType(String tok) {
        if (tok.startsWith("string")) {
            return STRING_TYPE;
        } else if (tok.startsWith("belong")) {
            return BELONG_TYPE;
        } else if (tok.equals("short")) {
            return SHORT_TYPE;
        } else if (tok.startsWith("lelong")) {
            return LELONG_TYPE;
        } else if (tok.startsWith("beshort")) {
            return BESHORT_TYPE;
        } else if (tok.startsWith("leshort")) {
            return LESHORT_TYPE;
        } else if (tok.equals("byte")) {
            return BYTE_TYPE;
        }

        return UNKNOWN_TYPE;
    }

    public int getCheckBytesFrom() {
        return checkBytesFrom;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    MagicMimeEntry getMatch(InputStream in) throws IOException {
        int bytesToRead = getInputStreamMarkLength();
        in.mark(bytesToRead);
        try {
            byte[] content = new byte[bytesToRead];

            // Since an InputStream might return only some data (not all
            // requested), we have to read in a loop until
            // either EOF is reached or the desired number of bytes have been
            // read. Marco.
            int offset = 0;
            int restBytesToRead = bytesToRead;
            while (restBytesToRead > 0) {
                int bytesRead = in.read(content, offset, restBytesToRead);
                if (bytesRead < 0)
                    break; // EOF

                offset += bytesRead;
                restBytesToRead -= bytesRead;
            }

            return getMatch(content);
        } finally {
            in.reset();
        }
    }

    MagicMimeEntry getMatch(byte[] content) throws IOException {
        ByteBuffer buf = readBuffer(content);
        if (buf == null)
            return null;

        buf.position(0);
        boolean matches = match(buf);
        if (matches) {
            int subLen = subEntries.size();
            MimeType mimeType = getMimeType();
            if (subLen > 0) {
                for (int k = 0; k < subLen; k++) {
                    MagicMimeEntry me = (MagicMimeEntry) subEntries.get(k);
                    MagicMimeEntry matchingEntry = me.getMatch(content);
                    if (matchingEntry != null) {
                        return matchingEntry;
                    }
                }
                if (mimeType != null) {
                    return this;
                }
            } else {
                if (mimeType != null)
                    return this;
            }
        }

        return null;
    }

    MagicMimeEntry getMatch(RandomAccessFile raf) throws IOException {
        ByteBuffer buf = readBuffer(raf);
        if (buf == null) {
            return null;
        }
        boolean matches = match(buf);
        if (matches) {
            MimeType mimeType = getMimeType();
            if (subEntries.size() > 0) {
                for (int i = 0; i < subEntries.size(); i++) {
                    MagicMimeEntry me = (MagicMimeEntry) subEntries.get(i);
                    MagicMimeEntry matchingEntry = me.getMatch(raf);
                    if (matchingEntry != null) {
                        return matchingEntry;
                    }
                }
                if (mimeType != null) {
                    return this;
                }
            } else {
                if (mimeType != null)
                    return this;
            }
        }

        return null;
    }

    /*
     * private methods for reading to local buffer
     */
    private ByteBuffer readBuffer(byte[] content) throws IOException {
        int startPos = getCheckBytesFrom();
        if (content == null || startPos > content.length) {
            return null;
        }

        ByteBuffer buf = null;
        try {
            switch (getType()) {
                case MagicMimeEntry.STRING_TYPE: {
                    int len = 0;
                    // The following is not documented in the Magic(5)
                    // documentation.
                    // This is an extension to the magic rules and is provided by
                    // this utility.
                    // It allows for better matching of some text based files such
                    // as XML files
                    int index = typeStr.indexOf(">");
                    if (index != -1) {
                        len = Integer.parseInt(typeStr.substring(index + 1, typeStr
                                .length() - 1));
                        isBetween = true;
                    } else {
                        len = getContent().length();
                    }
                    buf = ByteBuffer.allocate(len);
                    buf.put(content, startPos, len);
                    break;
                }

                case MagicMimeEntry.SHORT_TYPE:
                case MagicMimeEntry.LESHORT_TYPE:
                case MagicMimeEntry.BESHORT_TYPE: {
                    buf = ByteBuffer.allocate(2);
                    buf.put(content, startPos, 2);
                    break;
                }

                case MagicMimeEntry.LELONG_TYPE:
                case MagicMimeEntry.BELONG_TYPE: {
                    buf = ByteBuffer.allocate(4);
                    buf.put(content, startPos, 4);
                    break;
                }

                case MagicMimeEntry.BYTE_TYPE: {
                    buf = ByteBuffer.allocate(1);
                    buf.put(content, startPos, 1);
                    break;
                }

                default: {
                    buf = null;
                    break;
                }
            }
        } catch (IndexOutOfBoundsException iobe) {
            // Content passed in is to small for the comparison so just ignore this match
            return null;
        }
        return buf;
    }

    private ByteBuffer readBuffer(RandomAccessFile raf) throws IOException {
        int startPos = getCheckBytesFrom();
        if (startPos > raf.length())
            return null;
        raf.seek(startPos);
        ByteBuffer buf;
        switch (getType()) {
            case MagicMimeEntry.STRING_TYPE: {
                int len = 0;
                // The following is not documented in the Magic(5) documentation.
                // This is an extension to the magic rules and is provided by this
                // utility.
                // It allows for better matching of some text based files such as
                // XML files
                int index = typeStr.indexOf(">");
                if (index != -1) {
                    len = Integer.parseInt(typeStr.substring(index + 1, typeStr
                            .length() - 1));
                    isBetween = true;
                } else {
                    len = getContent().length();
                }
                buf = ByteBuffer.allocate(len);
                raf.read(buf.array(), 0, len);
                break;
            }

            case MagicMimeEntry.SHORT_TYPE:
            case MagicMimeEntry.LESHORT_TYPE:
            case MagicMimeEntry.BESHORT_TYPE: {
                buf = ByteBuffer.allocate(2);
                raf.read(buf.array(), 0, 2);
                break;
            }

            case MagicMimeEntry.LELONG_TYPE:
            case MagicMimeEntry.BELONG_TYPE: {
                buf = ByteBuffer.allocate(4);
                raf.read(buf.array(), 0, 4);
                break;
            }

            case MagicMimeEntry.BYTE_TYPE: {
                buf = ByteBuffer.allocate(1);
                raf.read(buf.array(), 0, 1);
                break;
            }

            default: {
                buf = null;
                break;
            }
        }

        return buf;
    }

    private int getInputStreamMarkLength() {
        int len = _getInputStreamMarkLength();
        for (Iterator it = subEntries.iterator(); it.hasNext(); ) {
            MagicMimeEntry subEntry = (MagicMimeEntry) it.next();
            int subLen = subEntry.getInputStreamMarkLength();
            if (len < subLen)
                len = subLen;
        }
        return len;
    }

    private int _getInputStreamMarkLength() {
        switch (getType()) {
            case MagicMimeEntry.STRING_TYPE: {
                int len = 0;
                // Lets check if its a between test
                int index = typeStr.indexOf(">");
                if (index != -1) {
                    len = Integer.parseInt(typeStr.substring(index + 1, typeStr
                            .length() - 1));
                    isBetween = true;
                } else {
                    if (getContent() != null) // TODO content should never be null!
                        // We should already prevent this
                        // when parsing the magic file.
                        len = getContent().length();
                }
                return getCheckBytesFrom() + len + 1;
            }

            case MagicMimeEntry.SHORT_TYPE:
            case MagicMimeEntry.LESHORT_TYPE:
            case MagicMimeEntry.BESHORT_TYPE: {
                return getCheckBytesFrom() + 2;
            }

            case MagicMimeEntry.LELONG_TYPE:
            case MagicMimeEntry.BELONG_TYPE: {
                return getCheckBytesFrom() + 4;
            }

            case MagicMimeEntry.BYTE_TYPE: {
                return getCheckBytesFrom() + 1;
            }

            default: {
                return 0;
            }
        }
    }

    /*
     * private methods used for matching different types
     */
    private boolean match(ByteBuffer buf) throws IOException {
        boolean matches = true;
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

        switch (getType()) {
            case MagicMimeEntry.STRING_TYPE: {
                matches = matchString(buf);
                break;
            }

            case MagicMimeEntry.SHORT_TYPE: {
                matches = matchShort(buf, byteOrder);
                break;
            }

            case MagicMimeEntry.LESHORT_TYPE:
            case MagicMimeEntry.BESHORT_TYPE: {
                if (getType() == MagicMimeEntry.LESHORT_TYPE) {
                    byteOrder = ByteOrder.LITTLE_ENDIAN;
                }
                matches = matchShort(buf, byteOrder);
                break;
            }

            case MagicMimeEntry.LELONG_TYPE:
            case MagicMimeEntry.BELONG_TYPE: {
                if (getType() == MagicMimeEntry.LELONG_TYPE) {
                    byteOrder = ByteOrder.LITTLE_ENDIAN;
                }
                matches = matchLong(buf, byteOrder);
                break;
            }

            case MagicMimeEntry.BYTE_TYPE: {
                matches = matchByte(buf);
                break;
            }

            default: {
                matches = false;
                break;
            }
        }
        return matches;
    }

    private boolean matchString(ByteBuffer bbuf) throws IOException {
        if (isBetween) {
            String buffer = new String(bbuf.array());
            if (StringUtil.contains(buffer, getContent())) {
                return true;
            }
            return false;
        }

        if (operation.equals(MagicMimeEntryOperation.EQUALS)) {
            int read = getContent().length();
            for (int j = 0; j < read; j++) {
                if ((bbuf.get(j) & 0xFF) != getContent().charAt(j)) {
                    return false;
                }
            }
            return true;
        } else if (operation.equals(MagicMimeEntryOperation.NOT_EQUALS)) {
            int read = getContent().length();
            for (int j = 0; j < read; j++) {
                if ((bbuf.get(j) & 0xFF) != getContent().charAt(j)) {
                    return true;
                }
            }
            return false;
        } else if (operation.equals(MagicMimeEntryOperation.GREATER_THAN)) {
            String buffer = new String(bbuf.array());
            return buffer.compareTo(getContent()) > 0;
        } else if (operation.equals(MagicMimeEntryOperation.LESS_THAN)) {
            String buffer = new String(bbuf.array());
            return buffer.compareTo(getContent()) < 0;
        } else
            return false;
    }

    private long getMask(String maskString) {
        String[] tokens = maskString.split("&");
        if (tokens.length < 2) {
            return 0xffffffffL;
        }
        if (tokens[1].startsWith("0x")) {
            return Long.parseLong(tokens[1].substring(2).trim(), 16);
        } else if (tokens[1].startsWith("0")) {
            return Long.parseLong(tokens[1], 8);
        } else {
            return Long.parseLong(tokens[1]);
        }
    }

    private boolean matchByte(ByteBuffer bbuf) throws IOException {
        short found = (short) ((bbuf.get(0) & 0xff) & (short) getMask(typeStr));

        if (operation.equals(MagicMimeEntryOperation.EQUALS)) {
            return found == contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.NOT_EQUALS)) {
            return found != contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.GREATER_THAN)) {
            return found > contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.LESS_THAN)) {
            return found < contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.AND)) {
            boolean result = (found & contentNumber) == contentNumber;
            return result;
        } else if (operation.equals(MagicMimeEntryOperation.ANY)) {
            return true;
        } else if (operation.equals(MagicMimeEntryOperation.CLEAR)) {
            long maskedFound = found & contentNumber;
            boolean result = (maskedFound ^ contentNumber) == 0;
            return result;
        } else if (operation.equals(MagicMimeEntryOperation.NEGATED)) {
            int negatedFound = ~found;
            return negatedFound == contentNumber;
        } else
            return false;
    }

    private boolean matchShort(ByteBuffer bbuf, ByteOrder bo)
            throws IOException {
        bbuf.order(bo);

        int found = (int) ((bbuf.getShort() & 0xffff) & (int) getMask(typeStr));

        if (operation.equals(MagicMimeEntryOperation.EQUALS)) {
            return found == contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.NOT_EQUALS)) {
            return found != contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.GREATER_THAN)) {
            return found > contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.LESS_THAN)) {
            return found < contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.AND)) {
            boolean result = (found & contentNumber) == contentNumber;
            return result;
        } else if (operation.equals(MagicMimeEntryOperation.ANY)) {
            return true;
        } else if (operation.equals(MagicMimeEntryOperation.CLEAR)) {
            long maskedFound = found & contentNumber;
            boolean result = (maskedFound ^ contentNumber) == 0;
            return result;
        } else if (operation.equals(MagicMimeEntryOperation.NEGATED)) {
            int negatedFound = ~found;
            return negatedFound == contentNumber;
        } else
            return false;
    }

    private boolean matchLong(ByteBuffer bbuf, ByteOrder bo)
            throws IOException {
        bbuf.order(bo);

        long found = (long) ((bbuf.getInt() & 0xffffffffL) & getMask(typeStr));

        if (operation.equals(MagicMimeEntryOperation.EQUALS)) {
            return found == contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.NOT_EQUALS)) {
            return found != contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.GREATER_THAN)) {
            return found > contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.LESS_THAN)) {
            return found < contentNumber;
        } else if (operation.equals(MagicMimeEntryOperation.AND)) {
            boolean result = (found & contentNumber) == contentNumber;
            return result;
        } else if (operation.equals(MagicMimeEntryOperation.ANY)) {
            return true;
        } else if (operation.equals(MagicMimeEntryOperation.CLEAR)) {
            long maskedFound = found & contentNumber;
            boolean result = (maskedFound ^ contentNumber) == 0;
            return result;
        } else if (operation.equals(MagicMimeEntryOperation.NEGATED)) {
            long negatedFound = ~found;
            return negatedFound == contentNumber;
        } else
            return false;
    }

    /*
     * when bytes are read from the magic.mime file, the readers in java will
     * read escape sequences as regular bytes. That is, a sequence like \040
     * (representing ' ' - space character) will be read as a backslash followed
     * by a zero, four and zero -- 4 different bytes and not a single byte
     * representing space. This method parses the string and converts the
     * sequence of bytes representing escape sequence to a single byte
     *
     * NOTE: not all regular escape sequences are added yet. add them, if you
     * don't find one here
     */
    private static String stringWithEscapeSubstitutions(String s) {
        StringBuffer ret = new StringBuffer();
        int len = s.length();
        int indx = 0;
        int c;
        while (indx < len) {
            c = s.charAt(indx);
            if (c == '\n') {
                break;
            }

            if (c == '\\') {
                indx++;
                if (indx >= len) {
                    ret.append((char) c);
                    break;
                }

                int cn = s.charAt(indx);

                if (cn == '\\') {
                    ret.append('\\');
                } else if (cn == ' ') {
                    ret.append(' ');
                } else if (cn == 't') {
                    ret.append('\t');
                } else if (cn == 'n') {
                    ret.append('\n');
                } else if (cn == 'r') {
                    ret.append('\r');
                } else if (cn == 'x') { // Bugfix by Marco: Implemented reading
                    // of hex-encoded values.
                    // 2 hex digits should follow
                    indx += 2;
                    if (indx >= len) {
                        ret.append((char) c);
                        ret.append((char) cn);
                        break;
                    }
                    String hexDigits = s.substring(indx - 1, indx + 1);
                    int hexEncodedValue;
                    try {
                        hexEncodedValue = Integer.parseInt(hexDigits, 16);
                    } catch (NumberFormatException x) {
                        ret.append((char) c);
                        ret.append(hexDigits);
                        break;
                    }
                    ret.append((char) hexEncodedValue);
                } else if (cn >= '\60' && cn <= '\67') {
                    int escape = cn - '0';
                    indx++;
                    if (indx >= len) {
                        ret.append((char) escape);
                        break;
                    }
                    cn = s.charAt(indx);
                    if (cn >= '\60' && cn <= '\67') {
                        escape = escape << 3;
                        escape = escape | (cn - '0');

                        indx++;
                        if (indx >= len) {
                            ret.append((char) escape);
                            break;
                        }
                        cn = s.charAt(indx);
                        if (cn >= '\60' && cn <= '\67') {
                            escape = escape << 3;
                            escape = escape | (cn - '0');
                        } else {
                            indx--;
                        }
                    } else {
                        indx--;
                    }
                    ret.append((char) escape);
                } else {
                    ret.append((char) cn);
                }
            } else {
                ret.append((char) c);
            }
            indx++;
        }
        return new String(ret);
    }

    public boolean containsMimeType(String mimeType) {
        if (this.mimeType != null && this.mimeType.equals(mimeType))
            return true;

        for (Iterator it = subEntries.iterator(); it.hasNext(); ) {
            MagicMimeEntry subEntry = (MagicMimeEntry) it.next();
            if (subEntry.containsMimeType(mimeType))
                return true;
        }
        return false;
    }

    public MagicMimeEntry getParent() {
        return parent;
    }

    public List getSubEntries() {
        return Collections.unmodifiableList(subEntries);
    }

}
