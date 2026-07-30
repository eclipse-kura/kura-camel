/*******************************************************************************
 * Copyright (c) 2016, 2026 Red Hat Inc and others
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
package org.eclipse.kura.camel.test.component;

import java.util.Map;

import org.eclipse.kura.camel.component.AbstractXmlCamelComponent;
import org.osgi.framework.BundleContext;

/**
 * A concrete {@link AbstractXmlCamelComponent} that widens the lifecycle methods to public so that
 * the tests can drive them.
 * <p>
 * These tests used to be an OSGi fragment of {@code org.eclipse.kura.camel} and lived in the very
 * package they exercise, which is how they reached the {@code protected} lifecycle. As a standalone
 * test bundle that is no longer possible — sharing a package with an exported package of the host
 * bundle is a split package, and the imported wiring would win for the whole package at runtime.
 * Widening {@code protected} to {@code public} on an override is legal in Java, so this subclass is
 * all that is needed.
 * </p>
 */
public class TestableXmlCamelComponent extends AbstractXmlCamelComponent {

    public TestableXmlCamelComponent(final String xmlDataProperty) {
        super(xmlDataProperty);
    }

    @Override
    public void activate(final BundleContext context, final Map<String, Object> properties) throws Exception {
        super.activate(context, properties);
    }

    @Override
    public void deactivate(final BundleContext context) throws Exception {
        super.deactivate(context);
    }

    @Override
    public void modified(final Map<String, Object> properties) throws Exception {
        super.modified(properties);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

}
