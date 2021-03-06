package org.ndp.service.task.utils

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.*
import org.ndp.service.task.beans.Image
import org.ndp.service.task.beans.ImageInfo
import org.ndp.service.task.beans.MQTask
import org.ndp.service.task.beans.Task
import org.ndp.service.task.utils.Logger.logger
import java.io.FileReader
import java.util.*

object DatabaseHandler {

    init {
        val dbUrl = Settings.setting["dbUrl"] as String
        val dbDriver = Settings.setting["dbDriver"] as String
        val dbUser = Settings.setting["dbUser"] as String
        val dbPassword = Settings.setting["dbPassword"] as String
        Database.Companion.connect(
            dbUrl,
            dbDriver,
            dbUser,
            dbPassword
        )
    }

    fun selectImageStatus(imageID: Int) {
        logger.debug("image id: $imageID")
        var isLoaded = -1
        var imageName = ""
        var fileName = ""
        Image.select(Image.isLoaded, Image.imageName, Image.fileName)
            .where {
                Image.id eq imageID
            }
            .limit(0, 1)
            .forEach {
                isLoaded = it[Image.isLoaded]!!
                imageName = it[Image.imageName]!!
                fileName = it[Image.fileName]!!
            }
        if (isLoaded == 0) {
            DockerHandler.push(imageName, "/image/$fileName")
            Image.update {
                it.isLoaded to 1
                where {
                    Image.id eq imageID
                }
            }
        }
    }

    private fun selectTaskCount(statusCode: Int): Int {
        val countLabel = count(Task.id).aliased("c")
        return Task.select(count(Task.id))
            .where {
                Task.taskStatus eq statusCode
            }
            .toList()[0][countLabel] ?: 0
    }

    fun selectRunningTaskCount(): Int {
        return selectTaskCount(20020)
    }

    fun selectWaitingTaskCount(): Int {
        return selectTaskCount(20000)
    }

    fun selectImageInfo(imageID: Int): ImageInfo {
        var imageName = ""
        var taskTopic = ""
        var rrimageName = ""
        Image.select(Image.imageName, Image.taskTopic, Image.rrImageName)
            .where { Image.id eq imageID }
            .forEach {
                imageName = it[Image.imageName]!!
                taskTopic = it[Image.taskTopic]!!
                rrimageName = it[Image.rrImageName]!!
            }
        return ImageInfo(
            imageID,
            imageName,
            taskTopic,
            rrimageName
        )
    }

    fun selectImageByFirstWaitingTask(): Int {
        val imageIDs = Task.select(Task.imageID)
            .where { Task.taskStatus eq 20000 }
            .limit(0, 1)
            .map { it[Task.imageID]!! }
            .toList()
        return if (imageIDs.isEmpty()) {
            0
        } else {
            imageIDs[0]
        }
    }

    fun selectWaitingTasks(limit: Int, imageID: Int): List<MQTask> {
        val tasks = ArrayList<MQTask>()
        // todo 镜像状态检查
        val imageInfo = selectImageInfo(imageID)
        Task.select(Task.id, Task.param)
            .where { Task.taskStatus eq 20000 }
            .limit(0, limit)
            .forEach {
                tasks.add(
                    MQTask(
                        it[Task.id]!!,
                        (DockerHandler.dockerProperties["registry.url"] as String) + "/" + imageInfo.imageName,
                        it[Task.param]!!
                    )
                )
            }
        return tasks
    }

    fun batchUpdateTaskStatus(tasks: List<Int>, status: Int) {
        Task.batchUpdate {
            for (t in tasks) {
                item {
                    it.taskStatus to status
                    where {
                        Task.id eq t
                    }
                }
            }
        }
    }
}