/*******************************************************************************
 * Copyright (c) 2017, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.service.user.steps;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.apache.shiro.SecurityUtils;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.domain.Actions;
import org.eclipse.kapua.model.domain.Domain;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.qa.steps.BaseQATests;
import org.eclipse.kapua.qa.steps.DBHelper;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.TestJAXBContextProvider;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountCreator;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.account.internal.AccountFactoryImpl;
import org.eclipse.kapua.service.authentication.AuthenticationService;
import org.eclipse.kapua.service.authentication.credential.CredentialListResult;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authorization.access.AccessInfoCreator;
import org.eclipse.kapua.service.authorization.access.AccessInfoService;
import org.eclipse.kapua.service.authorization.access.shiro.AccessInfoFactoryImpl;
import org.eclipse.kapua.service.authorization.permission.Permission;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.authorization.permission.shiro.PermissionFactoryImpl;
import org.eclipse.kapua.service.authorization.steps.TestPermission;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserCreator;
import org.eclipse.kapua.service.user.UserDomain;
import org.eclipse.kapua.service.user.UserQuery;
import org.eclipse.kapua.service.user.UserService;
import org.eclipse.kapua.service.user.internal.UserFactoryImpl;
import org.eclipse.kapua.service.user.internal.UserQueryImpl;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of Gherkin steps used in UserServiceI9n.feature scenarios.
 */
