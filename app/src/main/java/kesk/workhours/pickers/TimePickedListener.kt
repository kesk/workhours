package kesk.workhours.pickers

import kesk.workhours.model.Time

interface TimePickedListener {
    fun onTimePicked(id: Int, time: Time)
}