/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.service.eventlog.steps;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.apache.shiro.SecurityUtils;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicateImpl;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.qa.steps.BaseQATests;
import org.eclipse.kapua.qa.steps.DBHelper;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.TestJAXBContextProvider;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.eventlog.EventLogFactory;
import org.eclipse.kapua.service.eventlog.EventLogListResult;
import org.eclipse.kapua.service.eventlog.EventLogAttributes;
import org.eclipse.kapua.service.eventlog.EventLogQuery;
import org.eclipse.kapua.service.eventlog.EventLogService;
import org.eclipse.kapua.service.user.steps.TestConfig;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Gherkin steps used in TagService.feature scenarios.
 */

@ScenarioScoped
public class EventLogServiceSteps extends BaseQATests {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventLogServiceSteps.class);

    private static EventLogService eventLogService;
    private static EventLogFactory eventLogFactory;
    private static AccountService accountService;

    @Inject
    public EventLogServiceSteps(StepData stepData, DBHelper dbHelper) {

        this.database = dbHelper;
        this.stepData = stepData;
    }

/**
 * Setup and teardown methods
 */
    @Before
    public void beforeScenario(Scenario scenario) {

        this.database.setup();
        this.scenario = scenario;
        this.stepData.clear();

        KapuaLocator locator = KapuaLocator.getInstance();
        eventLogService = locator.getService(EventLogService.class);
        eventLogFactory = locator.getFactory(EventLogFactory.class);
        accountService = locator.getService(AccountService.class);

        XmlUtil.setContextProvider(new TestJAXBContextProvider());
    }

    @After
    public void afterScenario() {
        try {
            LOGGER.info("Logging out in cleanup");
            SecurityUtils.getSubject().logout();
            KapuaSecurityUtils.clearSession();
        } catch (Exception e) {
            LOGGER.error("Failed to log out in @After", e);
        }
    }

/**
 * Implementation of cucumber scenario steps
 */

    @When("^I query for all event logs$")
    public void queryForAllEventLogs() throws Exception {

        EventLogQuery tmpQuery = eventLogFactory.newQuery(ROOT_SCOPE_ID);

        try {
            primeException();
            stepData.remove("EventLog");
            stepData.remove("EventLogList");
            EventLogListResult el = eventLogService.query(tmpQuery);
            stepData.put("EventLogList", el);
            if (!el.isEmpty()) {
                stepData.put("EventLog", el.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for events for account \"(.+)\"$")
    public void queryForEventsInAccount(String account) throws Exception {

        KapuaId scopeId = accountService.findByName(account).getId();
        EventLogQuery tmpQuery = eventLogFactory.newQuery(scopeId);

        try {
            primeException();
            stepData.remove("EventLog");
            stepData.remove("EventLogList");
            EventLogListResult el = eventLogService.query(tmpQuery);
            stepData.put("EventLogList", el);
            if (!el.isEmpty()) {
                stepData.put("EventLog", el.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for events for the current account$")
    public void queryForEventsInCurrentAccount() throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (tmpAcc != null) ? tmpAcc.getId() : ROOT_SCOPE_ID;
        EventLogQuery tmpQuery = eventLogFactory.newQuery(ROOT_SCOPE_ID);
        tmpQuery.setPredicate(new AttributePredicateImpl<>(EventLogAttributes.ENTITY_SCOPE_ID, scopeId));

        try {
            primeException();
            stepData.remove("EventLog");
            stepData.remove("EventLogList");
            EventLogListResult el = eventLogService.query(tmpQuery);
            stepData.put("EventLogList", el);
            if (!el.isEmpty()) {
                stepData.put("EventLog", el.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^There is no such event log")
    public void checkThatThereIsNoEventLog() {
        Assert.assertNull("Unexpected event logs were found!", stepData.get("EventLog"));
    }

    @Then("^There (?:are|is) such event logs?$")
    public void checkThatThereIsSuchAnEventLog() {
        Assert.assertNotNull("No event logs were found!", stepData.get("EventLog"));
    }

    @Then("^There (?:are|is) exactly (\\d+) event log entr(?:y|ies)$")
    public void checkNumberOfEventLog(int num) {

        EventLogListResult el = (EventLogListResult) stepData.get("EventLogList");
        Assert.assertEquals("Wrong number of event logs!", num, el.getSize());
    }

    @When("^I configure the event logging service$")
    public void setEventLoggerServiceConfig(List<TestConfig> testConfigs)
            throws Exception {

        Map<String, Object> valueMap = new HashMap<>();
        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
        }

        primeException();
        try {
            eventLogService.setConfigValues(ROOT_SCOPE_ID, ROOT_SCOPE_ID, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }
}
