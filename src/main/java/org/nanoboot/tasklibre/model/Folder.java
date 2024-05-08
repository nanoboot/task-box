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

package org.nanoboot.tasklibre.model;

import org.nanoboot.tasklibre.model.Action.Resolution;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Folder implements Iterable<Action> {

    private final List<Action> actions = new ArrayList<>();
    private final GTDModel parent;
    private final int id;
    private final EventListenerList listeners = new EventListenerList();
    private String name;
    private FolderType type;
    private Comparator<Action> comparator;
    private int openCount = 0;
    private boolean closed = false;
    private boolean suspentedForMultipleChanges;
    private String description;

    /**
     * @param parent
     * @param name
     */
    public Folder(GTDModel parent, int id, String name, FolderType type) {
        super();
        this.id = id;
        this.parent = parent;
        this.name = name;
        this.type = type;
    }

    /**
     * @return the comparator
     */
    public Comparator<Action> getComparator() {
        return comparator;
    }

    /**
     * @param comparator the comparator to set
     */
    public void setComparator(Comparator<Action> comparator) {
        this.comparator = comparator;
    }

    /**
     * @return the type
     */
    public FolderType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    void setType(FolderType type) {
        this.type = type;
    }

    public void addFolderListener(FolderListener l) {
        listeners.add(FolderListener.class, l);
    }

    public void removeFolderListener(FolderListener l) {
        listeners.remove(FolderListener.class, l);
    }

    private void fireElementAdded(Action i) {
        fireElementAdded(i, !isUserFolder() && !isInBucket());
    }

    private void fireElementAdded(Action i, boolean recycled) {
        if (i.isOpen()) {
            openCount++;
        }
        FolderEvent f = new FolderEvent(this, i, recycled);
        FolderListener[] l = listeners.getListeners(FolderListener.class);
        for (FolderListener listener : l) {
            try {
                listener.elementAdded(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void fireElementRemoved(Action i) {
        fireElementRemoved(i, !isUserFolder() && !isInBucket());
    }

    private void fireElementRemoved(Action i, boolean recycled) {
        if (i.isOpen()) {
            openCount--;
        }
        FolderEvent f = new FolderEvent(this, i, recycled);
        FolderListener[] l = listeners.getListeners(FolderListener.class);
        for (FolderListener listener : l) {
            try {
                listener.elementRemoved(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void fireElementModified(Action a, String property, Object oldVal,
            Object newVal) {
        fireElementModified(a, property, oldVal, newVal, false);
    }

    public void fireElementModified(Action a, String property, Object oldVal,
            Object newVal, boolean recycled) {
        if ((oldVal == null && newVal == null) || (oldVal != null && oldVal
                .equals(newVal))) {
            return;
        }
        fireElementModified(
                new ActionEvent(this, a, property, oldVal, newVal, recycled));
    }

    private void fireElementModified(ActionEvent i) {
        if (!actions.contains(i.getAction())) {
            return;
        }
        if (i.getProperty().equals("resolution")) {
            if (Resolution.OPEN == i.getOldValue()) {
                openCount--;
            } else if (Resolution.OPEN == i.getNewValue()) {
                openCount++;
            }
        }
        FolderListener[] l = listeners.getListeners(FolderListener.class);
        for (FolderListener listener : l) {
            try {
                listener.elementModified(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void fireOrderChanged() {
        FolderListener[] l = listeners.getListeners(FolderListener.class);
        for (FolderListener listener : l) {
            try {
                listener.orderChanged(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sort() {
        if (getComparator() != null && !suspentedForMultipleChanges) {
            actions.sort(getComparator());
        }
    }

    synchronized void add(int i, Action a) {
        if (actions.contains(a)) {
            return;
        }
        actions.add(i, a);
		if (!isMeta()) {
			a.setFolder(this);
		}
        sort();
        fireElementAdded(a);
    }

    synchronized void add(Action a) {
        if (actions.contains(a)) {
            return;
        }
        actions.add(a);
		if (!isMeta()) {
			a.setFolder(this);
		}
        sort();
        fireElementAdded(a);
    }

    synchronized boolean remove(Action i) {
        boolean b = actions.remove(i);
        if (b) {
            fireElementRemoved(i);
        }
        return b;
    }

    public void rename(String newName) {
        parent.renameFolder(this, newName);
    }

    public String getName() {
        return name;
    }

    void setName(String newName) {
        name = newName;
    }

    public Iterator<Action> iterator() {
        return actions.iterator();
    }

    public Action getActionByID(int id) {
        for (Action a : this) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    public GTDModel getParent() {
        return parent;
    }

    public int size() {
        return actions.size();
    }

    /**
     * Meta folder is folder which does to actually contain actions but provide folder-like
     * interface to group of actions with particular common feature.
     *
     * @return
     */
    public boolean isMeta() {
        return isBuildIn() || type == FolderType.PROJECT
               || type == FolderType.QUEUE;
    }

    public synchronized void moveUp(Action a) {
        int open = 0;
        for (int i = 0; i < actions.size(); i++) {
            if (a == actions.get(i)) {
                if (open == i) {
                    return;
                }
                actions.set(i, actions.get(open));
                actions.set(open, a);
                fireOrderChanged();
                return;
            }
            if (actions.get(i).isOpen()) {
                open = i;
            }
        }
    }

    public boolean canMoveUp(Action a) {
        if (a == null) {
            return false;
        }
        for (Action action : actions) {
            if (a == action) {
                return false;
            }
            if (action.isOpen()) {
                return true;
            }
        }
        return false;
    }

    public synchronized void moveDown(Action a) {
        int open = actions.size() - 1;
        for (int i = actions.size() - 1; i > -1; i--) {
            if (a == actions.get(i)) {
                if (open == i) {
                    return;
                }
                actions.set(i, actions.get(open));
                actions.set(open, a);
                fireOrderChanged();
                return;
            }
            if (actions.get(i).isOpen()) {
                open = i;
            }
        }
    }

    public boolean canMoveDown(Action a) {
        if (a == null) {
            return false;
        }
        for (int i = actions.size() - 1; i > -1; i--) {
            if (a == actions.get(i)) {
                return false;
            }
            if (actions.get(i).isOpen()) {
                return true;
            }
        }
        return false;
    }

    public Action get(int i) {
        return actions.get(i);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Folder{ id= ");
        sb.append(id);
        sb.append(", name= ");
        sb.append(name);
        sb.append(", type= ");
        sb.append(type);
        sb.append(", actions= ");
        sb.append(actions);
        sb.append("}");
        return sb.toString();
    }

    public synchronized Action[] actions() {
        return actions.toArray(new Action[0]);
    }

    /**
     * @return the openCount
     */
    public int getOpenCount() {
        return openCount;
    }

    public synchronized void visit(Visitor v) {
        v.meet(this);
        for (Action a : actions) {
            v.meet(a);
        }
        v.depart(this);
    }

    public synchronized int indexOf(Action selectedAction) {
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i) == selectedAction) {
                return i;
            }
        }
        return -1;
    }

    public int getId() {
        return id;
    }

    public boolean isBuildIn() {
        return type == FolderType.BUILDIN || type == FolderType.BUILDIN_REMIND
               || type == FolderType.BUILDIN_RESOLVED
               || type == FolderType.BUILDIN_DELETED
               || type == FolderType.BUILDIN_PRIORITY;
    }

    public boolean contains(Action a) {
        return actions.contains(a);
    }

    /**
     * Returns true if folder is ACTION or REFERENCE or SOMEDAY.
     *
     * @return true if folder is ACTION or REFERENCE or SOMEDAY
     */
    public boolean isUserFolder() {
        return type == FolderType.ACTION || type == FolderType.REFERENCE
               || type == FolderType.SOMEDAY;
    }

    public boolean isProject() {
        return type == FolderType.PROJECT;
    }

    public boolean isInBucket() {
        return type == FolderType.INBUCKET;
    }

    /**
     * @return the closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @param closed the closed to set
     */
    public void setClosed(boolean closed) {
        if (this.closed == closed) {
            return;
        }
        this.closed = closed;
        parent.fireFolderModified(this, "closed", !closed, closed, false);
    }

    public boolean isSuspentedForMultipleChanges() {
        return suspentedForMultipleChanges;
    }

    public void setSuspentedForMultipleChanges(
            boolean suspentedForMultipleChanges) {
        this.suspentedForMultipleChanges = suspentedForMultipleChanges;
        if (!suspentedForMultipleChanges) {
            sort();
        }
    }

    public boolean isQueue() {
        return type == FolderType.QUEUE;
    }

    public boolean isReference() {
        return type == FolderType.REFERENCE;
    }

    public boolean isAction() {
        return type == FolderType.ACTION;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        if ((description != null && description.equals(d)) || (
                description == null && d == null)) {
            return;
        }
        String old = description;
        description = d;
        if (parent != null) {
            parent.fireFolderModified(this, "description", old, d, false);
        }
    }

    public int count(ActionFilter f) {
        int c = 0;
        for (Action a : actions) {
            if (f.isAcceptable(this, a)) {
                c++;
            }
        }
        return c;
    }

    public boolean isTickler() {
        return type == FolderType.BUILDIN_REMIND;
    }

    void purgeAll() {
        Action[] aa = actions();

        actions.clear();

        for (Action action : aa) {
            action.getFolder().remove(action);
            fireElementRemoved(action);
        }

    }

    public enum FolderType {
        INBUCKET, ACTION, REFERENCE, BUILDIN, PROJECT, QUEUE, BUILDIN_REMIND,
        BUILDIN_RESOLVED, BUILDIN_PRIORITY, SOMEDAY, BUILDIN_DELETED
    }

}
