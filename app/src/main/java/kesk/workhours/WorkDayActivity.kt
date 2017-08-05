package kesk.workhours

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.DatePicker
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_work_day.*
import java.text.SimpleDateFormat
import java.util.*

class WorkDayActivity : AppCompatActivity(),
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    val dateFormater = SimpleDateFormat("yyyy-MM-dd")
    val timeFormater = SimpleDateFormat("HH:mm")

    var workDayStart = Calendar.getInstance()
    var workDayEnd: Calendar? = null

    var lunchStart: Calendar? = null
    var lunchEnd: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_day)

        datePickButton.text = dateFormater.format(workDayStart.time)
        datePickButton.setOnClickListener {
            DatePickerFragment().show(fragmentManager, "datePicker")
        }

        workDayStartButton.text = timeFormater.format(workDayStart.time)
        workDayStartButton.setOnClickListener {
            TimePickerFragment().show(fragmentManager, "timePicker")
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        workDayStart.set(year, month, day)
        datePickButton.text = dateFormater.format(workDayStart.time)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        workDayStart.set(Calendar.HOUR_OF_DAY, hourOfDay)
        workDayStart.set(Calendar.MINUTE, minute)
        workDayStart.set(Calendar.SECOND, 0)
        workDayStartButton.text = timeFormater.format(workDayStart.time)
    }

}

class DatePickerFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mainActivity = activity as WorkDayActivity
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(mainActivity, mainActivity, year, month, day)
    }
}

class TimePickerFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mainActivity = activity as WorkDayActivity
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val second = cal.get(Calendar.SECOND)

        return TimePickerDialog(mainActivity, mainActivity, hour, second, true)
    }
}
