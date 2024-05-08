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

import org.apache.commons.lang3.StringEscapeUtils;
import org.nanoboot.tasklibre.ApplicationHelper;
import org.nanoboot.tasklibre.model.Action.ActionType;
import org.nanoboot.tasklibre.model.Folder.FolderType;

import javax.swing.event.EventListenerList;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GTDModel implements Iterable<Folder> {

    private final static String EOL = "\n";
    private final static String SKIP = "  ";
    private final static String SKIPSKIP = "    ";
    private final Map<Integer, Folder> folders = new HashMap<>();
    private final Map<Integer, Project> projects = new HashMap<>();
    private final ModelListenerSupport support = new ModelListenerSupport();
    private int lastActionID = 0;
    private int lastFolderID = 0;
    private Folder resolved;
    private Folder deleted;
    private Folder reminder;
    private Folder inBucket;
    private boolean suspentedForMultipleChanges = false;
    private Folder queue;
    private Folder priority;
    public GTDModel() {
        createMetaFolders();
    }

    public static void main(String[] args) {
        GTDModel m = new GTDModel();

        //Folder f= m.createFolder("Input Bin", FolderType.NOTE);

        //Action id= m.createAction(f,"My stupid idea.");

        try {
            m.store(new File("out.xml"));

            m.load(new File("out.xml"));

            m.store(new File("out1.xml"));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void checkConsistency(GTDModel m)
            throws ConsistencyException {

        Map<Integer, Folder> ids2Folders = new HashMap<>();
        Map<Integer, Action> ids2Actions = new HashMap<>();
        Map<Action, Folder> actions2Folders = new HashMap<>();
        Map<Action, Integer> actions2Projects = new HashMap<>();
        Set<Action> resolved = new HashSet<>();

        for (Folder f : m) {
            if (ids2Folders.containsKey(f.getId())) {
                throw new ConsistencyException("Lists has same ID.", null,
                        new Folder[] {f, ids2Folders.get(f.getId())}, null);
            }
            ids2Folders.put(f.getId(), f);
            if (!f.isMeta()) {
                for (Action a : f) {
                    if (ids2Actions.containsKey(a.getId())) {
                        throw new ConsistencyException("Actions has same ID.",
                                new Action[] {a, ids2Actions.get(a.getId())},
                                null, null);
                    }
                    ids2Actions.put(a.getId(), a);
                    if (actions2Folders.containsKey(a)) {
                        throw new ConsistencyException(
                                "Action is in more than one list.",
                                new Action[] {a},
                                new Folder[] {f, actions2Folders.get(a)}, null);
                    }
                    actions2Folders.put(a, f);
                    if (a.getProject() != null) {
                        actions2Projects.put(a, a.getProject());
                    }
                    if (!a.isOpen()) {
                        resolved.add(a);
                    }
                }
            }
        }

        for (Folder f : m) {
            if (f.isBuildIn()) {
                for (Action a : f) {
                    if (!actions2Folders.containsKey(a)) {
                        throw new ConsistencyException(
                                "Action has no user defined folder, only default folder.",
                                new Action[] {a}, new Folder[] {f}, null);
                    }
                    if (m.getResolvedFolder().getId() == f.getId()) {
                        resolved.remove(a);
                    }
                }
            } else if (f.isProject()) {
                for (Action a : f) {
                    if (!actions2Folders.containsKey(a)) {
                        throw new ConsistencyException(
                                "Action has no user defined folder, only project folder.",
                                new Action[] {a}, new Folder[] {f}, null);
                    }
                }
                for (Action a : f) {
                    Integer p = actions2Projects.get(a);
                    if (p == null) {
                        throw new ConsistencyException(
                                "Action has no project set but is located in project folder.",
                                new Action[] {a}, null,
                                new Project[] {(Project) f});
                    }
                    if (p != f.getId()) {
                        throw new ConsistencyException(
                                "Action's project and action's project folder are not same.",
                                new Action[] {a}, new Folder[] {f},
                                new Project[] {m.getProject(p)});
                    }
                    actions2Projects.remove(a);
                }
            }
        }

        if (actions2Projects.size() > 0) {
            throw new ConsistencyException(
                    "Actions has project set, but not located in project folders.",
                    actions2Projects.keySet().toArray(
                            new Action[0]), null, null);
        }
        if (actions2Projects.size() > 0) {
            throw new ConsistencyException(
                    "Actions are resolved, but not located in resolved default folder.",
                    resolved.toArray(
                            new Action[0]), null, null);
        }

    }

    public Action createAction(Folder f, String desc) {
        Action id = new Action(++lastActionID, new Date(), null, desc);
        f.add(0, id);
        return id;
    }

    /**
     *
     */
    private void createMetaFolders() {
        resolved = createFolder(-1, "Resolved", FolderType.BUILDIN_RESOLVED);
        resolved.setDescription(
                "This is build-in list automatically filled with all resolved actions.");
        resolved.setComparator((o1, o2) -> o1.getId() - o2.getId());
        reminder = createFolder(-2, "Tickler", FolderType.BUILDIN_REMIND);
        reminder.setDescription(
                "This is build-in list automatically filled with all actions, which has reminder date set.");
        reminder.setComparator((o1, o2) -> {
            if (o1.getRemind() == null && o2.getRemind() == null) {
                return 0;
            }
            if (o1.getRemind() == null) {
                return -1;
            }
            if (o2.getRemind() == null) {
                return 1;
            }
            return o1.getRemind().compareTo(o2.getRemind());
        });
        inBucket = createFolder(-3, "In-Bucket", FolderType.INBUCKET);
        inBucket.setDescription(
                "This is build-in list where all thougths, ideas and actions are collected.\nProcess and empty this list regualry.");

        queue = createFolder(-4, "Next Actions", FolderType.QUEUE);
        queue.setDescription(
                "This is build-in list where you queue all actions, whcih should be processed immediatelly.\nQueue here only actions, which should be processed in next work period (hour or day) and switch to Execute tab. This will help you keep focus.");

        priority = createFolder(-5, "Priority", FolderType.BUILDIN_PRIORITY);
        priority.setDescription(
                "This is build-in list automatically filled with all actions, which has priority value set to level higher or equal to 'Low'.");
        priority.setComparator(
                (o1, o2) -> -o1.getPriority().compareTo(o2.getPriority()));
        deleted = createFolder(-6, "Deleted", FolderType.BUILDIN_DELETED);
        deleted.setDescription(
                "This is build-in list automatically filled with all deleted actions.");
        deleted.setComparator((o1, o2) -> o1.getId() - o2.getId());
    }

    public void addGTDModelListener(GTDModelListener l) {
        support.addlistener(l);
    }

    public void removeGTDModelListener(GTDModelListener l) {
        support.removelistener(l);
    }

    public synchronized Folder createFolder(String name, FolderType type) {
        return createFolder(++lastFolderID, name, type);
    }

    private synchronized Folder createFolder(int id, String name,
            FolderType type) {
        Folder f = folders.get(id);
        if (f == null) {
            if (lastFolderID < id) {
                lastFolderID = id;
            }
            if (type == FolderType.PROJECT) {
                Project p = new Project(this, id, name);
                f = p;
                projects.put(id, p);
            } else {
                f = new Folder(this, id, name, type);
            }
            f.addFolderListener(support);
            folders.put(id, f);
            support.folderAdded(f);
        }
        return f;
    }

    public synchronized void renameFolder(Folder f, String newName) {
        String o = f.getName();
        f.setName(newName);
        support.folderModified(f, "name", o, newName, false);
    }

    void fireFolderModified(Folder f, String p, Object o, Object n,
            boolean recycled) {
        support.folderModified(f, p, o, n, recycled);
    }

    public synchronized Folder removeFolder(String name) {
        Folder f = folders.remove(name);
        if (f != null) {
            f.removeFolderListener(support);
            support.folderRemoved(f);
        }
        return f;
    }

    public void store(File f)
            throws IOException, XMLStreamException, FactoryConfigurationError {
        BufferedOutputStream bw =
                new BufferedOutputStream(new FileOutputStream(f));
        store(bw);
        bw.close();
    }

    public void store(OutputStream out)
            throws IOException, XMLStreamException, FactoryConfigurationError {
        XMLStreamWriter w = XMLOutputFactory.newInstance()
                .createXMLStreamWriter(out, "UTF-8");

        w.writeStartDocument("UTF-8", "1.0");

        w.writeCharacters(EOL);
        w.writeCharacters(EOL);

        w.writeStartElement("gtd-data");
        w.writeAttribute("version", "2.2");
        w.writeAttribute("modified",
                ApplicationHelper.formatLongISO(new Date()));
        w.writeAttribute("lastActionID", Integer.toString(lastActionID));
        w.writeCharacters(EOL);
        w.writeCharacters(EOL);

        // Write folders

        Folder[] fn = folders();
        w.writeStartElement("lists");
        w.writeCharacters(EOL);

        for (Folder ff : fn) {
            if (ff.isMeta()) {
                continue;
            }
            w.writeCharacters(SKIP);
            w.writeStartElement("list");
            w.writeAttribute("id", String.valueOf(ff.getId()));
            w.writeAttribute("name", ff.getName());
            w.writeAttribute("type", ff.getType().toString());
            w.writeAttribute("closed", Boolean.toString(ff.isClosed()));
            if (!ff.isInBucket() && ff.getDescription() != null) {
                w.writeAttribute("description",
                        ApplicationHelper.escapeControls(ff.getDescription()));
            }
            w.writeCharacters(EOL);

            Action[] aa = ff.actions();
            for (Action a : aa) {
                w.writeCharacters(SKIPSKIP);
                w.writeStartElement("action");
                w.writeAttribute("id", Integer.toString(a.getId()));
                w.writeAttribute("created",
                        Long.toString(a.getCreated().getTime()));
                w.writeAttribute("resolution", a.getResolution().toString());
                if (a.getResolved() != null) {
                    w.writeAttribute("resolved",
                            Long.toString(a.getResolved().getTime()));
                }

                if (a.getDescription() != null) {
                    w.writeAttribute("description", ApplicationHelper
                            .escapeControls(a.getDescription()));
                }

				if (a.getStart() != null) {
					w.writeAttribute("start",
							Long.toString(a.getStart().getTime()));
				}
				if (a.getRemind() != null) {
					w.writeAttribute("remind",
							Long.toString(a.getRemind().getTime()));
				}
				if (a.getDue() != null) {
					w.writeAttribute("due",
							Long.toString(a.getDue().getTime()));
				}
				if (a.getType() != null) {
					w.writeAttribute("type", a.getType().toString());
				}
				if (a.getUrl() != null) {
					w.writeAttribute("url", a.getUrl().toString());
				}
				if (a.isQueued()) {
					w.writeAttribute("queued", Boolean.toString(a.isQueued()));
				}
				if (a.getProject() != null) {
					w.writeAttribute("project", a.getProject().toString());
				}
				if (a.getPriority() != null) {
					w.writeAttribute("priority", a.getPriority().toString());
				}
                w.writeEndElement();
                w.writeCharacters(EOL);
            }
            w.writeCharacters(SKIP);
            w.writeEndElement();
            w.writeCharacters(EOL);
        }
        w.writeEndElement();
        w.writeCharacters(EOL);

        // Write projects
        Project[] pn = projects();
        w.writeStartElement("projects");
        w.writeCharacters(EOL);

        for (Project ff : pn) {
            w.writeCharacters(SKIP);
            w.writeStartElement("project");
            w.writeAttribute("id", String.valueOf(ff.getId()));
            w.writeAttribute("name", ff.getName());
            w.writeAttribute("closed", String.valueOf(ff.isClosed()));

            if (ff.getDescription() != null) {
                w.writeAttribute("description",
                        ApplicationHelper.escapeControls(ff.getDescription()));
            }

            StringBuilder sb = new StringBuilder();
            Action[] aa = ff.actions();
            if (aa.length > 0) {
                sb.append(aa[0].getId());
            }
            for (int j = 1; j < aa.length; j++) {
                sb.append(",");
                sb.append(aa[j].getId());
            }
            w.writeAttribute("actions", sb.toString());
            w.writeEndElement();
            w.writeCharacters(EOL);
        }
        w.writeEndElement();
        w.writeCharacters(EOL);

        // Write queue
        Folder f = getQueue();
        w.writeStartElement("queue");
        w.writeAttribute("id", String.valueOf(f.getId()));
        w.writeAttribute("name", f.getName());

        StringBuilder sb = new StringBuilder();
        Action[] aa = f.actions();
        if (aa.length > 0) {
            sb.append(aa[0].getId());
        }
        for (int j = 1; j < aa.length; j++) {
            sb.append(",");
            sb.append(aa[j].getId());
        }
        w.writeAttribute("actions", sb.toString());
        w.writeEndElement();
        w.writeCharacters(EOL);

        // containers
        w.writeEndElement();
        w.writeEndDocument();

        w.flush();
        w.close();

    }

    public void load(File f) throws XMLStreamException, IOException {
        InputStream r = new FileInputStream(f);
        try {
            load(r);
        } finally {
            try {
                r.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DataHeader load(InputStream in)
            throws XMLStreamException, IOException {

        lastActionID = -1;
        lastFolderID = -1;
        folders.clear();
        projects.clear();
        createMetaFolders();

        setSuspentedForMultipleChanges(true);

        XMLStreamReader r;
        try {

            // buffer size is same as default in 1.6, we explicitly request it so, not to brake if defaut changes.
            BufferedInputStream bin = new BufferedInputStream(in, 8192);
            bin.mark(8191);

            Reader rr = new InputStreamReader(bin);
            CharBuffer b = CharBuffer.allocate(96);
            rr.read(b);
            b.position(0);
            //System.out.println(b);
            Pattern pattern = Pattern.compile("<\\?.*?encoding\\s*?=.*?\\?>",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(b);

            // reset back to start of file
            bin.reset();

            // we check if encoding is defined in xml, by the book encoding on r should be null if not defined in xml,
            // but in reality it can be arbitrary if not defined in xml. So we have to check ourselves.
            if (matcher.find()) {
                //System.out.println(matcher);
                // if defined, then XML parser will pick it up and use it
                r = XMLInputFactory.newInstance().createXMLStreamReader(bin);
                System.out.println("XML declared encoding: " + r.getEncoding()
                                   + ", system default encoding: " + Charset
                                           .defaultCharset());
            } else {
                //System.out.println(matcher);
                // if not defined, then we assume it is generated by task-libre version 0.4 or some local editor,
                // so we assume system default encoding.
                r = XMLInputFactory.newInstance()
                        .createXMLStreamReader(new InputStreamReader(bin));
                System.out.println(
                        "XML assumed system default encoding: " + Charset
                                .defaultCharset());
            }

            r.nextTag();
            if ("gtd-data".equals(r.getLocalName())) {
                DataHeader dh = new DataHeader(null,
                        r.getAttributeValue(null, "version"),
                        r.getAttributeValue(null, "modified"));
                if (dh.version != null) {
                    if (dh.version.equals("2.0")) {
                        r.nextTag();
                        _load_2_0(r);
                        return dh;
                    }
                }
                String s = r.getAttributeValue(null, "lastActionID");
                if (s != null) {
                    try {
                        lastActionID = Integer.parseInt(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (dh.version != null) {
                    if (dh.version.equals("2.1")) {
                        r.nextTag();
                        _load_2_1(r);
                        return dh;

                    }
                    if (dh.version.equals("2.2")) {
                        r.nextTag();
                        _load_2_2(r);
                        return dh;
                    }
                }
                throw new IOException(
                        "XML task-libre data with version number " + dh.version
                        + " can not be imported. Data version is newer then supported versions. Update your Task Libre application to latest version.");
            }

            _load_1_0(r);

            return null;

        } catch (XMLStreamException e) {
            if (e.getNestedException() != null) {
                e.getNestedException().printStackTrace();
            } else {
                e.printStackTrace();
            }
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            setSuspentedForMultipleChanges(false);
        }

    }

    private void _load_1_0(XMLStreamReader r) throws XMLStreamException {
        if (checkTagStart(r, "folders")) {

            r.nextTag();

            while (checkTagStart(r, "folder")) {
                String type = r.getAttributeValue(null, "type").trim();
                Folder ff = null;
                if ("NOTE".equals(type)) {
                    ff = getInBucketFolder();
                } else {
                    ff = createFolder(r.getAttributeValue(null, "name"),
                            FolderType.valueOf(type));
                }
                r.nextTag();

                while (checkTagStart(r, "action")) {
                    int i = Integer.parseInt(r.getAttributeValue(null, "id"));
                    Date cr = new Date(Long.parseLong(
                            r.getAttributeValue(null, "created")));
                    Date re = r.getAttributeValue(null, "resolved") == null ?
                            null : new Date(Long.parseLong(
                            r.getAttributeValue(null, "resolved")));
                    String d = r.getAttributeValue(null, "description");
                    if (d != null) {
                        d = d.replace("\\n", "\n");
                    }
                    Action a = new Action(i, cr, re, d);
                    a.setResolution(Action.Resolution.toResolution(
                            r.getAttributeValue(null, "resolution")));

                    String s = r.getAttributeValue(null, "start");
					if (s != null) {
						a.setStart(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "remind");
					if (s != null) {
						a.setRemind(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "due");
					if (s != null) {
						a.setDue(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "type");
					if (s != null) {
						a.setType(ActionType.valueOf(s));
					}

                    s = r.getAttributeValue(null, "url");
                    if (s != null) {
                        try {
                            a.setUrl(new URL(s));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    ff.add(a);
                    if (a.getId() > lastActionID) {
                        lastActionID = a.getId();
                    }
                    findTagEnd(r, "action");
                    r.nextTag();
                }

                findTagEnd(r, "folder");
                r.nextTag();
            }

        }
    }

    private void _load_2_0(XMLStreamReader r) throws XMLStreamException {

        HashMap<Integer, Action> withProject = new HashMap<>();
        HashMap<Integer, Action> queued = new HashMap<>();

        if (checkTagStart(r, "folders")) {

            r.nextTag();
            while (checkTagStart(r, "folder")) {
                Folder ff;
                String id = r.getAttributeValue(null, "id");
                if (id != null) {
                    ff = createFolder(Integer.parseInt(id),
                            r.getAttributeValue(null, "name"), FolderType
                                    .valueOf(
                                            r.getAttributeValue(null, "type")));
                } else {
                    String s = r.getAttributeValue(null, "type")
                            .replace("NOTE", "INBUCKET");
                    ff = createFolder(r.getAttributeValue(null, "name"),
                            FolderType.valueOf(s));
                }
                String s = r.getAttributeValue(null, "closed");
				if (s != null) {
					ff.setClosed(Boolean.parseBoolean(s));
				}
                s = r.getAttributeValue(null, "description");
                if (s != null) {
                    s = s.replace("\\n", "\n");
                }
                if (!ff.isInBucket()) {
                    ff.setDescription(s);
                }

                r.nextTag();

                while (checkTagStart(r, "action")) {
                    int i = Integer.parseInt(r.getAttributeValue(null, "id"));
                    Date cr = new Date(Long.parseLong(
                            r.getAttributeValue(null, "created")));
                    Date re = r.getAttributeValue(null, "resolved") == null ?
                            null : new Date(Long.parseLong(
                            r.getAttributeValue(null, "resolved")));
                    String d = r.getAttributeValue(null, "description");
                    if (d != null) {
                        d = d.replace("\\n", "\n");
                    }
                    Action a = new Action(i, cr, re, d);

                    s = r.getAttributeValue(null, "type");
					if (s != null) {
						a.setType(ActionType.valueOf(s));
					}

                    s = r.getAttributeValue(null, "url");
                    if (s != null) {
                        try {
                            a.setUrl(new URL(s));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    s = r.getAttributeValue(null, "start");
					if (s != null) {
						a.setStart(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "remind");
					if (s != null) {
						a.setRemind(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "due");
					if (s != null) {
						a.setDue(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "queued");
					if (s != null) {
						a.setQueued(Boolean.parseBoolean(s));
					}

                    s = r.getAttributeValue(null, "project");
					if (s != null) {
						a.setProject(Integer.parseInt(s));
					}

                    s = r.getAttributeValue(null, "priority");
					if (s != null) {
						a.setPriority(Priority.valueOf(s));
					}

                    ff.add(a);

                    a.setResolution(Action.Resolution.toResolution(
                            r.getAttributeValue(null, "resolution")));

                    if (a.getProject() != null) {
                        withProject.put(a.getId(), a);
                    }

                    if (a.isQueued()) {
                        queued.put(a.getId(), a);
                    }

                    if (a.getId() > lastActionID) {
                        lastActionID = a.getId();
                    }
                    findTagEnd(r, "action");
                    r.nextTag();
                }

                findTagEnd(r, "folder");
                r.nextTag();
            }
            findTagEnd(r, "folders");
            //r.nextTag();
        }

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }
        // read projects
        r.nextTag();

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }

        if (checkTagStart(r, "projects")) {

            r.nextTag();
            while (checkTagStart(r, "project")) {
                Project pp;
                String id = r.getAttributeValue(null, "id");
                if (id != null) {
                    pp = (Project) createFolder(Integer.parseInt(id),
                            r.getAttributeValue(null, "name"),
                            FolderType.PROJECT);
                } else {
                    pp = (Project) createFolder(
                            r.getAttributeValue(null, "name"),
                            FolderType.PROJECT);
                }
                pp.setClosed(Boolean.parseBoolean(
                        r.getAttributeValue(null, "closed")));
                pp.setGoal(r.getAttributeValue(null, "goal"));

                String s = r.getAttributeValue(null, "actions");

                if (s != null && s.trim().length() > 0) {
                    String[] ss = s.trim().split(",");
                    for (String value : ss) {
                        if (value.trim().length() > 0) {
                            int ii = Integer.parseInt(value.trim());
                            Action a = withProject.remove(ii);
                            if (a != null) {
                                pp.add(a);
                            }
                        }
                    }
                }
                r.nextTag();
                findTagEnd(r, "project");
                r.nextTag();
            }
            findTagEnd(r, "projects");
        }

        for (Action a : withProject.values()) {
            if (a.getProject() != null) {
                Project p = getProject(a.getProject());

                if (p != null) {
                    p.add(a);
                } else {
                    System.err.println("Project " + p + " in action " + a
                                       + " does not exsist.");
                    a.setProject(null);
                }
            }
        }

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }

        // read projects
        r.nextTag();

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }

        if (checkTagStart(r, "queue")) {
            Folder f = getQueue();

            String s = r.getAttributeValue(null, "actions");

            if (s != null && s.trim().length() > 0) {
                String[] ss = s.trim().split(",");
                for (String value : ss) {
                    if (value.trim().length() > 0) {
                        int ii = Integer.parseInt(value.trim());
                        Action a = queued.remove(ii);
                        if (a != null) {
                            f.add(a);
                        }
                    }
                }
            }
            r.nextTag();
            findTagEnd(r, "queue");
            r.nextTag();
        }

        for (Action a : queued.values()) {
            if (a.isQueued()) {
                System.err.println(
                        "Action " + a + " is queued but not in queue list.");
                getQueue().add(a);
            }
        }

    }

    private void _load_2_1(XMLStreamReader r) throws XMLStreamException {

        HashMap<Integer, Action> withProject = new HashMap<>();
        HashMap<Integer, Action> queued = new HashMap<>();

        if (checkTagStart(r, "lists")) {

            r.nextTag();
            while (checkTagStart(r, "list")) {
                Folder ff;
                String id = r.getAttributeValue(null, "id");
                if (id != null) {
                    ff = createFolder(Integer.parseInt(id),
                            r.getAttributeValue(null, "name"), FolderType
                                    .valueOf(
                                            r.getAttributeValue(null, "type")));
                } else {
                    String s = r.getAttributeValue(null, "type")
                            .replace("NOTE", "INBUCKET");
                    ff = createFolder(r.getAttributeValue(null, "name"),
                            FolderType.valueOf(s));
                }
                String s = r.getAttributeValue(null, "closed");
				if (s != null) {
					ff.setClosed(Boolean.parseBoolean(s));
				}
                s = r.getAttributeValue(null, "description");
                if (s != null) {
                    s = s.replace("\\n", "\n");
                }
                if (!ff.isInBucket()) {
                    ff.setDescription(s);
                }

                r.nextTag();

                while (checkTagStart(r, "action")) {
                    int i = Integer.parseInt(r.getAttributeValue(null, "id"));
                    Date cr = new Date(Long.parseLong(
                            r.getAttributeValue(null, "created")));
                    Date re = r.getAttributeValue(null, "resolved") == null ?
                            null : new Date(Long.parseLong(
                            r.getAttributeValue(null, "resolved")));
                    String d = r.getAttributeValue(null, "description");
                    if (d != null) {
                        d = d.replace("\\n", "\n");
                    }

                    Action a = new Action(i, cr, re, d);

                    s = r.getAttributeValue(null, "type");
					if (s != null) {
						a.setType(ActionType.valueOf(s));
					}

                    s = r.getAttributeValue(null, "url");
                    if (s != null) {
                        try {
                            a.setUrl(new URL(s));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    s = r.getAttributeValue(null, "start");
					if (s != null) {
						a.setStart(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "remind");
					if (s != null) {
						a.setRemind(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "due");
					if (s != null) {
						a.setDue(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "queued");
					if (s != null) {
						a.setQueued(Boolean.parseBoolean(s));
					}

                    s = r.getAttributeValue(null, "project");
					if (s != null) {
						a.setProject(Integer.parseInt(s));
					}

                    s = r.getAttributeValue(null, "priority");
					if (s != null) {
						a.setPriority(Priority.valueOf(s));
					}

                    ff.add(a);

                    a.setResolution(Action.Resolution.toResolution(
                            r.getAttributeValue(null, "resolution")));

                    if (a.getProject() != null) {
                        withProject.put(a.getId(), a);
                    }

                    if (a.isQueued()) {
                        queued.put(a.getId(), a);
                    }

                    if (a.getId() > lastActionID) {
                        lastActionID = a.getId();
                    }
                    findTagEnd(r, "action");
                    r.nextTag();
                }

                findTagEnd(r, "list");
                r.nextTag();
            }
            findTagEnd(r, "lists");
            //r.nextTag();
        }

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }
        // read projects
        r.nextTag();

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }

        if (checkTagStart(r, "projects")) {

            r.nextTag();
            while (checkTagStart(r, "project")) {
                Project pp;
                String id = r.getAttributeValue(null, "id");
                if (id != null) {
                    pp = (Project) createFolder(Integer.parseInt(id),
                            r.getAttributeValue(null, "name"),
                            FolderType.PROJECT);
                } else {
                    pp = (Project) createFolder(
                            r.getAttributeValue(null, "name"),
                            FolderType.PROJECT);
                }
                pp.setClosed(Boolean.parseBoolean(
                        r.getAttributeValue(null, "closed")));
                pp.setGoal(r.getAttributeValue(null, "goal"));

                String s = r.getAttributeValue(null, "actions");

                if (s != null && s.trim().length() > 0) {
                    String[] ss = s.trim().split(",");
                    for (String value : ss) {
                        if (value.trim().length() > 0) {
                            int ii = Integer.parseInt(value.trim());
                            Action a = withProject.remove(ii);
                            if (a != null) {
                                pp.add(a);
                            }
                        }
                    }
                }
                r.nextTag();
                findTagEnd(r, "project");
                r.nextTag();
            }
            findTagEnd(r, "projects");
        }

        for (Action a : withProject.values()) {
            if (a.getProject() != null) {
                Project p = getProject(a.getProject());

                if (p != null) {
                    p.add(a);
                } else {
                    System.err.println("Project " + p + " in action " + a
                                       + " does not exsist.");
                    a.setProject(null);
                }
            }
        }

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }

        // read projects
        r.nextTag();

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }

        if (checkTagStart(r, "queue")) {
            Folder f = getQueue();

            String s = r.getAttributeValue(null, "actions");

            if (s != null && s.trim().length() > 0) {
                String[] ss = s.trim().split(",");
                for (String value : ss) {
                    if (value.trim().length() > 0) {
                        int ii = Integer.parseInt(value.trim());
                        Action a = queued.remove(ii);
                        if (a != null) {
                            f.add(a);
                        }
                    }
                }
            }
            r.nextTag();
            findTagEnd(r, "queue");
            r.nextTag();
        }

        for (Action a : queued.values()) {
            if (a.isQueued()) {
                System.err.println(
                        "Action " + a + " is queued but not in queue list.");
                getQueue().add(a);
            }
        }

    }

    private void _load_2_2(XMLStreamReader r) throws XMLStreamException {

        HashMap<Integer, Action> withProject = new HashMap<>();
        HashMap<Integer, Action> queued = new HashMap<>();

        if (checkTagStart(r, "lists")) {

            r.nextTag();
            while (checkTagStart(r, "list")) {
                Folder ff;
                String id = r.getAttributeValue(null, "id");
                if (id != null) {
                    ff = createFolder(Integer.parseInt(id),
                            r.getAttributeValue(null, "name"), FolderType
                                    .valueOf(
                                            r.getAttributeValue(null, "type")));
                } else {
                    String s = r.getAttributeValue(null, "type")
                            .replace("NOTE", "INBUCKET");
                    ff = createFolder(r.getAttributeValue(null, "name"),
                            FolderType.valueOf(s));
                }
                String s = r.getAttributeValue(null, "closed");
				if (s != null) {
					ff.setClosed(Boolean.parseBoolean(s));
				}

                s = StringEscapeUtils
                        .unescapeJava(r.getAttributeValue(null, "description"));

                if (!ff.isInBucket()) {
                    ff.setDescription(s);
                }

                r.nextTag();

                while (checkTagStart(r, "action")) {
                    int i = Integer.parseInt(r.getAttributeValue(null, "id"));
                    Date cr = new Date(Long.parseLong(
                            r.getAttributeValue(null, "created")));
                    Date re = r.getAttributeValue(null, "resolved") == null ?
                            null : new Date(Long.parseLong(
                            r.getAttributeValue(null, "resolved")));

                    String d = StringEscapeUtils.unescapeJava(
                            r.getAttributeValue(null, "description"));

                    Action a = new Action(i, cr, re, d);

                    s = r.getAttributeValue(null, "type");
					if (s != null) {
						a.setType(ActionType.valueOf(s));
					}

                    s = r.getAttributeValue(null, "url");
                    if (s != null) {
                        try {
                            a.setUrl(new URL(s));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    s = r.getAttributeValue(null, "start");
					if (s != null) {
						a.setStart(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "remind");
					if (s != null) {
						a.setRemind(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "due");
					if (s != null) {
						a.setDue(new Date(Long.parseLong(s)));
					}

                    s = r.getAttributeValue(null, "queued");
					if (s != null) {
						a.setQueued(Boolean.parseBoolean(s));
					}

                    s = r.getAttributeValue(null, "project");
					if (s != null) {
						a.setProject(Integer.parseInt(s));
					}

                    s = r.getAttributeValue(null, "priority");
					if (s != null) {
						a.setPriority(Priority.valueOf(s));
					}

                    ff.add(a);

                    a.setResolution(Action.Resolution.toResolution(
                            r.getAttributeValue(null, "resolution")));

                    if (a.getProject() != null) {
                        withProject.put(a.getId(), a);
                    }

                    if (a.isQueued()) {
                        queued.put(a.getId(), a);
                    }

                    if (a.getId() > lastActionID) {
                        lastActionID = a.getId();
                    }
                    findTagEnd(r, "action");
                    r.nextTag();
                }

                findTagEnd(r, "list");
                r.nextTag();
            }
            findTagEnd(r, "lists");
            //r.nextTag();
        }

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }
        // read projects
        r.nextTag();

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }

        if (checkTagStart(r, "projects")) {

            r.nextTag();
            while (checkTagStart(r, "project")) {
                Project pp;
                String id = r.getAttributeValue(null, "id");
                if (id != null) {
                    pp = (Project) createFolder(Integer.parseInt(id),
                            r.getAttributeValue(null, "name"),
                            FolderType.PROJECT);
                } else {
                    pp = (Project) createFolder(
                            r.getAttributeValue(null, "name"),
                            FolderType.PROJECT);
                }
                pp.setClosed(Boolean.parseBoolean(
                        r.getAttributeValue(null, "closed")));
                pp.setGoal(r.getAttributeValue(null, "goal"));

                String s = StringEscapeUtils
                        .unescapeJava(r.getAttributeValue(null, "description"));
                if (s != null) {
                    pp.setDescription(s);
                }

                s = r.getAttributeValue(null, "actions");

                if (s != null && s.trim().length() > 0) {
                    String[] ss = s.trim().split(",");
                    for (String value : ss) {
                        if (value.trim().length() > 0) {
                            int ii = Integer.parseInt(value.trim());
                            Action a = withProject.remove(ii);
                            if (a != null) {
                                pp.add(a);
                            }
                        }
                    }
                }
                r.nextTag();
                findTagEnd(r, "project");
                r.nextTag();
            }
            findTagEnd(r, "projects");
        }

        for (Action a : withProject.values()) {
            if (a.getProject() != null) {
                Project p = getProject(a.getProject());

                if (p != null) {
                    p.add(a);
                } else {
                    System.err.println("Project " + p + " in action " + a
                                       + " does not exsist.");
                    a.setProject(null);
                }
            }
        }

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }

        // read projects
        r.nextTag();

        if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
            return;
        }

        if (checkTagStart(r, "queue")) {
            Folder f = getQueue();

            String s = r.getAttributeValue(null, "actions");

            if (s != null && s.trim().length() > 0) {
                String[] ss = s.trim().split(",");
                for (String value : ss) {
                    if (value.trim().length() > 0) {
                        int ii = Integer.parseInt(value.trim());
                        Action a = queued.remove(ii);
                        if (a != null) {
                            f.add(a);
                        }
                    }
                }
            }
            r.nextTag();
            findTagEnd(r, "queue");
            r.nextTag();
        }

        for (Action a : queued.values()) {
            if (a.isQueued()) {
                System.err.println(
                        "Action " + a + " is queued but not in queue list.");
                getQueue().add(a);
            }
        }

    }

    private boolean checkTagStart(XMLStreamReader r, String tag)
            throws XMLStreamException {
        return tag.equals(r.getLocalName())
               && r.getEventType() == XMLStreamReader.START_ELEMENT;
    }

    private void findTagEnd(XMLStreamReader r, String tag)
            throws XMLStreamException {
        while (!r.getLocalName().equals(tag) || XMLStreamReader.END_ELEMENT != r
                .getEventType()) {
            if (r.getEventType() == XMLStreamReader.END_DOCUMENT) {
                return;
            }
            r.nextTag();
        }
    }

    public Iterator<Folder> iterator() {
        return folders.values().iterator();
    }

    public int size() {
        return folders.size();
    }

    public Action getAction(int id) {
        for (Folder f : this) {
            Action a = f.getActionByID(id);
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    public boolean moveAction(Action action, Folder toFolder) {
        Folder f = action.getFolder();
        if (f != null && toFolder != null && f != toFolder && !toFolder
                .contains(action)) {
            toFolder.add(0, action);
            f.remove(action);
            return true;
        }
        return false;

    }

    public synchronized Folder[] folders() {
        return folders.values().toArray(new Folder[0]);
    }

    public synchronized Project[] projects() {
        return projects.values().toArray(new Project[0]);
    }

    public synchronized void visit(Visitor v) {
        for (Folder f : folders.values()) {
            f.visit(v);
        }
    }

    public Project getProject(int id) {
        return projects.get(id);
    }

    public Folder getFolder(int id) {
        return folders.get(id);
    }

    public Folder getInBucketFolder() {
        return inBucket;
    }

    public Folder getResolvedFolder() {
        return resolved;
    }

    public Folder getDeletedFolder() {
        return deleted;
    }

    public void importFile(File file)
            throws XMLStreamException, FactoryConfigurationError, IOException {
        InputStream r = new FileInputStream(file);
        try {
            importFile(r);
        } finally {
            try {
                r.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void importFile(InputStream in)
            throws XMLStreamException, FactoryConfigurationError, IOException {
        GTDModel m = new GTDModel();
        m.load(in);
        importData(m);
    }

    public void importData(GTDModel m) {

        Map<Integer, Integer> folderMap = new HashMap<>();
        Map<String, Folder> folderNames = new HashMap<>();

        for (Folder f : this) {
            folderNames.put(f.getName() + "TYPE" + f.getType(), f);
        }

        Folder[] pp = m.folders();
        for (Folder inP : pp) {
            if (inP.isBuildIn()) {
                continue;
            }
            Folder f = folderNames.get(inP.getName() + "TYPE" + inP.getType());
            if (f == null) {
                f = createFolder(inP.getName(), inP.getType());
                f.setDescription(inP.getDescription());
            } else {
                f.setClosed(false);
            }
            folderMap.put(inP.getId(), f.getId());
        }

        for (Folder inF : m) {
            if (!inF.isMeta()) {
                Folder f = getFolder(folderMap.get(inF.getId()));
                for (int i = inF.size() - 1; i > -1; i--) {
                    //for (int i= 0; i< inF.size(); i++) {
                    Action inA = inF.get(i);
                    Action a = createAction(f, inA.getDescription());
                    a.copy(inA);
                    a.setProject(folderMap.get(inA.getProject()));
                }
            }
        }
    }

    public int getLastActionID() {
        return lastActionID;
    }

    public boolean isSuspentedForMultipleChanges() {
        return suspentedForMultipleChanges;
    }

    public void setSuspentedForMultipleChanges(
            boolean suspentedForMultipleChanges) {
        this.suspentedForMultipleChanges = suspentedForMultipleChanges;
        reminder.setSuspentedForMultipleChanges(suspentedForMultipleChanges);
        resolved.setSuspentedForMultipleChanges(suspentedForMultipleChanges);
        deleted.setSuspentedForMultipleChanges(suspentedForMultipleChanges);
        queue.setSuspentedForMultipleChanges(suspentedForMultipleChanges);
        priority.setSuspentedForMultipleChanges(suspentedForMultipleChanges);
    }

    public Folder getQueue() {
        return queue;
    }

    public Folder getRemindFolder() {
        return reminder;
    }

    public Folder getPriorityFolder() {
        return priority;
    }

    public void purgeDeletedActions() {
        deleted.purgeAll();
    }

    public static class DataHeader {
        private final File file;
        private String version;
        private Date modified;
        public DataHeader(File file, String ver, String mod) {
            this.file = file;
            version = ver;
            if (mod != null) {
                try {
                    modified = ApplicationHelper.parseLongISO(mod);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        public DataHeader(File f)
                throws FileNotFoundException, XMLStreamException,
                javax.xml.stream.FactoryConfigurationError {
            file = f;

            InputStream in = null;
            XMLStreamReader r = null;
            try {

                in = new BufferedInputStream(new FileInputStream(f));
                r = XMLInputFactory.newInstance().createXMLStreamReader(in);
                r.nextTag();

                if ("gtd-data".equals(r.getLocalName())) {
                    version = r.getAttributeValue(null, "version");
                    try {
                        modified = ApplicationHelper.parseLongISO(
                                r.getAttributeValue(null, "modified"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (modified == null) {
                        modified = new Date(f.lastModified());
                    }
                }

            } finally {
                if (r != null) {
                    try {
                        r.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }

        /**
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        /**
         * @return the modified
         */
        public Date getModified() {
            return modified;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return file.toString() + " " + ApplicationHelper
                    .toISODateTimeString(modified) + " " + version;
        }
    }

    class ModelListenerSupport implements GTDModelListener {
        final EventListenerList listeners = new EventListenerList();

        public void addlistener(GTDModelListener l) {
            listeners.add(GTDModelListener.class, l);
        }

        public void removelistener(GTDModelListener l) {
            listeners.remove(GTDModelListener.class, l);
        }

        void checkEvent(ActionEvent e) {
            if (e.getNewValue() == e.getOldValue()) {
                throw new RuntimeException(
						"Internal error, property not changed: " + e);
            }
        }

        void updateMetaAdd(FolderEvent a) {
            if (a.getAction().isResolved()) {
                resolved.add(a.getAction());
            }
            if (a.getAction().isDeleted()) {
                deleted.add(a.getAction());
            }
            if (a.getAction().getRemind() != null) {
                reminder.add(a.getAction());
            }
            if (a.getAction().getPriority() != null
                && a.getAction().getPriority() != Priority.None) {
                priority.add(a.getAction());
            }
            if (a.getAction().isQueued() && !suspentedForMultipleChanges) {
                queue.add(a.getAction());
            }
            if (a.getAction().getProject() != null
                && getProject(a.getAction().getProject()) != null) {
                getProject(a.getAction().getProject()).add(a.getAction());
            }
        }

        private Project getProject(Integer project) {
            return projects.get(project);
        }

        void updateMetaRemove(FolderEvent a) {
            if (a.getFolder() != deleted && a.getAction().isDeleted()) {
                deleted.remove(a.getAction());
            }
            if (a.getFolder() == deleted) {
                if (a.getAction().getRemind() != null) {
                    reminder.remove(a.getAction());
                }
                if (a.getAction().getPriority() != null
                    && a.getAction().getPriority() != Priority.None) {
                    priority.remove(a.getAction());
                }
                if (a.getAction().isQueued()) {
                    queue.remove(a.getAction());
                }
                if (a.getAction().getProject() != null
                    && getProject(a.getAction().getProject()) != null) {
                    getProject(a.getAction().getProject())
                            .remove(a.getAction());
                }
            }
			/*if (!a.getAction().isOpen()) {
				resolved.remove(a.getAction());
			}
			if (a.getAction().getRemind()!=null) {
				reminder.remove(a.getAction());
			}
			if (a.getAction().getPriority()==null || a.getAction().getPriority()==Priority.None) {
				priority.remove(a.getAction());
			}*/
        }

        void updateMetaModify(ActionEvent a) {
            if (a.getProperty().equals(Action.RESOLUTION_PROPERTY_NAME)) {
                if (a.getAction().isResolved()) {
                    resolved.add(a.getAction());
                } else {
                    resolved.remove(a.getAction());
                }
                if (a.getAction().isDeleted()) {
                    deleted.add(a.getAction());
                } else {
                    deleted.remove(a.getAction());
                }
            }
            if (a.getProperty().equals(Action.REMIND_PROPERTY_NAME)) {
                if (a.getAction().getRemind() != null) {
                    reminder.add(a.getAction());
                } else {
                    reminder.remove(a.getAction());
                }
            }
            if (a.getProperty().equals(Action.PRIORITY_PROPERTY_NAME)) {
                if (a.getAction().getPriority() != null
                    && a.getAction().getPriority() != Priority.None) {
                    priority.add(a.getAction());
                } else {
                    priority.remove(a.getAction());
                }
            }
            if (a.getProperty().equals(Action.QUEUED_PROPERTY_NAME)
                && !suspentedForMultipleChanges) {
                if (a.getAction().isQueued()) {
                    queue.add(a.getAction());
                } else {
                    queue.remove(a.getAction());
                }
            }
            if (a.getProperty().equals(Action.PROJECT_PROPERTY_NAME)) {
                if (a.getOldValue() != null) {
                    getProject((Integer) a.getOldValue()).remove(a.getAction());
                }
                if (a.getNewValue() != null) {
                    getProject((Integer) a.getNewValue()).add(a.getAction());
                }
            }
        }

        public void elementAdded(FolderEvent a) {
            updateMetaAdd(a);
            GTDModelListener[] l =
                    listeners.getListeners(GTDModelListener.class);
            for (GTDModelListener gtdModelListener : l) {
                try {
                    gtdModelListener.elementAdded(a);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void elementModified(ActionEvent a) {
            checkEvent(a);
            if (!((Folder) a.getSource()).isMeta()) {
                updateMetaModify(a);
                // rethrow events for meta folders
                if (a.getAction().isQueued()) {
                    queue.fireElementModified(a.getAction(), a.getProperty(),
                            a.getOldValue(), a.getNewValue(), true);
                }
                if (a.getAction().getRemind() != null) {
                    reminder.fireElementModified(a.getAction(), a.getProperty(),
                            a.getOldValue(), a.getNewValue(), true);
                }
                if (a.getAction().getPriority() == null
                    || a.getAction().getPriority() != Priority.None) {
                    priority.fireElementModified(a.getAction(), a.getProperty(),
                            a.getOldValue(), a.getNewValue(), true);
                }
                if (a.getAction().getProject() != null) {
                    Project p = getProject(a.getAction().getProject());
                    if (p != null) {
                        p.fireElementModified(a.getAction(), a.getProperty(),
                                a.getOldValue(), a.getNewValue(), true);
                    }
                }
            }
            GTDModelListener[] l =
                    listeners.getListeners(GTDModelListener.class);
            for (GTDModelListener gtdModelListener : l) {
                try {
                    gtdModelListener.elementModified(a);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void elementRemoved(FolderEvent a) {
            updateMetaRemove(a);
            GTDModelListener[] l =
                    listeners.getListeners(GTDModelListener.class);
            for (GTDModelListener gtdModelListener : l) {
                try {
                    gtdModelListener.elementRemoved(a);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void folderAdded(Folder folder) {
            GTDModelListener[] l =
                    listeners.getListeners(GTDModelListener.class);
            for (GTDModelListener gtdModelListener : l) {
                try {
                    gtdModelListener.folderAdded(folder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void folderModified(Folder f, String p, Object o, Object n,
                boolean recycled) {
            folderModified(new FolderEvent(f, null, p, o, n, recycled));
        }

        public void folderModified(FolderEvent folder) {
            GTDModelListener[] l =
                    listeners.getListeners(GTDModelListener.class);
            for (GTDModelListener gtdModelListener : l) {
                try {
                    gtdModelListener.folderModified(folder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void folderRemoved(Folder folder) {
            GTDModelListener[] l =
                    listeners.getListeners(GTDModelListener.class);
            for (GTDModelListener gtdModelListener : l) {
                try {
                    gtdModelListener.folderRemoved(folder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void orderChanged(Folder f) {
            GTDModelListener[] l =
                    listeners.getListeners(GTDModelListener.class);
            for (GTDModelListener gtdModelListener : l) {
                try {
                    gtdModelListener.orderChanged(f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

/*	public Folder getFolder(String name) {
		for (Folder f : folders.values()) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
*/
}
