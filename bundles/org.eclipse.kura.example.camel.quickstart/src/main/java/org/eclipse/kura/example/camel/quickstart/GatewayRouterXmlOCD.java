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
@ObjectClassDefinition(id = "org.eclipse.kura.example.camel.quickstart.GatewayRouterXml", name = "Camel Kura quickstart XML", description = "Configurable Camel-based Kura gateway", icon = @Icon(resource = "OSGI-INF/logo.png", size = 32))
public @interface GatewayRouterXmlOCD {

    @AttributeDefinition(name = "Camel router XML", required = false, max = "10000", description = "Camel XML route definition|TextArea")
    String camel_route_xml() default "<routes xmlns=\"http://camel.apache.org/schema/spring\">\n"
            + "    <route id=\"cloudConsumer\">\n"
            + "        <from uri=\"kura-cloud:myapp/xmltopic\"/>\n"
            + "        <to uri=\"log:MESSAGE_SENT_TO_CLOUD\"/>\n"
            + "    </route>\n"
            + "</routes>";

}
