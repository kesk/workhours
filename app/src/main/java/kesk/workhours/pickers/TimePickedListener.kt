package kesk.workhours.pickers

interface TimePickedListener {
    fun onTimePicked(id: Int, hourOfDay: Int, minute: Int)
}