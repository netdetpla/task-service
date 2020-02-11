package org.ndp.service.task.utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.ndp.service.task.beans.K8sJob
import org.ndp.service.task.beans.MQTask
import org.ndp.service.task.utils.Logger.logger
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStream

object KubernetesHandler {

    private val k8sClient: DefaultKubernetesClient

    init {
        val configContent = FileReader("config.yaml").readText()
        val config = Config.fromKubeconfig(configContent)
        config.namespace = "default"
        k8sClient = DefaultKubernetesClient(config)
    }

    fun applyJob(jobJSON: InputStream) {
        k8sClient.load(jobJSON).createOrReplace()
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