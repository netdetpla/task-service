package org.ndp.service.task.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.ndp.service.task.beans.KafkaTask
import java.util.*

object KafkaHandler {

    private val producer: KafkaProducer<String, String>

    init {
        val producerProps = Properties()
        producerProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] =
            Settings.setting["BOOTSTRAP_SERVERS_CONFIG"] as String
        producerProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] =
            Settings.setting["KEY_SERIALIZER_CLASS_CONFIG"] as String
        producerProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] =
            Settings.setting["VALUE_SERIALIZER_CLASS_CONFIG"] as String
        producer = KafkaProducer<String, String>(producerProps)
    }

    fun produceTask(kafkaTask: KafkaTask, taskTopic: String) {
        val mapper = ObjectMapper()
        val value = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(kafkaTask)
        val task = ProducerRecord<String, String>(taskTopic, value)
        producer.send(task)
    }
}