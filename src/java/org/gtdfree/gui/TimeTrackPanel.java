/*
 *    Copyright (C) 2008 Igor Kriznar
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

package org.gtdfree.gui;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.gtdfree.GTDFreeEngine;

/**
 * @author ikesan
 *
 */
public class TimeTrackPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private GTDFreeEngine engine;
	//private JComboBox journalCombo;
	
	public TimeTrackPanel() {
		initialize();
	}

	private void initialize() {
		setLayout(new GridBagLayout());
		
		//journalCombo= new JComboBox();
		
	}

	/**
	 * @param engine the engine to set
	 */
	public void setEngine(GTDFreeEngine engine) {
		this.engine = engine;
	}

	/**
	 * @return the engine
	 */
	public GTDFreeEngine getEngine() {
		return engine;
	}
	
	

}
