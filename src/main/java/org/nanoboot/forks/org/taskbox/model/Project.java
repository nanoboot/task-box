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

/**
 * @author ikesan
 *
 */
public class Project extends Folder {
	private String goal;
	public Project(GTDModel parent, int id, String name) {
		super(parent, id, name, Folder.FolderType.PROJECT);
	}
	/**
	 * @return the goal
	 */
	public String getGoal() {
		return goal;
	}
	/**
	 * @param goal the goal to set
	 */
	public void setGoal(String goal) {
		if ((this.goal!=null && this.goal.equals(goal)) || (this.goal==null && goal==null)) {
			return;
		}
		String o= this.goal;
		this.goal = goal;
		getParent().fireFolderModified(this,"goal",o,goal,false);
	}
	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		sb.append("Project{ id= ");
		sb.append(getId());
		sb.append(", name= ");
		sb.append(getName());
		sb.append(", closed= ");
		sb.append(isClosed());
		sb.append("}");
		return sb.toString();
	}
}
