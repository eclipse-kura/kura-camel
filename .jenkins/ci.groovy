@Library('add-ons-shared-libs@develop') _

node {
    continuousIntegrationPipeline(
        sonar: [
            enable: false
        ],
    )
}
