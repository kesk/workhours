package kesk.workhours.model

import org.joda.time.DateTime
import java.text.DateFormat

data class Date(val year: Int, val month: Int, val day: Int) {
    fun format(formatter: DateFormat): CharSequence {
        val date = DateTime(year, month, day, 0, 0)
        return formatter.format(date.toDate())
    }
}

data class Time(val hour: Int, val minute: Int) {
    fun format(formatter: DateFormat): CharSequence {
        val date = DateTime.now().withHourOfDay(hour).withMinuteOfHour(minute)
        return formatter.format(date.toDate())
    }
}