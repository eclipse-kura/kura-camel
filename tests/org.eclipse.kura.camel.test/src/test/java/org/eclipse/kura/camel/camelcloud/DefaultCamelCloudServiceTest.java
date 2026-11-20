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
package org.eclipse.kura.camel.camelcloud;

import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_CONTROL;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_DEVICEID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_MESSAGEID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_PRIORITY;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_QOS;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_RETAIN;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.StartupListener;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.ExecutorServiceManager;
import org.apache.camel.spi.RouteController;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloudconnection.listener.CloudDeliveryListener;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.subscriber.listener.CloudSubscriberListener;
import org.eclipse.kura.message.KuraPayload;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/*
 * Unit test - no point in integration tests as it is not registered as a service.
 */
public class DefaultCamelCloudServiceTest {

    private CamelContext camelContextMock;
    private ExecutorServiceManager esmMock;
    private ExecutorService esMock;
    private ProducerTemplate producerMock;

    @Before
    public void setUp() {
        this.camelContextMock = mock(CamelContext.class);
        this.esmMock = mock(ExecutorServiceManager.class);
        this.esMock = mock(ExecutorService.class);
        this.producerMock = mock(ProducerTemplate.class);

        when(this.esmMock.newThreadPool(any(), anyString(), anyInt(), anyInt())).thenReturn(this.esMock);
        when(this.camelContextMock.getExecutorServiceManager()).thenReturn(this.esmMock);
        when(this.camelContextMock.createProducerTemplate()).thenReturn(this.producerMock);
    }

    private static Map<String, Object> props(String appId, String topic) {
        final Map<String, Object> props = new HashMap<>();
        props.put(PROP_APP_ID, appId);
        props.put(PROP_APP_TOPIC, topic);
        return props;
    }

    @Test
    public void testIsConnected() {
        DefaultCamelCloudService dccs = new DefaultCamelCloudService(this.camelContextMock);

        when(this.camelContextMock.getStatus()).thenReturn(ServiceStatus.Stopped);
        assertFalse(dccs.isConnected());

        when(this.camelContextMock.getStatus()).thenReturn(ServiceStatus.Started);
        assertTrue(dccs.isConnected());
    }

    @Test
    public void testPublishDefaultEndpoint() throws KuraException {
        DefaultCamelCloudService dccs = new DefaultCamelCloudService(this.camelContextMock);

        byte[] body = new byte[] {0x01};
        KuraPayload payload = new KuraPayload();
        payload.setBody(body);

        final Map<String, Object> props = props("appId", "topic");
        props.put(PROP_QOS, 1);
        props.put(PROP_RETAIN, true);
        props.put(PROP_PRIORITY, 3);
        props.put(PROP_CONTROL, false);

        doAnswer(invocation -> {
                    assertEquals("vm:appId:topic", invocation.getArgument(0, String.class));
                    assertArrayEquals(
                            body, invocation.getArgument(1, KuraPayload.class).getBody());

                    final Map<String, Object> headers = invocation.getArgument(2, Map.class);
                    assertFalse((boolean) headers.get(CAMEL_KURA_CLOUD_CONTROL));
                    assertNotNull(headers.get(CAMEL_KURA_CLOUD_MESSAGEID));
                    assertEquals(null, headers.get(CAMEL_KURA_CLOUD_DEVICEID));
                    assertEquals(1, headers.get(CAMEL_KURA_CLOUD_QOS));
                    assertEquals(true, headers.get(CAMEL_KURA_CLOUD_RETAIN));
                    assertEquals(3, headers.get(CAMEL_KURA_CLOUD_PRIORITY));
                    return null;
                })
                .when(this.producerMock)
                .sendBodyAndHeaders(anyString(), any(), any());

        final String id = dccs.publish(new KuraMessage(payload, props));

        assertNotNull(id);
        verify(this.producerMock).sendBodyAndHeaders(anyString(), any(), any());
    }

    @Test
    public void testPublishWithDeviceIdAndControlOnCustomEndpoint() throws KuraException {
        DefaultCamelCloudService dccs = new DefaultCamelCloudService(this.camelContextMock);
        dccs.registerBaseEndpoint("appId", "endpoint:%s");

        final Map<String, Object> props = props("appId", "topic");
        props.put(PROP_DEVICE_ID, "deviceId");
        props.put(PROP_CONTROL, true);

        doAnswer(invocation -> {
                    assertEquals("endpoint:deviceId:appId:topic", invocation.getArgument(0, String.class));

                    final Map<String, Object> headers = invocation.getArgument(2, Map.class);
                    assertTrue((boolean) headers.get(CAMEL_KURA_CLOUD_CONTROL));
                    assertEquals("deviceId", headers.get(CAMEL_KURA_CLOUD_DEVICEID));
                    // defaults
                    assertEquals(0, headers.get(CAMEL_KURA_CLOUD_QOS));
                    assertEquals(false, headers.get(CAMEL_KURA_CLOUD_RETAIN));
                    assertEquals(5, headers.get(CAMEL_KURA_CLOUD_PRIORITY));
                    return null;
                })
                .when(this.producerMock)
                .sendBodyAndHeaders(anyString(), any(), any());

        dccs.publish(new KuraMessage(new KuraPayload(), props));

        verify(this.producerMock).sendBodyAndHeaders(anyString(), any(), any());
    }

