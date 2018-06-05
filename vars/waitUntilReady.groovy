#!/usr/bin/env groovy

def call(String label,
         String containerName,
         String version,
         String namespace) {
    def podVersion = ''
    container('kubectl') {
        while (podVersion != version) {
            def pods = sh(
                    script: "kubectl -n ${namespace} get po -l ${label} --field-selector=status.phase=Running --no-headers",
                    returnStdout: true
            ).trim()
            def podNameLine = pods.split('\n')[0]
            def startIndex = podNameLine.indexOf(' ')
            if (startIndex == -1) {
                return
            }
            def podName = podNameLine.substring(0, startIndex)
            try {
                def versionString = sh(
                        script: "kubectl -n test exec ${podName} -c ${containerName} env | grep ^VERSION=",
                        returnStdout: true
                ).trim()
                podVersion = versionString.split('=')[1]
                echo "podVersion: ${podVersion}"
            } catch (e) {
                echo e.getMessage()
            }
        }
    }
}