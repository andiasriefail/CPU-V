package com.CPU.V.Vhynlain

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private val tabs = listOf(
        "dashboard", "device", "system", "cpu", "battery", "network", "connectivity", "bluetooth",
        "display", "memory", "camera", "audio", "features", "codecs", "thermal", "sensors", "tests"
    )
    private val titles = intArrayOf(
        R.string.dashboard, R.string.device, R.string.system, R.string.cpu, R.string.battery,
        R.string.network, R.string.connectivity, R.string.bluetooth, R.string.display, R.string.memory, R.string.camera,
        R.string.audio, R.string.features, R.string.codecs, R.string.thermal, R.string.sensors, R.string.tests
    )
    private var themeAtCreate = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.setup(this)
        super.onCreate(savedInstanceState)
        themeAtCreate = PrefUtil.theme(this)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_share) {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "CPU-V report")
                    putExtra(Intent.EXTRA_TEXT, SystemInfo.report(this@MainActivity))
                }
                startActivity(Intent.createChooser(send, getString(R.string.share_report)))
            } else {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            true
        }

        val pager = findViewById<ViewPager2>(R.id.pager)
        pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int): Fragment = when (tabs[position]) {
                "dashboard" -> DashboardFragment()
                "sensors" -> SensorsFragment()
                "tests" -> TestsFragment()
                "codecs" -> CodecsFragment()
                else -> InfoFragment.newInstance(tabs[position])
            }
        }
        TabLayoutMediator(findViewById<TabLayout>(R.id.tabs), pager) { tab, position ->
            tab.setText(titles[position])
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        if (PrefUtil.theme(this) != themeAtCreate) {
            recreate()
        }
    }
}
