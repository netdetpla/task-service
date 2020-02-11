package org.ndp.service.task.beans

import org.ndp.service.task.utils.Logger.logger
import org.ndp.service.task.utils.Settings

data class Metadata(
    var name: String
)

data class Container(
    var name: String,
    var image: String
)

data class TemplateSpec(
    val containers: List<Container>,
    val restartPolicy: String
)

data class Template(
    val metadata: Metadata,
    val spec: TemplateSpec
)

data class JobSpec(
    var completions: Int,
    var parallelism: Int,
    val template: Template
)

data class K8sJob(
    val apiVersion: String,
    val kind: String,
    val metadata: Metadata,
    val spec: JobSpec
) {

    fun generateNonce(size: Int): String {
        val nonceScope = "1234567890abcdefghijklmnopqrstuvwxyz"
        val scopeSize = nonceScope.length
        val nonceItem: (Int) -> Char = { nonceScope[(scopeSize * Math.random()).toInt()] }
        return Array(size, nonceItem).joinToString("")
    }

    fun initJob(name: String, count: Int): String {
        logger.debug("init job json...")
        spec.template.metadata.name = name
        spec.template.spec.containers[0].image =
            Settings.setting["registry.url"] as String + "/" + name
        completionsReset(count)
        return jobRename()
    }

    private fun jobRename(): String {
        metadata.name = spec.template.metadata.name + "-" + generateNonce(5)
        return metadata.name
    }

    private fun completionsReset(newCount: Int) {
        spec.completions = newCount
        spec.parallelism = newCount
    }
}