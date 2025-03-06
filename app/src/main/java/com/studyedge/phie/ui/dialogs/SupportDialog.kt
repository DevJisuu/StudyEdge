package com.studyedge.phie.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.studyedge.phie.R
import com.studyedge.phie.databinding.DialogSupportBinding

class SupportDialog(
    context: Context,
    private val email: String,
    private val website: String,
    private val telegram: String
) : Dialog(context, R.style.ThemeOverlay_StudyEdge_MaterialDialog) {

    private lateinit var binding: DialogSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogSupportBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(true)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnEmail.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$email")
                    }
                    context.startActivity(Intent.createChooser(intent, "Send email"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Unable to open email app", Toast.LENGTH_SHORT).show()
                }
            }

            btnWebsite.setOnClickListener {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(website)))
                } catch (e: Exception) {
                    Toast.makeText(context, "Unable to open website", Toast.LENGTH_SHORT).show()
                }
            }

            btnTelegram.setOnClickListener {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(telegram)))
                } catch (e: Exception) {
                    Toast.makeText(context, "Unable to open Telegram", Toast.LENGTH_SHORT).show()
                }
            }

            btnClose.setOnClickListener {
                dismiss()
            }
        }
    }
} 