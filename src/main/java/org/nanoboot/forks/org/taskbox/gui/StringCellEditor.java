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

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import java.awt.Font;

/**
 * @author ikesan
 */
public class StringCellEditor extends DefaultCellEditor
        implements TableCellEditor {

    private static final long serialVersionUID = 1L;

    public StringCellEditor() {
        super(new JTextField());
    }

    public Font getFont() {
        return editorComponent.getFont();
    }

    public void setFont(Font f) {
        editorComponent.setFont(f);
    }
}
