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

package org.nanoboot.gtdfree.gui;

import org.nanoboot.gtdfree.GTDFreeEngine;
import org.nanoboot.gtdfree.model.ActionFilter;

import javax.swing.JPanel;

public abstract class AbstractFilterPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private ActionTable table;
    private GTDFreeEngine engine;

    public AbstractFilterPanel() {
        super();
    }

    protected void fireSearch(ActionFilter f) {
        if (table == null) {
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
        table.addPropertyChangeListener("folder", evt -> {
            clearFilters();
            setEnabled(table.getFolder() != null);
        });
        setEnabled(table.getFolder() != null);
    }

    protected abstract void clearFilters();

    /**
     * @return the engine
     */
    public GTDFreeEngine getEngine() {
        return engine;
    }

    /**
     * @param engine the engine to set
     */
    public void setEngine(GTDFreeEngine engine) {
        this.engine = engine;
    }

}
