package kesk.workhours.pickers

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import kesk.workhours.model.Date
import java.util.*

class DatePickerFragment : DialogFragment(),
        DatePickerDialog.OnDateSetListener {

    var listener: DatePickedListener? = null
    var id: Int? = null

    companion object {
        fun create(id: Int): DatePickerFragment {
            val bundle = Bundle()
            bundle.putInt("picker_id", id)
            val fragment = DatePickerFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        id = arguments.getInt("picker_id")

        return DatePickerDialog(activity, this, year, month, dayOfMonth)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        listener = context as DatePickedListener
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        id?.let { listener?.onDatePicked(it, Date(year, month, dayOfMonth)) }
    }
}
