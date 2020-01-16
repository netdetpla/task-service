package org.ndp.service.task.beans

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.text
import me.liuwj.ktorm.schema.varchar

object Image : Table<Nothing>("image") {
    val id by int("id").primaryKey()
    val imageName by varchar("image_name")
    val tag by varchar("tag")
    val size by varchar("size")
    val uploadTime by varchar("upload_time")
    val fileName by varchar("file_name")
    val isLoaded by int("is_loaded")
    val taskTopic by varchar("task_topic")
    val resultTopic by varchar("result_topic")
    val k8sYAML by varchar("k8s_yaml")
}
