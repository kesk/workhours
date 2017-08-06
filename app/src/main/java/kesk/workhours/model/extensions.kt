package kesk.workhours

import android.os.Bundle
import kesk.workhours.model.Date
import kesk.workhours.model.Time

fun Bundle.putDate(key: String, value: Date?) {
    value?.let { putIntArray(key, intArrayOf(it.year, it.month, it.day)) }
}

fun Bundle.getDate(key: String): Date? {
    return getIntArray(key)?.let { Date(it[0], it[1], it[2]) }
}

fun Bundle.putTime(key: String, value: Time?) {
    value?.let { putIntArray(key, intArrayOf(it.hour, it.minute)) }
}

fun Bundle.getTime(key: String): Time? {
    return getIntArray(key)?.let { Time(it[0], it[1]) }
}