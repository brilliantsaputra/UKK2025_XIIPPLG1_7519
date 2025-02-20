package com.example.ukk

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class RiwayatTugasActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listViewHistory: ListView
    private lateinit var historyAdapter: ArrayAdapter<String>
    private var historyList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_tugas)

        dbHelper = DatabaseHelper(this)
        listViewHistory = findViewById(R.id.listViewHistory)


        val btnSelectDateRange = findViewById<Button>(R.id.btnSelectDateRange)
        btnSelectDateRange.setOnClickListener {
            showDateRangeDialog()
        }

        loadHistory(0, Long.MAX_VALUE)
    }

    private fun showDateRangeDialog() {

        val calendarStart = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendarStart.set(year, month, dayOfMonth)


            val calendarEnd = Calendar.getInstance()
            DatePickerDialog(this, { _, yearEnd, monthEnd, dayEnd ->
                calendarEnd.set(yearEnd, monthEnd, dayEnd)


                val startDate = calendarStart.timeInMillis
                val endDate = calendarEnd.timeInMillis


                if (startDate > endDate) {

                    val temp = startDate

                    loadHistory(endDate, temp)
                } else {
                    loadHistory(startDate, endDate)
                }
            }, calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH), calendarEnd.get(Calendar.DAY_OF_MONTH)).show()

        }, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadHistory(startDate: Long, endDate: Long) {
        val cursor = dbHelper.getCompletedTasksByDateRange(startDate, endDate)
        historyList.clear()

        val sdf = SimpleDateFormat("dd/MM/yyyy (EEEE)", Locale.getDefault())

        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val dueDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("due_date"))

                val dateText = if (dueDateMillis > 0) {
                    sdf.format(Date(dueDateMillis))
                } else {
                    "Tidak ada tanggal"
                }


                val displayText = "$title - $description\nSelesai pada: $dateText"
                historyList.add(displayText)
            } while (cursor.moveToNext())
        }
        cursor.close()

        historyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historyList)
        listViewHistory.adapter = historyAdapter
    }
}
