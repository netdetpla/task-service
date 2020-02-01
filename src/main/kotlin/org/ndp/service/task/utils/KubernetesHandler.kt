package org.ndp.service.task.utils

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import java.io.FileInputStream
import java.io.FileReader

object KubernetesHandler {

    private val k8sClient: DefaultKubernetesClient

    init {
        val configContent = FileReader("config.yaml").readText()
        val config = Config.fromKubeconfig(configContent)
        config.namespace = "default"
        k8sClient = DefaultKubernetesClient(config)
    }

    fun applyJob(jobYAML: String) {
        k8sClient.load(FileInputStream(jobYAML)).createOrReplace()
    }
}