package kesk.workhours.pickers

import kesk.workhours.model.Date

interface DatePickedListener {
    fun onDatePicked(id: String, pickedDate: Date)
}