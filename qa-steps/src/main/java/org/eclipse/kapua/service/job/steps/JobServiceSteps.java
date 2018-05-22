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
package org.eclipse.kapua.service.job.steps;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
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
import org.eclipse.kapua.service.job.Job;
import org.eclipse.kapua.service.job.JobAttributes;
import org.eclipse.kapua.service.job.JobCreator;
import org.eclipse.kapua.service.job.JobFactory;
import org.eclipse.kapua.service.job.JobListResult;
import org.eclipse.kapua.service.job.JobQuery;
import org.eclipse.kapua.service.job.JobService;
import org.eclipse.kapua.service.scheduler.trigger.Trigger;
import org.eclipse.kapua.service.scheduler.trigger.TriggerAttributes;
import org.eclipse.kapua.service.scheduler.trigger.TriggerCreator;
import org.eclipse.kapua.service.scheduler.trigger.TriggerFactory;
import org.eclipse.kapua.service.scheduler.trigger.TriggerListResult;
import org.eclipse.kapua.service.scheduler.trigger.TriggerProperty;
import org.eclipse.kapua.service.scheduler.trigger.TriggerQuery;
import org.eclipse.kapua.service.scheduler.trigger.TriggerService;
import org.eclipse.kapua.service.user.steps.TestConfig;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Gherkin steps used in TagService.feature scenarios.
 */

@ScenarioScoped
public class JobServiceSteps extends BaseQATests {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceSteps.class);

    private static JobService jobService;
    private static JobFactory jobFactory;
    private static TriggerService triggerService;
    private static TriggerFactory triggerFactory;
    private static AccountService accountService;

    @Inject
    public JobServiceSteps(StepData stepData, DBHelper dbHelper) {

        this.database = dbHelper;
        this.stepData = stepData;
    }

/**
 * Setup and teardown functions
 */
    @Before
    public void beforeScenario(Scenario scenario) {

        this.database.setup();
        this.scenario = scenario;
        this.stepData.clear();

        KapuaLocator locator = KapuaLocator.getInstance();
        jobService = locator.getService(JobService.class);
        jobFactory = locator.getFactory(JobFactory.class);
        triggerService = locator.getService(TriggerService.class);
        triggerFactory = locator.getFactory(TriggerFactory.class);
        accountService = locator.getService(AccountService.class);

        XmlUtil.setContextProvider(new TestJAXBContextProvider());
    }

    @After
    public void afterScenario() {
        try {
            logger.info("Logging out in cleanup");
            SecurityUtils.getSubject().logout();
            KapuaSecurityUtils.clearSession();
        } catch (Exception e) {
            logger.error("Failed to log out in @After", e);
        }
    }

