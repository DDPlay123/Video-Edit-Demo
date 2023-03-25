package com.side.project.video.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Coroutine 工具
 * 這裡的 work 參數是一個 lambda 函式，並且是 suspend 的，所以可以在裡面使用 coroutine 的語法
 */
object Coroutines {
    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO).launch { work() }

    fun main(work: suspend () -> Unit) =
        CoroutineScope(Dispatchers.Main).launch { work() }

    fun default(work: suspend () -> Unit) =
        CoroutineScope(Dispatchers.Default).launch { work() }

    fun unconfined(work: suspend () -> Unit) =
        CoroutineScope(Dispatchers.Unconfined).launch { work() }
}