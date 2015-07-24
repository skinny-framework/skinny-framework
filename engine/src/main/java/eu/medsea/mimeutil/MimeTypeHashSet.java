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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is used to represent a collection of <code>MimeType</code> objects.
 * <p>
 * It uses a {@link LinkedHashSet} as the backing collection and implements all
 * methods of both the {@link Set} and {@link Collection} interfaces and maintains the list in insertion order.
 * </p>
 * <p>
 * This class is pretty tolerant of the parameter type that can be passed to methods that
 * take an {@link Object} parameter. These methods can take any of the following types:
 * <ul>
 * <li><code>MimeType</code> see {@link MimeType}</li>
 * <li><code>String</code>. This can be a string representation of a mime type such as text/plain or a
 * String representing a comma separated list of mime types such as text/plain,application/xml</li>
 * <li><code>String []</code>. Each element of the array can be a string representation of a mime type or a comma separated
 * list of mime types. See above.</li>
 * <li><code>Collection</code>. Each element in the collection can be one of the above types or another Collection.</li>
 * </ul>
 * <p>
 * Also methods taking a Collection as the parameter are able to handle Collections containing elements that are any of the types listed above.
 * </p>
 * If an object is passed that is not one of these types the method will throw a MimeException unless the method returns a
 * boolean in which case it will return false.
 * </p>
 * <p>
 * Note that this implementation is not synchronized. If multiple threads access a set concurrently, and at least one of the threads modifies the set,
 * it must be synchronized externally. This is typically accomplished by synchronizing on some object that naturally encapsulates the set.
 * If no such object exists, the set should be "wrapped" using the Collections.synchronizedSet method. This is best done at creation time,
 * to prevent accidental unsynchronized access to the HashSet  instance:
 * <ul>
 * <li><code>Set s = Collections.synchronizedSet(new MimeTypeHashSet(...));</code></li>
 * <li><code>Collection c = Collections.synchronizedSet(new MimeTypeHashSet(...));</code></li>
 * </ul>
 *
 * @author Steven McArdle
 * @see LinkedHashSet for a description of the way the Iterator works with regard to the fail-fast functionality.
 * </p>
 */
class MimeTypeHashSet implements Set, Collection {

    private Set hashSet = new LinkedHashSet();

    MimeTypeHashSet() {
    }

    /**
     * Construct a new MimeTypeHashSet from a collection containing elements that can represent mime types.
     *
     * @param collection See the introduction to this class for a description of the elements the Collection can contain.
     */
    MimeTypeHashSet(final Collection collection) {
        addAll(collection);
    }

    /**
     * This method will add MimeType(s) to the internal HashSet if it does not already contain them.
     * It is able to take different types of object related to mime types as discussed in the introduction to this class.
     * <p>
     * This is a pretty useful override of the HashSet add(Object) method and can be used in the following ways:
     * </p>
     * <p>
     * <ul>
     * <li>add(String mimeType) examples <code>add("text/plain"); add("text/plain,application/xml");</code></li>
     * <li>add(String [] mimeTypes) examples <code>add(new String [] {"text/plain", "application/xml"});</code></li>
     * <li>add(Collection mimeTypes) This delegates to the addAll(Collection) method</li>
     * <li>add(MimeType)</li>
     * </ul>
     * </p>
     *
     * @param arg0 can be a MimeType, String, String [] or Collection. See the introduction to this class.
     * @return true if the set did not already contain the specified element.
     */
    public boolean add(final Object arg0) {
        if (arg0 == null) {
            // We don't allow null
            return false;
        }
        if ((arg0 instanceof MimeType)) {
            // Add a MimeType
            if (contains(arg0)) {
                // We already have an entry so get it and update the specificity
                updateSpecificity((MimeType) arg0);
            }
            MimeUtil.addKnownMimeType((MimeType) arg0);
            return hashSet.add(arg0);

        } else if (arg0 instanceof Collection) {
            // Add a collection
            return addAll((Collection) arg0);
        } else if (arg0 instanceof String) {
            // Add a string representation of a mime type that could be a comma separated list
            String[] mimeTypes = ((String) arg0).split(",");
            boolean added = false;
            for (int i = 0; i < mimeTypes.length; i++) {
                try {
                    if (add(new MimeType(mimeTypes[i]))) {
                        added = true;
                    }
                } catch (Exception e) {
                    // Ignore this as it's not a type we can use
                }
            }
            return added;
        } else if (arg0 instanceof String[]) {
            // Add a String array of mime types each of which can be a comma separated list of mime types
            boolean added = false;
            String[] mimeTypes = (String[]) arg0;
            for (int i = 0; i < mimeTypes.length; i++) {
                String[] parts = mimeTypes[i].split(",");
                for (int j = 0; j < parts.length; j++) {
                    try {
                        if (add(new MimeType(parts[j]))) {
                            added = true;
                        }
                    } catch (Exception e) {
                        // Ignore this as it's not a type we can use
                    }
                }
            }
            return added;
        }
        // Can't add this type
        return false;
    }

