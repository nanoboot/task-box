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

import org.nanoboot.forks.de.wannawork.jcalendar.JCalendarComboBox;
import org.nanoboot.forks.org.taskbox.ApplicationHelper;
import org.nanoboot.forks.org.taskbox.GTDBoxEngine;
import org.nanoboot.forks.org.taskbox.journal.JournalEntry;
import org.nanoboot.forks.org.taskbox.journal.JournalTools;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ikesan
 */
public class JournalPane extends JPanel {

    private static final long serialVersionUID = 1L;
    private JPanel entriesPanel;
    private long day;
    private Date date;
    private GTDBoxEngine engine;
    private JCalendarComboBox datePicker;
    private boolean setting = false;
    private AbstractAction newJournalEntryAction;

    public JournalPane() {
        initialize();
    }

    private void initialize() {

        setLayout(new GridBagLayout());

        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());

        JButton b = new JButton("<<");
        b.addActionListener(e -> setDay(day - 7));
        b.setToolTipText("Previous week");
        jp.add(b, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        b = new JButton("<");
        b.addActionListener(e -> setDay(day - 1));
        b.setToolTipText("Previous day");
        jp.add(b, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        datePicker = new JCalendarComboBox();
        datePicker.setDateFormat(new SimpleDateFormat("EEE, d MMM yyyy"));
        //datePicker.setFieldEditable(false);
        //datePicker.setShowTodayButton(true);
        //datePicker.setStripTime(true);
        //datePicker.setShowNoneButton(false);
        datePicker.addChangeListener(e -> {
            setting = true;
            //System.out.println("PICKER "+datePicker.getDate().getTime()+" "+((Date)evt.getNewValue()).getTime());
            setDate(datePicker.getDate());
            setting = false;
        });
        jp.add(datePicker, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        b = new JButton(">");
        b.addActionListener(e -> setDay(day + 1));
        b.setToolTipText("Next day");
        jp.add(b, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        b = new JButton(">>");
        b.addActionListener(e -> setDay(day + 7));
        b.setToolTipText("Next week");
        jp.add(b, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        add(jp, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

        entriesPanel = new JPanel();
        entriesPanel.setLayout(new GridBagLayout());
        add(new JScrollPane(entriesPanel),
                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
                        GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

        jp = new JPanel();
        jp.setLayout(new GridBagLayout());

        b = new JButton(getNewJournalEntryAction());
        jp.add(b, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        add(jp, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

    }

    private void addNewjournalEntry() {
        JournalEntry en = engine.getJournalModel().addEntry(day);
        addEntry(en, true);
    }

    private void addEntry(JournalEntry en, boolean last) {
        JournalEntryPanel ep = new JournalEntryPanel(en);
        if (entriesPanel.getComponentCount() > 1 && last) {
            entriesPanel.remove(entriesPanel.getComponentCount() - 1);
        }
        entriesPanel.add(ep,
                new GridBagConstraints(0, entriesPanel.getComponentCount(), 1,
                        1, 1, 0, GridBagConstraints.CENTER,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
                        0, 0));
        if (last) {
            entriesPanel.add(new JPanel(),
                    new GridBagConstraints(0, entriesPanel.getComponentCount(),
                            1, 1, 1, 1, GridBagConstraints.CENTER,
                            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,
                            0));
            validate();
        }
    }

    private Action getNewJournalEntryAction() {
        if (newJournalEntryAction == null) {
            newJournalEntryAction = new AbstractAction("New Journal Entry") {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    addNewjournalEntry();
                }
            };
            newJournalEntryAction.putValue(Action.LARGE_ICON_KEY,
                    ApplicationHelper
                            .getIcon(ApplicationHelper.icon_name_large_add));
        }

        return newJournalEntryAction;
    }

    public void setEngine(GTDBoxEngine engine) {
        this.engine = engine;
        setDay(JournalTools.today());

    }

    public void setDate(Date date) {
        setDay(JournalTools.toDay(date.getTime()));
    }

    public void setDay(long day) {
        if (this.day == day) {
            return;
        }
        this.day = day;

        date = JournalTools.toDate(day);

        //System.out.println("SET "+day+" "+date.getTime()+" "+datePicker.getDate().getTime());

        if (!setting) {
            datePicker.setDate(date);
        }

        entriesPanel.removeAll();

        JournalEntry[] entries = engine.getJournalModel().getEntries(day);

        if (entries != null) {
            for (int i = 0; i < entries.length; i++) {
                addEntry(entries[i], i == entries.length - 1);
            }
        }
        //entriesPanel.invalidate();
        entriesPanel.validate();
        entriesPanel.repaint();
    }
}
