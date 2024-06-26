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

package com.gtdfree.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ikesan
 *
 */
public class GTDFreeTestSuite extends TestSuite {
	public static Test suite() {
		return new GTDFreeTestSuite();
	}
	
	public GTDFreeTestSuite() {
		addTestSuite(GTDFreeEngineTest.class);
		addTestSuite(ModelTest.class);
		addTestSuite(SaveLoadTest.class);
	}
}
