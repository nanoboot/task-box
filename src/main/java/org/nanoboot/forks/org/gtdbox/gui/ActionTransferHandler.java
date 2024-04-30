/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *    
 *    This file is part of GTD-Box.
 *    
 *    GTD-Box is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *    
 *    GTD-Box is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *    
 *    You should have received a copy of the GNU General Public License
 *    along with GTD-Box.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.forks.org.gtdbox.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.nanoboot.forks.org.gtdbox.model.Action;

public abstract class ActionTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;

	class ActionTransfer implements Transferable {
		Integer id;
		public ActionTransfer(Integer i) {
			id=i;
		}
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return ACTION_FLAVOR==flavor;
		}
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{ACTION_FLAVOR};
		}
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (flavor==ACTION_FLAVOR) {
				return id;
			}
			return null;
		}
		@Override
		public String toString() {
			return "transferable={"+id+"}";
		}
	}
	
	public static final DataFlavor ACTION_FLAVOR= new DataFlavor(Action.class,"Action");

	/**
	 * @param pane
	 */
	ActionTransferHandler() {
	}

	@Override
	public boolean importData(TransferSupport support) {
		Object drop=null;
		Integer i=null;
		try {
			drop= support.getTransferable().getTransferData(ACTION_FLAVOR);
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (drop!=null) {
			i= (Integer)drop;
		}
		if (i!=null) {
			//System.out.println("IMPORT "+component.getClass().getName()+" "+i+" "+support.getTransferable());
			return importAction(i,support);
		}
		//System.out.println("IMPORT FALSE "+component.getClass().getName()+" "+i+" "+support.getTransferable());
		return false;
	}

	protected abstract boolean importAction(int id, TransferSupport support);
	protected abstract Integer exportAction();

	@Override
	public boolean canImport(TransferSupport support) {
		DataFlavor[] f= support.getDataFlavors();
		for (int i = 0; i < f.length; i++) {
			if (f[i]==ACTION_FLAVOR) {
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
}
