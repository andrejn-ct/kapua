/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.user.internal.setting;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class KapuaUserSettingTest {

    @Test
    public void getUserSettingInstance() throws Exception {
        KapuaUserSetting userSettings = KapuaUserSetting.getInstance();

        assertNotNull("User settings not configured.", userSettings);
    }

}