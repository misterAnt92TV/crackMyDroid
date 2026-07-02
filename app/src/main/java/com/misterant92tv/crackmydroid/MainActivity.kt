package com.misterant92tv.crackmydroid

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.misterant92tv.crackmydroid.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Entry point of CrackMyDroid.
 *
 * Displays a list of all user-installed applications ordered by security risk (highest first).
 * Tapping an app opens [AppDetailActivity] with a full security report.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter
    private var allReports: List<SecurityReport> = emptyList()
    private var showSystemApps = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = AppListAdapter { report ->
            startActivity(AppDetailActivity.createIntent(this, report.packageName))
        }
        binding.recyclerView.adapter = adapter

        loadApps()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterReports(newText.orEmpty())
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_system_apps -> {
                showSystemApps = !showSystemApps
                item.isChecked = showSystemApps
                loadApps()
                true
            }
            R.id.action_refresh -> {
                loadApps()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadApps() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.textEmpty.visibility = View.GONE

        lifecycleScope.launch {
            val checker = AppSecurityChecker(packageManager)
            val reports = withContext(Dispatchers.IO) {
                if (showSystemApps) checker.analyseAllApps() else checker.analyseAllUserApps()
            }
            allReports = reports
            updateUi(reports)
        }
    }

    private fun filterReports(query: String) {
        val filtered = if (query.isBlank()) {
            allReports
        } else {
            allReports.filter { report ->
                report.appName.contains(query, ignoreCase = true) ||
                    report.packageName.contains(query, ignoreCase = true)
            }
        }
        updateUi(filtered)
    }

    private fun updateUi(reports: List<SecurityReport>) {
        binding.progressBar.visibility = View.GONE
        if (reports.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.textEmpty.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.textEmpty.visibility = View.GONE
            adapter.submitList(reports)
        }

        val highCount = reports.count { it.riskLevel == SecurityReport.RiskLevel.HIGH }
        val mediumCount = reports.count { it.riskLevel == SecurityReport.RiskLevel.MEDIUM }
        binding.textSummary.text = getString(
            R.string.summary_format,
            reports.size,
            highCount,
            mediumCount
        )
    }
}
