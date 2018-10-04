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
package org.eclipse.kapua.service.eventlog.logger.internal;

import org.eclipse.kapua.commons.event.ServiceEventClientConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventModule;
import org.eclipse.kapua.commons.event.ServiceEventModuleConfiguration;
import org.eclipse.kapua.commons.event.ServiceInspector;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.eventlog.logger.EventLogService;
import org.eclipse.kapua.service.eventlog.logger.internal.setting.KapuaEventLogSetting;
import org.eclipse.kapua.service.eventlog.logger.internal.setting.KapuaEventLogSettingKeys;

import javax.inject.Inject;
import java.util.List;

@KapuaProvider
public class EventLogServiceModule extends ServiceEventModule {

    @Inject
    private EventLogService eventLogService;

    @Override
    protected ServiceEventModuleConfiguration initializeConfiguration() {
        KapuaEventLogSetting kels = KapuaEventLogSetting.getInstance();
        List<ServiceEventClientConfiguration> selc = ServiceInspector.getEventBusClients(eventLogService, EventLogService.class);
        return new ServiceEventModuleConfiguration(
                kels.getString(KapuaEventLogSettingKeys.EVENT_LOG_EVENT_ADDRESS), 
                EventLogEntityManagerFactory.getInstance(), 
                selc.toArray(new ServiceEventClientConfiguration[0]));
    }
}
