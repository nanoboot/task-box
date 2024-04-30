/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *    
 *    This file is part of GTD-Box.
 *    
 *    GTD-Box is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *    
 *    GTD-Box is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *    
 *    You should have received a copy of the GNU General Public License
 *    along with GTD-Box.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.forks.org.gtdbox.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.nanoboot.forks.org.gtdbox.ApplicationHelper;
import org.nanoboot.forks.org.gtdbox.GTDBoxEngine;
import org.nanoboot.forks.org.gtdbox.model.Folder;
import org.nanoboot.forks.org.gtdbox.model.GTDModelAdapter;

/**
 * @author ikesan
 *
 */
public class FolderPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private FolderTree folderTree;
	private AbstractAction addFolderAction;
	private JTextField folderNameField;
	private AbstractAction renameFolderAction;
	private GTDBoxEngine engine;
	private AbstractAction closeFolderAction;
	private AbstractAction reopenFolderAction;
	private FoldingPanel foldingPanel;

	public FolderPanel() {
		initialize();
	}
	
	private void initialize() {
		setLayout(new GridBagLayout());
		
		folderTree= new FolderTree();
		folderTree.addPropertyChangeListener("selectedFolder", new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				
				folderNameField.setEnabled(folderTree.isFolderRenamePossible() || folderTree.isFolderAddPossible());
				
				getRenameFolderAction().setEnabled(folderTree.isFolderRenamePossible());
				getAddFolderAction().setEnabled(folderTree.isFolderAddPossible());
				getCloseFolderAction().setEnabled(getSelectedFolder()!=null && !getSelectedFolder().isBuildIn() && getSelectedFolder().getType() != Folder.FolderType.INBUCKET && getSelectedFolder().getType() != Folder.FolderType.QUEUE && !getSelectedFolder().isClosed() && getSelectedFolder().getOpenCount() == 0);
				getReopenFolderAction().setEnabled(getSelectedFolder()!=null && getSelectedFolder().isClosed());
		
				if (getSelectedFolder()!=null) {
					folderNameField.setText(getSelectedFolder().getName());
				}
			}
		
		});
		add(new JScrollPane(folderTree), new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		
		JPanel p= new JPanel();
		p.setLayout(new GridBagLayout());
		
		folderNameField= new JTextField();
		folderNameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getRenameFolderAction().isEnabled()) {
					getRenameFolderAction().actionPerformed(e);
				} else if (getAddFolderAction().isEnabled()) {
					getAddFolderAction().actionPerformed(e);
				}
			}
		});
		p.add(folderNameField,new GridBagConstraints(0,0,2,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		
		JButton b= new JButton(getRenameFolderAction());
		b.setMargin(new Insets(1,1,1,1));
		p.add(b,new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,0,4,0),0,0));

		b= new JButton(getAddFolderAction());
		b.setMargin(new Insets(1,1,1,1));
		p.add(b,new GridBagConstraints(1,1,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,0,4,0),0,0));
		
		b= new JButton(getCloseFolderAction());
		b.setMargin(new Insets(1,1,1,1));
		p.add(b,new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,4,0),0,0));

		b= new JButton(getReopenFolderAction());
		b.setMargin(new Insets(1,1,1,1));
		p.add(b,new GridBagConstraints(1,2,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,4,0),0,0));

		foldingPanel= new FoldingPanel();
		foldingPanel.addFold("Edit", p, true, false);
		add(foldingPanel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
	}

	public Folder getSelectedFolder() {
		return folderTree.getSelectedFolder();
	}

	private Action getAddFolderAction() {
		if (addFolderAction == null) {
			addFolderAction = new AbstractAction("Add",ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_add)) {
				
				private static final long serialVersionUID = 0L;
				
				public void actionPerformed(ActionEvent e) {
					folderTree.addFolder(folderNameField.getText());
				}
			};
			
		}
	
		return addFolderAction;
	}

	private Action getRenameFolderAction() {
		if (renameFolderAction == null) {
			renameFolderAction = new AbstractAction("Rename",ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_rename)) {
				private static final long serialVersionUID = 0L;
				public void actionPerformed(ActionEvent e) {
					getSelectedFolder().rename(folderNameField.getText());
				}
			};
			
		}
	
		return renameFolderAction;
	}

	private Action getCloseFolderAction() {
		if (closeFolderAction == null) {
			closeFolderAction = new AbstractAction("Close",ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_delete)) {
				private static final long serialVersionUID = 0L;
				public void actionPerformed(ActionEvent e) {
					if (getSelectedFolder().getOpenCount()==0) {
						getSelectedFolder().setClosed(true);
					}
				}
			};
			closeFolderAction.putValue(Action.SHORT_DESCRIPTION, "Closes list or project, which contains no open actions.");
		}
		return closeFolderAction;
	}

	private Action getReopenFolderAction() {
		if (reopenFolderAction == null) {
			reopenFolderAction = new AbstractAction("Reopen",ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_undelete)) {
				private static final long serialVersionUID = 0L;
				public void actionPerformed(ActionEvent e) {
					getSelectedFolder().setClosed(false);
				}
			};
			reopenFolderAction.putValue(Action.SHORT_DESCRIPTION, "Opens closed list.");
		}
		return reopenFolderAction;
	}

	/**
	 * @param engine the engine to set
	 */
	public void setEngine(GTDBoxEngine engine) {
		this.engine = engine;
		folderTree.setEngine(this.engine);
		engine.getGTDModel().addGTDModelListener(new GTDModelAdapter() {
			@Override
			public void elementModified(
                    org.nanoboot.forks.org.gtdbox.model.ActionEvent a) {
				if (a.getAction().getFolder()==getSelectedFolder() || (getSelectedFolder()!=null && a.getAction().getProject()!=null && getSelectedFolder().getId()==a.getAction().getProject())) {
					getCloseFolderAction().setEnabled(getSelectedFolder()!=null && !getSelectedFolder().isBuildIn() && getSelectedFolder().getType() != Folder.FolderType.INBUCKET && getSelectedFolder().getType() != Folder.FolderType.QUEUE && !getSelectedFolder().isClosed() && getSelectedFolder().getOpenCount() == 0);
					getReopenFolderAction().setEnabled(getSelectedFolder()!=null && getSelectedFolder().isClosed());
				}
			}
		});
	}

	public void setDefaultFoldersVisible(boolean b) {
		folderTree.setDefaultFoldersVisible(b);
	}
	
	public boolean isDefaultFoldersVisible() {
		return folderTree.isDefaultFoldersVisible();
	}
	
	public void setShowClosedFolders(boolean b) {
		folderTree.setShowClosedFolders(b);
	}

	public boolean isShowClosedFolders() {
		return folderTree.isShowClosedFolders();
	}

	public void setExpendedNodes(int[] ii) {
		folderTree.setExpendedNodes(ii);
	}
	public int[] getExpendedNodes() {
		return folderTree.getExpendedNodes();
	}
	public boolean[] getFoldingStates() {
		return foldingPanel.getFoldingStates();
	}
	public void setFoldingStates(boolean[] b) {
		foldingPanel.setFoldingStates(b);
	}

	public void setSelectedFolder(Folder f) {
		folderTree.setSelectedFolder(f);
	}
}
