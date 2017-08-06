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

    val WORK_DAY_DATE = "workDayDate"
    val WORK_DAY_START = "workDayStart"
    val WORK_DAY_END = "wordDayEnd"
    val LUNCH_START = "lunchStart"
    val LUNCH_END = "lunchEnd"

    var workDayDate: Date? = null
    var workDayStart: Time? = null
    var workDayEnd: Time? = null
    var lunchStart: Time? = null
    var lunchEnd: Time? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JodaTimeAndroid.init(this)
        setContentView(R.layout.activity_work_day)

        restoreState(savedInstanceState)

        if (workDayDate == null) {
            onDatePicked(WORK_DAY_DATE, Date.now())
        }

        if (workDayStart == null) {
            onTimePicked(WORK_DAY_START, Time.now())
        }

        workDateButton.setOnClickListener {
            DatePickerFragment.create(WORK_DAY_DATE)
                    .show(fragmentManager, "datePicker")
        }

        workDayStartButton.setOnClickListener {
            TimePickerFragment.create(WORK_DAY_START)
                    .show(fragmentManager, "timePicker")
        }

        workDayEndButton.setOnClickListener {
            TimePickerFragment.create(WORK_DAY_END)
                    .show(fragmentManager, "timePicker")
        }

        lunchStartButton.setOnClickListener {
            TimePickerFragment.create(LUNCH_START)
                    .show(fragmentManager, "timePicker")
        }

        lunchEndButton.setOnClickListener {
            TimePickerFragment.create(LUNCH_END)
                    .show(fragmentManager, "timePicker")
        }
    }

    override fun onDatePicked(id: String, pickedDate: Date) {
        val dateFormat = android.text.format.DateFormat.getDateFormat(this)
        workDayDate = pickedDate
        workDateButton.text = pickedDate.format(dateFormat)
    }

    override fun onTimePicked(id: String, pickedTime: Time) {
        val timeFormat = android.text.format.DateFormat.getTimeFormat(this)

        when (id) {
            WORK_DAY_START -> {
                workDayStart = pickedTime
                workDayStartButton.text = pickedTime.format(timeFormat)
            }

            WORK_DAY_END -> {
                workDayEnd = pickedTime
                workDayEndButton.text = pickedTime.format(timeFormat)
            }

            LUNCH_START -> {
                lunchStart = pickedTime
                lunchStartButton.text = pickedTime.format(timeFormat)
            }

            LUNCH_END -> {
                lunchEnd = pickedTime
                lunchEndButton.text = pickedTime.format(timeFormat)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val bundle = Bundle()

        bundle.putDate(WORK_DAY_DATE, workDayDate)
        bundle.putTime(WORK_DAY_START, workDayStart)
        bundle.putTime(WORK_DAY_END, workDayEnd)
        bundle.putTime(LUNCH_START, lunchStart)
        bundle.putTime(LUNCH_END, lunchEnd)
        outState?.putAll(bundle)
    }

    private fun restoreState(bundle: Bundle?) {
        bundle?.getDate(WORK_DAY_DATE)?.let {
            onDatePicked(WORK_DAY_DATE, it)
        }

        bundle?.getTime(WORK_DAY_START)?.let {
            onTimePicked(WORK_DAY_START, it)
        }

        bundle?.getTime(WORK_DAY_END)?.let {
            onTimePicked(WORK_DAY_END, it)
        }

        bundle?.getTime(LUNCH_START)?.let {
            onTimePicked(LUNCH_START, it)
        }

        bundle?.getTime(LUNCH_END)?.let {
            onTimePicked(LUNCH_END, it)
        }
    }
}
