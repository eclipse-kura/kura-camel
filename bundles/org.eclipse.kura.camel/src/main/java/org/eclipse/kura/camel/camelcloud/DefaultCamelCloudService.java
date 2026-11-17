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

import static java.util.Objects.requireNonNull;
import static org.apache.camel.ServiceStatus.Started;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.IntSupplier;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.StartupListener;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.engine.DefaultShutdownStrategy;
import org.apache.camel.spi.ShutdownStrategy;
import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloudconnection.listener.CloudConnectionListener;
import org.eclipse.kura.cloudconnection.listener.CloudDeliveryListener;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.subscriber.listener.CloudSubscriberListener;
import org.eclipse.kura.message.KuraPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default implementation of the {@link CamelCloudService}
 * <p>
 * Published messages are sent to a Camel endpoint (by default {@code vm:<applicationId>:<topic>}), and
 * subscriptions are implemented by adding a Camel route consuming from that same endpoint.
 * </p>
 * <p>
 * The message properties understood by {@link #publish(KuraMessage)} and
 * {@link #registerSubscriber(Map, CloudSubscriberListener)} are the ones used by the Kura cloud connections
 * (see the {@code PROP_*} constants of {@link KuraCloudClientConstants}): application ID, application topic, QoS,
 * retain, priority and control, plus the optional device ID.
 * </p>
 */
public class DefaultCamelCloudService implements CamelCloudService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCamelCloudService.class);

    private static final int DEFAULT_PRIORITY = 5;

    private final CamelContext camelContext;
    private final ProducerTemplate producerTemplate;
    private final ExecutorService executorService;
    private final Map<String, String> baseEndpoints = new ConcurrentHashMap<>();
    private final Map<CloudSubscriberListener, Map<String, Object>> subscriptions = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<CloudConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<CloudDeliveryListener> deliveryListeners = new CopyOnWriteArrayList<>();
    private final Random messageIdRandom = new Random();
    private final IntSupplier messageIdGenerator = () -> Math.abs(this.messageIdRandom.nextInt());

    public DefaultCamelCloudService(CamelContext camelContext) {
        this.camelContext = camelContext;
        this.producerTemplate = camelContext.createProducerTemplate();
        this.executorService = camelContext.getExecutorServiceManager().newThreadPool(this, "CamelCloudService", 0, 1);
    }

    // Cloud connection manager API

    @Override
    public void connect() {
        // the connection state is bound to the lifecycle of the Camel context
    }

    @Override
    public void disconnect() {
        // the connection state is bound to the lifecycle of the Camel context
    }

    @Override
    public boolean isConnected() {
        return this.camelContext.getStatus() == Started;
    }

    @Override
    public void registerCloudConnectionListener(CloudConnectionListener cloudConnectionListener) {
        this.connectionListeners.add(cloudConnectionListener);
    }

    @Override
    public void unregisterCloudConnectionListener(CloudConnectionListener cloudConnectionListener) {
        this.connectionListeners.remove(cloudConnectionListener);
    }

    // Cloud endpoint API

    @Override
    public String publish(KuraMessage message) throws KuraException {
        requireNonNull(message, "'message' must not be null");

        final Map<String, Object> props = message.getProperties();

        final String applicationId = asString(props.get(PROP_APP_ID));
        final String topic = asString(props.get(PROP_APP_TOPIC));
        final String deviceId = asString(props.get(PROP_DEVICE_ID));

        if (applicationId == null || topic == null) {
            throw new KuraException(
                    KuraErrorCode.INVALID_PARAMETER, "'" + PROP_APP_ID + "' and '" + PROP_APP_TOPIC + "' are required");
        }

        final String target = target(applicationId, buildTopicName(applicationId, deviceId, topic));
        final int messageId = this.messageIdGenerator.getAsInt();

        final Map<String, Object> headers = new HashMap<>();
        headers.put(CAMEL_KURA_CLOUD_CONTROL, asBoolean(props.get(PROP_CONTROL), false));
        headers.put(CAMEL_KURA_CLOUD_MESSAGEID, messageId);
        headers.put(CAMEL_KURA_CLOUD_DEVICEID, deviceId);
        headers.put(CAMEL_KURA_CLOUD_QOS, asInt(props.get(PROP_QOS), 0));
        headers.put(CAMEL_KURA_CLOUD_RETAIN, asBoolean(props.get(PROP_RETAIN), false));
        headers.put(CAMEL_KURA_CLOUD_PRIORITY, asInt(props.get(PROP_PRIORITY), DEFAULT_PRIORITY));

        logger.trace("Publishing: {} -> {} / {}", target, message.getPayload(), this.camelContext);

        this.producerTemplate.sendBodyAndHeaders(target, message.getPayload(), headers);

        final String id = String.valueOf(messageId);
        for (final CloudDeliveryListener listener : this.deliveryListeners) {
            listener.onMessageConfirmed(id);
        }
        return id;
    }

    @Override
    public void registerSubscriber(
            Map<String, Object> subscriptionProperties, CloudSubscriberListener cloudSubscriberListener) {
        requireNonNull(subscriptionProperties, "'subscriptionProperties' must not be null");
        requireNonNull(cloudSubscriberListener, "'cloudSubscriberListener' must not be null");

        final String applicationId = asString(subscriptionProperties.get(PROP_APP_ID));
        final String topic = asString(subscriptionProperties.get(PROP_APP_TOPIC));
        final String deviceId = asString(subscriptionProperties.get(PROP_DEVICE_ID));

        if (applicationId == null || topic == null) {
            logger.warn("Ignoring subscription without '{}' or '{}'", PROP_APP_ID, PROP_APP_TOPIC);
            return;
        }

        this.subscriptions.put(cloudSubscriberListener, subscriptionProperties);

        forkSubscribe(applicationId, deviceId, topic, cloudSubscriberListener);
    }

    @Override
    public void unregisterSubscriber(CloudSubscriberListener cloudSubscriberListener) {
        final Map<String, Object> props = this.subscriptions.remove(cloudSubscriberListener);
        if (props == null) {
            return;
        }

        doUnsubscribe(
                asString(props.get(PROP_APP_ID)),
                asString(props.get(PROP_DEVICE_ID)),
                asString(props.get(PROP_APP_TOPIC)),
                cloudSubscriberListener);
    }

    @Override
    public void registerCloudDeliveryListener(CloudDeliveryListener cloudDeliveryListener) {
        this.deliveryListeners.add(cloudDeliveryListener);
    }

    @Override
    public void unregisterCloudDeliveryListener(CloudDeliveryListener cloudDeliveryListener) {
        this.deliveryListeners.remove(cloudDeliveryListener);
    }

    // Camel specific API

    @Override
    public void registerBaseEndpoint(String applicationId, String baseEndpoint) {
        this.baseEndpoints.put(applicationId, baseEndpoint);
    }

    public void dispose() {
        this.subscriptions.clear();
        this.camelContext.getExecutorServiceManager().shutdown(this.executorService);
    }

    // Helpers

    private static String routeId(String applicationId, String deviceId, String topic, Object listener) {
        return buildTopicName(applicationId, deviceId, topic) + "@" + System.identityHashCode(listener);
    }

    private static String buildTopicName(final String applicationId, final String deviceId, final String topic) {
        requireNonNull(topic, "'topic' must not be null");
        if (deviceId == null) {
            return String.format("%s:%s", applicationId, topic);
        } else {
            return String.format("%s:%s:%s", deviceId, applicationId, topic);
        }
    }

    private void doUnsubscribe(
            final String applicationId, final String deviceId, final String topic, final CloudSubscriberListener l) {
        final String routeId = routeId(applicationId, deviceId, topic, l);
        try {
            ShutdownStrategy strategy = this.camelContext.getShutdownStrategy();
            if (strategy instanceof DefaultShutdownStrategy
                    && ((DefaultShutdownStrategy) strategy).getCurrentShutdownTaskFuture() != null) {
                logger.info("Skipping cleanup of '{}' since the camel context is being shut down", routeId);
                // we are "in shutdown" and would deadlock
                return;
            }
            if (this.camelContext.getRoute(routeId) != null) {
                this.camelContext.getRouteController().stopRoute(routeId);
                this.camelContext.removeRoute(routeId);
            }
        } catch (Exception e) {
            logger.warn("Failed to remove subscription '{}'", routeId, e);
        }
    }

    private void forkSubscribe(
            final String applicationId,
            final String deviceId,
            final String topic,
            final CloudSubscriberListener listener) {
        /*
         * This construct is needed due to CAMEL-10206
         *
         * It does fork off the subscription process, which actually creates a
         * new camel route, into the background since we currently may be in the
         * process of starting the camel context. If that is the case then the
         * newly added route won't be started since the camel context is in the
         * "starting" mode. Events won't get processed.
         *
         * So we do fork off the subscription process after the camel context
         * has been started. The executor is needed since, according to the
         * camel javadoc on StartupListener, the camel context may still be in
         * "starting" mode when the "onCamelContextStarted" method is called.
         */
        try {
            this.camelContext.addStartupListener(new StartupListener() {
                @Override
                public void onCamelContextStarted(CamelContext context, boolean alreadyStarted) throws Exception {
                    DefaultCamelCloudService.this.executorService.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            doSubscribe(applicationId, deviceId, topic, listener);
                            return null;
                        }
                    });
                }
            });
        } catch (Exception e) {
            logger.warn("Failed to register subscription for '{}'", buildTopicName(applicationId, deviceId, topic), e);
        }
    }

    private void doSubscribe(
            final String applicationId,
            final String deviceId,
            final String topic,
            final CloudSubscriberListener listener) {
        if (!this.subscriptions.containsKey(listener)) {
            // unregistered in the meantime
            return;
        }

        final String internalQueue = buildTopicName(applicationId, deviceId, topic);
        final String routeId = routeId(applicationId, deviceId, topic, listener);
        logger.debug("About to subscribe to '{}' / {}", target(applicationId, internalQueue), this.camelContext);

        try {
            this.camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(target(applicationId, internalQueue)).routeId(routeId).process(exchange -> {
                        logger.debug("Processing: {}", exchange);

                        final Object body = exchange.getIn().getBody();
                        final KuraPayload payload;
                        if (body instanceof KuraPayload) {
                            payload = (KuraPayload) body;
                        } else {
                            payload = new KuraPayload();
                            payload.setBody(getContext().getTypeConverter().convertTo(byte[].class, body));
                        }

                        final Map<String, Object> props = new HashMap<>();
                        props.put(PROP_DEVICE_ID, exchange.getIn().getHeader(CAMEL_KURA_CLOUD_DEVICEID, String.class));
                        props.put("appTopic", topic);
                        props.put(PROP_QOS, exchange.getIn().getHeader(CAMEL_KURA_CLOUD_QOS, 0, int.class));
                        props.put(
                                PROP_RETAIN, exchange.getIn().getHeader(CAMEL_KURA_CLOUD_RETAIN, true, boolean.class));
                        props.put(
                                PROP_CONTROL,
                                exchange.getIn().getHeader(CAMEL_KURA_CLOUD_CONTROL, false, boolean.class));

                        listener.onMessageArrived(new KuraMessage(payload, props));
                    });
                }
            });
        } catch (Exception e) {
            logger.warn("Error while adding subscription route '{}'", routeId, e);
        }
    }

    private String target(final String applicationId, final String topic) {
        final String baseEndpoint = this.baseEndpoints.getOrDefault(applicationId, "vm:%s");
        if (baseEndpoint.contains("%s")) {
            return String.format(baseEndpoint, topic);
        }
        return baseEndpoint + topic;
    }

    private static String asString(final Object value) {
        return value == null ? null : value.toString();
    }

    private static int asInt(final Object value, final int defaultValue) {
        return value instanceof Number ? ((Number) value).intValue() : defaultValue;
    }

    private static boolean asBoolean(final Object value, final boolean defaultValue) {
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }
}
