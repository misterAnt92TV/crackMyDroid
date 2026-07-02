package com.misterant92tv.crackmydroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.misterant92tv.crackmydroid.databinding.ActivityAppDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Shows the full [SecurityReport] for a single application.
 */
class AppDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
            ?: run { finish(); return }

        loadReport(packageName)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadReport(packageName: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollContent.visibility = View.GONE

        lifecycleScope.launch {
            val report = withContext(Dispatchers.IO) {
                try {
                    AppSecurityChecker(packageManager).analyse(packageName)
                } catch (_: Exception) {
                    null
                }
            }

            binding.progressBar.visibility = View.GONE

            if (report == null) {
                binding.textFindings.text = getString(R.string.error_loading_report)
                binding.scrollContent.visibility = View.VISIBLE
                return@launch
            }

            binding.scrollContent.visibility = View.VISIBLE
            populateUi(report)
        }
    }

    private fun populateUi(report: SecurityReport) {
        supportActionBar?.title = report.appName

        val icon = try {
            packageManager.getApplicationIcon(report.packageName)
        } catch (_: Exception) {
            null
        }
        binding.imageAppIcon.setImageDrawable(icon)

        binding.textAppName.text = report.appName
        binding.textPackageName.text = report.packageName
        binding.textVersion.text = getString(
            R.string.version_format,
            report.versionName ?: "-",
            report.versionCode
        )
        binding.textMinSdk.text = getString(R.string.min_sdk_format, report.minSdkVersion)
        binding.textSystemApp.text = getString(
            R.string.system_app_format,
            if (report.isSystemApp) getString(R.string.yes) else getString(R.string.no)
        )

        val riskColorRes = when (report.riskLevel) {
            SecurityReport.RiskLevel.HIGH -> R.color.risk_high_bg
            SecurityReport.RiskLevel.MEDIUM -> R.color.risk_medium_bg
            SecurityReport.RiskLevel.LOW -> R.color.risk_low_bg
        }
        binding.textRiskScore.text = getString(
            R.string.risk_score_detail_format,
            report.riskScore,
            report.riskLevel.name
        )
        binding.textRiskScore.setBackgroundColor(getColor(riskColorRes))

        binding.textFindings.text = report.findings().joinToString("\n\n")
    }

    companion object {
        private const val EXTRA_PACKAGE_NAME = "extra_package_name"

        fun createIntent(context: Context, packageName: String): Intent =
            Intent(context, AppDetailActivity::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
    }
}
