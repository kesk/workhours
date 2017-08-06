package kesk.workhours

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kesk.workhours.model.Date
import kesk.workhours.model.Time
import kesk.workhours.pickers.DatePickedListener
import kesk.workhours.pickers.DatePickerFragment
import kesk.workhours.pickers.TimePickedListener
import kesk.workhours.pickers.TimePickerFragment
import kotlinx.android.synthetic.main.activity_work_day.*
import net.danlew.android.joda.JodaTimeAndroid

class WorkDayActivity : AppCompatActivity(),
        DatePickedListener,
        TimePickedListener {

    val WORK_DAY_DATE_PICKER = 1
    val WORK_DAY_START_PICKER = 2
    val LUNCH_START_PICKER = 3
    val LUNCH_END_PICKER = 4
    val WORK_DAY_END_PICKER = 5

    var workDayDate: Date? = null
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

    override fun onDatePicked(id: Int, pickedDate: Date) {
        val dateFormat = android.text.format.DateFormat.getDateFormat(this)
        workDayDate = pickedDate
        datePickButton.text = pickedDate.format(dateFormat)
    }

    override fun onTimePicked(id: Int, pickedTime: Time) {
        val timeFormat = android.text.format.DateFormat.getTimeFormat(this)

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

        bundle.putDate("work_day_date", workDayDate)
        bundle.putTime("work_day_start_time", workDayStartTime)
        bundle.putTime("work_day_end_time", workDayEndTime)
        bundle.putTime("lunch_start_time", lunchStart)
        bundle.putTime("lunch_end_time", lunchEnd)
        outState?.putAll(bundle)
    }

    private fun restoreState(bundle: Bundle?) {
        bundle?.getDate("work_day_date")?.let {
            onDatePicked(WORK_DAY_DATE_PICKER, it)
        }

        bundle?.getTime("work_day_start_time")?.let {
            onTimePicked(WORK_DAY_START_PICKER, it)
        }

        bundle?.getTime("work_day_end_time")?.let {
            onTimePicked(WORK_DAY_END_PICKER, it)
        }

        bundle?.getTime("lunch_start_time")?.let {
            onTimePicked(LUNCH_START_PICKER, it)
        }

        bundle?.getTime("lunch_end_time")?.let {
            onTimePicked(LUNCH_END_PICKER, it)
        }
    }
}
