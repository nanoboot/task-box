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

package org.gtdfree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.gtdfree.gui.StateMachine;
import org.gtdfree.journal.JournalModel;
import org.gtdfree.model.ActionEvent;
import org.gtdfree.model.Folder;
import org.gtdfree.model.FolderEvent;
import org.gtdfree.model.GTDModel;
import org.gtdfree.model.GTDModelListener;
import org.gtdfree.model.GTDModel.DataHeader;


public class GTDFreeEngine {
	
	class SaveThread extends Thread implements GTDModelListener {
		boolean destroyed= false;
		public SaveThread() {
		}
		@Override
		public void run() {
			while (!destroyed) {
				if (save) {
					try {
						save=false;
						save();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				synchronized (this) {
					try {
						wait(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		public void elementAdded(FolderEvent a) {
			if (!a.isRecycled()) {
				notifySave();
			}
		}
		public void elementModified(ActionEvent a) {
			if (!a.isRecycled()) {
				notifySave();
			}
		}
		public void elementRemoved(FolderEvent a) {
			if (!a.isRecycled()) {
				notifySave();
			}
		}
		public void folderAdded(Folder folder) {
			notifySave();
		}
		public void folderModified(FolderEvent folder) {
			if (!folder.isRecycled()) {
				notifySave();
			}
		}
		public void folderRemoved(Folder folder) {
			notifySave();
		}
		public void orderChanged(Folder f) {
			notifySave();
		}
		public synchronized void notifySave() {
			//Thread.dumpStack();
			save=true;
			notify();
		}
		public synchronized void stopSave() {
			destroyed=true;
			notify();
		}
	}
	
	volatile private GTDModel gtdModel;
	private SaveThread saveThread;
	private volatile boolean save= false;
	private boolean autoSave=true;
	private int i=0;
	private Properties configuration;
	private GlobalProperties globalProperties;

	private JournalModel journalModel;
	private StateMachine stateMachine;
	private ActionMap actionMap;
	private boolean aborting= false;
	
	public GTDFreeEngine() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError, MalformedURLException {
	}
	
	public Properties getConfiguration() {
		if (configuration==null) {
			configuration= ApplicationHelper.loadConfiguration();
		}
		return configuration;
	}
	
	public GTDModel getGTDModel() {
		if (gtdModel == null) {
			gtdModel = new GTDModel();
			
			System.out.println("Using "+getDataFile().getAbsolutePath());

			if (getDataFile().exists()) {
				try {
					gtdModel.load(getDataFile());
				} catch (Exception e) {
					e.printStackTrace();
					
					DataHeader[] dh= findBackupFiles();
					
					handleFailedLoad(gtdModel, dh, 0, e);
					
				}
			} else {
				DataHeader[] dh= findBackupFiles();
				if (dh!=null && dh.length>0) {
					handleFailedLoad(gtdModel, dh, 0, new FileNotFoundException("Missing main data file: '"+getDataFile().getAbsolutePath()+"'."));
				}
			}
			setAutoSave(getGlobalProperties().getBoolean(GlobalProperties.AUTO_SAVE , true));
		}

		return gtdModel;
	}
	
	private boolean handleFailedLoad(GTDModel m, DataHeader[] dh, int i, Exception e) {
		if (dh==null || dh.length<=i) {
			// handle when there is no backup file
			
			int option= JOptionPane.showConfirmDialog(
					null, 
					"Your data was NOT properly loaded into the GTD-Free because of file loading error: \n\""+
					e.toString().replace(". ", ".\n")+
					"\"\n\nDo you want to abort the application with no changes made to data files, make backup of data folder and manually corect problem?" +
					"\n\n'No' will continue starting the application with partially loaded data." +
					"\n\nTip: your data files are stored in the folder: '"+ApplicationHelper.getDataFolder().getAbsolutePath()+
					"'.\nMain data file '"+
					getDataFile().getName()+
					"' has ten backup copies, they are named as" +
					"\ngtd-free-data.backupN.xml where 'N' stands for numbers from 0 to 9." ,
					"GTD-Free - Error Loading Data File", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (option== JOptionPane.YES_OPTION) {
				autoSave=false;
				if (saveThread!=null) {
					saveThread.stopSave();
				}
				aborting=true;
				System.exit(0);
				return false;
			} 

			return true;

		}
			
		int option= JOptionPane.showConfirmDialog(
				null, 
				"Your data was NOT properly loaded into the GTD-Free because of file loading error: \n\""+
				e.toString().replace(". ", ".\n")+
				"\"\n\nDo you want to try to load backup file '" +
				dh[i].getFile().getAbsolutePath() +
				"',\nsaved on " + ApplicationHelper.toISODateTimeString(dh[i].getModified()) + "?" +
				"\n\n'No' will continue starting the application with partially loaded data." +
				"\n\n'Cancel' will abort the application with no changes made to data files. You will have chance" +
				"\nto make backup of data folder and manually corect problem." +
				"\n\nTip: your data files are stored in the folder: '"+ApplicationHelper.getDataFolder().getAbsolutePath()+
				"'.\nMain data file '"+
				getDataFile().getName()+
				"' has ten backup copies, they are named as" +
				"\ngtd-free-data.backupN.xml where 'N' stands for numbers from 0 to 9." ,
				"GTD-Free - Error Loading Data File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
		if (option==JOptionPane.YES_OPTION) {
			try {
				System.out.println("Loading "+dh[i].getFile().getAbsolutePath());
				gtdModel.load(dh[i].getFile());
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return handleFailedLoad(m, dh, ++i, ex);
			}	
		} else if (option == JOptionPane.NO_OPTION) {
			return true;
		} else {
			//if (option== JOptionPane.CANCEL_OPTION) {
			autoSave=false;
			if (saveThread!=null) {
				saveThread.stopSave();
			}
			aborting=true;
			System.exit(0);
			return false;
		} 
		
	}
	
	private DataHeader[] findBackupFiles() {
		List<DataHeader> l= new ArrayList<DataHeader>(10);
		
		for (int i=0; i<10; i++) {
			
			File f= ApplicationHelper.createBackupDataFile(i);
			if (f.exists() && f.length()>0) {
				try {
					DataHeader dh= new DataHeader(f);
					l.add(dh);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
		}
		
		Collections.sort(l, new Comparator<DataHeader>() {
		
			@Override
			public int compare(DataHeader o1, DataHeader o2) {
				if (o1.getModified()==null || o2.getModified()==null) {
					return 0;
				}
				return (int)(o2.getModified().getTime()-o1.getModified().getTime());
			}
		});
		
		for (DataHeader dataHeader : l) {
			System.out.println(dataHeader);
		}
		
		return l.toArray(new DataHeader[l.size()]);
		
		
	}
	
	public JournalModel getJournalModel() {
		if (journalModel == null) {
			journalModel = new JournalModel();
			/*if (file==null) {
				file=ApplicationHelper.getDefaultFile();
			}
			System.out.println("Using "+file);
			//File f= new File(file.getAbsoluteFile()+".bak");
			if (file.exists()) {
				try {
					gtdModel.load(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				} catch (FactoryConfigurationError e) {
					e.printStackTrace();
				}
			}*/
		}

		return journalModel;
	}

	public void save() throws IOException, XMLStreamException, FactoryConfigurationError {
		if (isAborting()) {
			return;
		}
		
		File backup = ApplicationHelper.createBackupDataFile(i++%10);
		if (backup.exists() && !backup.delete()) {
			throw new IOException("Failed to remove backup file '"+backup.getAbsolutePath()+"'.");
		}
		if (getDataFile().exists() && !getDataFile().renameTo(backup)) {
			throw new IOException("Failed to make backup copy file '"+backup.getAbsolutePath()+"'.");
		}
		gtdModel.store(getDataFile());
		System.out.println("Saved to "+getDataFile().getAbsolutePath());
	}

	public void emergencySave() {
		File save = new File(ApplicationHelper.getDataFolder(),ApplicationHelper.SHUTDOWN_EMERGENCY_BACKUP_DATA_FILE_NAME);
		if (save.exists() && !save.delete()) {
			new IOException("Failed to remove emergency backup file '"+save.getAbsolutePath()+"'.").toString();
		}
		try {
			gtdModel.store(save);
			System.out.println("Shutdown emergency backup saved to "+save.getAbsolutePath());
		} catch (Exception e) {
			System.out.println("Failed to make shutdown emergency backup to "+save.getAbsolutePath());
			System.out.println(e.toString());
		}
	}
	
	/**
	 * @return the file
	 */
	public File getDataFile() {
		return ApplicationHelper.getDataFile();
	}
	/**
	 * @return the file
	 */
	public File getDataFolder() {
		return ApplicationHelper.getDataFolder();
	}

	/**
	 * @return the autoSave
	 */
	public boolean isAutoSave() {
		return autoSave;
	}

	/**
	 * @param autoSave the autoSave to set
	 */
	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
		if (autoSave) {
			if (saveThread!=null) {
				saveThread.stopSave();
				getGTDModel().removeGTDModelListener(saveThread);
			}
			saveThread=new SaveThread();
			saveThread.start();
			getGTDModel().addGTDModelListener(saveThread);
		} else {
			if (saveThread!=null) {
				saveThread.stopSave();
			} else {
				saveThread=new SaveThread();
				saveThread.stopSave();
				getGTDModel().addGTDModelListener(saveThread);
			}
		}
	}
	
	public GlobalProperties getGlobalProperties() {
		if (globalProperties==null) {
			globalProperties= new GlobalProperties();
			
			File f= new File(getDataFolder(),ApplicationHelper.OPTIONS_FILE_NAME);
			if (f.exists()) {
				BufferedReader r=null;
				
				try {
					r= new BufferedReader(new FileReader(f));
					
					globalProperties.load(r);
					
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (r!=null) {
							r.close();
						}
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
		return globalProperties;
	}
	
	/**
	 * 
	 * @param terminal if <code>true</code> then close procedure can not be aborted 
	 * @return <code>true</code> if close can proceed, <code>false</code> if close should be aborted 
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	public boolean close(final boolean terminal) throws IOException, XMLStreamException, FactoryConfigurationError {
		
		if (aborting) {
			return true;
		}
		
		if (isAutoSave()) {
			setAutoSave(false);
			if (isSaveTriggered()) {
				save();
			}
		} else if (isSaveTriggered()) {
			
			if (terminal) {
				emergencySave();
			} else {
			
				int option = JOptionPane.showConfirmDialog(
								null, 
								"Do you want to save changes before closing?\nChanges will permamantly lost if \"No\" is pressed.", 
								"GTD-Free Closing!", 
								JOptionPane.YES_NO_CANCEL_OPTION, 
								JOptionPane.WARNING_MESSAGE);
				
				if (option == JOptionPane.OK_OPTION) {
					setAutoSave(false);
					save();
				} else if (option == JOptionPane.CANCEL_OPTION) {
					return false;
				}
			}
		}
		
		File f= new File(getDataFolder(),ApplicationHelper.OPTIONS_FILE_NAME);
		BufferedWriter w=null;
		try {
			w= new BufferedWriter(new FileWriter(f));
			globalProperties.store(w);	
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (w!=null) {
					w.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
		
		return true;
		
	}

	private boolean isSaveTriggered() {
		return save;
	}

	public StateMachine getStateMachine() {
		if (stateMachine == null) {
			stateMachine = new StateMachine();
		}
		return stateMachine;
	}
	
	public ActionMap getActionMap() {
		if (actionMap == null) {
			actionMap = new ActionMap();
			
		}
		return actionMap;
	}
	
	public boolean isAborting() {
		return aborting;
	}

}
