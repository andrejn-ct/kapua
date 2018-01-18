/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.qa.base;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

/**
 * Simple container for between step data that is persisted between steps and
 * between step implementation classes.
 */
@Singleton
public class TestData {

    /**
     * Generic map that accepts string key that represents data and data
     * as any object.
     * Dev-user has to know type of data stored under specified key.
     * Key could be class name.
     */
    Map<String, Object> testDataMap;

    public TestData() {
        testDataMap = new HashMap<>();
    }

    public void clear() {
        testDataMap.clear();
    }

    public void put(String key, Object value) {
        testDataMap.put(key, value);
    }

    public Object get(String key) {
        return testDataMap.get(key);
    }

    public boolean contains(String key) {
        return testDataMap.containsKey(key);
    }

    public void remove(String key) {
        testDataMap.remove(key);
    }
}
