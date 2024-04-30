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

import org.nanoboot.gtdlibre.journal.JournalTools;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author ikesan
 */
public class TimeField extends JPanel {

    public static final String PROPERTY_TIME_OF_DAY = "timeOfDay";
    private static final long serialVersionUID = 1L;
    private int timeOfDay = -1;
    private JButton hourButton;
    private Border buttonBorder;
    private Border emptyBorder;
    private JButton minuteButton;
    private JWindow hourPicker;
    private JWindow minutePicker;
    public TimeField() {
        initialize();
    }

    public static void main(String[] args) {
        TimeField tf = new TimeField();
        tf.addPropertyChangeListener(PROPERTY_TIME_OF_DAY,
                evt -> System.out.println(evt.getNewValue()));

        JFrame f = new JFrame();
        f.setContentPane(tf);
        f.pack();
        f.setVisible(true);
    }

    private void initialize() {
        hourButton = new JButton();
        minuteButton = new JButton();

        hourButton.setOpaque(false);
        minuteButton.setOpaque(false);

        hourButton.setMargin(new Insets(1, 1, 1, 1));
        minuteButton.setMargin(new Insets(1, 1, 1, 1));

        buttonBorder = hourButton.getBorder();
        emptyBorder = new Border() {

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y,
                    int width,
                    int height) {
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return buttonBorder.getBorderInsets(c);
            }
        };

        hourButton.setBorder(emptyBorder);
        minuteButton.setBorder(emptyBorder);

        hourButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hourButton.setBorder(buttonBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hourButton.setBorder(emptyBorder);
            }
        });
        minuteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                minuteButton.setBorder(buttonBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                minuteButton.setBorder(emptyBorder);
            }
        });

        hourButton.addActionListener(e -> {
            if (minutePicker != null) {
                minutePicker.setVisible(false);
            }
            if (getHourPicker().isVisible()) {
                getHourPicker().setVisible(false);
            } else {
                getHourPicker().setLocation(hourButton.getLocationOnScreen().x,
                        hourButton.getLocationOnScreen().y + hourButton
                                .getHeight());
                getHourPicker().setVisible(true);
            }
        });

        minuteButton.addActionListener(e -> {
            if (hourPicker != null) {
                hourPicker.setVisible(false);
            }
            if (getMinutePicker().isVisible()) {
                getMinutePicker().setVisible(false);
            } else {
                getMinutePicker()
                        .setLocation(minuteButton.getLocationOnScreen().x,
                                minuteButton.getLocationOnScreen().y
                                + minuteButton.getHeight());
                getMinutePicker().setVisible(true);
            }
        });

        setLayout(new GridBagLayout());
        add(hourButton, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        add(new JLabel(":"), new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        add(minuteButton, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        setTimeOfDay(0);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                hidePickers();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                hidePickers();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                hidePickers();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hidePickers();
            }
        });

    }

    protected JWindow getHourPicker() {
        if (hourPicker == null) {
            hourPicker = new JWindow();

            ActionListener al = e -> {
                getHourPicker().setVisible(false);
                int i = Integer.parseInt(e.getActionCommand());
                setHour(i);
            };

            JPanel p = new JPanel();
            p.setLayout(new GridLayout(4, 6, 0, 0));

            Insets ins = new Insets(1, 3, 1, 3);

            for (int i = 0; i < 24; i++) {
                JButton b = new JButton(String.valueOf(i));
                b.setActionCommand(String.valueOf(i));
                b.addActionListener(al);
                b.setMargin(ins);
                p.add(b);
            }

            hourPicker.setContentPane(p);
            hourPicker.pack();

        }
        return hourPicker;
    }

    protected JWindow getMinutePicker() {
        if (minutePicker == null) {
            minutePicker = new JWindow();

            ActionListener al = e -> {
                getMinutePicker().setVisible(false);
                int i = Integer.parseInt(e.getActionCommand());
                setMinute(i);
            };

            JPanel p = new JPanel();
            p.setLayout(new GridLayout(2, 6, 0, 0));

            Insets ins = new Insets(1, 3, 1, 3);

            for (int i = 0; i < 60; i += 5) {
                JButton b = new JButton(String.valueOf(i));
                b.setActionCommand(String.valueOf(i));
                b.addActionListener(al);
                b.setMargin(ins);
                p.add(b);
            }

            minutePicker.setContentPane(p);
            minutePicker.pack();

        }
        return minutePicker;
    }

    private int getMinute() {
        return (int) (timeOfDay % JournalTools.MILLIS_IN_HOUR) / 60000;
    }

    protected void setMinute(int i) {
        setTimeOfDay((getHour() * 60 + i) * 60000);
    }

    private int getHour() {
        return (int) Math.floor(timeOfDay / JournalTools.MILLIS_IN_HOUR) % 24;
    }

    protected void setHour(int i) {
        setTimeOfDay((i * 60 + getMinute()) * 60000);
    }

    private void hidePickers() {
        if (hourPicker != null) {
            hourPicker.setVisible(false);
        }
        if (minutePicker != null) {
            minutePicker.setVisible(false);
        }
    }

    public int getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(int i) {
        if (i == timeOfDay) {
            return;
        }
        hidePickers();
        int old = timeOfDay;
        timeOfDay = i;
        hourButton.setText(String.valueOf(getHour()));
        minuteButton.setText(String.valueOf(getMinute()));
        firePropertyChange(PROPERTY_TIME_OF_DAY, old, timeOfDay);
    }
}
