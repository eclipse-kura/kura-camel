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
| `org.eclipse.kura.camel.sun.misc` | Metadata-only OSGi fragment adding a `sun.misc` import to `camel-core`. |

The Camel runtime itself (`camel-core` and friends, Spring, Qpid) is shipped by the Debian package,
not embedded in the bundles.

## Building

Requires **JDK 21** and Maven 3.9+.

```bash
mvn clean install                               # bundles + Debian package, unit tests only
mvn clean install -Presolve-integration-tests   # also re-resolve the integration test runtime
mvn clean install -DreleaseBuild                # release version numbers, no ~gitYYYY... suffix
```

The Debian package is produced at
`distrib/kura-camel/target/deb/kura-camel_<version>-<revision>_all.deb`.

Bundle start levels are encoded in the directory names under `distrib/kura-camel/target/plugins/`:
`<n>` means start level `n`, `<n>s` means start level `n` with auto-start. `camel-core` and
`camel-script` must be auto-started because their `Bundle-Activator` is what discovers the
`META-INF/services/org/apache/camel/*` registrations; the `sun.misc` fragment must **not** be, since
Equinox cannot start a fragment.

## Installing on a device

```bash
dpkg -i kura-camel_<version>_all.deb
systemctl restart kura
```

Camel components then appear in the Kura web console: `Camel XML router` and `Camel Cloud Client`
as service factories, and the three Camel wire components in the Wire graph editor.

## Versioning

Bundle and package versions follow the versions these bundles carried inside the Kura framework
(`2.0.0`). The exported `org.eclipse.kura.camel.*` packages keep their historical `1.1.0` version so
that existing consumers continue to resolve; `bnd-baseline-maven-plugin` enforces this.
