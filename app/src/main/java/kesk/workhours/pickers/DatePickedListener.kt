package kesk.workhours.pickers

interface DatePickedListener {
    fun onDatePicked(id: Int, year: Int, month: Int, dayOfMonth: Int)
}