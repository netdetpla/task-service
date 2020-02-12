package org.ndp.service.task

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.ndp.service.task.beans.K8sJob
import org.ndp.service.task.utils.*
import org.ndp.service.task.utils.Logger.logger
import java.io.FileReader
import java.util.*


object Main {
    private val k8sJobTemplate: K8sJob
    private val k8sJobAdapter: JsonAdapter<K8sJob>
    private val parallelNum = Integer.parseInt(Settings.setting["parallelNum"] as String)

    init {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        k8sJobAdapter = moshi.adapter(K8sJob::class.java)
        k8sJobTemplate = k8sJobAdapter.fromJson(
            FileReader("job-template.json").readText()
        )!!
    }

    data class Job(
        val jobName: String,
        val rrImageName: String
    )

    private fun createNewJob(limit: Int): Job? {
        val imageID = DatabaseHandler.selectImageByFirstWaitingTask()
        if (imageID == 0) {
            logger.debug("nothing to do.")
            return null
        }
        val imageInfo = DatabaseHandler.selectImageInfo(imageID)
        val tasks = DatabaseHandler.selectWaitingTasks(limit, imageID)
        logger.debug("new tasks: ${tasks.size}")
        logger.debug("producing task message...")
        for (t in tasks) {
            RedisHandler.produceTask(t, imageInfo.taskTopic)
        }
        logger.debug("starting k8s job...")
        val jobName = k8sJobTemplate.initJob(imageInfo.imageName, tasks.size)
        KubernetesHandler.applyJob(k8sJobAdapter.toJson(k8sJobTemplate).byteInputStream())
        logger.debug("updating task status...")
        DatabaseHandler.batchUpdateTaskStatus(
            tasks.map {
                it.taskID
            },
            20020
        )
        return Job(jobName, imageInfo.rrImageName)
    }

    private fun createNewRRJob(rrImageName: String): Job {
        val rrJobName = k8sJobTemplate.initJob(rrImageName, 1)
        KubernetesHandler.applyJob(k8sJobAdapter.toJson(k8sJobTemplate).byteInputStream())
        return Job(rrJobName, "")
    }

    @JvmStatic
    fun main(args: Array<String>) {

        logger.info("task service started")
        val jobs = ArrayList<Job>()
        while (true) {
            logger.debug("checking active pod count...")
            var count = 0
            for (job in jobs) {
                count += KubernetesHandler.getActivePodCountInJob(job.jobName)
            }
            logger.debug("active pods: $count")
            if (count < parallelNum) {
                logger.debug("creating a new job")
//                jobs.add(createNewJob(parallelNum - count)?)
                createNewJob(parallelNum - count)?.let { jobs.add(it) }
            }
            logger.debug("checking finished jobs...")
            val finishedTasks = ArrayList<Job>()
            for (j in jobs) {
                if (KubernetesHandler.checkJobCompletion(j.jobName)) {
                    finishedTasks.add(j)
                }
            }
            logger.debug("finished jobs: ${finishedTasks.size}")
            val rrJobs = HashSet<String>()
            for (fj in finishedTasks) {
                jobs.remove(fj)
                KubernetesHandler.deleteJob(fj.jobName)
                rrJobs.add(fj.rrImageName)
            }
            logger.debug("starting rr images: ${rrJobs.size}")
            for (rr in rrJobs) {
                if (rr != "") {
                    jobs.add(createNewRRJob(rr))
                }
            }
            logger.debug("waiting 60s...")
            Thread.sleep(60000)
        }
    }
}