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

import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_TOPIC;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_CONTROL;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_DEVICE_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_PRIORITY;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_QOS;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_RETAIN;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants;
import org.eclipse.kura.cloudconnection.CloudEndpoint;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class KuraCloudProducerTest {

    private static final String APP_ID = "app";
    private static final String TOPIC = "topic";

    private static class Fixture {

        final CloudEndpoint cloudEndpointMock = mock(CloudEndpoint.class);
        final Exchange exchangeMock = mock(Exchange.class);
        final Message msgMock = mock(Message.class);
        final KuraCloudProducer producer;

        Fixture() {
            final KuraCloudEndpoint endpointMock = mock(KuraCloudEndpoint.class);
            when(endpointMock.getApplicationId()).thenReturn(APP_ID);
            this.producer = new KuraCloudProducer(endpointMock, this.cloudEndpointMock);
            when(this.exchangeMock.getIn()).thenReturn(this.msgMock);
        }

        Fixture headers(boolean control, String deviceId) {
            when(this.msgMock.getHeader(KuraCloudClientConstants.CAMEL_KURA_CLOUD_TOPIC, String.class))
                    .thenReturn(TOPIC);
            when(this.msgMock.getHeader(KuraCloudClientConstants.CAMEL_KURA_CLOUD_QOS, Integer.class))
                    .thenReturn(1);
            when(this.msgMock.getHeader(KuraCloudClientConstants.CAMEL_KURA_CLOUD_PRIORITY, Integer.class))
                    .thenReturn(2);
            when(this.msgMock.getHeader(KuraCloudClientConstants.CAMEL_KURA_CLOUD_RETAIN, Boolean.class))
                    .thenReturn(true);
            when(this.msgMock.getHeader(KuraCloudClientConstants.CAMEL_KURA_CLOUD_CONTROL, Boolean.class))
                    .thenReturn(control);
            when(this.msgMock.getHeader(KuraCloudClientConstants.CAMEL_KURA_CLOUD_DEVICEID, String.class))
                    .thenReturn(deviceId);
            return this;
        }

        KuraMessage process() throws Exception {
            this.producer.process(this.exchangeMock);

            final ArgumentCaptor<KuraMessage> captor = ArgumentCaptor.forClass(KuraMessage.class);
            verify(this.cloudEndpointMock).publish(captor.capture());
            return captor.getValue();
        }
    }

    private static void assertCommonProperties(final Map<String, Object> props, boolean control) {
        assertEquals(APP_ID, props.get(PROP_APP_ID));
        assertEquals(TOPIC, props.get(PROP_APP_TOPIC));
        assertEquals(1, props.get(PROP_QOS));
        assertEquals(2, props.get(PROP_PRIORITY));
        assertEquals(true, props.get(PROP_RETAIN));
        assertEquals(control, props.get(PROP_CONTROL));
    }

    @Test
    public void testProcessNoBody() throws Exception {
        // process without body

        KuraCloudEndpoint endpointMock = mock(KuraCloudEndpoint.class);
        KuraCloudProducer kcp = new KuraCloudProducer(endpointMock, null);

        Exchange exchangeMock = mock(Exchange.class);
        Message msgMock = mock(Message.class);
        when(exchangeMock.getIn()).thenReturn(msgMock);

        try {
            kcp.process(exchangeMock);
            fail("Exception was expected.");
        } catch (RuntimeException e) {
            assertEquals("Cannot produce null payload.", e.getMessage());
        }
    }

    @Test
    public void testProcessNoControl() throws Exception {
        // process with body and no control flag

        Fixture f = new Fixture().headers(false, "id");
        byte[] body = new byte[] {0x01, 0x02, 0x03};
        when(f.msgMock.getBody()).thenReturn(body);

        KuraMessage published = f.process();

        assertCommonProperties(published.getProperties(), false);
        assertEquals("id", published.getProperties().get(PROP_DEVICE_ID));
        assertArrayEquals(body, published.getPayload().getBody());
    }

    @Test
    public void testProcessControlNoDeviceId() throws Exception {
        // process with control flag and no device id

        Fixture f = new Fixture().headers(true, null);
        byte[] body = new byte[] {0x01, 0x02, 0x03};
        when(f.msgMock.getBody()).thenReturn(body);

        KuraMessage published = f.process();

        assertCommonProperties(published.getProperties(), true);
        assertFalse(published.getProperties().containsKey(PROP_DEVICE_ID));
        assertArrayEquals(body, published.getPayload().getBody());
    }

    @Test
    public void testProcessOtherBody() throws Exception {
        // process with Object body convertible to byte[]

        Fixture f = new Fixture().headers(false, "id");
        when(f.msgMock.getBody()).thenReturn(new Object());
        byte[] bodyB = new byte[] {0x01, 0x02, 0x03};
        when(f.msgMock.getBody(byte[].class)).thenReturn(bodyB);

        KuraMessage published = f.process();

        assertCommonProperties(published.getProperties(), false);
        assertArrayEquals(bodyB, published.getPayload().getBody());
    }

    @Test
    public void testProcessOtherBodyString() throws Exception {
        // process with Object body converted to String

        Fixture f = new Fixture().headers(false, "id");
        when(f.msgMock.getBody()).thenReturn(new Object());
        when(f.msgMock.getBody(byte[].class)).thenReturn(null);
        when(f.msgMock.getBody(String.class)).thenReturn("body");

        KuraMessage published = f.process();

        assertCommonProperties(published.getProperties(), false);
        assertArrayEquals("body".getBytes(), published.getPayload().getBody());
    }

    @Test
    public void testProcess() throws Exception {
        // process with body, control flag and device id

        Fixture f = new Fixture().headers(true, "id");
        byte[] body = new byte[] {0x01, 0x02, 0x03};
        when(f.msgMock.getBody()).thenReturn(body);

        KuraMessage published = f.process();

        assertCommonProperties(published.getProperties(), true);
        assertEquals("id", published.getProperties().get(PROP_DEVICE_ID));
        assertArrayEquals(body, published.getPayload().getBody());
    }
}
