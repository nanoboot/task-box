/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *    
 *    This file is part of GTD-Box.
 *    
 *    GTD-Box is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *    
 *    GTD-Box is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *    
 *    You should have received a copy of the GNU General Public License
 *    along with GTD-Box.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.forks.org.gtdbox.journal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

/**
 * @author ikesan
 *
 */
public class JournalModel {
	
	class EventHandler implements JournalModelListener {
		
		@Override
		public void journalEntryIntervalRemoved(JournalEntryEvent e) {
			JournalModelListener[] l= listeners.getListeners(JournalModelListener.class);
			
			for (int i = 0; i < l.length; i++) {
				l[i].journalEntryIntervalRemoved(e);
			}
		}
	
		@Override
		public void journalEntryIntervalAdded(JournalEntryEvent e) {
			JournalModelListener[] l= listeners.getListeners(JournalModelListener.class);
			
			for (int i = 0; i < l.length; i++) {
				l[i].journalEntryIntervalAdded(e);
			}
		}
	
		@Override
		public void journalEntryChanged(JournalEntryEvent e) {
			JournalModelListener[] l= listeners.getListeners(JournalModelListener.class);
			
			for (int i = 0; i < l.length; i++) {
				l[i].journalEntryChanged(e);
			}
		}
	
		@Override
		public void journalEntryAdded(JournalEntryEvent e) {
			JournalModelListener[] l= listeners.getListeners(JournalModelListener.class);
			
			for (int i = 0; i < l.length; i++) {
				l[i].journalEntryAdded(e);
			}
		}

		public void journalEntryAdded(JournalEntry je) {
			JournalEntryEvent e= new JournalEntryEvent(je,"entries",je,null,-1); 
			journalEntryAdded(e);
		}
	};

	
	private int lastEntryID=0; 
	private Map<Long,List<JournalEntry>> data;
	private EventListenerList listeners= new EventListenerList();
	private EventHandler eventHandler= new EventHandler();

	public JournalModel() {
		data= new HashMap<Long, List<JournalEntry>>();
	}
	
	public JournalEntry[] getEntries(long day) {
		List<JournalEntry> l= data.get(day);
		
		if (l!=null) {
			return l.toArray(new JournalEntry[l.size()]);
		}
		
		return null;
	}
	
	public JournalEntry addEntry(long day) {
		
		JournalEntry e= new JournalEntry(lastEntryID++);
		
		List<JournalEntry> l= data.get(day);
		if (l==null) {
			l= new ArrayList<JournalEntry>();
			data.put(day, l);
		}
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
	
	
}
