package org.ndp.service.task.beans

data class ImageInfo(
    val imageID: Int,
    val imageName: String,
    val taskTopic: String,
    val K8sYAML: String
)