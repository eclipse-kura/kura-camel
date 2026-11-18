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
package org.eclipse.kura.camel.cloud.factory.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloudconnection.factory.CloudConnectionFactory;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.configuration.metatype.OCD;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CamelCloudServiceFactoryTest {

    private ConfigurationService configurationService;
    private CamelCloudServiceFactory factory;

    private String factoryPid;
    private List<String> stackPids;
    private Set<String> managedPids;

    @Before
    public void prepareFactoryBackedByConfigurationService() {
        this.configurationService = mock(ConfigurationService.class);
        this.factory = new CamelCloudServiceFactory();
        this.factory.setConfigurationService(this.configurationService);
    }

    /**
     * Scenarios
     */
    @Test
    public void factoryPidIsCamelFactoryId() {
        whenAskingForTheFactoryPid();

        thenTheFactoryPidIs(CamelFactory.FACTORY_ID);
    }

    @Test
    public void newConnectionGetsDefaultRoutesAndFactoryMarkers() throws KuraException {
        whenCreatingAConfigurationFor("myPid");

        final Map<String, Object> created = thenAFactoryConfigurationWasCreatedFor("myPid-CloudFactory");
        thenTheXmlIsTheDefaultOne(created);
        thenThePropertyIs(created, "cloud.service.pid", "myPid");
        thenThePropertyIs(
                created, CloudConnectionFactory.KURA_CLOUD_CONNECTION_FACTORY_PID, CamelCloudServiceFactory.PID);
        thenThereIsNoProperty(created, "serviceRanking");
    }

    @Test
    public void providedRoutesAndServiceRankingAreStored() throws KuraException {
        final Map<String, Object> input = givenInputWith("xml", "<routes/>", "serviceRanking", 5);

        whenAddingAConfiguration("p", input);

        final Map<String, Object> created = thenAFactoryConfigurationWasCreatedFor("p-CloudFactory");
        thenThePropertyIs(created, "xml", "<routes/>");
        thenThePropertyIs(created, "serviceRanking", 5);
    }

    @Test
    public void blankRoutesAreReplacedWithEmptyRoutesDocument() throws KuraException {
        final Map<String, Object> input = givenInputWith("xml", "   ", null, null);

        whenAddingAConfiguration("p", input);

        thenTheXmlIsTheDefaultOne(thenAFactoryConfigurationWasCreatedFor("p-CloudFactory"));
    }

    @Test(expected = NullPointerException.class)
    public void addingConfigurationWithoutPidIsRejected() throws KuraException {
        whenAddingAConfiguration(null, Collections.<String, Object>emptyMap());
    }

    @Test
    public void deletingConnectionDeletesItsCamelFactoryConfiguration() throws KuraException {
        whenDeletingTheConfigurationOf("myPid");

        thenTheFactoryConfigurationWasDeleted("myPid-CloudFactory");
    }

    @Test
    public void connectionStackContainsOnlyCamelFactoryComponent() throws KuraException {
        whenGettingTheStackComponentsOf("myPid");

        thenTheStackPidsAre(Collections.singletonList("myPid-CloudFactory"));
    }

    @Test
    public void managedConnectionsAreOnlyThoseOfCamelFactory() throws KuraException {
        givenTheseConfigurations(
                componentConfiguration("a-CloudFactory", CamelFactory.FACTORY_ID),
                componentConfiguration("b", CamelFactory.FACTORY_ID),
                componentConfiguration("c-CloudFactory", "some.other.factory"),
                componentConfiguration("d-CloudFactory", null));

        whenGettingTheManagedPids();

        thenTheManagedPidsAre("a", "b");
    }

    /**
     * Given
     */
    private Map<String, Object> givenInputWith(
            final String key1, final Object value1, final String key2, final Object value2) {
        final Map<String, Object> input = new HashMap<>();
        input.put(key1, value1);
        if (key2 != null) {
            input.put(key2, value2);
        }
        return input;
    }

    private void givenTheseConfigurations(final ComponentConfiguration... configurations) throws KuraException {
        when(this.configurationService.getComponentConfigurations()).thenReturn(Arrays.asList(configurations));
    }

    /**
     * When
     */
    private void whenAskingForTheFactoryPid() {
        this.factoryPid = this.factory.getFactoryPid();
    }

    private void whenCreatingAConfigurationFor(final String pid) throws KuraException {
        this.factory.createConfiguration(pid);
    }

    private void whenAddingAConfiguration(final String pid, final Map<String, Object> input) throws KuraException {
        this.factory.add(pid, input);
    }

    private void whenDeletingTheConfigurationOf(final String pid) throws KuraException {
        this.factory.deleteConfiguration(pid);
    }

    private void whenGettingTheStackComponentsOf(final String pid) throws KuraException {
        this.stackPids = this.factory.getStackComponentsPids(pid);
    }

    private void whenGettingTheManagedPids() throws KuraException {
        this.managedPids = this.factory.getManagedCloudConnectionPids();
    }

    /**
     * Then
     */
    private void thenTheFactoryPidIs(final String expected) {
        assertEquals(expected, this.factoryPid);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> thenAFactoryConfigurationWasCreatedFor(final String internalPid) throws KuraException {
        final ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(this.configurationService)
                .createFactoryConfiguration(eq(CamelFactory.FACTORY_ID), eq(internalPid), captor.capture(), eq(true));
        return captor.getValue();
    }

    private void thenTheXmlIsTheDefaultOne(final Map<String, Object> created) {
        assertNotNull(created.get("xml"));
        assertEquals(
                "<routes xmlns=\"http://camel.apache.org/schema/spring\"></routes>",
                created.get("xml").toString());
    }

    private void thenThePropertyIs(final Map<String, Object> created, final String key, final Object expected) {
        assertEquals(expected, created.get(key));
    }

    private void thenThereIsNoProperty(final Map<String, Object> created, final String key) {
        assertFalse(created.containsKey(key));
    }

    private void thenTheFactoryConfigurationWasDeleted(final String internalPid) throws KuraException {
        verify(this.configurationService).deleteFactoryConfiguration(internalPid, true);
    }

    private void thenTheStackPidsAre(final List<String> expected) {
        assertEquals(expected, this.stackPids);
    }

    private void thenTheManagedPidsAre(final String... expected) {
        assertEquals(expected.length, this.managedPids.size());
        for (final String pid : expected) {
            assertTrue(this.managedPids.contains(pid));
        }
    }

    /**
     * Utilities
     */
    private static ComponentConfiguration componentConfiguration(final String pid, final String ocdId) {
        final ComponentConfiguration cc = mock(ComponentConfiguration.class);
        when(cc.getPid()).thenReturn(pid);
        if (ocdId != null) {
            final OCD ocd = mock(OCD.class);
            when(ocd.getId()).thenReturn(ocdId);
            when(cc.getDefinition()).thenReturn(ocd);
        }
        return cc;
    }
}
