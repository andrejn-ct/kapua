/*******************************************************************************
 * Copyright (c) 2011, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.device.registry.internal;

import com.google.common.collect.Lists;
import org.eclipse.kapua.KapuaDuplicateNameException;
import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaMaxNumberOfItemsReachedException;
import org.eclipse.kapua.commons.configuration.AbstractKapuaConfigurableResourceLimitedService;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicateImpl;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.event.ListenServiceEvent;
import org.eclipse.kapua.event.ServiceEvent;
import org.eclipse.kapua.event.ServiceEventBusException;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.model.query.predicate.QueryPredicate;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.device.registry.DeviceCreator;
import org.eclipse.kapua.service.device.registry.DeviceDomains;
import org.eclipse.kapua.service.device.registry.DeviceFactory;
import org.eclipse.kapua.service.device.registry.DeviceListResult;
import org.eclipse.kapua.service.device.registry.DeviceAttributes;
import org.eclipse.kapua.service.device.registry.DeviceQuery;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.device.registry.RegistryServiceConstants;
import org.eclipse.kapua.service.device.registry.common.DeviceValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link DeviceRegistryService} implementation.
 *
 * @since 1.0.0
 */
@KapuaProvider
public class DeviceRegistryServiceImpl extends AbstractKapuaConfigurableResourceLimitedService<Device, DeviceCreator, DeviceRegistryService, DeviceListResult, DeviceQuery, DeviceFactory>
        implements DeviceRegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRegistryServiceImpl.class);

    /**
     * Constructor
     *
     * @param deviceEntityManagerFactory
     */
    public DeviceRegistryServiceImpl(DeviceEntityManagerFactory deviceEntityManagerFactory) {
        super(DeviceRegistryService.class.getName(), DeviceDomains.DEVICE_DOMAIN, deviceEntityManagerFactory, DeviceRegistryService.class, DeviceFactory.class);
    }

    /**
     * Constructor
     */
    public DeviceRegistryServiceImpl() {
        this(DeviceEntityManagerFactory.instance());
    }

    // Operations implementation
    @Override
    public Device create(DeviceCreator deviceCreator) throws KapuaException {
        DeviceValidation.validateCreatePreconditions(deviceCreator);
        if (allowedChildEntities(deviceCreator.getScopeId()) <= 0) {
            throw new KapuaMaxNumberOfItemsReachedException("Devices");
        }

        DeviceQuery query = new DeviceQueryImpl(deviceCreator.getScopeId());
        query.setPredicate(new AttributePredicateImpl<>(DeviceAttributes.CLIENT_ID, deviceCreator.getClientId()));
        DeviceListResult deviceListResult = query(query);
        if (!deviceListResult.isEmpty()) {
            throw new KapuaDuplicateNameException(deviceCreator.getClientId());
        }
        return entityManagerSession.onTransactedInsert(entityManager -> DeviceDAO.create(entityManager, deviceCreator));
    }

    @Override
    public Device update(Device device) throws KapuaException {
        DeviceValidation.validateUpdatePreconditions(device);

        return entityManagerSession.onTransactedResult(entityManager -> {
            Device currentDevice = DeviceDAO.find(entityManager, device.getScopeId(), device.getId());
            if (currentDevice == null) {
                throw new KapuaEntityNotFoundException(Device.TYPE, device.getId());
            }
            // Update
            return DeviceDAO.update(entityManager, device);
        });
    }

    @Override
    public Device find(KapuaId scopeId, KapuaId entityId) throws KapuaException {
        DeviceValidation.validateFindPreconditions(scopeId, entityId);
        return entityManagerSession.onResult(entityManager -> DeviceDAO.find(entityManager, scopeId, entityId));
    }

    @Override
    public DeviceListResult query(KapuaQuery<Device> query) throws KapuaException {
        DeviceValidation.validateQueryPreconditions(query);
        return entityManagerSession.onResult(entityManager -> DeviceDAO.query(entityManager, query));
    }

    @Override
    public long count(KapuaQuery<Device> query) throws KapuaException {
        DeviceValidation.validateCountPreconditions(query);
        return entityManagerSession.onResult(entityManager -> DeviceDAO.count(entityManager, query));
    }

    @Override
    public void delete(KapuaId scopeId, KapuaId deviceId) throws KapuaException {
        DeviceValidation.validateDeletePreconditions(scopeId, deviceId);
        entityManagerSession.onTransactedAction(entityManager -> DeviceDAO.delete(entityManager, scopeId, deviceId));
    }

    @Override
    public Device findByClientId(KapuaId scopeId, String clientId) throws KapuaException {
        DeviceValidation.validateFindByClientIdPreconditions(scopeId, clientId);

        DeviceQueryImpl query = new DeviceQueryImpl(scopeId);
        QueryPredicate predicate = new AttributePredicateImpl<>(DeviceAttributes.CLIENT_ID, clientId);
        query.setFetchAttributes(Lists.newArrayList(DeviceAttributes.CONNECTION, DeviceAttributes.LAST_EVENT));
        query.setPredicate(predicate);

        //
        // Query and parse result
        Device device = null;
        DeviceListResult result = query(query);
        if (!result.isEmpty()) {
            device = result.getFirstItem();
        }

        return device;
    }

    @ListenServiceEvent(fromAddress="account")
    @ListenServiceEvent(fromAddress="authorization")
    @ListenServiceEvent(fromAddress="tag")
    public void onKapuaEvent(ServiceEvent kapuaEvent) throws KapuaException {
        if (kapuaEvent == null) {
            LOGGER.warn("DeviceRegistryService: Service bus error. Received null ServiceEvent");
            throw new ServiceEventBusException("Service bus error. Received null ServiceEvent.");
        }
        LOGGER.info("DeviceRegistryService: received kapua event from {}, operation {}", kapuaEvent.getService(), kapuaEvent.getOperation());

        if (RegistryServiceConstants.GROUP_SERVICE_NAME.equals(kapuaEvent.getService()) &&
                RegistryServiceConstants.OPERATION_DELETE.equals(kapuaEvent.getOperation())) {
            removeDevicesFromGroup(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        } else if (RegistryServiceConstants.ACCOUNT_SERVICE_NAME.equals(kapuaEvent.getService()) &&
                RegistryServiceConstants.OPERATION_DELETE.equals(kapuaEvent.getOperation())) {
            deleteDeviceByAccountId(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        } else if (RegistryServiceConstants.TAG_SERVICE_NAME.equals(kapuaEvent.getService()) &&
                RegistryServiceConstants.OPERATION_DELETE.equals(kapuaEvent.getOperation())) {
            removeTagFromDevices(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        }
    }

    // -----------------------------------------------------------------------------------------
    //
    // Private Methods
    //
    // -----------------------------------------------------------------------------------------

    private void removeDevicesFromGroup(KapuaId scopeId, KapuaId groupId) throws KapuaException {

        DeviceFactory deviceFactory = KapuaLocator.getInstance().getFactory(DeviceFactory.class);
        DeviceQuery query = deviceFactory.newQuery(scopeId);
        query.setPredicate(new AttributePredicateImpl<>(DeviceAttributes.GROUP_ID, groupId));

        KapuaSecurityUtils.doPrivileged(()-> {
            DeviceListResult devicesToRemove = query(query);
            for (Device d : devicesToRemove.getItems()) {
                d.setGroupId(null);
                update(d);
            }
        });
    }

    private void deleteDeviceByAccountId(KapuaId scopeId, KapuaId accountId) throws KapuaException {

        DeviceFactory deviceFactory = KapuaLocator.getInstance().getFactory(DeviceFactory.class);
        DeviceQuery query = deviceFactory.newQuery(accountId);

        KapuaSecurityUtils.doPrivileged(()-> {
            DeviceListResult devicesToDelete = query(query);
            for (Device d : devicesToDelete.getItems()) {
                delete(d.getScopeId(), d.getId());
            }
        });
    }

    private void removeTagFromDevices(KapuaId scopeId, KapuaId tagId)
            throws KapuaException {

        DeviceFactory deviceFactory = KapuaLocator.getInstance().getFactory(DeviceFactory.class);
        DeviceQuery query = deviceFactory.newQuery(scopeId);
        query.setPredicate(new AttributePredicateImpl<>(DeviceAttributes.TAG_IDS, tagId));

        KapuaSecurityUtils.doPrivileged(()-> {
            DeviceListResult devicesToUpdate = query(query);
            for (Device d : devicesToUpdate.getItems()) {
                Device tmpDev = stripTagIdFromDevice(d, tagId);
                update(tmpDev);
            }
        });
    }

    private Device stripTagIdFromDevice(Device dev, KapuaId tagId) {

        Set<KapuaId> tmpIdList = new HashSet<>();
        for(KapuaId tmpId : dev.getTagIds()) {
            if (!tmpId.getId().equals(tagId.getId())) {
                tmpIdList.add(tmpId);
            }
        }
        dev.setTagIds(tmpIdList);

        return dev;
    }
}
