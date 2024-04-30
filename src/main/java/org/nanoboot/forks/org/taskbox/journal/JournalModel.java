/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *
 *    This file is part of Task-Box.
 *
 *    Task-Box is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Task-Box is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Task-Box.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.forks.org.taskbox.journal;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ikesan
 */
public class JournalModel {

    private final Map<Long, List<JournalEntry>> data;
    private final EventListenerList listeners = new EventListenerList();
    private final EventHandler eventHandler = new EventHandler();
    private int lastEntryID = 0;
    public JournalModel() {
        data = new HashMap<>();
    }

    public JournalEntry[] getEntries(long day) {
        List<JournalEntry> l = data.get(day);

        if (l != null) {
            return l.toArray(new JournalEntry[0]);
        }

        return null;
    }

    public JournalEntry addEntry(long day) {

        JournalEntry e = new JournalEntry(lastEntryID++);

        List<JournalEntry> l =
                data.computeIfAbsent(day, k -> new ArrayList<JournalEntry>());
        l.add(e);

        e.addJournalEntryListener(eventHandler);
        eventHandler.journalEntryAdded(e);

        return e;
    }

    public void addJournalModelListener(JournalModelListener l) {
        listeners.add(JournalModelListener.class, l);
    }

    public void removeJournalModelListener(JournalModelListener l) {
        listeners.remove(JournalModelListener.class, l);
    }

    class EventHandler implements JournalModelListener {

        @Override
        public void journalEntryIntervalRemoved(JournalEntryEvent e) {
            JournalModelListener[] l =
                    listeners.getListeners(JournalModelListener.class);

            for (JournalModelListener journalModelListener : l) {
                journalModelListener.journalEntryIntervalRemoved(e);
            }
        }

        @Override
        public void journalEntryIntervalAdded(JournalEntryEvent e) {
            JournalModelListener[] l =
                    listeners.getListeners(JournalModelListener.class);

            for (JournalModelListener journalModelListener : l) {
                journalModelListener.journalEntryIntervalAdded(e);
            }
        }

        @Override
        public void journalEntryChanged(JournalEntryEvent e) {
            JournalModelListener[] l =
                    listeners.getListeners(JournalModelListener.class);

            for (JournalModelListener journalModelListener : l) {
                journalModelListener.journalEntryChanged(e);
            }
        }

        @Override
        public void journalEntryAdded(JournalEntryEvent e) {
            JournalModelListener[] l =
                    listeners.getListeners(JournalModelListener.class);

            for (JournalModelListener journalModelListener : l) {
                journalModelListener.journalEntryAdded(e);
            }
        }

        public void journalEntryAdded(JournalEntry je) {
            JournalEntryEvent e =
                    new JournalEntryEvent(je, "entries", je, null, -1);
            journalEntryAdded(e);
        }
    }

}
