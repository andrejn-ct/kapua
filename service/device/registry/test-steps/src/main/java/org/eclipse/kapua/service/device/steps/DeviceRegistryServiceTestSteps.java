/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
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
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.configuration.KapuaConfigurableServiceSchemaUtils;
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
import org.eclipse.kapua.qa.base.TestConfig;
import org.eclipse.kapua.qa.base.TestData;
import org.eclipse.kapua.qa.base.TestJAXBContextProvider;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.authorization.permission.shiro.PermissionFactoryImpl;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.device.registry.DeviceCreator;
import org.eclipse.kapua.service.device.registry.DeviceFactory;
import org.eclipse.kapua.service.device.registry.DeviceListResult;
import org.eclipse.kapua.service.device.registry.DeviceQuery;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.device.registry.DeviceStatus;
import org.eclipse.kapua.service.device.registry.internal.DeviceCreatorImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceCreatorProxy;
import org.eclipse.kapua.service.device.registry.internal.DeviceEntityManagerFactory;
import org.eclipse.kapua.service.device.registry.internal.DeviceFactoryImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceQueryImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceRegistryServiceImpl;
import org.eclipse.kapua.test.MockedLocator;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.kapua.commons.model.query.predicate.AttributePredicate.attributeIsEqualTo;
import static org.eclipse.kapua.commons.model.query.predicate.AttributePredicate.attributeIsNotEqualTo;
import static org.eclipse.kapua.test.KapuaTest.scriptSession;

/**
 * Implementation of Gherkin steps used in DeviceRegistry.feature scenarios.
 *
 * MockedLocator is used for Location Service. Mockito is used to mock other
 * services that the Device Registry services dependent on. Dependent services are: -
 * Authorization Service -
 *
 *
 */
@ScenarioScoped
public class DeviceRegistryServiceTestSteps extends TestBase {

    public static final String DEFAULT_PATH = "src/main/sql/H2";
    public static final String DEFAULT_COMMONS_PATH = "../../../../commons";
    public static final String CREATE_DEVICE_TABLES = "dvc_*_create.sql";
    public static final String DROP_DEVICE_TABLES = "dvc_*_drop.sql";

    public static final String TEST_DEVICE_NAME = "test_name";
    public static final String TEST_BIOS_VERSION_1 = "bios_version_1";
    public static final String TEST_BIOS_VERSION_2 = "bios_version_2";
    public static final String TEST_BIOS_VERSION_3 = "bios_version_3";

    // Strings for client ID character set and length checks
    public static String simpleClientId = "simpleClientIdWith64Chars_12345678901234567890123456789012345678";
    public static String fullClientId = "fullClientIdWith64Chars_✁✂✃✄✅✆✇✈✉✊✋✌✍✎✏✐✑✒✓✔✕✁✂✃✄✅✆✇✈✉✊✋✌✍✎✏✐✑✒✓";
    public static String simpleClientIdTooLong = "simpleClientIdWith65Chars_123456789012345678901234567890123456789";
    public static String fullClientIdTooLong = "fullClientIdWith65Chars_✁✂✃✄✅✆✇✈✉✊✋✌✍✎✏✐✑✒✓✔✕✁✂✃✄✅✆✇✈✉✊✋✌✍✎✏✐✑✒✓✔";

    private static final Logger logger = LoggerFactory.getLogger(DeviceRegistryValidationTestSteps.class);

    // Various device registry related service references
    DeviceRegistryService deviceRegistryService;
    DeviceFactory deviceFactory;

    // Device registry related objects
    DeviceCreator deviceCreator;
    Device device;

    // The registry ID of a device
    KapuaId deviceId;

    // A list result for device query operations
    DeviceListResult deviceList;

    // Item count
    long count;

    // String scratchpad
    String stringValue;

    @Inject
    public DeviceRegistryServiceTestSteps(TestData stepData, DBHelper dbHelper) {
        this.database = dbHelper;
        this.stepData = stepData;
    }

    // *************************************
    // Definition of Cucumber scenario steps
    // *************************************

    // Setup and tear-down steps

    @Before
    public void beforeScenario(Scenario scenario)
            throws Exception {

        // Initialize the database
        this.database.setup();

        // Services by default Locator
        locator = KapuaLocator.getInstance();
        deviceRegistryService = locator.getService(DeviceRegistryService.class);
        deviceFactory = locator.getFactory(DeviceFactory.class);

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
        // Drop the Device Registry Service tables
        scriptSession(DeviceEntityManagerFactory.instance(), DROP_DEVICE_TABLES);
        KapuaConfigurableServiceSchemaUtils.dropSchemaObjects(DEFAULT_COMMONS_PATH);
        KapuaSecurityUtils.clearSession();
    }

