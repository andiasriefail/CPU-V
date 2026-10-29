package com.CPU.V.Vhynlain

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class InfoFragment : Fragment() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var rows: LinearLayout
    private lateinit var permissionBox: LinearLayout
    private val type: String get() = requireArguments().getString("type")!!

    private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        refresh()
    }

    private val tick = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 2000)
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun missing() = SystemInfo.permissions(type).filter { !SystemInfo.granted(requireContext(), it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        val root = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        permissionBox = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(12))
            visibility = View.GONE
        }
        permissionBox.addView(TextView(ctx).apply { setText(R.string.permission_needed) })
        permissionBox.addView(MaterialButton(ctx).apply {
            setText(R.string.grant_permission)
            setOnClickListener { launcher.launch(missing().toTypedArray()) }
        })
        rows = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        if (type == "system") root.addView(androidBanner(inflater, root))
        root.addView(permissionBox)
        root.addView(rows)
        return ScrollView(ctx).apply { addView(root) }
    }

    private fun androidBanner(inflater: LayoutInflater, parent: ViewGroup): View {
        val v = inflater.inflate(R.layout.header_android, parent, false)
        val api = android.os.Build.VERSION.SDK_INT
        val name = SystemInfo.androidName(api)
        v.findViewById<TextView>(R.id.bannerTitle).text = name.substringBefore(" - ")
        v.findViewById<TextView>(R.id.bannerCodename).text = if (name.contains(" - ")) name.substringAfter(" - ") else ""
        v.findViewById<TextView>(R.id.bannerDate).text = getString(R.string.released) + ": " + SystemInfo.androidReleaseDate(api)
        v.findViewById<TextView>(R.id.bannerApi).text = "API $api"
        val icon = v.findViewById<android.widget.ImageView>(R.id.bannerIcon)
        val file = ANDROID_LOGOS[api]
        icon.setColorFilter(0xFFFFFFFF.toInt())
        val generic = {
            ChipsetIconLoader.loadIcon(requireContext(), "android", BuildConfig.ANDROID_LOGO_URL_TEMPLATE) { bitmap ->
                if (isAdded && bitmap != null) icon.setImageBitmap(bitmap)
            }
        }
        if (file == null) {
            generic()
        } else {
            ChipsetIconLoader.loadIcon(requireContext(), file, BuildConfig.ANDROID_VERSION_LOGO_URL_TEMPLATE) { bitmap ->
                if (!isAdded) return@loadIcon
                if (bitmap != null) {
                    icon.clearColorFilter()
                    icon.setImageBitmap(bitmap)
                } else {
                    generic()
                }
            }
        }
        return v
    }

    private fun refresh() {
        if (!isAdded) return
        permissionBox.visibility = if (missing().isNotEmpty()) View.VISIBLE else View.GONE
        RowBinder.bind(rows, SystemInfo.rows(requireContext(), type))
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
    }

    companion object {
        // API level -> file name on Wikimedia Commons (names taken from Wikimedia Commons search results)
        private val ANDROID_LOGOS = mapOf(
            21 to "Android_Lollipop_Logo.svg",
            22 to "Android_Lollipop_Logo.svg",
            23 to "Android_Marshmallow_logo.svg",
            24 to "Android_Nougat_Logo.svg",
            25 to "Android_Nougat_Logo.svg",
            26 to "Android_Oreo_logo.svg",
            27 to "Android_Oreo_8.1_logo.svg",
            28 to "Android_P_logo.svg",
            29 to "Android_10_logo.svg",
            30 to "Android_11_logo.svg",
            31 to "Android_12_Logo.svg",
            32 to "Android_12_Logo.svg",
            33 to "Android_13_logo.svg",
            34 to "Android_14_official_logo.svg",
            35 to "Android_15_Logo.svg",
            36 to "Android_16_Logo_(SVG).svg",
            37 to "Android_17_logo.svg"
        )

        fun newInstance(type: String) = InfoFragment().apply {
            arguments = Bundle().apply { putString("type", type) }
        }
    }
}
