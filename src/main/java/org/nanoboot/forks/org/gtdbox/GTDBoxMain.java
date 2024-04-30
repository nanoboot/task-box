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

package org.nanoboot.forks.org.gtdbox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.nanoboot.forks.org.gtdbox.gui.ExecutePane;
import org.nanoboot.forks.org.gtdbox.gui.ImportExampleDialog;
import org.nanoboot.forks.org.gtdbox.gui.InBasketPane;
import org.nanoboot.forks.org.gtdbox.gui.JournalPane;
import org.nanoboot.forks.org.gtdbox.gui.OrganizePane;
import org.nanoboot.forks.org.gtdbox.gui.ProcessPane;
import org.nanoboot.forks.org.gtdbox.gui.QuickCollectPanel;
import org.nanoboot.forks.org.gtdbox.model.ConsistencyException;
import org.nanoboot.forks.org.gtdbox.model.Folder;
import org.nanoboot.forks.org.gtdbox.model.FolderEvent;
import org.nanoboot.forks.org.gtdbox.model.GTDModel;
import org.nanoboot.forks.org.gtdbox.model.GTDModelListener;
import org.nanoboot.forks.org.gtdbox.model.Utils;

/**
 * @author ikesan
 *
 */
public class GTDBoxMain {
	
	class OverviewTabPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public OverviewTabPanel(final Icon icon, final int tab, final String desc) {
			
			setLayout(new GridBagLayout());
			
