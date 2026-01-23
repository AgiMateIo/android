package ru.agimate.android.util

import java.util.UUID

object DeviceIdGenerator {
    fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
