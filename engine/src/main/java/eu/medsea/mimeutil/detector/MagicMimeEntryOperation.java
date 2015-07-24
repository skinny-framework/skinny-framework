package eu.medsea.mimeutil.detector;

import java.util.HashMap;
import java.util.Map;

/**
 * Quote from <a href="http://linux.die.net/man/5/magic">http://linux.die.net/man/5/magic</a>:
 * <p>
 * Numeric values may be preceded by a character indicating the operation to be performed.
 * It may be =, to specify that the value from the file must equal the specified value, &lt;,
 * to specify that the value from the file must be less than the specified value, &gt;, to
 * specify that the value from the file must be greater than the specified value, &amp;, to
 * specify that the value from the file must have set all of the bits that are set in the
 * specified value, ^, to specify that the value from the file must have clear any of the
 * bits that are set in the specified value, or ~, the value specified after is negated before
 * tested. x, to specify that any value will match. If the character is omitted, it is assumed
 * to be =. For all tests except string and regex, operation ! specifies that the line matches
 * if the test does not succeed.
 * </p>
 * <p>
 * For string values, the byte string from the file must match the specified byte string. The
 * operators =, &lt; and &gt; (but not &amp;) can be applied to strings. The length used for
 * matching is that of the string argument in the magic file. This means that a line can match any
 * string, and then presumably print that string, by doing &gt;\0 (because all strings are greater
 * than the null string).
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
final class MagicMimeEntryOperation {

    private static final Map operationID2operation = new HashMap();

    public static final MagicMimeEntryOperation EQUALS = new MagicMimeEntryOperation('=');
    public static final MagicMimeEntryOperation LESS_THAN = new MagicMimeEntryOperation('<');
    public static final MagicMimeEntryOperation GREATER_THAN = new MagicMimeEntryOperation('>');
    public static final MagicMimeEntryOperation AND = new MagicMimeEntryOperation('&');
    public static final MagicMimeEntryOperation CLEAR = new MagicMimeEntryOperation('^');
    public static final MagicMimeEntryOperation NEGATED = new MagicMimeEntryOperation('~');
    public static final MagicMimeEntryOperation ANY = new MagicMimeEntryOperation('x');
    public static final MagicMimeEntryOperation NOT_EQUALS = new MagicMimeEntryOperation('!');

    public static MagicMimeEntryOperation getOperation(char operationID) {
        Character operationIDCharacter = new Character(operationID);
        return (MagicMimeEntryOperation) operationID2operation.get(operationIDCharacter);
    }

    public static MagicMimeEntryOperation getOperationForStringField(String content) {
        MagicMimeEntryOperation operation = getOperation(content);
        // String and regex do only support a subset of the operations => filter.
        if (EQUALS.equals(operation) || LESS_THAN.equals(operation) || GREATER_THAN.equals(operation))
            return operation;
        else
            return EQUALS;
    }

    public static MagicMimeEntryOperation getOperationForNumberField(String content) {
        return getOperation(content);
    }

    private static MagicMimeEntryOperation getOperation(String content) {
        if (content.length() == 0)
            return EQUALS;

        MagicMimeEntryOperation operation = getOperation(content.charAt(0));
        if (operation == null)
            return EQUALS;
        else
            return operation;
    }

    private static void registerOperation(MagicMimeEntryOperation operation) {
        Character operationIDCharacter = new Character(operation.getOperationID());
        if (operationID2operation.containsKey(operationIDCharacter))
            throw new IllegalStateException("Duplicate registration of operation " + operationIDCharacter);

        operationID2operation.put(operationIDCharacter, operation);
    }

    private final char operationID;

    MagicMimeEntryOperation(char operationID) {
        this.operationID = operationID;

        registerOperation(this);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operationID;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MagicMimeEntryOperation other = (MagicMimeEntryOperation) obj;
        return this.operationID == other.operationID;
    }

    public final char getOperationID() {
        return operationID;
    }

    public String toString() {
        return this.getClass().getName() + '[' + operationID + ']';
    }

}
