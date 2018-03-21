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
package org.eclipse.kapua.service.authentication.steps;

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
import org.eclipse.kapua.service.authentication.AuthenticationService;
import org.eclipse.kapua.service.authentication.LoginCredentials;
import org.eclipse.kapua.service.authentication.credential.Credential;
import org.eclipse.kapua.service.authentication.credential.CredentialAttributes;
import org.eclipse.kapua.service.authentication.credential.CredentialCreator;
import org.eclipse.kapua.service.authentication.credential.CredentialFactory;
import org.eclipse.kapua.service.authentication.credential.CredentialListResult;
import org.eclipse.kapua.service.authentication.credential.CredentialQuery;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authentication.credential.CredentialStatus;
import org.eclipse.kapua.service.authentication.credential.CredentialType;
import org.eclipse.kapua.service.authentication.credential.shiro.CredentialFactoryImpl;
import org.eclipse.kapua.service.authentication.shiro.UsernamePasswordCredentialsImpl;
import org.eclipse.kapua.service.authentication.token.AccessToken;
import org.eclipse.kapua.service.authentication.token.AccessTokenAttributes;
import org.eclipse.kapua.service.authentication.token.AccessTokenCreator;
import org.eclipse.kapua.service.authentication.token.AccessTokenFactory;
import org.eclipse.kapua.service.authentication.token.AccessTokenListResult;
import org.eclipse.kapua.service.authentication.token.AccessTokenQuery;
import org.eclipse.kapua.service.authentication.token.AccessTokenService;
import org.eclipse.kapua.service.authorization.steps.TestCredential;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserService;
import org.eclipse.kapua.service.user.steps.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

/**
 * Implementation of Gherkin steps used in UserServiceI9n.feature scenarios.
 */
