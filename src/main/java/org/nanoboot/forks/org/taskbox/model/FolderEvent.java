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
 */
public class FolderEvent extends ActionEvent {
    private static final long serialVersionUID = 1L;

    /**
     * @param source
     * @param folder
     * @param action
     */
    public FolderEvent(Folder folder, Action action, boolean recycled) {
        super(folder, action, recycled);
    }

    /**
     * @param source
     * @param action
     * @param property
     * @param oldValue
     * @param newValue
     */
    public FolderEvent(Folder f, Action action, String property,
            Object oldValue, Object newValue, boolean recycled) {
        super(f, action, property, oldValue, newValue, recycled);
    }

    /**
     * @return the folder
     */
    public Folder getFolder() {
        return (Folder) getSource();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FolderEvent={source=");
        sb.append(((Folder) getSource()).getName());
        sb.append(", prop=");
        sb.append(getProperty());
        sb.append(", old=");
        sb.append(getOldValue());
        sb.append(", new=");
        sb.append(getNewValue());
        sb.append("}");
        return sb.toString();
    }

}
