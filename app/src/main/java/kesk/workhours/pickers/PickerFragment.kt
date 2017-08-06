package kesk.workhours.pickers

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle

abstract class PickerFragment : DialogFragment() {
    var id: String? = null

    companion object {
        fun create(id: String, fragment: PickerFragment): PickerFragment {
            val bundle = Bundle()
            bundle.putString("picker_id", id)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        id = arguments.getString("picker_id")

        return onCreatePickerDialog(savedInstanceState)
    }

    abstract fun onCreatePickerDialog(savedInstanceState: Bundle?): Dialog
}