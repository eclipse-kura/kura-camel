@Library('add-ons-shared-libs@develop') _

node {
    continuousIntegrationPipeline(
        buildType: "deploy",
        sonar: [
            enable: false,
            projectKey: "eclipse-kura_kura-camel",
            tokenId: "sonarcloud-token-kura-camel",
            exclusions: "tests/**/*.java"
        ],
    )
}
