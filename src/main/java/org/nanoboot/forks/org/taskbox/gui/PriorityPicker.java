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

import org.nanoboot.forks.org.taskbox.ApplicationHelper;
import org.nanoboot.forks.org.taskbox.model.Priority;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author ikesan
 */
public class PriorityPicker extends JPanel {

    public static final String PRIORITY_PROPERTY_NAME = "priority";
    private static final long serialVersionUID = 1L;
    private static final Icon yellow = ApplicationHelper
            .getIcon(ApplicationHelper.icon_name_small_star_yellow);
    private static final Icon orange = ApplicationHelper
            .getIcon(ApplicationHelper.icon_name_small_star_orange);
    private static final Icon red = ApplicationHelper
            .getIcon(ApplicationHelper.icon_name_small_star_red);
    private static final Icon grey = ApplicationHelper
            .getIcon(ApplicationHelper.icon_name_small_star_grey);
    private static final Icon blue = ApplicationHelper
            .getIcon(ApplicationHelper.icon_name_small_star_blue);
    private Priority priority = Priority.None;
    private Priority hover;
    public PriorityPicker() {

        Dimension d = new Dimension(3 * (16 + 4), 16 + 4);
        setMinimumSize(d);
        setPreferredSize(d);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                callHover(e.getPoint(), true);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isEnabled()) {
                    return;
                }

                int q = getWidth() / 3;

                if (e.getX() < q) {
                    if (priority == Priority.Low) {
                        setPriority(Priority.None);
                    } else {
                        setPriority(Priority.Low);
                    }
                } else if (e.getX() < q * 2) {
                    if (priority == Priority.Medium) {
                        setPriority(Priority.None);
                    } else {
                        setPriority(Priority.Medium);
                    }
                } else {
                    if (priority == Priority.High) {
                        setPriority(Priority.None);
                    } else {
                        setPriority(Priority.High);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isEnabled()) {
                    return;
                }

                setHover(null, true);
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public static void main(String[] args) {

        JFrame f = new JFrame();
        PriorityPicker p = new PriorityPicker();
        f.setContentPane(p);
        f.pack();
        f.setVisible(true);

    }

    /**
     * @return the priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(Priority priority) {
        if (this.priority == priority) {
            return;
        }
        Priority old = this.priority;
        this.priority = priority;
        firePropertyChange(PRIORITY_PROPERTY_NAME, old, priority);
        setToolTipText("Priority: " + priority);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int space = (getWidth() - 3 * 16) / 6;
        int top = (getHeight() - 16) / 2;

        Icon i1 = grey;
        Icon i2 = grey;
        Icon i3 = grey;

        if (hover != null && isEnabled()) {
            if (hover.ordinal() > 0) {
                i1 = blue;
            }
            if (hover.ordinal() > 1) {
                i2 = blue;
            }
            if (hover.ordinal() > 2) {
                i3 = blue;
            }
        } else if (priority != null && isEnabled()) {
            if (priority.ordinal() > 0) {
                i1 = yellow;
            }
            if (priority.ordinal() > 1) {
                i2 = orange;
            }
            if (priority.ordinal() > 2) {
                i3 = red;
            }
        }

        i1.paintIcon(this, g, space, top);
        i2.paintIcon(this, g, space * 3 + 16, top);
        i3.paintIcon(this, g, space * 5 + 16 * 2, top);

    }

    protected void callHover(Point p, boolean repaint) {
        if (!isEnabled()) {
            return;
        }
        int q = getWidth() / 3;

        if (p.getX() < q) {
            setHover(Priority.Low, repaint);
        } else if (p.getX() < q * 2) {
            setHover(Priority.Medium, repaint);
        } else {
            setHover(Priority.High, repaint);
        }

    }

    private void setHover(Priority hover, boolean repaint) {
        if (this.hover == hover) {
            return;
        }
        this.hover = hover;
        setToolTipText("Priority: " + (hover != null ? hover : priority));

        if (repaint) {
            repaint();
        }
    }
}
