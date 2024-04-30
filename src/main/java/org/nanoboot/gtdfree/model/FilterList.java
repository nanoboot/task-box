/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *
 *    This file is part of GTD-Free.
 *
 *    GTD-Free is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    GTD-Free is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with GTD-Free.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.gtdfree.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows aggregation of filter.
 *
 * @author ikesan
 */
public class FilterList implements ActionFilter {

    private final List<ActionFilter> filters = new ArrayList<>(5);

    /* (non-Javadoc)
     * @see org.gtdfree.model.ActionFilter#isAccepted(org.gtdfree.model.Folder, org.gtdfree.model.Action)
     */
    @Override
    public boolean isAcceptable(Folder f, Action a) {
        for (ActionFilter filter : filters) {
            if (!filter.isAcceptable(f, a)) {
                return false;
            }
        }
        return true;
    }

    public void add(ActionFilter f) {
        filters.add(f);
    }

    public ActionFilter get(int i) {
        return filters.get(i);
    }

    public int size() {
        return filters.size();
    }

    public ActionFilter[] filters() {
        return filters.toArray(new ActionFilter[0]);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof FilterList fl) {

            return fl.filters.equals(filters);
        }

        return false;
    }

}
