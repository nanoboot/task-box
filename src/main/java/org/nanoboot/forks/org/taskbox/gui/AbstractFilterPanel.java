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

package org.nanoboot.forks.org.taskbox.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.nanoboot.forks.org.taskbox.GTDBoxEngine;
import org.nanoboot.forks.org.taskbox.model.ActionFilter;

public abstract class AbstractFilterPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private ActionTable table;
	private GTDBoxEngine engine;

	public AbstractFilterPanel() {
		super();
	}

	protected void fireSearch(ActionFilter f) {
		if (table==null) {
			return;
		}
		table.setFilter(f);
	}

	/**
	 * @return the table
	 */
	public ActionTable getTable() {
		return table;
	}

	/**
	 * @param table the table to set
	 */
	public void setTable(ActionTable t) {
		this.table = t;
		table.addPropertyChangeListener("folder",new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				clearFilters();
				setEnabled(table.getFolder()!=null);
			}
		});
		setEnabled(table.getFolder()!=null);
	}



	protected abstract void clearFilters();

	/**
	 * @param engine the engine to set
	 */
	public void setEngine(GTDBoxEngine engine) {
		this.engine = engine;
	}

	/**
	 * @return the engine
	 */
	public GTDBoxEngine getEngine() {
		return engine;
	}

}
