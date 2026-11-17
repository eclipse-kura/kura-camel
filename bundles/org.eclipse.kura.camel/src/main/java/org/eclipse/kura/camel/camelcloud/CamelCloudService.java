/*******************************************************************************
 * Copyright (c) 2011, 2026 Red Hat Inc and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Red Hat Inc
 *  Eurotech
 *******************************************************************************/
package org.eclipse.kura.camel.camelcloud;

import org.eclipse.kura.cloudconnection.CloudConnectionManager;
import org.eclipse.kura.cloudconnection.CloudEndpoint;

/**
 * An extension interface adding Camel specific functionality to the {@link CloudConnectionManager} and
 * {@link CloudEndpoint} interfaces
 */
public interface CamelCloudService extends CloudConnectionManager, CloudEndpoint {

    /**
     * Set the base Camel endpoint used to exchange messages of an application
     * <p>
     * The base endpoint is a format string (e.g. {@code vm:%s}) in which the internal queue name will be
     * substituted. If the string contains no {@code %s} the queue name is appended instead.
     * </p>
     *
     * @param applicationId
     *            the application ID the base endpoint applies to
     * @param baseEndpoint
     *            the base endpoint
     */
    void registerBaseEndpoint(String applicationId, String baseEndpoint);
}