    @Test
    public void testPublishWithoutAppIdFails() {
        DefaultCamelCloudService dccs = new DefaultCamelCloudService(this.camelContextMock);

        try {
            dccs.publish(new KuraMessage(new KuraPayload(), props(null, "topic")));
            fail("Exception was expected.");
        } catch (KuraException e) {
            // expected
        }

        verify(this.producerMock, never()).sendBodyAndHeaders(anyString(), any(), any());
    }

    @Test
    public void testDeliveryListenerNotified() throws KuraException {
        DefaultCamelCloudService dccs = new DefaultCamelCloudService(this.camelContextMock);

        CloudDeliveryListener listener = mock(CloudDeliveryListener.class);
        dccs.registerCloudDeliveryListener(listener);

        final String id = dccs.publish(new KuraMessage(new KuraPayload(), props("appId", "topic")));
        verify(listener).onMessageConfirmed(id);

        dccs.unregisterCloudDeliveryListener(listener);
        dccs.publish(new KuraMessage(new KuraPayload(), props("appId", "topic")));
        verify(listener).onMessageConfirmed(id); // still only the first call
    }

    @Test
    public void testSubscribeWithoutTopicIsIgnored() throws Exception {
        DefaultCamelCloudService dccs = new DefaultCamelCloudService(this.camelContextMock);

        dccs.registerSubscriber(props("appId", null), mock(CloudSubscriberListener.class));

        verify(this.camelContextMock, never()).addStartupListener(any());
    }

    @Test
    public void testSubscribeAndUnsubscribe() throws Exception {
        DefaultCamelCloudService dccs = new DefaultCamelCloudService(this.camelContextMock);
        CloudSubscriberListener listener = mock(CloudSubscriberListener.class);

        // run submitted callables right away
        when(this.esMock.submit(any(Callable.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Callable.class).call();
            return null;
        });

        dccs.registerSubscriber(props("appId", "topic"), listener);

        // the subscription is deferred to the startup of the camel context
        ArgumentCaptor<StartupListener> startupCaptor = ArgumentCaptor.forClass(StartupListener.class);
        verify(this.camelContextMock).addStartupListener(startupCaptor.capture());
        verify(this.camelContextMock, never()).addRoutes(any());

        startupCaptor.getValue().onCamelContextStarted(this.camelContextMock, false);
        verify(this.camelContextMock).addRoutes(any(RouteBuilder.class));

        // unsubscribe removes the route
        Route routeMock = mock(Route.class);
        RouteController controllerMock = mock(RouteController.class);
        when(this.camelContextMock.getRoute(anyString())).thenReturn(routeMock);
        when(this.camelContextMock.getRouteController()).thenReturn(controllerMock);

        dccs.unregisterSubscriber(listener);

        verify(controllerMock).stopRoute(anyString());
        verify(this.camelContextMock).removeRoute(anyString());
    }

    @Test
    public void testSubscribeUnregisteredBeforeStartup() throws Exception {
        DefaultCamelCloudService dccs = new DefaultCamelCloudService(this.camelContextMock);
        CloudSubscriberListener listener = mock(CloudSubscriberListener.class);

        when(this.esMock.submit(any(Callable.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Callable.class).call();
            return null;
        });

        dccs.registerSubscriber(props("appId", "topic"), listener);
        dccs.unregisterSubscriber(listener);

        ArgumentCaptor<StartupListener> startupCaptor = ArgumentCaptor.forClass(StartupListener.class);
        verify(this.camelContextMock).addStartupListener(startupCaptor.capture());
        startupCaptor.getValue().onCamelContextStarted(this.camelContextMock, false);

        verify(this.camelContextMock, never()).addRoutes(any(RouteBuilder.class));
    }

    @Test
    public void testDispose() {
        DefaultCamelCloudService dccs = new DefaultCamelCloudService(this.camelContextMock);

        dccs.dispose();

        verify(this.esmMock).shutdown(this.esMock);
    }
}
