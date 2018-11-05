###############################################################################
# Copyright (c) 2018 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech
###############################################################################
@serviceevents
Feature: Authentication Service

    Scenario: Start event broker for all scenarios

        Given Start Event Broker

    Scenario: Delete a user and all the credentials for the user should be deleted
    Provide a number of accounts and users. Set each user with a set of credentials.
    After deleting a user, the user credentials must be deleted too.
    The credentials for the remaining users must remain.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        Given Account
            | name      | scopeId |
            | account-a | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the credential service
            | type    | name                       | value |
            | boolean | lockoutPolicy.enabled      | false |
            | integer | lockoutPolicy.maxFailures  | 1     |
            | integer | lockoutPolicy.resetAfter   | 300   |
            | integer | lockoutPolicy.lockDuration | 1     |
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-a-1 | Kapua User A 1 | kapua_a_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-2 | Kapua User A 2 | kapua_a_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name      | password          | enabled |
            | kapua-a-1 | ToManySecrets123# | true    |
            | kapua-a-2 | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the credential service
            | type    | name                   | value |
            | type    | name                       | value |
            | boolean | lockoutPolicy.enabled      | false |
            | integer | lockoutPolicy.maxFailures  | 1     |
            | integer | lockoutPolicy.resetAfter   | 300   |
            | integer | lockoutPolicy.lockDuration | 1     |
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-b-1 | Kapua User B 1 | kapua_b_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-b-2 | Kapua User B 2 | kapua_b_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name      | password          | enabled |
            | kapua-b-1 | ToManySecrets123# | true    |
            | kapua-b-2 | ToManySecrets123# | true    |

        When I query for all the credentials in account "account-a"
        Then I find 2 credentials
        When I query for all the credentials in account "account-b"
        Then I find 2 credentials
        Given I select user "kapua-a-1"
        When I try to delete user "kapua-a-1"
        Then I don't find user "kapua-a-1"
        Then I wait for 10 seconds
        When I query for all the credentials in account "account-a"
        Then I find 1 credential
        When I query for the credentials of user "kapua-a-2"
        Then I find 1 credential
        When I query for all the credentials in account "account-b"
        Then I find 2 credential

    Scenario: Delete an account and all the credentials for that account should be deleted
    Provide a number of accounts and users. Set each user with a set of credentials.
    After deleting an account, the user credentials must be deleted too.
    The credentials for the remaining accounts must remain.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        Given Account
            | name      | scopeId |
            | account-a | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the credential service
            | type    | name                       | value |
            | boolean | lockoutPolicy.enabled      | false |
            | integer | lockoutPolicy.maxFailures  | 1     |
            | integer | lockoutPolicy.resetAfter   | 300   |
            | integer | lockoutPolicy.lockDuration | 1     |
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-a-1 | Kapua User A 1 | kapua_a_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-2 | Kapua User A 2 | kapua_a_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name      | password          | enabled |
            | kapua-a-1 | ToManySecrets123# | true    |
            | kapua-a-2 | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the credential service
            | type    | name                   | value |
            | type    | name                       | value |
            | boolean | lockoutPolicy.enabled      | false |
            | integer | lockoutPolicy.maxFailures  | 1     |
            | integer | lockoutPolicy.resetAfter   | 300   |
            | integer | lockoutPolicy.lockDuration | 1     |
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-b-1 | Kapua User B 1 | kapua_b_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-b-2 | Kapua User B 2 | kapua_b_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name      | password          | enabled |
            | kapua-b-1 | ToManySecrets123# | true    |
            | kapua-b-2 | ToManySecrets123# | true    |

        When I query for all the credentials in account "account-a"
        Then I find 2 credentials
        When I query for all the credentials in account "account-b"
        Then I find 2 credentials
        Given I select account "account-a"
        When I try to delete account "account-a"
        And I search for account "account-a"
        Then I don't find the account
        Given I wait for 10 seconds
        When I query for all the credentials in the last account
        Then I find no credentials
        When I query for all the credentials in account "account-b"
        Then I find 2 credential

    Scenario: Delete an account and all the access tokens for this account have to be deleted too
    Provide a number of accounts and users. Create a number of access tokens for the users in the accounts.
    After deleting an account, all the access tokens must be deleted too.
    Access tokens for other accounts must remain.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        Given Account
            | name      | scopeId |
            | account-a | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the credential service
            | type    | name                       | value |
            | boolean | lockoutPolicy.enabled      | false |
            | integer | lockoutPolicy.maxFailures  | 1     |
            | integer | lockoutPolicy.resetAfter   | 300   |
            | integer | lockoutPolicy.lockDuration | 1     |
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-a-1 | Kapua User A 1 | kapua_a_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-2 | Kapua User A 2 | kapua_a_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name      | password          | enabled |
            | kapua-a-1 | ToManySecrets123# | true    |
            | kapua-a-2 | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the credential service
            | type    | name                   | value |
            | type    | name                       | value |
            | boolean | lockoutPolicy.enabled      | false |
            | integer | lockoutPolicy.maxFailures  | 1     |
            | integer | lockoutPolicy.resetAfter   | 300   |
            | integer | lockoutPolicy.lockDuration | 1     |
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-b-1 | Kapua User B 1 | kapua_b_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-b-2 | Kapua User B 2 | kapua_b_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name      | password          | enabled |
            | kapua-b-1 | ToManySecrets123# | true    |
            | kapua-b-2 | ToManySecrets123# | true    |

        Given The access token "token-a-1-1" for user "kapua-a-1" in scope "account-a"
        And The access token "token-a-1-2" for user "kapua-a-1" in scope "account-a"
        And The access token "token-a-1-3" for user "kapua-a-1" in scope "account-a"
        Given The access token "token-a-2-1" for user "kapua-a-2" in scope "account-a"
        And The access token "token-a-2-2" for user "kapua-a-2" in scope "account-a"

        Given The access token "token-b-1-1" for user "kapua-b-1" in scope "account-b"
        And The access token "token-b-1-2" for user "kapua-b-1" in scope "account-b"
        Given The access token "token-b-2-1" for user "kapua-b-2" in scope "account-b"
        And The access token "token-b-2-2" for user "kapua-b-2" in scope "account-b"
        And The access token "token-b-2-3" for user "kapua-b-2" in scope "account-b"

        When I query for all access tokens in account "account-a"
        Then I find 5 tokens
        When I query for all access tokens for user "kapua-a-1"
        Then I find 3 tokens

        Given I select account "account-b"
        When I try to delete account "account-b"
        And I wait for 10 seconds
        Then I don't find user "kapua-b-1"
        Then I don't find user "kapua-b-2"
        When I query for all access tokens in the last account
        Then I find no such access tokens
        When I query for all access tokens in account "account-a"
        Then I find 5 tokens

    Scenario: Delete a user and all the access tokens for this user have to be deleted too
    Provide a number of accounts and users. Create a number of access tokens for the users in the accounts.
    After deleting a user, all the user access tokens must be deleted too.
    Access tokens for other users must remain.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        Given Account
            | name      | scopeId |
            | account-a | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the credential service
            | type    | name                       | value |
            | boolean | lockoutPolicy.enabled      | false |
            | integer | lockoutPolicy.maxFailures  | 1     |
            | integer | lockoutPolicy.resetAfter   | 300   |
            | integer | lockoutPolicy.lockDuration | 1     |
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-a-1 | Kapua User A 1 | kapua_a_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-2 | Kapua User A 2 | kapua_a_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name      | password          | enabled |
            | kapua-a-1 | ToManySecrets123# | true    |
            | kapua-a-2 | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the credential service
            | type    | name                   | value |
            | type    | name                       | value |
            | boolean | lockoutPolicy.enabled      | false |
            | integer | lockoutPolicy.maxFailures  | 1     |
            | integer | lockoutPolicy.resetAfter   | 300   |
            | integer | lockoutPolicy.lockDuration | 1     |
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-b-1 | Kapua User B 1 | kapua_b_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-b-2 | Kapua User B 2 | kapua_b_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name      | password          | enabled |
            | kapua-b-1 | ToManySecrets123# | true    |
            | kapua-b-2 | ToManySecrets123# | true    |

        Given The access token "token-a-1-1" for user "kapua-a-1" in scope "account-a"
        And The access token "token-a-1-2" for user "kapua-a-1" in scope "account-a"
        And The access token "token-a-1-3" for user "kapua-a-1" in scope "account-a"
        Given The access token "token-a-2-1" for user "kapua-a-2" in scope "account-a"
        And The access token "token-a-2-2" for user "kapua-a-2" in scope "account-a"

        Given The access token "token-b-1-1" for user "kapua-b-1" in scope "account-b"
        And The access token "token-b-1-2" for user "kapua-b-1" in scope "account-b"
        Given The access token "token-b-2-1" for user "kapua-b-2" in scope "account-b"
        And The access token "token-b-2-2" for user "kapua-b-2" in scope "account-b"
        And The access token "token-b-2-3" for user "kapua-b-2" in scope "account-b"

        When I query for all access tokens in account "account-a"
        Then I find 5 tokens
        When I query for all access tokens for user "kapua-a-1"
        Then I find 3 tokens

        Given I select user "kapua-a-1"
        When I try to delete user "kapua-a-1"
        Then I don't find user "kapua-a-1"
        And I wait for 10 seconds
        When I query for all access tokens for the last user
        Then I find no such access tokens
        When I query for all access tokens for user "kapua-a-2"
        When I query for all access tokens in account "account-a"
        Then I find 2 tokens
        When I query for all access tokens in account "account-b"
        Then I find 5 tokens

    Scenario: Stop event broker for all scenarios

        Given Stop Event Broker
