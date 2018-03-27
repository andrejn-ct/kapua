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
package org.eclipse.kapua.service.authorization.steps;


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
import org.eclipse.kapua.model.domain.Actions;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.qa.steps.BaseQATests;
import org.eclipse.kapua.qa.steps.DBHelper;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.TestJAXBContextProvider;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.authentication.AuthenticationService;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authorization.access.AccessInfo;
import org.eclipse.kapua.service.authorization.access.AccessInfoCreator;
import org.eclipse.kapua.service.authorization.access.AccessInfoFactory;
import org.eclipse.kapua.service.authorization.access.AccessInfoListResult;
import org.eclipse.kapua.service.authorization.access.AccessInfoPredicates;
import org.eclipse.kapua.service.authorization.access.AccessInfoQuery;
import org.eclipse.kapua.service.authorization.access.AccessInfoService;
import org.eclipse.kapua.service.authorization.group.Group;
import org.eclipse.kapua.service.authorization.group.GroupCreator;
import org.eclipse.kapua.service.authorization.group.GroupFactory;
import org.eclipse.kapua.service.authorization.group.GroupListResult;
import org.eclipse.kapua.service.authorization.group.GroupPredicates;
import org.eclipse.kapua.service.authorization.group.GroupQuery;
import org.eclipse.kapua.service.authorization.group.GroupService;
import org.eclipse.kapua.service.authorization.permission.Permission;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.authorization.role.Role;
import org.eclipse.kapua.service.authorization.role.RoleCreator;
import org.eclipse.kapua.service.authorization.role.RoleDomain;
import org.eclipse.kapua.service.authorization.role.RoleFactory;
import org.eclipse.kapua.service.authorization.role.RoleListResult;
import org.eclipse.kapua.service.authorization.role.RolePredicates;
import org.eclipse.kapua.service.authorization.role.RoleQuery;
import org.eclipse.kapua.service.authorization.role.RoleService;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserService;
import org.eclipse.kapua.service.user.steps.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

/**
 * Implementation of Gherkin steps used in UserServiceI9n.feature scenarios.
 */
