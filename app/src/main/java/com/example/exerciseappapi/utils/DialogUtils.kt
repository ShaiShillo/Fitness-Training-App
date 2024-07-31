package com.example.exerciseappapi.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.exerciseappapi.R

object DialogUtils {
    fun showDeleteConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onCancel()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .show()
    }
}