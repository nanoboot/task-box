/*
 *    Copyright (C) 2008 Igor Kriznar Copyright (C) 2024 Robert Vokac
 *
 *    This file is part of GTD-Free.
 *
 *    GTD-Free is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    GTD-Free is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with GTD-Free.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nanoboot.gtdfree.gui;

import org.nanoboot.gtdfree.ApplicationHelper;
import org.nanoboot.gtdfree.GTDFreeEngine;
import org.nanoboot.gtdfree.model.GTDModel;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/**
 * @author ikesan
 */
public class ImportExampleDialog {

    private JDialog dialog;
    private JRadioButton serverRadio;
    private JRadioButton localRadio;
    private GTDFreeEngine engine;

    public static void main(String[] args) {
        ImportExampleDialog id = new ImportExampleDialog();
        id.getDialog(null).setVisible(true);
    }

    public JDialog getDialog(final Frame owner) {
        if (dialog == null) {
            dialog = new JDialog(owner, true);
            dialog.setTitle("Import Example XML");

            JPanel p = new JPanel();
            p.setLayout(new GridBagLayout());

            int row = 0;

            JLabel l = new JLabel(
                    "Import GTD-Free database file with demo list and some helpful actions.");
            p.add(l, new GridBagConstraints(0, row++, 2, 1, 1, 0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(11, 11, 11, 11), 0, 0));

            ButtonGroup bg = new ButtonGroup();

            serverRadio = new JRadioButton();
            serverRadio.setText("Import example file from server.");
            bg.add(serverRadio);
            p.add(serverRadio, new GridBagConstraints(0, row++, 2, 1, 1, 0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(4, 11, 0, 11), 0, 0));

            l = new JLabel(
                    "Download and import latest examples from GTD-Free web site.");
            l.setFont(l.getFont().deriveFont(Font.ITALIC));
            p.add(l, new GridBagConstraints(0, row++, 2, 1, 1, 0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(4, 33, 4, 33), 0, 0));

            localRadio = new JRadioButton();
            localRadio.setText("Import local example file.");
            bg.add(localRadio);
            p.add(localRadio, new GridBagConstraints(0, row++, 2, 1, 1, 0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(4, 11, 0, 11), 0, 0));

            l = new JLabel(
                    "Imports local examples from installation, works off-line.");
            l.setFont(l.getFont().deriveFont(Font.ITALIC));
            p.add(l, new GridBagConstraints(0, row++, 2, 1, 1, 0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(4, 33, 4, 33), 0, 0));

            serverRadio.setSelected(true);

            JButton b = new JButton();
            b.setText("Import");
            b.addActionListener(e -> {
                dialog.dispose();

                ImportThread im =
                        new ImportThread(serverRadio.isSelected(), owner);
                im.start();

            });
            p.add(b, new GridBagConstraints(0, row, 1, 1, 1, 0,
                    GridBagConstraints.EAST, GridBagConstraints.NONE,
                    new Insets(11, 11, 11, 4), 0, 0));

            b = new JButton();
            b.setText("Cancel");
            b.addActionListener(e -> dialog.dispose());
            p.add(b, new GridBagConstraints(1, row, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(11, 4, 11, 11), 0, 0));

            dialog.setContentPane(p);
            dialog.pack();
            dialog.setResizable(false);
            dialog.setLocationRelativeTo(owner);

        }
        return dialog;
    }

    public void setEngine(GTDFreeEngine e) {
        engine = e;
    }

    class ImportThread extends Thread {

        private final boolean server;
        private final Frame owner;
        private ProgressMonitor monitor;
        private GTDModel model;

        public ImportThread(boolean server, Frame owner) {
            this.server = server;
            this.owner = owner;
        }

        @Override
        public void run() {
            try {
                monitor = new ProgressMonitor(owner, "Loading example xml.", "",
                        0, 3);
                monitor.setMillisToDecideToPopup(0);
                monitor.setMillisToPopup(0);

                importExample(server);

                if (!monitor.isCanceled()) {
                    monitor.close();
                    engine.getGlobalProperties()
                            .putProperty("examplesImported", true);
                    JOptionPane.showMessageDialog(owner,
                            "Example successfully file imported", "Import",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                monitor.close();
                JOptionPane.showMessageDialog(owner,
                        "Failed to import example file: " + e.getMessage(),
                        "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void importExample(boolean server)
                throws IOException, XMLStreamException,
                FactoryConfigurationError {

            InputStream example = null;

            if (server) {
                monitor.setNote(
                        "Contacting http://gtd-free.sourceforge.net/...");
                monitor.setProgress(0);

                String page =
                        engine.getConfiguration().getProperty("example.url");
                URL url = new URL(page);
                BufferedReader rr = new BufferedReader(
                        new InputStreamReader(url.openStream()));

                try {
                    sleep(3000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                if (monitor.isCanceled()) {
                    return;
                }

                try {
                    while (rr.ready()) {
                        if (monitor.isCanceled()) {
                            return;
                        }
                        String l = rr.readLine();
                        if (example == null) {
                            int i = l.indexOf("id=\"example\"");
                            if (i > 0) {
                                l = l.substring(i + 19);
                                l = l.substring(0, l.indexOf('"'));
                                url = new URL(l);
                                example = url.openStream();
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw ex;
                } finally {
                    if (rr != null) {
                        try {
                            rr.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                InputStream is = ApplicationHelper.class.getClassLoader()
                        .getResourceAsStream(
                                "gtd-free-example.xml");
                if (is != null) {
                    example = is;
                }
            }

            if (example != null) {

                if (monitor.isCanceled()) {
                    try {
                        example.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                monitor.setNote("Loading example file...");
                monitor.setProgress(1);

                model = new GTDModel();
                model.load(example);

                try {
                    example.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (monitor.isCanceled()) {
                    return;
                }

                monitor.setNote("Importing example file...");
                monitor.setProgress(2);

                try {
                    SwingUtilities.invokeAndWait(() -> {

                        if (monitor.isCanceled()) {
                            return;
                        }
                        engine.getGTDModel().importData(model);

                    });
                } catch (InterruptedException | InvocationTargetException e1) {
                    e1.printStackTrace();
                }

            } else {
                throw new IOException("Failed to obtain remote example file.");
            }
        }
    }

}