    // The Cucumber test steps

    @Given("^A default device creator$")
    public void prepareDefaultDeviceCreator()
            throws KapuaException {
        deviceCreator = prepareRegularDeviceCreator(SYS_SCOPE_ID, "device_1");
        assertNotNull(deviceCreator);
    }

    @Given("^A device named \"(.*)\"$")
    public void createNamedDevice(String name)
            throws KapuaException {
        deviceCreator = prepareRegularDeviceCreator(SYS_SCOPE_ID, name);
        assertNotNull(deviceCreator);
        device = deviceRegistryService.create(deviceCreator);
        assertNotNull(device);
        deviceId = device.getId();
    }

    @Given("^A device with BIOS version \"(.*)\" named \"(.*)\"$")
    public void createNamedDeviceWithBiosVersion(String version, String name)
            throws KapuaException {
        deviceCreator = prepareRegularDeviceCreator(SYS_SCOPE_ID, name);
        assertNotNull(deviceCreator);
        deviceCreator.setBiosVersion(version);
        device = deviceRegistryService.create(deviceCreator);
        assertNotNull(device);
        deviceId = device.getId();
    }

    @Given("^I create (\\d+) randomly named devices with BIOS version \"(.*)\"$")
    public void generateABunchOfTestDevices(int number, String version)
            throws KapuaException {
        DeviceCreator tmpDevCr = null;

        for (int i = 0; i < number; i++) {
            tmpDevCr = deviceFactory.newCreator(SYS_SCOPE_ID, "test_" + String.valueOf(random.nextInt()));
            tmpDevCr.setBiosVersion(version);
            deviceRegistryService.create(tmpDevCr);
        }
    }

    @Given("^I create (\\d+) randomly named devices in scope (\\d+)$")
    public void generateABunchOfTestDevicesInScope(int number, int scope)
            throws KapuaException {
        DeviceCreator tmpDevCr = null;
        KapuaId tmpId;
        String tmpClient;

        for (int i = 0; i < number; i++) {
            tmpId = new KapuaEid(BigInteger.valueOf(scope));
            tmpClient = "test_" + String.valueOf(random.nextInt());
            tmpDevCr = deviceFactory.newCreator(tmpId, tmpClient);
            deviceRegistryService.create(tmpDevCr);
        }
    }

    @When("^I create a device from the existing creator$")
    public void createDeviceFromExistingCreator()
            throws KapuaException {
        device = deviceRegistryService.create(deviceCreator);
        assertNotNull(device);
        deviceId = device.getId();
    }

