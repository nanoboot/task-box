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

import java.util.EventListener;

/**
 * @author ikesan
 */
public interface JournalEntryListener extends EventListener {

    void journalEntryChanged(JournalEntryEvent e);

    void journalEntryIntervalAdded(JournalEntryEvent e);

    void journalEntryIntervalRemoved(JournalEntryEvent e);

}
