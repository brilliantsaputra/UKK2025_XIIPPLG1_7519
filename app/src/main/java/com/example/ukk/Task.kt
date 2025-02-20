package com.example.ukk

data class Task(
    val id: Int,
    val title: String,
    val description: String?,
    val dueDate: Long?,
    val completed: Boolean,
    val categoryId: Int

)