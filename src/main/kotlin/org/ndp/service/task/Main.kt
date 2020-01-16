package org.ndp.service.task

import me.liuwj.ktorm.dsl.*
import org.ndp.service.task.beans.KafkaTask
import org.ndp.service.task.beans.Task
import org.ndp.service.task.utils.DatabaseHandler
import org.ndp.service.task.utils.DockerHandler
import org.ndp.service.task.utils.KafkaHandler
import org.ndp.service.task.utils.KubernetesHandler
import java.io.FileReader
import java.util.*

fun main() {
    val settings = Properties()
    settings.load(FileReader("settings.properties"))
    val parallelNum = settings["parallelNum"] as Int
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
                    KafkaHandler.produceTask(
                        KafkaTask(
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