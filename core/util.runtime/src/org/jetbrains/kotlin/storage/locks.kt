/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.storage

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

private const val CHECK_CANCELLATION_PERIOD_MS: Long = 50

interface LockBlock {
    fun <T> guarded(computable: () -> T): T
}

object NoLockBlock : LockBlock {
    override fun <Any> guarded(computable: () -> Any): Any {
        return computable()
    }
}

class SimpleLock(val lock: Any = Object()) : LockBlock {
    override fun <T> guarded(computable: () -> T): T =
        // Use `synchronized` as dead lock case will be handled by JVM and would be immediately visible rather with ReentrantLock
        synchronized(lock) {
            computable()
        }
}

class CancellableLock(val lock: Lock, val checkCancelled: () -> Unit) : LockBlock {
    constructor(checkCancelled: () -> Unit) : this(checkCancelled = checkCancelled, lock = ReentrantLock())

    override fun <T> guarded(computable: () -> T): T {
        while (!lock.tryLock(CHECK_CANCELLATION_PERIOD_MS, TimeUnit.MILLISECONDS)) {
            //ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
            checkCancelled()
        }

        try {
            return computable()
        } finally {
            lock.unlock()
        }
    }
}