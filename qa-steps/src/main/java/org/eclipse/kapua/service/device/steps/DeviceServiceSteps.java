/*******************************************************************************
 * Copyright (c) 2017, 2018 Eurotech and/or its affiliates and others
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

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.inject.Inject;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.id.IdGenerator;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.model.query.FieldSortCriteria;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicateImpl;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
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
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.qa.steps.BaseQATests;
import org.eclipse.kapua.qa.steps.DBHelper;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.TestJAXBContextProvider;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.device.registry.DeviceAttributes;
import org.eclipse.kapua.service.device.registry.DeviceCreator;
import org.eclipse.kapua.service.device.registry.DeviceFactory;
import org.eclipse.kapua.service.device.registry.DeviceListResult;
import org.eclipse.kapua.service.device.registry.DeviceQuery;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.device.registry.DeviceStatus;
import org.eclipse.kapua.service.device.registry.event.DeviceEventListResult;
import org.eclipse.kapua.service.device.registry.event.DeviceEventQuery;
import org.eclipse.kapua.service.device.registry.event.DeviceEventService;
import org.eclipse.kapua.service.device.registry.event.internal.DeviceEventListResultImpl;
import org.eclipse.kapua.service.device.registry.event.internal.DeviceEventQueryImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceListResultImpl;
import org.eclipse.kapua.service.device.registry.internal.DeviceQueryImpl;
import org.eclipse.kapua.service.device.registry.lifecycle.DeviceLifeCycleService;
import org.eclipse.kapua.service.tag.Tag;
import org.eclipse.kapua.service.tag.TagAttributes;
import org.eclipse.kapua.service.tag.TagCreator;
import org.eclipse.kapua.service.tag.TagFactory;
import org.eclipse.kapua.service.tag.TagListResult;
import org.eclipse.kapua.service.tag.TagQuery;
import org.eclipse.kapua.service.tag.TagService;
import org.eclipse.kapua.service.tag.internal.TagFactoryImpl;
import org.eclipse.kapua.service.tag.internal.TagQueryImpl;
import org.eclipse.kapua.service.user.steps.TestConfig;
import org.junit.Assert;
import org.springframework.security.crypto.codec.Hex;

// Implementation of Gherkin steps used in DeviceRegistryI9n.feature scenarios.
@ScenarioScoped
public class DeviceServiceSteps extends BaseQATests {

    protected static Random random = new Random();

    // Device registry services
    private DeviceRegistryService deviceRegistryService;
    private DeviceFactory deviceFactory;
    private DeviceEventService deviceEventsService;
    private DeviceLifeCycleService deviceLifeCycleService;
    private TagService tagService;
    private TagFactory tagFactory;
    private AccountService accountService;

    @Inject
    public DeviceServiceSteps(StepData stepData, DBHelper dbHelper) {
        this.database = dbHelper;
        this.stepData = stepData;
    }

    // Database setup and tear-down steps
    @Before
    public void beforeScenario(Scenario scenario) {

        // Find all the required services with the default Locator
        KapuaLocator locator = KapuaLocator.getInstance();
        deviceRegistryService = locator.getService(DeviceRegistryService.class);
        deviceFactory = locator.getFactory(DeviceFactory.class);
        deviceEventsService = locator.getService(DeviceEventService.class);
        deviceLifeCycleService = locator.getService(DeviceLifeCycleService.class);
        tagService = locator.getService(TagService.class);
        tagFactory = locator.getFactory(TagFactory.class);
        accountService = locator.getService(AccountService.class);

        this.scenario = scenario;

        // Initialize the database
        database.setup();

        stepData.clear();

        XmlUtil.setContextProvider(new TestJAXBContextProvider());
    }

    @After
    public void afterScenario() throws Exception {

        // Clean up the database
        database.deleteAll();
        KapuaSecurityUtils.clearSession();
    }

    // Cucumber test steps

    @Given("^A birth message from device \"(.+)\"$")
    public void createABirthMessage(String clientId)
            throws KapuaException {

        Account tmpAccount = (Account) stepData.get("LastAccount");

        Assert.assertNotNull(clientId);
        Assert.assertFalse(clientId.isEmpty());
        Assert.assertNotNull(tmpAccount);
        Assert.assertNotNull(tmpAccount.getId());

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

        deviceLifeCycleService.birth(generateRandomId(), tmpMsg);
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
            deviceLifeCycleService.death(generateRandomId(), tmpMsg);
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
            deviceLifeCycleService.missing(generateRandomId(), tmpMsg);
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
            deviceLifeCycleService.applications(generateRandomId(), tmpMsg);
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

    @When("^I search for the devices with the tag \"(.+)\"$")
    public void findDevicesWithTag(String tagName) throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        DeviceQuery devQuery = deviceFactory.newQuery(tmpAcc.getId());
        TagQuery tagQuery = tagFactory.newQuery(tmpAcc.getId());
        tagQuery.setPredicate(new AttributePredicateImpl<>(TagAttributes.NAME, tagName));

        TagListResult tmpTagList;
        DeviceListResult tmpDevList;

        try {
            primeException();
            stepData.remove("DeviceList");
            tmpTagList = tagService.query(tagQuery);
            Assert.assertNotNull("Required tag not found", tmpTagList);
            Assert.assertFalse("Required tag not found", tmpTagList.isEmpty());
            devQuery.setPredicate(new AttributePredicateImpl<>(DeviceAttributes.TAG_IDS, tmpTagList.getFirstItem().getId()));
            tmpDevList = deviceRegistryService.query(devQuery);
            stepData.put("DeviceList", tmpDevList);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I search for the devices that are tagged with the last tag$")
    public void findDeviceWithLastTag() throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        Tag tmpTag = (Tag) stepData.get("Tag");
        DeviceQuery tmpQuery = deviceFactory.newQuery(tmpTag.getScopeId());
        tmpQuery.setPredicate(new AttributePredicateImpl<>(DeviceAttributes.TAG_IDS, tmpTag.getId()));
        DeviceListResult tmpList;

        try {
            primeException();
            tmpList = deviceRegistryService.query(tmpQuery);
            stepData.put("DeviceList", tmpList);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^(?:A d|D)evices? such as$")
    public void createADevicesAsSpecified(List<CucDevice> devLst)
            throws KapuaException {

        KapuaSecurityUtils.doPrivileged(() -> {
            Assert.assertNotNull(devLst);

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

        Device tmpDev;
        DeviceListResult tmpList = new DeviceListResultImpl();

        Account tmpAcc = accountService.findByName(account);
        Assert.assertNotNull(tmpAcc);
        Assert.assertNotNull(tmpAcc.getId());

        stepData.remove("Device");
        stepData.remove("DeviceList");

        tmpDev = deviceRegistryService.findByClientId(tmpAcc.getId(), clientId);
        if (tmpDev != null) {
            Vector<Device> dv = new Vector<>();
            dv.add(tmpDev);
            tmpList.addItems(dv);
            stepData.put("Device", tmpDev);
            stepData.put("DeviceList", tmpList);
        }
    }

    @When("^I search for the device \"(.+)\" in the last account$")
    public void searchForDeviceWithClientIDInLastAccount(String clientId)
            throws KapuaException {

        Account tmpAcc;
        Device tmpDev;
        DeviceListResult tmpList = new DeviceListResultImpl();

        tmpAcc = (Account) stepData.get("LastAccount");
        Assert.assertNotNull(tmpAcc);
        Assert.assertNotNull(tmpAcc.getId());

        stepData.remove("Device");
        stepData.remove("DeviceList");

        tmpDev = deviceRegistryService.findByClientId(tmpAcc.getId(), clientId);
        if (tmpDev != null) {
            Vector<Device> dv = new Vector<>();
            dv.add(tmpDev);
            tmpList.addItems(dv);
            stepData.put("Device", tmpDev);
            stepData.put("DeviceList", tmpList);
        }
    }

    @And("^I tag device with \"([^\"]*)\" tag$")
    public void iTagDeviceWithTag(String deviceTagName) throws Throwable {

        Account account = (Account) stepData.get("LastAccount");
        Device device = (Device) stepData.get("Device");
        TagCreator tagCreator = new TagFactoryImpl().newCreator(account.getId());

        tagCreator.setName(deviceTagName);
        Tag tag = tagService.create(tagCreator);
        Set<KapuaId> tags = new HashSet<>();
        try {
            primeException();
            tags.add(tag.getId());
            device.setTagIds(tags);
            deviceRegistryService.update(device);
            stepData.put("Tag", tag);
            stepData.put("Tags", tags);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I tag device \"(.+)\" with the existing tag \"(.+)\"$")
    public void tagExistingDeviceWithExistingTag(String devName, String tagName)
            throws Exception {

        try {
            primeException();
            KapuaId tmpAccId = getLastAccountId();
            Device tmpDevice = deviceRegistryService.findByClientId(tmpAccId, devName);
            Assert.assertNotNull("Requested device not found", tmpDevice);

            TagQuery tagQuery = tagFactory.newQuery(tmpAccId);
            tagQuery.setPredicate(new AttributePredicateImpl<>(TagAttributes.NAME, tagName));
            TagListResult tagLst = tagService.query(tagQuery);
            Assert.assertNotNull("Requested tag not found", tagLst);
            Assert.assertNotEquals("Requested tag not found", 0, tagLst.getSize());
            Tag tmpTag = tagLst.getFirstItem();

            Set<KapuaId> devTagList = tmpDevice.getTagIds();
            devTagList.add(tmpTag.getId());
            tmpDevice.setTagIds(devTagList);
            deviceRegistryService.update(tmpDevice);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I search for device with tag \"([^\"]*)\"$")
    public void iSearchForDeviceWithTag(String deviceTagName) throws Throwable {

        Account lastAcc = (Account) stepData.get("LastAccount");
        DeviceQueryImpl deviceQuery = new DeviceQueryImpl(lastAcc.getId());

        KapuaQuery<Tag> tagQuery = new TagFactoryImpl().newQuery(lastAcc.getId());
        tagQuery.setPredicate(new AttributePredicateImpl<>(TagAttributes.NAME, deviceTagName, AttributePredicate.Operator.EQUAL));

        TagListResult tagQueryResult = tagService.query(tagQuery);
        Tag tag = tagQueryResult.getFirstItem();
        deviceQuery.setPredicate(AttributePredicateImpl.attributeIsEqualTo(DeviceAttributes.TAG_IDS, tag.getId()));
        DeviceListResult deviceList = deviceRegistryService.query(deviceQuery);

        stepData.put("DeviceList", deviceList);
    }

    @Then("^I find device \"([^\"]*)\"$")
    public void iFindDeviceWithTag(String deviceName) {

        DeviceListResult deviceList = (DeviceListResult) stepData.get("DeviceList");
        Device device = deviceList.getFirstItem();

        Assert.assertNotNull(device);
        Assert.assertEquals(deviceName, device.getClientId());
    }

    @Then("^Device \"(.+)\" has the tag \"(.+)\"$")
    public void checkThatDeviceHasTag(String deviceName, String tagName)
            throws Exception {

        try {
            primeException();
            KapuaId tmpAccId = getLastAccountId();
            Device tmpDevice = deviceRegistryService.findByClientId(tmpAccId, deviceName);
            Assert.assertNotNull("Requested device not found", tmpDevice);

            TagQuery tmpQuery = new TagQueryImpl(tmpDevice.getScopeId());
            tmpQuery.setPredicate(new AttributePredicateImpl<>(TagAttributes.NAME, tagName));
            Tag tmpTag = tagService.query(tmpQuery).getFirstItem();
            Assert.assertNotNull("Requested tag not found", tmpTag);

            Assert.assertTrue("The device does not have the required tag", tmpDevice.getTagIds().contains(tmpTag.getId()));
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^The device \"(.+)\" does not have the tag \"(.+)\"$")
    public void checkThatDeviceHasNoSuchTag(String deviceName, String tagName)
            throws Exception {

        try {
            primeException();
            KapuaId tmpAccId = getLastAccountId();
            Device tmpDevice = deviceRegistryService.findByClientId(tmpAccId, deviceName);
            Assert.assertNotNull("Requested device not found", tmpDevice);

            Set<KapuaId> tagList = tmpDevice.getTagIds();
            for(KapuaId tmpId : tagList) {
                Tag tmpTag = tagService.find(tmpDevice.getScopeId(), tmpId);
                Assert.assertNotEquals("The device still has the obsolete tag", tagName.trim(), tmpTag.getName());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^The device \"(.+)\" has (\\d+) tags$")
    public void checkThatDeviceHasANumberOfTags(String deviceName, int count)
            throws Exception {

        try {
            primeException();
            KapuaId tmpAccId = getLastAccountId();
            Device tmpDevice = deviceRegistryService.findByClientId(tmpAccId, deviceName);
            Assert.assertNotNull("Requested device not found", tmpDevice);
            Assert.assertEquals("The device has an unexpected number of tags", count, tmpDevice.getTagIds().size());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @And("^I untag device with \"([^\"]*)\" tag$")
    public void iDeleteTag(String deviceTagName) throws Throwable {

        Tag foundTag = (Tag) stepData.get("tag");
        Assert.assertEquals(deviceTagName, foundTag.getName());
        Device device = (Device) stepData.get("Device");
        stepData.remove("Tag");
        stepData.remove("Tags");
        Set<KapuaId> tags = new HashSet<>();
        device.setTagIds(tags);
        Device updatedDevice = deviceRegistryService.update(device);
        stepData.put("Device", updatedDevice);
        Assert.assertEquals(device.getTagIds().isEmpty(), true);
    }

    @And("^I verify that tag \"([^\"]*)\" is deleted$")
    public void iVerifyTagIsDeleted(String deviceTagName) throws Throwable {

        Tag foundTag = (Tag) stepData.get("tag");
        Assert.assertEquals(null, foundTag);
    }

    @When("^I search for events from device \"(.+)\" in account \"(.+)\"$")
    public void searchForEventsFromDeviceWithClientID(String clientId, String account)
            throws KapuaException {

        DeviceEventQuery tmpQuery;
        Device tmpDev;
        DeviceEventListResult tmpList;
        Account tmpAcc;

        tmpAcc = KapuaLocator.getInstance().getService(AccountService.class).findByName(account);
        Assert.assertNotNull(tmpAcc);
        Assert.assertNotNull(tmpAcc.getId());

        tmpDev = deviceRegistryService.findByClientId(tmpAcc.getId(), clientId);
        Assert.assertNotNull(tmpDev);
        Assert.assertNotNull(tmpDev.getId());

        tmpQuery = new DeviceEventQueryImpl(tmpAcc.getId());
        tmpQuery.setPredicate(AttributePredicateImpl.attributeIsEqualTo("deviceId", tmpDev.getId()));
        tmpQuery.setSortCriteria(new FieldSortCriteria("receivedOn", FieldSortCriteria.SortOrder.ASCENDING));
        tmpList = deviceEventsService.query(tmpQuery);

        Assert.assertNotNull(tmpList);
        stepData.put("DeviceEventList", tmpList);
    }

    @Then("^I find (\\d+) events?$")
    public void checkEventListLength(int cnt) {
        Assert.assertNotNull(stepData.get("DeviceEventList"));
        Assert.assertEquals(cnt, ((DeviceEventListResultImpl) stepData.get("DeviceEventList")).getSize());
    }

    @Then("^I find (\\d+) devices?$")
    public void checkDeviceListLength(int cnt) {
        Assert.assertNotNull(stepData.get("DeviceList"));
        Assert.assertEquals(cnt, ((DeviceListResultImpl) stepData.get("DeviceList")).getSize());
    }

    @Then("^I find no device$")
    public void checkNoDevicesAreFound() {

        if (stepData.get("DeviceList") == null) {
            return;
        }
        if (((DeviceListResult) stepData.get("DeviceList")).getSize() == 0) {
            return;
        }
        Assert.fail("There were unexpected device items");
    }

    @Then("^The type of the last event is \"(.+)\"$")
    public void checkLastEventType(String type) {
        DeviceEventListResult tmpList;

        Assert.assertNotNull(stepData.get("DeviceEventList"));
        Assert.assertNotEquals(0, ((DeviceEventListResultImpl) stepData.get("DeviceEventList")).getSize());
        tmpList = (DeviceEventListResultImpl) stepData.get("DeviceEventList");
        Assert.assertEquals(type.trim().toUpperCase(), tmpList.getItem(tmpList.getSize() - 1).getResource().trim().toUpperCase());
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
        return new KapuaBirthPayloadImpl(
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
                generateRandomHexString(24), // modemImei
                generateRandomHexString(15), // modemImsi
                generateRandomHexString(22)  // modemIccid
        );

    }

    private KapuaDisconnectPayload prepareDefaultDeathPayload() {
        return new KapuaDisconnectPayloadImpl(
                "1000", // uptime
                "ReliaGate 10-20" // displayName
        );
    }

    private KapuaMissingPayload prepareDefaultMissingPayload() {
        KapuaMissingPayload tmpPayload = new KapuaMissingPayloadImpl();
        return tmpPayload;
    }

    private KapuaAppsPayload prepareDefaultApplicationPayload() {
        return new KapuaAppsPayloadImpl(
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
                generateRandomHexString(24), // modemImei
                generateRandomHexString(15), // modemImsi
                generateRandomHexString(22)  // modemIccid
        );
    }

    private DeviceCreator prepareDeviceCreatorFromCucDevice(CucDevice dev) {
        Account tmpAccount = (Account) stepData.get("LastAccount");
        DeviceCreator tmpCr;
        KapuaId tmpScope;

        if (dev.scopeId != null) {
            tmpScope = dev.getScopeId();
        } else {
            Assert.assertNotNull(tmpAccount);
            Assert.assertNotNull(tmpAccount.getId());
            tmpScope = tmpAccount.getId();
        }

        Assert.assertNotNull(dev.clientId);
        Assert.assertNotEquals(0, dev.clientId.length());

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
        DeviceCreator tmpCr;

        tmpCr = deviceFactory.newCreator(scopeId, clientId);

        tmpCr.setConnectionId(generateRandomId());
        tmpCr.setDisplayName("display_name");
        tmpCr.setSerialNumber("serialNumber");
        tmpCr.setModelId("modelId");
        tmpCr.setImei(generateRandomHexString(24));
        tmpCr.setImsi(generateRandomHexString(15));
        tmpCr.setIccid(generateRandomHexString(22));
        tmpCr.setBiosVersion("biosVersion");
        tmpCr.setFirmwareVersion("firmwareVersion");
        tmpCr.setOsVersion("osVersion");
        tmpCr.setJvmVersion("jvmVersion");
        tmpCr.setOsgiFrameworkVersion("osgiFrameworkVersion");
        tmpCr.setApplicationFrameworkVersion("kapuaVersion");
        tmpCr.setApplicationIdentifiers("applicationIdentifiers");
        tmpCr.setAcceptEncoding("acceptEncoding");
        tmpCr.setCustomAttribute1("customAttribute1");
        tmpCr.setCustomAttribute2("customAttribute2");
        tmpCr.setCustomAttribute3("customAttribute3");
        tmpCr.setCustomAttribute4("customAttribute4");
        tmpCr.setCustomAttribute5("customAttribute5");
        tmpCr.setStatus(DeviceStatus.ENABLED);

        return tmpCr;
    }

    private KapuaId generateRandomId() {
        return new KapuaEid(IdGenerator.generate());
    }

    private String generateRandomHexString(int length) {

        byte[] tmpBuff = new byte[length];

        random.nextBytes(tmpBuff);
        String hexString = (new String(Hex.encode(tmpBuff))).substring(0, length -1);

        return hexString;
    }
}
