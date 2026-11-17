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

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultEndpoint;
import org.eclipse.kura.cloudconnection.CloudEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint implementation for {@link KuraCloudComponent}
 */
@UriEndpoint(
        scheme = "kura-cloud",
        title = "Kura Cloud",
        category = {Category.IOT, Category.CLOUD},
        syntax = "kura-cloud:applicationId/appTopic")
public class KuraCloudEndpoint extends DefaultEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(KuraCloudEndpoint.class);

    @UriParam(defaultValue = "")
    private String applicationId = "";

    @UriParam(defaultValue = "")
    private String topic = "";

    @UriParam(defaultValue = "0")
    private int qos;

    @UriParam(defaultValue = "false")
    private boolean retain = false;

    @UriParam(defaultValue = "5")
    private int priority = 5;

    @UriParam(defaultValue = "false")
    private boolean control = false;

    @UriParam(defaultValue = "")
    private String deviceId;

    private CloudEndpoint cloudEndpoint;

    public KuraCloudEndpoint(String uri, KuraCloudComponent kuraCloudComponent) {
        super(uri, kuraCloudComponent);
    }

    public KuraCloudEndpoint(String uri, KuraCloudComponent kuraCloudComponent, CloudEndpoint cloudEndpoint) {
        super(uri, kuraCloudComponent);
        this.cloudEndpoint = cloudEndpoint;
    }

    @Override
    protected void doStart() throws Exception {
        synchronized (this) {
            if (this.cloudEndpoint == null) {
                this.cloudEndpoint = getComponent().getCloudEndpoint();
            }
            logger.debug("CloudEndpoint {} -> {}", this.applicationId, this.cloudEndpoint);
        }
        super.doStart();
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new KuraCloudConsumer(this, processor, getCloudEndpoint());
    }

    @Override
    public KuraCloudProducer createProducer() throws Exception {
        return new KuraCloudProducer(this, getCloudEndpoint());
    }

    private synchronized CloudEndpoint getCloudEndpoint() {
        if (this.cloudEndpoint == null) {
            this.cloudEndpoint = getComponent().getCloudEndpoint();
        }
        return this.cloudEndpoint;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public KuraCloudComponent getComponent() {
        return (KuraCloudComponent) super.getComponent();
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQos() {
        return this.qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public boolean isRetain() {
        return this.retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isControl() {
        return this.control;
    }

    public void setControl(boolean control) {
        this.control = control;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
