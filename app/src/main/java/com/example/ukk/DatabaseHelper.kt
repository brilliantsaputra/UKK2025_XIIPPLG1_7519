package com.example.ukk



import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Ukk.db"
        private const val DATABASE_VERSION = 3
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                email TEXT,
                name TEXT,
                is_active INTEGER DEFAULT 1
            );
        """.trimIndent()
        db?.execSQL(createUsersTable)

        val createCategoriesTable = """
          CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_name TEXT NOT NULL UNIQUE,
                user_id INTEGER,
                completed INTEGER DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );


        """.trimIndent()

        db?.execSQL(createCategoriesTable)

        val createTasksTable = """
            CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                due_date INTEGER,
                completed INTEGER DEFAULT 0,
                category_id INTEGER,
                FOREIGN KEY (category_id) REFERENCES categories(id)
            );
        """.trimIndent()
        db?.execSQL(createTasksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }


    fun addUser(username: String, password: String, email: String?, name: String?): Long {
        val db = writableDatabase


        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", arrayOf(username))
        if (cursor.count > 0) {
            cursor.close()
            throw IllegalArgumentException("Username sudah digunakan")
        }
        cursor.close()


        if (password.length < 8 || !password.matches(Regex(".*[A-Za-z].*")) || !password.matches(Regex(".*[0-9].*"))) {
            throw IllegalArgumentException("Password harus minimal 8 karakter dan mengandung huruf serta angka")
        }

        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
            put("email", email)
            put("name", name)
        }
        return db.insert("users", null, values)
    }



    fun checkUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = 1"
        val cursor = db.rawQuery(query, arrayOf(username, password))
        val result = cursor.count > 0
        cursor.close()
        return result
    }


    fun getUserId(username: String): Int {
        val db = readableDatabase
        val query = "SELECT id FROM users WHERE username = ?"
        val cursor = db.rawQuery(query, arrayOf(username))
        val userId = if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow("id")) else -1
        cursor.close()
        return userId
    }


    fun addCategory(categoryName: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("category_Name", categoryName)



        val result = db.insert("categories", null, values)
        db.close()

        return result != -1L
    }




    fun deleteCategory(id: Int): Int {
        val db = writableDatabase
        return db.delete("categories", "id = ?", arrayOf(id.toString()))
    }

    fun getAllCategories(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM categories", null)
    }


    fun getCategoryById(id: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM categories WHERE id = ?", arrayOf(id.toString()))
    }
    fun updateCategory(id: Int, name: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", name)
        db.update("categories", values, "id = ?", arrayOf(id.toString()))
        db.close()
    }
    fun setCategoryCompleted(id: Int, completed: Boolean) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("completed", if (completed) 1 else 0)
        db.update("categories", values, "id = ?", arrayOf(id.toString()))
        db.close()
    }



    fun addTask(title: String, description: String?, dueDate: Long?, categoryId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("description", description)
            put("due_date", dueDate)
            put("category_id", categoryId)
        }
        val result = db.insert("tasks", null, values)
        db.close()
        return result
    }



    fun getAllTasks(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM tasks", null)
    }
    fun getTaskById(taskId: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM tasks WHERE id = ?", arrayOf(taskId.toString()))
    }



    fun deleteTask(taskId: Int): Int {
        val db = writableDatabase
        return db.delete("tasks", "id = ?", arrayOf(taskId.toString()))
    }


    fun updateTask(taskId: Int, title: String, description: String?, dueDate: Long?, categoryId: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("description", description)
            put("due_date", dueDate)
            put("category_id", categoryId)
        }
        return db.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }



    fun setTaskCompleted(taskId: Int, isCompleted: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("completed", if (isCompleted) 1 else 0)
        }
        return db.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }



    fun getTasksByCategory(categoryId: Int): List<Task> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tasks WHERE category_id = ?", arrayOf(categoryId.toString()))
        val tasks = mutableListOf<Task>()

        if (cursor.moveToFirst()) {
            do {
                val task = Task(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    dueDate = cursor.getLong(cursor.getColumnIndexOrThrow("due_date")),
                    completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1,
                    categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"))
                )
                tasks.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tasks
    }


}