@ScenarioScoped
public class AuthenticationServiceSteps extends BaseQATests {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceSteps.class);


    /**
     * Security service objects
     */
    private AuthenticationService authenticationService;
    private AccessTokenService accessTokenService;
    private AccessTokenFactory accessTokenFactory;
    private CredentialService credentialService;
    private CredentialFactory credentialFactory;

    /**
     * Supporting service references. Obtained by locator.
     */
    private AccountService accountService;
    private UserService userService;

    @Inject
    public AuthenticationServiceSteps(StepData stepData, DBHelper dbHelper) {

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
        accessTokenService = locator.getService(AccessTokenService.class);
        accessTokenFactory = locator.getFactory(AccessTokenFactory.class);
        credentialService = locator.getService(CredentialService.class);
        credentialFactory = locator.getFactory(CredentialFactory.class);

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

    @When("^I configure the credential service$")
    public void setCredentialServiceConfig(List<TestConfig> testConfigs)
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
            credentialService.setConfigValues(accId, scopeId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I login as user with name \"(.*)\" and password \"(.*)\"$")
    public void loginUser(String userName, String password) throws Exception {

        String passwd = password;
        LoginCredentials credentials = new UsernamePasswordCredentialsImpl(userName, passwd);
        authenticationService.logout();

        primeException();
        try {
            authenticationService.login(credentials);
        } catch (KapuaException e) {
            verifyException(e);
        }
    }

    @Then("^I logout$")
    public void logout() throws KapuaException {
        authenticationService.logout();
    }

    @Given("^The access token \"(.+)\" for user \"(.+)\" in the current scope$")
    public void createAccessTokenForUser(String tokenName, String userName) throws Exception {

        Account requiredAccount = (Account) stepData.get("LastAccount");
        User requiredUser;
        Date expirationDate = new Date(System.currentTimeMillis() + 1000 * 3600 * 24 * 14);
        AccessTokenCreator tokenCreator;
        AccessToken tmpToken;

        try {
            primeException();
            stepData.remove("AccessToken");
            requiredUser = userService.findByName(userName);
            tokenCreator = accessTokenFactory.newCreator(requiredAccount.getId(), requiredUser.getId(), tokenName,
                    expirationDate, "No", expirationDate);
            tmpToken = accessTokenService.create(tokenCreator);
            stepData.put("AccessToken", tmpToken);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^The access token \"(.+)\" for user \"(.+)\" in scope \"(.+)\"$")
    public void createAccessTokenForUserInScope(String tokenName, String userName, String scopeName) throws Exception {

        Account requiredAccount;
        User requiredUser;
        Date expirationDate = new Date(System.currentTimeMillis() + 1000 * 3600 * 24 * 14);
        AccessTokenCreator tokenCreator;
        AccessToken tmpToken;

        try {
            primeException();
            stepData.remove("AccessToken");
            requiredAccount = accountService.findByName(scopeName);
            requiredUser = userService.findByName(userName);
            tokenCreator = accessTokenFactory.newCreator(requiredAccount.getId(), requiredUser.getId(), tokenName,
                    expirationDate, "No", expirationDate);
            tmpToken = accessTokenService.create(tokenCreator);
            stepData.put("AccessToken", tmpToken);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the access tokens? \"(.+)\" in account \"(.+)\"$")
    public void queryForAccessTokenInScope(String tokenName, String scopeName) throws Exception {

        Account tmpAcc = accountService.findByName(scopeName);
        AccessTokenListResult tmpList;
        AccessTokenQuery tmpQuery = accessTokenFactory.newQuery(tmpAcc.getId());
        tmpQuery.setPredicate(new AttributePredicateImpl<>(AccessTokenAttributes.TOKEN_ID, tokenName));

        try {
            stepData.remove("AccessToken");
            stepData.remove("AccessTokenList");
            primeException();
            tmpList = (AccessTokenListResult) accessTokenService.query(tmpQuery);
            stepData.put("AccessTokenList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("AccessToken", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for all access tokens in account \"(.+)\"$")
    public void queryForAllAccessTokenInScope(String scopeName) throws Exception {

        Account tmpAcc = accountService.findByName(scopeName);
        AccessTokenListResult tmpList;
        AccessTokenQuery tmpQuery = accessTokenFactory.newQuery(tmpAcc.getId());

        try {
            stepData.remove("AccessToken");
            stepData.remove("AccessTokenList");
            primeException();
            tmpList = (AccessTokenListResult) accessTokenService.query(tmpQuery);
            stepData.put("AccessTokenList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("AccessToken", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for all access tokens in the last account$")
    public void queryForAllAccessTokenInScope() throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");;
        AccessTokenListResult tmpList;
        AccessTokenQuery tmpQuery = accessTokenFactory.newQuery(tmpAcc.getId());

        try {
            stepData.remove("AccessToken");
            stepData.remove("AccessTokenList");
            primeException();
            tmpList = (AccessTokenListResult) accessTokenService.query(tmpQuery);
            stepData.put("AccessTokenList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("AccessToken", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the access tokens? \"(.+)\" for user \"(.+)\"$")
    public void queryForAccessTokenForUser(String tokenName, String userName) throws Exception {

        User tmpUser = userService.findByName(userName);
        AccessTokenListResult tmpList;
        AccessTokenQuery tmpQuery = accessTokenFactory.newQuery(tmpUser.getScopeId());
        tmpQuery.setPredicate(new AttributePredicateImpl<>(AccessTokenAttributes.USER_ID, tmpUser.getId()));

        try {
            stepData.remove("AccessToken");
            stepData.remove("AccessTokenList");
            primeException();
            tmpList = (AccessTokenListResult) accessTokenService.query(tmpQuery);
            stepData.put("AccessTokenList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("AccessToken", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for all access tokens for user \"(.+)\"$")
    public void queryForAllAccessTokenForUser(String userName) throws Exception {

        User tmpUser = userService.findByName(userName);
        AccessTokenListResult tmpList;
        AccessTokenQuery tmpQuery = accessTokenFactory.newQuery(tmpUser.getScopeId());
        tmpQuery.setPredicate(new AttributePredicateImpl<>(AccessTokenAttributes.USER_ID, tmpUser.getId()));

        try {
            stepData.remove("AccessToken");
            stepData.remove("AccessTokenList");
            primeException();
            tmpList = (AccessTokenListResult) accessTokenService.query(tmpQuery);
            stepData.put("AccessTokenList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("AccessToken", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for all access tokens for the last user$")
    public void queryForAllAccessTokenForTheLastUser() throws Exception {

        User tmpUser = (User) stepData.get("LastUser");
        AccessTokenListResult tmpList;
        AccessTokenQuery tmpQuery = accessTokenFactory.newQuery(tmpUser.getScopeId());
        tmpQuery.setPredicate(new AttributePredicateImpl<>(AccessTokenAttributes.USER_ID, tmpUser.getId()));

        try {
            stepData.remove("AccessToken");
            stepData.remove("AccessTokenList");
            primeException();
            tmpList = (AccessTokenListResult) accessTokenService.query(tmpQuery);
            stepData.put("AccessTokenList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("AccessToken", tmpList.getFirstItem());
            }
        } catch(KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^I find no such access tokens?$")
    public void verifyThatNoTokenWasFound() {
        AccessTokenListResult tmpList = (AccessTokenListResult) stepData.get("AccessTokenList");
        Assert.assertTrue("An unexpected access token was found!", tmpList.isEmpty());
    }

    @Then("^I find such tokens?$")
    public void verifyThatATokenWasFound() {
        AccessTokenListResult tmpList = (AccessTokenListResult) stepData.get("AccessTokenList");
        Assert.assertFalse("No such access token!", tmpList.isEmpty());
    }

    @Then("^I find (\\d+) tokens?$")
    public void verifyThatSomeTokensWereFound(int count) {
        AccessTokenListResult tmpList = (AccessTokenListResult) stepData.get("AccessTokenList");
        Assert.assertEquals("Not the right number of access token!", count, tmpList.getSize());
    }

    @Given("^Credentials$")
    public void givenCredentials(List<TestCredential> credentialsList) throws Exception {

        for (TestCredential tmpTstCred : credentialsList) {
            createCredentials(tmpTstCred);
        }
    }

    @When("^I query for the credentials of user \"(.+)\"$")
    public void queryForUserCredentials(String userName) throws Exception {

        User targetUser = userService.findByName(userName);
        CredentialQuery query = credentialFactory.newQuery(targetUser.getScopeId());
        query.setPredicate(new AttributePredicateImpl<>(CredentialAttributes.USER_ID, targetUser.getId()));

        try {
            primeException();
            stepData.remove("Credential");
            stepData.remove("CredentialList");
            CredentialListResult tmpList = credentialService.query(query);
            stepData.put("CredentialList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("Credential", tmpList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the credentials of the last user$")
    public void queryForTheLastUserCredentials() throws Exception {

        User targetUser = (User) stepData.get("LastUser");
        CredentialQuery query = credentialFactory.newQuery(targetUser.getScopeId());
        query.setPredicate(new AttributePredicateImpl<>(CredentialAttributes.USER_ID, targetUser.getId()));

        try {
            primeException();
            stepData.remove("Credential");
            stepData.remove("CredentialList");
            CredentialListResult tmpList = credentialService.query(query);
            stepData.put("CredentialList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("Credential", tmpList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for all the credentials in account \"(.+)\"$")
    public void queryForAccountCredentials(String accountName) throws Exception {

        Account targetAccount = accountService.findByName(accountName);
        CredentialQuery query = credentialFactory.newQuery(targetAccount.getId());

        try {
            primeException();
            stepData.remove("Credential");
            stepData.remove("CredentialList");
            CredentialListResult tmpList = credentialService.query(query);
            stepData.put("CredentialList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("Credential", tmpList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for all the credentials in the last account$")
    public void queryForLastAccountCredentials() throws Exception {

        Account targetAccount = (Account) stepData.get("LastAccount");
        CredentialQuery query = credentialFactory.newQuery(targetAccount.getId());

        try {
            primeException();
            stepData.remove("Credential");
            stepData.remove("CredentialList");
            CredentialListResult tmpList = credentialService.query(query);
            stepData.put("CredentialList", tmpList);
            if (!tmpList.isEmpty()) {
                stepData.put("Credential", tmpList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^I find no credentials$")
    public void verifyThatNoCredentialWasFound() {
        CredentialListResult tmpCreds = (CredentialListResult) stepData.get("CredentialList");
        if (tmpCreds != null) {
            Assert.assertTrue("Unexpected credentials were found!", tmpCreds.isEmpty());
        }
    }

    @Then("^I find such credentials?$")
    public void verifyThatSomeCredentialsWereFound() {
        CredentialListResult tmpCreds = (CredentialListResult) stepData.get("CredentialList");
        Assert.assertFalse("No such credentials!", tmpCreds.isEmpty());
    }

    @Then("^I find (\\d+) credentials?$")
    public void verifyThatExactlySoMuchCredentialsWereFound(int count) {
        CredentialListResult tmpCreds = (CredentialListResult) stepData.get("CredentialList");
        Assert.assertEquals("Not the right number of credentials!", count, tmpCreds.getSize());
    }

    /**
     * Create credentials for specific user, set users password.
     * It finds user by name and sets its password.
     *
     * @param testCredentials
     *            username and open password
     * @return created credential
     */
    private Credential createCredentials(TestCredential testCredentials) throws Exception {
        List<Credential> credentialList = new ArrayList<>();

        KapuaSecurityUtils.doPrivileged(() -> {
            primeException();
            try {
                User user = userService.findByName(testCredentials.getName());

                Credential credential = credentialService.create(credentialCreatorCreator(user.getScopeId(),
                        user.getId(), testCredentials.getPassword(),
                        testCredentials.getStatus(), testCredentials.getExpirationDate()));
                credentialList.add(credential);
            } catch (KapuaException ke) {
                verifyException(ke);
            }

            return null;
        });

        return credentialList.size() == 1 ? credentialList.get(0) : null;
    }

    /**
     * Create credential creator for user with password.
     *
     * @param scopeId
     *            scopeId in which user is
     * @param userId
     *            userId for which credetntials are set
     * @param password
     *            open password as credetntials
     * @param status status of credentials enabled or disabled
     * @param expirationDate credential expiration date
     * @return credential creator used for creating credentials
     */
    private CredentialCreator credentialCreatorCreator(KapuaId scopeId, KapuaId userId, String password, CredentialStatus status, Date expirationDate) {
        CredentialCreator credentialCreator;

        credentialCreator = new CredentialFactoryImpl().newCreator(scopeId, userId, CredentialType.PASSWORD, password, status, expirationDate);

        return credentialCreator;
    }
}
