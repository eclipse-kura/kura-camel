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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ServiceConfigurationTest {

    private ServiceConfiguration configuration;
    private ServiceConfiguration other;

    /**
     * Scenarios
     */
    @Test
    public void newConfigurationHasNoRoutesAndJmxDisabled() {
        givenANewConfiguration();

        whenReadingTheDefaults();

        thenXmlIs(null);
        thenJmxIs(false);
    }

    @Test
    public void configurationKeepsTheValuesSet() {
        givenANewConfiguration();

        whenSettingXmlAndJmx("<routes/>", true);

        thenXmlIs("<routes/>");
        thenJmxIs(true);
    }

    @Test
    public void configurationWithoutRoutesIsInvalid() {
        givenAConfigurationWith(null, false);
        thenItIsInvalid();

        givenAConfigurationWith("", false);
        thenItIsInvalid();

        givenAConfigurationWith("   \n", false);
        thenItIsInvalid();
    }

    @Test
    public void configurationWithRoutesIsValid() {
        givenAConfigurationWith("<routes/>", false);

        thenItIsValid();
    }

    @Test
    public void configurationsWithSameValuesAreEqual() {
        givenAConfigurationWith("<routes/>", true);
        givenAnotherConfigurationWith("<routes/>", true);

        thenTheyAreEqual();
        thenItIsEqualToItself();
    }

    @Test
    public void configurationsBothWithoutRoutesAreEqual() {
        givenAConfigurationWith(null, true);
        givenAnotherConfigurationWith(null, true);

        thenTheyAreEqual();
    }

    @Test
    public void configurationsWithDifferentValuesAreNotEqual() {
        givenAConfigurationWith("<routes/>", true);

        thenItDiffersFromANull();
        thenItDiffersFromAConfigurationWith("<routes/>", false);
        thenItDiffersFromAConfigurationWith("<other/>", true);
        thenItDiffersFromAConfigurationWith(null, true);
    }

    @Test
    public void configurationWithoutRoutesDiffersFromOneWithRoutes() {
        givenAConfigurationWith(null, true);
        givenAnotherConfigurationWith("<routes/>", true);

        thenTheyAreNotEqual();
    }

    @Test
    public void equalConfigurationsShareHashCode() {
        givenAConfigurationWith("<routes/>", true);
        givenAnotherConfigurationWith("<routes/>", true);

        thenTheHashCodesMatch();
    }

    @Test
    public void jmxSettingAffectsHashCode() {
        givenAConfigurationWith("<routes/>", true);
        givenAnotherConfigurationWith("<routes/>", false);

        thenTheHashCodesDiffer();
    }

    /**
     * Given
     */
    private void givenANewConfiguration() {
        this.configuration = new ServiceConfiguration();
    }

    private void givenAConfigurationWith(final String xml, final boolean jmx) {
        this.configuration = configurationWith(xml, jmx);
    }

    private void givenAnotherConfigurationWith(final String xml, final boolean jmx) {
        this.other = configurationWith(xml, jmx);
    }

    /**
     * When
     */
    private void whenReadingTheDefaults() {
        // nothing to do: the defaults are inspected by the Then steps
    }

    private void whenSettingXmlAndJmx(final String xml, final boolean jmx) {
        this.configuration.setXml(xml);
        this.configuration.setEnableJmx(jmx);
    }

    /**
     * Then
     */
    private void thenXmlIs(final String expected) {
        if (expected == null) {
            assertNull(this.configuration.getXml());
        } else {
            assertEquals(expected, this.configuration.getXml());
        }
    }

    private void thenJmxIs(final boolean expected) {
        assertEquals(expected, this.configuration.isEnableJmx());
    }

    private void thenItIsValid() {
        assertTrue(this.configuration.isValid());
    }

    private void thenItIsInvalid() {
        assertFalse(this.configuration.isValid());
    }

    private void thenTheyAreEqual() {
        assertEquals(this.configuration, this.other);
    }

    private void thenTheyAreNotEqual() {
        assertNotEquals(this.configuration, this.other);
        assertNotEquals(this.other, this.configuration);
    }

    private void thenItIsEqualToItself() {
        assertEquals(this.configuration, this.configuration);
    }

    private void thenItDiffersFromANull() {
        assertNotEquals(this.configuration, null);
    }

    private void thenItDiffersFromAConfigurationWith(final String xml, final boolean jmx) {
        assertNotEquals(this.configuration, configurationWith(xml, jmx));
    }

    private void thenTheHashCodesMatch() {
        assertEquals(this.configuration.hashCode(), this.other.hashCode());
    }

    private void thenTheHashCodesDiffer() {
        assertNotEquals(this.configuration.hashCode(), this.other.hashCode());
    }

    /**
     * Utilities
     */
    private static ServiceConfiguration configurationWith(final String xml, final boolean jmx) {
        final ServiceConfiguration c = new ServiceConfiguration();
        c.setXml(xml);
        c.setEnableJmx(jmx);
        return c;
    }
}
