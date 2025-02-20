package com.example.ukk

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listViewCategories: ListView
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private var categoryList = mutableListOf<Pair<Int, String>>()
    private var selectedCategoryId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)



        dbHelper = DatabaseHelper(this)
        listViewCategories = findViewById(R.id.listViewCategories)
        listViewCategories.choiceMode = ListView.CHOICE_MODE_SINGLE

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        btnHistory.setOnClickListener {
            val intent = Intent(this, RiwayatTugasActivity::class.java)
            startActivity(intent)
        }



        loadCategories()



        findViewById<Button>(R.id.btnAddCategory).setOnClickListener { showCategoryDialog(null) }
        findViewById<Button>(R.id.btnEditCategory).setOnClickListener { editCategory() }
        findViewById<Button>(R.id.btnDeleteCategory).setOnClickListener { deleteCategory() }
        findViewById<Button>(R.id.btnMarkCategoryComplete).setOnClickListener { markCategoryComplete() }


        findViewById<Button>(R.id.btnGoToTasks).setOnClickListener {
            if (selectedCategoryId != null) {
                val intent = Intent(this, ManageTasksActivity::class.java)
                intent.putExtra("CATEGORY_ID", selectedCategoryId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Pilih kategori terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }


        listViewCategories.setOnItemClickListener { _, _, position, _ ->
            selectedCategoryId = categoryList[position].first
            Toast.makeText(this, "Kategori dipilih: ${categoryList[position].second}", Toast.LENGTH_SHORT).show()
        }

    }


    private fun loadCategories() {
        val cursor = dbHelper.getAllCategories()
        categoryList.clear()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
                val completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1
                val displayText = if (completed) "[✓] $categoryName" else "[ ] $categoryName"
                categoryList.add(Pair(id, displayText))
            } while (cursor.moveToNext())
        }
        cursor.close()

        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryList.map { it.second })
        listViewCategories.adapter = categoryAdapter
    }

    private fun showCategoryDialog(categoryId: Int?) {
        val dialogView = layoutInflater.inflate(R.layout.activity_dialog_category, null)
        val etCategoryName = dialogView.findViewById<EditText>(R.id.etCategoryName)

        if (categoryId != null) {
            val cursor = dbHelper.getCategoryById(categoryId)
            if (cursor.moveToFirst()) {
                etCategoryName.setText(cursor.getString(cursor.getColumnIndexOrThrow("category_name")))
            }
            cursor.close()
        }

        AlertDialog.Builder(this)
            .setTitle(if (categoryId == null) "Tambah Kategori" else "Edit Kategori")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val categoryName = etCategoryName.text.toString()
                if (categoryName.isNotEmpty()) {
                    if (categoryId == null) dbHelper.addCategory(categoryName)
                    else dbHelper.updateCategory(categoryId, categoryName)
                    loadCategories()
                } else {
                    Toast.makeText(this, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun editCategory() {
        if (selectedCategoryId == null) {
            Toast.makeText(this, "Pilih kategori yang ingin diedit!", Toast.LENGTH_SHORT).show()
            return
        }
        showCategoryDialog(selectedCategoryId)
    }

    private fun deleteCategory() {
        if (selectedCategoryId == null) {
            Toast.makeText(this, "Pilih kategori yang ingin dihapus!", Toast.LENGTH_SHORT).show()
            return
        }
        dbHelper.deleteCategory(selectedCategoryId!!)
        Toast.makeText(this, "Kategori dihapus", Toast.LENGTH_SHORT).show()
        selectedCategoryId = null
        loadCategories()
    }

    private fun markCategoryComplete() {
        if (selectedCategoryId == null) {
            Toast.makeText(this, "Pilih kategori yang ingin ditandai selesai!", Toast.LENGTH_SHORT).show()
            return
        }
        dbHelper.setCategoryCompleted(selectedCategoryId!!, true)
        Toast.makeText(this, "Kategori ditandai selesai", Toast.LENGTH_SHORT).show()
        loadCategories()
    }


}
