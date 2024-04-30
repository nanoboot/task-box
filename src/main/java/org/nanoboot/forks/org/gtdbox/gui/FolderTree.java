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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.nanoboot.forks.org.gtdbox.GTDBoxEngine;
import org.nanoboot.forks.org.gtdbox.model.Action;
import org.nanoboot.forks.org.gtdbox.model.ActionEvent;
import org.nanoboot.forks.org.gtdbox.model.Folder;
import org.nanoboot.forks.org.gtdbox.model.FolderEvent;
import org.nanoboot.forks.org.gtdbox.model.GTDModel;
import org.nanoboot.forks.org.gtdbox.model.GTDModelListener;

/**
 * @author ikesan
 *
 */
public class FolderTree extends JTree {
	
	private static final long serialVersionUID = 1L;

	class SortedTreeNode extends DefaultMutableTreeNode implements Comparator<TreeNode> {
		
		
		private static final long serialVersionUID = 4899968443878756400L;
		public SortedTreeNode() {
			super();
		}
		/**
		 * @param userObject
		 * @param allowsChildren
		 */
		public SortedTreeNode(Object userObject, boolean allowsChildren) {
			super(userObject, allowsChildren);
		}
		/**
		 * @param userObject
		 */
		public SortedTreeNode(Object userObject) {
			super(userObject);
		}
		@SuppressWarnings("unchecked")
		@Override
		public void add(MutableTreeNode newChild) {
			super.add(newChild);
			
			Collections.sort(children, this);
		}
		public int compare(TreeNode o1, TreeNode o2) {
			if(!(o1 instanceof DefaultMutableTreeNode)) {
				return 0;
			}
			if(!(o2 instanceof DefaultMutableTreeNode)) {
				return 0;
			}
			Object f1= ((DefaultMutableTreeNode)o1).getUserObject();
			Object f2= ((DefaultMutableTreeNode)o2).getUserObject();
			if (f1 instanceof Folder && f2 instanceof Folder) {
				return ((Folder)f1).getName().compareTo(((Folder)f2).getName());
			}
			return 0;
		}
		
		@SuppressWarnings("unchecked")
		public void sort() {
			if (children!=null) {
				Collections.sort(children, this);
			}
		}
	}
	
	private GTDModel gtdModel;
	private GTDBoxEngine engine;
	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	private SortedTreeNode actions;
	private SortedTreeNode meta;
	private SortedTreeNode references;
	private SortedTreeNode projects;
	private SortedTreeNode someday;
	private Folder selectedFolder;
	private boolean defaultFoldersVisible=true;
	private boolean emptyFoldersVisible=true;
	private boolean showClosedFolders=false;

	/**
	 * @return the selectedFolder
	 */
	public Folder getSelectedFolder() {
		return selectedFolder;
	}

	public FolderTree() {
		initialize();
	}

