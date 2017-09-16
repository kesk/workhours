package kesk.workhours

import android.content.SharedPreferences
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

fun SharedPreferences.Editor.putDate(key: String, value: Date?) {
    value?.let {
        putInt(key + "-year", value.year)
        putInt(key + "-month", value.month)
        putInt(key + "-day", value.day)
    }
}

fun SharedPreferences.getDate(key: String): Date? {
    val year = getInt(key + "-year", -1)
    val month = getInt(key + "-month", -1)
    val day = getInt(key + "-day", -1)

    return if (year != -1 && month != -1 && day != -1) Date(year, month, day) else null
}

fun SharedPreferences.Editor.putTime(key: String, value: Time?) {
    value?.let {
        putInt(key + "-hour", value.hour)
        putInt(key + "-minute", value.minute)
    }
}

fun SharedPreferences.getTime(key: String): Time? {
    val hour = getInt(key + "-hour", -1)
    val minute = getInt(key + "-minute", -1)

    return if (hour != -1 && minute != -1) Time(hour, minute) else null
}

fun SharedPreferences.Editor.removeDate(key: String) {
    remove(key + "-year")
    remove(key + "-month")
    remove(key + "-day")
}

fun SharedPreferences.Editor.removeTime(key: String) {
    remove(key + "-hour")
    remove(key + "-minute")
}
