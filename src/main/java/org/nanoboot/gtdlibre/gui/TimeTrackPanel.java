/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *
 *    This file is part of GTD-Libre.
 *
 *    GTD-Libre is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    GTD-Libre is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with GTD-Libre.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.gtdlibre.gui;

import org.nanoboot.gtdlibre.GTDLibreEngine;

import javax.swing.JPanel;
import java.awt.GridBagLayout;

/**
 * @author ikesan
 */
public class TimeTrackPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private GTDLibreEngine engine;
    //private JComboBox journalCombo;

    public TimeTrackPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new GridBagLayout());

        //journalCombo= new JComboBox();

    }

    /**
     * @return the engine
     */
    public GTDLibreEngine getEngine() {
        return engine;
    }

    /**
     * @param engine the engine to set
     */
    public void setEngine(GTDLibreEngine engine) {
        this.engine = engine;
    }

}
