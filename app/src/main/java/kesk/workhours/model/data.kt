package kesk.workhours.model

import org.joda.time.DateTime
import java.text.DateFormat

data class Date(val year: Int, val month: Int, val day: Int) {
    companion object {
        fun now(): Date {
            val now = DateTime.now()
            return Date(now.year, now.monthOfYear, now.dayOfMonth)
        }
    }

    fun format(formatter: DateFormat): CharSequence {
        val date = DateTime(year, month, day, 0, 0)
        return formatter.format(date.toDate())
    }
}

data class Time(val hour: Int, val minute: Int) {
    companion object {
        fun now(): Time {
            val now = DateTime.now()
            return Time(now.hourOfDay, now.minuteOfHour)
        }
    }

    fun format(formatter: DateFormat): CharSequence {
        val date = DateTime.now().withHourOfDay(hour).withMinuteOfHour(minute)
        return formatter.format(date.toDate())
    }
}

data class WorkDay(val date: Date,
                   val start: Time,
                   val end: Time,
                   val lunchStart: Time,
                   val lunchEnd: Time) {
    companion object {
        fun orNull(date: Date?,
                   start: Time?,
                   end: Time?,
                   lunchStart: Time?,
                   lunchEnd: Time?): WorkDay? {
            return if (date != null && start != null && end != null && lunchStart != null
                    && lunchEnd != null) {
                WorkDay(date, start, end, lunchStart, lunchEnd)
            } else {
                null
            }
        }
    }
}