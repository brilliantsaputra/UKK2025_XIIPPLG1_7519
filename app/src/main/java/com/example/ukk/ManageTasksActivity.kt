package com.example.ukk

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ukk.DatabaseHelper


class ManageTasksActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listViewTasks: ListView
    private lateinit var taskAdapter: ArrayAdapter<String>
    private var userId: Int = -1
    private var taskList = mutableListOf<Pair<Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_tasks)

        dbHelper = DatabaseHelper(this)
        listViewTasks = findViewById(R.id.listViewTasks)
        userId = intent.getIntExtra("USER_ID", -1)

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

        taskAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskList.map { it.second })
        listViewTasks.adapter = taskAdapter
    }

    private fun showTaskDialog(taskId: Int?) {
        val dialogView = layoutInflater.inflate(R.layout.activity_dialog_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val etDesc = dialogView.findViewById<EditText>(R.id.etTaskDescription)

        if (taskId != null) {
            val cursor = dbHelper.getAllTasks()
            if (cursor.moveToFirst()) {
                etTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")))
                etDesc.setText(cursor.getString(cursor.getColumnIndexOrThrow("description")))
            }
            cursor.close()
        }

        AlertDialog.Builder(this)
            .setTitle(if (taskId == null) "Tambah Tugas" else "Edit Tugas")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val title = etTitle.text.toString()
                val description = etDesc.text.toString()

                if (title.isEmpty()) {
                    Toast.makeText(this, "Judul tugas tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (taskId == null) {
                    dbHelper.addTask(title, description, null, 1)
                } else {
                    dbHelper.updateTask(taskId, title, description, null, 1)
                }

                loadTasks()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun editTask() {
        val selectedPos = listViewTasks.checkedItemPosition
        if (selectedPos == ListView.INVALID_POSITION) {
            Toast.makeText(this, "Pilih tugas yang ingin diedit!", Toast.LENGTH_SHORT).show()
            return
        }

        val taskId = taskList[selectedPos].first
        showTaskDialog(taskId)
    }

    private fun deleteTask() {
        val selectedPos = listViewTasks.checkedItemPosition
        if (selectedPos == ListView.INVALID_POSITION) {
            Toast.makeText(this, "Pilih tugas yang ingin dihapus!", Toast.LENGTH_SHORT).show()
            return
        }

        val taskId = taskList[selectedPos].first
        dbHelper.deleteTask(taskId)
        loadTasks()
    }

    private fun markTaskComplete() {
        val selectedPos = listViewTasks.checkedItemPosition
        if (selectedPos == ListView.INVALID_POSITION) {
            Toast.makeText(this, "Pilih tugas yang ingin ditandai selesai!", Toast.LENGTH_SHORT).show()
            return
        }

        val taskId = taskList[selectedPos].first
        dbHelper.setTaskCompleted(taskId, true)
        loadTasks()
    }


}