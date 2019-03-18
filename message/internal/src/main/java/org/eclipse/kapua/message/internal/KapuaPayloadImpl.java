/*******************************************************************************
 * Copyright (c) 2016, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.message.internal;

import org.eclipse.kapua.message.KapuaPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link KapuaPayload} implementation.
 *
 * @since 1.0.0
 */
public class KapuaPayloadImpl implements KapuaPayload {

    private Map<String, Object> metrics;
    private byte[] body;

    /**
     * Constructor
     */
    public KapuaPayloadImpl() {
    }

    @Override
    public Map<String, Object> getMetrics() {
        if (metrics == null) {
            metrics = new HashMap<>();
        }

        return metrics;
    }

    @Override
    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public void setBody(byte[] body) {
        this.body = body;
    }
}
