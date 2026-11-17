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

import static org.apache.camel.builder.ExchangeBuilder.anExchange;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_CONTROL;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_DEVICEID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_QOS;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_RETAIN;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_TOPIC;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_TOPIC;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_CONTROL;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_DEVICE_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_QOS;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_RETAIN;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;
import org.eclipse.kura.cloudconnection.CloudEndpoint;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.subscriber.listener.CloudSubscriberListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer implementation for {@link KuraCloudComponent}
 * <p>
 * The consumer registers itself as subscriber of the {@link CloudEndpoint}. Re-establishing the subscription after
 * a connection loss is up to the cloud endpoint implementation.
 * </p>
 */
public class KuraCloudConsumer extends DefaultConsumer implements CloudSubscriberListener {

    // Camel 3 removed the inherited 'log' field from DefaultConsumer
    private static final Logger logger = LoggerFactory.getLogger(KuraCloudConsumer.class);

    private final CloudEndpoint cloudEndpoint;

    public KuraCloudConsumer(final Endpoint endpoint, final Processor processor, final CloudEndpoint cloudEndpoint) {
        super(endpoint, processor);
        this.cloudEndpoint = cloudEndpoint;
    }

    // Life-cycle

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        logger.debug("Registering cloud subscriber.");

        final Map<String, Object> subscriptionProperties = new HashMap<>();
        subscriptionProperties.put(PROP_APP_ID, getEndpoint().getApplicationId());
        subscriptionProperties.put(PROP_APP_TOPIC, getEndpoint().getTopic());
        subscriptionProperties.put(PROP_QOS, getEndpoint().getQos());
        subscriptionProperties.put(PROP_CONTROL, getEndpoint().isControl());

        this.cloudEndpoint.registerSubscriber(subscriptionProperties, this);
    }

    @Override
    protected void doStop() throws Exception {
        logger.debug("Unregistering cloud subscriber.");

        try {
            this.cloudEndpoint.unregisterSubscriber(this);
        } catch (final Exception e) {
            logger.info("Failed to unsubscribe", e);
        }

        super.doStop();
    }

    // CloudSubscriberListener callback

    @Override
    public void onMessageArrived(final KuraMessage message) {
        final Map<String, Object> properties = message.getProperties();

        final String deviceId = (String) properties.get(PROP_DEVICE_ID);
        final String appTopic = (String) properties.get("appTopic");

        logger.debug("Received message with deviceId {}, application topic {}.", deviceId, appTopic);

        final Object qos = properties.getOrDefault(PROP_QOS, getEndpoint().getQos());
        final Object control =
                properties.getOrDefault(PROP_CONTROL, getEndpoint().isControl());

        final Exchange exchange = anExchange(getEndpoint().getCamelContext()) //
                .withBody(message.getPayload()) //
                .withHeader(CAMEL_KURA_CLOUD_TOPIC, appTopic) //
                .withHeader(CAMEL_KURA_CLOUD_DEVICEID, deviceId) //
                .withHeader(CAMEL_KURA_CLOUD_QOS, qos) //
                .withHeader(CAMEL_KURA_CLOUD_CONTROL, control) //
                .withHeader(CAMEL_KURA_CLOUD_RETAIN, properties.get(PROP_RETAIN)) //
                .build();
        exchange.getExchangeExtension().setFromEndpoint(getEndpoint());

        try {
            getProcessor().process(exchange);
        } catch (final Exception e) {
            handleException("Error while processing an incoming message:", e);
        }
    }

    // Getters

    @Override
    public KuraCloudEndpoint getEndpoint() {
        return (KuraCloudEndpoint) super.getEndpoint();
    }
}
