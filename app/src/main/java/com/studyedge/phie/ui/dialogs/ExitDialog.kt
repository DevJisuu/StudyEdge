package com.studyedge.phie.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.studyedge.phie.R
import com.studyedge.phie.databinding.DialogExitBinding

class ExitDialog(
    context: Context,
    private val onExit: () -> Unit
) : Dialog(context, R.style.ThemeOverlay_StudyEdge_MaterialDialog) {

    private lateinit var binding: DialogExitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogExitBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)

        binding.apply {
            btnExit.setOnClickListener {
                dismiss()
                onExit()
            }
            btnCancel.setOnClickListener { dismiss() }
        }
    }
} 