    /**
     * Add a collection of objects to the internal HashSet. See the introduction to this class to see what the Collection can contain.
     *
     * @param arg0 is a collection of objects each of which should contain or be items that can be used to represent mime types.
     *             Objects that are not recognised as being able to represent a mime type are ignored.
     * @return true if this collection changed as a result of the call.
     * @throws NullPointerException
     */
    public boolean addAll(final Collection arg0) throws NullPointerException {
        if (arg0 == null) {
            throw new NullPointerException();
        }
        boolean added = false;
        for (Iterator it = arg0.iterator(); it.hasNext(); ) {
            try {
                if (add(it.next())) {
                    added = true;
                }
            } catch (Exception e) {
                // Ignore this entry as it's not a types that can be turned into MimeTypes
            }
        }
        return added;
    }

    /**
     * @see LinkedHashSet#clear()
     */
    public void clear() {
        hashSet.clear();
    }

    /**
     * Checks if this Collection contains the type passed in. See the introduction of this class for a description of the types that can be parsed.
     *
     * @param o an object representing one of the recognised types that can represent mime types.
     * @return true if this set contains the specified element or elements.
     */
    public boolean contains(final Object o) {
        if (o instanceof MimeType) {
            return hashSet.contains(o);
        } else if (o instanceof Collection) {
            return containsAll((Collection) o);
        } else if (o instanceof String) {
            String[] parts = ((String) o).split(",");
            for (int i = 0; i < parts.length; i++) {
                if (!contains(new MimeType(parts[i]))) {
                    return false;
                }
            }
            return true;
        } else if (o instanceof String[]) {
            String[] mimeTypes = (String[]) o;
            for (int i = 0; i < mimeTypes.length; i++) {
                String[] parts = mimeTypes[i].split(",");
                for (int j = 0; j < parts.length; j++) {
                    if (!contains(new MimeType(parts[j]))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks that this Collection contains this collection of object that can represent mime types.
     * See the introduction to this class for a description of these types.
     *
     * @param arg0 a collection of objects each of which can be a type that can represent a mime type
     * @ return true if this collection contains all of the elements in the specified collection.
     * @ throws NullPointerException if the passed in argument in null.
     */
    public boolean containsAll(final Collection arg0) {
        if (arg0 == null) {
            throw new NullPointerException();
        }
        for (Iterator it = arg0.iterator(); it.hasNext(); ) {
            if (!contains(it.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see LinkedHashSet#isEmpty()
     */
    public boolean isEmpty() {
        return hashSet.isEmpty();
    }

    /**
     * @see LinkedHashSet#iterator()
     */
    public Iterator iterator() {
        return hashSet.iterator();
    }

    /**
     * Remove mime types from the collection. The parameter can be any type described in the introduction to this class.
     *
     * @param o - Object to be removed
     * @return true if the set was modified.
     */
    public boolean remove(final Object o) {
        boolean removed = false;
        if (o == null) {
            return removed;
        }
        if (o instanceof MimeType) {
            return hashSet.remove(o);
        } else if (o instanceof String) {
            String[] parts = ((String) o).split(",");
            for (int i = 0; i < parts.length; i++) {
                if (remove(new MimeType(parts[i]))) {
                    removed = true;
                }
            }
        } else if (o instanceof String[]) {
            String[] mimeTypes = (String[]) o;
            for (int i = 0; i < mimeTypes.length; i++) {
                String[] parts = mimeTypes[i].split(",");
                for (int j = 0; j < parts.length; j++) {
                    if (remove(new MimeType(parts[j]))) {
                        removed = true;
                    }
                }
            }
        } else if (o instanceof Collection) {
            return removeAll((Collection) o);
        }
        return removed;
    }

    /**
     * Remove all the items in the passed in Collection that can represent a mime type.
     * See the introduction of this class to see the types of objects the passed in collection can contain.
     *
     * @return true if the set was modified.
     * @throws NullPointerException if the Collection passed in is null
     */
    public boolean removeAll(final Collection arg0) {
        if (arg0 == null) {
            throw new NullPointerException();
        }
        boolean removed = false;
        for (Iterator it = ((Collection) arg0).iterator(); it.hasNext(); ) {
            if (remove(it.next())) {
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Keep only the MimeType(s) in this collection that are also present in the passed in collection.
     * The passed in Collection is normalised into a MimeTypeHashSet before delegating down to the HashSet
     * retainAll(Collection) method.
     *
     * @param arg0 - collection of types each of which can represent a mime type.
     * @ return true if this collection changed as a result of the call.
     */
    public boolean retainAll(final Collection arg0) {
        if (arg0 == null) {
            throw new NullPointerException();
        }
        // Make sure our collection is a real collection of MimeType(s)
        Collection c = new MimeTypeHashSet(arg0);
        return hashSet.retainAll(c);
    }

    /**
     * @see LinkedHashSet#size()
     */
    public int size() {
        return hashSet.size();
    }

    /**
     * @see LinkedHashSet#toArray()
     */
    public Object[] toArray() {
        return hashSet.toArray();
    }

    /**
     * @see LinkedHashSet#add(Object)
     */
    public Object[] toArray(final Object[] arg0) {
        return hashSet.toArray(arg0);
    }

    /**
     * Create a String representation of this Collection as a comma separated list
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Iterator it = iterator(); it.hasNext(); ) {
            buf.append(((MimeType) it.next()).toString());
            if (it.hasNext()) {
                buf.append(",");
            }
        }
        return buf.toString();
    }

    /**
     * Compares the specified object with this set for equality. See the introduction of this class for a description of what this parameter can represent
     *
     * @param o - Object to be compared for equality with this set.
     * @return true if the specified object is equal to this set.
     */
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        Collection c = new MimeTypeHashSet();
        c.add(o);
        return match(c);
    }

    private boolean match(final Collection c) {
        if (this.size() != c.size()) {
            return false;
        }
        MimeType[] mt = (MimeType[]) c.toArray(new MimeType[c.size()]);

        for (int i = 0; i < mt.length; i++) {
            if (!this.contains(mt[i])) {
                return false;
            }
        }
        return true;
    }

    private void updateSpecificity(final MimeType o) {
        MimeType mimeType = get(o);
        int specificity = mimeType.getSpecificity() + o.getSpecificity();
        mimeType.setSpecificity(specificity);
        o.setSpecificity(specificity);
    }

    private MimeType get(MimeType mimeType) {
        for (Iterator it = hashSet.iterator(); it.hasNext(); ) {
            MimeType mt = (MimeType) it.next();
            if (mt.equals(mimeType)) {
                return mt;
            }
        }
        return null;
    }

	/*
     * The following functions are extensions to the Collection and Set interfaces
	 * implemented by this class and require the Collection to be cast to a MimeTypeHashSet
	 * before they can be accessed.
	 */

    /**
     * Return a sub collection from this collection containing all MimeType(s) that match the
     * pattern passed in. The pattern can be any pattern supported by the {@Link Pattern} class.
     *
     * @param pattern to match against the collection of MimeType(s)
     * @return Collection of matching MimeType(s) or an empty set if no matches found
     * @see String#matches(String) for a full description of the regular expression matching
     */
    public Collection matches(String pattern) {
        Collection c = new MimeTypeHashSet();
        for (Iterator it = iterator(); it.hasNext(); ) {
            MimeType mimeType = (MimeType) it.next();
            if (mimeType.toString().matches(pattern)) {
                c.add(mimeType);
            }
        }
        return c;
    }

}