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

import org.nanoboot.gtdfree.ApplicationHelper;
import org.nanoboot.gtdfree.GTDFreeEngine;
import org.nanoboot.gtdfree.GlobalProperties;
import org.nanoboot.gtdfree.gui.ActionTable.CellAction;
import org.nanoboot.gtdfree.model.Action;
import org.nanoboot.gtdfree.model.Folder;
import org.nanoboot.gtdfree.model.FolderEvent;
import org.nanoboot.gtdfree.model.FolderListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author ikesan
 */
public class InBasketPane extends JPanel {

    private static final long serialVersionUID = 3933752664249964846L;
    JTextArea ideaText;
    private GTDFreeEngine engine;
    private AbstractAction newNoteAction;
    private ActionTable noteTable;
    private Action selectedAction;
    private JLabel idLabel;
    private AbstractAction delAction;
    private AbstractAction clearAction;
    private boolean setting = false;

    private final FolderListener noteListener = new FolderListener() {

        public void elementRemoved(FolderEvent note) {
            //
        }

        public void elementModified(
                org.nanoboot.gtdfree.model.ActionEvent note) {
            if (!setting && getSelectedAction() != null && !getSelectedAction()
                    .getDescription().equals(ideaText.getText())) {
                setting = true;
                ideaText.setText(getSelectedAction().getDescription());
                setting = false;
            }
        }

        public void elementAdded(FolderEvent note) {
            //
        }

        public void orderChanged(Folder f) {
            //
        }

    };

    private AbstractAction cloneNoteAction;

    private AbstractAction doneNoteAction;

    private JSplitPane split;

    private boolean adding;

    public InBasketPane() {
        initialize();
    }

    /**
     * @return the selectedAction
     */
    public Action getSelectedAction() {
        return selectedAction;
    }

    /**
     * @param selectedAction the selectedAction to set
     */
    public void setSelectedAction(Action selectedAction) {
        if (adding) {
            return;
        }
        if (this.selectedAction == null
            && ideaText.getDocument().getLength() > 0 && !adding) {
            adding = true;
            getEngine().getGTDModel()
                    .createAction(noteTable.getFolder(), ideaText.getText());
            noteTable.setSelectedAction(selectedAction);
            adding = false;
        }
        this.selectedAction = selectedAction;
        if (selectedAction == null) {
            idLabel.setText(Messages.getString("ActionPanel.ID") + Messages
                    .getString("ActionPanel.NA"));
            ideaText.setText(ApplicationHelper.EMPTY_STRING);
            //getModifyAction().setEnabled(false);
            getCloneNoteAction().setEnabled(false);
            getDeleteAction().setEnabled(false);
            getDoneNoteAction().setEnabled(true);
            getNewNoteAction().setEnabled(false);
        } else {
            setting = true;
            idLabel.setText(
                    Messages.getString("ActionPanel.ID") + selectedAction
                            .getId());
            ideaText.setText(selectedAction.getDescription());
            ideaText.setCaretPosition(0);
            //getModifyAction().setEnabled(true);
            getCloneNoteAction().setEnabled(true);
            getDeleteAction().setEnabled(true);
            getDoneNoteAction().setEnabled(false);
            getNewNoteAction().setEnabled(true);
            setting = false;
        }
    }

