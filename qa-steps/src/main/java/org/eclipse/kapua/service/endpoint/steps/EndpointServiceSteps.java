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
package org.eclipse.kapua.service.endpoint.steps;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.apache.shiro.SecurityUtils;
import org.eclipse.kapua.KapuaException;
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
import org.eclipse.kapua.service.endpoint.EndpointInfoCreator;
import org.eclipse.kapua.service.endpoint.EndpointInfoFactory;
import org.eclipse.kapua.service.endpoint.EndpointInfoListResult;
import org.eclipse.kapua.service.endpoint.EndpointInfoQuery;
import org.eclipse.kapua.service.endpoint.EndpointInfoService;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * Implementation of Gherkin steps used in TagService.feature scenarios.
 */

@ScenarioScoped
public class EndpointServiceSteps extends BaseQATests {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointServiceSteps.class);

    private static EndpointInfoService endpointService;
    private static EndpointInfoFactory endpointFactory;
    private static AccountService accountService;

    @Inject
    public EndpointServiceSteps(StepData stepData, DBHelper dbHelper) {

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
        endpointService = locator.getService(EndpointInfoService.class);
        endpointFactory = locator.getFactory(EndpointInfoFactory.class);
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
    @Given("^The following endpoints? for account \"(.+)\"$")
    public void createEndpointsInAccount(String account, List<TestEndpoint> endpoints) throws Exception {

        KapuaId scopeId = accountService.findByName(account).getId();

        for (TestEndpoint te : endpoints) {
            EndpointInfoCreator ec = endpointFactory.newCreator(scopeId);
            ec.setSchema(te.getSchema());
            ec.setDns(te.getUrl());
            ec.setPort(te.getPort());

            try {
                primeException();
                endpointService.create(ec);
            } catch (KapuaException ex) {
                verifyException(ex);
            }
        }
    }

    @Given("^The following endpoints? for the current account$")
    public void createEndpointsInTheCurrentAccount(List<TestEndpoint> endpoints) throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (lastAcc != null) ? lastAcc.getId() : ROOT_SCOPE_ID;

        for (TestEndpoint te : endpoints) {
            EndpointInfoCreator ec = endpointFactory.newCreator(scopeId);
            ec.setSchema(te.getSchema());
            ec.setDns(te.getUrl());
            ec.setPort(te.getPort());

            try {
                primeException();
                endpointService.create(ec);
            } catch (KapuaException ex) {
                verifyException(ex);
            }
        }
    }

    @When("^I query for endpoints in account \"(.+)\"$")
    public void queryForEndpointsInAccount(String account) throws Exception {

        KapuaId scopeId = accountService.findByName(account).getId();

        EndpointInfoQuery tmpQuery = endpointFactory.newQuery(scopeId);
        try {
            primeException();
            stepData.remove("Endpoint");
            stepData.remove("EndpointList");
            EndpointInfoListResult el = endpointService.query(tmpQuery);
            stepData.put("EndpointList", el);
            if (!el.isEmpty()) {
                stepData.put("Endpoint", el.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for endpoints in the current account$")
    public void queryForEndpointsInTheCurrentAccount() throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (tmpAcc != null) ? tmpAcc.getId() : ROOT_SCOPE_ID;

        EndpointInfoQuery tmpQuery = endpointFactory.newQuery(scopeId);
        try {
            primeException();
            stepData.remove("Endpoint");
            stepData.remove("EndpointList");
            EndpointInfoListResult el = endpointService.query(tmpQuery);
            stepData.put("EndpointList", el);
            if (!el.isEmpty()) {
                stepData.put("Endpoint", el.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^There is no such endpoint$")
    public void checkThatThereIsNoEndpoint() {
        Assert.assertNull("Unexpected endpoints were found!", stepData.get("Endpoint"));
    }

    @Then("^There (?:are|is) such endpoints?$")
    public void checkThatThereIsSuchAnEndpoint() {
        Assert.assertNotNull("No endpoints were found!", stepData.get("Endpoint"));
    }

    @Then("^There (?:are|is) exactly (\\d+) endpoints?$")
    public void checkNumberOfEndpoints(int num) {

        EndpointInfoListResult el = (EndpointInfoListResult) stepData.get("EndpointList");
        Assert.assertEquals("Wrong number of endpoints!", num, el.getSize());
    }
}
