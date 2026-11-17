/*******************************************************************************
 * Copyright (c) 2011, 2026 Red Hat Inc and/or its affiliates and others
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
package org.eclipse.kura.camel.camelcloud;

/**
 * Kura cloud constants
 */
public final class KuraCloudClientConstants {

    public static final String TOPIC = "topic";

    public static final String APPLICATION_ID = "applicationId";

    public static final String CAMEL_KURA_CLOUD = "CamelKuraCloudService";

    public static final String CAMEL_KURA_CLOUD_TOPIC = CAMEL_KURA_CLOUD + ".topic";

    public static final String CAMEL_KURA_CLOUD_QOS = CAMEL_KURA_CLOUD + ".qos";

    public static final String CAMEL_KURA_CLOUD_RETAIN = CAMEL_KURA_CLOUD + ".retain";

    public static final String CAMEL_KURA_CLOUD_PRIORITY = CAMEL_KURA_CLOUD + ".priority";

    public static final String CAMEL_KURA_CLOUD_CONTROL = CAMEL_KURA_CLOUD + ".control";

    public static final String CAMEL_KURA_CLOUD_DEVICEID = CAMEL_KURA_CLOUD + ".deviceId";

    public static final String CAMEL_KURA_CLOUD_MESSAGEID = CAMEL_KURA_CLOUD + ".messageId";

    /**
     * The {@link org.eclipse.kura.cloudconnection.message.KuraMessage} property holding the application ID
     */
    public static final String PROP_APP_ID = "APP_ID";

    /**
     * The {@link org.eclipse.kura.cloudconnection.message.KuraMessage} property holding the application topic
     */
    public static final String PROP_APP_TOPIC = "APP_TOPIC";

    /**
     * The {@link org.eclipse.kura.cloudconnection.message.KuraMessage} property holding the QoS
     */
    public static final String PROP_QOS = "QOS";

    /**
     * The {@link org.eclipse.kura.cloudconnection.message.KuraMessage} property holding the retain flag
     */
    public static final String PROP_RETAIN = "RETAIN";

    /**
     * The {@link org.eclipse.kura.cloudconnection.message.KuraMessage} property holding the priority
     */
    public static final String PROP_PRIORITY = "PRIORITY";

    /**
     * The {@link org.eclipse.kura.cloudconnection.message.KuraMessage} property holding the control flag
     */
    public static final String PROP_CONTROL = "CONTROL";

    /**
     * The (optional) {@link org.eclipse.kura.cloudconnection.message.KuraMessage} property holding the device ID
     */
    public static final String PROP_DEVICE_ID = "deviceId";

    private KuraCloudClientConstants() {}
}
