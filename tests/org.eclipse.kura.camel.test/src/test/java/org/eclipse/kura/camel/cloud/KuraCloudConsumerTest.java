/*******************************************************************************
 * Copyright (c) 2017, 2026 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 ******************************************************************************/
package org.eclipse.kura.camel.cloud;

import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_DEVICEID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_TOPIC;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_TOPIC;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_CONTROL;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_DEVICE_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_QOS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.kura.cloudconnection.CloudEndpoint;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.message.KuraPayload;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class KuraCloudConsumerTest {

    @Test
    public void testStartRegistersAndStopUnregistersSubscriber() throws Exception {
        try (CamelContext context = new DefaultCamelContext()) {
            KuraCloudEndpoint endpointMock = mock(KuraCloudEndpoint.class);
            when(endpointMock.getCamelContext()).thenReturn(context);
            when(endpointMock.getApplicationId()).thenReturn("app");
            when(endpointMock.getTopic()).thenReturn("topic");
            when(endpointMock.getQos()).thenReturn(1);
            when(endpointMock.isControl()).thenReturn(true);

            CloudEndpoint cloudEndpointMock = mock(CloudEndpoint.class);
            KuraCloudConsumer consumer = new KuraCloudConsumer(endpointMock, mock(Processor.class), cloudEndpointMock);

            consumer.start();

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(cloudEndpointMock).registerSubscriber(propsCaptor.capture(), eq(consumer));
            assertEquals("app", propsCaptor.getValue().get(PROP_APP_ID));
            assertEquals("topic", propsCaptor.getValue().get(PROP_APP_TOPIC));
            assertEquals(1, propsCaptor.getValue().get(PROP_QOS));
            assertEquals(true, propsCaptor.getValue().get(PROP_CONTROL));

            consumer.stop();

            verify(cloudEndpointMock).unregisterSubscriber(consumer);
        }
    }

    @Test
    public void testMessageArrived() throws Exception {
        try (CamelContext context = new DefaultCamelContext()) {
            KuraCloudEndpoint endpointMock = mock(KuraCloudEndpoint.class);
            when(endpointMock.getCamelContext()).thenReturn(context);
            when(endpointMock.getQos()).thenReturn(0);

            Processor processorMock = mock(Processor.class);
            KuraCloudConsumer consumer = new KuraCloudConsumer(endpointMock, processorMock, mock(CloudEndpoint.class));

            KuraPayload payload = new KuraPayload();
            Map<String, Object> props = new HashMap<>();
            props.put(PROP_DEVICE_ID, "device");
            props.put("appTopic", "the/topic");

            consumer.onMessageArrived(new KuraMessage(payload, props));

            ArgumentCaptor<Exchange> captor = ArgumentCaptor.forClass(Exchange.class);
            verify(processorMock).process(captor.capture());

            Exchange exchange = captor.getValue();
            assertSame(payload, exchange.getIn().getBody());
            assertEquals("the/topic", exchange.getIn().getHeader(CAMEL_KURA_CLOUD_TOPIC));
            assertEquals("device", exchange.getIn().getHeader(CAMEL_KURA_CLOUD_DEVICEID));
        }
    }
}
