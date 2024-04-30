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

package org.nanoboot.tasklibre;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ikesan
 */
public class TaskLibreMainTestSuite extends TestSuite {
    public TaskLibreMainTestSuite() {
        addTestSuite(TaskLibreMainEngineTest.class);
        addTestSuite(ModelTest.class);
        addTestSuite(SaveLoadTest.class);
    }

    public static Test suite() {
        return new TaskLibreMainTestSuite();
    }
}
