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
package org.eclipse.kapua.service.eventlog.internal.setting;

import org.eclipse.kapua.commons.setting.AbstractKapuaSetting;

/**
 * Class that offers access to event log settings
 *
 * 
 * @since 1.0
 *
 */
public class KapuaEventLogSetting extends AbstractKapuaSetting<KapuaEventLogSettingKeys> {

    /**
     * Resource file from which source properties.
     * 
     */
    private static final String EVENT_LOG_SETTING_RESOURCE = "kapua-eventlog-setting.properties";

    /**
     * Singleton instance of this {@link Class}.
     * 
     */
    private static final KapuaEventLogSetting INSTANCE = new KapuaEventLogSetting();

    /**
     * Initialize the {@link AbstractKapuaSetting} with the {@link KapuaEventLogSettingKeys#EVENT_LOG_KEY} value.
     * 
     */
    private KapuaEventLogSetting() {
        super(EVENT_LOG_SETTING_RESOURCE);
    }

    /**
     * Gets a singleton instance of {@link KapuaEventLogSetting}.
     * 
     * @return A singleton instance of JmsClientSetting.
     */
    public static KapuaEventLogSetting getInstance() {
        return INSTANCE;
    }
}
