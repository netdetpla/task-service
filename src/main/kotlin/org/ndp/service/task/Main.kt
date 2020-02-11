package org.ndp.service.task

import org.ndp.service.task.utils.*
import org.ndp.service.task.utils.Logger.logger
import java.util.*

fun createNewJob(limit: Int) {
    val imageID = DatabaseHandler.selectImageByFirstWaitingTask()
    if (imageID == 0) {
        logger.debug("nothing to do.")
        return
    }
    val imageInfo = DatabaseHandler.selectImageInfo(imageID)
    val tasks = DatabaseHandler.selectWaitingTasks(limit, imageID)
    logger.debug("new tasks: ${tasks.size}")
    logger.debug("producing task message...")
    for (t in tasks) {
        RedisHandler.produceTask(t, imageInfo.K8sYAML)
    }
    logger.debug("starting k8s job...")
    KubernetesHandler.applyJob(imageInfo.K8sYAML)
}

fun main() {
    val parallelNum = Integer.parseInt(Settings.setting["parallelNum"] as String)
    logger.info("task service started")
    val jobs = ArrayList<String>()
    while (true) {
        logger.debug("checking active pod count...")
        var count = 0
        for (job in jobs) {
            count += KubernetesHandler.getActivePodCountInJob(job)
        }
        logger.debug("active pods: $count")
        if (count < parallelNum) {
            logger.debug("creating a new job")
            createNewJob(parallelNum - count)
        }
        logger.debug("checking finished jobs...")
        val finishedTasks = ArrayList<String>()
        for (j in jobs) {
            if (KubernetesHandler.checkJobCompletion(j)) {
                finishedTasks.add(j)
            }
        }
        logger.debug("finished jobs: ${finishedTasks.size}")
        for (fj in finishedTasks) {
            jobs.remove(fj)
            KubernetesHandler.deleteJob(fj)
        }
        logger.debug("waiting 60s...")
        Thread.sleep(60000)
    }
}