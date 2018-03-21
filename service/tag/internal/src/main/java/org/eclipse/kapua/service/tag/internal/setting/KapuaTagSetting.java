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
package org.eclipse.kapua.service.tag.internal.setting;

import org.eclipse.kapua.commons.setting.AbstractKapuaSetting;

/**
 * Class that offers access to tag settings
 *
 * 
 * @since 1.0
 *
 */
public class KapuaTagSetting extends AbstractKapuaSetting<KapuaTagSettingKeys> {

    /**
     * Resource file from which source properties.
     * 
     */
    private static final String TAG_SETTING_RESOURCE = "kapua-tag-setting.properties";

    /**
     * Singleton instance of this {@link Class}.
     * 
     */
    private static final KapuaTagSetting INSTANCE = new KapuaTagSetting();

    /**
     * Initialize the {@link AbstractKapuaSetting} with the {@link KapuaTagSettingKeys#TAG_KEY} value.
     * 
     */
    private KapuaTagSetting() {
        super(TAG_SETTING_RESOURCE);
    }

    /**
     * Gets a singleton instance of {@link KapuaTagSetting}.
     * 
     * @return A singleton instance of JmsClientSetting.
     */
    public static KapuaTagSetting getInstance() {
        return INSTANCE;
    }
}
