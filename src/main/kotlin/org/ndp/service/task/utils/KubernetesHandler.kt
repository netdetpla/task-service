package org.ndp.service.task.utils

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.ndp.service.task.utils.Logger.logger
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

    fun applyJob(jobJSON: String) {
        k8sClient.load(FileInputStream(jobJSON)).createOrReplace()
    }

    fun getActivePodCountInJob(jobName: String): Int {
        val job = k8sClient.batch().jobs().withName(jobName).get()
        return job.status.active
    }

    fun checkJobCompletion(jobName: String): Boolean {
        val job = k8sClient.batch().jobs().withName(jobName).get()
        return job.status.succeeded == job.spec.completions
    }

    fun deleteJob(jobName: String) {
        logger.debug("deleting job: $jobName")
        k8sClient.batch().jobs().withName(jobName).delete()
    }
}