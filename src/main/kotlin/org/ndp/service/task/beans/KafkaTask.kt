package org.ndp.service.task.beans

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class KafkaTask(
    @JsonProperty(value = "task-id") val taskID: Int,
    @JsonProperty(value = "full-image-name") val fullImageName: String,
    val param: String
)