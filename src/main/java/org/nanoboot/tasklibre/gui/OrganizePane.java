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

import org.nanoboot.tasklibre.ApplicationHelper;
import org.nanoboot.tasklibre.TaskLibreEngine;
import org.nanoboot.tasklibre.GlobalProperties;
import org.nanoboot.tasklibre.gui.ActionTable.CellAction;
import org.nanoboot.tasklibre.model.Folder;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

/**
 * @author ikesan
 */
public class OrganizePane extends JPanel {

    private static final long serialVersionUID = 1L;
    private FolderPanel folders;
    private ActionTable actions;
    private ActionPanel actionPanel;
    private TaskLibreEngine engine;
    private AbstractFilterPanel filterPanel;
    private JSplitPane split;
    private JSplitPane split1;
    private FoldingPanel actionsPanel;
    private JTextArea description;
    private boolean setting = false;
    private TickleFilterPanel ticlePanel;
    private AbstractAction purgeDeletedAction;
    private JButton purgeDeletedButton;

    public OrganizePane() {
        initialize();
    }

    public static void main(String[] args) {
        try {

            JFrame f = new JFrame();
            OrganizePane p = new OrganizePane();
            f.setContentPane(p);
            f.pack();
            f.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the engine
     */
    public TaskLibreEngine getEngine() {
        return engine;
    }

    /**
     * @param engine the engine to set
     */
    public void setEngine(TaskLibreEngine engine) {
        this.engine = engine;
        folders.setEngine(engine);
        actionPanel.setEngine(engine);
        filterPanel.setEngine(engine);
        actions.setEngine(engine);
        actions.setShowAll(engine.getGlobalProperties()
                .getBoolean(GlobalProperties.SHOW_ALL_ACTIONS));
        engine.getGlobalProperties()
                .addPropertyChangeListener(GlobalProperties.SHOW_ALL_ACTIONS,
                        evt -> {
                            Folder f = actions.getFolder();
                            if (f != null) {
                                actions.setShowAll(getEngine().getStateMachine()
                                                           .getShowAllActions(
                                                                   f.getType())
                                                   || getEngine()
                                                           .getGlobalProperties()
                                                           .getBoolean(
                                                                   GlobalProperties.SHOW_ALL_ACTIONS));
                            } else {
                                actions.setShowAll(OrganizePane.this.engine
                                        .getGlobalProperties().getBoolean(
                                                GlobalProperties.SHOW_ALL_ACTIONS));
                            }
                        });
        folders.setShowClosedFolders(engine.getGlobalProperties()
                .getBoolean(GlobalProperties.SHOW_CLOSED_FOLDERS));
        engine.getGlobalProperties()
                .addPropertyChangeListener(GlobalProperties.SHOW_CLOSED_FOLDERS,
                        evt -> folders.setShowClosedFolders(
                                OrganizePane.this.engine.getGlobalProperties()
                                        .getBoolean(
                                                GlobalProperties.SHOW_CLOSED_FOLDERS)));
    }

    private void initialize() {

        setLayout(new BorderLayout());

        split = new JSplitPane();
        add(split);

        JPanel jp = new JPanel();
        jp.setBorder(new TitledBorder("Lists"));
        jp.setLayout(new GridBagLayout());

        folders = new FolderPanel();
        folders.addPropertyChangeListener("selectedFolder", evt -> {
            Folder f = folders.getSelectedFolder();
            if (f != null) {
                if (f.getType() == Folder.FolderType.INBUCKET) {
                    actions.setCellAction(CellAction.DELETE);
                } else {
                    actions.setCellAction(CellAction.RESOLVE);
                }
                actions.setCellAction(CellAction.RESOLVE);
                actions.setFolder(f,
                        engine.getStateMachine().getShowAllActions(f.getType())
                        || engine.getGlobalProperties()
                                .getBoolean(GlobalProperties.SHOW_ALL_ACTIONS));
                if (f.isProject()) {
                    actionsPanel.setTitle("Project: " + f.getName());
                } else {
                    actionsPanel.setTitle("List: " + f.getName());
                }
                setting = true;
                description.setText(f.getDescription());
                setting = false;
                actionPanel.setReopenButtonVisible(
                        f.getType() == Folder.FolderType.BUILDIN_DELETED
                        || f.getType() == Folder.FolderType.BUILDIN_RESOLVED);
            } else {
                actions.setFolder(f, engine.getGlobalProperties()
                        .getBoolean(GlobalProperties.SHOW_ALL_ACTIONS));
                description.setText("");
                actionsPanel.setTitle("");
                actions.setCellAction(CellAction.RESOLVE);
                actionPanel.setReopenButtonVisible(false);
            }
            description.setEditable(
                    f != null && (f.isUserFolder() || f.isProject()));
            purgeDeletedButton.setVisible(
                    f == getEngine().getGTDModel().getDeletedFolder());
        });
        jp.add(folders, new GridBagConstraints(0, 0, 2, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        split.setLeftComponent(jp);

        split1 = new JSplitPane();
        split1.setResizeWeight(1.0);
        split.setRightComponent(split1);
        split.setDividerLocation(250);

        actions = new ActionTable();
        actions.setMoveEnabled(true);
        actions.setCellAction(CellAction.RESOLVE);
        actions.addPropertyChangeListener("selectedAction",
                evt -> actionPanel.setAction(actions.getSelectedAction()));
        //jsppp.setBackground(actions.getCellRenderer(0,0).getTableCellRendererComponent(actions, ApplicationHelper.EMPTY_STRING, false, false, 0, 0).getBackground());
        //jsppp.setOpaque(false);

        jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        //jp.setBorder(new TitledBorder("Selected List"));

        actionsPanel = new FoldingPanel();

        JPanel dp = new JPanel();
        dp.setLayout(new GridBagLayout());
        dp.add(new JLabel("Description:"),
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

        description = new JTextArea();
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setMargin(new Insets(2, 4, 2, 4));
        description.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(description.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update(description.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(description.getText());
            }

            private void update(String d) {
                if (setting || actions.getFolder() == null) {
                    return;
                }
                setting = true;
                actions.getFolder().setDescription(d);
                setting = false;
            }
        });
        JScrollPane jsp = new JScrollPane();
        jsp.setViewportView(description);
        jsp.setPreferredSize(new Dimension(100,
                description.getFont().getSize() * 3 + 7 + 8));
        jsp.setMinimumSize(jsp.getPreferredSize());
        dp.add(jsp, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        actionsPanel.addFold("Description", dp, true, false);

        filterPanel = new FilterPanel();
        filterPanel.setTable(actions);
        actionsPanel.addFold("Filter", filterPanel, false, false);

        ticlePanel = new TickleFilterPanel();
        ticlePanel.setTable(actions);
        actionsPanel.addFold("Tickler", ticlePanel, false, false);

        jp.add(actionsPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 2, 0, 2), 0, 0));

        JScrollPane jsppp = new JScrollPane(actions);
        jp.add(jsppp, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));

        JPanel jpp = new JPanel();
        jpp.setLayout(new GridBagLayout());

        purgeDeletedButton = new JButton();
        purgeDeletedButton.setAction(getPurgeDeletedAction());
        purgeDeletedButton.setVisible(false);
        jpp.add(purgeDeletedButton, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(1, 1, 1, 1), 0, 0));
        jp.add(jpp, new GridBagConstraints(0, 2, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        split1.setLeftComponent(jp);

        actionPanel = new ActionPanel();
        actionPanel.putActions(actions.getActionMap());
        actionPanel.setBorder(new TitledBorder("Selected Action"));
        split1.setRightComponent(actionPanel);
        //jsp1.setDividerLocation(jsp1.getWidth()-500);

    }

    private Action getPurgeDeletedAction() {
        if (purgeDeletedAction == null) {
            purgeDeletedAction =
                    new AbstractAction("Remove All Deleted Actions") {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            getEngine().getGTDModel().purgeDeletedActions();
                        }
                    };
            purgeDeletedAction.putValue(Action.LARGE_ICON_KEY, ApplicationHelper
                    .getIcon(ApplicationHelper.icon_name_large_delete));
            purgeDeletedAction.putValue(Action.SHORT_DESCRIPTION,
                    "List with deleted actions will be unconditionally emptied, deleted actions completely removed, no undo possible.");
        }

        return purgeDeletedAction;
    }

    public void store(GlobalProperties p) {
        p.putProperty("organize.dividerLocation1", split.getDividerLocation());
        p.putProperty("organize.dividerLocation2", split1.getDividerLocation());
        p.putProperty("organize.tree.openNodes", folders.getExpendedNodes());
        p.putProperty("organize.tree.foldingStates",
                folders.getFoldingStates());
    }

    public void restore(GlobalProperties p) {
        Integer i = p.getInteger("organize.dividerLocation1");
        if (i != null) {
            split.setDividerLocation(i);
        }
        i = p.getInteger("organize.dividerLocation2");
        if (i != null) {
            split1.setDividerLocation(i);
        }
        int[] ii = p.getIntegerArray("organize.tree.openNodes");
        if (ii != null) {
            folders.setExpendedNodes(ii);
        }
        boolean[] bb = p.getBooleanArray("organize.tree.foldingStates");
        if (bb != null) {
            folders.setFoldingStates(bb);
        }
    }

    public void openTicklerForPast() {
        folders.setSelectedFolder(getEngine().getGTDModel().getRemindFolder());
        actionsPanel.setFoldingState("Tickler", true);
        ticlePanel.selectPast();
    }

    public void openTicklerForToday() {
        folders.setSelectedFolder(getEngine().getGTDModel().getRemindFolder());
        actionsPanel.setFoldingState("Tickler", true);
        ticlePanel.selectToday();
    }

}
