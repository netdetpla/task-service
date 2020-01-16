package org.ndp.service.task.utils

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.command.PushImageResultCallback
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit

object DockerHandler {
    private val dockerClient: DockerClient
    val dockerProperties = Properties()

    init {
        dockerProperties["DOCKER_HOST"] = Settings.setting["DOCKER_HOST"] as String
        dockerProperties["DOCKER_CONFIG"] = Settings.setting["DOCKER_CONFIG"] as String
        dockerProperties["api.version"] = Settings.setting["api.version"] as String
        dockerProperties["registry.url"] = Settings.setting["registry.url"] as String
        val config = DefaultDockerClientConfig.Builder().withProperties(dockerProperties)
        val dockerCmdExecFactory = JerseyDockerCmdExecFactory()
            .withReadTimeout(1000)
            .withConnectTimeout(1000)
            .withMaxTotalConnections(100)
            .withMaxPerRouteConnections(10)
        dockerClient = DockerClientBuilder.getInstance(config)
            .withDockerCmdExecFactory(dockerCmdExecFactory)
            .build()
    }

    @Synchronized
    fun push(imageName: String, imagePath: String) {
        val fullImageName = (dockerProperties["registry.url"] as String) + "/" + imageName
        val imageInputStream = FileInputStream(imagePath)
        dockerClient.loadImageCmd(imageInputStream).exec()
        dockerClient.pushImageCmd(fullImageName)
            .exec(PushImageResultCallback())
            .awaitCompletion(30, TimeUnit.SECONDS)
    }
}