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

import static org.eclipse.kapua.commons.model.query.predicate.AttributePredicate.attributeIsEqualTo;
import static org.eclipse.kapua.commons.model.query.predicate.AttributePredicate.attributeIsNotEqualTo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.shiro.SecurityUtils;
import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.configuration.metatype.KapuaMetatypeFactoryImpl;
import org.eclipse.kapua.commons.model.id.IdGenerator;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.model.id.KapuaIdFactoryImpl;
import org.eclipse.kapua.commons.model.query.FieldSortCriteria;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.security.KapuaSession;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.message.KapuaPosition;
import org.eclipse.kapua.message.device.lifecycle.KapuaAppsChannel;
import org.eclipse.kapua.message.device.lifecycle.KapuaAppsMessage;
import org.eclipse.kapua.message.device.lifecycle.KapuaAppsPayload;
import org.eclipse.kapua.message.device.lifecycle.KapuaBirthChannel;
import org.eclipse.kapua.message.device.lifecycle.KapuaBirthMessage;
import org.eclipse.kapua.message.device.lifecycle.KapuaBirthPayload;
import org.eclipse.kapua.message.device.lifecycle.KapuaDisconnectChannel;
import org.eclipse.kapua.message.device.lifecycle.KapuaDisconnectMessage;
import org.eclipse.kapua.message.device.lifecycle.KapuaDisconnectPayload;
import org.eclipse.kapua.message.device.lifecycle.KapuaMissingChannel;
import org.eclipse.kapua.message.device.lifecycle.KapuaMissingMessage;
import org.eclipse.kapua.message.device.lifecycle.KapuaMissingPayload;
import org.eclipse.kapua.message.internal.KapuaPositionImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaAppsChannelImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaAppsMessageImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaAppsPayloadImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaBirthChannelImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaBirthMessageImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaBirthPayloadImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaDisconnectChannelImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaDisconnectMessageImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaDisconnectPayloadImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaMissingChannelImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaMissingMessageImpl;
import org.eclipse.kapua.message.internal.device.lifecycle.KapuaMissingPayloadImpl;
import org.eclipse.kapua.model.config.metatype.KapuaMetatypeFactory;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.id.KapuaIdFactory;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.model.query.predicate.KapuaAttributePredicate;
import org.eclipse.kapua.qa.base.TestBase;
import org.eclipse.kapua.qa.base.DBHelper;
import org.eclipse.kapua.qa.base.TestData;
import org.eclipse.kapua.qa.base.TestJAXBContextProvider;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.authorization.permission.shiro.PermissionFactoryImpl;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.device.registry.DeviceCreator;
import org.eclipse.kapua.service.device.registry.DeviceFactory;
import org.eclipse.kapua.service.device.registry.DeviceListResult;
import org.eclipse.kapua.service.device.registry.DevicePredicates;
import org.eclipse.kapua.service.device.registry.DeviceQuery;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.device.registry.DeviceStatus;
import org.eclipse.kapua.service.device.registry.event.DeviceEventListResult;
import org.eclipse.kapua.service.device.registry.event.DeviceEventQuery;
import org.eclipse.kapua.service.device.registry.event.DeviceEventService;
import org.eclipse.kapua.service.device.registry.event.internal.DeviceEventListResultImpl;
import org.eclipse.kapua.service.device.registry.event.internal.DeviceEventQueryImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceCreatorImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceCreatorProxy;
import org.eclipse.kapua.service.device.registry.internal.DeviceEntityManagerFactory;
import org.eclipse.kapua.service.device.registry.internal.DeviceFactoryImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceListResultImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceQueryImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceRegistryServiceImpl;
import org.eclipse.kapua.service.device.registry.lifecycle.DeviceLifeCycleService;
import org.eclipse.kapua.service.tag.Tag;
import org.eclipse.kapua.service.tag.TagCreator;
import org.eclipse.kapua.service.tag.TagListResult;
import org.eclipse.kapua.service.tag.TagService;
import org.eclipse.kapua.service.tag.internal.TagFactoryImpl;
import org.eclipse.kapua.service.tag.internal.TagPredicates;
import org.eclipse.kapua.qa.base.TestConfig;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.eclipse.kapua.test.MockedLocator;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Implementation of Gherkin steps used in DeviceRegistryI9n.feature scenarios.
@ScenarioScoped
public class DeviceServiceSteps extends TestBase {

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

    private static final Logger logger = LoggerFactory.getLogger(DeviceServiceSteps.class);

    // Device registry services
    private DeviceRegistryService deviceRegistryService;
    private DeviceFactory deviceFactory;
    private DeviceEventService deviceEventsService;
    private DeviceLifeCycleService deviceLifeCycleService;
    private TagService tagService;

