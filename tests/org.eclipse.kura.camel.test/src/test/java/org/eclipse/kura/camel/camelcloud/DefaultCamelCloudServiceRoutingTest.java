/*******************************************************************************
 * Copyright (c) 2026 Eurotech and/or its affiliates and others
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

import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.CAMEL_KURA_CLOUD_DEVICEID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_APP_TOPIC;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_DEVICE_ID;
import static org.eclipse.kura.camel.camelcloud.KuraCloudClientConstants.PROP_QOS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloudconnection.listener.CloudConnectionListener;
import org.eclipse.kura.cloudconnection.listener.CloudDeliveryListener;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.subscriber.listener.CloudSubscriberListener;
import org.eclipse.kura.message.KuraPayload;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultCamelCloudServiceRoutingTest {

    private static final int AWAIT_SECONDS = 10;

    private CamelContext context;
    private DefaultCamelCloudService service;

    /**
     * vm: queues are shared by the whole JVM, so every test needs its own
     * application id
     */
    private String app;

    private Collector collector;
    private CloudDeliveryListener deliveryListener;
    private String publishedId;
    private KuraMessage received;

    @Before
    public void prepareFreshServiceOnNewCamelContext() {
        this.app = "app" + UUID.randomUUID().toString().replace("-", "");
        this.context = new DefaultCamelContext();
        this.service = new DefaultCamelCloudService(this.context);
        this.collector = new Collector();
    }

    @After
    public void disposeServiceAndStopContext() {
        this.service.dispose();
        this.context.stop();
    }

    /**
     * Scenarios
     */
    @Test
    public void serviceBecomesConnectedWhenCamelContextStarts() {
        givenAStoppedContext();

        whenTheContextIsStarted();

        thenTheServiceIsConnected(true);
    }

    @Test
    public void connectAndDisconnectDoNotChangeConnectionState() {
        givenAStartedContext();

        whenConnectingAndDisconnecting();

        thenTheServiceIsConnected(true);
    }

    @Test
    public void connectionListenerCanBeUnregisteredMoreThanOnce() {
        final CloudConnectionListener listener = givenAConnectionListener();

        whenRegisteringAndUnregisteringTwice(listener);

        thenTheConnectionListenerWasNeverCalled(listener);
    }

    @Test
    public void publishedPayloadIsDeliveredToTopicSubscriber() throws InterruptedException, KuraException {
        givenAStartedContext();
        givenASubscriberOn("topic", null);

        whenPublishing("topic", null, new byte[] {1, 2, 3});

        thenAnIdIsReturned();
        thenTheSubscriberReceivedBody(new byte[] {1, 2, 3});
        thenTheReceivedTopicIs("topic");
        thenTheReceivedDeviceIdIs(null);
    }

    @Test
    public void deviceIdAndDefaultQosAreForwardedToSubscriber() throws InterruptedException, KuraException {
        givenAStartedContext();
        givenASubscriberOn("topic", "dev");

        whenPublishing("topic", "dev", new byte[] {9});

        thenTheSubscriberReceivedBody(new byte[] {9});
        thenTheReceivedDeviceIdIs("dev");
        thenTheReceivedQosIs(0);
    }

    @Test
    public void nonKuraPayloadBodyIsConvertedToBytes() throws InterruptedException {
        givenAStartedContext();
        givenASubscriberOn("raw", null);

        whenSendingPlainTextDirectlyToTheEndpoint("raw", "hello", "devX");

        thenTheSubscriberReceivedBody("hello".getBytes());
        thenTheReceivedDeviceIdIs("devX");
    }

    @Test
    public void customBaseEndpointWithPlaceholderIsUsedForRouting() throws InterruptedException, KuraException {
        givenABaseEndpoint("seda:custom-%s");
        givenAStartedContext();
        givenASubscriberOn("topic", null);

        whenPublishing("topic", null, new byte[] {7});

        thenTheSubscriberReceivedBody(new byte[] {7});
    }

    @Test
    public void customBaseEndpointWithoutPlaceholderIsUsedAsPrefix() throws InterruptedException, KuraException {
        givenABaseEndpoint("seda:prefix-");
        givenAStartedContext();
        givenASubscriberOn("topic", null);

        whenPublishing("topic", null, new byte[] {7});

        thenTheSubscriberReceivedBody(new byte[] {7});
    }

    @Test
    public void unsubscribingRemovesSubscriptionRoute() throws KuraException, InterruptedException {
        givenAStartedContext();
        givenASubscriberOn("topic", null);
        givenAMessageAlreadyDelivered("topic");

        whenUnsubscribing(this.collector);

        thenThereAreRoutes(0);
    }

    @Test
    public void unsubscribingUnknownListenerIsIgnored() {
        givenAStartedContext();

        whenUnsubscribing(new Collector());

        thenThereAreRoutes(0);
    }

    @Test
    public void unregisteredDeliveryListenerReceivesNoMoreConfirmations() throws KuraException {
        givenAStartedContext();
        givenARegisteredDeliveryListener();

        whenPublishingThenUnregisteringThenPublishingAgain();

        thenOnlyTheFirstPublicationWasConfirmed();
    }

    /**
     * Given
     */
    private void givenAStoppedContext() {
        assertFalse(this.service.isConnected());
    }

    private void givenAStartedContext() {
        this.context.start();
    }

    private CloudConnectionListener givenAConnectionListener() {
        return mock(CloudConnectionListener.class);
    }

    private void givenABaseEndpoint(final String baseEndpoint) {
        this.service.registerBaseEndpoint(this.app, baseEndpoint);
    }

    private void givenASubscriberOn(final String topic, final String deviceId) {
        this.service.registerSubscriber(subscription(topic, deviceId), this.collector);
        awaitStartedRoutes(1);
    }

    private void givenAMessageAlreadyDelivered(final String topic) throws KuraException, InterruptedException {
        whenPublishing(topic, null, new byte[] {1});
        thenTheSubscriberReceivedBody(new byte[] {1});
    }

    private void givenARegisteredDeliveryListener() {
        this.deliveryListener = mock(CloudDeliveryListener.class);
        this.service.registerCloudDeliveryListener(this.deliveryListener);
    }

    /**
     * When
     */
    private void whenTheContextIsStarted() {
        this.context.start();
    }

    private void whenConnectingAndDisconnecting() {
        // no-ops, as the connection state is bound to the context
        this.service.connect();
        this.service.disconnect();
    }

    private void whenRegisteringAndUnregisteringTwice(final CloudConnectionListener listener) {
        this.service.registerCloudConnectionListener(listener);
        this.service.unregisterCloudConnectionListener(listener);
        this.service.unregisterCloudConnectionListener(listener);
    }

    private void whenPublishing(final String topic, final String deviceId, final byte[] body) throws KuraException {
        final KuraPayload payload = new KuraPayload();
        payload.setBody(body);
        this.publishedId = this.service.publish(new KuraMessage(payload, subscription(topic, deviceId)));
    }

    private void whenSendingPlainTextDirectlyToTheEndpoint(
            final String topic, final String text, final String deviceId) {
        this.context
                .createProducerTemplate()
                .sendBodyAndHeader("vm:" + this.app + ":" + topic, text, CAMEL_KURA_CLOUD_DEVICEID, deviceId);
    }

    private void whenUnsubscribing(final CloudSubscriberListener listener) {
        this.service.unregisterSubscriber(listener);
    }

    private void whenPublishingThenUnregisteringThenPublishingAgain() throws KuraException {
        whenPublishing("t", null, new byte[0]);
        final String firstId = this.publishedId;
        this.service.unregisterCloudDeliveryListener(this.deliveryListener);
        whenPublishing("t", null, new byte[0]);
        this.publishedId = firstId;
    }

    /**
     * Then
     */
    private void thenTheServiceIsConnected(final boolean expected) {
        assertEquals(expected, this.service.isConnected());
    }

    private void thenTheConnectionListenerWasNeverCalled(final CloudConnectionListener listener) {
        verifyNoInteractions(listener);
    }

    private void thenAnIdIsReturned() {
        assertNotNull(this.publishedId);
    }

    private void thenTheSubscriberReceivedBody(final byte[] expected) throws InterruptedException {
        this.received = this.collector.messages.poll(AWAIT_SECONDS, TimeUnit.SECONDS);
        assertNotNull("message must be delivered to the subscriber", this.received);
        assertArrayEquals(expected, this.received.getPayload().getBody());
    }

    private void thenTheReceivedTopicIs(final String expected) {
        assertEquals(expected, this.received.getProperties().get("appTopic"));
    }

    private void thenTheReceivedDeviceIdIs(final String expected) {
        assertEquals(expected, this.received.getProperties().get(PROP_DEVICE_ID));
    }

    private void thenTheReceivedQosIs(final int expected) {
        assertEquals(expected, this.received.getProperties().get(PROP_QOS));
    }

    private void thenThereAreRoutes(final int expected) {
        assertEquals(expected, this.context.getRoutes().size());
    }

    private void thenOnlyTheFirstPublicationWasConfirmed() {
        verify(this.deliveryListener, times(1)).onMessageConfirmed(this.publishedId);
        verifyNoMoreInteractions(this.deliveryListener);
    }

    /**
     * Utilities
     */
    private Map<String, Object> subscription(final String topic, final String deviceId) {
        final Map<String, Object> props = new HashMap<>();
        props.put(PROP_APP_ID, this.app);
        props.put(PROP_APP_TOPIC, topic);
        if (deviceId != null) {
            props.put(PROP_DEVICE_ID, deviceId);
        }
        return props;
    }

    /**
     * The subscription route is created, and started, asynchronously once the
     * context is up.
     */
    private void awaitStartedRoutes(final int count) {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(AWAIT_SECONDS);
        while (!routesStarted(count)) {
            assertTrue("timeout waiting for " + count + " started route(s)", System.nanoTime() < deadline);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean routesStarted(final int count) {
        final List<Route> routes = this.context.getRoutes();
        if (routes.size() != count) {
            return false;
        }
        return routes.stream()
                .allMatch(r -> this.context.getRouteController().getRouteStatus(r.getId()) == ServiceStatus.Started);
    }

    private static class Collector implements CloudSubscriberListener {

        final BlockingQueue<KuraMessage> messages = new LinkedBlockingQueue<>();

        @Override
        public void onMessageArrived(final KuraMessage message) {
            this.messages.add(message);
        }
    }
}
