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

package org.nanoboot.tasklibre.model;

/**
 * Filters acitions with certain priority level.
 *
 * @author ikesan
 */
public final class PriorityFilter implements ActionFilter {

    private final Priority priority;
    private final boolean exactMatch;

    public PriorityFilter(Priority priority, boolean exactMatch) {
        this.exactMatch = exactMatch;
        this.priority = priority;
    }

    /* (non-Javadoc)
     * @see org.gtdfree.model.ActionFilter#isAccepted(org.gtdfree.model.Folder, org.gtdfree.model.Action)
     */
    @Override
    public boolean isAcceptable(Folder f, Action a) {

        if (exactMatch) {
            return priority == a.getPriority();
        }
        return priority.ordinal() <= a.getPriority().ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PriorityFilter f) {

            return f.exactMatch == exactMatch && f.priority == priority;

        }
        return false;
    }

}
