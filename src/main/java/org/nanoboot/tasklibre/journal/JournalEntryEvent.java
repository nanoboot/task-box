/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *
 *    This file is part of Task-Libre.
 *
 *    Task-Libre is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Task-Libre is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Task-Libre.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.tasklibre.journal;

import java.util.EventObject;

/**
 * @author ikesan
 */
public class JournalEntryEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private final JournalEntry yournalEntry;
    private final int index;
    private final String property;
    private final Object oldValue;
    private final Object newValue;

    /**
     * @param source
     * @param index
     * @param newValue
     * @param oldValue
     * @param property
     * @param yournalEntry
     */
    public JournalEntryEvent(JournalEntry yournalEntry, String property,
            Object newValue,
            Object oldValue, int index) {
        super(yournalEntry);
        this.index = index;
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.property = property;
        this.yournalEntry = yournalEntry;
    }

    /**
     * @return the yournalEntry
     */
    public JournalEntry getJournalEntry() {
        return yournalEntry;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the property
     */
    public String getProperty() {
        return property;
    }

    /**
     * @return the oldValue
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * @return the newValue
     */
    public Object getNewValue() {
        return newValue;
    }

}
