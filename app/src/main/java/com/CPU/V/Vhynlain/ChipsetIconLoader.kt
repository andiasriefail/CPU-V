package com.CPU.V.Vhynlain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.caverock.androidsvg.SVG
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object ChipsetIconLoader {
    private const val SIZE = 256

    /**
     * Rules checked in order: regex -> logo file. Real values seen on devices:
     * Build.HARDWARE: mt6785, ums9230_25c10, sp9832e_1h10_go, s9863aA667L_go_32b, exynos980, kirin8020, qcom, rk30board
     * Build.SOC_MANUFACTURER: Mediatek, QTI, Qualcomm, Samsung, Google, Hisilicon, Allwinner, Spreadtrum
     * Build.SOC_MODEL: MT6886, SM8845, SDM450, UMS9230T, Tensor G5, s5e9965
     */
    private val rules: List<Pair<Regex, String>> = listOf(
        Regex("""rockchip|\brk\d{2}""") to "Rockchip%27s_logo.jpg",
        Regex("""allwinner|sunxi|exdroid|\bsun\d+i|\bsun[5-9]\d?\b""") to "wp:Allwinner.svg",
        Regex("""amlogic|meson|\bgxl|\bgxbb|\bgxm|\bg12[ab]|\bsm1\b|\btxlx?\b|\bs9(05|12|22)""") to "Amlogic_logo.svg",
        Regex("""unisoc|spreadtrum|sprd|\bums\d|\bsc\d{4}|\bsp\d{4}|\bs9[6-8]\d{2}[a-z]|\bt[3-7]\d\d\b|tiger""") to "Unisoc_Logo.png",
        Regex("""qualcomm|\bqti\b|qcom|snapdragon|\bsm\d{4}|\bsdm\d|\bmsm\d|\bapq\d|\bqcm\d|\bsxr\d|msmnile|\bkona|lahaina|\btaro|kalama|pineapple|bengal|trinket|atoll|\blito|\bholi|waipio|diwali|\bcrow|\bparrot|\bsun\b|\bcanoe""") to "Snapdragon_Logo.svg",
        Regex("""mediatek|\bmt\d{4}|dimensity|helio""") to "MediaTek_logo.svg",
        Regex("""exynos|\bs5e\d|universal\d|smdk|samsungexynos|\bsamsung\b""") to "Exynos_Logo.svg",
        Regex("""tensor|\bgs\d{3}|zuma|laguna|\bgoogle\b""") to "Google_Tensor_GS101.svg",
        Regex("""kirin|hisilicon|\bhi\d{4}""") to "Hisilicon_logo.png",
        Regex("""nvidia|tegra""") to "NVIDIA_logo.svg",
        Regex("""intel|\batom|moorefield|merrifield|baytrail|cherrytrail|saltbay|medfield|clovertrail""") to "Intel_logo_(2020,_light_blue).svg",
        Regex("""broadcom|\bbcm\d|hawaii|capri""") to "Broadcom_logo_(2016-present).svg",
        Regex("""realtek|\brtd\d|\brtk""") to "Realtek_logotype.svg",
        Regex("""omap|\btexas""") to "wc:texas_instruments_logo.svg"
    )

    private fun prop(name: String): String = try {
        val p = ProcessBuilder("getprop", name).redirectErrorStream(true).start()
        p.inputStream.bufferedReader().use { it.readLine() ?: "" }.trim()
    } catch (e: Throwable) {
        ""
    }

    private fun match(text: String): String? = rules.firstOrNull { it.first.containsMatchIn(text) }?.second

    /** Picks the logo file name from the chipset vendor, or null if unknown. */
    fun vendorKey(): String? {
        // 1) most precise: the SoC fields (Android 12+)
        if (Build.VERSION.SDK_INT >= 31) {
            val soc = (Build.SOC_MANUFACTURER + " " + Build.SOC_MODEL).lowercase(Locale.US)
            match(soc)?.let { return it }
        }
        // 2) hardware, board and platform names (all versions)
        val hw = (
            Build.HARDWARE + " " + Build.BOARD + " " + prop("ro.board.platform") + " " +
                prop("ro.hardware.chipname") + " " + prop("ro.soc.model") + " " + prop("ro.soc.manufacturer")
            ).lowercase(Locale.US)
        return match(hw)
    }

    private fun logoUrl(key: String, template: String): String = when {
        key.startsWith("wp:") -> "https://en.wikipedia.org/wiki/Special:FilePath/" + key.substring(3)
        key.startsWith("wc:") -> "https://en.wikichip.org/wiki/Special:FilePath/" + key.substring(3)
        else -> String.format(Locale.US, template, key)
    }

    fun load(context: Context, done: (Bitmap?) -> Unit) = loadIcon(context, vendorKey(), BuildConfig.CHIPSET_ICON_URL_TEMPLATE, done)

    /** [key] names the cache file and fills the "%s" in [template]. */
    fun loadIcon(context: Context, key: String?, template: String, done: (Bitmap?) -> Unit) {
        val app = context.applicationContext
        if (key == null || template.isBlank()) {
            done(null)
            return
        }
        Thread {
            val target = File(app.filesDir, "chipset_icon_" + key.replace(Regex("[^A-Za-z0-9.]"), "_"))
            if (!target.exists()) {
                val temp = File(app.filesDir, "chipset_icon.tmp")
                try {
                    val url = logoUrl(key, template)
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "CPU-V/1.0 (Android; device info app)")
                    connection.connectTimeout = 10000
                    connection.readTimeout = 15000
                    if (connection.responseCode == 200) {
                        connection.inputStream.use { input ->
                            temp.outputStream().use { output -> input.copyTo(output) }
                        }
                        if (render(temp) != null) temp.renameTo(target) else temp.delete()
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    temp.delete()
                }
            }
            val bitmap = if (target.exists()) render(target) else null
            Handler(Looper.getMainLooper()).post { done(bitmap) }
        }.start()
    }

    private fun render(file: File): Bitmap? {
        try {
            file.inputStream().use { input ->
                val svg = SVG.getFromInputStream(input)
                svg.setDocumentWidth(SIZE.toFloat())
                svg.setDocumentHeight(SIZE.toFloat())
                val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
                svg.renderToCanvas(Canvas(bitmap))
                return bitmap
            }
        } catch (e: Throwable) {
            // not an SVG: try PNG/JPG
        }
        return try {
            BitmapFactory.decodeFile(file.path)
        } catch (e: Throwable) {
            null
        }
    }
}
