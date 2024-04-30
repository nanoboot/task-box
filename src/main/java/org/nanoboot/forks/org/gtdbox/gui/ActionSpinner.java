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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.nanoboot.forks.org.gtdbox.ApplicationHelper;
import org.nanoboot.forks.org.gtdbox.model.Action;
import org.nanoboot.forks.org.gtdbox.model.Folder;
import org.nanoboot.forks.org.gtdbox.model.FolderEvent;
import org.nanoboot.forks.org.gtdbox.model.FolderListener;
import org.nanoboot.forks.org.gtdbox.model.ActionEvent;

/**
 * @author ikesan
 *
 */
public class ActionSpinner extends JPanel implements FolderListener {

	class FolderModel extends AbstractSpinnerModel {
		private int index;
		private int lastIndex;
		
		public void setValue(Object value) {
			int i= folder.indexOf((Action)value);
			if (i!=index) {
				index=i;
				fireStateChanged();
			}
		}
	
		public Object getValue() {
			if (folder==null || folder.size()==0) {
				return null;
			}
			if (index>=folder.size()) {
				index=folder.size()-1;
			}
			Action a= folder.get(index);
			if (!a.isOpen()) {
				a= (Action)getNextValue();
				if (a==null) {
					a= (Action)getPreviousValue();
				}
				index=lastIndex;
			}
			return a;
		}
	
		public Object getPreviousValue() {
			return getPreviousValue(index);
		}

		private Object getPreviousValue(int i) {
			if (folder==null || folder.size()==0 || i>=folder.size()-1) {
				return null;
			}
			Action a= folder.get(lastIndex=i+1);
			if (!a.isOpen()) {
				a= (Action)getPreviousValue(lastIndex);
			}
			return a;
		}
	
		public Object getNextValue() {
			return getNextValue(index);
		}
		private Object getNextValue(int i) {
			if (folder==null || folder.size()<=0 || i==0) {
				return null;
			}
			Action a=folder.get(lastIndex=i-1);
			if (!a.isOpen()) {
				a= (Action)getNextValue(lastIndex);
			}
			return a;
		}
		
		@Override
		public void fireStateChanged() {
			super.fireStateChanged();
		}

		public void reset() {
			index=0;
			fireStateChanged();
		}

		public void removed(Action action) {
			if (action == selectedAction) {
				Object a= getPreviousValue();
				if (a==null) {
					a= getNextValue();
				}
				index=lastIndex;
				fireStateChanged();
			} else {
				int i= folder.indexOf(selectedAction); {
					if (i<0) {
						reset();
					}
				}
			}
		}
	}
	
	private static final long serialVersionUID = -7868587669764300896L;
	private JLabel idLabel;
	private JTextArea descText;
	private Action selectedAction;
	private Folder folder;
	private JSpinner spinner;
	private FolderModel model;
	private boolean setting;

	public ActionSpinner() {
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		JPanel jp= new JPanel();
		jp.setLayout(new GridBagLayout());
		
		idLabel= new JLabel(Messages.getString("ActionPanel.ID-NA")); //$NON-NLS-1$
		Dimension d= new Dimension(75,21);
		idLabel.setPreferredSize(d);
		idLabel.setMinimumSize(d);
		jp.add(idLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		
		descText= new JTextArea();
		descText.setWrapStyleWord(true);
		descText.setLineWrap(true);
		descText.setMargin(new Insets(2,4,2,4));
		descText.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				if (setting || selectedAction==null) {
					return;
				}
				selectedAction.setDescription(descText.getText());
			}
			public void insertUpdate(DocumentEvent e) {
				if (setting || selectedAction==null) {
					return;
				}
				selectedAction.setDescription(descText.getText());
			}
			public void changedUpdate(DocumentEvent e) {
				if (setting || selectedAction==null) {
					return;
				}
				selectedAction.setDescription(descText.getText());
			}
		});
		JScrollPane sp= new JScrollPane(descText);
		jp.add(sp, new GridBagConstraints(1,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		
		spinner= new JSpinner();
		spinner.setEditor(jp);
		spinner.addChangeListener(new ChangeListener() {
		
			public void stateChanged(ChangeEvent e) {
				Action a= selectedAction;
				selectedAction= (Action)spinner.getValue();
				updateGUI();
				firePropertyChange("selectedAction", a, selectedAction);
			}
		
		});
		model= new FolderModel();
		spinner.setModel(model);
		add(spinner);

		updateGUI();
		//setEnabled(false);
	}

	/**
	 * @return the action
	 */
	public Action getSelectedAction() {
		return selectedAction;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		idLabel.setEnabled(enabled);
		descText.setEnabled(enabled);
		spinner.setEnabled(enabled && folder!=null && folder.size()>0);
		//dateText.setEnabled(enabled);
		//dateSelectorButton.setEnabled(enabled);
		//getDeleteAction().setEnabled(enabled);
		//getResolveAction().setEnabled(enabled);
	}

	private void updateGUI() {
		Action a=selectedAction;
		if (a==null) {
			idLabel.setText(Messages.getString("ActionPanel.ID")+Messages.getString("ActionPanel.NA")); //$NON-NLS-1$
			descText.setEnabled(false);
			descText.setEditable(false);
			descText.setText(ApplicationHelper.EMPTY_STRING);
		} else {
			setting=true;
			String s= Messages.getString("ActionPanel.ID")+a.getId(); //$NON-NLS-1$
			idLabel.setText(s);
			descText.setEnabled(true);
			descText.setEditable(true);
			s= a.getDescription()==null ? ApplicationHelper.EMPTY_STRING : a.getDescription();
			if (!s.equals(descText.getText())) {
				descText.setText(s);
				descText.setCaretPosition(0);
			}
			setting=false;
		}
	}
	
	public static void main(String[] args) {
		try {
			
			JFrame f= new JFrame();
			ActionSpinner ap= new ActionSpinner();
			ap.setEnabled(true);
			f.setContentPane(ap);
			f.setSize(300, 300);
			f.setVisible(true);
			
		} catch (Exception e) {
			e.toString();
		}
	}

	public void elementAdded(FolderEvent note) {
		//if (selectedAction==null) {
			model.reset();
		//}
	}

	public void elementModified(ActionEvent note) {
		if (model.getValue()==selectedAction) {
			updateGUI();
		} else {
			model.fireStateChanged();
		}
	}

	public void elementRemoved(FolderEvent note) {
		model.removed(note.getAction());
	}
	
	public void orderChanged(Folder f) {
		int i= folder.indexOf(selectedAction); {
			if (i<0) {
				model.reset();
			}
		}
	}

	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder f) {
		this.folder = f;
		folder.addFolderListener(this);
		model.reset();
	}

}
