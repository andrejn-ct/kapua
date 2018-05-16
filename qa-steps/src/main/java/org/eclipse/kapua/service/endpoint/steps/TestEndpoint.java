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

/**
 * Data object used in Cucumber features to define endpoint properties.
 */

public class TestEndpoint {

    private String schema;
    private String url;
    private int port;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