@ScenarioScoped
public class UserServiceSteps extends BaseQATests {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceSteps.class);

    /**
     * User service by locator.
     */
    private UserService userService;

    /**
     * Account service by locator.
     */
    private AccountService accountService;

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

    @Inject
    public UserServiceSteps(StepData stepData, DBHelper dbHelper) {

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
        userService = locator.getService(UserService.class);
        accountService = locator.getService(AccountService.class);
        authenticationService = locator.getService(AuthenticationService.class);
        credentialService = locator.getService(CredentialService.class);
        accessInfoService = locator.getService(AccessInfoService.class);

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

    @Given("^Accounts?$")
    public void givenAccount(List<TestAccount> accountList) throws Exception {

        Account newAccount = null;
        Account lastAccount = (Account) stepData.get("LastAccount");

        for (TestAccount tmpTestAccount : accountList) {
            // If accountId is not set in account list, use last created Account for scope id
            if (tmpTestAccount.getScopeId() == null) {
                tmpTestAccount.setScopeId(lastAccount.getId().getId());
            }
            newAccount = createAccount(tmpTestAccount);
            stepData.put("LastAccount", newAccount);
        }

    }

    @Given("^Permissions$")
    public void givenPermissions(List<TestPermission> permissionList) throws Exception {
        createPermissions(permissionList, new ComparableUser((User) stepData.get("LastUser")), (Account) stepData.get("LastAccount"));
    }

    @Given("^Full permissions$")
    public void givenFullPermissions() throws Exception {
        createPermissions(null, new ComparableUser((User) stepData.get("LastUser")), (Account) stepData.get("LastAccount"));
    }

    @Given("^User A$")
    public void givenUserA(List<TestUser> userList) throws Exception {
        // User is created within account that was last created in steps
        ComparableUser tmpUser = null;
        HashSet<ComparableUser> createdList = createUsersInList(userList, (Account) stepData.get("LastAccount"));
        Iterator<ComparableUser> userIterator = createdList.iterator();
        while (userIterator.hasNext()) {
            tmpUser = userIterator.next();
        }

        stepData.put("UserA", tmpUser);
        stepData.put("LastUser", tmpUser.getUser());
    }

    @Given("^User B$")
    public void givenUserB(List<TestUser> userList) throws Exception {
        // User is created within account that was last created in steps
        ComparableUser tmpUser = null;
        HashSet<ComparableUser> createdList = createUsersInList(userList, (Account) stepData.get("LastAccount"));
        Iterator<ComparableUser> userIterator = createdList.iterator();
        while (userIterator.hasNext()) {
            tmpUser = userIterator.next();
        }

        stepData.put("UserB", tmpUser);
        stepData.put("LastUser", tmpUser.getUser());
    }

    @Given("^(?:A|The) generic users?$")
    public void givenGenericUser(List<TestUser> userList) throws Exception {
        // User is created within account that was last created in steps
        ComparableUser tmpUser = null;
        HashSet<ComparableUser> createdList = createUsersInList(userList, (Account) stepData.get("LastAccount"));
        Iterator<ComparableUser> userIterator = createdList.iterator();
        while (userIterator.hasNext()) {
            tmpUser = userIterator.next();
        }
        stepData.put("LastUser", tmpUser.getUser());
    }

    @When("^I select user \"(.*)\"$")
    public void selectUser(String name) throws KapuaException{

        stepData.remove("LastUser");
        User tmpUsr = userService.findByName(name);
        stepData.put("LastUser", tmpUsr);
    }

    @When ("^I search for account \"(.+)\"$")
    public void findAccountByName(String accName) throws Exception {

        primeException();
        try {
            stepData.remove("Account");
            Account tmpAcc = accountService.findByName(accName);
            stepData.put("Account", tmpAcc);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I select account \"(.*)\"$")
    public void selectAccount(String accountName) throws KapuaException {
        Account tmpAccount;
        tmpAccount = accountService.findByName(accountName);
        if (tmpAccount != null) {
            stepData.put("LastAccount", tmpAccount);
        } else {
            stepData.remove("LastAccount");
        }
    }

    @When("I change the current account expiration date to \"(.+)\"")
    public void changeCurrentAccountExpirationDate(String newExpiration) throws Exception {

        Account currAcc = (Account) stepData.get("LastAccount");
        Date newDate = parseDateString(newExpiration);

        try {
            primeException();
            currAcc.setExpirationDate(newDate);
            Account tmpAcc = accountService.update(currAcc);
            stepData.put("LastAccount", tmpAcc);
        } catch (KapuaException e) {
            verifyException(e);
        }
    }

    @Then("^I find the account$")
    public void checkThatAnAccountWasFound() {
        Assert.assertNotNull("No such account was found", stepData.get("Account"));
    }

    @Then("^I don't find the account$")
    public void checkThatTheAccountWasNotFound() {
        Assert.assertNull("The account still exists", stepData.get("Account"));
    }

    @When("^I delete account \"(.*)\"$")
    public void deleteAccount(String accountName) throws Exception {
        Account accountToDelete;
        primeException();
        try {
            accountToDelete = accountService.findByName(accountName);
            if (accountToDelete != null) {
                accountService.delete(accountToDelete.getScopeId(), accountToDelete.getId());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I try to delete account \"(.*)\"$")
    public void tryToDeleteAccount(String accountName) throws KapuaException {
        Account accountToDelete;
        accountToDelete = accountService.findByName(accountName);
        if (accountToDelete != null) {
            accountService.delete(accountToDelete.getScopeId(), accountToDelete.getId());
        }
        accountToDelete = accountService.findByName(accountName);
        Assert.assertNull("Account not deleted.", accountToDelete);
    }

    @Then("^I try to delete user \"(.*)\"$")
    public void thenDeleteUser(String userName) throws Exception {

        primeException();
        try {
            User userToDelete = userService.findByName(userName);
            if (userToDelete != null) {
                userService.delete(userToDelete);
            }
        } catch (KapuaException e) {
            verifyException(e);
        }
    }

    @When("I count the users in account \"(.+)\"")
    public void countUsersInAccount(String accountName) throws Exception {

        primeException();
        try {
            stepData.remove("Count");
            Account targetAcc = accountService.findByName(accountName);
            Assert.assertNotNull("The requested account doesn't exist.", targetAcc);
            UserQuery usrQuery = new UserQueryImpl(targetAcc.getId());
            long usrCount = userService.count(usrQuery);
            stepData.put("Count", usrCount);
        } catch (KapuaException e) {
            verifyException(e);
        }
    }

    @When("I count the users in the current account")
    public void countUsersInTheCurrentAccount() throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = (tmpAcc != null) ? tmpAcc.getId() : ROOT_SCOPE_ID;

        primeException();
        try {
            stepData.remove("Count");
            UserQuery usrQuery = new UserQueryImpl(scopeId);
            long usrCount = userService.count(usrQuery);
            stepData.put("Count", usrCount);
        } catch (KapuaException e) {
            verifyException(e);
        }
    }

    @Then("^I find user \"(.*)\"$")
    public void thenIFindUser(String userName) throws Exception {

        primeException();
        try {
            User user = userService.findByName(userName);
            Assert.assertNotNull("User doesn't exist.", user);
        } catch (KapuaException e) {
            verifyException(e);
        }
    }

    @Then("^I don't find user \"(.*)\"$")
    public void thenIdontFindUser(String userName) throws Exception {

        primeException();
        try {
            User user = userService.findByName(userName);
            Assert.assertNull("User still exists.", user);
            Account findAcc = (Account) stepData.get("LastAccount");
            User findUser = (User) stepData.get("LastUser");
            user = userService.find(findAcc.getId(), findUser.getId());
            Assert.assertNull("User still exists.", user);
        } catch (KapuaException e) {
            verifyException(e);
        }
    }

    @Then("^I don't find user credentials$")
    public void thenIdontFindUserCredentials() throws Exception {

        primeException();
        try {
            Account findAcc = (Account) stepData.get("LastAccount");
            User findUser = (User) stepData.get("LastUser");
            CredentialListResult credentials = credentialService.findByUserId(findAcc.getId(), findUser.getId());
            Assert.assertTrue("Credentials for user still exist.", credentials.isEmpty());
        } catch (KapuaException e) {
            verifyException(e);
        }
    }

    @When("^I configure the account service$")
    public void setAccountServiceConfig(List<TestConfig> testConfigs)
            throws Exception {
        Map<String, Object> valueMap = new HashMap<>();
        KapuaId accId;
        KapuaId scopeId;

        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
        }

        primeException();
        try {
            Account tmpAccount = (Account) stepData.get("LastAccount");
            if (tmpAccount != null) {
                accId = tmpAccount.getId();
                scopeId = (tmpAccount.getScopeId() != null) ? tmpAccount.getScopeId() : ROOT_SCOPE_ID;
            } else {
                accId = ROOT_SCOPE_ID;
                scopeId = ROOT_SCOPE_ID;
            }
            accountService.setConfigValues(accId, scopeId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I configure the user service$")
    public void setUserServiceConfig(List<TestConfig> testConfigs)
            throws Exception {
        Map<String, Object> valueMap = new HashMap<>();
        KapuaId accId;
        KapuaId scopeId;
        Account tmpAccount = (Account) stepData.get("LastAccount");

        if (tmpAccount != null) {
            accId = tmpAccount.getId();
            scopeId = (tmpAccount.getScopeId() != null) ? tmpAccount.getScopeId() : ROOT_SCOPE_ID;
        } else {
            accId = ROOT_SCOPE_ID;
            scopeId = ROOT_SCOPE_ID;
        }

        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
        }

        primeException();
        try {
            userService.setConfigValues(accId, scopeId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @And("^Using kapua-sys account$")
    public void usingSysAccount() {
        stepData.put("LastAccount", null);
    }

    /**
     * Extract list of users form step parameter table and create those users in
     * kapua.
     * Operation is performed in privileged mode, without access and authorization checks.
     *
     * @param userList list of users in step
     * @param account  account in which users are created
     * @return Set of created users as ComparableUser Set
     * @throws Exception
     */
    private HashSet<ComparableUser> createUsersInList(List<TestUser> userList, Account account) throws Exception {
        HashSet<ComparableUser> users = new HashSet<>();
        KapuaSecurityUtils.doPrivileged(() -> {
            primeException();
            try {
                for (TestUser userItem : userList) {
                    String name = userItem.getName();
                    String displayName = userItem.getDisplayName();
                    String email = userItem.getEmail();
                    String phone = userItem.getPhoneNumber();
                    KapuaEid scopeId = (KapuaEid) account.getId();
                    Date expirationDate = userItem.getExpirationDate();

                    UserCreator userCreator = userCreatorCreator(name, displayName, email, phone, scopeId, expirationDate);
                    User user = userService.create(userCreator);
                    users.add(new ComparableUser(user));
                }
            } catch (KapuaException ke) {
                verifyException(ke);
            }

            return null;
        });

        return users;
    }

    /**
     * Create account in privileged mode as kapua-sys user.
     * Account is created in scope specified by scopeId in testAccount parameter.
     * This is not accountId, but account under which it is created. AccountId itself
     * is created automatically.
     *
     * @param testAccount basic data about account
     * @return Kapua Account object
     */
    private Account createAccount(TestAccount testAccount) throws Exception {
        List<Account> accountList = new ArrayList<>();
        KapuaSecurityUtils.doPrivileged(() -> {
            primeException();
            try {
                Account account = accountService.create(accountCreatorCreator(testAccount.getName(),
                        testAccount.getScopeId(), testAccount.getExpirationDate()));
                accountList.add(account);
            } catch (KapuaException ke) {
                verifyException(ke);
            }

            return null;
        });

        return accountList.size() == 1 ? accountList.get(0) : null;
    }

    /**
     * Creates permissions for user with specified account. Permissions are created in priveledged mode.
     *
     * @param permissionList list of permissions for user, if targetScopeId is not set user scope that is
     *                       specifed as account
     * @param user           user for whom permissions are set
     * @param account        account in which user is defined
     * @throws Exception
     */
    private void createPermissions(List<TestPermission> permissionList, ComparableUser user, Account account)
            throws Exception {

        KapuaSecurityUtils.doPrivileged(() -> {
            primeException();
            try {
                accessInfoService.create(accessInfoCreatorCreator(permissionList, user, account));
            } catch (KapuaException ke) {
                verifyException(ke);
            }

            return null;
        });

        return;
    }

    /**
     * Create account creator.
     *
     * @param name    account name
     * @param scopeId acount scope id
     * @return
     */
    private AccountCreator accountCreatorCreator(String name, BigInteger scopeId, Date expiration) {
        AccountCreator accountCreator;

        accountCreator = new AccountFactoryImpl().newCreator(new KapuaEid(scopeId), name);
        if (expiration != null) {
            accountCreator.setExpirationDate(expiration);
        }
        accountCreator.setOrganizationName("ACME Inc.");
        accountCreator.setOrganizationEmail("some@one.com");

        return accountCreator;
    }

    /**
     * Create userCreator instance with full data about user.
     *
     * @return UserCreator instance for creating user
     */
    private UserCreator userCreatorCreator(String name, String displayName, String email, String phone, KapuaEid scopeId, Date expirationDate) {
        UserCreator userCreator = new UserFactoryImpl().newCreator(scopeId, name);

        userCreator.setName(name);
        userCreator.setDisplayName(displayName);
        userCreator.setEmail(email);
        userCreator.setPhoneNumber(phone);
        userCreator.setExpirationDate(expirationDate);

        return userCreator;
    }

    /**
     * Create accessInfoCreator instance with data about user permissions.
     * If target scope is not defined in permission list use account scope.
     *
     * @param permissionList list of all permissions
     * @param user           user for which permissions are set
     * @param account        that user belongs to
     * @return AccessInfoCreator instance for creating user permissions
     */
    private AccessInfoCreator accessInfoCreatorCreator(List<TestPermission> permissionList,
            ComparableUser user, Account account) {

        PermissionFactory permissionFactory = new PermissionFactoryImpl();
        AccessInfoCreator accessInfoCreator = new AccessInfoFactoryImpl().newCreator(account.getId());
        accessInfoCreator.setUserId(user.getUser().getId());
        accessInfoCreator.setScopeId(user.getUser().getScopeId());
        Set<Permission> permissions = new HashSet<>();
        if (permissionList != null) {
            for (TestPermission testPermission : permissionList) {
                Actions action = testPermission.getAction();
                KapuaEid targetScopeId = testPermission.getTargetScopeId();
                if (targetScopeId == null) {
                    targetScopeId = (KapuaEid) account.getId();
                }
                Domain domain = new UserDomain();
                Permission permission = permissionFactory.newPermission(domain,
                        action, targetScopeId);
                permissions.add(permission);
            }
        } else {
            Permission permission = permissionFactory.newPermission(null, null, null);
            permissions.add(permission);
        }
        accessInfoCreator.setPermissions(permissions);

        return accessInfoCreator;
    }

    private Date parseDateString(String date) {
        DateFormat df = new SimpleDateFormat("dd/mm/yyyy");
        Date expDate = null;
        Instant now = Instant.now();

        if (date == null) {
            return null;
        }
        // Special keywords for date
        switch (date.trim().toLowerCase()) {
            case "yesterday":
                expDate = Date.from(now.minus(Duration.ofDays(1)));
                break;
            case "today":
                expDate = Date.from(now);
                break;
            case "tomorrow":
                expDate = Date.from(now.plus(Duration.ofDays(1)));
                break;
            case "null":
                break;
        }

        // Not one of the special cases. Just parse the date.
        try {
            expDate = df.parse(date.trim().toLowerCase());
        } catch (ParseException | NullPointerException e) {
            // skip, leave date null
        }

        return expDate;
    }

}
