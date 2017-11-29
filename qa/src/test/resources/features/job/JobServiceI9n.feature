###############################################################################
# Copyright (c) 2017 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech
###############################################################################
Feature: Job Service
    The Job Service is responsible for executing predefined Jobs on target devices.

    @StartBroker
    Scenario: Start broker for all scenarios

    @StartDatastore
    Scenario: Start datastore for all scenarios

    Scenario: Mock test scenario
        Given A mock test step
        Then Nothing happens

    @StopBroker
    Scenario: Stop broker after all scenarios

    @StopDatastore
    Scenario: Stop datastore after all scenarios
