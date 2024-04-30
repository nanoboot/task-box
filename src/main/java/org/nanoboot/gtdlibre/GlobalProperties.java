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

package org.nanoboot.gtdlibre;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author ikesan
 */
public class GlobalProperties {

    public static final String DATE_FORMAT = "dateFormat";
    public static final String SHOW_ALL_ACTIONS = "showAllActions";
    public static final String SHOW_CLOSED_FOLDERS = "showClosedFolders";
    public static final String PROJECT_EDITOR_PREFERED_SIZE =
            "projectEditorPreferedSize";
    public static final String SHOW_OVERVIEW_TAB = "showOverviewTab";
    public static final String SHOW_QUICK_COLLECT = "showQuickCollectBar";
    public static final String AUTO_SAVE = "autoSave";

    private final Properties prop = new Properties();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(String s, PropertyChangeListener l) {
        pcs.addPropertyChangeListener(s, l);
    }

    public void removePropertyChangeListener(String s,
            PropertyChangeListener l) {
        pcs.removePropertyChangeListener(s, l);
    }

    public void putProperty(String s, Object v) {
        Object old = null;
        if (v == null) {
            old = prop.remove(s);
        } else {
            old = prop.put(s, v);
        }
        pcs.firePropertyChange(s, old, v);
    }

    public Object getProperty(String s) {
        return prop.get(s);
    }

    public Object getProperty(String s, Object defaultValue) {
        Object o = prop.get(s);
        if (o == null) {
            return defaultValue;
        }
        return o;
    }

    public boolean getBoolean(String s) {
        return getBoolean(s, false);
    }

    public boolean getBoolean(String s, boolean def) {
        Object o = prop.get(s);

        if (o == null) {
            return def;
        }

        if (o instanceof Boolean) {
            return (Boolean) o;
        }

        Boolean b = Boolean.valueOf(o.toString());
        prop.put(s, b);

        return b;
    }

    public Integer getInteger(String s) {
        Object o = prop.get(s);

        if (o == null) {
            return null;
        }

        if (o instanceof Integer) {
            return (Integer) o;
        }

        try {
            Integer b = Integer.valueOf(o.toString());
            prop.put(s, b);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void load(Reader r) throws IOException {
        prop.load(r);
    }

    public void store(BufferedWriter w) throws IOException {
        Properties p = new Properties();

        for (Object s : prop.keySet()) {

            Object o = prop.get(s);

            if (o instanceof int[] ii) {
                StringBuilder sb = new StringBuilder();

                if (ii.length > 0) {
                    sb.append(ii[0]);
                }

                for (int i = 1; i < ii.length; i++) {
                    sb.append(',');
                    sb.append(ii[i]);
                }
                p.put(s, sb.toString());
            } else if (o instanceof boolean[] ii) {
                StringBuilder sb = new StringBuilder(ii.length * 6);

                if (ii.length > 0) {
                    sb.append(ii[0]);
                }

                for (int i = 1; i < ii.length; i++) {
                    sb.append(',');
                    sb.append(ii[i]);
                }
                p.put(s, sb.toString());
            } else if (o != null) {
                p.put(s, o.toString());
            }

        }

        p.store(w, "");
    }

    public int[] getIntegerArray(String s) {
        Object o = prop.get(s);

        if (o == null) {
            return null;
        }

        if (o instanceof int[]) {
            return (int[]) o;
        }

        if (o instanceof String && s.length() > 0) {
            String[] ss = o.toString().split(",");
            if (ss != null && ss.length > 0) {
                List<Integer> il = new ArrayList<>(ss.length);
                for (int i = 0; i < ss.length; i++) {
                    ss[i] = ss[i].trim();
                    if (ss[i].length() > 0) {
                        try {
                            il.add(Integer.valueOf(ss[i]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                int[] ii = new int[il.size()];
                for (int i = 0; i < ii.length; i++) {
                    ii[i] = il.get(i);
                }
                prop.put(s, ii);
                return ii;
            }
        }

        return null;
    }

    public boolean[] getBooleanArray(String s) {
        Object o = prop.get(s);

        if (o == null) {
            return null;
        }

        if (o instanceof boolean[]) {
            return (boolean[]) o;
        }

        if (o instanceof String && s.length() > 0) {
            String[] ss = o.toString().split(",");
            if (ss != null && ss.length > 0) {
                boolean[] ii = new boolean[ss.length];
                for (int i = 0; i < ii.length; i++) {
                    try {
                        ii[i] = Boolean.parseBoolean(ss[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                prop.put(s, ii);
                return ii;
            }
        }

        return null;
    }
}