/**
 * Implementation of cucumber scenario steps
 */
    @When("^I configure the job service$")
    public void setJobServiceConfig(List<TestConfig> testConfigs)
            throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = ROOT_SCOPE_ID;
        KapuaId parentId = ROOT_SCOPE_ID;
        if (lastAcc != null) {
            scopeId = lastAcc.getId();
            parentId = lastAcc.getScopeId();
        }
        Map<String, Object> valueMap = new HashMap<>();

        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
        }
        try {
            primeException();
            jobService.setConfigValues(scopeId, parentId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^A job named \"(.+)\" in the current scope$")
    public void createANamedJob(String jobName) throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        KapuaId tmpScope = (tmpAcc != null) ? tmpAcc.getId() : ROOT_SCOPE_ID;
        JobCreator tmpCreator = prepareRegularJobCreator(tmpScope, jobName);
        Job tmpJob;

        try {
            primeException();
            stepData.remove("Job");
            tmpJob = jobService.create(tmpCreator);
            stepData.put("Job", tmpJob);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I delete the job \"(.+)\" in the current account$")
    public void deleteJobInCurrentAccount(String jobName) throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        KapuaId tmpScope = (tmpAcc != null) ? tmpAcc.getId() : ROOT_SCOPE_ID;

        JobQuery jobQuery = jobFactory.newQuery(tmpScope);
        jobQuery.setPredicate(new AttributePredicateImpl<>(JobAttributes.NAME, jobName));
        JobListResult jobList = jobService.query(jobQuery);

        try {
            primeException();
            jobService.delete(tmpScope, jobList.getFirstItem().getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the job \"(.+)\" in account \"(.+)\"$")
    public void queryForNamedJobInAccount(String jobName, String accName) throws Exception{

        Account tmpAcc = accountService.findByName(accName);
        KapuaId tmpScopeId = tmpAcc.getId();
        JobQuery tmpQuery = jobFactory.newQuery(tmpScopeId);
        tmpQuery.setPredicate(new AttributePredicateImpl<>(JobAttributes.NAME, jobName));
        JobListResult tmpJobList;

        try {
            primeException();
            stepData.remove("Job");
            stepData.remove("JobList");
            tmpJobList = jobService.query(tmpQuery);
            stepData.put("JobList", tmpJobList);
            if (!tmpJobList.isEmpty()) {
                stepData.put("Job", tmpJobList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the job \"(.+)\" in the last account$")
    public void queryForNamedJob(String jobName) throws Exception{

        Account tmpAcc = (Account) stepData.get("LastAccount");
        KapuaId tmpScopeId = (tmpAcc != null) ? tmpAcc.getId() : ROOT_SCOPE_ID;
        JobQuery tmpQuery = jobFactory.newQuery(tmpScopeId);
        tmpQuery.setPredicate(new AttributePredicateImpl<>(JobAttributes.NAME, jobName));
        JobListResult tmpJobList;

        try {
            primeException();
            stepData.remove("Job");
            stepData.remove("JobList");
            tmpJobList = jobService.query(tmpQuery);
            stepData.put("JobList", tmpJobList);
            if (!tmpJobList.isEmpty()) {
                stepData.put("Job", tmpJobList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^There is no such job$")
    public void checkThatThereIsNoJob() {

        Assert.assertNull(stepData.get("Job"));
    }

    @Then("^There is such a job$")
    public void checkThatTheJobExists() {

        Assert.assertNotNull(stepData.get("Job"));
    }

    @Given("^I configure the scheduler service$")
    public void setTriggerServiceConfig(List<TestConfig> testConfigs)
            throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (lastAcc != null) ? lastAcc.getId() : ROOT_SCOPE_ID;
        KapuaId parentId = (lastAcc != null) ? lastAcc.getScopeId() : ROOT_SCOPE_ID;
        Map<String, Object> valueMap = new HashMap<>();

        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
        }
        try {
            primeException();
            triggerService.setConfigValues(scopeId, parentId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^I select the schedule \"(.+)\" in the current account$")
    public void selectScheduleInCurrentAccount(String scheduleName) throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (lastAcc != null) ? lastAcc.getId() : ROOT_SCOPE_ID;
        TriggerQuery trigQuery = triggerFactory.newQuery(scopeId);
        trigQuery.setPredicate(new AttributePredicateImpl<>(TriggerPredicates.NAME, scheduleName));

        try {
            primeException();
            stepData.remove("CurrentSchedule");
            TriggerListResult trigLst = triggerService.query(trigQuery);
            Assert.assertNotEquals("The requested schedule was not found.", 0, trigLst.getSize());
            stepData.put("CurrentSchedule", trigLst.getFirstItem());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^I select the schedule \"(.+)\" in account \"(.+)\"$")
    public void selectScheduleInAccount(String scheduleName, String accountName) throws Exception {

        Account tgtAcc = accountService.findByName(accountName);
        Assert.assertNotNull("The requested account was not found!", tgtAcc);
        TriggerQuery trigQuery = triggerFactory.newQuery(tgtAcc.getId());
        trigQuery.setPredicate(new AttributePredicateImpl<>(TriggerPredicates.NAME, scheduleName));

        try {
            primeException();
            stepData.remove("CurrentSchedule");
            TriggerListResult trigLst = triggerService.query(trigQuery);
            Assert.assertNotEquals("The requested schedule was not found.", 0, trigLst.getSize());
            stepData.put("CurrentSchedule", trigLst.getFirstItem());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I create the schedule \"(.+)\" for the job \"(.+)\" in the current account$")
    public void createScheduleForJob(String scheduleName, String jobName)
            throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (lastAcc != null) ? lastAcc.getId() : ROOT_SCOPE_ID;

        JobQuery jobQuery = jobFactory.newQuery(scopeId);
        jobQuery.setPredicate(new AttributePredicateImpl<>(JobAttributes.NAME, jobName));
        Job targetJob = jobService.query(jobQuery).getFirstItem();

        TriggerCreator triggerCreator = prepareRegularTriggerCreator(scopeId, scheduleName, targetJob.getId());

        primeException();
        try {
            stepData.remove("LastTrigger");
            Trigger trigger = triggerService.create(triggerCreator);
            stepData.put("LastTrigger", trigger);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I search for the schedule \"(.+)\" in the current account$")
    public void queryForScheduleInCurrentAccount(String scheduleName) throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (lastAcc != null) ? lastAcc.getId() : ROOT_SCOPE_ID;
        TriggerQuery trigQuery = triggerFactory.newQuery(scopeId);
        trigQuery.setPredicate(new AttributePredicateImpl<>(TriggerAttributes.NAME, scheduleName));

        try {
            primeException();
            stepData.remove("LastTrigger");
            stepData.remove("TriggerList");
            TriggerListResult trigLst = triggerService.query(trigQuery);
            stepData.put("TriggerList", trigLst);
            if (!trigLst.isEmpty()) {
                stepData.put("LastTrigger", trigLst.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I search for the schedule \"(.+)\" in account \"(.+)\"$")
    public void queryForScheduleInAccount(String scheduleName, String accountName) throws Exception {

        Account tgtAcc = accountService.findByName(accountName);
        TriggerQuery trigQuery = triggerFactory.newQuery(tgtAcc.getId());
        trigQuery.setPredicate(new AttributePredicateImpl<>(TriggerPredicates.NAME, scheduleName));

        try {
            primeException();
            stepData.remove("LastTrigger");
            TriggerListResult trigLst = triggerService.query(trigQuery);
            stepData.put("LastTrigger", trigLst.getFirstItem());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I count the schedules in account \"(.+)\"$")
    public void countSchedulesInAccount(String account) throws Exception {

        Account tmpAcc = accountService.findByName(account);
        KapuaId scopeId = tmpAcc.getId();
        TriggerQuery trigQuery = triggerFactory.newQuery(scopeId);

        try {
            primeException();
            stepData.remove("TriggerList");
            TriggerListResult trigLst = triggerService.query(trigQuery);
            stepData.put("TriggerList", trigLst);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I count the schedules in the current account$")
    public void countSchedulesInCurrentAccount() throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (lastAcc != null) ? lastAcc.getId() : ROOT_SCOPE_ID;
        TriggerQuery trigQuery = triggerFactory.newQuery(scopeId);

        try {
            primeException();
            stepData.remove("TriggerList");
            TriggerListResult trigLst = triggerService.query(trigQuery);
            stepData.put("TriggerList", trigLst);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I delete the selected schedule$")
    public void deleteCurrentSchedule() throws Exception {

        Trigger selectedTrigger = (Trigger) stepData.get("CurrentSchedule");
        Assert.assertNotNull("No schedule currently selected.", selectedTrigger);

        try {
            primeException();
            triggerService.delete(selectedTrigger.getScopeId(), selectedTrigger.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I delete the schedule \"(.+)\" in the current account$")
    public void deleteScheduleInCurrentAccount(String scheduleName) throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (lastAcc != null) ? lastAcc.getId() : ROOT_SCOPE_ID;
        TriggerQuery trigQuery = triggerFactory.newQuery(scopeId);
        trigQuery.setPredicate(new AttributePredicateImpl<>(TriggerPredicates.NAME, scheduleName));

        try {
            primeException();
            TriggerListResult trigLst = triggerService.query(trigQuery);
            Assert.assertNotEquals("The requested schedule was not found.", 0, trigLst.getSize());
            triggerService.delete(trigLst.getFirstItem().getScopeId(), trigLst.getFirstItem().getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I delete the schedule \"(.+)\" in account \"(.+)\"$")
    public void deleteScheduleInAccount(String scheduleName, String accountName) throws Exception {

        Account tmpAcc = accountService.findByName(accountName);
        Assert.assertNotNull("The requested account was not found.", tmpAcc);
        TriggerQuery trigQuery = triggerFactory.newQuery(tmpAcc.getId());
        trigQuery.setPredicate(new AttributePredicateImpl<>(TriggerPredicates.NAME, scheduleName));

        try {
            primeException();
            TriggerListResult trigLst = triggerService.query(trigQuery);
            Assert.assertNotEquals("The requested schedule was not found.", 0, trigLst.getSize());
            triggerService.delete(trigLst.getFirstItem().getScopeId(), trigLst.getFirstItem().getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I rename the selected schedule to \"(.+)\"$")
    public void renameSelectedSchedule(String newName) throws Exception {

        Trigger selectedTrigger = (Trigger) stepData.get("CurrentSchedule");

        try {
            primeException();
            selectedTrigger.setName(newName);
            triggerService.update(selectedTrigger);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^There is no such schedule")
    public void checkThatThereIsNoTrigger() {

        Assert.assertNull(stepData.get("LastTrigger"));
    }

    @Then("^There (?:are|is) exactly (\\d+) schedules?$")
    public void checkNumberOfSchedules(int num) {

        TriggerListResult trigLst = (TriggerListResult) stepData.get("TriggerList");
        Assert.assertEquals(num, trigLst.getSize());
    }

    @Then("^There is such a schedule")
    public void checkThatTheTriggerExists() {

        Assert.assertNotNull(stepData.get("LastTrigger"));
    }

/**
 * Private helper functions
 */
    JobCreator prepareRegularJobCreator(KapuaId scopeId, String jobName) {
        JobCreator tmpCreator = jobFactory.newCreator(scopeId);

        tmpCreator.setName(jobName);
        tmpCreator.setDescription("TestJobDescription");

        return tmpCreator;
    }

    TriggerCreator prepareRegularTriggerCreator(KapuaId scopeId, String triggerName, KapuaId jobId) {

        TriggerCreator tmpCreator = triggerFactory.newCreator(scopeId);
        tmpCreator.setName(triggerName);

        Calendar tmpCalStart = Calendar.getInstance();
        tmpCalStart.add(Calendar.HOUR, 5);
        Calendar tmpCalEnd = tmpCalStart;
        tmpCalEnd.add(Calendar.HOUR, 5);

        tmpCreator.setStartsOn(tmpCalStart.getTime());
        tmpCreator.setEndsOn(tmpCalEnd.getTime());
        tmpCreator.setRetryInterval(Long.valueOf(5));

        List<TriggerProperty> propList = new ArrayList<>();
        propList.add(triggerFactory.newTriggerProperty("jobId", KapuaId.class.getName(), jobId.toCompactId()));
        tmpCreator.setTriggerProperties(propList);

        return tmpCreator;
    }

}
