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
package org.eclipse.kura.camel.cloud;

import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_CONTROL;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_DEVICEID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_PRIORITY;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_QOS;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_RETAIN;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_TOPIC;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_TOPIC;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_CONTROL;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_DEVICE_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_PRIORITY;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_QOS;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_RETAIN;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.support.DefaultProducer;
import org.eclipse.kura.cloudconnection.CloudEndpoint;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.message.KuraPayload;

/**
 * Producer implementation for {@link KuraCloudComponent}
 */
public class KuraCloudProducer extends DefaultProducer {

    // Visible for testing
    CloudEndpoint cloudEndpoint;

    public KuraCloudProducer(KuraCloudEndpoint endpoint, CloudEndpoint cloudEndpoint) {
        super(endpoint);
        this.cloudEndpoint = cloudEndpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();

        String topic = firstNotNull(
                in.getHeader(CAMEL_KURA_CLOUD_TOPIC, String.class),
                getEndpoint().getTopic());
        int qos = firstNotNull(
                in.getHeader(CAMEL_KURA_CLOUD_QOS, Integer.class), getEndpoint().getQos());
        int priority = firstNotNull(
                in.getHeader(CAMEL_KURA_CLOUD_PRIORITY, Integer.class),
                getEndpoint().getPriority());
        boolean retain = firstNotNull(
                in.getHeader(CAMEL_KURA_CLOUD_RETAIN, Boolean.class),
                getEndpoint().isRetain());
        boolean control = firstNotNull(
                in.getHeader(CAMEL_KURA_CLOUD_CONTROL, Boolean.class),
                getEndpoint().isControl());
        String deviceId = firstNotNull(
                in.getHeader(CAMEL_KURA_CLOUD_DEVICEID, String.class),
                getEndpoint().getDeviceId());

        Object body = in.getBody();
        if (body == null) {
            throw new RuntimeException("Cannot produce null payload.");
        }

        if (!(body instanceof KuraPayload)) {
            KuraPayload payload = new KuraPayload();
            if (body instanceof byte[]) {
                payload.setBody((byte[]) body);
            } else {
                byte[] payloadBytes = in.getBody(byte[].class);
                if (payloadBytes != null) {
                    payload.setBody(in.getBody(byte[].class));
                } else {
                    payload.setBody(in.getBody(String.class).getBytes());
                }
            }
            body = payload;
        }

        final Map<String, Object> properties = new HashMap<>();
        properties.put(PROP_APP_ID, getEndpoint().getApplicationId());
        properties.put(PROP_APP_TOPIC, topic);
        properties.put(PROP_QOS, qos);
        properties.put(PROP_RETAIN, retain);
        properties.put(PROP_PRIORITY, priority);
        properties.put(PROP_CONTROL, control);
        if (deviceId != null) {
            properties.put(PROP_DEVICE_ID, deviceId);
        }

        this.cloudEndpoint.publish(new KuraMessage((KuraPayload) body, properties));
    }

    // Getters

    @Override
    public KuraCloudEndpoint getEndpoint() {
        return (KuraCloudEndpoint) super.getEndpoint();
    }

    // Helpers

    private static <T> T firstNotNull(T first, T second) {
        return first != null ? first : second;
    }
}
