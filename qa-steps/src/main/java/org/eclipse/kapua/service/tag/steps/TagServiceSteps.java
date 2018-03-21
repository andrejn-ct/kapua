/*******************************************************************************
 * Copyright (c) 2017, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.service.tag.steps;

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
import org.eclipse.kapua.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.qa.steps.BaseQATests;
import org.eclipse.kapua.qa.steps.DBHelper;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.TestJAXBContextProvider;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.tag.Tag;
import org.eclipse.kapua.service.tag.TagAttributes;
import org.eclipse.kapua.service.tag.TagCreator;
import org.eclipse.kapua.service.tag.TagFactory;
import org.eclipse.kapua.service.tag.TagListResult;
import org.eclipse.kapua.service.tag.TagQuery;
import org.eclipse.kapua.service.tag.TagService;
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
public class TagServiceSteps extends BaseQATests {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagServiceSteps.class);

    /**
     * Tag service.
     */
    private static TagService tagService;
    private static TagFactory tagFactory;
    private static AccountService accountService;

    private DBHelper database;

    @Inject
    public TagServiceSteps(StepData stepData, DBHelper dbHelper) {

        this.database = dbHelper;
        this.stepData = stepData;
        this.database = dbHelper;
    }

    @Before
    public void tagStepsBefore(Scenario scenario) {

        this.database.setup();
        this.scenario = scenario;
        this.stepData.clear();

        KapuaLocator locator = KapuaLocator.getInstance();
        tagService = locator.getService(TagService.class);
        tagFactory = locator.getFactory(TagFactory.class);
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

    @When("^I configure the tag service$")
    public void setTagServiceConfig(List<TestConfig> testConfigs) throws Exception {

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
            tagService.setConfigValues(scopeId, parentId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^Tag with name \"([^\"]*)\"$")
    public void tagWithName(String tagName) throws Exception {

        TagCreator tagCreator = tagCreatorCreator(tagName);
        try {
            primeException();
            Tag tag = tagService.create(tagCreator);
            stepData.put("Tag", tag);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^A new tag with name \"([^\"]*)\" in the last account$")
    public void tagWithNameInCurrentScope(String tagName) throws Exception {

        TagCreator tagCreator = tagCreatorCreator(tagName, getLastAccountId());
        try {
            primeException();
            Tag tag = tagService.create(tagCreator);
            stepData.put("Tag", tag);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^Tag with name \"([^\"]*)\" is searched$")
    public void tagWithNameIfSearched(String tagName) throws Exception {

        TagQuery query = tagFactory.newQuery(ROOT_SCOPE_ID);
        query.setPredicate(query.attributePredicate(TagAttributes.NAME, tagName, AttributePredicate.Operator.EQUAL));

        try {
            primeException();
            TagListResult queryResult = tagService.query(query);
            Tag foundTag = queryResult.getFirstItem();
            stepData.put("Tag", foundTag);
            stepData.put("TagList", queryResult);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I search for the tag \"(.*)\" in account \"(.+)\"$")
    public void searchForNamedTagInAccount(String tagName, String accountName) throws Exception {

        Tag tmpTag;
        Account tmpAcc;
        try {
            primeException();
            stepData.remove("Tag");
            tmpAcc = accountService.findByName(accountName);
            Assert.assertNotNull("The requested account does not exist!", tmpAcc);
            tmpTag = findNamedTagInScope(tagName, tmpAcc.getId());
            stepData.put("Tag", tmpTag);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I search for the tag \"(.*)\" in the last account$")
    public void searchForNamedTag(String tagName) throws Exception {

        Tag tmpTag;
        try {
            primeException();
            stepData.remove("Tag");
            tmpTag = findNamedTagInScope(tagName, getLastAccountId());
            stepData.put("Tag", tmpTag);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^I find such a tag$")
    public void checkThatATagWasFound() {

        Assert.assertNotNull(stepData.get("Tag"));
    }

    @Then("^I find no such tag$")
    public void checkThatNoTagWasFound() {

        Assert.assertNull(stepData.get("Tag"));
    }

    @Then("^Tag with name \"(.+)\" is found$")
    public void tagWithNameIsFound(String tagName) {

        Tag foundTag = (Tag) stepData.get("Tag");
        Assert.assertEquals(tagName, foundTag.getName());
    }

    @When("^I delete the tag \"(.+)\"$")
    public void deleteTagWithName(String tagName) throws Exception {

        Tag tmpTag = findNamedTagInScope(tagName, getLastAccountId());
        try {
            primeException();
            tagService.delete(tmpTag.getScopeId(), tmpTag.getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^Tag with name \"([^\"]*)\" is found and deleted$")
    public void tagWithNameIsDeleted(String tagName) throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId accId = (lastAcc != null) ? lastAcc.getId() : ROOT_SCOPE_ID;

        TagQuery tagQuery = tagFactory.newQuery(accId);
        tagQuery.setPredicate(tagQuery.attributePredicate(TagAttributes.NAME, tagName));

        try {
            primeException();
            TagListResult tagList = tagService.query(tagQuery);
            Assert.assertNotNull(String.format("Failed to find a tag with the name %s", tagName), tagList.getFirstItem());
            tagService.delete(tagList.getFirstItem().getScopeId(), tagList.getFirstItem().getId());
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    /**
     * Create TagCreator for creating tag with specified name.
     *
     * @param tagName name of tag
     * @return tag creator for tag with specified name
     */
    private TagCreator tagCreatorCreator(String tagName, KapuaId scopeId) {

        TagCreator tagCreator = tagFactory.newCreator(scopeId);
        tagCreator.setName(tagName);

        return tagCreator;
    }

    private TagCreator tagCreatorCreator(String tagName) {
        return tagCreatorCreator(tagName, ROOT_SCOPE_ID);
    }

    private Tag findNamedTagInScope(String tagName, KapuaId scope)
            throws Exception {

        TagQuery query = tagFactory.newQuery(scope);
        query.setPredicate(query.attributePredicate(TagAttributes.NAME, tagName));
        TagListResult tmpLst = tagService.query(query);

        if (tmpLst == null) {
            return null;
        }

        if (tmpLst.isEmpty()) {
            return null;
        }

        return tmpLst.getFirstItem();
    }
}
