# kura-camel

Eclipse Kura™ Camel addon.

Apache Camel integration for the [Eclipse Kura](https://eclipse.dev/kura/) framework, delivered as a
Debian package installed alongside `kura-core`. These bundles used to be part of the Kura framework
repository and were extracted here so that Camel support can be versioned, built and released
independently.

## Bundles

| Bundle | Purpose |
|---|---|
| `org.eclipse.kura.camel` | Core and API bundle: the `kura-cloud:` Camel component, the `CamelRunner` context lifecycle, the Kura↔Camel type converters and the `AbstractCamelComponent` base classes. Exports the `org.eclipse.kura.camel.*` API at version `1.1.0`. |
| `org.eclipse.kura.camel.cloud.factory` | A `CloudServiceFactory` backed by an XML-defined Camel route. |
| `org.eclipse.kura.camel.xml` | The `Camel XML router` configurable component: runs Camel routes defined as XML in the component configuration. |
| `org.eclipse.kura.wire.camel` | Camel wire components — `CamelConsume`, `CamelProcess`, `CamelProduce` — for use in a Kura Wire graph. |

Example applications, moved here from [kura-apps](https://github.com/eclipse-kura/kura-apps) so that
all the Camel code lives in one place:

| Bundle | Purpose |
|---|---|
| `org.eclipse.kura.example.camel.quickstart` | Two configurable gateways publishing a random temperature, one built with the Java DSL and one with the XML DSL. |
| `org.eclipse.kura.example.camel.publisher` | A periodic cloud publisher driven entirely by its component configuration. |
| `org.eclipse.kura.example.camel.aggregation` | An aggregation route averaging a generated temperature over a time window. |

## The Camel runtime

The Camel runtime itself is shipped by the Debian package, not embedded in the bundles. It is
**Apache Camel 4.18**, an LTS line, taken from **`org.apache.camel.karaf`**: as of Camel 4 the
`org.apache.camel` artifacts are plain jars carrying no OSGi metadata, and camel-karaf republishes
the very same classes as bundles. It is the only OSGi-ready Camel 4 distribution, and
`camel-core-osgi` — the OSGi glue — does not drag Karaf in.

Nothing in the package touches the `javax.*` namespace any more. That did not happen by swapping the
JAXB provider for its `jakarta.*` twin: Camel 4 loads XML routes with `camel-xml-io`, a StAX parser,
so **JAXB is not on the path at all**. `jaxb-api`, the JAXB reference implementation,
`javax.activation`, Spring, Qpid and `geronimo-jms` are all gone, and so is the `sun.misc` OSGi
fragment (`camel-core` never imported `sun.misc` to begin with).

## Building

Requires **JDK 21** and Maven 3.9+.

```bash
mvn clean install                               # bundles + Debian package, unit tests only
mvn clean install -Presolve-integration-tests   # also re-resolve the integration test runtime
mvn clean install -DreleaseBuild                # release version numbers, no ~gitYYYY... suffix
```

Two Debian packages are produced:

| Package | Contents |
|---|---|
| `distrib/kura-camel/target/deb/kura-camel_*.deb` | the four service bundles and the Camel runtime |
| `distrib/kura-camel-example/target/deb/kura-camel-example_*.deb` | the three example bundles; depends on `kura-camel` |

Bundle start levels are encoded in the directory names under `distrib/kura-camel/target/plugins/`:
`<n>` means start level `n`, `<n>s` means start level `n` with auto-start.

Every Camel bundle goes to `plugins/5s`, that is, all of them are auto-started. Only
`camel-core-osgi` declares a `Bundle-Activator`, but that activator opens a `BundleTracker` over
`Bundle.ACTIVE`, and that tracker is what registers each bundle's
`META-INF/services/org/apache/camel/component`, `/language` and `/TypeConverter` entries as OSGi
services. A Camel bundle left merely installed contributes nothing at all, and does so without
logging anything.

The set of Camel bundles shipped is the one the `integration-test.bndrun` resolve selects, plus
`camel-management` — which the resolve does not need but the `Camel Cloud Client` factory does, since
its `enableJmx` attribute defaults to `true`.

## Installing on a device

```bash
dpkg -i kura-camel_<version>_all.deb
systemctl restart kura
```

Camel components then appear in the Kura web console: `Camel XML router` and `Camel Cloud Client`
as service factories, and the three Camel wire components in the Wire graph editor.

## Versioning

The service bundles are at `3.0.0` and the examples at `4.0.0`, one major above the versions they
carried inside the Kura framework. The exported `org.eclipse.kura.camel.*` packages moved from
`1.1.0` to `2.0.0`. Both bumps are required rather than cosmetic: the Camel import ranges went from
`[2.21,3.0)` to `[4.18,5)`, and exported types changed supertype — `KuraCloudComponent` now extends
`org.apache.camel.support.DefaultComponent` instead of `org.apache.camel.impl.DefaultComponent`.
`bnd-baseline-maven-plugin` enforces this.

## Migrating from Camel 2.x

Three changes are visible to existing installations.

**The `initCode` attribute is gone**, from both the `Camel XML router` and the `Camel Cloud Client`
factory, together with the JavaScript init hook behind it. It ran on Nashorn, which the JDK removed
in Java 15, so on Kura 6 (Java 21) it could not have worked anyway. The property may still be present
in device snapshots, where it is now ignored.

**`<setHeader headerName="…">` is now `<setHeader name="…">`.** Camel 3 renamed the attribute, and the
Camel 4 XML parser rejects the old spelling outright rather than ignoring it. Existing route XML using
it has to be edited. Route documents themselves are otherwise accepted as they were, with or without
the historical `http://camel.apache.org/schema/spring` namespace.

**`camel-jms` and `camel-amqp` are no longer shipped**, and neither are Spring, Qpid `qpid-jms-client`,
`proton-j` and `geronimo-jms`. Routes using `jms:` or `amqp:` endpoints stop resolving. In Camel 4
`camel-jms` still requires Spring (`spring-jms`, `spring-tx`) and `camel-amqp` additionally requires
Netty, which Kura already ships at a version of its own; pulling that stack into an add-on package was
judged the wrong trade for a component no Kura bundle uses. If it is needed it belongs in a separate
optional package rather than in this one.
