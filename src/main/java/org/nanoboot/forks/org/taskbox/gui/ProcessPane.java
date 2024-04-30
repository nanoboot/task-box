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

package org.nanoboot.forks.org.taskbox.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import org.nanoboot.forks.org.taskbox.ApplicationHelper;
import org.nanoboot.forks.org.taskbox.GTDBoxEngine;
import org.nanoboot.forks.org.taskbox.GlobalProperties;
import org.nanoboot.forks.org.taskbox.model.Folder;
import org.nanoboot.forks.org.taskbox.model.FolderEvent;
import org.nanoboot.forks.org.taskbox.model.GTDModelAdapter;

/**
 * @author ikesan
 *
 */
public class ProcessPane extends JSplitPane {

	private static final long serialVersionUID = 1L;

	private GTDBoxEngine engine;
	private ActionTable actionTable;
	private Action resolveAction;
	private Action deleteAction;
	private AbstractAction moveAction;
	private ActionPanel actionPanel;
	private FolderPanel folders;
	private ActionSpinner actionSpinner;

	private JSplitPane split;

	private JLabel leftLabel;


	/**
	 * @return the engine
	 */
	public GTDBoxEngine getEngine() {
		return engine;
	}

	public ProcessPane() {
		initialize();
	}

	private void initialize() {
		
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		setDividerLocation(75);
		
		JPanel jp= new JPanel();
		jp.setLayout(new GridBagLayout());
		
		JPanel jpp= new JPanel();
		jpp.setLayout(new GridBagLayout());
		jpp.setBorder(new TitledBorder("In-Bucket"));
		actionSpinner= new ActionSpinner();
		actionSpinner.addPropertyChangeListener("selectedAction", evt -> {
			boolean b= actionSpinner.getSelectedAction()!=null;
			getDeleteAction().setEnabled(b);
			getResolveAction().setEnabled(b);

			b= b && folders.getSelectedFolder()!=null;
			getMoveAction().setEnabled(b);
		});
		
		jpp.add(actionSpinner,new GridBagConstraints(0,0,3,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(4,4,0,4),0,0));
		//jpp.setMinimumSize(new Dimension(100,actionSpinner.getRowHeight()+41));

		leftLabel= new JLabel();
		jpp.add(leftLabel,new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(4,4,4,4),0,0));

