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

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.gtdfree.ApplicationHelper;
import org.gtdfree.GTDFreeEngine;
import org.gtdfree.GlobalProperties;
import org.gtdfree.model.Action;
import org.gtdfree.model.Folder;
import org.gtdfree.model.FolderEvent;
import org.gtdfree.model.FolderListener;
import org.gtdfree.model.Priority;
import org.gtdfree.model.Action.Resolution;

import de.wannawork.jcalendar.JCalendarComboBox;

/**
 * @author ikesan
 *
 */
public class ActionPanel extends JPanel implements FolderListener {

	private static final long serialVersionUID = -7868587669764300896L;
	private JLabel idLabel;
	private JLabel createdLabel;
	private JTextArea descText;
	private JTextField urlText;
	private Action action;
	private JButton moveDownButton;
	private JButton moveUpButton;
	private boolean setting=false;
	private AbstractAction deleteAction;
	private AbstractAction resolveAction;
	//private DatePicker datePicker;
	private JCalendarComboBox datePicker;
	private ProjectsCombo projectCombo;
	private GTDFreeEngine engine;
	private AbstractAction reopenAction;
	private JLabel folderLabel;
	private AbstractAction queuedAction;
	private JToggleButton queueActionButton;
	private AbstractAction openURLAction;
	private JScrollPane descTextScroll;
	private PriorityPicker priorityPanel;
	private JButton selectNextButton;
	private JButton selectPreviousButton;
	private JButton reopenButton;
	private JPanel buttonsPanel;
	private JButton previousDayButton;
	private JButton nextDayButton;
	private JButton clearDateButton;
	private boolean reopenButtonVisible=false;

	public ActionPanel() {
		initialize(true);
	}

	public ActionPanel(boolean packHor) {
		initialize(packHor);
	}

