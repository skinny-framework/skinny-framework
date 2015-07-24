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

import java.util.Iterator;

class MatchingMagicMimeEntry {

    public MatchingMagicMimeEntry(MagicMimeEntry magicMimeEntry) // , boolean exactMatch)
    {
        this.magicMimeEntry = magicMimeEntry;
    }

    private MagicMimeEntry magicMimeEntry;
    private double specificity = -1; // can only be positive when initialised - at least with our current formula ;-)

    public MagicMimeEntry getMagicMimeEntry() {
        return magicMimeEntry;
    }

    private int getLevel() {
        int l = 0;
        MagicMimeEntry parent = magicMimeEntry.getParent();
        while (parent != null) {
            ++l;
            parent = parent.getParent();
        }
        return l;
    }

    private int getRecursiveSubEntryCount() {
        return getRecursiveSubEntryCount(magicMimeEntry, 0);
    }

    public int getRecursiveSubEntryCount(MagicMimeEntry entry, int subLevel) {
        ++subLevel;
        int result = 0;
        for (Iterator it = entry.getSubEntries().iterator(); it.hasNext(); ) {
            MagicMimeEntry subEntry = (MagicMimeEntry) it.next();
            result += subLevel * (1 + getRecursiveSubEntryCount(subEntry, subLevel));
        }
        return result;
    }

    public double getSpecificity() {
        if (specificity < 0) {
            // The higher the level, the more specific it probably is.
            // The more children below the current match, the less specific it is.
            // TODO This formula needs to be changed/optimized.
            specificity = (double) (getLevel() + 1) / (getRecursiveSubEntryCount() + 1);
        }

        return specificity;
    }

    public MimeType getMimeType() {
        return new MimeType(magicMimeEntry.getMimeType());
    }

    public String toString() {
        return this.getClass().getName() + '[' + getMimeType() + ',' + getSpecificity() + ']';
    }

}
