/*******************************************************************************
 * Copyright (c) 2011, 2026 Red Hat Inc and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Red Hat Inc
 *******************************************************************************/
package org.eclipse.kura.camel.cloud;

import static org.eclipse.kura.camel.internal.utils.KuraServiceFactory.retrieveService;

import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.eclipse.kura.cloudconnection.CloudEndpoint;

/**
 * The Camel component for providing "kura-cloud"
 */
public class KuraCloudComponent extends DefaultComponent {

    public static final String DEFAULT_NAME = "kura-cloud";

    private CloudEndpoint cloudEndpoint;

    public KuraCloudComponent() {
        super();
    }

    // Constructors

    public KuraCloudComponent(final CamelContext context) {
        super(context);
    }

    public KuraCloudComponent(final CamelContext context, final CloudEndpoint cloudEndpoint) {
        super(context);
        this.cloudEndpoint = cloudEndpoint;
    }

    @Override
    protected void doStart() throws Exception {
        // fail fast when no CloudEndpoint is resolvable, before any route starts
        getCloudEndpoint();
        super.doStart();
    }

    public synchronized CloudEndpoint getCloudEndpoint() {
        final CloudEndpoint endpoint = lookupCloudEndpoint();
        if (endpoint == null) {
            throw new IllegalStateException(
                    "'cloudEndpoint' is not set and not found in Camel context service registry");
        }
        return endpoint;
    }

    // Operations

    @Override
    protected Endpoint createEndpoint(String uri, String remain, Map<String, Object> parameters) throws Exception {
        final KuraCloudEndpoint kuraCloudEndpoint = new KuraCloudEndpoint(uri, this);

        final String[] res = remain.split("/", 2);
        if (res.length < 2) {
            throw new IllegalArgumentException("Wrong kura-cloud URI format. Should be: kura-cloud:app/topic");
        }
        parameters.put(KuraCloudConstants.APPLICATION_ID, res[0]);
        parameters.put(KuraCloudConstants.TOPIC, res[1]);

        setProperties(kuraCloudEndpoint, parameters);

        return kuraCloudEndpoint;
    }

    protected CloudEndpoint lookupCloudEndpoint() {
        if (this.cloudEndpoint == null) {
            this.cloudEndpoint =
                    retrieveService(CloudEndpoint.class, getCamelContext().getRegistry());
        }
        return this.cloudEndpoint;
    }
}
