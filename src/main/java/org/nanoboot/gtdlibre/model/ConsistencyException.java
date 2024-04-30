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

package org.nanoboot.gtdlibre.model;

/**
 * @author ikesan
 */
public class ConsistencyException extends Exception {

    private static final long serialVersionUID = 1L;

    private Action[] actions;
    private Folder[] folders;
    private Project[] projects;

    /**
     * @param message
     */
    public ConsistencyException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ConsistencyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ConsistencyException(String message, Action[] a, Folder[] f,
            Project[] p) {
        super(message);
        actions = a;
        folders = f;
        projects = p;
    }

    /**
     * @return the actions
     */
    public Action[] getActions() {
        return actions;
    }

    /**
     * @return the folders
     */
    public Folder[] getFolders() {
        return folders;
    }

    /**
     * @return the projects
     */
    public Project[] getProjects() {
        return projects;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(getMessage());
        sb.append('\n');
        sb.append('\n');
        if (actions != null) {
            if (actions.length == 1) {
                sb.append("Action:\n");
                append(sb, actions[0]);
            } else {
                sb.append("Actions:\n");
                for (Action action : actions) {
                    append(sb, action);
                }
            }
            sb.append('\n');
        }
        if (folders != null) {
            if (folders.length == 1) {
                sb.append("List:\n");
                append(sb, folders[0]);
            } else {
                sb.append("Lists:\n");
                for (Folder folder : folders) {
                    append(sb, folder);
                }
            }
            sb.append('\n');
        }
        if (projects != null) {
            if (projects.length == 1) {
                sb.append("Project:\n");
                append(sb, projects[0]);
            } else {
                sb.append("Projects:\n");
                for (Project project : projects) {
                    append(sb, project);
                }
            }
            sb.append('\n');
        }

        return super.toString();
    }

    private void append(StringBuilder sb, Action a) {
        sb.append("ID=");
        sb.append(a.getId());
        sb.append(" status=");
        sb.append(a.getResolution());
        sb.append(" desc='");
        if (a.getDescription() != null) {
            if (a.getDescription().length() > 15) {
                sb.append(a.getDescription(), 0, 11);
                sb.append("...'\n");
            } else {
                sb.append(a.getDescription());
                sb.append("'\n");
            }
        } else {
            sb.append("'\n");
        }
    }

    private void append(StringBuilder sb, Folder a) {
        sb.append("ID=");
        sb.append(a.getId());
        sb.append(" name=");
        sb.append(a.getName());
        sb.append(" type=");
        sb.append(a.getType());
        sb.append('\n');
    }

    private void append(StringBuilder sb, Project a) {
        sb.append("ID=");
        sb.append(a.getId());
        sb.append(" name=");
        sb.append(a.getName());
        sb.append('\n');
    }

}