		JButton b = new JButton(getResolveAction());
		jpp.add(b,new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(4,4,4,4),0,0));
		
		b = new JButton(getDeleteAction());
		jpp.add(b,new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,4,4,4),0,0));

		b = new JButton(getMoveAction());
		jpp.add(b,new GridBagConstraints(2,1,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(4,4,4,4),0,0));

		jp.add(jpp,new GridBagConstraints(0,0,3,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(4,4,0,4),0,0));
		jp.setMinimumSize(new Dimension(200,100));
		jp.setPreferredSize(new Dimension(200,100));
		
		setLeftComponent(jp);
		
		split= new JSplitPane();
		split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		split.setResizeWeight(0);
		
		jp= new JPanel();
		jp.setBorder(new TitledBorder("Lists"));
		jp.setLayout(new GridBagLayout());
		
		folders= new FolderPanel();
		folders.setDefaultFoldersVisible(false);
		folders.addPropertyChangeListener("selectedFolder", evt -> {
			actionTable.setFolder(folders.getSelectedFolder());
			getMoveAction().setEnabled(actionSpinner.getSelectedAction()!=null && folders.getSelectedFolder()!=null);
		});
		jp.add(folders,new GridBagConstraints(0,0,2,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(4,4,4,4),0,0));
		
		split.setLeftComponent(jp);
		
		
		jp = new JPanel();
		jp.setLayout(new GridBagLayout());
		
		actionPanel= new ActionPanel(false);
		actionPanel.setBorder(new TitledBorder("Selected Action"));
		actionPanel.setDescriptionTextMinimumHeight(48);
		jp.add(actionPanel,new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,4,0,0),0,0));

		actionTable= new ActionTable();
		actionTable.setMoveEnabled(true);
		actionTable.addPropertyChangeListener("selectedAction",
				evt -> actionPanel.setAction(actionTable.getSelectedAction()));
		JScrollPane jsp = new JScrollPane(actionTable);
		jp.add(jsp,new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(4,4,4,4),0,0));
		
		split.setRightComponent(jp);
		split.setDividerLocation(210);
		split.setPreferredSize(new Dimension(200,200));
		split.setMinimumSize(new Dimension(200,200));
		
		setRightComponent(split);
		setDividerLocation(140);
		//packLayout();
		
		actionPanel.putActions(actionTable.getActionMap());
		
	}

	public void packLayout() {
		setDividerLocation((int)((JSplitPane)getRightComponent()).getLeftComponent().getMinimumSize().getHeight()+68);
	}

	private Action getMoveAction() {
		if (moveAction == null) {
			moveAction = new AbstractAction("Move to Selected List",ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_move)) {
				private static final long serialVersionUID = -8908528493980828208L;

				public void actionPerformed(ActionEvent e) {
					org.nanoboot.forks.org.taskbox.model.Action n= actionSpinner.getSelectedAction();
					Folder f= folders.getSelectedFolder();
					if (n!=null && f!=null) {
						engine.getGTDModel().moveAction(n, f);
						/*if (noteTable.getRowCount()>0) {
							noteTable.getSelectionModel().setSelectionInterval(0, 0);
						}*/
					}
				}
			
			};
			moveAction.setEnabled(false);
		}

		return moveAction;
	}

	private Action getDeleteAction() {
		if (deleteAction == null) {
			deleteAction = new AbstractAction("Delete",ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_delete)) {
				private static final long serialVersionUID = 5483273497818329289L;

				public void actionPerformed(ActionEvent e) {
					org.nanoboot.forks.org.taskbox.model.Action n= actionSpinner.getSelectedAction();
					if (n!=null) {
						n.setResolution(org.nanoboot.forks.org.taskbox.model.Action.Resolution.DELETED);
/*						if (noteTable.getRowCount()>0) {
							noteTable.getSelectionModel().setSelectionInterval(0, 0);
						}*/
					}
				}
			
			};
			deleteAction.setEnabled(false);
		}

		return deleteAction;
	}

	private Action getResolveAction() {
		if (resolveAction == null) {
			resolveAction = new AbstractAction("Resolve",ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_resolve)){
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					org.nanoboot.forks.org.taskbox.model.Action n= actionSpinner.getSelectedAction();
					if (n!=null) {
						n.setResolution(org.nanoboot.forks.org.taskbox.model.Action.Resolution.RESOLVED);
						/*if (noteTable.getRowCount()>0) {
							noteTable.getSelectionModel().setSelectionInterval(0, 0);
						}*/
					}
				}
			};
			resolveAction.setEnabled(false);
		}

		return resolveAction;
	}

	public void setEngine(GTDBoxEngine engine) {
		this.engine=engine;
		actionSpinner.setFolder(engine.getGTDModel().getInBucketFolder());
		folders.setEngine(getEngine());
		actionPanel.setEngine(engine);
		actionTable.setEngine(engine);
		
		engine.getGTDModel().addGTDModelListener(new GTDModelAdapter() {
		
			@Override
			public void elementRemoved(FolderEvent a) {
				checkSelection();
				if (a.getFolder().isInBucket()) {
					checkLeft();
				}
			}
		
			@Override
			public void elementAdded(FolderEvent a) {
				checkSelection();
				if (a.getFolder().isInBucket()) {
					checkLeft();
				}
			}
			
			@Override
			public void elementModified(
                    org.nanoboot.forks.org.taskbox.model.ActionEvent a) {
				if (a.getAction().getFolder().isInBucket()) {
					checkLeft();
				}
			}
		});
		
		checkSelection();
		checkLeft();
	}
	
	private void checkSelection() {
		/*if (actionSpinner.getSelectedAction()==null && actionSpinner.getRowCount()>0) {
			actionSpinner.getSelectionModel().setSelectionInterval(0, 0);
		}*/
		if (actionTable.getSelectedAction()==null && actionTable.getRowCount()>0) {
			actionTable.getSelectionModel().setSelectionInterval(0, 0);
		}
	}
	
	private void checkLeft() {
		leftLabel.setText("Items to process: "+engine.getGTDModel().getInBucketFolder().getOpenCount());
	}

	public void store(GlobalProperties p) {
		p.putProperty("process.dividerLocation1",getDividerLocation());
		p.putProperty("process.dividerLocation2",split.getDividerLocation());
		p.putProperty("process.tree.openNodes",folders.getExpendedNodes());
		p.putProperty("organize.tree.foldingStates", folders.getFoldingStates());
	}

	public void restore(GlobalProperties p) {
		Integer i= p.getInteger("process.dividerLocation1");
		if (i!=null) {
			setDividerLocation(i);
		}
		i= p.getInteger("process.dividerLocation2");
		if (i!=null) {
			split.setDividerLocation(i);
		}
		int[] ii= p.getIntegerArray("process.tree.openNodes");
		if (ii!=null) {
			folders.setExpendedNodes(ii);
		}
		boolean[] bb= p.getBooleanArray("organize.tree.foldingStates");
		if (bb!=null) {
			folders.setFoldingStates(bb);
		}
	}
}
