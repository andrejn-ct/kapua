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
package org.eclipse.kapua.service.device.steps;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.configuration.metatype.KapuaMetatypeFactoryImpl;
import org.eclipse.kapua.commons.model.id.IdGenerator;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.model.id.KapuaIdFactoryImpl;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.security.KapuaSession;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.config.metatype.KapuaMetatypeFactory;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.id.KapuaIdFactory;
import org.eclipse.kapua.qa.base.DBHelper;
import org.eclipse.kapua.qa.base.TestBase;
import org.eclipse.kapua.qa.base.TestData;
import org.eclipse.kapua.qa.base.TestJAXBContextProvider;
import org.eclipse.kapua.service.authentication.AuthenticationService;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.access.AccessInfoService;
import org.eclipse.kapua.service.authorization.permission.Permission;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.authorization.permission.shiro.PermissionFactoryImpl;
import org.eclipse.kapua.service.device.registry.DeviceCreator;
import org.eclipse.kapua.service.device.registry.DeviceFactory;
import org.eclipse.kapua.service.device.registry.DeviceQuery;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.device.registry.DeviceStatus;
import org.eclipse.kapua.service.device.registry.common.DeviceValidation;
import org.eclipse.kapua.service.device.registry.internal.DeviceCreatorImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceEntityManagerFactory;
import org.eclipse.kapua.service.device.registry.internal.DeviceFactoryImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceRegistryServiceImpl;
import org.eclipse.kapua.test.MockedLocator;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigInteger;

/**
 * Implementation of Gherkin steps used in DeviceRegistryValidation.feature scenarios.
 * <p>
 * MockedLocator is used for Location Service. Mockito is used to mock other
 * services that the Device Registry services dependent on. Dependent services are: -
 * Authorization Service.
 */
