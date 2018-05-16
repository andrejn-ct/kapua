###############################################################################
# Copyright (c) 2018 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech - initial API and implementation
###############################################################################
@serviceevents
Feature: EndpointInfo service with Service Events
    Basic workflow of Endpoint Info creation and deletion, where Service Events are triggered on
    account deletion.

    Background: Creation of account and user with credentials
        Create a set of accounts and users with all the required service configurations.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
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
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |

    @StartEventBroker
    Scenario: Start event broker for all scenarios

    Scenario: Account is deleted, all endpoint info items for the account must be deleted too

        Given I select account "account-a"
        And The following endpoints for the current account
            | schema  | url    | port  |
            | http    | a.b.c  | 111   |
            | http    | a.b.c  | 112   |
            | http    | a.b.c  | 113   |
        Given I select account "account-b"
        And The following endpoints for the current account
            | schema  | url    | port  |
            | http    | d.e.f  | 222   |
            | http    | d.e.f  | 223   |
        When I query for endpoints in account "account-a"
        Then There are exactly 3 endpoints
        When I query for endpoints in account "account-b"
        Then There are exactly 2 endpoints
        Given I select account "account-b"
        Given I try to delete account "account-b"
        And I wait for 10 seconds
        Given I expect the exception "KapuaException" with the text "Persistence Operation"
        When I query for endpoints in the current account
        Then An exception was thrown
#        Then There is no such endpoint
        When I query for endpoints in account "account-a"
        Then There are exactly 3 endpoints

    @StopEventBroker
    Scenario: Stop event broker for all scenarios
