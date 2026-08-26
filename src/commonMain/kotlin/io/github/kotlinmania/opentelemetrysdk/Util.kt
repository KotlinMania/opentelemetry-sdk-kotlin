// port-lint: source util.rs
package io.github.kotlinmania.opentelemetrysdk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

/**
 * Internal utilities.
 */

/**
 * Helper which creates a periodic flow emitting at [period] intervals.
 */
fun intervalFlow(period: Duration): Flow<Unit> =
    flow {
        while (true) {
            delay(period)
            emit(Unit)
        }
    }
