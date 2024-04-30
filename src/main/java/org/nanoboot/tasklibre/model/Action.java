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

package org.nanoboot.tasklibre.model;

import java.net.URL;
import java.util.Date;

public final class Action {

    public static final String RESOLUTION_PROPERTY_NAME = "resolution";
    public static final String PROJECT_PROPERTY_NAME = "project";
    public static final String QUEUED_PROPERTY_NAME = "queued";
    public static final String REMIND_PROPERTY_NAME = "remind";
    public static final String PRIORITY_PROPERTY_NAME = "priority";
    private final int id;
    private final Date created;
    private Date resolved;
    private String description;
    private Resolution resolution = Resolution.OPEN;
    private Folder folder;
    private ActionType type;
    private URL url;
    private Date start;
    private Date remind;
    private Date due;
    private Integer project;
    private boolean queued = false;
    private Priority priority = Priority.None;
    public Action(int id, Date created, Date resolved, String description) {
        this.id = id;
        this.created = created;
        this.resolved = resolved;
        this.setDescription(description);
    }

    public boolean isOpen() {
        return getResolution() == Resolution.OPEN;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @return the owner
     */
    public Folder getFolder() {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    void setFolder(Folder folder) {
        this.folder = folder;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        if (description != null && description.equals(this.description)) {
            return;
        }
        if (this.description != null && this.description.equals(description)) {
            return;
        }
        String old = this.description;
        this.description = description;
		if (folder != null) {
			folder.fireElementModified(this, "description", old, description);
		}
    }

    /**
     * @return the resolution
     */
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * @param resolution the resolution to set
     */
    public void setResolution(Resolution resolution) {
        if (this.resolution == resolution) {
            return;
        }
        Resolution old = this.resolution;
        this.resolution = resolution;
        if (resolved == null && !isOpen()) {
            resolved = new Date();
        } else if (isOpen()) {
            resolved = null;
        }
		if (folder != null) {
			folder.fireElementModified(this, RESOLUTION_PROPERTY_NAME, old,
					resolution);
		}
    }

    /**
     * @return the resolved
     */
    public Date getResolved() {
        return resolved;
    }

    /**
     * @return the start
     */
    public Date getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Date start) {
        Date old = this.start;
        this.start = start;
		if (folder != null) {
			getFolder().fireElementModified(this, "start", old, start);
		}
    }

    /**
     * @return the type
     */
    public ActionType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(ActionType type) {
        if (this.type == type) {
            return;
        }
        ActionType old = this.type;
        this.type = type;
		if (folder != null) {
			getFolder().fireElementModified(this, "type", old, type);
		}
    }

    /**
     * @return the url
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(URL url) {
        if ((this.url == null && url == null) || (this.url != null && this.url
                .equals(url))) {
            return;
        }
        URL old = this.url;
        this.url = url;
		if (folder != null) {
			getFolder().fireElementModified(this, "url", old, url);
		}
    }

    /**
     * @return the priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(Priority priority) {
        if (this.priority == priority) {
            return;
        }
        Priority old = this.priority;
        this.priority = priority;
		if (folder != null) {
			getFolder().fireElementModified(this, PRIORITY_PROPERTY_NAME, old,
					priority);
		}

    }

    /**
     * @return the remind
     */
    public Date getRemind() {
        return remind;
    }

    /**
     * @param remind the remind to set
     */
    public void setRemind(Date remind) {
        Date old = this.remind;
        this.remind = remind;
		if (folder != null) {
			getFolder().fireElementModified(this, REMIND_PROPERTY_NAME, old,
					remind);
		}
    }

    /**
     * @return the due
     */
    public Date getDue() {
        return due;
    }

    /**
     * @param due the due to set
     */
    public void setDue(Date due) {
        Date old = this.due;
        this.due = due;
		if (folder != null) {
			getFolder().fireElementModified(this, "due", old, due);
		}
    }

    public void moveUp() {
        folder.moveUp(this);
    }

    public void moveDown() {
        folder.moveDown(this);
    }

    public boolean canMoveUp() {
        return folder.canMoveUp(this);
    }

    public boolean canMoveDown() {
        return folder.canMoveDown(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Action={id=");
        sb.append(id);
        sb.append(",resolution=");
        sb.append(resolution);
        sb.append("}");
        return sb.toString();
    }

    /**
     * @return the project
     */
    public Integer getProject() {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(Integer project) {
        if (project != null && project.equals(this.project)) {
            return;
        }
        if (this.project != null && this.project.equals(project)) {
            return;
        }
        Integer old = this.project;
        this.project = project;
		if (getFolder() != null) {
			getFolder().fireElementModified(this, PROJECT_PROPERTY_NAME, old,
					project);
		}
    }

    public void copy(Action a) {
        setDescription(a.getDescription());
        setDue(a.getDue());
        setPriority(a.getPriority());
        setRemind(a.getRemind());
        setResolution(a.getResolution());
        setStart(a.getStart());
        setType(a.getType());
        setUrl(a.getUrl());
        setQueued(a.isQueued());
    }

    /**
     * @return the queued
     */
    public boolean isQueued() {
        return queued;
    }

    /**
     * @param queued the queued to set
     */
    public void setQueued(boolean queued) {
        this.queued = queued;
		if (folder != null) {
			getFolder().fireElementModified(this, QUEUED_PROPERTY_NAME, !queued,
					queued);
		}
    }

    public boolean isResolved() {
        return resolution == Resolution.RESOLVED;
    }

    public boolean isDeleted() {
        return resolution == Resolution.DELETED;
    }

    public enum ActionType {Mail, Phone, Meet, Read, Watch}

    public enum Resolution {
        OPEN, DELETED, RESOLVED, STALLED;

        public static Resolution toResolution(String s) {
            if ("TRASHED".equalsIgnoreCase(s)) {
                return DELETED;
            }
            if ("RESOVED".equalsIgnoreCase(s)) {
                return RESOLVED;
            }
            return valueOf(s);
        }
    }
}