    @Inject
    public DeviceServiceSteps(TestData stepData, DBHelper dbHelper) {
        this.database = dbHelper;
        this.stepData = stepData;
    }

    // Database setup and tear-down steps
    @Before
    public void beforeScenario(Scenario scenario) throws KapuaException {

        // Initialize the database
        this.database.setup();

        // Find all the required services with the default Locator
        KapuaLocator locator = KapuaLocator.getInstance();
        deviceRegistryService = locator.getService(DeviceRegistryService.class);
        deviceFactory = locator.getFactory(DeviceFactory.class);
        deviceEventsService = locator.getService(DeviceEventService.class);
        deviceLifeCycleService = locator.getService(DeviceLifeCycleService.class);
        tagService = locator.getService(TagService.class);

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
    public void afterScenario() {

        // Clean up the database
        try {
            logger.info("Logging out in cleanup");
            if (isIntegrationTest()) {
                database.deleteAll();
                SecurityUtils.getSubject().logout();
            } else {
                database.dropAll();
            }
            KapuaSecurityUtils.clearSession();
        } catch (Exception e) {
            logger.error("Failed to log out in @After", e);
        }
    }

    // Cucumber test steps

    @Given("^A birth message from device \"(.+)\"$")
    public void createABirthMessage(String clientId)
            throws KapuaException {

        Account tmpAccount = (Account) stepData.get("LastAccount");

        assertNotNull(clientId);
        assertFalse(clientId.isEmpty());
        assertNotNull(tmpAccount);
        assertNotNull(tmpAccount.getId());

        Device tmpDev;
        List<String> tmpSemParts = new ArrayList<>();
        KapuaBirthMessage tmpMsg = new KapuaBirthMessageImpl();
        KapuaBirthChannel tmpChan = new KapuaBirthChannelImpl();
        KapuaBirthPayload tmpPayload = prepareDefaultBirthPayload();

        tmpChan.setClientId(clientId);
        tmpSemParts.add("part1");
        tmpSemParts.add("part2");
        tmpChan.setSemanticParts(tmpSemParts);

        tmpMsg.setChannel(tmpChan);
        tmpMsg.setPayload(tmpPayload);
        tmpMsg.setScopeId(tmpAccount.getId());
        tmpMsg.setClientId(clientId);
        tmpMsg.setId(UUID.randomUUID());
        tmpMsg.setReceivedOn(new Date());
        tmpMsg.setPosition(getDefaultPosition());

        tmpDev = deviceRegistryService.findByClientId(tmpAccount.getId(), clientId);
        if (tmpDev != null) {
            tmpMsg.setDeviceId(tmpDev.getId());
        } else {
            tmpMsg.setDeviceId(null);
        }

        deviceLifeCycleService.birth(getRandomId(), tmpMsg);
    }

    @Given("^A disconnect message from device \"(.+)\"$")
    public void createADeathMessage(String clientId)
            throws Exception {

        Account tmpAccount = (Account) stepData.get("LastAccount");
        Device tmpDev;
        List<String> tmpSemParts = new ArrayList<>();
        KapuaDisconnectMessage tmpMsg = new KapuaDisconnectMessageImpl();
        KapuaDisconnectChannel tmpChan = new KapuaDisconnectChannelImpl();
        KapuaDisconnectPayload tmpPayload = prepareDefaultDeathPayload();

        tmpChan.setClientId(clientId);
        tmpSemParts.add("part1");
        tmpSemParts.add("part2");
        tmpChan.setSemanticParts(tmpSemParts);

        tmpMsg.setChannel(tmpChan);
        tmpMsg.setPayload(tmpPayload);
        tmpMsg.setScopeId(tmpAccount.getId());
        tmpMsg.setClientId(clientId);
        tmpMsg.setId(UUID.randomUUID());
        tmpMsg.setReceivedOn(new Date());
        tmpMsg.setPosition(getDefaultPosition());

        tmpDev = deviceRegistryService.findByClientId(tmpAccount.getId(), clientId);
        if (tmpDev != null) {
            tmpMsg.setDeviceId(tmpDev.getId());
        } else {
            tmpMsg.setDeviceId(null);
        }

        try {
            primeException();
            deviceLifeCycleService.death(getRandomId(), tmpMsg);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^A missing message from device \"(.+)\"$")
    public void createAMissingMessage(String clientId)
            throws Exception {

        Account tmpAccount = (Account) stepData.get("LastAccount");
        Device tmpDev;
        List<String> tmpSemParts = new ArrayList<>();
        KapuaMissingMessage tmpMsg = new KapuaMissingMessageImpl();
        KapuaMissingChannel tmpChan = new KapuaMissingChannelImpl();
        KapuaMissingPayload tmpPayload = prepareDefaultMissingPayload();

        tmpChan.setClientId(clientId);
        tmpSemParts.add("part1");
        tmpSemParts.add("part2");
        tmpChan.setSemanticParts(tmpSemParts);

        tmpMsg.setChannel(tmpChan);
        tmpMsg.setPayload(tmpPayload);
        tmpMsg.setScopeId(tmpAccount.getId());
        tmpMsg.setId(UUID.randomUUID());
        tmpMsg.setReceivedOn(new Date());
        tmpMsg.setPosition(getDefaultPosition());

        tmpDev = deviceRegistryService.findByClientId(tmpAccount.getId(), clientId);
        if (tmpDev != null) {
            tmpMsg.setDeviceId(tmpDev.getId());
        } else {
            tmpMsg.setDeviceId(null);
        }

        try {
            primeException();
            deviceLifeCycleService.missing(getRandomId(), tmpMsg);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^An application message from device \"(.+)\"$")
    public void createAnApplicationMessage(String clientId)
            throws Exception {

        Account tmpAccount = (Account) stepData.get("LastAccount");
        Device tmpDev;
        List<String> tmpSemParts = new ArrayList<>();
        KapuaAppsMessage tmpMsg = new KapuaAppsMessageImpl();
        KapuaAppsChannel tmpChan = new KapuaAppsChannelImpl();
        KapuaAppsPayload tmpPayload = prepareDefaultApplicationPayload();

        tmpChan.setClientId(clientId);
        tmpSemParts.add("part1");
        tmpSemParts.add("part2");
        tmpChan.setSemanticParts(tmpSemParts);

        tmpMsg.setChannel(tmpChan);
        tmpMsg.setPayload(tmpPayload);
        tmpMsg.setScopeId(tmpAccount.getId());
        tmpMsg.setId(UUID.randomUUID());
        tmpMsg.setReceivedOn(new Date());
        tmpMsg.setPosition(getDefaultPosition());

        tmpDev = deviceRegistryService.findByClientId(tmpAccount.getId(), clientId);
        if (tmpDev != null) {
            tmpMsg.setDeviceId(tmpDev.getId());
        } else {
            tmpMsg.setDeviceId(null);
        }

        try {
            primeException();
            deviceLifeCycleService.applications(getRandomId(), tmpMsg);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I configure the device service$")
    public void setDeviceServiceConfig(List<TestConfig> testConfigs)
            throws Exception {

        Account tmpAccount = (Account) stepData.get("LastAccount");
        Map<String, Object> valueMap = new HashMap<>();

        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
        }
        try {
            primeException();
            deviceRegistryService.setConfigValues(tmpAccount.getId(), tmpAccount.getScopeId(), valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I configure the tag service$")
    public void setTagServiceConfig(List<TestConfig> testConfigs)
            throws Exception {

        Account tmpAccount = (Account) stepData.get("LastAccount");
        Map<String, Object> valueMap = new HashMap<>();

        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
        }
        try {
            primeException();
            tagService.setConfigValues(tmpAccount.getId(), tmpAccount.getScopeId(), valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^(?:A d|D)evices? such as$")
    public void createADevicesAsSpecified(List<CucDevice> devLst)
            throws KapuaException {

        KapuaSecurityUtils.doPrivileged(() -> {
            assertNotNull(devLst);

            Device tmpDevice = null;
            for (CucDevice tmpCDev : devLst) {
                tmpCDev.parse();
                DeviceCreator devCr = prepareDeviceCreatorFromCucDevice(tmpCDev);
                tmpDevice = deviceRegistryService.create(devCr);
            }
            stepData.put("LastDevice", tmpDevice);
        });
    }

    @When("^I search for the device \"(.+)\" in account \"(.+)\"$")
    public void searchForDeviceWithClientID(String clientId, String account)
            throws KapuaException {

        Account tmpAcc;
        Device tmpDev;
        DeviceListResult tmpList = new DeviceListResultImpl();

        tmpAcc = KapuaLocator.getInstance().getService(AccountService.class).findByName(account);
        assertNotNull(tmpAcc);
        assertNotNull(tmpAcc.getId());

        tmpDev = deviceRegistryService.findByClientId(tmpAcc.getId(), clientId);
        if (tmpDev != null) {
            Vector<Device> dv = new Vector<>();
            dv.add(tmpDev);
            tmpList.addItems(dv);
            stepData.put("Device", tmpDev);
            stepData.put("DeviceList", tmpList);
        }
    }

    @Then("^I find device \"([^\"]*)\"$")
    public void iFindDeviceWithTag(String deviceName) throws Throwable {

        DeviceListResult deviceList = (DeviceListResult) stepData.get("DeviceList");
        Device device = deviceList.getFirstItem();

        assertEquals(deviceName, device.getClientId());
    }

    @Then("^There is no such device$")
    public void deviceMustBeNull() {
        assertNull(stepData.get("Device"));
    }

    @Then("^The device has a non-null ID$")
    public void checkCreatedDeviceId() {

        Device origDevice = (Device) stepData.get("Device");
        DeviceCreator devCr = (DeviceCreator) stepData.get("DeviceCreator");

        assertNotNull(origDevice.getId());
        assertEquals(devCr.getScopeId(), origDevice.getScopeId());
        assertEquals(devCr.getClientId(), origDevice.getClientId());
    }

    @When("^I query for devices with BIOS version \"(.*)\"$")
    public void queryForDevicesBasedOnBiosVersion(String version)
            throws Exception {

        DeviceListResult tmpList;
        DeviceQuery tmpQuery = new DeviceQueryImpl(SYS_SCOPE_ID);
        tmpQuery.setPredicate(attributeIsEqualTo("biosVersion", version));

        // Search for the known bios version string
        try {
            primeException();
            tmpList = deviceRegistryService.query(tmpQuery);
            assertNotNull(tmpList);
            stepData.put("DeviceList", tmpList);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for devices with BIOS different from \"(.*)\"$")
    public void queryForDevicesWithDifferentBiosVersion(String version)
            throws Exception {

        DeviceListResult tmpList;
        DeviceQuery tmpQuery = new DeviceQueryImpl(SYS_SCOPE_ID);
        tmpQuery.setPredicate(attributeIsNotEqualTo("biosVersion", version));

        // Search for the known bios version string
        try {
            primeException();
            tmpList = deviceRegistryService.query(tmpQuery);
            assertNotNull(tmpList);
            stepData.put("DeviceList", tmpList);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for devices with Client Id \"(.*)\"$")
    public void queryForDevicesBasedOnClientId(String id)
            throws Exception {

        DeviceListResult tmpList;
        DeviceQuery tmpQuery = new DeviceQueryImpl(SYS_SCOPE_ID);
        tmpQuery.setPredicate(attributeIsEqualTo("clientId", id));

        // Search for the known client id string
        try {
            primeException();
            tmpList = deviceRegistryService.query(tmpQuery);
            assertNotNull(tmpList);
            stepData.put("DeviceList", tmpList);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @And("^I extract the first device$")
    public void getFirstDeviceFromList() {
        // A device should have been found
        DeviceListResult tmpList = (DeviceListResult) stepData.get("DeviceList");
        assertNotEquals(0, tmpList.getSize());

        Device tmpDev = tmpList.getFirstItem();
        assertNotNull(tmpDev);

        stepData.put("Device", tmpDev);
    }

    @When("^I count the devices in scope (\\d+)$")
    public void countDevicesInScope(int scope)
            throws Exception {

        DeviceQuery tmpQuery = new DeviceQueryImpl(new KapuaEid(BigInteger.valueOf(scope)));

        try {
            primeException();
            long count = deviceRegistryService.count(tmpQuery);
            stepData.put("Count", count);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @When("^I count devices with BIOS version \"(.*)\"$")
    public void countDevicesWithBIOSVersion(String version)
            throws Exception {

        DeviceQuery tmpQuery = new DeviceQueryImpl(SYS_SCOPE_ID);
        assertNotNull(tmpQuery);
        tmpQuery.setPredicate(attributeIsEqualTo("biosVersion", version));
        stepData.remove("Count");

        try {
            primeException();
            long count = deviceRegistryService.count(tmpQuery);
            stepData.put("Count", count);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @When("^I update some device parameters$")
    public void updateDeviceParameters()
            throws Exception {

        Device origDev = (Device) stepData.get("Device");
        origDev.setBiosVersion(origDev.getBiosVersion() + "_upd");
        origDev.setCustomAttribute1(origDev.getCustomAttribute1() + "_upd");

        try {
            primeException();
            Device tmpDev = deviceRegistryService.update(origDev);
            stepData.put("Device", tmpDev);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @When("^I update the device cleint ID to \"(.+)\"$")
    public void updateDeviceClientId(String newId)
            throws Exception {

        Device origDev = (Device) stepData.get("Device");
        stepData.put("StringValue", origDev.getClientId());
        origDev.setClientId(newId);

        try {
            primeException();
            Device tmpDev = deviceRegistryService.update(origDev);
            stepData.put("Device", tmpDev);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @When("^I update a device with an invalid ID$")
    public void updateDeviceWithInvalidId()
            throws Exception {

        Device origDev = (Device) stepData.get("Device");
        origDev.setId(getRandomId());

        try {
            primeException();
            Device tmpDev = deviceRegistryService.update(origDev);
            stepData.put("Device", tmpDev);
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
    }

    @When("^I delete the device with the client id \"(.+)\"$")
    public void deleteDeviceWithClientId(String clientId)
            throws Exception {

        try {
            primeException();
            Device tmpDev = deviceRegistryService.findByClientId(SYS_SCOPE_ID, clientId);
            assertNotNull(tmpDev);
            deviceRegistryService.delete(SYS_SCOPE_ID, tmpDev.getId());
        } catch (KapuaEntityNotFoundException ex) {
            verifyException(ex);
        }
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

    @Then("^It is possible to find the device based on its registry ID$")
    public void fincDeviceByRememberedId()
            throws KapuaException {

        Device origDev = (Device) stepData.get("Device");
        Device tmpDev = deviceRegistryService.find(SYS_SCOPE_ID, origDev.getId());

        assertNotNull(tmpDev);
        assertEquals(origDev.getClientId(), tmpDev.getClientId());
    }

    @Then("^It is possible to find the device based on its client ID$")
    public void fincDeviceByRememberedClientId()
            throws KapuaException {

        Device origDev = (Device) stepData.get("Device");
        Device tmpDev = deviceRegistryService.findByClientId(SYS_SCOPE_ID, origDev.getClientId());

        assertNotNull(tmpDev);
        assertEquals(origDev.getId(), tmpDev.getId());
    }

    @Then("^Named device registry searches are case sesntitive$")
    public void checkCaseSensitivnessOfRegistrySearches()
            throws KapuaException {

        DeviceCreator deviceCreator = (DeviceCreator) stepData.get("DeviceCreator");
        assertNull(deviceRegistryService.findByClientId(SYS_SCOPE_ID, deviceCreator.getClientId().toLowerCase()));
        assertNull(deviceRegistryService.findByClientId(SYS_SCOPE_ID, deviceCreator.getClientId().toUpperCase()));
        assertNotNull(deviceRegistryService.findByClientId(SYS_SCOPE_ID, deviceCreator.getClientId()));
    }

    @Then("^The device matches the creator parameters$")
    public void checkCreatedDeviceAgainstCreatorParameters() {

        DeviceCreator origCreator = (DeviceCreator) stepData.get("DeviceCreator");
        Device origDevice = (Device) stepData.get("Device");

        assertNotNull(origDevice.getId());
        assertEquals(origCreator.getScopeId(), origDevice.getScopeId());
        assertEquals(origCreator.getClientId().length(), origDevice.getClientId().length());
        assertEquals(origCreator.getClientId(), origDevice.getClientId());
        assertEquals(origCreator.getConnectionId(), origDevice.getConnectionId());
        assertEquals(origCreator.getDisplayName(), origDevice.getDisplayName());
        assertEquals(origCreator.getSerialNumber(), origDevice.getSerialNumber());
        assertEquals(origCreator.getModelId(), origDevice.getModelId());
        assertEquals(origCreator.getImei(), origDevice.getImei());
        assertEquals(origCreator.getImsi(), origDevice.getImsi());
        assertEquals(origCreator.getIccid(), origDevice.getIccid());
        assertEquals(origCreator.getBiosVersion(), origDevice.getBiosVersion());
        assertEquals(origCreator.getFirmwareVersion(), origDevice.getFirmwareVersion());
        assertEquals(origCreator.getOsVersion(), origDevice.getOsVersion());
        assertEquals(origCreator.getJvmVersion(), origDevice.getJvmVersion());
        assertEquals(origCreator.getOsgiFrameworkVersion(), origDevice.getOsgiFrameworkVersion());
        assertEquals(origCreator.getApplicationFrameworkVersion(), origDevice.getApplicationFrameworkVersion());
        assertEquals(origCreator.getApplicationIdentifiers(), origDevice.getApplicationIdentifiers());
        assertEquals(origCreator.getAcceptEncoding(), origDevice.getAcceptEncoding());
        assertEquals(origCreator.getCustomAttribute1(), origDevice.getCustomAttribute1());
        assertEquals(origCreator.getCustomAttribute2(), origDevice.getCustomAttribute2());
        assertEquals(origCreator.getCustomAttribute3(), origDevice.getCustomAttribute3());
        assertEquals(origCreator.getCustomAttribute4(), origDevice.getCustomAttribute4());
        assertEquals(origCreator.getCustomAttribute5(), origDevice.getCustomAttribute5());
        assertEquals(origCreator.getStatus(), origDevice.getStatus());
    }

    @Then("^The device was correctly updated$")
    public void checkUpdatedDeviceAgainstOriginal()
            throws KapuaException {

        Device tmpDevice;
        Device origDevice = (Device) stepData.get("Device");

        tmpDevice = deviceRegistryService.find(origDevice.getScopeId(), origDevice.getId());
        assertNotNull(tmpDevice);

        assertEquals(tmpDevice.getScopeId(), origDevice.getScopeId());
        assertEquals(tmpDevice.getClientId().length(), origDevice.getClientId().length());
        assertEquals(tmpDevice.getClientId(), origDevice.getClientId());
        assertEquals(tmpDevice.getConnectionId(), origDevice.getConnectionId());
        assertEquals(tmpDevice.getDisplayName(), origDevice.getDisplayName());
        assertEquals(tmpDevice.getSerialNumber(), origDevice.getSerialNumber());
        assertEquals(tmpDevice.getModelId(), origDevice.getModelId());
        assertEquals(tmpDevice.getImei(), origDevice.getImei());
        assertEquals(tmpDevice.getImsi(), origDevice.getImsi());
        assertEquals(tmpDevice.getIccid(), origDevice.getIccid());
        assertEquals(tmpDevice.getBiosVersion(), origDevice.getBiosVersion());
        assertEquals(tmpDevice.getFirmwareVersion(), origDevice.getFirmwareVersion());
        assertEquals(tmpDevice.getOsVersion(), origDevice.getOsVersion());
        assertEquals(tmpDevice.getJvmVersion(), origDevice.getJvmVersion());
        assertEquals(tmpDevice.getOsgiFrameworkVersion(), origDevice.getOsgiFrameworkVersion());
        assertEquals(tmpDevice.getApplicationFrameworkVersion(), origDevice.getApplicationFrameworkVersion());
        assertEquals(tmpDevice.getApplicationIdentifiers(), origDevice.getApplicationIdentifiers());
        assertEquals(tmpDevice.getAcceptEncoding(), origDevice.getAcceptEncoding());
        assertEquals(tmpDevice.getCustomAttribute1(), origDevice.getCustomAttribute1());
        assertEquals(tmpDevice.getCustomAttribute2(), origDevice.getCustomAttribute2());
        assertEquals(tmpDevice.getCustomAttribute3(), origDevice.getCustomAttribute3());
        assertEquals(tmpDevice.getCustomAttribute4(), origDevice.getCustomAttribute4());
        assertEquals(tmpDevice.getCustomAttribute5(), origDevice.getCustomAttribute5());
        assertEquals(tmpDevice.getStatus(), origDevice.getStatus());
    }

    @Then("^The device client id is \"(.*)\"$")
    public void checkDeviceClientName(String name)
            throws KapuaException {
        assertEquals(name, ((Device) stepData.get("Device")).getClientId());
    }

    @Then("^I find (\\d+) devices?$")
    public void checkDeviceListLength(int cnt) {
        assertNotNull(stepData.get("DeviceList"));
        assertEquals(cnt, ((DeviceListResultImpl) stepData.get("DeviceList")).getSize());
    }

    @Then("^There (?:are|is) (\\d+) devices?$")
    public void checkNumberOfDevices(int number) {
        assertEquals((long) number, (long) stepData.get("Count"));
    }

    @Then("^The client ID was not changed$")
    public void checkDeviceClientIdForChanges()
            throws KapuaException {

        Device origDevice = (Device) stepData.get("Device");
        Device tmpDevice = deviceRegistryService.find(SYS_SCOPE_ID, origDevice.getId());
        assertNotEquals(origDevice.getClientId(), tmpDevice.getClientId());
        assertEquals((String) stepData.get("StringValue"), tmpDevice.getClientId());
    }

    @Then("^There is no device with the client ID \"(.+)\"$")
    public void checkWhetherNamedDeviceStillExists(String clientId)
            throws KapuaException {
        Device tmpDevice = deviceRegistryService.findByClientId(SYS_SCOPE_ID, clientId);
        assertNull(tmpDevice);
    }

    @And("^I tag device with \"([^\"]*)\" tag$")
    public void iTagDeviceWithTag(String deviceTagName) throws Throwable {

        Device device = (Device) stepData.get("Device");
        // stepData.clear();
        TagCreator tagCreator = new TagFactoryImpl().newCreator(SYS_SCOPE_ID);
        tagCreator.setName(deviceTagName);
        Tag tag = tagService.create(tagCreator);
        Set<KapuaId> tags = new HashSet<>();
        try {
            stepData.put("ExceptionCaught", false);
            tags.add(tag.getId());
            device.setTagIds(tags);
            deviceRegistryService.update(device);
            stepData.put("tag", tag);
            stepData.put("tags", tags);
        } catch (KapuaException ex) {
            stepData.put("ExceptionCaught", true);
        }
    }

    @When("^I search for device with tag \"([^\"]*)\"$")
    public void iSearchForDeviceWithTag(String deviceTagName) throws Throwable {

        Account lastAcc = (Account) stepData.get("LastAccount");
        DeviceQueryImpl deviceQuery = new DeviceQueryImpl(lastAcc.getId());

        KapuaQuery<Tag> tagQuery = new TagFactoryImpl().newQuery(SYS_SCOPE_ID);
        tagQuery.setPredicate(new AttributePredicate<String>(TagPredicates.NAME, deviceTagName, KapuaAttributePredicate.Operator.EQUAL));
        TagListResult tagQueryResult = tagService.query(tagQuery);
        Tag tag = tagQueryResult.getFirstItem();
        deviceQuery.setPredicate(attributeIsEqualTo(DevicePredicates.TAG_IDS, tag.getId()));
        DeviceListResult deviceList = (DeviceListResult) deviceRegistryService.query(deviceQuery);

        stepData.put("DeviceList", deviceList);
    }

    @And("^I untag device with \"([^\"]*)\" tag$")
    public void iDeleteTag(String deviceTagName) throws Throwable {

        Tag foundTag = (Tag) stepData.get("tag");
        assertEquals(deviceTagName, foundTag.getName());
        Device device = (Device) stepData.get("Device");
        stepData.remove("tag");
        stepData.remove("tags");
        Set<KapuaId> tags = new HashSet<>();
        device.setTagIds(tags);
        deviceRegistryService.update(device);
        assertEquals(device.getTagIds().isEmpty(), true);
    }

    @And("^I verify that tag \"([^\"]*)\" is deleted$")
    public void iVerifyTagIsDeleted(String deviceTagName) throws Throwable {

        Tag foundTag = (Tag) stepData.get("tag");
        assertEquals(null, foundTag);
    }

    @When("^I search for events from device \"(.+)\" in account \"(.+)\"$")
    public void searchForEventsFromDeviceWithClientID(String clientId, String account)
            throws KapuaException {

        DeviceEventQuery tmpQuery;
        Device tmpDev;
        DeviceEventListResult tmpList;
        Account tmpAcc;

        tmpAcc = KapuaLocator.getInstance().getService(AccountService.class).findByName(account);
        assertNotNull(tmpAcc);
        assertNotNull(tmpAcc.getId());

        tmpDev = deviceRegistryService.findByClientId(tmpAcc.getId(), clientId);
        assertNotNull(tmpDev);
        assertNotNull(tmpDev.getId());

        tmpQuery = new DeviceEventQueryImpl(tmpAcc.getId());
        tmpQuery.setPredicate(attributeIsEqualTo("deviceId", tmpDev.getId()));
        tmpQuery.setSortCriteria(new FieldSortCriteria("receivedOn", FieldSortCriteria.SortOrder.ASCENDING));
        tmpList = deviceEventsService.query(tmpQuery);

        assertNotNull(tmpList);
        stepData.put("DeviceEventList", tmpList);
    }

    @Then("^I find (\\d+) events?$")
    public void checkEventListLength(int cnt) {
        assertNotNull(stepData.get("DeviceEventList"));
        assertEquals(cnt, ((DeviceEventListResultImpl) stepData.get("DeviceEventList")).getSize());
    }

    @Then("^The type of the last event is \"(.+)\"$")
    public void checkLastEventType(String type) {
        DeviceEventListResult tmpList;

        assertNotNull(stepData.get("DeviceEventList"));
        assertNotEquals(0, ((DeviceEventListResultImpl) stepData.get("DeviceEventList")).getSize());
        tmpList = (DeviceEventListResultImpl) stepData.get("DeviceEventList");
        assertEquals(type.trim().toUpperCase(), tmpList.getItem(tmpList.getSize() - 1).getResource().trim().toUpperCase());
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

    private KapuaPosition getDefaultPosition() {
        KapuaPosition tmpPos = new KapuaPositionImpl();

        tmpPos.setAltitude(250.0);
        tmpPos.setHeading(90.0);
        tmpPos.setLatitude(45.5);
        tmpPos.setLongitude(13.6);
        tmpPos.setPrecision(0.3);
        tmpPos.setSatellites(12);
        tmpPos.setSpeed(120.0);
        tmpPos.setStatus(2);
        tmpPos.setTimestamp(new Date());

        return tmpPos;
    }

    private KapuaBirthPayload prepareDefaultBirthPayload() {
        KapuaBirthPayload tmpPayload = new KapuaBirthPayloadImpl(
                "500", // uptime
                "ReliaGate 10-20", // displayName
                "ReliaGate", // modelName
                "ReliaGate 10-20", // modelId
                "ABC123456", // partNumber
                "12312312312", // serialNumber
                "Kura", // firmware
                "2.0", // firmwareVersion
                "BIOStm", // bios
                "1.2.3", // biosVersion
                "linux", // os
                "4.9.18", // osVersion
                "J9", // jvm
                "2.4", // jvmVersion
                "J8SE", // jvmProfile
                "OSGi", // containerFramework
                "1.2.3", // containerFrameworkVersion
                "Kura", // applicationFramework
                "2.0", // applicationFrameworkVersion
                "eth0", // connectionInterface
                "192.168.1.2", // connectionIp
                "gzip", // acceptEncoding
                "CLOUD-V1", // applicationIdentifiers
                "1", // availableProcessors
                "1024", // totalMemory
                "linux", // osArch
                "123456789ABCDEF", // modemImei
                "123456789", // modemImsi
                "ABCDEF" // modemIccid
        );

        return tmpPayload;
    }

    private KapuaDisconnectPayload prepareDefaultDeathPayload() {
        KapuaDisconnectPayload tmpPayload = new KapuaDisconnectPayloadImpl(
                "1000", // uptime
                "ReliaGate 10-20" // displayName
        );

        return tmpPayload;
    }

    private KapuaMissingPayload prepareDefaultMissingPayload() {
        KapuaMissingPayload tmpPayload = new KapuaMissingPayloadImpl();
        return tmpPayload;
    }

    private KapuaAppsPayload prepareDefaultApplicationPayload() {
        KapuaAppsPayload tmpPayload = new KapuaAppsPayloadImpl(
                "500", // uptime
                "ReliaGate 10-20", // displayName
                "ReliaGate", // modelName
                "ReliaGate 10-20", // modelId
                "ABC123456", // partNumber
                "12312312312", // serialNumber
                "Kura", // firmware
                "2.0", // firmwareVersion
                "BIOStm", // bios
                "1.2.3", // biosVersion
                "linux", // os
                "4.9.18", // osVersion
                "J9", // jvm
                "2.4", // jvmVersion
                "J8SE", // jvmProfile
                "OSGi", // containerFramework
                "1.2.3", // containerFrameworkVersion
                "Kura", // applicationFramework
                "2.0", // applicationFrameworkVersion
                "eth0", // connectionInterface
                "192.168.1.2", // connectionIp
                "gzip", // acceptEncoding
                "CLOUD-V1", // applicationIdentifiers
                "1", // availableProcessors
                "1024", // totalMemory
                "linux", // osArch
                "123456789ABCDEF", // modemImei
                "123456789", // modemImsi
                "ABCDEF" // modemIccid
        );

        return tmpPayload;
    }

    private DeviceCreator prepareDeviceCreatorFromCucDevice(CucDevice dev) {
        Account tmpAccount = (Account) stepData.get("LastAccount");
        DeviceCreator tmpCr;
        KapuaId tmpScope;

        if (dev.scopeId != null) {
            tmpScope = dev.getScopeId();
        } else {
            assertNotNull(tmpAccount);
            assertNotNull(tmpAccount.getId());
            tmpScope = tmpAccount.getId();
        }

        assertNotNull(dev.clientId);
        assertNotEquals(0, dev.clientId.length());

        tmpCr = prepareDefaultDeviceCreator(tmpScope, dev.clientId);

        if (dev.groupId != null) {
            tmpCr.setGroupId(dev.getGroupId());
        }
        if (dev.connectionId != null) {
            tmpCr.setConnectionId(dev.getConnectionId());
        }
        if (dev.displayName != null) {
            tmpCr.setDisplayName(dev.displayName);
        }
        if (dev.status != null) {
            tmpCr.setStatus(dev.getStatus());
        }
        if (dev.modelId != null) {
            tmpCr.setModelId(dev.modelId);
        }
        if (dev.serialNumber != null) {
            tmpCr.setSerialNumber(dev.serialNumber);
        }
        if (dev.imei != null) {
            tmpCr.setImei(dev.imei);
        }
        if (dev.imsi != null) {
            tmpCr.setImsi(dev.imsi);
        }
        if (dev.iccid != null) {
            tmpCr.setIccid(dev.iccid);
        }
        if (dev.biosVersion != null) {
            tmpCr.setBiosVersion(dev.biosVersion);
        }
        if (dev.firmwareVersion != null) {
            tmpCr.setFirmwareVersion(dev.firmwareVersion);
        }
        if (dev.osVersion != null) {
            tmpCr.setOsVersion(dev.osVersion);
        }
        if (dev.jvmVersion != null) {
            tmpCr.setJvmVersion(dev.jvmVersion);
        }
        if (dev.osgiFrameworkVersion != null) {
            tmpCr.setOsgiFrameworkVersion(dev.osgiFrameworkVersion);
        }
        if (dev.applicationFrameworkVersion != null) {
            tmpCr.setApplicationFrameworkVersion(dev.applicationFrameworkVersion);
        }
        if (dev.applicationIdentifiers != null) {
            tmpCr.setApplicationIdentifiers(dev.applicationIdentifiers);
        }
        if (dev.acceptEncoding != null) {
            tmpCr.setAcceptEncoding(dev.acceptEncoding);
        }

        return tmpCr;
    }

    private DeviceCreator prepareDefaultDeviceCreator(KapuaId scopeId, String clientId) {

//        DeviceCreator tmpDeviceCreator = KapuaLocator.getInstance().getFactory(DeviceFactory.class).newCreator(
//                scopeId,
//                clientId);

        DeviceCreatorImpl tmpDeviceCreator = DeviceCreatorProxy.newCreator(scopeId, clientId);

        tmpDeviceCreator.setClientId(clientId);
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
