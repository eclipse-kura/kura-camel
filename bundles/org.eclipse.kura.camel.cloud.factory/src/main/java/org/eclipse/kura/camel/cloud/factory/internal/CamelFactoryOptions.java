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
package org.eclipse.kura.camel.cloud.factory.internal;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Metatype for {@link CamelFactory}.
 * <p>
 * The {@code cardinality = 1} on every attribute is not the metatype default (which is 0): it is
 * carried over verbatim from the hand-written descriptor this type replaces, so that existing
 * device configurations keep resolving.
 * </p>
 */
@ObjectClassDefinition(id = "org.eclipse.kura.camel.cloud.factory.CamelFactory", name = "Camel Cloud Client", description = "Camel Cloud Client factory")
public @interface CamelFactoryOptions {

    @AttributeDefinition(name = "Router XML", cardinality = 1, description = "The camel XML router configuration|TextArea")
    String xml();

    @AttributeDefinition(name = "Enable Camel JMX support", cardinality = 1, description = "This setting controls if JMX support for the Camel context will be activated or not.")
    boolean enableJmx() default true;

}
