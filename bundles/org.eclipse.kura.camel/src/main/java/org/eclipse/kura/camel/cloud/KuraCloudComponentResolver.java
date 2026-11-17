/*******************************************************************************
 * Copyright (c) 2016, 2026 Red Hat Inc and others
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
package org.eclipse.kura.camel.cloud;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.spi.ComponentResolver;
import org.eclipse.kura.cloudconnection.CloudEndpoint;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resolver for "kura-cloud"
 * <p>
 * This resolver will register to any instance of {@link CloudEndpoint} and
 * wrap it into a {@link KuraCloudComponent} instance.
 * </p>
 * <p>
 * If you need finer grained control, consider using the {@link org.eclipse.kura.camel.runner.CamelRunner} mechanism.
 * </p>
 */
// The annotation is fully qualified on purpose: the simple name Component is already
// taken by org.apache.camel.Component, which this class returns.
@org.osgi.service.component.annotations.Component(
        name = "org.eclipse.kura.camel.KuraCloudComponentResolver",
        service = ComponentResolver.class,
        properties = "OSGI-INF/kuraCloudResolver.properties")
public class KuraCloudComponentResolver implements ComponentResolver {

    private static final Logger logger = LoggerFactory.getLogger(KuraCloudComponentResolver.class);

    private CloudEndpoint cloudEndpoint;

    @Reference(name = "CloudEndpoint", service = CloudEndpoint.class, unbind = "-")
    public void setCloudEndpoint(final CloudEndpoint cloudEndpoint) {
        this.cloudEndpoint = cloudEndpoint;
    }

    @Override
    public Component resolveComponent(final String name, final CamelContext context) throws Exception {
        switch (name) {
            case KuraCloudComponent.DEFAULT_NAME:
                final KuraCloudComponent component = new KuraCloudComponent(context, this.cloudEndpoint);
                logger.debug("Created new cloud component: {}", component);
                return component;
            default:
        }
        return null;
    }
}
