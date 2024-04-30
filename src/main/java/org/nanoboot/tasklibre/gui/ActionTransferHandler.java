/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *
 *    This file is part of Task-Libre.
 *
 *    Task-Libre is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Task-Libre is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Task-Libre.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.tasklibre.gui;

import org.nanoboot.tasklibre.model.Action;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public abstract class ActionTransferHandler extends TransferHandler {

    public static final DataFlavor ACTION_FLAVOR =
            new DataFlavor(Action.class, "Action");
    private static final long serialVersionUID = 1L;

    /**
     * @param pane
     */
    ActionTransferHandler() {
    }

    @Override
    public boolean importData(TransferSupport support) {
        Object drop = null;
        Integer i = null;
        try {
            drop = support.getTransferable().getTransferData(ACTION_FLAVOR);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
        if (drop != null) {
            i = (Integer) drop;
        }
        if (i != null) {
            //System.out.println("IMPORT "+component.getClass().getName()+" "+i+" "+support.getTransferable());
            return importAction(i, support);
        }
        //System.out.println("IMPORT FALSE "+component.getClass().getName()+" "+i+" "+support.getTransferable());
        return false;
    }

    protected abstract boolean importAction(int id, TransferSupport support);

    protected abstract Integer exportAction();

    @Override
    public boolean canImport(TransferSupport support) {
        DataFlavor[] f = support.getDataFlavors();
        for (DataFlavor dataFlavor : f) {
            if (dataFlavor == ACTION_FLAVOR) {
                //System.out.println("CAN IMPORT TRUE "+component.getClass().getName()+" "+support.getComponent().getClass().getName()+" "+support.getTransferable());
                return true;
            }
        }
        //System.out.println("CAN IMPORT FALSE "+component.getClass().getName()+" "+support.getComponent().getClass().getName()+" "+support.getTransferable());
        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        //System.out.println("CREATE TRANSFER "+c.getClass().getName()+" "+exportAction());
        return new ActionTransfer(exportAction());
    }

    static class ActionTransfer implements Transferable {
        final Integer id;

        public ActionTransfer(Integer i) {
            id = i;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return ACTION_FLAVOR == flavor;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {ACTION_FLAVOR};
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (flavor == ACTION_FLAVOR) {
                return id;
            }
            return null;
        }

        @Override
        public String toString() {
            return "transferable={" + id + "}";
        }
    }
}
