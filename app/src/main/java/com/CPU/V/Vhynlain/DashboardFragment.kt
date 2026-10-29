package com.CPU.V.Vhynlain

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {
    private val handler = Handler(Looper.getMainLooper())
    private var ramChart: LineChartView? = null
    private var curChart: LineChartView? = null
    private var ramLabel: TextView? = null
    private var batteryText: TextView? = null

    private val tick = object : Runnable {
        override fun run() {
            val ctx = context ?: return
            val ram = SystemInfo.ramPercent(ctx)
            ramChart?.add(ram.toFloat())
            ramLabel?.text = getString(R.string.ram_usage) + ": " + ram + "%"
            curChart?.add(SystemInfo.currentMa(ctx).toFloat())
            batteryText?.text = SystemInfo.batterySummary(ctx)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ramChart = view.findViewById<LineChartView>(R.id.ramChart).apply { setFixedRange(0f, 100f) }
        curChart = view.findViewById(R.id.curChart)
        ramLabel = view.findViewById(R.id.ramLabel)
        batteryText = view.findViewById(R.id.batteryText)
        view.findViewById<TextView>(R.id.curLabel).setText(R.string.battery_current)
        view.findViewById<TextView>(R.id.chipName).text = SystemInfo.chipsetName()
        view.findViewById<TextView>(R.id.modelText).text = Build.MANUFACTURER + " " + Build.MODEL
        val name = SystemInfo.androidName(Build.VERSION.SDK_INT)
        view.findViewById<TextView>(R.id.androidName).text = name.substringBefore(" - ")
        view.findViewById<TextView>(R.id.androidDetail).text =
            (if (name.contains(" - ")) name.substringAfter(" - ") + " \u00B7 " else "") + "API " + Build.VERSION.SDK_INT
        val androidIcon = view.findViewById<ImageView>(R.id.androidIcon)
        androidIcon.setImageResource(R.drawable.ic_android)
        androidIcon.setColorFilter(0xFF3DDC84.toInt())
        ChipsetIconLoader.loadIcon(requireContext(), "android", BuildConfig.ANDROID_LOGO_URL_TEMPLATE) { bitmap ->
            if (isAdded && bitmap != null) androidIcon.setImageBitmap(bitmap)
        }
        val icon = view.findViewById<ImageView>(R.id.chipIcon)
        ChipsetIconLoader.load(requireContext()) { bitmap ->
            if (isAdded && bitmap != null) {
                icon.setImageBitmap(bitmap)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ramChart = null
        curChart = null
        ramLabel = null
        batteryText = null
    }
}
