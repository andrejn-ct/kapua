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
package org.eclipse.kapua.service.tag.internal;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.kapua.commons.event.ServiceEventClientConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventModule;
import org.eclipse.kapua.commons.event.ServiceEventModuleConfiguration;
import org.eclipse.kapua.commons.event.ServiceInspector;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.tag.TagService;
import org.eclipse.kapua.service.tag.internal.setting.KapuaTagSetting;
import org.eclipse.kapua.service.tag.internal.setting.KapuaTagSettingKeys;

@KapuaProvider
public class TagServiceModule extends ServiceEventModule {

    @Inject
    private TagService tagService;

    @Override
    protected ServiceEventModuleConfiguration initializeConfiguration() {
        KapuaTagSetting kts = KapuaTagSetting.getInstance();
        List<ServiceEventClientConfiguration> selc = ServiceInspector.getEventBusClients(tagService, TagService.class);
        return new ServiceEventModuleConfiguration(
                kts.getString(KapuaTagSettingKeys.TAG_EVENT_ADDRESS), 
                TagEntityManagerFactory.getInstance(), 
                selc.toArray(new ServiceEventClientConfiguration[0]));
    }
}