@ScenarioScoped
public class AuthorizationServiceSteps extends BaseQATests {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServiceSteps.class);

    /**
     * Security service objects
     */
    private AuthenticationService authenticationService;
    private CredentialService credentialService;
    private RoleService roleService;
    private RoleFactory roleFactory;
    private GroupService groupService;
    private GroupFactory groupFactory;
    private AccessInfoService accessInfoService;
    private AccessInfoFactory accessinfoFactory;
    private PermissionFactory permissionFactory;

    /**
     * Supporting service references. Obtained by locator.
     */
    private AccountService accountService;
    private UserService userService;

    @Inject
    public AuthorizationServiceSteps(StepData stepData, DBHelper dbHelper) {

        this.stepData = stepData;
        this.database = dbHelper;
    }

    @Before
    public void beforeScenario(Scenario scenario) {

        this.database.setup();
        this.scenario = scenario;
        this.stepData.clear();

        // Services by default Locator
        KapuaLocator locator = KapuaLocator.getInstance();
        authenticationService = locator.getService(AuthenticationService.class);
        credentialService = locator.getService(CredentialService.class);
        accessInfoService = locator.getService(AccessInfoService.class);
        accessinfoFactory = locator.getFactory(AccessInfoFactory.class);
        roleService = locator.getService(RoleService.class);
        roleFactory = locator.getFactory(RoleFactory.class);
        groupService = locator.getService(GroupService.class);
        groupFactory = locator.getFactory(GroupFactory.class);
        permissionFactory = locator.getFactory(PermissionFactory.class);

        accountService = locator.getService(AccountService.class);
        userService = locator.getService(UserService.class);

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

    @When("^I configure the role service$")
    public void setRoleServiceConfig(List<TestConfig> testConfigs)
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
            roleService.setConfigValues(scopeId, parentId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I configure the group service$")
    public void setGroupServiceConfig(List<TestConfig> testConfigs)
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
            groupService.setConfigValues(scopeId, parentId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^The following roles? in the current scope$")
    public void createRolesInTheCurrentScope(List<TestRole> roleParams) throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        KapuaId tmpScope = tmpAcc.getId();
        RoleCreator tmpCreator;
        Role tmpRole;

        for (TestRole tmpRoleParams : roleParams) {
            tmpRoleParams.doParse();
            Set<Permission> permissions = new HashSet<>();
            if ((tmpRoleParams.getActions() != null) && (tmpRoleParams.getActions().size() > 0)) {
                for (Actions tmpAct : tmpRoleParams.getActions()) {
                    permissions.add(permissionFactory.newPermission(new RoleDomain(), tmpAct, tmpScope));
                }
            }
            tmpCreator = roleFactory.newCreator(tmpScope);
            tmpCreator.setName(tmpRoleParams.getName());
            tmpCreator.setPermissions(permissions);
            try {
                primeException();
                tmpRole = roleService.create(tmpCreator);
                stepData.put("Role", tmpRole);
            } catch (KapuaException ex) {
                verifyException(ex);
            }
        }
    }

    @Given("^The following roles? in scope \"(.+)\"$")
    public void createANamedRoleInTheNamedScope(String scope, List<TestRole> roleParams) throws Exception {

        Account tmpAcc = accountService.findByName(scope);
        KapuaId tmpScope = tmpAcc.getId();
        RoleCreator tmpCreator;
        Role tmpRole;

        for (TestRole tmpRoleParams : roleParams) {
            tmpRoleParams.doParse();
            Set<Permission> permissions = new HashSet<>();
            if ((tmpRoleParams.getActions() != null) && (tmpRoleParams.getActions().size() > 0)) {
                for (Actions tmpAct : tmpRoleParams.getActions()) {
                    permissions.add(permissionFactory.newPermission(new RoleDomain(), tmpAct, tmpScope));
                }
            }
            tmpCreator = roleFactory.newCreator(tmpScope);
            tmpCreator.setName(tmpRoleParams.getName());
            tmpCreator.setPermissions(permissions);
            try {
                primeException();
                tmpRole = roleService.create(tmpCreator);
                stepData.put("Role", tmpRole);
            } catch (KapuaException ex) {
                verifyException(ex);
            }
        }
    }

    @When("^I query for the role \"(.+)\" in the last account$")
    public void queryForRoleInCurrentScope(String roleName) throws Exception {

        RoleListResult tmpList;
        Account currAcc = (Account) stepData.get("LastAccount");
        RoleQuery tmpQuery = roleFactory.newQuery(currAcc.getId());
        tmpQuery.setPredicate(new AttributePredicateImpl<>(RolePredicates.NAME, roleName));

        try {
            stepData.remove("RoleList");
            stepData.remove("Role");
            primeException();
            tmpList = roleService.query(tmpQuery);
            stepData.put("RoleList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("Role", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the role \"(.+)\" in account \"(.+)\"$")
    public void queryForRoleInScope(String roleName, String scopeName) throws Exception {

        RoleListResult tmpList;
        Account tmpAcc = accountService.findByName(scopeName);
        RoleQuery tmpQuery = roleFactory.newQuery(tmpAcc.getId());
        tmpQuery.setPredicate(new AttributePredicateImpl<>(RolePredicates.NAME, roleName));

        try {
            stepData.remove("RoleList");
            stepData.remove("Role");
            primeException();
            tmpList = roleService.query(tmpQuery);
            stepData.put("RoleList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("Role", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^I find such a role$")
    public void verifyThatARoleWasFound() {
        Assert.assertNotNull("No such role!", stepData.get("Role"));
    }

    @Then("^I find no such role$")
    public void verifyThatNoRoleWasFound() {
        Assert.assertNull("Am unexpected role was found!", stepData.get("Role"));
    }

    @Given("^The following groups? in the current scope$")
    public void createGroupsInTheCurrentScope(List<TestGroup> groupParams) throws Exception {

        Account currAccount = (Account) stepData.get("LastAccount");
        KapuaId tmpScope;
        GroupCreator tmpCreator;
        Group tmpGroup;

        for (TestGroup tmpGroupParams : groupParams) {
            tmpGroupParams.doParse();
            tmpScope = (tmpGroupParams.getScopeId() != null) ? tmpGroupParams.getScopeId() : currAccount.getId();
            tmpCreator = groupFactory.newCreator(tmpScope);
            tmpCreator.setName(tmpGroupParams.getName());
            try {
                primeException();
                tmpGroup = groupService.create(tmpCreator);
                stepData.put("Group", tmpGroup);
            } catch (KapuaException ex) {
                verifyException(ex);
            }
        }
    }

    @Given("^The following groups? in scope \"(.+)\"$")
    public void createNamedGroupsInTheNamedScope(String scope, List<TestGroup> groupParams) throws Exception {

        Account namedAccount = accountService.findByName(scope);
        KapuaId tmpScope;
        GroupCreator tmpCreator;
        Group tmpGroup;

        for (TestGroup tmpGroupParams : groupParams) {
            tmpGroupParams.doParse();
            tmpScope = (tmpGroupParams.getScopeId() != null) ? tmpGroupParams.getScopeId() : namedAccount.getId();
            tmpCreator = groupFactory.newCreator(tmpScope);
            tmpCreator.setName(tmpGroupParams.getName());
            try {
                primeException();
                tmpGroup = groupService.create(tmpCreator);
                stepData.put("Group", tmpGroup);
            } catch (KapuaException ex) {
                verifyException(ex);
            }
        }
    }

    @When("^I query for the group \"(.+)\" in the last account$")
    public void queryForGroupInCurrentScope(String groupName) throws Exception {

        GroupListResult tmpList;
        Account currAcc = (Account) stepData.get("LastAccount");
        GroupQuery tmpQuery = groupFactory.newQuery(currAcc.getId());
        tmpQuery.setPredicate(new AttributePredicateImpl<>(GroupPredicates.NAME, groupName));

        try {
            stepData.remove("GroupList");
            stepData.remove("Group");
            primeException();
            tmpList = groupService.query(tmpQuery);
            stepData.put("GroupList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("Group", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the group \"(.+)\" in account \"(.+)\"$")
    public void queryForGroupInScope(String roleName, String scopeName) throws Exception {

        GroupListResult tmpList;
        Account tmpAcc = accountService.findByName(scopeName);
        GroupQuery tmpQuery = groupFactory.newQuery(tmpAcc.getId());
        tmpQuery.setPredicate(new AttributePredicateImpl<>(RolePredicates.NAME, roleName));

        try {
            stepData.remove("GroupList");
            stepData.remove("Group");
            primeException();
            tmpList = groupService.query(tmpQuery);
            stepData.put("GroupList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("Group", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^I find such a group$")
    public void verifyThatAGroupWasFound() {
        Assert.assertNotNull("No such group!", stepData.get("Group"));
    }

    @Then("^I find no such group$")
    public void verifyThatNoGroupWasFound() {
        Assert.assertNull("Am unexpected group was found!", stepData.get("Group"));
    }

    @When("^I create an empty access info entity for user \"(.+)\" in the current scope$")
    public void createEmptyAccessInfoForUser(String userName) throws Exception {

        Account currAcc = (Account) stepData.get("LastAccount");
        AccessInfo accessInfo;

        try {
            primeException();
            stepData.remove("AccessInfo");
            User tmpUser = userService.findByName(userName);
            AccessInfoCreator tmpCreator = accessinfoFactory.newCreator(currAcc.getId());
            tmpCreator.setUserId(tmpUser.getId());
            accessInfo = accessInfoService.create(tmpCreator);
            stepData.put("AccessInfo", accessInfo);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I create an empty access info entity for user \"(.+)\" in scope \"(.+)\"$")
    public void createEmptyAccessInfoForUser(String userName, String accountName) throws Exception {

        Account targetAcc;
        AccessInfo accessInfo;

        try {
            primeException();
            stepData.remove("AccessInfo");
            targetAcc = accountService.findByName(accountName);
            User tmpUser = userService.findByName(userName);
            AccessInfoCreator tmpCreator = accessinfoFactory.newCreator(targetAcc.getId());
            tmpCreator.setUserId(tmpUser.getId());
            accessInfo = accessInfoService.create(tmpCreator);
            stepData.put("AccessInfo", accessInfo);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the access info entities in scope \"(.+)\"$")
    public void queryForAccessInfoEntitiesInScope(String accountName) throws Exception {

        Account targetAcc;
        AccessInfoQuery tmpQuery;
        AccessInfoListResult accessInfoList;

        try {
            primeException();
            stepData.remove("AccessInfoList");
            stepData.remove("AccessInfo");
            targetAcc = accountService.findByName(accountName);
            tmpQuery = accessinfoFactory.newQuery(targetAcc.getId());
            accessInfoList = accessInfoService.query(tmpQuery);
            stepData.put("AccessInfoList", accessInfoList);
            if (accessInfoList != null) {
                stepData.put("AccessInfo", accessInfoList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the access info entities in the last scope$")
    public void queryForAccessInfoEntitiesInLastScope() throws Exception {

        Account targetAcc = (Account) stepData.get("LastAccount");
        AccessInfoQuery tmpQuery;
        AccessInfoListResult accessInfoList;

        try {
            primeException();
            stepData.remove("AccessInfoList");
            stepData.remove("AccessInfo");
            tmpQuery = accessinfoFactory.newQuery(targetAcc.getId());
            accessInfoList = accessInfoService.query(tmpQuery);
            stepData.put("AccessInfoList", accessInfoList);
            if (accessInfoList != null) {
                stepData.put("AccessInfo", accessInfoList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the access info entities for the user \"(.+)\"$")
    public void countAccessInfoEntitiesForUser(String userName) throws Exception {

        User targetUser;
        AccessInfoQuery tmpQuery;
        AccessInfoListResult accessInfoList;

        try {
            primeException();
            stepData.remove("AccessInfoList");
            stepData.remove("AccessInfo");
            targetUser = userService.findByName(userName);
            tmpQuery = accessinfoFactory.newQuery(targetUser.getScopeId());
            tmpQuery.setPredicate(new AttributePredicateImpl<>(AccessInfoPredicates.USER_ID, targetUser.getId()));
            accessInfoList = accessInfoService.query(tmpQuery);
            stepData.put("AccessInfoList", accessInfoList);
            if (accessInfoList != null) {
                stepData.put("AccessInfo", accessInfoList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the access info entities for the last user$")
    public void countAccessInfoEntitiesForLastUser() throws Exception {

        User targetUser = (User) stepData.get("LastUser");
        AccessInfoQuery tmpQuery;
        AccessInfoListResult accessInfoList;

        try {
            primeException();
            stepData.remove("AccessInfoList");
            stepData.remove("AccessInfo");
            tmpQuery = accessinfoFactory.newQuery(targetUser.getScopeId());
            tmpQuery.setPredicate(new AttributePredicateImpl<>(AccessInfoPredicates.USER_ID, targetUser.getId()));
            accessInfoList = accessInfoService.query(tmpQuery);
            stepData.put("AccessInfoList", accessInfoList);
            if (accessInfoList != null) {
                stepData.put("AccessInfo", accessInfoList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^I find such an access info item$")
    public void verifyThatAnAccessInfoWasFound() {
        Assert.assertNotNull("No such access info!", stepData.get("AccessInfo"));
    }

    @Then("^I find no such access info item$")
    public void verifyThatNoAccessInfoWasFound() {
        Assert.assertNull("An unexpected access info item was found!", stepData.get("AccessInfo"));
    }

    @Then("^There (?:are|is) exactly (\\d+) access info items?$")
    public void checkNumberOfAccessinfoItems(int count) {

        AccessInfoListResult tmpList = (AccessInfoListResult) stepData.get("AccessInfoList");
        Assert.assertEquals("Wrong number of access info items!", count, tmpList.getSize());
    }
}
