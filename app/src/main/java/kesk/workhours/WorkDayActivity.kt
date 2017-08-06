package kesk.workhours

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kesk.workhours.pickers.DatePickedListener
import kesk.workhours.pickers.DatePickerFragment
import kesk.workhours.pickers.TimePickedListener
import kesk.workhours.pickers.TimePickerFragment
import kotlinx.android.synthetic.main.activity_work_day.*
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime
import java.text.DateFormat

class WorkDayActivity : AppCompatActivity(),
        DatePickedListener,
        TimePickedListener {

    val WORK_DAY_DATE_PICKER = 1
    val WORK_DAY_START_PICKER = 2
    val LUNCH_START_PICKER = 3
    val LUNCH_END_PICKER = 4
    val WORK_DAY_END_PICKER = 5

    var workDayDate: WorkDayDate? = null
    var workDayStartTime: Time? = null
    var workDayEndTime: Time? = null
    var lunchStart: Time? = null
    var lunchEnd: Time? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JodaTimeAndroid.init(this)
        setContentView(R.layout.activity_work_day)

        restoreState(savedInstanceState)

        datePickButton.setOnClickListener {
            DatePickerFragment.create(WORK_DAY_DATE_PICKER)
                    .show(fragmentManager, "datePicker")
        }

        workDayStartButton.setOnClickListener {
            TimePickerFragment.create(WORK_DAY_START_PICKER)
                    .show(fragmentManager, "timePicker")
        }

        workDayEndButton.setOnClickListener {
            TimePickerFragment.create(WORK_DAY_END_PICKER)
                    .show(fragmentManager, "timePicker")
        }

        lunchStartButton.setOnClickListener {
            TimePickerFragment.create(LUNCH_START_PICKER)
                    .show(fragmentManager, "timePicker")
        }

        lunchEndButton.setOnClickListener {
            TimePickerFragment.create(LUNCH_END_PICKER)
                    .show(fragmentManager, "timePicker")
        }
    }

    override fun onDatePicked(id: Int, year: Int, month: Int, day: Int) {
        val dateFormat = android.text.format.DateFormat.getDateFormat(this)
        val newDate = WorkDayDate(year, month, day)
        workDayDate = newDate
        datePickButton.text = newDate.format(dateFormat)
    }

    override fun onTimePicked(id: Int, hourOfDay: Int, minute: Int) {
        val timeFormat = android.text.format.DateFormat.getTimeFormat(this)
        val pickedTime = Time(hourOfDay, minute)

        when (id) {
            WORK_DAY_START_PICKER -> {
                workDayStartTime = pickedTime
                workDayStartButton.text = pickedTime.format(timeFormat)
            }

            WORK_DAY_END_PICKER -> {
                workDayEndTime = pickedTime
                workDayEndButton.text = pickedTime.format(timeFormat)
            }

            LUNCH_START_PICKER -> {
                lunchStart = pickedTime
                lunchStartButton.text = pickedTime.format(timeFormat)
            }

            LUNCH_END_PICKER -> {
                lunchEnd = pickedTime
                lunchEndButton.text = pickedTime.format(timeFormat)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val bundle = Bundle()

        bundle.putIntArray("work_day_date", workDayDate?.toArray())
        bundle.putIntArray("work_day_start_time", workDayStartTime?.toArray())
        bundle.putIntArray("work_day_end_time", workDayEndTime?.toArray())
        bundle.putIntArray("lunch_start_time", lunchStart?.toArray())
        bundle.putIntArray("lunch_end_time", lunchEnd?.toArray())
        outState?.putAll(bundle)
    }

    private fun restoreState(bundle: Bundle?) {
        try {
            bundle?.getIntArray("work_day_date")?.let {
                onDatePicked(WORK_DAY_DATE_PICKER, it[0], it[1], it[2])
            }

            bundle?.getIntArray("work_day_start_time")?.let {
                onTimePicked(WORK_DAY_START_PICKER, it[0], it[1])
            }

            bundle?.getIntArray("work_day_end_time")?.let {
                onTimePicked(WORK_DAY_END_PICKER, it[0], it[1])
            }

            bundle?.getIntArray("lunch_start_time")?.let {
                onTimePicked(LUNCH_START_PICKER, it[0], it[1])
            }

            bundle?.getIntArray("lunch_end_time")?.let {
                onTimePicked(LUNCH_END_PICKER, it[0], it[1])
            }
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("Could not restore state", e)
        }
    }
}

data class WorkDayDate(val year: Int, val month: Int, val day: Int) {
    fun format(formatter: DateFormat): CharSequence {
        val date = DateTime(year, month, day, 0, 0)
        return formatter.format(date.toDate())
    }

    fun toArray(): IntArray {
        return intArrayOf(year, month, day)
    }
}

data class Time(val hour: Int, val minute: Int) {
    fun format(formatter: DateFormat): CharSequence {
        val date = DateTime.now().withHourOfDay(hour).withMinuteOfHour(minute)
        return formatter.format(date.toDate())
    }

    fun toArray(): IntArray {
        return intArrayOf(hour, minute)
    }
}
