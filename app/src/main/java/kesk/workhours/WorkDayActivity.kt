package kesk.workhours

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateFormat.getDateFormat
import android.text.format.DateFormat.getTimeFormat
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import kesk.workhours.model.Date
import kesk.workhours.model.Time
import kesk.workhours.model.WorkDay
import kesk.workhours.pickers.DatePickedListener
import kesk.workhours.pickers.DatePickerFragment
import kesk.workhours.pickers.TimePickedListener
import kesk.workhours.pickers.TimePickerFragment
import kotlinx.android.synthetic.main.activity_work_day.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import net.danlew.android.joda.JodaTimeAndroid
import org.jetbrains.anko.coroutines.experimental.asReference
import org.jetbrains.anko.coroutines.experimental.bg
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormat


class WorkDayActivity : AppCompatActivity(),
        DatePickedListener,
        TimePickedListener {

    private val LOG_TAG = "WorkDayActivity"

    companion object {
        const val PREFS_NAME = "WorkDayPreferences"
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1002
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1003
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1004
    }

    private val WORK_DAY_DATE = "workDayDate"
    private val WORK_DAY_START = "workDayStart"
    private val WORK_DAY_END = "wordDayEnd"
    private val LUNCH_START = "lunchStart"
    private val LUNCH_END = "lunchEnd"
    private val SCOPES = arrayListOf(SheetsScopes.SPREADSHEETS)

    private val SHARED_PREF_CURRENT_WORKDAY = "currentWorkDay"

    private val PREF_ACCOUNT_NAME = "googleAccountName"

    private var credential: GoogleAccountCredential? = null

    private var workDayDate: Date? = null
    private var workDayStart: Time? = null
    private var workDayEnd: Time? = null
    private var lunchStart: Time? = null
    private var lunchEnd: Time? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JodaTimeAndroid.init(this)
        setContentView(R.layout.activity_work_day)

        credential = GoogleAccountCredential.usingOAuth2(applicationContext, SCOPES)
                .setBackOff(ExponentialBackOff())

        restoreState()

        if (workDayDate == null) {
            onDatePicked(WORK_DAY_DATE, Date.now())
        }

        if (workDayStart == null) {
            onTimePicked(WORK_DAY_START, Time.now())
        }

        workDateButton.setOnClickListener {
            DatePickerFragment.create(WORK_DAY_DATE)
                    .show(fragmentManager, "datePicker")
        }

        workDayStartButton.setOnClickListener {
            TimePickerFragment.create(WORK_DAY_START)
                    .show(fragmentManager, "timePicker")
        }

        workDayEndButton.setOnClickListener {
            TimePickerFragment.create(WORK_DAY_END)
                    .show(fragmentManager, "timePicker")
        }

        lunchStartButton.setOnClickListener {
            TimePickerFragment.create(LUNCH_START)
                    .show(fragmentManager, "timePicker")
        }

        lunchEndButton.setOnClickListener {
            TimePickerFragment.create(LUNCH_END)
                    .show(fragmentManager, "timePicker")
        }

        submitButton.setOnClickListener {
            getResultsFromApi()
        }

        resetButton.setOnClickListener { resetWordDay() }
    }

    override fun onDatePicked(id: String, pickedDate: Date) {
        val dateFormat = getDateFormat(this)
        workDayDate = pickedDate
        workDateButton.text = pickedDate.format(dateFormat)
    }

    override fun onTimePicked(id: String, pickedTime: Time) {
        val timeFormat = getTimeFormat(this)
        when (id) {
            WORK_DAY_START -> {
                workDayStart = pickedTime
                workDayStartButton.text = pickedTime.format(timeFormat)
            }

            WORK_DAY_END -> {
                workDayEnd = pickedTime
                workDayEndButton.text = pickedTime.format(timeFormat)
            }

            LUNCH_START -> {
                lunchStart = pickedTime
                lunchStartButton.text = pickedTime.format(timeFormat)
            }

            LUNCH_END -> {
                lunchEnd = pickedTime
                lunchEndButton.text = pickedTime.format(timeFormat)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        val preferences = getSharedPreferences(SHARED_PREF_CURRENT_WORKDAY, Context.MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putDate(WORK_DAY_DATE, workDayDate)
        editor.putTime(WORK_DAY_START, workDayStart)
        editor.putTime(WORK_DAY_END, workDayEnd)
        editor.putTime(LUNCH_START, lunchStart)
        editor.putTime(LUNCH_END, lunchEnd)
        editor.commit()
    }

    private fun restoreState() {
        val preferences = getSharedPreferences(SHARED_PREF_CURRENT_WORKDAY, Context.MODE_PRIVATE)

        preferences.getDate(WORK_DAY_DATE)?.let {
            onDatePicked(WORK_DAY_DATE, it)
        }

        preferences?.getTime(WORK_DAY_START)?.let {
            onTimePicked(WORK_DAY_START, it)
        }

        preferences?.getTime(WORK_DAY_END)?.let {
            onTimePicked(WORK_DAY_END, it)
        }

        preferences?.getTime(LUNCH_START)?.let {
            onTimePicked(LUNCH_START, it)
        }

        preferences?.getTime(LUNCH_END)?.let {
            onTimePicked(LUNCH_END, it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES ->
                if (resultCode != RESULT_OK) {
                    Log.d(LOG_TAG, "Did not get permissions")
                } else {
                    getResultsFromApi()
                }

            REQUEST_ACCOUNT_PICKER ->
                if (resultCode == RESULT_OK && data != null &&
                        data.extras != null) {
                    data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)?.let { accountName ->
                        val settings = getPreferences(Context.MODE_PRIVATE)
                        val editor = settings.edit()
                        editor.putString(PREF_ACCOUNT_NAME, accountName)
                        editor.apply()
                        credential?.selectedAccountName = accountName
                        getResultsFromApi()
                    }
                }

            REQUEST_AUTHORIZATION ->
                if (resultCode == RESULT_OK) {
                    getResultsFromApi()
                }
        }
    }

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (credential?.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            // Show not online text
        } else {
            val credential = credential
            val workDay = WorkDay.orNull(workDayDate, workDayStart, workDayEnd, lunchStart, lunchEnd)

            if (credential != null && workDay != null) {
                submitToGoogleSheets(workDay, credential)
            }
        }
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                credential?.selectedAccountName = accountName
                getResultsFromApi()
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        credential?.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    private fun resetWordDay() {
        val preferences = getSharedPreferences(SHARED_PREF_CURRENT_WORKDAY, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.removeTime(WORK_DAY_END)
        editor.removeTime(LUNCH_START)
        editor.removeTime(LUNCH_END)
        editor.apply()

        workDayEnd = null
        lunchStart = null
        lunchEnd = null

        onDatePicked(WORK_DAY_DATE, Date.now())
        onTimePicked(WORK_DAY_START, Time.now())
        workDayEndButton.text = "Time"
        lunchStartButton.text = "Start"
        lunchEndButton.text = "End"
    }

    private fun submitToGoogleSheets(workDay: WorkDay, credential: GoogleAccountCredential) {
        val spreadsheetId = "1f0Q2GDFvoJKlXm0sdxEZlLHJTapMLW2l-gjqnLQfFzw"
        val range = "Blad1!A:E"
        val workDayActivity = this.asReference()
        submitButton.isEnabled = false

        async(UI) {
            val dateFormat = getDateFormat(workDayActivity())
            val timeFormat = getTimeFormat(workDayActivity())

            bg {
                appendWorkDayToSheet(credential, spreadsheetId, range, workDay, dateFormat, timeFormat)
            }.await()
            workDayActivity().submitButton.isEnabled = true
        }
    }

    private fun appendWorkDayToSheet(credential: GoogleAccountCredential,
                                     spreadsheetId: String,
                                     range: String,
                                     workDay: WorkDay,
                                     dateFormat: DateFormat,
                                     timeFormat: DateFormat): Boolean {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val service = com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Sheets API Android Quickstart")
                .build()

        val values: List<List<String>> = listOf(listOf(
                workDay.date.format(dateFormat).toString(),
                workDay.start.format(timeFormat).toString(),
                workDay.end.format(timeFormat).toString(),
                workDay.lunchStart.format(timeFormat).toString(),
                workDay.lunchEnd.format(timeFormat).toString()
        ))

        val data = ValueRange().setValues(values)

        val result = service.spreadsheets().values()
                .append(spreadsheetId, range, data)
                .setValueInputOption("RAW")
                .execute()

        return result.updates != null
    }
}
