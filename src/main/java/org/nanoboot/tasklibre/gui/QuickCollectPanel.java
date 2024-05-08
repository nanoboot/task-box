/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *
 *    This file is part of Task Libre.
 *
 *    Task Libre is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Task Libre is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Task Libre.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.tasklibre.gui;

import org.nanoboot.tasklibre.ApplicationHelper;
import org.nanoboot.tasklibre.TaskLibreEngine;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author ikesan
 */
public class QuickCollectPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    JTextArea ideaText;
    private TaskLibreEngine engine;
    private AbstractAction clearAction;
    private AbstractAction doneNoteAction;

    public QuickCollectPanel() {
        initialize();
    }

    private void initialize() {

        setLayout(new GridBagLayout());

        int col = 0;
        add(new JLabel("Quick Collect:"),
                new GridBagConstraints(col++, 0, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(2, 4, 2, 0), 0, 0));

        JScrollPane jsp = new JScrollPane();
        ideaText = new JTextArea();
        ideaText.setLineWrap(true);
        ideaText.setWrapStyleWord(true);
        ideaText.setRows(1);
        ideaText.setMargin(new Insets(2, 4, 2, 4));
        ideaText.getInputMap()
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
        ideaText.getActionMap().put("Enter", new AbstractAction() {
            private static final long serialVersionUID = 1348070910974195411L;

            public void actionPerformed(ActionEvent e) {
                if (getDoneNoteAction().isEnabled()) {
                    getDoneNoteAction().actionPerformed(e);
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
        jsp.setViewportView(ideaText);
        jsp.setMinimumSize(ideaText.getPreferredScrollableViewportSize());
        jsp.setPreferredSize(ideaText.getPreferredScrollableViewportSize());
        add(jsp, new GridBagConstraints(col++, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 4, 2, 0), 0, 0));

        JButton b = new JButton();
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setAction(getDoneNoteAction());
        add(b, new GridBagConstraints(col++, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(2, 4, 2, 0), 0, 0));

        b = new JButton();
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setAction(getClearAction());
        add(b, new GridBagConstraints(col++, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(2, 4, 2, 4), 0, 0));
    }

    private javax.swing.Action getClearAction() {
        if (clearAction == null) {
            clearAction =
                    new AbstractAction(Messages.getString("InBasketPane.Clear"),
                            ApplicationHelper.getIcon(
                                    ApplicationHelper.icon_name_large_clear)) { //$NON-NLS-1$

                        private static final long serialVersionUID = 1L;

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

    private javax.swing.Action getDoneNoteAction() {
        if (doneNoteAction == null) {
            doneNoteAction =
                    new AbstractAction(Messages.getString("InBasketPane.Add"),
                            ApplicationHelper.getIcon(
                                    ApplicationHelper.icon_name_large_add)) { //$NON-NLS-1$

                        private static final long serialVersionUID = 1L;

                        public void actionPerformed(ActionEvent e) {
                            if (ideaText.getText() == null
                                || ideaText.getText().length() == 0) {
                                return;
                            }
                            getEngine().getGTDModel().createAction(
                                    engine.getGTDModel().getInBucketFolder(),
                                    ideaText.getText());
                            ideaText.setText(ApplicationHelper.EMPTY_STRING);
                        }
                    };
            doneNoteAction.putValue(AbstractAction.SHORT_DESCRIPTION,
                    Messages.getString(
                            "InBasketPane.Add-Tooltip")); //$NON-NLS-1$
        }

        return doneNoteAction;
    }

    public TaskLibreEngine getEngine() {
        return engine;
    }

    public void setEngine(TaskLibreEngine engine) {
        this.engine = engine;
    }

}
