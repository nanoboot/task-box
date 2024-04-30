/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *
 *    This file is part of GTD-Libre.
 *
 *    GTD-Libre is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    GTD-Libre is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with GTD-Libre.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.gtdlibre.gui;

import org.nanoboot.gtdlibre.ApplicationHelper;
import org.nanoboot.gtdlibre.journal.Interval;
import org.nanoboot.gtdlibre.journal.JournalEntry;
import org.nanoboot.gtdlibre.journal.JournalEntryEvent;
import org.nanoboot.gtdlibre.journal.JournalEntryListener;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ikesan
 */
public class IntervalField extends JPanel implements JournalEntryListener {

    private static final long serialVersionUID = 1L;
    private static final DateFormat format = new SimpleDateFormat("HH:mm");
    private JournalEntry entry;
    private Interval interval;
    private int index;
    private JTextField field;
    private boolean setting;
    private IntervalFieldPanel parent;
    private boolean removeEnabled = true;
    private JButton removeButton;
    private Dimension minFieldSize;

    public IntervalField() {
    }

    public IntervalField(IntervalFieldPanel parent, JournalEntry entry,
            int index) {
        initialize(parent, entry, index);
    }

    private void initialize(IntervalFieldPanel parent, JournalEntry entry,
            int index) {
        this.entry = entry;
        this.index = index;
        this.parent = parent;

        setLayout(new GridBagLayout());

        field = new JTextField();
        field.setInputVerifier(new InputVerifier() {

            @Override
            public boolean verify(JComponent input) {
                String s = ((JTextField) input).getText();
                return toInterval(s) != null;
            }
        });
        field.addActionListener(e -> {
            setting = true;
            updateInterval(toInterval(field.getText()));
            setting = false;
        });

        add(field, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        entry.addJournalEntryListener(this);

        JButton b = new JButton();
        b.setIcon(ApplicationHelper
                .getIcon(ApplicationHelper.icon_name_small_add));
        b.setMargin(new Insets(0, 0, 0, 0));
        b.addActionListener(e -> {
            int i = interval.getEnd();
            IntervalField.this.parent.addInterval(new Interval(i, i + 60));
        });
        add(b, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 11, 0, 0), 0, 0));

        removeButton = new JButton();
        removeButton.setIcon(ApplicationHelper
                .getIcon(ApplicationHelper.icon_name_small_remove));
        removeButton.setMargin(new Insets(0, 0, 0, 0));
        removeButton.addActionListener(
                e -> IntervalField.this.parent.removeField(IntervalField.this));
        add(removeButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        updateInterval(entry.getInterval(index));
    }

    @Override
    public void journalEntryIntervalRemoved(JournalEntryEvent e) {
        if (e.getIndex() == IntervalField.this.index) {
            release();
        } else if (e.getIndex() < IntervalField.this.index) {
            IntervalField.this.index--;
        }

    }

    @Override
    public void journalEntryIntervalAdded(JournalEntryEvent e) {
        //

    }

    @Override
    public void journalEntryChanged(JournalEntryEvent e) {
        if (e.getIndex() == IntervalField.this.index) {
            updateInterval((Interval) e.getNewValue());
        }
    }

    public void release() {

        entry.removeJournalEntryListener(this);
        entry = null;
        interval = null;
    }

    private String toString(Interval i) {
        StringBuilder sb = new StringBuilder(16);
        sb.append(format.format(new Date(i.getStart() * 60000)));
        sb.append(" - ");
        sb.append(format.format(new Date(i.getEnd() * 60000)));
        return sb.toString();
    }

    private Interval toInterval(String s) {
        if (s.length() != 13) {
            return null;
        }

        String s1 = s.substring(0, 2);
        Integer t1 = Integer.parseInt(s1);
        t1 *= 60;
        s1 = s.substring(3, 5);
        t1 += Integer.parseInt(s1);

        s1 = s.substring(8, 10);
        Integer t2 = Integer.parseInt(s1);
        t2 *= 60;
        s1 = s.substring(11, 13);
        t2 += Integer.parseInt(s1);

        return new Interval(t1, t2);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void updateInterval(Interval i) {
        if (i == null || i.equals(interval)) {
            return;
        }
        Interval old = interval;
        interval = i;
        if (i != entry.getInterval(index)) {
            entry.setInterval(index, i);
        }

        if (!setting) {
            field.setText(toString(i));
        }

        if (minFieldSize == null) {
            minFieldSize = new Dimension(field.getPreferredSize().width,
                    removeButton.getPreferredSize().height);
            field.setMinimumSize(minFieldSize);
            field.setPreferredSize(minFieldSize);
            validate();
        }

        firePropertyChange("interval", old, i);
    }

    public boolean isRemoveEnabled() {
        return removeEnabled;
    }

    public void setRemoveEnabled(boolean removeEnabled) {
        this.removeEnabled = removeEnabled;
        removeButton.setEnabled(removeEnabled);
    }

}
