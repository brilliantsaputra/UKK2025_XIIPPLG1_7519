package com.example.ukk

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManageTasksActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listViewTasks: ListView
    private lateinit var taskAdapter: ArrayAdapter<String>
    private var userId: Int = -1
    private var taskList = mutableListOf<Pair<Int, String>>()
    private var selectedTaskId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_tasks)

        dbHelper = DatabaseHelper(this)
        listViewTasks = findViewById(R.id.listViewTasks)
        userId = intent.getIntExtra("USER_ID", -1)
        val categoryId = intent.getIntExtra("CATEGORY_ID", -1)

        loadTasks()

        findViewById<Button>(R.id.btnAddTask).setOnClickListener { showTaskDialog(null) }



        loadTasks()

        findViewById<Button>(R.id.btnAddTask).setOnClickListener { showTaskDialog(null) }
        findViewById<Button>(R.id.btnEditTask).setOnClickListener { editTask() }
        findViewById<Button>(R.id.btnDeleteTask).setOnClickListener { deleteTask() }
        findViewById<Button>(R.id.btnMarkComplete).setOnClickListener { markTaskComplete() }
    }

    private fun loadTasks() {
        val cursor = dbHelper.getAllTasks()
        taskList.clear()

        if (cursor.moveToFirst()) {
            do {
                val taskId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1
                val displayText = if (completed) "[✓] $title" else "[ ] $title"
                taskList.add(Pair(taskId, displayText))
            } while (cursor.moveToNext())
        }
        cursor.close()
        listViewTasks.setOnItemClickListener { _, _, position, _ ->
            selectedTaskId = taskList[position].first
            Toast.makeText(this, "Tugas dipilih: ${taskList[position].second}", Toast.LENGTH_SHORT).show()
        }

        taskAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskList.map { it.second })
        listViewTasks.adapter = taskAdapter
    }

    private fun showTaskDialog(taskId: Int?) {

        val dialogView = layoutInflater.inflate(R.layout.activity_dialog_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val etDesc = dialogView.findViewById<EditText>(R.id.etTaskDescription)
        val etDueDate = dialogView.findViewById<EditText>(R.id.etDueDate)


        etDueDate.isFocusable = false
        etDueDate.isClickable = true


        var selectedDueDate: Long? = null


        etDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }

                    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val sdfDay = SimpleDateFormat("EEEE", Locale.getDefault())
                    etDueDate.setText("${sdfDate.format(selectedCalendar.time)} (${sdfDay.format(selectedCalendar.time)})")
                    selectedDueDate = selectedCalendar.timeInMillis
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }


        if (taskId != null) {
            val cursor = dbHelper.getTaskById(taskId)
            if (cursor.moveToFirst()) {
                etTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")))
                etDesc.setText(cursor.getString(cursor.getColumnIndexOrThrow("description")))
                val dueDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("due_date"))
                if (dueDateMillis > 0) {
                    val calendar = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
                    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val sdfDay = SimpleDateFormat("EEEE", Locale.getDefault())
                    etDueDate.setText("${sdfDate.format(calendar.time)} (${sdfDay.format(calendar.time)})")
                    selectedDueDate = dueDateMillis
                }
            }
            cursor.close()
        }

        AlertDialog.Builder(this)
            .setTitle(if (taskId == null) "Tambah Tugas" else "Edit Tugas")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDesc.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(this, "Judul tugas tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }


                val dueDate = selectedDueDate ?: 0L


                val categoryId = 1

                if (taskId == null) {
                    dbHelper.addTask(title, description, dueDate, categoryId)
                } else {
                    dbHelper.updateTask(taskId, title, description, dueDate, categoryId)
                }

                loadTasks()
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun editTask() {
        if (selectedTaskId == null) {
            Toast.makeText(this, "Pilih tugas yang ingin diedit!", Toast.LENGTH_SHORT).show()
            return
        }
        showTaskDialog(selectedTaskId)
    }


    private fun deleteTask() {
        if (selectedTaskId == null) {
            Toast.makeText(this, "Pilih tugas yang ingin dihapus!", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Yakin ingin menghapus tugas ini?")
            .setPositiveButton("Ya") { _, _ ->
                dbHelper.deleteTask(selectedTaskId!!)
                selectedTaskId = null
                loadTasks()
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun markTaskComplete() {
        if (selectedTaskId == null) {
            Toast.makeText(this, "Pilih tugas yang ingin ditandai selesai!", Toast.LENGTH_SHORT).show()
            return
        }

        dbHelper.setTaskCompleted(selectedTaskId!!, true)
        selectedTaskId = null
        loadTasks()
    }

}