@ScenarioScoped
public class DeviceRegistryValidationTestSteps extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(DeviceRegistryValidationTestSteps.class);

    // Commons related objects
    KapuaIdFactory kapuaIdFactory = new KapuaIdFactoryImpl();

    /**
     * User service by locator.
     */
    private DeviceRegistryService deviceRegistryService;
    private DeviceFactory deviceFactory;
    private DeviceCreator deviceCreator;
    private DeviceImpl device;
    private DeviceQuery query;

    /**
     * Authentication service by locator.
     */
    private AuthenticationService authenticationService;

    /**
     * Credential service by locator.
     */
    private CredentialService credentialService;

    /**
     * Permission service by locator.
     */
    private AccessInfoService accessInfoService;

    // *************************************
    // Definition of Cucumber scenario steps
    // *************************************

    // Setup and tear-down steps

    @Inject
    public DeviceRegistryValidationTestSteps(TestData stepData, DBHelper dbHelper) {
        this.database = dbHelper;
        this.stepData = stepData;
    }

    @Before
    public void beforeScenario(Scenario scenario) {

        // Initialize the database
        this.database.setup();

        // Services by default Locator
        locator = KapuaLocator.getInstance();
        deviceRegistryService = locator.getService(DeviceRegistryService.class);
        deviceFactory = locator.getFactory(DeviceFactory.class);
        authenticationService = locator.getService(AuthenticationService.class);
        credentialService = locator.getService(CredentialService.class);
        accessInfoService = locator.getService(AccessInfoService.class);

        XmlUtil.setContextProvider(new TestJAXBContextProvider());

        this.scenario = scenario;
        this.stepData.clear();

        if (isUnitTest()) {
            try {
                setupMockLocatorForDeviceService();
            } catch (Exception ex) {
                logger.error("Failed to set up mock locator in @Before", ex);
            }
        }
    }

    @After
    public void afterScenario()
            throws Exception {
        // Clean up the database
        database.deleteAll();
        KapuaSecurityUtils.clearSession();
    }

    // The Cucumber test steps

    @Given("^A regular device creator$")
    public void createARegularDeviceCreator() {
        deviceCreator = prepareRegularDeviceCreator(SYS_SCOPE_ID, "testDev");
    }

    @Given("^A null device creator$")
    public void createANullDeviceCreator() {
        deviceCreator = null;
    }

    @Given("^A regular device$")
    public void createRegularDevice() {
        device = prepareRegularDevice(SYS_SCOPE_ID, new KapuaEid(IdGenerator.generate()));
    }

    @Given("^A null device$")
    public void createANullDevice() {
        device = null;
    }

    @Given("^A regular query$")
    public void createRegularQuery() {
        query = deviceFactory.newQuery(SYS_SCOPE_ID);
    }

    @Given("^A query with a null Scope ID$")
    public void createQueryWithNullScopeId() {
        query = deviceFactory.newQuery(null);
    }

    @Given("^A null query$")
    public void createNullQuery() {
        query = null;
    }

    @When("^I set the creator scope ID to null$")
    public void setDeviceCreatorScopeToNull() {
        deviceCreator.setScopeId(null);
    }

    @When("^I set the creator client ID to null$")
    public void setDeviceCreatorClientToNull() {
        deviceCreator.setClientId(null);
    }

    @When("^I set the device scope ID to null$")
    public void setDeviceScopeToNull() {
        device.setScopeId(null);
    }

    @When("^I set the device ID to null$")
    public void setDeviceIdToNull() {
        device.setId(null);
    }

    @When("^I validate the device creator$")
    public void validateExistingDeviceCreator()
            throws Exception {
        try {
            primeException();
            DeviceValidation.validateCreatePreconditions(deviceCreator);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I validate the device for updates$")
    public void validateExistingDeviceForUpdates()
            throws Exception {
        try {
            primeException();
            DeviceValidation.validateUpdatePreconditions(device);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^Validating a find operation for scope (.+) and device (.+)$")
    public void validateDeviceSearch(String scopeId, String deviceId)
            throws Exception {
        KapuaId scope;
        KapuaId dev;

        if (scopeId.trim().toLowerCase().equals("null")) {
            scope = null;
        } else {
            scope = new KapuaEid(new BigInteger(scopeId));
        }

        if (deviceId.trim().toLowerCase().equals("null")) {
            dev = null;
        } else {
            dev = new KapuaEid(new BigInteger(deviceId));
        }

        try {
            primeException();
            DeviceValidation.validateFindPreconditions(scope, dev);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^Validating a find operation for scope (.+) and client \"(.*)\"$")
    public void validateDeviceSearchByClientId(String scopeId, String clientId)
            throws Exception {
        KapuaId scope;
        String client;

        if (scopeId.trim().toLowerCase().equals("null")) {
            scope = null;
        } else {
            scope = new KapuaEid(new BigInteger(scopeId));
        }

        if (clientId.trim().toLowerCase().equals("null")) {
            client = null;
        } else {
            client = clientId;
        }

        try {
            primeException();
            DeviceValidation.validateFindByClientIdPreconditions(scope, client);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^Validating a delete operation for scope (.+) and device (.+)$")
    public void validateDeviceDelete(String scopeId, String deviceId)
            throws Exception {
        KapuaId scope;
        KapuaId dev;

        if (scopeId.trim().toLowerCase().equals("null")) {
            scope = null;
        } else {
            scope = new KapuaEid(new BigInteger(scopeId));
        }

        if (deviceId.trim().toLowerCase().equals("null")) {
            dev = null;
        } else {
            dev = new KapuaEid(new BigInteger(deviceId));
        }

        try {
             primeException();
            DeviceValidation.validateDeletePreconditions(scope, dev);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I validate a query operation$")
    public void checkQueryOperation()
            throws Exception {
        try {
            primeException();
            DeviceValidation.validateQueryPreconditions(query);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I validate a count operation$")
    public void checkCountOperation()
            throws Exception {
        try {
            primeException();
            DeviceValidation.validateCountPreconditions(query);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    // *******************
    // * Private Helpers *
    // *******************

    // Create a device creator object. The creator is pre-filled with default data.
    private DeviceCreator prepareRegularDeviceCreator(KapuaId accountId, String client) {
        DeviceCreatorImpl tmpDeviceCreator = (DeviceCreatorImpl) deviceFactory.newCreator(accountId, client);

        tmpDeviceCreator.setClientId(client);
        tmpDeviceCreator.setConnectionId(new KapuaEid(IdGenerator.generate()));
        tmpDeviceCreator.setDisplayName("test_name");
        tmpDeviceCreator.setSerialNumber("serialNumber");
        tmpDeviceCreator.setModelId("modelId");
        tmpDeviceCreator.setImei(String.valueOf(random.nextInt()));
        tmpDeviceCreator.setImsi(String.valueOf(random.nextInt()));
        tmpDeviceCreator.setIccid(String.valueOf(random.nextInt()));
        tmpDeviceCreator.setBiosVersion("biosVersion");
        tmpDeviceCreator.setFirmwareVersion("firmwareVersion");
        tmpDeviceCreator.setOsVersion("osVersion");
        tmpDeviceCreator.setJvmVersion("jvmVersion");
        tmpDeviceCreator.setOsgiFrameworkVersion("osgiFrameworkVersion");
        tmpDeviceCreator.setApplicationFrameworkVersion("kapuaVersion");
        tmpDeviceCreator.setApplicationIdentifiers("applicationIdentifiers");
        tmpDeviceCreator.setAcceptEncoding("acceptEncoding");
        tmpDeviceCreator.setGpsLatitude(45.2);
        tmpDeviceCreator.setGpsLongitude(26.3);
        tmpDeviceCreator.setCustomAttribute1("customAttribute1");
        tmpDeviceCreator.setCustomAttribute2("customAttribute2");
        tmpDeviceCreator.setCustomAttribute3("customAttribute3");
        tmpDeviceCreator.setCustomAttribute4("customAttribute4");
        tmpDeviceCreator.setCustomAttribute5("customAttribute5");
        tmpDeviceCreator.setStatus(DeviceStatus.ENABLED);

        return tmpDeviceCreator;
    }

    // Create a device object. The device is pre-filled with default data.
    private DeviceImpl prepareRegularDevice(KapuaId accountId, KapuaId deviceId) {
        DeviceImpl tmpDevice = (DeviceImpl) deviceFactory.newEntity(accountId);

        tmpDevice.setId(deviceId);
        tmpDevice.setConnectionId(new KapuaEid(IdGenerator.generate()));
        tmpDevice.setDisplayName("test_name");
        tmpDevice.setSerialNumber("serialNumber");
        tmpDevice.setModelId("modelId");
        tmpDevice.setImei(String.valueOf(random.nextInt()));
        tmpDevice.setImsi(String.valueOf(random.nextInt()));
        tmpDevice.setIccid(String.valueOf(random.nextInt()));
        tmpDevice.setBiosVersion("biosVersion");
        tmpDevice.setFirmwareVersion("firmwareVersion");
        tmpDevice.setOsVersion("osVersion");
        tmpDevice.setJvmVersion("jvmVersion");
        tmpDevice.setOsgiFrameworkVersion("osgiFrameworkVersion");
        tmpDevice.setApplicationFrameworkVersion("kapuaVersion");
        tmpDevice.setApplicationIdentifiers("applicationIdentifiers");
        tmpDevice.setAcceptEncoding("acceptEncoding");
        tmpDevice.setCustomAttribute1("customAttribute1");
        tmpDevice.setCustomAttribute2("customAttribute2");
        tmpDevice.setCustomAttribute3("customAttribute3");
        tmpDevice.setCustomAttribute4("customAttribute4");
        tmpDevice.setCustomAttribute5("customAttribute5");
        tmpDevice.setStatus(DeviceStatus.ENABLED);

        return tmpDevice;
    }

    /**
     * Set up the preconditions for unit tests. This includes filling the mock locator with the correct
     * mocked services and the actual service implementation under test.
     * Also, all the unit tests will be run with the kapua-sys user.
     *
     * @throws Exception
     */
    private void setupMockLocatorForDeviceService() throws Exception {
        MockedLocator mockedLocator = (MockedLocator) KapuaLocator.getInstance();

        AbstractModule module = new AbstractModule() {

            @Override
            protected void configure() {

                // Inject mocked Authorization Service method checkPermission
                AuthorizationService mockedAuthorization = Mockito.mock(AuthorizationService.class);
                try {
                    Mockito.doNothing().when(mockedAuthorization).checkPermission(Matchers.any(Permission.class));
                } catch (KapuaException e) {
                    // skip
                }
                bind(AuthorizationService.class).toInstance(mockedAuthorization);
                // Inject mocked Permission Factory
//                PermissionFactory mockedPermissionFactory = Mockito.mock(PermissionFactory.class);
//                bind(PermissionFactory.class).toInstance(mockedPermissionFactory);
                // Set KapuaMetatypeFactory for Metatype configuration

                // Inject actual implementations of required services and factories
                KapuaIdFactory idFactory = new KapuaIdFactoryImpl();
                bind(KapuaIdFactory.class).toInstance(idFactory);
                KapuaMetatypeFactory metaFactory = new KapuaMetatypeFactoryImpl();
                bind(KapuaMetatypeFactory.class).toInstance(metaFactory);
                PermissionFactory permissionFactory = new PermissionFactoryImpl();
                bind(PermissionFactory.class).toInstance(permissionFactory);
                DeviceEntityManagerFactory deviceEntityManagerFactory = DeviceEntityManagerFactory.instance();
                bind(DeviceEntityManagerFactory.class).toInstance(deviceEntityManagerFactory);
                DeviceRegistryService deviceRegistryService = new DeviceRegistryServiceImpl();
                bind(DeviceRegistryService.class).toInstance(deviceRegistryService);
                DeviceFactory deviceFactory = new DeviceFactoryImpl();
                bind(DeviceFactory.class).toInstance(deviceFactory);
            }
        };

        Injector injector = Guice.createInjector(module);
        mockedLocator.setInjector(injector);

        deviceRegistryService = locator.getService(DeviceRegistryService.class);
        deviceFactory = locator.getFactory(DeviceFactory.class);

        // Create KapuaSession using KapuaSecurtiyUtils and kapua-sys user as logged in user.
        // All operations on database are performed using system user.
        KapuaSession kapuaSession = new KapuaSession(null, SYS_SCOPE_ID, SYS_USER_ID);
        KapuaSecurityUtils.setSession(kapuaSession);
    }
}
