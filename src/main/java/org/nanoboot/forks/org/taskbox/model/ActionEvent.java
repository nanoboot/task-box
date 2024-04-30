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

package org.nanoboot.forks.org.taskbox.model;

import java.util.EventObject;

import org.nanoboot.forks.org.taskbox.ApplicationHelper;

/**
 * @author ikesan
 *
 */
public class ActionEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;

	private Action action;
	private String property;
	private Object oldValue;
	private Object newValue;

	private boolean recycled=false;
	
	/**
	 * @param source
	 * @param action
	 * @param property
	 * @param oldValue
	 * @param newValue
	 */
	public ActionEvent(Folder f, Action action, String property, Object oldValue, Object newValue, boolean recycled) {
		super(f);
		this.action = action;
		this.property = property;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.recycled = recycled;
		if (property!=null) {
			if (getNewValue()==getOldValue()) {
				throw new RuntimeException("Internal error, property not changed: "+toString());
			}
			if ((getNewValue()==null || ApplicationHelper.EMPTY_STRING.equals(getNewValue())) 
					&& (!Action.REMIND_PROPERTY_NAME.equals(property))) {
				try {
					throw new RuntimeException("Internal warning, new value is null: "+toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * @param source
	 * @param action
	 * @param property
	 * @param oldValue
	 * @param newValue
	 */
	public ActionEvent(Folder f, Action action, boolean recycled) {
		super(f);
		this.action = action;
		this.recycled=recycled;
	}
	
	public boolean isRecycled() {
		return recycled;
	}
	
	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @return the newValue
	 */
	public Object getNewValue() {
		return newValue;
	}

	/**
	 * @return the oldValue
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * @return the property
	 */
	public String getProperty() {
		return property;
	}
	
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		sb.append("ActionEvent={source=");
		sb.append(((Folder)getSource()).getName());
		sb.append(", action=");
		sb.append(action.getId());
		sb.append(", prop=");
		sb.append(property);
		sb.append(", old=");
		sb.append(oldValue);
		sb.append(", new=");
		sb.append(newValue);
		sb.append("}");
		return sb.toString();
	}
}
