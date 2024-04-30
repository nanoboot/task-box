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

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;

/**
 * @author ikesan
 */
public class TimeInputPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String TIME_OF_DAY = "timeOfDay";
    private JComboBox hour;
    private JComboBox minutes;
    private int timeOfDay;
    public TimeInputPanel() {
        initialize();
    }

    public static void main(String[] args) {
        TimeInputPanel p = new TimeInputPanel();
        p.addPropertyChangeListener(TIME_OF_DAY,
                evt -> System.out.println(evt.getNewValue()));

        JFrame f = new JFrame();
        f.setContentPane(p);
        f.pack();
        f.setVisible(true);
    }

    private void initialize() {
        setLayout(new GridBagLayout());

        hour = new JComboBox(
                new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
                        29});
        hour.setEditable(false);
        hour.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateTime();
            }
        });

        minutes = new JComboBox(
                new Integer[] {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55});
        minutes.setEditable(false);
        minutes.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateTime();
            }
        });

        add(hour, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        add(new JLabel(":"), new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        add(minutes, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
    }

    private void updateTime() {
        setTimeOfDay(60 * (Integer) hour.getSelectedItem() + (Integer) minutes
                .getSelectedItem());
    }

    /**
     * @return the timeOfDay
     */
    public int getTimeOfDay() {
        return timeOfDay;
    }

    /**
     * @param timeOfDay the timeOfDay to set
     */
    public void setTimeOfDay(int timeOfDay) {
        if (this.timeOfDay == timeOfDay) {
            return;
        }
        int old = this.timeOfDay;
        this.timeOfDay = timeOfDay;
        hour.setSelectedItem(timeOfDay / 60);
        minutes.setSelectedItem(timeOfDay % 60);
        firePropertyChange(TIME_OF_DAY, old, timeOfDay);
    }
}