    private void initialize() {

        split = new JSplitPane();
        split.setOrientation(JSplitPane.VERTICAL_SPLIT);
        split.setOneTouchExpandable(true);

        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        jp.setBorder(new TitledBorder(
                Messages.getString("InBasketPane.ActionTitle"))); //$NON-NLS-1$

        idLabel = new JLabel(Messages.getString("ActionPanel.ID") + Messages
                .getString("ActionPanel.NA"));
        jp.add(idLabel, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 4, 0, 4), 0, 0));

        JScrollPane jsp = new JScrollPane();
        ideaText = new JTextArea();
        ideaText.setLineWrap(true);
        ideaText.setWrapStyleWord(true);
        ideaText.setRows(3);
        ideaText.setMargin(new Insets(2, 4, 2, 4));
        ideaText.getInputMap()
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
        ideaText.getActionMap().put("Enter", new AbstractAction() {
            private static final long serialVersionUID = 1348070910974195411L;

            public void actionPerformed(ActionEvent e) {
                if (getDoneNoteAction().isEnabled()) {
                    getDoneNoteAction().actionPerformed(e);
                } else {
                    getNewNoteAction().actionPerformed(e);
                }
            }
        });
        ideaText.getInputMap().put(KeyStroke
                        .getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK),
                "NewLine");
        ideaText.getInputMap().put(KeyStroke
                        .getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
                "NewLine");
        ideaText.getActionMap().put("NewLine", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                ideaText.insert("\n", ideaText.getCaretPosition());
            }
        });
		/*ideaText.setDragEnabled(true);
		ideaText.setTransferHandler(new StringTransferHandler(){
			@Override
			protected String exportString() {
				return ideaText.getSelectedText();
			}
			@Override
			protected boolean importString(String s, TransferSupport support) {
				ideaText.insert(s, ideaText.viewToModel(support.getDropLocation().getDropPoint()));
				return true;
			}
		});*/

        ideaText.getDocument().addDocumentListener(new DocumentListener() {
            public void removeUpdate(DocumentEvent e) {
                updateSelectedAction();
            }

            public void insertUpdate(DocumentEvent e) {
                updateSelectedAction();
            }

            public void changedUpdate(DocumentEvent e) {
                updateSelectedAction();
            }
        });
        jsp.setViewportView(ideaText);
        jsp.setMinimumSize(ideaText.getPreferredScrollableViewportSize());
        jsp.setPreferredSize(ideaText.getPreferredScrollableViewportSize());
        jp.add(jsp, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 4, 0, 4), 0, 0));

        JPanel jp1 = new JPanel();
        jp1.setLayout(new GridBagLayout());
        JButton b = new JButton();
        b.setAction(getNewNoteAction());
        jp1.add(b, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(4, 4, 4, 4), 0, 0));

        b = new JButton();
        b.setAction(getDoneNoteAction());
        jp1.add(b, new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(4, 4, 4, 4), 0, 0));

        b = new JButton();
        b.setAction(getCloneNoteAction());
        jp1.add(b, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(4, 4, 4, 4), 0, 0));

        b = new JButton();
        b.setAction(getClearAction());
        jp1.add(b, new GridBagConstraints(3, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(4, 4, 4, 4), 0, 0));

        b = new JButton();
        b.setAction(getDeleteAction());
        jp1.add(b, new GridBagConstraints(4, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(4, 4, 4, 4), 0, 0));
        jp.add(jp1, new GridBagConstraints(0, 2, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        //add(jp,new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(4,4,4,4),0,0));

        split.setTopComponent(jp);

        noteTable = new ActionTable();
        noteTable.setShowQueueColumn(false);
        noteTable.setCellAction(CellAction.DELETE);
        noteTable.addPropertyChangeListener(
                ActionTable.SELECTED_ACTION_PROPERTY_NAME,
                evt -> setSelectedAction(noteTable.getSelectedAction()));

        jp = new JPanel();
        jp.setBorder(new TitledBorder(Messages.getString(
                "InBasketPane.InBasketTitle"))); //$NON-NLS-1$
        jp.setLayout(new BorderLayout());
        jsp = new JScrollPane();
        jsp.setViewportView(noteTable);
        jp.add(jsp);

        //add(jp,new GridBagConstraints(0,2,3,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(4,4,4,4),0,0));
        split.setBottomComponent(jp);
        setLayout(new BorderLayout());
        add(split);

        setSelectedAction(null);
    }

    private javax.swing.Action getDeleteAction() {
        if (delAction == null) {
            delAction = new AbstractAction(Messages.getString("delete"),
                    ApplicationHelper.getIcon(
                            ApplicationHelper.icon_name_large_delete)) {
                private static final long serialVersionUID = 0L;

                public void actionPerformed(ActionEvent e) {
                    if (selectedAction != null) {
                        selectedAction.setResolution(Action.Resolution.DELETED);
                    }
                }
            };
            delAction.putValue(AbstractAction.SHORT_DESCRIPTION,
                    Messages.getString("delete.Tooltip"));
            delAction.setEnabled(false);

        }

        return delAction;
    }

    private javax.swing.Action getClearAction() {
        if (clearAction == null) {
            clearAction =
                    new AbstractAction(Messages.getString("InBasketPane.Clear"),
                            ApplicationHelper.getIcon(
                                    ApplicationHelper.icon_name_large_clear)) { //$NON-NLS-1$
                        private static final long serialVersionUID =
                                -6062912427153528136L;

                        public void actionPerformed(ActionEvent e) {
                            ideaText.setText(ApplicationHelper.EMPTY_STRING);
                        }
                    };
            clearAction.putValue(AbstractAction.SHORT_DESCRIPTION,
                    Messages.getString(
                            "InBasketPane.Clear.Tooltip")); //$NON-NLS-1$
            clearAction.setEnabled(true);

        }

        return clearAction;
    }

    private void updateSelectedAction() {
        if (!setting && selectedAction != null && ideaText.getText() != null
            && !ideaText.getText().equals(selectedAction.getDescription())) {
            setting = true;
            selectedAction.setDescription(ideaText.getText());
            setting = false;
        }
    }

    private javax.swing.Action getNewNoteAction() {
        if (newNoteAction == null) {
            newNoteAction =
                    new AbstractAction(Messages.getString("InBasketPane.New"),
                            ApplicationHelper.getIcon(
                                    ApplicationHelper.icon_name_large_new)) { //$NON-NLS-1$
                        private static final long serialVersionUID = 1L;

                        public void actionPerformed(ActionEvent e) {
                            noteTable.getSelectionModel().clearSelection();
                            ideaText.setText(ApplicationHelper.EMPTY_STRING);
                        }

                    };
            newNoteAction.putValue(AbstractAction.SHORT_DESCRIPTION,
                    Messages.getString(
                            "InBasketPane.New.Tooltip")); //$NON-NLS-1$

        }

        return newNoteAction;
    }

    private javax.swing.Action getDoneNoteAction() {
        if (doneNoteAction == null) {
            doneNoteAction =
                    new AbstractAction(Messages.getString("InBasketPane.Add"),
                            ApplicationHelper.getIcon(
                                    ApplicationHelper.icon_name_large_add)) { //$NON-NLS-1$

                        private static final long serialVersionUID =
                                5246080843920948091L;

                        public void actionPerformed(ActionEvent e) {
                            if (ideaText.getText() == null
                                || ideaText.getText().length() == 0) {
                                return;
                            }
                            adding = true;
                            getEngine().getGTDModel()
                                    .createAction(noteTable.getFolder(),
                                            ideaText.getText());
                            adding = false;
                            noteTable.getSelectionModel().clearSelection();
                            ideaText.setText(ApplicationHelper.EMPTY_STRING);
                        }
                    };
            doneNoteAction.putValue(AbstractAction.SHORT_DESCRIPTION,
                    Messages.getString(
                            "InBasketPane.Add-Tooltip")); //$NON-NLS-1$
        }

        return doneNoteAction;
    }

    private javax.swing.Action getCloneNoteAction() {
        if (cloneNoteAction == null) {
            cloneNoteAction =
                    new AbstractAction(Messages.getString("InBasketPane.Clone"),
                            ApplicationHelper.getIcon(
                                    ApplicationHelper.icon_name_large_clone)) { //$NON-NLS-1$

                        private static final long serialVersionUID =
                                5246080843920948091L;

                        public void actionPerformed(ActionEvent e) {
                            getEngine().getGTDModel()
                                    .createAction(noteTable.getFolder(),
                                            ideaText.getText());
                            noteTable.getSelectionModel()
                                    .setSelectionInterval(0, 0);
                        }

                    };
            cloneNoteAction.putValue(AbstractAction.SHORT_DESCRIPTION,
                    Messages.getString(
                            "InBasketPane.Clone.Tooltip")); //$NON-NLS-1$

        }

        return cloneNoteAction;
    }

    public GTDFreeEngine getEngine() {
        return engine;
    }

    public void setEngine(GTDFreeEngine engine) {
        this.engine = engine;
        Folder folder = engine.getGTDModel().getInBucketFolder();
        noteTable.setEngine(engine);
        noteTable.setFolder(folder);

        folder.addFolderListener(noteListener);

    }

    public void store(GlobalProperties p) {
        p.putProperty("inbasket.dividerLocation", split.getDividerLocation());
    }

    public void restore(GlobalProperties p) {
        Integer i = p.getInteger("inbasket.dividerLocation");
        if (i != null) {
            split.setDividerLocation(i);
        }
    }

}
