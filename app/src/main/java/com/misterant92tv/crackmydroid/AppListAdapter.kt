package com.misterant92tv.crackmydroid

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.misterant92tv.crackmydroid.databinding.ItemAppBinding

/**
 * RecyclerView adapter that displays a list of [SecurityReport]s, colour-coded by risk level.
 */
class AppListAdapter(
    private val onItemClick: (SecurityReport) -> Unit
) : ListAdapter<SecurityReport, AppListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: SecurityReport) {
            val context = binding.root.context

            binding.textAppName.text = report.appName
            binding.textPackageName.text = report.packageName
            binding.textRiskScore.text = context.getString(R.string.risk_score_format, report.riskScore)

            val icon: Drawable? = try {
                context.packageManager.getApplicationIcon(report.packageName)
            } catch (_: Exception) {
                ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
            }
            binding.imageAppIcon.setImageDrawable(icon)

            val (bgColor, textColor) = when (report.riskLevel) {
                SecurityReport.RiskLevel.HIGH -> Pair(
                    ContextCompat.getColor(context, R.color.risk_high_bg),
                    ContextCompat.getColor(context, R.color.risk_high_text)
                )
                SecurityReport.RiskLevel.MEDIUM -> Pair(
                    ContextCompat.getColor(context, R.color.risk_medium_bg),
                    ContextCompat.getColor(context, R.color.risk_medium_text)
                )
                SecurityReport.RiskLevel.LOW -> Pair(
                    ContextCompat.getColor(context, R.color.risk_low_bg),
                    ContextCompat.getColor(context, R.color.risk_low_text)
                )
            }
            binding.chipRiskLevel.setChipBackgroundColorResource(android.R.color.transparent)
            binding.chipRiskLevel.chipBackgroundColor = android.content.res.ColorStateList.valueOf(bgColor)
            binding.chipRiskLevel.setTextColor(textColor)
            binding.chipRiskLevel.text = report.riskLevel.name

            binding.root.setOnClickListener { onItemClick(report) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SecurityReport>() {
            override fun areItemsTheSame(oldItem: SecurityReport, newItem: SecurityReport) =
                oldItem.packageName == newItem.packageName

            override fun areContentsTheSame(oldItem: SecurityReport, newItem: SecurityReport) =
                oldItem == newItem
        }
    }
}
