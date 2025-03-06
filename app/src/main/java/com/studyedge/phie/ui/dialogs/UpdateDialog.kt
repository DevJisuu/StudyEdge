package com.studyedge.phie.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import com.studyedge.phie.R
import com.studyedge.phie.databinding.DialogUpdateBinding
import com.studyedge.phie.data.model.ForceUpdate
import com.studyedge.phie.data.model.Version

class UpdateDialog(
    context: Context,
    private val forceUpdate: ForceUpdate,
    private val versions: List<Version>
) : Dialog(context, R.style.ThemeOverlay_StudyEdge_MaterialDialog) {

    private lateinit var binding: DialogUpdateBinding
    private val latestVersion = versions.maxByOrNull { it.version }?.version

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogUpdateBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(!forceUpdate.enabled)
        setCancelable(!forceUpdate.enabled)

        // Check if update is needed
        if (!shouldShowUpdate()) {
            dismiss()
            return
        }

        setupDialog()
    }

    private fun setupDialog() {
        binding.apply {
            tvUpdateMessage.text = forceUpdate.message
            
            btnUpdate.setOnClickListener {
                try {
                    val updateUrl = forceUpdate.update_url.takeIf { it.isNotBlank() }
                        ?: throw IllegalStateException("Update URL is empty")
                    
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl)))
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Unable to open update link. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            // Show/Hide Later button based on version checks
            btnLater.apply {
                visibility = if (shouldShowLaterButton()) View.VISIBLE else View.GONE
                setOnClickListener {
                    try {
                        cancel()
                    } catch (e: Exception) {
                        try {
                            dismiss()
                        } catch (e2: Exception) {
                            hide()
                        }
                    }
                }
            }
        }
    }

    private fun shouldShowLaterButton(): Boolean {
        try {
            val currentVersion = context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                ?: return false

            // If below minimum version, never show later button
            val minVersion = forceUpdate.min_version.takeIf { it.isNotBlank() }
                ?: return true // If no min version specified, show later button
                
            val normalizedCurrentVersion = normalizeVersion(currentVersion)
            val normalizedMinVersion = normalizeVersion(minVersion)
            
            if (compareVersions(normalizedCurrentVersion, normalizedMinVersion) < 0) {
                return false
            }
            
            // If above minimum version but not latest, follow force update setting
            return !forceUpdate.enabled
            
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return false
        }
    }

    private fun shouldShowUpdate(): Boolean {
        try {
            val currentVersion = context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                ?: return false

            // Normalize current version to match API format (e.g., "1.0" to "1.0.0")
            val normalizedCurrentVersion = normalizeVersion(currentVersion)
            
            // ALWAYS check latest version first - if user has latest version, never show dialog
            if (latestVersion != null) {
                val normalizedLatestVersion = normalizeVersion(latestVersion)
                // If on latest version, never show dialog regardless of force update
                if (compareVersions(normalizedCurrentVersion, normalizedLatestVersion) >= 0) {
                    return false
                }
            }
            
            // If we get here, user is not on latest version
            // Check minimum version requirement
            val minVersion = forceUpdate.min_version.takeIf { it.isNotBlank() }
                ?: return false
            
            val normalizedMinVersion = normalizeVersion(minVersion)
            
            // If below minimum version, always show update
            if (compareVersions(normalizedCurrentVersion, normalizedMinVersion) < 0) {
                return true
            }
            
            // If above minimum but not latest, show only if force update is enabled
            return forceUpdate.enabled
            
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return false
        }
    }

    private fun normalizeVersion(version: String): String {
        val parts = version.split(".")
        return when (parts.size) {
            1 -> "$version.0.0"
            2 -> "$version.0"
            else -> version
        }
    }

    private fun compareVersions(version1: String?, version2: String?): Int {
        // Handle null cases
        if (version1 == null && version2 == null) return 0
        if (version1 == null) return -1
        if (version2 == null) return 1

        val parts1 = version1.split(".")
        val parts2 = version2.split(".")
        
        val maxLength = maxOf(parts1.size, parts2.size)
        
        for (i in 0 until maxLength) {
            val v1 = if (i < parts1.size) parts1[i].toIntOrNull() ?: 0 else 0
            val v2 = if (i < parts2.size) parts2[i].toIntOrNull() ?: 0 else 0
            
            when {
                v1 < v2 -> return -1
                v1 > v2 -> return 1
            }
        }
        
        return 0
    }
} 