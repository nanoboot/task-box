/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *
 *    This file is part of Task Libre.
 *
 *    Task Libre is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Task Libre is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Task Libre.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.tasklibre.gui;

import org.nanoboot.tasklibre.TaskLibreEngine;
import org.nanoboot.tasklibre.GlobalProperties;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;

/**
 * @author ikesan
 */
public class ExecutePane extends JPanel {
    private static final long serialVersionUID = 1L;

    private ActionPanel actionPanel;
    private ActionTable queueTable;
    private TaskLibreEngine engine;

    private JSplitPane split;

    public ExecutePane() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        split = new JSplitPane();
        split.setOrientation(JSplitPane.VERTICAL_SPLIT);
        split.setOneTouchExpandable(true);

        actionPanel = new ActionPanel(false);
        actionPanel.setBorder(new TitledBorder("Next Action"));

        split.setTopComponent(actionPanel);

        queueTable = new ActionTable();
        queueTable.setCellAction(ActionTable.CellAction.RESOLVE);
        queueTable.setMoveEnabled(true);
        queueTable.addPropertyChangeListener(
                ActionTable.SELECTED_ACTION_PROPERTY_NAME,
                evt -> {
                    actionPanel.setAction(queueTable.getSelectedAction());
					/*if (queueTable.getSelectedAction()==null && queueTable.getRowCount()>0) {
						queueTable.getSelectionModel().setSelectionInterval(0, 0);
					}*/
                });

        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.setBorder(new TitledBorder("Next Action Queue"));
        jp.add(new JScrollPane(queueTable));

        split.setBottomComponent(jp);

        add(split);

        actionPanel.putActions(queueTable.getActionMap());
    }

    public TaskLibreEngine getEngine() {
        return engine;
    }

    public void setEngine(TaskLibreEngine engine) {
        this.engine = engine;
        queueTable.setEngine(engine);
        queueTable.setFolder(engine.getGTDModel().getQueue());
        actionPanel.setEngine(engine);
		/*if (queueTable.getSelectedAction()==null && queueTable.getRowCount()>0) {
			queueTable.getSelectionModel().setSelectionInterval(0, 0);
		}*/
    }

    public void store(GlobalProperties p) {
        p.putProperty("execute.dividerLocation", split.getDividerLocation());
    }

    public void restore(GlobalProperties p) {
        Integer i = p.getInteger("execute.dividerLocation");
        if (i != null) {
            split.setDividerLocation(i);
        }
    }
}