    @When("^I configure$")
    public void setConfigurationValue(List<TestConfig> testConfigs)
            throws Exception {
        Map<String, Object> valueMap = new HashMap<>();
        KapuaEid scopeId = null;
        KapuaEid parentScopeId = null;

        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
            scopeId = new KapuaEid(BigInteger.valueOf(Long.valueOf(config.getScopeId())));
            parentScopeId = new KapuaEid(BigInteger.valueOf(Long.valueOf(config.getParentScopeId())));
        }
        try {
            primeException();
            deviceRegistryService.setConfigValues(scopeId, parentScopeId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I search for a device with the remembered ID$")
    public void findDeviceWithRememberedId()
            throws KapuaException {
        device = deviceRegistryService.find(SYS_SCOPE_ID, deviceId);
    }

    @When("^I search for a device with the client ID \"(.+)\"$")
    public void findDeviceWithClientId(String clientId)
            throws KapuaException {
        device = deviceRegistryService.findByClientId(SYS_SCOPE_ID, clientId);
        assertNotNull(device);
    }

    @When("^I search for a device with a random ID$")
    public void findDeviceWithRandomId()
            throws KapuaException {
        KapuaId tmpId = new KapuaEid(IdGenerator.generate());
        device = deviceRegistryService.find(SYS_SCOPE_ID, tmpId);
    }

    @When("^I search for a device with a random client ID$")
    public void findDeviceWithRandomClientId()
            throws KapuaException {
        String tmpClientId = String.valueOf(random.nextLong());
        device = deviceRegistryService.findByClientId(SYS_SCOPE_ID, tmpClientId);
    }

    @When("^I query for devices with BIOS version \"(.*)\"$")
    public void queryForDevicesBasedOnBiosVersion(String version)
            throws KapuaException {
        DeviceQuery tmpQuery = new DeviceQueryImpl(SYS_SCOPE_ID);

        // Search for the known bios version string
        tmpQuery.setPredicate(attributeIsEqualTo("biosVersion", version));
        deviceList = deviceRegistryService.query(tmpQuery);
        assertNotNull(deviceList);
    }

    @When("^I query for devices with BIOS different from \"(.*)\"$")
    public void queryForDevicesWithDifferentBiosVersion(String version)
            throws KapuaException {
        DeviceQuery tmpQuery = new DeviceQueryImpl(SYS_SCOPE_ID);

        // Search for the known bios version string
        tmpQuery.setPredicate(attributeIsNotEqualTo("biosVersion", version));
        deviceList = deviceRegistryService.query(tmpQuery);
        assertNotNull(deviceList);
    }

    @When("^I query for devices with Client Id \"(.*)\"$")
    public void queryForDevicesBasedOnClientId(String id)
            throws KapuaException {
        DeviceQuery tmpQuery = new DeviceQueryImpl(SYS_SCOPE_ID);

        // Search for the known bios version string
        tmpQuery.setPredicate(attributeIsEqualTo("clientId", id));
        deviceList = deviceRegistryService.query(tmpQuery);
        assertNotNull(deviceList);
    }

    @And("^I extract the first device$")
    public void getFirstDeviceFromList() {
        // A device should have been found
        assertNotEquals(0, deviceList.getSize());
        device = deviceList.getItem(0);
        assertNotNull(device);
    }

    @When("^I count the devices in scope (\\d+)$")
    public void countDevicesInScope(int scope)
            throws KapuaException {
        DeviceQuery tmpQuery = new DeviceQueryImpl(new KapuaEid(BigInteger.valueOf(scope)));
        count = 0;
        assertNotNull(tmpQuery);
        count = deviceRegistryService.count(tmpQuery);
    }

    @When("^I count devices with BIOS version \"(.*)\"$")
    public void countDevicesWithBIOSVersion(String version)
            throws KapuaException {
        DeviceQuery tmpQuery = new DeviceQueryImpl(SYS_SCOPE_ID);
        assertNotNull(tmpQuery);
        tmpQuery.setPredicate(attributeIsEqualTo("biosVersion", version));
        count = 0;
        count = deviceRegistryService.count(tmpQuery);
    }

    @When("^I update some device parameters$")
    public void updateDeviceParameters()
            throws KapuaException {
        device.setBiosVersion(device.getBiosVersion() + "_upd");
        device.setCustomAttribute1(device.getCustomAttribute1() + "_upd");
        deviceRegistryService.update(device);
    }

    @When("^I update the device cleint ID to \"(.+)\"$")
    public void updateDeviceClientId(String newId)
            throws KapuaException {
        stringValue = device.getClientId();
        device.setClientId(newId);
        deviceRegistryService.update(device);
    }

    @When("^I update a device with an invalid ID$")
    public void updateDeviceWithInvalidId()
            throws Exception {

        device.setId(new KapuaEid(IdGenerator.generate()));
        try {
            primeException();
            deviceRegistryService.update(device);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @When("^I delete the device with the cleint id \"(.+)\"$")
    public void deleteDeviceWithClientId(String clientId)
            throws KapuaException {
        Device tmpDev = deviceRegistryService.findByClientId(SYS_SCOPE_ID, clientId);
        assertNotNull(tmpDev);
        deviceRegistryService.delete(SYS_SCOPE_ID, tmpDev.getId());
    }

    @When("^I delete a device with random IDs$")
    public void deleteDeviceWithRandomIds()
            throws Exception {

        KapuaId rndScope = new KapuaEid(IdGenerator.generate());
        KapuaId rndDev = new KapuaEid(IdGenerator.generate());

        try {
            primeException();
            deviceRegistryService.delete(rndScope, rndDev);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @Then("^The device has a non-null ID$")
    public void checkCreatedDeviceId()
            throws KapuaException {
        assertNotNull(device.getId());
        assertEquals(deviceCreator.getScopeId(), device.getScopeId());
        assertEquals(deviceCreator.getClientId(), device.getClientId());
    }

    @Then("^It is possible to find the device based on its registry ID$")
    public void fincDeviceByRememberedId()
            throws KapuaException {
        Device tmpDev = deviceRegistryService.find(SYS_SCOPE_ID, device.getId());

        assertNotNull(tmpDev);
        assertEquals(device.getClientId(), tmpDev.getClientId());
    }

    @Then("^It is possible to find the device based on its client ID$")
    public void fincDeviceByRememberedClientId()
            throws KapuaException {
        Device tmpDev = deviceRegistryService.findByClientId(SYS_SCOPE_ID, device.getClientId());

        assertNotNull(tmpDev);
        assertEquals(device.getId(), tmpDev.getId());
    }

    @Then("^Named device registry searches are case sesntitive$")
    public void checkCaseSensitivnessOfRegistrySearches()
            throws KapuaException {
        assertNull(deviceRegistryService.findByClientId(SYS_SCOPE_ID, deviceCreator.getClientId().toLowerCase()));
        assertNull(deviceRegistryService.findByClientId(SYS_SCOPE_ID, deviceCreator.getClientId().toUpperCase()));
        assertNotNull(deviceRegistryService.findByClientId(SYS_SCOPE_ID, deviceCreator.getClientId()));
    }

    @Then("^The device matches the creator parameters$")
    public void checkCreatedDeviceAgainstCreatorParameters()
            throws KapuaException {
        assertNotNull(device.getId());
        assertEquals(deviceCreator.getScopeId(), device.getScopeId());
        assertEquals(deviceCreator.getClientId().length(), device.getClientId().length());
        assertEquals(deviceCreator.getClientId(), device.getClientId());
        assertEquals(deviceCreator.getConnectionId(), device.getConnectionId());
        assertEquals(deviceCreator.getDisplayName(), device.getDisplayName());
        assertEquals(deviceCreator.getSerialNumber(), device.getSerialNumber());
        assertEquals(deviceCreator.getModelId(), device.getModelId());
        assertEquals(deviceCreator.getImei(), device.getImei());
        assertEquals(deviceCreator.getImsi(), device.getImsi());
        assertEquals(deviceCreator.getIccid(), device.getIccid());
        assertEquals(deviceCreator.getBiosVersion(), device.getBiosVersion());
        assertEquals(deviceCreator.getFirmwareVersion(), device.getFirmwareVersion());
        assertEquals(deviceCreator.getOsVersion(), device.getOsVersion());
        assertEquals(deviceCreator.getJvmVersion(), device.getJvmVersion());
        assertEquals(deviceCreator.getOsgiFrameworkVersion(), device.getOsgiFrameworkVersion());
        assertEquals(deviceCreator.getApplicationFrameworkVersion(), device.getApplicationFrameworkVersion());
        assertEquals(deviceCreator.getApplicationIdentifiers(), device.getApplicationIdentifiers());
        assertEquals(deviceCreator.getAcceptEncoding(), device.getAcceptEncoding());
        assertEquals(deviceCreator.getCustomAttribute1(), device.getCustomAttribute1());
        assertEquals(deviceCreator.getCustomAttribute2(), device.getCustomAttribute2());
        assertEquals(deviceCreator.getCustomAttribute3(), device.getCustomAttribute3());
        assertEquals(deviceCreator.getCustomAttribute4(), device.getCustomAttribute4());
        assertEquals(deviceCreator.getCustomAttribute5(), device.getCustomAttribute5());
        assertEquals(deviceCreator.getStatus(), device.getStatus());
    }

    @Then("^The device was correctly updated$")
    public void checkUpdatedDeviceAgainstOriginal()
            throws KapuaException {
        Device tmpDevice = null;

        tmpDevice = deviceRegistryService.find(device.getScopeId(), device.getId());
        assertNotNull(tmpDevice);

        assertEquals(tmpDevice.getScopeId(), device.getScopeId());
        assertEquals(tmpDevice.getClientId().length(), device.getClientId().length());
        assertEquals(tmpDevice.getClientId(), device.getClientId());
        assertEquals(tmpDevice.getConnectionId(), device.getConnectionId());
        assertEquals(tmpDevice.getDisplayName(), device.getDisplayName());
        assertEquals(tmpDevice.getSerialNumber(), device.getSerialNumber());
        assertEquals(tmpDevice.getModelId(), device.getModelId());
        assertEquals(tmpDevice.getImei(), device.getImei());
        assertEquals(tmpDevice.getImsi(), device.getImsi());
        assertEquals(tmpDevice.getIccid(), device.getIccid());
        assertEquals(tmpDevice.getBiosVersion(), device.getBiosVersion());
        assertEquals(tmpDevice.getFirmwareVersion(), device.getFirmwareVersion());
        assertEquals(tmpDevice.getOsVersion(), device.getOsVersion());
        assertEquals(tmpDevice.getJvmVersion(), device.getJvmVersion());
        assertEquals(tmpDevice.getOsgiFrameworkVersion(), device.getOsgiFrameworkVersion());
        assertEquals(tmpDevice.getApplicationFrameworkVersion(), device.getApplicationFrameworkVersion());
        assertEquals(tmpDevice.getApplicationIdentifiers(), device.getApplicationIdentifiers());
        assertEquals(tmpDevice.getAcceptEncoding(), device.getAcceptEncoding());
        assertEquals(tmpDevice.getCustomAttribute1(), device.getCustomAttribute1());
        assertEquals(tmpDevice.getCustomAttribute2(), device.getCustomAttribute2());
        assertEquals(tmpDevice.getCustomAttribute3(), device.getCustomAttribute3());
        assertEquals(tmpDevice.getCustomAttribute4(), device.getCustomAttribute4());
        assertEquals(tmpDevice.getCustomAttribute5(), device.getCustomAttribute5());
        assertEquals(tmpDevice.getStatus(), device.getStatus());
    }

    @Then("^The device client id is \"(.*)\"$")
    public void checkDeviceClientName(String name)
            throws KapuaException {
        assertEquals(name, device.getClientId());
    }

    @Then("^I find (\\d+) devices?$")
    public void checkListForNumberOfItems(int number) {
        assertEquals(number, deviceList.getSize());
    }

    @Then("^There (?:are|is) (\\d+) devices?$")
    public void checkNumberOfDevices(int number) {
        assertEquals(number, count);
    }

    @Then("^The client ID was not changed$")
    public void checkDeviceClientIdForChanges()
            throws KapuaException {
        Device tmpDevice = deviceRegistryService.find(SYS_SCOPE_ID, device.getId());
        assertNotEquals(device.getClientId(), tmpDevice.getClientId());
        assertEquals(stringValue, tmpDevice.getClientId());
    }

    @Then("^There is no device with the client ID \"(.+)\"$")
    public void checkWhetherNamedDeviceStillExists(String clientId)
            throws KapuaException {
        Device tmpDevice = deviceRegistryService.findByClientId(SYS_SCOPE_ID, clientId);
        assertNull(tmpDevice);
    }

    @Then("^There is no such device$")
    public void deviceMustBeNull() {
        assertNull(device);
    }

    @Then("^All device factory functions must return non null values$")
    public void exerciseAllDeviceFactoryFunctions() {
        Device tmpDevice = null;
        DeviceCreator tmpCreator = null;
        DeviceQuery tmpQuery = null;
        DeviceListResult tmpListRes = null;

        tmpDevice = deviceFactory.newEntity(SYS_SCOPE_ID);
        tmpCreator = deviceFactory.newCreator(SYS_SCOPE_ID, "TestDevice");
        tmpQuery = deviceFactory.newQuery(SYS_SCOPE_ID);
        tmpListRes = deviceFactory.newListResult();

        assertNotNull(tmpDevice);
        assertNotNull(tmpCreator);
        assertNotNull(tmpQuery);
        assertNotNull(tmpListRes);
    }

    // *******************
    // * Private Helpers *
    // *******************

    // Create a device creator object. The creator is pre-filled with default data.
    private DeviceCreator prepareRegularDeviceCreator(KapuaId accountId, String client) {

//        DeviceCreatorImpl tmpDeviceCreator = new DeviceCreatorImpl(accountId);
        DeviceCreatorImpl tmpDeviceCreator = DeviceCreatorProxy.newCreator(accountId);

        tmpDeviceCreator.setClientId(client);
        tmpDeviceCreator.setConnectionId(new KapuaEid(IdGenerator.generate()));
        tmpDeviceCreator.setDisplayName(TEST_DEVICE_NAME);
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

    /**
     * Set up the preconditions for unit tests. This includes filling the mock locator with the correct
     * mocked services and the actual service implementation under test.
     * Also, all the unit tests will be run with the kapua-sys user.
     *
     * @throws Exception
     */
    private void setupMockLocatorForDeviceService() {
        MockedLocator mockedLocator = (MockedLocator) KapuaLocator.getInstance();

        AbstractModule module = new AbstractModule() {

            @Override
            protected void configure() {

                // Inject mocked Authorization Service method checkPermission
                AuthorizationService mockedAuthorization = Mockito.mock(AuthorizationService.class);
                try {
                    Mockito.doNothing().when(mockedAuthorization).checkPermission(Matchers.any(org.eclipse.kapua.service.authorization.permission.Permission.class));
                } catch (KapuaException e) {
                    // skip
                }
                bind(AuthorizationService.class).toInstance(mockedAuthorization);
                // Inject mocked Permission Factory
//                PermissionFactory mockedPermissionFactory = Mockito.mock(PermissionFactory.class);
//                bind(PermissionFactory.class).toInstance(mockedPermissionFactory);

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
