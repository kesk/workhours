package kesk.workhours

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.DatePicker
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_work_day.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class WorkDayActivity : AppCompatActivity(),
        DatePickerDialog.OnDateSetListener,
        TimePickedListener {

    val WORK_DAY_DATE_PICKER = 1
    val WORK_DAY_START_PICKER = 2
    val LUNCH_START_PICKER = 3
    val LUNCH_END_PICKER = 4
    val WORK_DAY_END_PICKER = 5

    val dateFormater = SimpleDateFormat("yyyy-MM-dd")
    val timeFormater = SimpleDateFormat("HH:mm")

    val workDayStart = Calendar.getInstance()
    var workDayEnd: Calendar? = null

    var lunchStart: Calendar? = null
    var lunchEnd: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_day)

        datePickButton.text = dateFormater.format(workDayStart.time)
        datePickButton.setOnClickListener {
            //DatePickerFragment().show(fragmentManager, "datePicker")
        }

        workDayStartButton.text = timeFormater.format(workDayStart.time)
        workDayStartButton.setOnClickListener {
            TimePickerFragment.create(WORK_DAY_START_PICKER)
                    .show(fragmentManager, "timePicker")
        }

        lunchStartButton.setOnClickListener {
            TimePickerFragment.create(LUNCH_START_PICKER)
                    .show(fragmentManager, "timePicker")
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        workDayStart.set(year, month, day)
        datePickButton.text = dateFormater.format(workDayStart.time)
    }

    override fun timePicked(id: Int, hourOfDay: Int, minute: Int) {
        when (id) {
            WORK_DAY_START_PICKER -> {
                setTime(workDayStart, hourOfDay, minute)
                workDayStartButton.text = timeFormater.format(workDayStart.time)
            }

            LUNCH_START_PICKER -> {
                if (lunchStart == null) lunchStart = Calendar.getInstance()
                setTime(lunchStart, hourOfDay, minute)
                lunchStartButton.text = timeFormater.format(lunchStart?.time)
            }
        }
    }

    private fun setTime(calendar: Calendar?, hourOfDay: Int, minute: Int) {
        calendar?.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar?.set(Calendar.MINUTE, minute)
        calendar?.set(Calendar.SECOND, 0)
    }

}

class TimePickerFragment : DialogFragment(),
        TimePickerDialog.OnTimeSetListener {

    var listener: TimePickedListener? = null
    var id: Int? = null

    companion object {
        fun create(id: Int): TimePickerFragment {
            val bundle = Bundle()
            bundle.putInt("picker_id", id)
            val fragment = TimePickerFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        id = arguments.getInt("picker_id")

        return TimePickerDialog(activity, this, hourOfDay, minute, true)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        listener = context as WorkDayActivity
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        id?.let { listener?.timePicked(it, hourOfDay, minute) }
    }
}

interface TimePickedListener {
    fun timePicked(id: Int, hourOfDay: Int, minute: Int)
}
