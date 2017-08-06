package kesk.workhours.pickers

import android.app.Dialog
import android.app.DialogFragment
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import java.util.*

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

        listener = context as TimePickedListener
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        id?.let { listener?.onTimePicked(it, hourOfDay, minute) }
    }
}