	private void initialize() {
		
		root= new DefaultMutableTreeNode(null,true);
		model  = new DefaultTreeModel(root, true);
		setModel(model);
		
		setRootVisible(false);
		setExpandsSelectedPaths(true);
		setToggleClickCount(1);
		setEditable(false);
		
		actions= new SortedTreeNode("Actions",true);
		meta= new SortedTreeNode("Default Lists",true);
		references= new SortedTreeNode("References",true);
		someday= new SortedTreeNode("Someday/Maybe",true);
		projects= new SortedTreeNode("Projects",true);
		
		addTreeSelectionListener(new TreeSelectionListener() {
		
			public void valueChanged(TreeSelectionEvent e) {
				Folder old= selectedFolder;
				selectedFolder= null;
				if (e.getNewLeadSelectionPath()!=null) {
					Object o= ((DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent()).getUserObject();
					if (o instanceof Folder) {
						selectedFolder= (Folder)o;
					}
				}
				firePropertyChange("selectedFolder", old, selectedFolder);
			}
		
		});
		
		setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				DefaultMutableTreeNode node= (DefaultMutableTreeNode)value;
				super.getTreeCellRendererComponent(tree, "", sel, expanded, leaf,
						row, hasFocus);
				
				Font font= FolderTree.this.getFont();
				
				if (node.getLevel()==1) {
					super.setFont(font.deriveFont(Font.BOLD));
					setBackground(Color.LIGHT_GRAY);
				} else {
					super.setFont(font);
					//setBackground(FolderTree.this.getBackground());
				}
				
				Folder f=null;
				if (node.getUserObject() instanceof Folder) {
					f = (Folder)node.getUserObject();
					StringBuilder sb= new StringBuilder(32);
					sb.append(f.getName());
					sb.append(' ');
					sb.append('(');
					if (f.getType() == Folder.FolderType.BUILDIN_RESOLVED || f.getType() == Folder.FolderType.BUILDIN_DELETED) {
						sb.append(f.size());
					} else {
						sb.append(f.getOpenCount());
					}
					sb.append(')');
					value= sb.toString();
				}
				if (f != null) {
					if (f.isClosed()) {
						setForeground(Color.LIGHT_GRAY);
					}
				}
				
				setText(String.valueOf(value));
				return this;
			}
			@Override
			public void setFont(Font font) {
				// ignore
			}
		});
		setCellEditor(new DefaultTreeCellEditor(this,(DefaultTreeCellRenderer)getCellRenderer()){@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			if (value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode)value).getUserObject() instanceof Folder) {
				value= ((Folder)((DefaultMutableTreeNode)value).getUserObject()).getName();
			}
			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded,
					leaf, row);
		}});
		
		setTransferHandler(new ActionTransferHandler() {
			
			private static final long serialVersionUID = 0L;

			@Override
			protected boolean importAction(int id, TransferSupport support) {
				try {
					Action a= gtdModel.getAction(id);
					//System.out.println(support.getDropLocation().getDropPoint());
					TreePath tp= getClosestPathForLocation(support.getDropLocation().getDropPoint().x,support.getDropLocation().getDropPoint().y);
					//System.out.println(tp);
					//System.out.println(tp.getLastPathComponent());
					DefaultMutableTreeNode n= (DefaultMutableTreeNode)tp.getLastPathComponent();
					if (n!=null && n.getUserObject() instanceof Folder && a!=null) {
						Folder target= (Folder)n.getUserObject();
						gtdModel.moveAction(a, target);
						return true;
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return false;
			}

			@Override
			protected Integer exportAction() {
				return null;
			}
			
			@Override
			public boolean canImport(TransferSupport support) {
				TreePath tp= getClosestPathForLocation(support.getDropLocation().getDropPoint().x,support.getDropLocation().getDropPoint().y);
				DefaultMutableTreeNode n= (DefaultMutableTreeNode)tp.getLastPathComponent();
				if (n!=null && n.getUserObject() instanceof Folder && !((Folder)n.getUserObject()).isMeta() && ((Folder)n.getUserObject()).getType() != Folder.FolderType.INBUCKET) {
					return super.canImport(support);
				} 
				/*if (n==references || n==actions) {
					expandPath(new TreePath(n.getPath()));
				}*/
				return false;
			}
			
		});
		
		/*addTreeExpansionListener(new TreeExpansionListener() {
			boolean expanding=false;
			public void treeExpanded(TreeExpansionEvent event) {
				TreePath ex= event.getPath();
				int i=0;
				while (i<getRowCount()) {
					if (getPathForRow(i)!=ex) {
						collapseRow(i);
					}
					i++;
				}
				DefaultMutableTreeNode n= (DefaultMutableTreeNode)ex.getLastPathComponent();
				if (n.getChildCount()>0 && !expanding) {
					expanding=true;
					setSelectionPath(new TreePath(((DefaultMutableTreeNode)n.getFirstChild()).getPath()));
					expanding=false;
				}
			}
		
			public void treeCollapsed(TreeExpansionEvent event) {
				//
		
			}
		
		});*/
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (getPathForLocation(e.getPoint().x, e.getPoint().y)!=null || e.getPoint().x<30) {
					return;
				}
				TreePath t= getClosestPathForLocation(e.getPoint().x, e.getPoint().y);
				/*if (getExpandedDescendants(t)!=null) {
					collapsePath(t);
				} else {
					expandPath(t);
				}*/
				setSelectionPath(t);
			}
		});
		
		rebuildTree();
	}
	
	

	/**
	 * @return the gtdModel
	 */
	GTDModel getGTDModel() {
		return gtdModel;
	}

	/**
	 * @param gtdModel the gtdModel to set
	 */
	private void setGTDModel(GTDModel gtdModel) {
		this.gtdModel = gtdModel;
		gtdModel.addGTDModelListener(new GTDModelListener() {
		
			public void elementRemoved(FolderEvent note) {
				model.nodeChanged(folderToNode(note.getFolder()));
				repaint();
			}
		
			public void elementModified(ActionEvent note) {
				int[] i= new int[meta.getChildCount()];
				for (int j = 0; j < i.length; j++) {
					i[j]=j;
				}
				model.nodesChanged(meta, i);
				repaint();
			}
		
			public void elementAdded(FolderEvent note) {
				model.nodeChanged(folderToNode(note.getFolder()));
				repaint();
			}
		
			public void folderRemoved(Folder folder) {
				removeFromTree(folder, true);
			}
		
			public void folderModified(FolderEvent f) {
				if (f.getProperty()=="closed") {
					if (f.getFolder().isClosed() && !showClosedFolders) {
						removeFromTree(f.getFolder(), true);
					} else if (!f.getFolder().isClosed() && folderToNode(f.getFolder())==null) {
						addToTree(f.getFolder(), true);
					} 
				} else if (f.getProperty()=="name") {
					SortedTreeNode p= folderToParentNode(f.getFolder());
					if (p!=null) {
						p.sort();
						model.nodeStructureChanged(p);
					}
				} else {
					TreeNode p= folderToNode(f.getFolder());
					if (p!=null) {
						model.nodeChanged(p);
					}
				}
				repaint();
			}
		
			public void folderAdded(Folder folder) {
				addToTree(folder, true);
			}
			public void orderChanged(Folder f) {
				//
			}
		
		});		
		rebuildTree();
	}
	
	private void rebuildTree() {
		//root.removeAllChildren();

		boolean rootChange=false;
		int pos=0;
		if (root.getChildCount()>pos && root.getChildAt(pos)!=actions) {
			root.removeAllChildren();
			rootChange=true;
		}
		if (root.getChildCount()==pos) {
			root.add(actions);
			rootChange=true;
		}
		pos++;
		
		if (defaultFoldersVisible) {
			while (root.getChildCount()>pos && root.getChildAt(pos)!=meta) {
				root.remove(root.getChildCount()-1);
				rootChange=true;
			}
			if (root.getChildCount()==pos) {
				root.add(meta);
				rootChange=true;
			}
			pos++;
			while (root.getChildCount()>pos && root.getChildAt(pos)!=projects) {
				root.remove(root.getChildCount()-1);
				rootChange=true;
			}
			if (root.getChildCount()==pos) {
				root.add(projects);
				rootChange=true;
			}
			pos++;
		}
		while (root.getChildCount()>pos && root.getChildAt(pos)!=someday) {
			root.remove(root.getChildCount()-1);
			rootChange=true;
		}
		if (root.getChildCount()==pos) {
			root.add(someday);
			rootChange=true;
		}
		pos++;
		while (root.getChildCount()>pos && root.getChildAt(pos)!=references) {
			root.remove(root.getChildCount()-1);
			rootChange=true;
		}
		if (root.getChildCount()==pos) {
			root.add(references);
			rootChange=true;
		}

		actions.removeAllChildren();
		someday.removeAllChildren();
		references.removeAllChildren();
		projects.removeAllChildren();
		meta.removeAllChildren();
		
		if (gtdModel!=null) {
			for (Folder f : gtdModel) {
				if (showClosedFolders || !f.isClosed()) {
					addToTree(f, false);
				}
			}
		}
		
		if (rootChange) {
			model.nodeStructureChanged(root);
		} else {
			model.nodeStructureChanged(actions);
			model.nodeStructureChanged(someday);
			model.nodeStructureChanged(references);
			model.nodeStructureChanged(projects);
			model.nodeStructureChanged(meta);
			
		}
	}
	private DefaultMutableTreeNode addToTree(Folder f, boolean notify) {
		if (!emptyFoldersVisible && f.size()==0) {
			return null;
		}
		TreeNode n=null;
		DefaultMutableTreeNode r=null;
		if (f.getType() == Folder.FolderType.ACTION) {
			actions.add(r=new DefaultMutableTreeNode(f,false));
			n=actions;
		} else if (f.getType() == Folder.FolderType.REFERENCE) {
			references.add(r=new DefaultMutableTreeNode(f,false));
			n=references;
		} else if (f.getType() == Folder.FolderType.SOMEDAY) {
			someday.add(r=new DefaultMutableTreeNode(f,false));
			n=someday;
		} else if (defaultFoldersVisible && engine.getStateMachine().getDefaultTreeBranch(f.getType())) {
			meta.add(r=new DefaultMutableTreeNode(f,false));
			n=meta;
		} else if (defaultFoldersVisible && f.getType() == Folder.FolderType.PROJECT) {
			projects.add(r=new DefaultMutableTreeNode(f,false));
			n=projects;
		}
		if (notify && n!=null) {
			model.nodeStructureChanged(n);
		}
		return r;
	}
	private TreeNode removeFromTree(Folder f, boolean notify) {
		TreeNode dn= folderToNode(f);
		if (dn!=null && dn.getParent()==f) {
			DefaultMutableTreeNode p= (DefaultMutableTreeNode)dn.getParent(); 
			int[] id= {p.getIndex(dn)};
			p.remove(id[0]);
			if (notify) {
				//model.nodeStructureChanged(p);
				model.nodesWereRemoved(p, id, new Object[]{dn});
				repaint();
			}
			return dn;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public TreeNode folderToNode(Folder f) {
		Enumeration<TreeNode> en=null;
		DefaultMutableTreeNode p= folderToParentNode(f);
		if (p!=null) {
			en= p.children();
		}
		if (en!=null) {
			while (en.hasMoreElements()) {
				TreeNode n= en.nextElement();
				if (n.getParent()==f) {
					return n;
				}
			}
		}
		return null;
	}

	public SortedTreeNode folderToParentNode(Folder f) {
		if (f.getType() == Folder.FolderType.ACTION) {
			return actions;
		} else if (f.isReference()) {
			return references;
		} else if (f.getType() == Folder.FolderType.SOMEDAY) {
			return someday;
		} else if (engine.getStateMachine().getDefaultTreeBranch(f.getType())) {
			return meta;
		} else if (f.getType() == Folder.FolderType.PROJECT ) {
			return projects;
		}
		
		return null;
	}
	
	public void addFolder(String name) {
		
		Folder.FolderType type= Folder.FolderType.ACTION;
		
		if (getSelectionPath()!=null && getSelectionPath().getPathCount()>0 ) {
			if (getSelectionPath().getPathComponent(1)==references) {
				type= Folder.FolderType.REFERENCE;
			} else if (getSelectionPath().getPathComponent(1)==someday) {
				type= Folder.FolderType.SOMEDAY;
			} else if (getSelectionPath().getPathComponent(1)==projects) {
				type= Folder.FolderType.PROJECT;
			}
		}

		Folder f= gtdModel.createFolder(name, type);
		setSelectionPath(new TreePath(model.getPathToRoot(folderToNode(f))));
	}

	public boolean isFolderRenamePossible() {
		return getSelectedFolder()!=null && (getSelectedFolder().isUserFolder() || getSelectedFolder().isProject());
	}
	
	public boolean isFolderAddPossible() {
		if (getSelectionPath()!=null && getSelectionPath().getPathCount()>0 ) {
			if (getSelectionPath().getPathComponent(1)==someday ||getSelectionPath().getPathComponent(1)==references || getSelectionPath().getPathComponent(1)==actions || getSelectionPath().getPathComponent(1)==projects) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * @return the defaultFoldersVisible
	 */
	public boolean isDefaultFoldersVisible() {
		return defaultFoldersVisible;
	}

	/**
	 * @param defaultFoldersVisible the defaultFoldersVisible to set
	 */
	public void setDefaultFoldersVisible(boolean defaultFoldersVisible) {
		this.defaultFoldersVisible = defaultFoldersVisible;
		rebuildTree();
	}

	/**
	 * @return the emptyFoldersVisible
	 */
	public boolean isEmptyFoldersVisible() {
		return emptyFoldersVisible;
	}

	/**
	 * @param emptyFoldersVisible the emptyFoldersVisible to set
	 */
	public void setEmptyFoldersVisible(boolean emptyFoldersVisible) {
		this.emptyFoldersVisible = emptyFoldersVisible;
		rebuildTree();
	}

	/**
	 * @return the showClosedFolders
	 */
	public boolean isShowClosedFolders() {
		return showClosedFolders;
	}

	/**
	 * @param showClosedFolders the showClosedFolders to set
	 */
	public void setShowClosedFolders(boolean showClosedFolders) {
		this.showClosedFolders = showClosedFolders;
		rebuildTree();
	}

	public void setExpendedNodes(int[] ii) {
		if (ii==null || ii.length==0) {
			return;
		}
		
		int i= 0;
		while (i<getRowCount()) {
			collapseRow(i++);
		}
		
		Arrays.sort(ii);
		
		for (i = 0; i < ii.length; i++) {
			if (i>-1) {
				expandRow(ii[i]);
			}
		}
	}

	public int[] getExpendedNodes() {
		
		List<Integer> i= new ArrayList<Integer>();
		Enumeration<TreePath> en= getExpandedDescendants(new TreePath(root.getPath()));
		
		if (en==null) {
			return null;
		}
		
		while(en.hasMoreElements()) {
			TreePath tp= en.nextElement();
			int r= getRowForPath(tp);
			if (r>-1) {
				i.add(r);
			}
		}
		int[] ii= new int[i.size()];
		
		for (int j = 0; j < ii.length; j++) {
			ii[j]=i.get(j);
		}
		
		Arrays.sort(ii);
		return ii;
	}

	/**
	 * @param engine the engine to set
	 */
	public void setEngine(GTDBoxEngine engine) {
		this.engine = engine;
		setGTDModel(engine.getGTDModel());
	}

	/**
	 * @return the engine
	 */
	public GTDBoxEngine getEngine() {
		return engine;
	}

	public void setSelectedFolder(Folder f) {
		TreeNode n= folderToNode(f);
		if (n!=null) {
			setSelectionPath(new TreePath(folderToNode(f).getParent()));
		}
	}

	
	
}
