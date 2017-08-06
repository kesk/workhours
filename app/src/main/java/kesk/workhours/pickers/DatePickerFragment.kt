package kesk.workhours.pickers

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import kesk.workhours.model.Date
import org.joda.time.DateTime

class DatePickerFragment : PickerFragment(),
        DatePickerDialog.OnDateSetListener {

    var listener: DatePickedListener? = null

    companion object {
        fun create(id: String): PickerFragment {
            return PickerFragment.create(id, DatePickerFragment())
        }
    }

    override fun onCreatePickerDialog(savedInstanceState: Bundle?): Dialog {
        val now = DateTime.now()
        listener = context as DatePickedListener

        return DatePickerDialog(activity, this, now.year, now.monthOfYear, now.dayOfMonth)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        id?.let { listener?.onDatePicked(it, Date(year, month, dayOfMonth)) }
    }
}
