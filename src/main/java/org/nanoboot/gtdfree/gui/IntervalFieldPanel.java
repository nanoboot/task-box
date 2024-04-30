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

import org.nanoboot.gtdfree.journal.Interval;
import org.nanoboot.gtdfree.journal.JournalEntry;
import org.nanoboot.gtdfree.journal.JournalEntryEvent;
import org.nanoboot.gtdfree.journal.JournalEntryListener;
import org.nanoboot.gtdfree.journal.JournalTools;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ikesan
 */
public class IntervalFieldPanel extends JPanel implements JournalEntryListener {

    private static final long serialVersionUID = 1L;
    private final List<IntervalField> fields = new ArrayList<>();
    //private JPanel strech= new JPanel();
    private JournalEntry entry;

    public IntervalFieldPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new GridBagLayout());

    }

    public void addInterval(Interval i) {

        entry.addInterval(i);

        IntervalField f =
                new IntervalField(this, entry, entry.getIntervalCount() - 1);

        fields.add(f);

        add(f, new GridBagConstraints(0, fields.size() - 1, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        checkLast();

        if (getParent() != null && getParent().getParent() != null) {
            getParent().getParent().validate();
        }
    }

    public void removeField(IntervalField f) {
        fields.remove(f);
        remove(f);
        f.release();

        checkLast();

        validate();
    }

    public void setEntry(JournalEntry entry) {
        if (entry == this.entry) {
            return;
        }

        if (this.entry != null) {
            this.entry.removeJournalEntryListener(this);
            removeAll();

            for (IntervalField f : fields) {
                f.release();
            }

            fields.clear();
        }

        this.entry = entry;

        if (this.entry != null) {
            this.entry.addJournalEntryListener(this);
            removeAll();

            Interval[] ii = entry.getIntervals();

            if (ii == null || ii.length == 0) {

                int i = JournalTools.minutesOfDay();
                addInterval(new Interval(i, i));

            } else {

                for (int i = 0; i < ii.length; i++) {
                    IntervalField f = new IntervalField(this, entry, i);
                    fields.add(f);
                    add(f, new GridBagConstraints(0, i, 1, 1, 1, 0,
                            GridBagConstraints.CENTER, GridBagConstraints.NONE,
                            new Insets(0, 0, 0, 0), 0, 0));
                }
            }
            checkLast();
        }
        validate();
    }

    private void checkLast() {
        fields.get(0).setRemoveEnabled(fields.size() > 1);

        setPreferredSize(new Dimension(fields.get(0).getPreferredSize().width,
                fields.get(0).getPreferredSize().height * fields.size()));
        setMinimumSize(getPreferredSize());
    }

    @Override
    public void journalEntryChanged(JournalEntryEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void journalEntryIntervalAdded(JournalEntryEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void journalEntryIntervalRemoved(JournalEntryEvent e) {
        // TODO Auto-generated method stub

    }
}
