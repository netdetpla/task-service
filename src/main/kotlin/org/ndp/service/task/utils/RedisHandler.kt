package org.ndp.service.task.utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import org.ndp.service.task.beans.MQTask

object RedisHandler {
    private val commands: RedisCommands<String, String>
    private val mqResultAdapter: JsonAdapter<MQTask>

    init {
        val client = RedisClient.create(Settings.setting["redis.url"] as String)
        val connection = client.connect()
        commands = connection.sync()

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        mqResultAdapter = moshi.adapter(MQTask::class.java)
    }

    fun produceTask(task: MQTask, streamName: String) {
        val body = HashMap<String, String>()
        body["task"] = mqResultAdapter.toJson(task)
        commands.xadd(streamName, body)
    }
}