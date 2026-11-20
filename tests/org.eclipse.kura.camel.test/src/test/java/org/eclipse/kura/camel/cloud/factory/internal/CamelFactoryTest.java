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
import static org.mockito.Mockito.mockStatic;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.osgi.framework.FrameworkUtil;

public class CamelFactoryTest {

    private CamelFactory factory;
    private MockedStatic<FrameworkUtil> framework;
    private Map<String, Object> properties;

    @Before
    public void prepareFactoryAndFrameworkMock() {
        this.factory = new CamelFactory();
        this.framework = mockStatic(FrameworkUtil.class);
    }

    @After
    public void releaseFrameworkMock() {
        this.framework.close();
    }

    /**
     * Sceanrios
     */
    @Test
    public void factoryIdMatchesComponentName() {
        thenTheFactoryIdIs("org.eclipse.kura.camel.cloud.factory.CamelFactory");
    }

    @Test
    public void configurationWithoutCloudServicePidCreatesNoService() throws Exception {
        givenPropertiesWith(null, "<routes/>");

        whenActivatingModifyingAndDeactivating();

        thenTheBundleWasNeverLookedUp();
    }

    @Test
    public void configurationWithoutRoutesCreatesNoService() throws Exception {
        givenPropertiesWith("pid", null);

        whenActivatingModifyingAndDeactivating();

        thenTheBundleWasNeverLookedUp();
    }

    @Test
    public void unchangedBlankRoutesConfigurationCreatesNoService() throws Exception {
        givenPropertiesWith("pid", "   ");

        whenActivatingModifyingAndDeactivating();

        thenTheBundleWasNeverLookedUp();
    }

    @Test
    public void deactivatingInactiveFactoryIsHarmless() {
        whenDeactivating();

        thenTheBundleWasNeverLookedUp();
    }

    /**
     * Given
     */
    private void givenPropertiesWith(final String pid, final String xml) {
        this.properties = new HashMap<>();
        if (pid != null) {
            this.properties.put("cloud.service.pid", pid);
        }
        if (xml != null) {
            this.properties.put("xml", xml);
        }
    }

    /**
     * When
     */
    private void whenActivatingModifyingAndDeactivating() throws Exception {
        this.factory.activate(this.properties);
        this.factory.modified(this.properties);
        this.factory.deactivate();
    }

    private void whenDeactivating() {
        this.factory.deactivate();
    }

    /**
     * Then
     */
    private void thenTheFactoryIdIs(final String currentValue) {
        assertEquals(CamelFactory.FACTORY_ID, currentValue);
    }

    private void thenTheBundleWasNeverLookedUp() {
        this.framework.verifyNoInteractions();
    }
}
