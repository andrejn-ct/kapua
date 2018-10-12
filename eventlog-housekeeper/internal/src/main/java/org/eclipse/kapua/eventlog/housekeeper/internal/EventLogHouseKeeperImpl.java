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
package org.eclipse.kapua.eventlog.housekeeper.internal;

import org.eclipse.kapua.commons.model.query.predicate.AttributePredicateImpl;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;
import org.eclipse.kapua.commons.util.ArgumentValidator;
import org.eclipse.kapua.eventlog.housekeeper.EventLogHouseKeeper;
import org.eclipse.kapua.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.service.eventlog.logger.EventLog;
import org.eclipse.kapua.service.eventlog.logger.EventLogAttributes;
import org.eclipse.kapua.service.eventlog.logger.EventLogFactory;
import org.eclipse.kapua.service.eventlog.logger.EventLogListResult;
import org.eclipse.kapua.service.eventlog.logger.EventLogQuery;
import org.eclipse.kapua.service.eventlog.logger.EventLogService;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;

import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * (@link EventLogHouseKeeper) implementation.
 */
@KapuaProvider
public class EventLogHouseKeeperImpl implements EventLogHouseKeeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventLogHouseKeeperImpl.class);

    private static KapuaLocator locator = KapuaLocator.getInstance();
    private static EventLogService eventLogService = locator.getService(EventLogService.class);
    private static EventLogFactory eventLogFactory = locator.getFactory(EventLogFactory.class);
    private final UserService userService = locator.getService(UserService.class);

    public EventLogHouseKeeperImpl() {}

    @Override
    public void schedulePurge() throws KapuaException {
    }

    @Override
    public void unschedulePurge() throws KapuaException {
    }

    @Override
    public void purgeLogs() throws KapuaException {
        //
        // Retrieve the administrator (for the account id)
        User adminUser = getSystemAdministrator();

        KapuaSecurityUtils.doPrivileged(() -> {
            //
            // Retrieve the configured log entry lifetime
            Map<String, Object> logConfig = eventLogService.getConfigValues(adminUser.getScopeId());
            int daysToLive = (int) logConfig.get("eventLogTimeToLive");
            ArgumentValidator.notNull(daysToLive, "logeEntry.timeToLive");

            //
            // Calculate the time threshold for deleting logs
            Date threshold = DateTime.now().minusDays(daysToLive).toDate();

            while (true) {
                //
                // Query for the records that need to be deleted
                EventLogQuery query = eventLogFactory.newQuery(adminUser.getScopeId());
                query.setLimit(1000);
                query.setPredicate(new AttributePredicateImpl<>(EventLogAttributes.EVENT_SENT_ON, threshold, AttributePredicate.Operator.LESS_THAN));
                EventLogListResult obsoleteRecords = eventLogService.query(query);

                //
                // Check whether there are still records to be deleted
                if (obsoleteRecords.isEmpty()) {
                    break;
                }

                //
                // Delete the obsolete records
                for (EventLog logEntry : obsoleteRecords.getItems()) {
                    eventLogService.delete(logEntry.getScopeId(), logEntry.getId());
                }
            }
        });
    }

    // -----------------------------------------------------------------------------------------
    //
    // Private Methods
    //
    // -----------------------------------------------------------------------------------------

    private User getSystemAdministrator() throws KapuaException {

        return KapuaSecurityUtils.doPrivileged(() -> {
            //
            // Retrieve the administrator (for the account id)
            String adminUsername = SystemSetting.getInstance().getString(SystemSettingKey.SYS_ADMIN_USERNAME);
            User adminUser = userService.findByName(adminUsername);
            return adminUser;
        });
    }

}
