/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.job.internal.setting;

import org.eclipse.kapua.commons.setting.AbstractKapuaSetting;

/**
 * Class that offers access to job settings
 *
 * 
 * @since 1.0
 *
 */
public class KapuaJobSetting extends AbstractKapuaSetting<KapuaJobSettingKeys> {

    /**
     * Resource file from which source properties.
     * 
     */
    private static final String JOB_SETTING_RESOURCE = "kapua-job-setting.properties";

    /**
     * Singleton instance of this {@link Class}.
     * 
     */
    private static final KapuaJobSetting INSTANCE = new KapuaJobSetting();

    /**
     * Initialize the {@link AbstractKapuaSetting} with the {@link KapuaJobSettingKeys#JOB_KEY} value.
     * 
     */
    private KapuaJobSetting() {
        super(JOB_SETTING_RESOURCE);
    }

    /**
     * Gets a singleton instance of {@link KapuaJobSetting}.
     * 
     * @return A singleton instance of JmsClientSetting.
     */
    public static KapuaJobSetting getInstance() {
        return INSTANCE;
    }
}
