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
package org.eclipse.kura.example.camel.quickstart;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Icon;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@SuppressWarnings("checkstyle:MethodName")
@ObjectClassDefinition(id = "org.eclipse.kura.example.camel.quickstart.GatewayRouterJava", name = "Camel Kura quickstart Java", description = "Configurable Camel-based Kura gateway", icon = @Icon(resource = "OSGI-INF/logo.png", size = 32))
public @interface GatewayRouterJavaOCD {

    @AttributeDefinition(name = "Enable service", cardinality = 1, description = "If the service is enabled it will publish data")
    boolean enabled() default true;

    @AttributeDefinition(name = "Cloud Service PID", cardinality = 1, required = false, description = "The service PID of the Cloud Service to use")
    String cloudService() default "org.eclipse.kura.cloud.CloudService";

    @AttributeDefinition(name = "Maximum Temperature", description = "The maximum value for the random temperature")
    int temperature_max() default 20;

}