			JButton b= new JButton();
			b.setIcon(icon);
			b.addActionListener(new ActionListener() {
			
				@Override
				public void actionPerformed(ActionEvent e) {
					tabbedPane.setSelectedIndex(tab);
				}
			});
			b.setMargin(new Insets(0,0,0,0));
			add(b,new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,7),0,0));

			JLabel l= new JLabel(desc);
			l.setFont(l.getFont().deriveFont(Font.BOLD));
			add(l,new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));

		}
	}
	
	class SummaryBean implements GTDModelListener {
		
		private PropertyChangeSupport supp= new PropertyChangeSupport(this);
		private int inbucketCount;
		private int queueCount; 
		private int pastActions;
		private int todayActions;
		private int totalCount;
		private int openCount;
		
		public SummaryBean() {
			getEngine().getGTDModel().addGTDModelListener(this);
			updateInBucket();
			updateQueue();
			updateReminders();
			updateMainCounts();
		}

		@Override
		public void folderAdded(Folder folder) {
			//
		}

		@Override
		public void folderModified(FolderEvent folder) {
			//
		}

		@Override
		public void folderRemoved(Folder folder) {
			//
		}

		@Override
		public void elementAdded(FolderEvent a) {
			if (a.getFolder().isQueue()) {
				updateQueue();
			} else if (a.getFolder().isInBucket()) {
				updateInBucket();
			} else if (a.getFolder().isTickler()) {
				updateReminders();
			}
			updateMainCounts();
		}

		@Override
		public void elementModified(
				org.nanoboot.forks.org.gtdbox.model.ActionEvent a) {
			if (a.getAction().getFolder().isQueue()||a.getAction().isQueued()) {
				updateQueue();
			} else if (a.getAction().getFolder().isInBucket()) {
				updateInBucket();
			} else if (a.getProperty().equals(
					org.nanoboot.forks.org.gtdbox.model.Action.REMIND_PROPERTY_NAME) 
					|| (a.getAction().getRemind()!=null && a.getProperty().equals(
					org.nanoboot.forks.org.gtdbox.model.Action.RESOLUTION_PROPERTY_NAME))) {
				updateReminders();
			}
		}

		@Override
		public void elementRemoved(FolderEvent a) {
			if (a.getFolder().isQueue()) {
				updateQueue();
			} else if (a.getFolder().isInBucket()) {
				updateInBucket();
			} else if (a.getFolder().isTickler()) {
				updateReminders();
			}
			updateMainCounts();
		}

		@Override
		public void orderChanged(Folder f) {
			// 
		}
		
		private void updateInBucket() {
			int n= getEngine().getGTDModel().getInBucketFolder().getOpenCount();
			if (n!=inbucketCount) {
				int old= inbucketCount;
				inbucketCount=n;
				supp.firePropertyChange("inbucketCount", old, n);
			}
		}
		private void updateQueue() {
			int n= getEngine().getGTDModel().getQueue().getOpenCount();
			if (n!=queueCount) {
				int old= queueCount;
				queueCount=n;
				supp.firePropertyChange("queueCount", old, n);
			}
		}
		private void updateMainCounts() {
			int open= 0;
			int total= 0;
			for (Folder f : getEngine().getGTDModel()) {
				if (!f.isMeta()) {
					open+=f.getOpenCount();
					total+=f.size();
				}
			}

			if (total!=this.totalCount || open!=this.openCount) {
				this.totalCount=total;
				this.openCount=open;
				supp.firePropertyChange("mainCounts", -1,total);
			}
		}
		private void updateReminders() {
			int past=0;
			int today=0;
			long time= Utils.today();
			for (org.nanoboot.forks.org.gtdbox.model.Action a : getEngine().getGTDModel().getRemindFolder()) {
				if (a.getRemind()!=null && a.isOpen()) {
					long t= a.getRemind().getTime();
					if (t<time) {
						past++;
					} else if ((t>=time) && (t<(time+Utils.MILLISECONDS_IN_DAY))) {
						today++;
					}
				}
			}
			if (past!=pastActions) {
				pastActions=past;
				supp.firePropertyChange("pastActions", -1, pastActions);
			}
			if (today!=todayActions) {
				todayActions=today;
				supp.firePropertyChange("todayActions", -1, todayActions);
			}
		}

		/**
		 * @return the inbasketCount
		 */
		public int getInbucketCount() {
			return inbucketCount;
		}
		
		public int getQueueCount() {
			return queueCount;
		}
		
		public void addPropertyChangeListener( String prop, PropertyChangeListener l) {
			supp.addPropertyChangeListener(prop, l);
		}

		public int getPastActions() {
			return pastActions;
		}
		
		public int getTodayActions() {
			return todayActions;
		}
		public int getOpenCount() {
			return openCount;
		}
		public int getTotalCount() {
			return totalCount;
		}
		
	}
	
	abstract class SummaryLabel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		
		protected JLabel label;
		protected JButton button;

		public SummaryLabel(final String prop) {
			setLayout(new GridBagLayout());
			
			label= new JLabel();
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			add(label,new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

			SummaryBean sb= getSummaryBean();

			sb.addPropertyChangeListener(prop, new PropertyChangeListener() {
			
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					updateText(evt);
				}
			});
			
			button= new JButton();
			button.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_next));
			button.addActionListener(this);
			button.setMargin(new Insets(1,1,1,1));
			add(button,new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(0,4,0,0),0,0));
			add(new JPanel(),new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
			
			updateText(new PropertyChangeEvent(this,prop,null,null));
		}

		abstract void updateText(PropertyChangeEvent evt);
	}

	private static final int TAB_COLECT = 1;
	private static final int TAB_PROCESS = 2;
	private static final int TAB_ORGANIZE = 3;
	private static final int TAB_EXECUTE = 4;

	private JFrame jFrame = null;
	private JPanel jContentPane = null;
	private JMenuBar jJMenuBar = null;
	private JMenu fileMenu = null;
	private JMenu helpMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem aboutMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JDialog aboutDialog = null;
	private JFileChooser fileChooser;
	private GTDBoxEngine engine;
	private JCheckBoxMenuItem autoSaveMenuItem;
	private ActionMap actionMap;
	private JCheckBoxMenuItem showAllActions;
	private JCheckBoxMenuItem showClosedFolders;
	private JTextArea consistencyCheckText;
	private ProcessPane processPane;
	private OrganizePane organizePane;
	private InBasketPane inBasketPane;
	private ExecutePane executePane;
	private JournalPane journalPane;
	private boolean closed;
	private JPanel overview;
	private JTabbedPane tabbedPane;
	private SummaryBean summaryBean;
	private JCheckBoxMenuItem showOverviewTab;
	private QuickCollectPanel quickCollectPanel;
	private JCheckBoxMenuItem showQuickCollectBar;
	private ImportExampleDialog importDialog;
	private int executeTabIndex;

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		
		try {
			if (System.getProperty("swing.crossplatformlaf")==null) {
				//System.setProperty("swing.crossplatformlaf", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
				String osName = System.getProperty("os.name");
				if (osName != null && osName.indexOf("Windows") != -1) {
					UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
				} else {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		//ApplicationHelper.changeDefaultFontSize(6, "TextField");
		//ApplicationHelper.changeDefaultFontSize(6, "TextArea");
		//ApplicationHelper.changeDefaultFontSize(6, "Table");
		//ApplicationHelper.changeDefaultFontSize(6, "Tree");
		
		//ApplicationHelper.changeDefaultFontStyle(Font.BOLD, "Tree");
		
		System.out.print("GTD-Box");
		try {
			System.out.println(" version "+ApplicationHelper.loadConfiguration().getProperty("build.version"));
		} catch (Exception e) {
			System.out.println();
			// ignore
		}
		System.out.println("Command line usage:");
		System.out.println("java -jar gtd-box.jar [-data <folder>]");
		//                           1         2         3         4         5         6         7         8 
		//                  123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
		System.out.println(" * <folder> the folder, where data and configurations is stored,\n         if omited default folder is used in user's home folder.");
		
		File f=null;
		if (args.length==2 && "-data".equals(args[0])) {
			System.setProperty(ApplicationHelper.DATA_PROPERTY, args[1]);
		}
		
		if (!ApplicationHelper.tryLock(f)) {
			System.out.println("Instance of GTD-Box already running, exiting.");
			System.exit(0);
		}
		
		try {
			final GTDBoxMain application = new GTDBoxMain();
					
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						application.getJFrame();
						application.restore();
						application.getJFrame().setVisible(true);
						//application.restoreState();
						//application.getJFrame().setSize(1024,768);
					} catch (Throwable t) {
						t.printStackTrace();
						System.exit(0);
					}
			}
			});
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						application.close(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
					ApplicationHelper.releaseLock();
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(0);
		}
	} 
	
	public SummaryBean getSummaryBean() {
		if (summaryBean == null) {
			summaryBean = new SummaryBean();
			
		}

		return summaryBean;
	}

	/**
	 * This method initializes jFrame
	 * 
	 * @return javax.swing.JFrame
	 */
	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			jFrame.setJMenuBar(getJJMenuBar());
			jFrame.setSize(1000, 600);
			jFrame.setContentPane(getJContentPane());
			jFrame.setTitle("GTD-Box");
			jFrame.setIconImage(ApplicationHelper.loadImage("splash96.png"));			
			jFrame.addWindowListener(new WindowAdapter() {
			
				@Override
				public void windowClosing(WindowEvent e) {
					close(false);
				}
			
			});
		}
		return jFrame;
	}
	
	/**
	 * 
	 * @param terminal if <code>true</code> then close procedure can not be aborted 
	 * @return <code>true</code> if close can proceed, <code>false</code> if close should be aborted 
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	private boolean close(boolean terminal) {
		if (closed) {
			return true;
		}
		
		if (getEngine().isAborting()) {
			return true;
		}
			
		store(getEngine().getGlobalProperties());
		
		try {
			closed = getEngine().close(terminal);
			if (closed && jFrame!=null && !terminal) {
				jFrame.dispose();
			}
			return closed;
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
	}
	
	private void restore() {
		GlobalProperties p = getEngine().getGlobalProperties();
		
		Integer i1= p.getInteger("window.position.x");
		Integer i2= p.getInteger("window.position.y");

		if (i1!=null && i2!=null) {
			getJFrame().setLocation(i1, i2);
		}

		i1= p.getInteger("window.width");
		i2= p.getInteger("window.heght");
		
		if (i1!=null && i2!=null) {
			getJFrame().setSize(i1, i2);
		}
		
		inBasketPane.restore(p);
		processPane.restore(p);
		organizePane.restore(p);
		executePane.restore(p);

	}
	
/*	private void restoreState() {
		GlobalProperties p = getEngine().getGlobalProperties();
		
		Integer i1= p.getInteger("window.state");
		
		if (i1!=null) {
			getJFrame().setExtendedState(i1);
		}
	}*/

	private ActionMap getActionMap() {
		if (actionMap == null) {
			actionMap = new ActionMap();
			
			AbstractAction a= new AbstractAction("Show Closed Actions") {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					getEngine().getGlobalProperties().putProperty(GlobalProperties.SHOW_ALL_ACTIONS, !getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_ALL_ACTIONS));
				}
			};
			a.putValue(Action.SHORT_DESCRIPTION, "Shows closed actions in folders, works only in Organize tab.");
			actionMap.put(GlobalProperties.SHOW_ALL_ACTIONS, a);

			a=new AbstractAction("Show Closed Folders & Projects") {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					getEngine().getGlobalProperties().putProperty(GlobalProperties.SHOW_CLOSED_FOLDERS, !getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_CLOSED_FOLDERS));
				}
			};
			a.putValue(Action.SHORT_DESCRIPTION, "Shows closed folders and projects, works only in Organize tab.");
			actionMap.put(GlobalProperties.SHOW_CLOSED_FOLDERS, a);
			
			a=new AbstractAction("Show Overview Tab") {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					getEngine().getGlobalProperties().putProperty(GlobalProperties.SHOW_OVERVIEW_TAB, !getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_OVERVIEW_TAB));
				}
			};
			a.putValue(Action.SHORT_DESCRIPTION, "Shows Overview tab.");
			actionMap.put(GlobalProperties.SHOW_OVERVIEW_TAB, a);

			a=new AbstractAction("Show Quick Collect Bar") {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					getEngine().getGlobalProperties().putProperty(GlobalProperties.SHOW_QUICK_COLLECT, !getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_QUICK_COLLECT));
				}
			};
			a.putValue(Action.SHORT_DESCRIPTION, "Shows bar with input for quick adding items to in-basket.");
			actionMap.put(GlobalProperties.SHOW_QUICK_COLLECT, a);

			a=new AbstractAction("Import Examples...") {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					getImportDialog().getDialog(getJFrame()).setVisible(true);
				}
			};
			a.putValue(Action.SHORT_DESCRIPTION, "Opens dialog for inporting GTD_Free XML with example lists and some helpful actions..");
			actionMap.put("importDialog", a);
		}

		return actionMap;
	}

	protected ImportExampleDialog getImportDialog() {
		if (importDialog == null) {
			importDialog = new ImportExampleDialog();
			importDialog.setEngine(getEngine());
		}
		return importDialog;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			
			
			tabbedPane = new JTabbedPane();
			
			if (getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_OVERVIEW_TAB,true)) {
				tabbedPane.addTab("Overview", ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_overview), getOverviewPane());
			}
			
			inBasketPane = new InBasketPane();
			inBasketPane.setEngine(getEngine());
			tabbedPane.addTab("Collect", ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_collecting), inBasketPane);
			
			processPane= new ProcessPane();
			processPane.setEngine(getEngine());
			tabbedPane.addTab("Process", ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_processing), processPane);
			
			organizePane= new OrganizePane();
			organizePane.setEngine(getEngine());
			tabbedPane.addTab("Organize/Review", ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_review), organizePane);
			
			executePane= new ExecutePane();
			executePane.setEngine(getEngine());
			tabbedPane.addTab("Execute ("+getSummaryBean().getQueueCount()+")", ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_queue_execute), executePane);
			executeTabIndex= tabbedPane.getTabCount()-1;
			
			getSummaryBean().addPropertyChangeListener("queueCount", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					tabbedPane.setTitleAt(executeTabIndex, "Execute ("+getSummaryBean().getQueueCount()+")");
				}
			});
			
			if (Boolean.valueOf(getEngine().getConfiguration().getProperty("journal.enabled","false"))) {
				journalPane = new JournalPane();
				journalPane.setEngine(getEngine());
				tabbedPane.addTab("Journal", ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_journaling),journalPane);
			}
			
			tabbedPane.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					enableQuickCollectPanel();
				}
			});

			jContentPane.add(tabbedPane);
			
			quickCollectPanel= new QuickCollectPanel();
			quickCollectPanel.setEngine(getEngine());
			enableQuickCollectPanel();
			jContentPane.add(quickCollectPanel,BorderLayout.SOUTH);
			
		}
		return jContentPane;
	}

	private void enableQuickCollectPanel() {
		quickCollectPanel.setVisible(getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_QUICK_COLLECT) && tabbedPane.getSelectedComponent()!=inBasketPane && tabbedPane.getSelectedComponent()!=overview);
	}

	private Component getOverviewPane() {
		if (overview == null) {
			overview = new JPanel();
			overview.setLayout(new GridBagLayout());

			int row=0;
			
			JLabel l= new JLabel("Manage Workflow");
			l.setFont(l.getFont().deriveFont((float)(l.getFont().getSize()*5.0/4.0)).deriveFont(Font.BOLD));
			overview.add(l,new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(18,18,7,18),0,0));

			overview.add(new OverviewTabPanel(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_collecting),TAB_COLECT,"Collect"),new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,18,4,18),0,0));
			overview.add(new OverviewTabPanel(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_processing),TAB_PROCESS,"Process"),new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,18,4,18),0,0));
			overview.add(new OverviewTabPanel(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_review),TAB_ORGANIZE,"Organize/Review"),new GridBagConstraints(0,row++,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,18,4,18),0,0));
			overview.add(new OverviewTabPanel(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_queue_execute),TAB_EXECUTE,"Execute"),new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,18,4,18),0,0));
			
			l= new JLabel("Actions Summary");
			l.setFont(l.getFont().deriveFont((float)(l.getFont().getSize()*5.0/4.0)).deriveFont(Font.BOLD));
			overview.add(l,new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(14,18,7,18),0,0));

			SummaryLabel sl= new SummaryLabel("inbucketCount") {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					tabbedPane.setSelectedIndex(TAB_PROCESS);
				}
				@Override
				void updateText(PropertyChangeEvent arg0) {
					if (getSummaryBean().getInbucketCount()>0) {
						label.setText(getSummaryBean().getInbucketCount()+" items in in-bucket list. Process them.");
						button.setVisible(true);
					} else {
						label.setText(getSummaryBean().getInbucketCount()+" items in in-bucket list.");
						button.setVisible(false);
					}
				}
			};
			overview.add(sl,new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(0,18,4,18),0,0));
			
			sl= new SummaryLabel("pastActions") {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					tabbedPane.setSelectedIndex(TAB_ORGANIZE);
					organizePane.openTicklerForPast();
				}
				@Override
				void updateText(PropertyChangeEvent arg0) {
					if (getSummaryBean().getPastActions()>0) {
						label.setText(getSummaryBean().getPastActions()+" actions are past reminder date. Update them.");
						button.setVisible(true);
					} else {
						label.setText(getSummaryBean().getPastActions()+" actions are past reminder date.");
						button.setVisible(false);
						
					}
				}
			};
			overview.add(sl,new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(0,18,4,18),0,0));
			
			sl= new SummaryLabel("todayActions") {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					tabbedPane.setSelectedIndex(TAB_ORGANIZE);
					organizePane.openTicklerForToday();
				}
				@Override
				void updateText(PropertyChangeEvent arg0) {
					if (getSummaryBean().getTodayActions()>0) {
						label.setText(getSummaryBean().getTodayActions()+" actions are due today. Open tickler.");
						button.setVisible(true);
					} else {
						label.setText(getSummaryBean().getTodayActions()+" actions are due today.");
						button.setVisible(false);
						
					}
				}
			};
			overview.add(sl,new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(0,18,4,18),0,0));

			sl= new SummaryLabel("queueCount") {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					tabbedPane.setSelectedIndex(TAB_EXECUTE);
				}
				@Override
				void updateText(PropertyChangeEvent arg0) {
					if (getSummaryBean().getQueueCount()>0) {
						label.setText(getSummaryBean().getQueueCount()+" actions queued in \"Next Action\" list. Get things done!");
						button.setVisible(true);
					} else {
						label.setText(getSummaryBean().getQueueCount()+" actions queued in \"Next Action\" list.");
						button.setVisible(false);
						
					}
				}
			};
			overview.add(sl,new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(0,18,4,18),0,0));

			sl= new SummaryLabel("mainCounts") {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					GTDBoxMain.this.getActionMap().get("importDialog").actionPerformed(e);
					engine.getGlobalProperties().putProperty("examplesImported", true);
					updateText(new PropertyChangeEvent(this,"mainCounts",-1,1));
				}
				@Override
				void updateText(PropertyChangeEvent arg0) {
					if (!getEngine().getGlobalProperties().getBoolean("examplesImported", false)) {
						label.setText(getSummaryBean().getOpenCount()+" open items out of total "+getSummaryBean().getTotalCount()+" items. From Help menu you can import file with example lists.");
						button.setVisible(true);
					} else {
						label.setText(getSummaryBean().getOpenCount()+" open items out of total "+getSummaryBean().getTotalCount()+" items.");
						button.setVisible(false);
						
					}
				}
			};
			overview.add(sl,new GridBagConstraints(0,row++,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(0,18,4,18),0,0));

			overview.add(new JPanel(),new GridBagConstraints(0,row++,1,1,1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(0,11,0,11),0,0));

		}

		return overview;
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
			
			JMenu jm= new JMenu("View");
			jm.add(getShowAllActionsMenuItem());
			jm.add(getShowClosedFoldersMenuItem());
			
			jm.add(new JSeparator());
			
			jm.add(getShowOverviewTabMenuItem());
			jm.add(getShowQuickCollectBarMenuItem());

			/*jm.add(new JSeparator());
			
			JMenu jmm= new JMenu("Look&Feel - need restart");
			JRadioButtonMenuItem jrbmi= new JRadioButtonMenuItem("System platform native L&F");
			jrbmi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			jrbmi.setSelected(true);
			jmm.add(jrbmi);
			ButtonGroup bg= new ButtonGroup();
			bg.add(jrbmi);
			jrbmi= new JRadioButtonMenuItem("Cross platform L&F");
			jrbmi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			jmm.add(jrbmi);
			bg.add(jrbmi);
			jm.add(jmm);*/
			jJMenuBar.add(jm);
			
			jJMenuBar.add(getHelpMenu());

		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("File");
			
			fileMenu.add(getSaveMenuItem());

			autoSaveMenuItem= new JCheckBoxMenuItem();
			autoSaveMenuItem.setText("Auto Save");
			autoSaveMenuItem.setSelected(getEngine().getGlobalProperties().getBoolean(GlobalProperties.AUTO_SAVE , true));
			//autoSaveMenuItem.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_save));
			autoSaveMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getEngine().setAutoSave(autoSaveMenuItem.isSelected());
					getEngine().getGlobalProperties().putProperty(GlobalProperties.AUTO_SAVE, autoSaveMenuItem.isSelected());
				}
			});
			fileMenu.add(autoSaveMenuItem);
			
			fileMenu.add(new JSeparator());
			
			JMenuItem jmi= new JMenuItem("Import...");
			//jmi.setIcon(ApplicationHelper.getIconLarge_Import());
			jmi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					importFile();
				}
			});
			fileMenu.add(jmi);
			
			jmi= new JMenuItem("Export...");
			//jmi.setIcon(ApplicationHelper.getIconLarge_Export());
			jmi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exportFile();
				}
			});
			fileMenu.add(jmi);

			fileMenu.add(new JSeparator());
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	private void importFile() {
		if (JFileChooser.APPROVE_OPTION==getFileChooser().showOpenDialog(getJFrame())) {
			
			try {
				getEngine().getGTDModel().importFile(getFileChooser().getSelectedFile());
				JOptionPane.showMessageDialog(getJFrame(), "File '"+getFileChooser().getSelectedFile()+"'succesfully imported.", "File import done", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getJFrame(), "Failed to import file '"+getFileChooser().getSelectedFile()+"'\nfor reason: "+e.getMessage(), "File import error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void exportFile() {
		if (JFileChooser.APPROVE_OPTION==getFileChooser().showSaveDialog(getJFrame())) {
			
			File f= getFileChooser().getSelectedFile();
			try {
				if (!f.getName().toLowerCase().endsWith(".xml")) {
					f= new File(f.toString()+".xml");
				}
				getEngine().getGTDModel().store(f);
				JOptionPane.showMessageDialog(getJFrame(), "Data succesfully exported to file '"+f+"'.", "File export done", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getJFrame(), "Failed to export to file '"+f+"'\nfor reason: "+e.getMessage(), "File export error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu();
			helpMenu.setText("Help");
			
			JMenuItem jmi= new JMenuItem("GTD-Box Home Page");
			jmi.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_browser));
			jmi.addActionListener(new ActionListener() {
			
				@Override
				public void actionPerformed(ActionEvent e) {
					URI uri;
					try {
						uri = new URI(getEngine().getConfiguration().getProperty("application.home.url", "http://gtd-free.sourceforge.net"));
						Desktop.getDesktop().browse(uri);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			helpMenu.add(jmi);
			
			jmi= new JMenuItem(getActionMap().get("importDialog"));
			helpMenu.add(jmi);

			helpMenu.add(getAboutMenuItem());
		}
		return helpMenu;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setText("Exit");
			exitMenuItem.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_exit));
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					close(false);
				}
			});
		}
		return exitMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem();
			aboutMenuItem.setText("About");
			aboutMenuItem.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_about));
			aboutMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JDialog aboutDialog = getAboutDialog();
					//aboutDialog.pack();
					Point loc = getJFrame().getLocation();
					loc.translate(20, 20);
					aboutDialog.setLocation(loc);
					aboutDialog.setVisible(true);
				}
			});
		}
		return aboutMenuItem;
	}

	/**
	 * This method initializes aboutDialog	
	 * 	
	 * @return javax.swing.JDialog
	 */
	private JDialog getAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = new JDialog(getJFrame(), true);
			aboutDialog.setTitle("About GTD-Box");
			Image i= ApplicationHelper.loadImage("splash48.png");
			ImageIcon ii= new ImageIcon(i);
			
			JTabbedPane jtp= new JTabbedPane();
			
			JPanel jp= new JPanel();
			jp.setLayout(new GridBagLayout());
			JLabel jl= new JLabel("GTD-Box",ii,SwingConstants.CENTER);
			jl.setIconTextGap(22);
			jl.setFont(jl.getFont().deriveFont((float)24));
			jp.add(jl, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, new Insets(11,11,11,11),0,0));
			String s= "Version "+getEngine().getConfiguration().getProperty("build.version")+" "+getEngine().getConfiguration().getProperty("build.type");
			jp.add(new JLabel(s,SwingConstants.CENTER), new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, new Insets(11,11,11,11),0,0));
			jp.add(new JLabel("Copyright © 2008 ikesan@users.sourceforge.net",SwingConstants.CENTER), new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, new Insets(11,11,11,11),0,0));
			jtp.addTab("About", jp);
			
			jp= new JPanel();
			jp.setLayout(new GridBagLayout());
			TableModel tm= new AbstractTableModel() {
				private static final long serialVersionUID = -8449423008172417278L;
				private String[] props;
				private String[] getProperties() {
					if (props==null) {
						props= System.getProperties().keySet().toArray(new String[System.getProperties().size()]);
						Arrays.sort(props);
					}
					return props;
				}
				@Override
				public String getColumnName(int column) {
					switch (column) {
						case 0:
							return "Property";
						case 1:
							return "Value";
						default:
							return null;
					}
				}
				public int getColumnCount() {
					return 2;
				}
				public int getRowCount() {
					return getProperties().length;
				}
				public Object getValueAt(int rowIndex, int columnIndex) {
					switch (columnIndex) {
						case 0:
							return getProperties()[rowIndex];
						case 1:
							return System.getProperty(getProperties()[rowIndex]);
						default:
							return null;
					}
				}
			};
			JTable jt= new JTable(tm);
			jp.add(new JScrollPane(jt), new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(11,11,11,11),0,0));
			jtp.addTab("System Properties", jp);
			
			jp= new JPanel();
			jp.setLayout(new GridBagLayout());
			JTextArea ta= new JTextArea();
			ta.setEditable(false);
			ta.setText(ApplicationHelper.loadLicense());
			ta.setCaretPosition(0);
			jp.add(new JScrollPane(ta),new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(11,11,11,11),0,0));
			jtp.addTab("License", jp);
			
			jp= new JPanel();
			jp.setLayout(new GridBagLayout());
			consistencyCheckText= new JTextArea();
			ta.setEditable(false);
			jp.add(new JScrollPane(consistencyCheckText),new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(11,11,4,11),0,0));
			
			JButton b= new JButton("Check Data Consistency");
			b.addActionListener(new ActionListener() {
			
				public void actionPerformed(ActionEvent e) {
					try {
						GTDModel.checkConsistency(getEngine().getGTDModel());
					} catch (ConsistencyException e1) {
						consistencyCheckText.setText(e1.toString());
						consistencyCheckText.setCaretPosition(0);
					}
					consistencyCheckText.setText("Data consistency proved to be OK.");
				}
			
			});
			jp.add(b,new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,11,11,11),0,0));

			jtp.addTab("Data Consistency", jp);
			
			aboutDialog.setContentPane(jtp);
			aboutDialog.setSize(500, 250);
			//aboutDialog.pack();
			aboutDialog.setLocationRelativeTo(getJFrame());
		}
		return aboutDialog;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem();
			saveMenuItem.setText("Save");
			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					Event.CTRL_MASK, true));
			saveMenuItem.setIcon(ApplicationHelper.getIcon(ApplicationHelper.icon_name_large_save));
			saveMenuItem.addActionListener(new ActionListener() {
			
				public void actionPerformed(ActionEvent e) {
					try {
						getEngine().save();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			
			});
		}
		return saveMenuItem;
	}
	
	public GTDBoxEngine getEngine() {
		if (engine == null) {
			try {
				engine = new GTDBoxEngine();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			} catch (FactoryConfigurationError e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return engine;
	}
	
	private JFileChooser getFileChooser() {
		if (fileChooser==null) {
			fileChooser= new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home", ".")));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileHidingEnabled(false);
			fileChooser.setFileFilter(new FileNameExtensionFilter("GTD-Box XML file","xml"));
		}
		return fileChooser;
	}
	
	private JCheckBoxMenuItem getShowAllActionsMenuItem() {
		if (showAllActions == null) {
			showAllActions = new JCheckBoxMenuItem(getActionMap().get(GlobalProperties.SHOW_ALL_ACTIONS));
			showAllActions.setSelected(getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_ALL_ACTIONS));
			getEngine().getGlobalProperties().addPropertyChangeListener(GlobalProperties.SHOW_ALL_ACTIONS, new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					showAllActions.setSelected(getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_ALL_ACTIONS));
				}
			});
		}
		return showAllActions;
	}

	private JCheckBoxMenuItem getShowClosedFoldersMenuItem() {
		if (showClosedFolders == null) {
			showClosedFolders = new JCheckBoxMenuItem(getActionMap().get(GlobalProperties.SHOW_CLOSED_FOLDERS));
			showClosedFolders.setSelected(getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_CLOSED_FOLDERS));
			getEngine().getGlobalProperties().addPropertyChangeListener(GlobalProperties.SHOW_CLOSED_FOLDERS, new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					showClosedFolders.setSelected(getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_CLOSED_FOLDERS));
				}
			});
		}
		return showClosedFolders;
	}

	private JCheckBoxMenuItem getShowOverviewTabMenuItem() {
		if (showOverviewTab == null) {
			showOverviewTab = new JCheckBoxMenuItem(getActionMap().get(GlobalProperties.SHOW_OVERVIEW_TAB));
			showOverviewTab.setSelected(getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_OVERVIEW_TAB,true));
			getEngine().getGlobalProperties().addPropertyChangeListener(GlobalProperties.SHOW_OVERVIEW_TAB, new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					showOverviewTab.setSelected(getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_OVERVIEW_TAB));
					
					if (getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_OVERVIEW_TAB)) {
						if (tabbedPane.getComponentAt(0)!=getOverviewPane()) {
							tabbedPane.insertTab("Overview", ApplicationHelper.getIcon(ApplicationHelper.icon_name_small_overview), getOverviewPane(), "", 0);
						}
					} else {
						if (tabbedPane.getComponentAt(0)==getOverviewPane()) {
							tabbedPane.remove(0);
						}
					}
				}
			});
		}
		return showOverviewTab;
	}

	private JCheckBoxMenuItem getShowQuickCollectBarMenuItem() {
		if (showQuickCollectBar == null) {
			showQuickCollectBar = new JCheckBoxMenuItem(getActionMap().get(GlobalProperties.SHOW_QUICK_COLLECT));
			showQuickCollectBar.setSelected(getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_QUICK_COLLECT));
			getEngine().getGlobalProperties().addPropertyChangeListener(GlobalProperties.SHOW_QUICK_COLLECT, new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					showQuickCollectBar.setSelected(getEngine().getGlobalProperties().getBoolean(GlobalProperties.SHOW_QUICK_COLLECT));
					enableQuickCollectPanel();
				}
			});
		}
		return showQuickCollectBar;
	}

	/**
	 * 
	 */
	private void store(GlobalProperties p) {
		p.putProperty("window.heght", jFrame.getSize().height);
		p.putProperty("window.width", jFrame.getSize().width);
		//p.putProperty("window.state", jFrame.getExtendedState());
		p.putProperty("window.position.x", jFrame.getLocation().x);
		p.putProperty("window.position.y", jFrame.getLocation().y);
		
		inBasketPane.store(p);
		processPane.store(p);
		organizePane.store(p);
		executePane.store(p);
	}
}
