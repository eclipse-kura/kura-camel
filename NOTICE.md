# Notices for the Eclipse Kura Camel add-on

This content is produced and maintained by the Eclipse Kura project.

* Project home: https://projects.eclipse.org/projects/iot.kura

## Trademarks

Eclipse Kura, and Kura are trademarks of the Eclipse Foundation.

## Copyright

All content is the property of the respective authors or their employers. For
more information regarding authorship of content, please consult the listed
source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Eclipse Public License v2.0 which is available at
https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html.

SPDX-License-Identifier: EPL-2.0

## Source Code

The project maintains the following source code repositories:

* https://github.com/eclipse-kura/kura-camel

## Third-party Content

This project leverages the following third party content.

Content provided by the Eclipse Kura framework itself is not repeated here; see
the `NOTICE.md` of https://github.com/eclipse-kura/kura. The list below was
produced with the Eclipse Dash License Tool and covers only what this
repository adds, that is the Apache Camel runtime shipped by the Debian package.
Test-only libraries are omitted, following the convention of the framework's own
notice file.

As of Camel 4 the `org.apache.camel` artifacts carry no OSGi metadata, so the
bundles consumed here are the `org.apache.camel.karaf` repackages of the same
classes. ClearlyDefined holds no license determination for those coordinates,
which is why every entry below is reported as `restricted` with an empty license
field: Apache Camel itself is Apache-2.0, but the repackaged artifacts have not
been vetted. **They require an Eclipse IP team review before this add-on can be
released.**

### Maven Dependencies

* maven/mavencentral/org.apache.camel.karaf/camel-api/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-attachments/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-base-engine/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-base/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-bean/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-core-engine/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-core-languages/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-core-model/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-core-osgi/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-core-processor/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-core-reifier/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-direct/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-health/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-log/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-management-api/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-management/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-mock/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-seda/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-stream/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-support/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-timer/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-util-json/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-util/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-vm/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-xml-io-util/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-xml-io/4.18.2, , restricted, clearlydefined
* maven/mavencentral/org.apache.camel.karaf/camel-xml-jaxp/4.18.2, , restricted, clearlydefined

## Cryptography

Content may contain encryption software. The country in which you are currently
may have restrictions on the import, possession, and use, and/or re-export to
another country, of encryption software. BEFORE using any encryption software,
please check the country's laws, regulations and policies concerning the import,
possession, or use, and re-export of encryption software, to see if this is
permitted.
