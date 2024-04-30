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

package org.nanoboot.forks.org.gtdbox.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.nanoboot.forks.org.gtdbox.model.Action;
import org.nanoboot.forks.org.gtdbox.model.ActionEvent;
import org.nanoboot.forks.org.gtdbox.model.ActionFilter;
import org.nanoboot.forks.org.gtdbox.model.Folder;
import org.nanoboot.forks.org.gtdbox.model.FolderEvent;
import org.nanoboot.forks.org.gtdbox.model.GTDModel;
import org.nanoboot.forks.org.gtdbox.model.GTDModelAdapter;

/**
 * @author ikesan
 *
 */
public class SelectionModel {
	
	public static enum SelectionMode { Lists, Projects, Lists_and_projects, Actions }
	public static enum FilterAggregator { AND, OR };
	
	private static final String PROP_SELECTED_FOLDERS = "selectedFolders";
	private static final String PROP_SELECTED_ACTIONS = "selectedActions";
	
	private SelectionMode selectionMode= SelectionMode.Lists;
	private GTDModel model;
	
	private List<Folder>selectedFolders = new ArrayList<Folder>();
	private List<Action>selectedActions = new ArrayList<Action>();
	
	private List<ActionFilter> selectionFilters= new ArrayList<ActionFilter>();
	
	private PropertyChangeSupport prop= new PropertyChangeSupport(this);
	private FilterAggregator filterAggregator= FilterAggregator.AND;
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		prop.addPropertyChangeListener(l);
	}
	public void removePropertyChangeListener(PropertyChangeListener l) {
		prop.removePropertyChangeListener(l);
	}
	public void addPropertyChangeListener(String p, PropertyChangeListener l) {
		prop.addPropertyChangeListener(p, l);
	}
	public void removePropertyChangeListener(String p, PropertyChangeListener l) {
		prop.removePropertyChangeListener(p, l);
	}
	
	public void setModel(GTDModel model) {
		this.model = model;
		model.addGTDModelListener(new GTDModelAdapter() {
		
			@Override
			public void elementRemoved(FolderEvent a) {
				removeSelectedAction(a.getAction());
			}
		
			@Override
			public void elementModified(ActionEvent a) {
				if (!isAcceptable(a.getAction().getFolder(),a.getAction())) {
					removeSelectedAction(a.getAction());
				}
			}
		
			@Override
			public void folderRemoved(Folder folder) {
				removeSelectedFolder(folder);
			}
		
			@Override
			public void folderModified(FolderEvent folder) {
				if (!isAcceptable(folder.getFolder(),null)) {
					removeSelectedFolder(folder.getFolder());
				}
			}
		
		});
	}
	
	private void firePropertyChangeEvent(String p, Object val) {
		prop.firePropertyChange(p, null, val);
	}

	protected void removeSelectedFolder(Folder folder) {
		if (selectedFolders.remove(folder)){
			firePropertyChangeEvent(PROP_SELECTED_FOLDERS,selectedFolders);
		}
	}
	protected boolean isAcceptable(Folder folder, Action action) {
		if (selectionFilters.size()>0) {
			for (ActionFilter f : selectionFilters) {
				if (filterAggregator==FilterAggregator.AND) {
					if (!f.isAcceptable(folder, action)) {
						return false;
					}
				} else {
					if (f.isAcceptable(folder, action)) {
						return true;
					}
				}
			}
			return filterAggregator==FilterAggregator.AND;
		}
		return true;
	}
	
	protected void removeSelectedAction(Action action) {
		if (selectedActions.remove(action)){
			firePropertyChangeEvent(PROP_SELECTED_ACTIONS,selectedActions);
		}
	}
	
}
