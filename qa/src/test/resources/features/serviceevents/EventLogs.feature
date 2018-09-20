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
Feature: Tests for service event logging
    Basic tests for the logging of service events. The functionality of the EventLog service
    is tested.

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
        And I configure the user service
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
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |

    @StartEventBroker
    Scenario: Start event broker for all scenarios

    Scenario: Account is deleted, the account users are deleted too and the events are logged
        Create an account with a number of users. Deleting the account must trigger the deletion of
        all the account users. Each deleted user should generate a service event log entry.

        Given I select account "account-a"
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-a-1 | Kapua User A 1 | kapua_a_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-2 | Kapua User A 2 | kapua_a_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-3 | Kapua User A 3 | kapua_a_3@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        When I count the users in account "account-a"
        Then I get 3
        When I delete account "account-a"
        And I wait 5 seconds
        When I count the users in the current account
        Then I get 0
        When I query for events for the current account
        Then There are exactly 3 event log entries

    Scenario: Event logging is disabled
        No log entries should be created even in case of service events being triggered.

        Given I select account "account-a"
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-a-1 | Kapua User A 1 | kapua_a_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-2 | Kapua User A 2 | kapua_a_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-3 | Kapua User A 3 | kapua_a_3@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        When I count the users in account "account-a"
        Then I get 3
        Given I configure the event logging service
            | type    | name                   | value |
            | boolean | eventLoggingEnabled    | false |
        When I delete account "account-a"
        And I wait 5 seconds
        When I count the users in the current account
        Then I get 0
        When I query for events for the current account
        Then There are exactly 0 event log entries

    Scenario: Toggling event logging at run time
        The test is conducted in two phases. First event logging is enabled by default. At this time, an
        account is deleted, resulting in the deletion of 3 users. 3 service events should be logged.
        In the second phase event logging is disabled. Another account with 3 users is deleted, but no additional
        events must be logged.

        Given I configure the event logging service
            | type    | name                   | value |
            | boolean | eventLoggingEnabled    | true  |
        Given I select account "account-a"
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-a-1 | Kapua User A 1 | kapua_a_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-2 | Kapua User A 2 | kapua_a_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-a-3 | Kapua User A 3 | kapua_a_3@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        Given I select account "account-b"
        Given The generic users
            | name      | displayName    | email               | phoneNumber     | status  | userType |
            | kapua-b-1 | Kapua User B 1 | kapua_b_1@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-b-2 | Kapua User B 2 | kapua_b_2@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
            | kapua-b-3 | Kapua User B 3 | kapua_b_3@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        Given I select account "account-a"
        When I count the users in account "account-a"
        Then I get 3
        When I delete account "account-a"
        And I wait 5 seconds
        When I count the users in the current account
        Then I get 0
        When I query for events for the current account
        Then There are exactly 3 event log entries
        Given I configure the event logging service
            | type    | name                   | value |
            | boolean | eventLoggingEnabled    | false |
        Given I select account "account-b"
        When I delete account "account-b"
        And I wait 5 seconds
        When I count the users in the current account
        Then I get 0
        When I query for events for the current account
        Then There are exactly 0 event log entries

    Scenario: Event log entry lifetime
        This test exercises the event log purging functionality. A number of events are created at different
        times (with different ages). Next, the log entry time to live is manipulated and entries purged several times.
        The number of existing log entries must decrease in line with the purging of gradually obsoleted entries.

        Given I prepare 10 events 15 days old
        And I prepare 10 events 20 days old
        And I prepare 10 events 25 days old

        Given I configure the event logging service
            | type    | name                   | value |
            | integer | eventLogTimeToLive     | 30    |
        When I purge obsolete event log entries
        And I query for all event logs
        Then There are exactly 30 event log entries

        Given I configure the event logging service
            | type    | name                   | value |
            | integer | eventLogTimeToLive     | 22    |
        When I purge obsolete event log entries
        And I query for all event logs
        Then There are exactly 20 event log entries

        Given I configure the event logging service
            | type    | name                   | value |
            | integer | eventLogTimeToLive     | 17    |
        When I purge obsolete event log entries
        And I query for all event logs
        Then There are exactly 10 event log entries

        Given I configure the event logging service
            | type    | name                   | value |
            | integer | eventLogTimeToLive     | 7    |
        When I purge obsolete event log entries
        And I query for all event logs
        Then There are exactly 0 event log entries

    @StopEventBroker
    Scenario: Stop event broker for all scenarios
