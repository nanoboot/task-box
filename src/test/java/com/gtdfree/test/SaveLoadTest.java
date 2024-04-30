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

import java.io.File;

import junit.framework.TestCase;

import org.gtdfree.model.Action;
import org.gtdfree.model.Folder;
import org.gtdfree.model.GTDModel;
import org.gtdfree.model.Project;
import org.gtdfree.model.Folder.FolderType;

/**
 * @author ikesan
 *
 */
public class SaveLoadTest extends TestCase {
	
	File outFile1= new File("out1.xml");
	
	public void testSaveLoadSave() {
		
		try {
			
			GTDModel m1= new GTDModel();
			
			int size=m1.size();
			
			Folder f1= m1.createFolder("F1", FolderType.ACTION);
			
			assertNotNull(f1);
			assertNotNull(m1.folders());
			assertEquals(size+1, m1.size());
			assertEquals(size+1, m1.folders().length);
			
			Folder f2= m1.createFolder("F2", FolderType.ACTION);
			f2.setClosed(true);
			
			assertNotNull(f2);
			assertNotNull(m1.folders());
			assertTrue(f2.isClosed());
			assertEquals(size+2, m1.size());
			assertEquals(size+2, m1.folders().length);

			Action a1= m1.createAction(f1, "A1");
			
			assertNotNull(a1);
			assertEquals(a1, m1.getAction(a1.getId()));
			assertEquals(1, f1.getOpenCount());
			assertEquals(1, f1.size());
			assertEquals(a1, f1.get(0));
			
			m1.store(outFile1);
			
			GTDModel m2= new GTDModel();
			
			m2.load(outFile1);
			
			assertEquals(m1.size(), m2.size());
			Folder[] ff1= m1.folders();
			Folder[] ff2= m2.folders();
			
			assertEquals(ff1.length, ff2.length);
			
			for (int i = 0; i < ff1.length; i++) {
				assertEquals(ff1[i].size(), ff2[i].size());
				assertEquals(ff1[i].getName(), ff2[i].getName());
				assertEquals(ff1[i].getId(), ff2[i].getId());
				assertEquals(ff1[i].getType(), ff2[i].getType());
				assertEquals(ff1[i].isClosed(), ff2[i].isClosed());
				Action[] aa1= ff1[i].actions();
				Action[] aa2= ff2[i].actions();
				assertEquals(aa1.length, aa1.length);
				for (int j = 0; j < aa2.length; j++) {
					assertEquals(aa1[j].getId(), aa2[j].getId());
					assertEquals(aa1[j].getDescription(), aa2[j].getDescription());
					assertEquals(aa1[j].getCreated(), aa2[j].getCreated());
				}
			}

			Project p1= (Project)m1.createFolder("P1", FolderType.PROJECT);
			
			assertNotNull(p1);
			assertNotNull(m1.projects());
			assertEquals(1, m1.projects().length);
			assertEquals(p1, m1.projects()[0]);
			
			Action a2= m1.createAction(f1, "A2");
			
			assertNotNull(a2);
			
			a2.setProject(p1.getId());
			
			assertEquals(p1.getId(),(int)a2.getProject());

			m1.store(outFile1);
			
			m2.load(outFile1);
			
			assertEquals(m1.size(), m2.size());
			ff1= m1.folders();
			ff2= m2.folders();
			
			assertEquals(ff1.length, ff2.length);
			
			for (int i = 0; i < ff1.length; i++) {
				assertEquals(ff1[i].size(), ff2[i].size());
				assertEquals(ff1[i].getName(), ff2[i].getName());
				assertEquals(ff1[i].getId(), ff2[i].getId());
				assertEquals(ff1[i].getType(), ff2[i].getType());
				assertEquals(ff1[i].isClosed(), ff2[i].isClosed());
				Action[] aa1= ff1[i].actions();
				Action[] aa2= ff2[i].actions();
				assertEquals(aa1.length, aa1.length);
				for (int j = 0; j < aa2.length; j++) {
					assertEquals(aa1[j].getId(), aa2[j].getId());
					assertEquals(aa1[j].getDescription(), aa2[j].getDescription());
					assertEquals(aa1[j].getCreated(), aa2[j].getCreated());
				}
			}
			
			Project[] pp1= m1.projects();
			Project[] pp2= m2.projects();
			
			assertEquals(pp1.length, pp2.length);
			
			for (int i = 0; i < pp1.length; i++) {
				assertEquals(pp1[i].size(), pp2[i].size());
				assertEquals(pp1[i].getName(), pp2[i].getName());
				assertEquals(pp1[i].getId(), pp2[i].getId());
				assertEquals(pp1[i].getType(), pp2[i].getType());
				Action[] aa1= pp1[i].actions();
				Action[] aa2= pp2[i].actions();
				assertEquals(aa1.length, aa1.length);
				for (int j = 0; j < aa2.length; j++) {
					assertEquals(aa1[j].getId(), aa2[j].getId());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (outFile1.exists()) {
			outFile1.delete();
		}
	}
	
	/*public void testOldData() {
		
		try {
			
			GTDModel m= new GTDModel();
			
			m.importFile(new File("./src/test/resources/old-data.xml"));
			
			assertTrue(m.size()>3);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}*/

}
