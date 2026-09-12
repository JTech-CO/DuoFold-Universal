package com.example.duofold.input

import android.content.Context
import com.example.duofold.model.FoldPose
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Call start/stop on the main thread with the host lifecycle. Stop releases collectors. */
class AdaptiveAndroidPoseSource(context: Context) : FoldPoseSource {
    private val motion = AndroidMotionPoseSource(context)
    private val hinge = AndroidHingePoseSource(context)
    private var scope: CoroutineScope? = null
    private val mutablePose = MutableStateFlow(FoldPose())
    private val mutableAvailable = MutableStateFlow(false)
    override val pose: StateFlow<FoldPose> = mutablePose.asStateFlow()
    override val available: StateFlow<Boolean> = mutableAvailable.asStateFlow()

    override fun start() {
        if (scope != null) return
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).also { owner ->
            owner.launch {
                combine(hinge.available, motion.available, hinge.pose, motion.pose) { h, m, hp, mp ->
                    (if (h) hp else mp) to (h || m)
                }.collect { (value, ready) ->
                    mutablePose.value = value
                    mutableAvailable.value = ready
                }
            }
        }
        hinge.start()
        motion.start()
    }

    override fun stop() {
        hinge.stop()
        motion.stop()
        scope?.cancel()
        scope = null
        mutableAvailable.value = false
    }

    override fun recalibrate() { motion.recalibrate() }
}
