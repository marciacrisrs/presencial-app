package com.presencial.app.presentation.settings

sealed class PendingRestore {
    data object Folder : PendingRestore()
    data class File(val file: java.io.File) : PendingRestore()
}
