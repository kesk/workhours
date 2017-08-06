package kesk.workhours.pickers

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import kesk.workhours.model.Time
import org.joda.time.DateTime

class TimePickerFragment : PickerFragment(),
        TimePickerDialog.OnTimeSetListener {

    var listener: TimePickedListener? = null

    companion object {
        fun create(id: String): PickerFragment {
            return PickerFragment.create(id, TimePickerFragment())
        }
    }

    override fun onCreatePickerDialog(savedInstanceState: Bundle?): Dialog {
        val now = DateTime.now()
        listener = context as TimePickedListener
        return TimePickerDialog(activity, this, now.hourOfDay, now.minuteOfHour, true)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        id?.let { listener?.onTimePicked(it, Time(hourOfDay, minute)) }
    }
}
