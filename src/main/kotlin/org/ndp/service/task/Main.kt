package org.ndp.service.task

import me.liuwj.ktorm.dsl.*
import org.ndp.service.task.beans.MQTask
import org.ndp.service.task.beans.Task
import org.ndp.service.task.utils.*
import java.io.FileReader
import java.util.*

fun main() {
    val settings = Properties()
    settings.load(FileReader("settings.properties"))
    val parallelNum = Integer.parseInt(settings["parallelNum"] as String)
    while (true) {
        val runningTasksCount = DatabaseHandler.selectRunningTaskCount()
        if (runningTasksCount < parallelNum) {
            val limit = parallelNum - runningTasksCount
            Task.select(Task.id, Task.imageID, Task.param)
                .where { Task.taskStatus eq 20000 }
                .limit(0, limit)
                .forEach {
                    val imageID = it[Task.imageID]!!
                    DatabaseHandler.selectImageStatus(imageID)
                    val imageInfo = DatabaseHandler.selectImageInfo(imageID)
                    RedisHandler.produceTask(
                        MQTask(
                            it[Task.id]!!,
                            (DockerHandler.dockerProperties["registry.url"] as String) + "/" + imageInfo.imageName,
                            it[Task.param]!!
                        ),
                        imageInfo.taskTopic
                    )
                    KubernetesHandler.applyJob(imageInfo.K8sYAML)
                }
        }
        Thread.sleep(60000)
    }
}