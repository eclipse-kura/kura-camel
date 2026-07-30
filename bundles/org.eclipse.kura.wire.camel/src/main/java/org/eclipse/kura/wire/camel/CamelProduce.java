/*******************************************************************************
 * Copyright (c) 2018, 2026 Red Hat Inc and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Red Hat Inc
 *  heyoulin <heyoulin@gmail.com>
 *******************************************************************************/
package org.eclipse.kura.wire.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.eclipse.kura.wire.WireEnvelope;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "org.eclipse.kura.wire.camel.CamelProduce", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, service = { org.eclipse.kura.configuration.ConfigurableComponent.class, org.eclipse.kura.wire.WireReceiver.class, org.eclipse.kura.wire.WireComponent.class, org.osgi.service.wireadmin.Consumer.class })
@Designate(ocd = CamelProduceOptions.class, factory = true)
public class CamelProduce extends AbstractReceiverWireComponent {

    private static final Logger logger = LoggerFactory.getLogger(CamelProduce.class);

    private FluentProducerTemplate template = null;

    @Override
    protected void bindContext(final CamelContext context) {

        try {
            closeTemplate();
            if (context != null) {
                this.template = context.createFluentProducerTemplate();
            }
        } catch (Exception e) {
            logger.warn("Failed to bind Camel context", e);
        }

    }

    @Override
    protected void processReceive(final CamelContext context, final String endpointUri, final WireEnvelope envelope)
            throws Exception {
        if (template != null && context != null) {
            try {
                template //
                        .withBody(envelope) //
                        .to(endpointUri) //
                        .asyncSend();
            } catch (Exception e) {
                logger.error("asyncSend error", e);
            }
        } else {
            logger.debug("FluentProducerTemplate is changing. Skip send massage and wait next massage");
        }
    }

    @Override
    protected void deactivate() {
        closeTemplate();
    }

    private void closeTemplate() {
        if (template != null) {
            try {
                template.stop();
            } catch (Exception ignored) {
            } finally {
                template = null;
            }
        }
    }
}
