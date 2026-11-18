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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Test;
import org.osgi.framework.BundleContext;

public class XmlCamelCloudServiceTest {

    private BundleContext context;
    private XmlCamelCloudService service;

    /**
     * Scenarios
     */
    @Test
    public void stoppingNeverStartedServiceIsHarmless() throws Exception {
        givenAServiceThatWasNeverStarted();

        whenStoppingItTwice();

        thenTheBundleContextWasNotTouched();
    }

    /**
     * Given
     */
    private void givenAServiceThatWasNeverStarted() {
        this.context = mock(BundleContext.class);
        final ServiceConfiguration configuration = new ServiceConfiguration();
        configuration.setXml("<routes/>");
        this.service = new XmlCamelCloudService(this.context, "pid", configuration);
    }

    /**
     * Given
     */
    private void whenStoppingItTwice() throws Exception {
        this.service.stop();
        this.service.stop();
    }

    /**
     * Then
     */
    private void thenTheBundleContextWasNotTouched() {
        verifyNoInteractions(this.context);
    }
}
