/*
 * Copyright 2007-2008 Medsea Business Solutions S.L.
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

import java.util.List;

/**
 * This exception is thrown if while parsing the magic.mime files an invalid un-parsable entry is found
 *
 * @author Steven McArdle
 */
class InvalidMagicMimeEntryException extends RuntimeException {

    private static final long serialVersionUID = -6705937358834408523L;

    public InvalidMagicMimeEntryException() {
        super("Invalid Magic Mime Entry: Unknown entry");
    }

    public InvalidMagicMimeEntryException(List mimeMagicEntry) {
        super("Invalid Magic Mime Entry: " + mimeMagicEntry.toString());
    }

    public InvalidMagicMimeEntryException(List mimeMagicEntry, Throwable t) {
        super("Invalid Magic Mime Entry: " + mimeMagicEntry.toString(), t);
    }

    public InvalidMagicMimeEntryException(Throwable t) {
        super(t);
    }

    public InvalidMagicMimeEntryException(String message, Throwable t) {
        super(message, t);
    }

}