	private void initialize(boolean packHorizontal) {
		
		setLayout(new GridBagLayout());

		int row=0;
		
		JPanel p=null;
		
		/*
		 * ID
		 */
		JLabel jl= new JLabel(Messages.getString("ActionPanel.ID")); //$NON-NLS-1$
		if (packHorizontal) {
			add(jl, new GridBagConstraints(0,row,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		} else {
			p= new JPanel();
			p.setLayout(new GridBagLayout());
			p.add(jl, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		}

		idLabel= new JLabel(Messages.getString("ActionPanel.NA")); //$NON-NLS-1$
		Dimension d= new Dimension(100,21);
		idLabel.setPreferredSize(d);
		idLabel.setMinimumSize(d);
		if (packHorizontal) {
			add(idLabel, new GridBagConstraints(1,row,2,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		} else {
			p.add(idLabel, new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		}

		/*
		 * Created
		 */
		jl= new JLabel(Messages.getString("ActionPanel.Created")); //$NON-NLS-1$
		if (packHorizontal) {
			add(jl, new GridBagConstraints(0,++row,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		} else {
			p.add(jl, new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		}

		createdLabel= new JLabel(Messages.getString("ActionPanel.NA")); //$NON-NLS-1$
		createdLabel.setPreferredSize(d);
		createdLabel.setMinimumSize(d);
		if (packHorizontal) {
			add(createdLabel, new GridBagConstraints(1,row,2,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		} else {
			p.add(createdLabel, new GridBagConstraints(3,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		}
		
		/*
		 * Folder
		 */
		jl= new JLabel(Messages.getString("ActionPanel.Folder")); //$NON-NLS-1$
		if (packHorizontal) {
			add(jl, new GridBagConstraints(0,++row,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		} else {
			p.add(jl, new GridBagConstraints(4,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		}

		folderLabel= new JLabel(Messages.getString("ActionPanel.NA")); //$NON-NLS-1$
		folderLabel.setPreferredSize(d);
		folderLabel.setMinimumSize(d);
		if (packHorizontal) {
			add(folderLabel, new GridBagConstraints(1,row,2,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		} else {
			p.add(folderLabel, new GridBagConstraints(5,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
			add(p, new GridBagConstraints(0,row,2,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		}

		/*
		 * Description
		 */
		descText= new JTextArea();
		descText.setWrapStyleWord(true);
		descText.setLineWrap(true);
		descText.setMargin(new Insets(2,4,2,4));
		descText.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "NewLine");
		descText.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "NewLine");
		descText.getActionMap().put("NewLine", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				descText.insert("\n", descText.getCaretPosition());
			}
		});
		descText.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				if (setting || action==null) {
					return;
				}
				action.setDescription(descText.getText());
			}
			public void insertUpdate(DocumentEvent e) {
				if (setting || action==null) {
					return;
				}
				action.setDescription(descText.getText());
			}
			public void changedUpdate(DocumentEvent e) {
				if (setting || action==null) {
					return;
				}
				action.setDescription(descText.getText());
			}
		});
		descTextScroll= new JScrollPane(descText);
		if (packHorizontal) {
			add(descTextScroll, new GridBagConstraints(0,++row,3,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(1,1,1,1),0,17));
		} else {
			add(descTextScroll, new GridBagConstraints(0,++row,2,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(1,1,1,1),0,17));
		}
		
		/*
		 * URL
		 */
		jl=new JLabel(Messages.getString("ActionPanel.URL"));
		if (packHorizontal) {
			add(jl, new GridBagConstraints(0,++row,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0)); //$NON-NLS-1$
		}
		urlText= new JTextField();
		urlText.getDocument().addDocumentListener(new DocumentListener() {
			private void update() {
				URL url=null;
				
				try {
					url= new URL(urlText.getText());
				} catch (Exception e) {
					//
				}
				if (url!=null) {
					urlText.setForeground(Color.black);
					if (action!=null) {
						setting=true;
						action.setUrl(url);
						setting=false;
					}
				} else {
					urlText.setForeground(Color.red);
				}
			}
			public void removeUpdate(DocumentEvent e) {
				update();
			}
		
			public void insertUpdate(DocumentEvent e) {
				update();
			}
		
			public void changedUpdate(DocumentEvent e) {
				update();
			}
		});
		if (packHorizontal) {
			add(urlText, new GridBagConstraints(1,row,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		}
		
		JButton b= new JButton();
		b.setMargin(new Insets(0,0,0,0));
		b.setAction(getOpenURLAction());
		if (packHorizontal) {
			add(b, new GridBagConstraints(2,row,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		} else {
			p= new JPanel();
			p.setLayout(new GridBagLayout());
			p.add(jl, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0)); //$NON-NLS-1$
			p.add(urlText, new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
			p.add(b, new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
			add(p, new GridBagConstraints(0,++row,2,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0));
		}


		/*
		 * Project
		 */
		jl= new JLabel(Messages.getString("ActionPanel.Project"));
		if (packHorizontal) {
			add(jl, new GridBagConstraints(0,++row,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0)); //$NON-NLS-1$
		} else {
			p= new JPanel();
			p.setLayout(new GridBagLayout());
			p.add(jl, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1,1,1,1),0,0)); //$NON-NLS-1$
		}
		
		projectCombo= new ProjectsCombo();
		projectCombo.addPropertyChangeListener("selectedProject", new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				if (setting || action==null) {
					return;
				}
				setting= true;
				if (projectCombo.getSelectedProject()!=null) {
					action.setProject(projectCombo.getSelectedProject().getId());
				} else {
					action.setProject(null);
				}
				setting=false;
			}
		
		});
		if (packHorizontal) {
			add(projectCombo, new GridBagConstraints(1,row,2,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		} else {
			p.add(projectCombo, new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		}
		
		/*
		 * Reminder
		 */
		if (packHorizontal) {
			add(new JLabel(Messages.getString("ActionPanel.Reminder")), new GridBagConstraints(0,++row,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0)); //$NON-NLS-1$
		} else {
			p.add(new JLabel(Messages.getString("ActionPanel.Reminder")), new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0)); //$NON-NLS-1$
		}
		
/*		datePicker= new DatePicker();
		datePicker.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
		datePicker.setMinimumSize(new Dimension(135,datePicker.getMinimumSize().height));
		datePicker.setPreferredSize(new Dimension(135,datePicker.getPreferredSize().height));
		datePicker.setShowNoneButton(true);
		datePicker.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				if (action!=null) {
					action.setRemind(datePicker.getDate());
				}
			}
		
		});
*/		
		JPanel pp= new JPanel();
		pp.setLayout(new GridBagLayout());
		
		datePicker= new JCalendarComboBox();
		datePicker.setMinimumSize(new Dimension(125,datePicker.getPreferredSize().height-3));
		datePicker.setPreferredSize(new Dimension(125,datePicker.getPreferredSize().height-3));
		d= new Dimension(datePicker.getPreferredSize().height,datePicker.getPreferredSize().height);
		datePicker.setDate(null);
		datePicker.addChangeListener(new ChangeListener() {
		
			@Override
			public void stateChanged(ChangeEvent e) {
				if (action!=null) {
					action.setRemind(datePicker.getDate());
				}
			}
		
		});
		pp.add(datePicker, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		
		
		previousDayButton= new JButton();
		previousDayButton.setMinimumSize(d);
		previousDayButton.setPreferredSize(d);
		previousDayButton.setMargin(new Insets(1,1,1,1));
		previousDayButton.setToolTipText("Select previous day");
		previousDayButton.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_previous));
		previousDayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (datePicker.getDate()==null) {
					datePicker.setDate(new Date());
				} else {
					Calendar cc= datePicker.getCalendar();
					cc.set(Calendar.DAY_OF_MONTH, cc.get(Calendar.DAY_OF_MONTH)-1);
					datePicker.setCalendar(cc);
				}
			}
		});
		pp.add(previousDayButton, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		
		nextDayButton= new JButton();
		nextDayButton.setMinimumSize(d);
		nextDayButton.setPreferredSize(d);
		nextDayButton.setMargin(new Insets(1,1,1,1));
		nextDayButton.setToolTipText("Select next day");
		nextDayButton.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_next));
		nextDayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (datePicker.getDate()==null) {
					datePicker.setDate(new Date());
				} else {
					Calendar cc= datePicker.getCalendar();
					cc.set(Calendar.DAY_OF_MONTH, cc.get(Calendar.DAY_OF_MONTH)+1);
					datePicker.setCalendar(cc);
				}
			}
		});
		pp.add(nextDayButton, new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		clearDateButton= new JButton();
		clearDateButton.setMinimumSize(d);
		clearDateButton.setPreferredSize(d);
		clearDateButton.setMargin(new Insets(1,1,1,1));
		clearDateButton.setToolTipText("Clear date selection");
		clearDateButton.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_clear));
		clearDateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				datePicker.setDate(null);
			}
		});
		pp.add(clearDateButton, new GridBagConstraints(3,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		if (packHorizontal) {
			add(pp, new GridBagConstraints(1,row,2,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		} else {
			p.add(pp, new GridBagConstraints(3,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		}

		/*
		 * Priority
		 */
		jl= new JLabel("Priority:");
		if (packHorizontal) {
			add(jl, new GridBagConstraints(0,++row,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		} else {
			p.add(jl, new GridBagConstraints(4,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		}
		
		priorityPanel= new PriorityPicker();
		priorityPanel.addPropertyChangeListener("priority",new PropertyChangeListener() {
		
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (setting || action==null) {
					return;
				}
				setting= true;
				action.setPriority(priorityPanel.getPriority());
				setting=false;
			}
		});
		if (packHorizontal) {
			add(priorityPanel, new GridBagConstraints(1,row,2,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(4,1,4,1),0,0));
		} else {
			p.add(priorityPanel, new GridBagConstraints(5,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(4,1,4,1),0,0));
			add(p, new GridBagConstraints(0,++row,2,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		}

		/*
		 * Navigate and move buttons
		 */
		
		JPanel jp= new JPanel();
		jp.setLayout(new GridBagLayout());
		
		int buttonIndex=0;
		
		jp.add(new JLabel("Go to:"), new GridBagConstraints(buttonIndex++,0,1,1,1,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(1,1,1,4),0,0));

		selectPreviousButton= new JButton();
		selectPreviousButton.setHideActionText(true);
		selectPreviousButton.setEnabled(false);
		selectPreviousButton.setMargin(new Insets(1,1,1,1));
		jp.add(selectPreviousButton, new GridBagConstraints(buttonIndex++,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));

		selectNextButton= new JButton();
		selectNextButton.setHideActionText(true);
		selectNextButton.setEnabled(false);
		selectNextButton.setMargin(new Insets(1,1,1,1));
		jp.add(selectNextButton, new GridBagConstraints(buttonIndex++,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));

		
		
		jp.add(new JLabel("Move:"), new GridBagConstraints(buttonIndex++,0,1,1,1,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(1,4,1,4),0,0));

		moveDownButton= new JButton();
		moveDownButton.setHideActionText(true);
		moveDownButton.setEnabled(false);
		moveDownButton.setMargin(new Insets(1,1,1,1));
		jp.add(moveDownButton, new GridBagConstraints(buttonIndex++,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));

		moveUpButton= new JButton();
		moveUpButton.setHideActionText(true);
		moveUpButton.setEnabled(false);
		moveUpButton.setMargin(new Insets(1,1,1,1));
		jp.add(moveUpButton, new GridBagConstraints(buttonIndex++,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));

		if (packHorizontal) {
			add(jp, new GridBagConstraints(0,++row,3,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(1,1,1,1),0,0));
		} else {
			add(jp, new GridBagConstraints(0,++row,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(1,1,1,1),0,0));
		}

		
		
		/*
		 * buttons at bottom
		 */
		buttonsPanel= new JPanel();
		buttonsPanel.setLayout(new GridBagLayout());
		
		buttonIndex=0;
		
		queueActionButton= new JToggleButton();
		queueActionButton.setAction(getQueuedAction());
		queueActionButton.setRolloverEnabled(false);
		queueActionButton.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_queue_off));
		queueActionButton.setSelectedIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_queue_on));
		queueActionButton.setDisabledSelectedIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_queue_off));
		buttonsPanel.add(queueActionButton,new GridBagConstraints(buttonIndex++,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));

		b = new JButton();
		b.setAction(getResolveAction());
		buttonsPanel.add(b,new GridBagConstraints(buttonIndex++,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		//jp.add(b);
		
		b = new JButton();
		b.setAction(getDeleteAction());
		buttonsPanel.add(b,new GridBagConstraints(buttonIndex++,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		//jp.add(b);

		reopenButton = new JButton();
		reopenButton.setAction(getReopenAction());
		reopenButton.setVisible(false);
		buttonsPanel.add(reopenButton,new GridBagConstraints(buttonIndex++,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(1,1,1,1),0,0));
		//jp.add(b);

		//d= new Dimension(150,jp.getPreferredSize().height);
		//jp.setPreferredSize(d);
		//jp.setMinimumSize(jp.getPreferredSize());

		//d= new Dimension(150,getPreferredSize().height);
		//setPreferredSize(jp.getPreferredSize());
		//setMinimumSize(jp.getMinimumSize());

		if (packHorizontal) {
			add(buttonsPanel, new GridBagConstraints(0,++row,3,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(1,1,1,1),0,0));
		} else {
			add(buttonsPanel, new GridBagConstraints(1,row,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(1,1,1,1),0,0));
		}


		setEnabled(false);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(buttonsPanel.getPreferredSize().width,super.getPreferredSize().height);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(buttonsPanel.getMinimumSize().width,super.getMinimumSize().height);
	}

	private javax.swing.Action getOpenURLAction() {
		if (openURLAction==null) {
			openURLAction= new AbstractAction(ApplicationHelper.EMPTY_STRING,ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_browser)) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					if (action!=null && action.getUrl()!=null) {
						try {
							Desktop.getDesktop().browse(action.getUrl().toURI());
						} catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane.showConfirmDialog(ActionPanel.this, Messages.getString("ActionPanel.LinkError.1")+action.getUrl()+Messages.getString("ActionPanel.LinkError.2")+e1.getMessage(), Messages.getString("ActionPanel.LinkError.Title"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
				}
			
			};
			openURLAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("open.Tooltip")); //$NON-NLS-1$
			openURLAction.setEnabled(false);
		}
		return openURLAction;

	}

	private javax.swing.Action getDeleteAction() {
		if (deleteAction==null) {
			deleteAction= new AbstractAction(Messages.getString("delete"),ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_delete)) { //$NON-NLS-1$
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					action.setResolution(Resolution.DELETED);
				}
			
			};
			deleteAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("delete.Tooltip")); //$NON-NLS-1$
			deleteAction.setEnabled(false);
		}
		return deleteAction;
	}

	private javax.swing.Action getResolveAction() {
		if (resolveAction==null) {
			resolveAction= new AbstractAction(Messages.getString("resolve"),ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_resolve)) { //$NON-NLS-1$
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					action.setResolution(Resolution.RESOLVED);
				}
			
			};
			resolveAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("resolve.Tooltip")); //$NON-NLS-1$
			resolveAction.setEnabled(false);
		}
		return resolveAction;
	}

	private javax.swing.Action getQueuedAction() {
		if (queuedAction==null) {
			queuedAction= new AbstractAction(Messages.getString("queue")) { //$NON-NLS-1$
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					action.setQueued(queueActionButton.isSelected());
				}
			
			};
			queuedAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("queue.Tooltip")); //$NON-NLS-1$
			queuedAction.setEnabled(false);
		}
		return queuedAction;
	}

	private javax.swing.Action getReopenAction() {
		if (reopenAction==null) {
			reopenAction= new AbstractAction(Messages.getString("reopen"),ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_undelete)) { //$NON-NLS-1$
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					action.setResolution(Resolution.OPEN);
				}
			
			};
			reopenAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("reopen.Tooltip")); //$NON-NLS-1$
			reopenAction.setEnabled(false);
		}
		return reopenAction;
	}

	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(Action action) {
		if (this.action!=null) {
			this.action.getFolder().removeFolderListener(this);
		}
		this.action = action;
		updateGUI();
		setEnabled(action!=null);
		if (this.action!=null) {
			this.action.getFolder().addFolderListener(this);
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		//super.setEnabled(enabled);
		descText.setEnabled(enabled);
		urlText.setEnabled(enabled);
		datePicker.setEnabled(enabled);
		previousDayButton.setEnabled(enabled);
		nextDayButton.setEnabled(enabled);
		clearDateButton.setEnabled(enabled);
		projectCombo.setEnabled(enabled);
		priorityPanel.setEnabled(enabled);
		getReopenAction().setEnabled(enabled && (action!=null && !action.isOpen()));
		getResolveAction().setEnabled(enabled && (action!=null && action.isOpen()));
		getDeleteAction().setEnabled(enabled && (action!=null && action.isOpen()));
		getQueuedAction().setEnabled(enabled && (action!=null && action.isOpen()));
		getOpenURLAction().setEnabled(enabled && (action!=null && action.getUrl()!=null));
		/*if (moveDownButton.getAction()!=null) {
			moveDownButton.getAction().setEnabled(enabled && action!=null);
		}
		if (moveUpButton.getAction()!=null) {
			moveUpButton.getAction().setEnabled(enabled && action!=null);
		}*/
	}

	private void updateGUI() {
		Action a=action;
		/*if (moveDownButton.getAction()!=null) {
			moveDownButton.getAction().setEnabled(action!=null);
		}
		if (moveUpButton.getAction()!=null) {
			moveUpButton.getAction().setEnabled(action!=null);
		}*/
		getReopenAction().setEnabled(action!=null && !action.isOpen());
		getResolveAction().setEnabled(action!=null && action.isOpen());
		getDeleteAction().setEnabled(action!=null && action.isOpen());
		getQueuedAction().setEnabled(action!=null && action.isOpen());
		getOpenURLAction().setEnabled(action!=null && action.getUrl()!=null);
		if (a==null) {
			idLabel.setText(Messages.getString("ActionPanel.NA")); //$NON-NLS-1$
			createdLabel.setText(Messages.getString("ActionPanel.NA")); //$NON-NLS-1$
			folderLabel.setText(Messages.getString("ActionPanel.NA")); //$NON-NLS-1$
			descText.setText(ApplicationHelper.EMPTY_STRING); //$NON-NLS-1$
			urlText.setText(ApplicationHelper.EMPTY_STRING); //$NON-NLS-1$
			projectCombo.setSelectedProject(null);
			datePicker.setDate(null);
			priorityPanel.setPriority(Priority.None);
		} else {
			String s= String.valueOf(a.getId()); //$NON-NLS-1$
			idLabel.setText(s);
			s= ApplicationHelper.toISODateTimeString(a.getCreated()); //$NON-NLS-1$
			createdLabel.setText(s);
			folderLabel.setText(a.getFolder().getName()); //$NON-NLS-1$
			if (!setting) {
				setting =true;
				if (queueActionButton.isSelected()!=a.isQueued()) {
					queueActionButton.setSelected(a.isQueued());
				}
				s= a.getDescription()==null ? ApplicationHelper.EMPTY_STRING : a.getDescription(); //$NON-NLS-1$
				if (!s.equals(descText.getText())) {
					descText.setText(s);
					descText.setCaretPosition(0);
				}
				if (a.getUrl()!=null) {
					urlText.setText(a.getUrl().toString());	
				} else {
					urlText.setText(ApplicationHelper.EMPTY_STRING); //$NON-NLS-1$
				}
				if (a.getRemind()!=null) {
					if (!a.getRemind().equals(datePicker.getDate())) {
						datePicker.setDate(a.getRemind());
					}
				} else {
					if (datePicker.getDate()!=null) {
						datePicker.setDate(null);
					}
				}
				if (action.getProject()!=null) {
					projectCombo.setSelectedProject(action.getFolder().getParent().getProject(action.getProject()));
				} else {
					projectCombo.setSelectedProject(null);
				}
				priorityPanel.setPriority(action.getPriority());
				setting=false;
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			
			JFrame f= new JFrame();
			ActionPanel ap= new ActionPanel(false);
			ap.setEnabled(true);
			f.setContentPane(ap);
			f.setSize(300, 300);
			f.setVisible(true);
			
		} catch (Exception e) {
			e.toString();
		}
	}

	public void elementAdded(FolderEvent note) {
		//
	}

	public void elementModified(org.gtdfree.model.ActionEvent note) {
		if (note.getAction()==action) {
			updateGUI();
		}
	}

	public void elementRemoved(FolderEvent note) {
		//
	}
	
	public void orderChanged(Folder f) {
		//
	}

	/**
	 * @return the gtdModel
	 */
	public GTDFreeEngine getEngine() {
		return engine;
	}

	/**
	 * @param gtdModel the gtdModel to set
	 */
	public void setEngine(GTDFreeEngine e) {
		this.engine = e;
		projectCombo.setGTDModel(engine.getGTDModel());
		engine.getGlobalProperties().addPropertyChangeListener(GlobalProperties.SHOW_ALL_ACTIONS, new PropertyChangeListener() {
		
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				reopenButton.setVisible(engine.getGlobalProperties().getBoolean(GlobalProperties.SHOW_ALL_ACTIONS));
			}
		});
		reopenButton.setVisible(reopenButtonVisible || engine.getGlobalProperties().getBoolean(GlobalProperties.SHOW_ALL_ACTIONS));
	}
	
	public void setDescriptionTextMinimumHeight(int min) {
		descTextScroll.setMinimumSize(new Dimension(descTextScroll.getMinimumSize().width,min));
	}
	public int getDescriptionTextMinimumHeight() {
		return descTextScroll.getMinimumSize().height;
	}
	
	public void putActions(ActionMap actions) {
		moveDownButton.setAction(actions.get(ActionTable.MOVE_DOWN));
		moveUpButton.setAction(actions.get(ActionTable.MOVE_UP));
		selectNextButton.setAction(actions.get(ActionTable.SELECT_NEXT));
		selectPreviousButton.setAction(actions.get(ActionTable.SELECT_PREVIOUS));
	}
	
	public void setReopenButtonVisible(boolean b) {
		reopenButtonVisible=b;
		reopenButton.setVisible(reopenButtonVisible || engine.getGlobalProperties().getBoolean(GlobalProperties.SHOW_ALL_ACTIONS));
	}
	
	public boolean isReopenButtonVisible() {
		return reopenButtonVisible;
	}
}
