package kesk.workhours

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateFormat.getDateFormat
import android.text.format.DateFormat.getTimeFormat
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import kesk.workhours.model.Date
import kesk.workhours.model.Time
import kesk.workhours.pickers.DatePickedListener
import kesk.workhours.pickers.DatePickerFragment
import kesk.workhours.pickers.TimePickedListener
import kesk.workhours.pickers.TimePickerFragment
import kotlinx.android.synthetic.main.activity_work_day.*
import net.danlew.android.joda.JodaTimeAndroid
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormat


class WorkDayActivity : AppCompatActivity(),
        DatePickedListener,
        TimePickedListener {

    private val LOG_TAG = "WorkDayActivity"

    companion object {
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
    private val PREF_ACCOUNT_NAME = "accountName"

    var credential: GoogleAccountCredential? = null

    var workDayDate: Date? = null
    var workDayStart: Time? = null
    var workDayEnd: Time? = null
    var lunchStart: Time? = null
    var lunchEnd: Time? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JodaTimeAndroid.init(this)
        setContentView(R.layout.activity_work_day)

        credential = GoogleAccountCredential.usingOAuth2(applicationContext, SCOPES)
                .setBackOff(ExponentialBackOff())

        restoreState(savedInstanceState)

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
        super.onSaveInstanceState(outState)
        val bundle = Bundle()

        bundle.putDate(WORK_DAY_DATE, workDayDate)
        bundle.putTime(WORK_DAY_START, workDayStart)
        bundle.putTime(WORK_DAY_END, workDayEnd)
        bundle.putTime(LUNCH_START, lunchStart)
        bundle.putTime(LUNCH_END, lunchEnd)
        outState?.putAll(bundle)
    }

    private fun restoreState(bundle: Bundle?) {
        bundle?.getDate(WORK_DAY_DATE)?.let {
            onDatePicked(WORK_DAY_DATE, it)
        }

        bundle?.getTime(WORK_DAY_START)?.let {
            onTimePicked(WORK_DAY_START, it)
        }

        bundle?.getTime(WORK_DAY_END)?.let {
            onTimePicked(WORK_DAY_END, it)
        }

        bundle?.getTime(LUNCH_START)?.let {
            onTimePicked(LUNCH_START, it)
        }

        bundle?.getTime(LUNCH_END)?.let {
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
            val date = workDayDate
            val dayStart = workDayStart
            val dayEnd = workDayEnd
            val lunchStart = lunchStart
            val lunchEnd = lunchEnd

            if (credential != null && date != null && dayStart != null && dayEnd != null &&
                    lunchStart != null && lunchEnd != null) {
                val dateFormat = getDateFormat(this)
                val timeFormat = getTimeFormat(this)
                MakeRequestTask(credential, date, dayStart, dayEnd, lunchStart, lunchEnd, dateFormat, timeFormat).execute()
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

    private inner class MakeRequestTask(credential: GoogleAccountCredential,
                                        val workDayDate: Date,
                                        val workDayStart: Time,
                                        val workDayEnd: Time,
                                        val lunchStart: Time,
                                        val lunchEnd: Time,
                                        val dateFormat: DateFormat,
                                        val timeFormat: DateFormat): AsyncTask<Unit, Unit, Unit>() {
        private val LOG_TAG = "AppendDataTask"

        private var service: com.google.api.services.sheets.v4.Sheets
        private var lastError: Exception? = null

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            service = com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build()
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        override fun doInBackground(vararg params: Unit) {
            try {
                appendData()
            } catch (e: Exception) {
                lastError = e
                cancel(true)
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * *
         * @throws IOException
         */
        private fun appendData() {
            val spreadsheetId = "1f0Q2GDFvoJKlXm0sdxEZlLHJTapMLW2l-gjqnLQfFzw"
            val range = "Blad1!A:E"

            val values: List<List<String>> = listOf(listOf(
                    workDayDate.format(dateFormat).toString(),
                    workDayStart.format(timeFormat).toString(),
                    workDayEnd.format(timeFormat).toString(),
                    lunchStart.format(timeFormat).toString(),
                    lunchEnd.format(timeFormat).toString()
            ))

            val data = ValueRange().setValues(values)

            this.service.spreadsheets().values()
                    .append(spreadsheetId, range, data)
                    .setValueInputOption("RAW")
                    .execute()
        }

        override fun onPreExecute() {
            submitButton.isEnabled = false
        }

        override fun onPostExecute(output: Unit?) {
            submitButton.isEnabled = true
            Log.d(LOG_TAG, "Done calling Sheets API")
        }

        override fun onCancelled() {
            submitButton.isEnabled = true
            if (lastError != null) {
                when (lastError) {
                    is GooglePlayServicesAvailabilityIOException ->
                        showGooglePlayServicesAvailabilityErrorDialog(
                                (lastError as GooglePlayServicesAvailabilityIOException)
                                        .connectionStatusCode)

                    is UserRecoverableAuthIOException ->
                        startActivityForResult(
                                (lastError as UserRecoverableAuthIOException).intent,
                                WorkDayActivity.REQUEST_AUTHORIZATION)

                    else ->
                        Log.d(LOG_TAG,"The following error occurred:\n" + lastError?.message)
                }
            } else {
                Log.d(LOG_TAG,"Request cancelled.")
            }
        }
    }
}
