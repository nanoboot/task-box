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

import org.nanoboot.gtdlibre.model.Action;
import org.nanoboot.gtdlibre.model.Folder;
import org.nanoboot.gtdlibre.model.GTDModel;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author ikesan
 */
public class ActionFolderList extends JTable {

    private static final long serialVersionUID = 1L;
    private final FolderTableModel model = new FolderTableModel();
    private GTDModel gtdModel;

    public ActionFolderList() {
        initialize();
    }

    private void initialize() {
        //setAutoCreateColumnsFromModel(false);
        setAutoCreateColumnsFromModel(true);
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(true);
        setModel(model);
        StringCellEditor sce = new StringCellEditor();
        sce.setFont(sce.getFont().deriveFont(Font.BOLD));
        setDefaultEditor(String.class, sce);
        setDefaultRenderer(String.class, new CellRenderer());
        FontMetrics fm = getFontMetrics(getFont());
        setRowHeight(fm.getHeight() + 3);
        //getTableHeader().setVisible(false);

        setTransferHandler(new ActionTransferHandler() {

            private static final long serialVersionUID = 0L;

            @Override
            protected boolean importAction(int id, TransferSupport support) {
                Action a = gtdModel.getAction(id);
                int i = rowAtPoint(support.getDropLocation().getDropPoint());

                if (i > -1 && a != null) {
                    Folder target = model.getFolder(i);
                    gtdModel.moveAction(a, target);
                    return true;
                }
                return false;
            }

            @Override
            protected Integer exportAction() {
                return null;
            }

        });
    }

    /**
     * @return the gtdModel
     */
    public GTDModel getGTDModel() {
        return gtdModel;
    }

    /**
     * @param gtdModel the gtdModel to set
     */
    public void setGTDModel(GTDModel m) {
        this.gtdModel = m;
        model.reload(m);
    }

    public Folder getSelectedFolder() {
        int i = getSelectedRow();
        if (i > -1) {
            return model.getFolder(i);
        }
        return null;
    }

    public void addFolder(String name) {
        Folder f = gtdModel.createFolder(name, Folder.FolderType.ACTION);
        int i = model.addFolder(f);
        getSelectionModel().setSelectionInterval(i, i);
    }

    static class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public CellRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus,
                    row, column);
            setFont(getFont().deriveFont(Font.BOLD));
            return this;
        }
    }

    static class FolderTableModel extends AbstractTableModel
            implements Comparator<Folder> {

        private static final long serialVersionUID = -3898990048337029213L;
        final List<Folder> data = new ArrayList<>();

        public int addFolder(Folder f) {
            data.add(f);
            sort();
            fireTableStructureChanged();
            return data.indexOf(f);
        }

        public void reload(GTDModel m) {
            data.clear();
            for (Folder folder : m) {
                if (folder.getType() == Folder.FolderType.ACTION) {
                    data.add(folder);
                }
            }
            sort();
            fireTableStructureChanged();
        }

        public int compare(Folder o1, Folder o2) {
            return o1.getName().compareTo(o2.getName());
        }

        private void sort() {
            data.sort(this);
        }

        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return data.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return data.get(rowIndex).getName();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public String getColumnName(int column) {
            return "Lists";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                data.get(rowIndex).rename(aValue.toString());
                sort();
                fireTableDataChanged();
            }
        }

        public Folder getFolder(int i) {
            return data.get(i);
        }
    }
}
