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
package org.eclipse.kura.camel.cloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.kura.cloudconnection.CloudEndpoint;
import org.junit.Test;

public class KuraCloudEndpointTest {

    private static final String URI = "kura-cloud:app/topic";

    private KuraCloudComponent component;
    private KuraCloudEndpoint endpoint;
    private Object producer;
    private Object secondProducer;
    private Object consumer;

    /**
     * Scenarios
     */
    @Test
    public void endpointOptionsHaveDocumentedDefaults() {
        givenAnEndpointWithoutCloudEndpoint(false);

        thenTheOptionsHaveTheirDefaultValues();
    }

    @Test
    public void endpointOptionsKeepTheValuesSet() {
        givenAnEndpointWithoutCloudEndpoint(false);

        whenSettingAllOptions();

        thenTheOptionsReturnTheSetValues();
    }

    @Test
    public void endpointExposesOwningComponent() {
        givenAnEndpointWithoutCloudEndpoint(false);

        thenTheEndpointBelongsToItsComponent();
    }

    @Test
    public void injectedCloudEndpointIsUsedForProducerAndConsumer() throws Exception {
        givenAnEndpointWithInjectedCloudEndpoint();

        whenCreatingAProducerAndAConsumer();

        thenBothWereCreated();
        thenTheComponentWasAskedForTheCloudEndpoint(0);
    }

    @Test
    public void cloudEndpointLookedUpFromComponentIsCached() throws Exception {
        givenAnEndpointWithoutCloudEndpoint(true);

        whenCreatingTwoProducers();

        thenBothProducersWereCreated();
        thenTheComponentWasAskedForTheCloudEndpoint(1);
    }

    @Test
    public void startingEndpointResolvesCloudEndpointFromComponent() throws Exception {
        givenAnEndpointWithoutCloudEndpoint(false);
        givenTheComponentProvidesACloudEndpoint();

        whenStartingTheEndpoint();

        thenTheComponentWasAskedForTheCloudEndpoint(1);
    }

    @Test
    public void startingEndpointKeepsInjectedCloudEndpoint() throws Exception {
        givenAnEndpointWithInjectedCloudEndpoint();

        whenStartingTheEndpoint();

        thenTheComponentWasAskedForTheCloudEndpoint(0);
    }

    /**
     * Given
     */
    private void givenAnEndpointWithoutCloudEndpoint(final boolean providedByComponent) {
        this.component = mock(KuraCloudComponent.class);
        if (providedByComponent) {
            when(this.component.getCamelContext()).thenReturn(new DefaultCamelContext());
            givenTheComponentProvidesACloudEndpoint();
        }
        this.endpoint = new KuraCloudEndpoint(URI, this.component);
    }

    private void givenAnEndpointWithInjectedCloudEndpoint() {
        this.component = mock(KuraCloudComponent.class);
        when(this.component.getCamelContext()).thenReturn(new DefaultCamelContext());
        this.endpoint = new KuraCloudEndpoint(URI, this.component, mock(CloudEndpoint.class));
    }

    private void givenTheComponentProvidesACloudEndpoint() {
        when(this.component.getCloudEndpoint()).thenReturn(mock(CloudEndpoint.class));
    }

    /**
     * When
     */
    private void whenSettingAllOptions() {
        this.endpoint.setApplicationId("a");
        this.endpoint.setTopic("t");
        this.endpoint.setQos(2);
        this.endpoint.setRetain(true);
        this.endpoint.setPriority(1);
        this.endpoint.setControl(true);
        this.endpoint.setDeviceId("d");
    }

    private void whenCreatingAProducerAndAConsumer() throws Exception {
        this.producer = this.endpoint.createProducer();
        this.consumer = this.endpoint.createConsumer(mock(Processor.class));
    }

    private void whenCreatingTwoProducers() throws Exception {
        this.producer = this.endpoint.createProducer();
        this.secondProducer = this.endpoint.createProducer();
    }

    private void whenStartingTheEndpoint() throws Exception {
        this.endpoint.doStart();
    }

    /**
     * Then
     */
    private void thenTheOptionsHaveTheirDefaultValues() {
        assertEquals("", this.endpoint.getApplicationId());
        assertEquals("", this.endpoint.getTopic());
        assertEquals(0, this.endpoint.getQos());
        assertFalse(this.endpoint.isRetain());
        assertEquals(5, this.endpoint.getPriority());
        assertFalse(this.endpoint.isControl());
        assertNull(this.endpoint.getDeviceId());
        assertTrue(this.endpoint.isSingleton());
    }

    private void thenTheOptionsReturnTheSetValues() {
        assertEquals("a", this.endpoint.getApplicationId());
        assertEquals("t", this.endpoint.getTopic());
        assertEquals(2, this.endpoint.getQos());
        assertTrue(this.endpoint.isRetain());
        assertEquals(1, this.endpoint.getPriority());
        assertTrue(this.endpoint.isControl());
        assertEquals("d", this.endpoint.getDeviceId());
    }

    private void thenTheEndpointBelongsToItsComponent() {
        assertSame(this.component, this.endpoint.getComponent());
    }

    private void thenBothWereCreated() {
        assertNotNull(this.producer);
        assertNotNull(this.consumer);
    }

    private void thenBothProducersWereCreated() {
        assertNotNull(this.producer);
        assertNotNull(this.secondProducer);
    }

    private void thenTheComponentWasAskedForTheCloudEndpoint(final int times) {
        verify(this.component, times(times)).getCloudEndpoint();
    }
}
