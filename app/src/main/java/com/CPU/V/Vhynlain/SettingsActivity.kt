package com.CPU.V.Vhynlain

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {
    private val tags = listOf("", "en", "id", "es", "fr", "de", "pt", "ru", "ar", "hi", "zh")
    private val names = listOf(
        "", "English", "Bahasa Indonesia", "Espa\u00F1ol", "Fran\u00E7ais", "Deutsch",
        "Portugu\u00EAs", "\u0420\u0443\u0441\u0441\u043A\u0438\u0439", "\u0627\u0644\u0639\u0631\u0628\u064A\u0629",
        "\u0939\u093F\u0928\u094D\u0926\u0940", "\u4E2D\u6587"
    )
    private lateinit var languageValue: TextView
    private lateinit var themeValue: TextView

    private fun languageNames() = names.mapIndexed { i, n -> if (i == 0) getString(R.string.system_default) else n }

    private fun themeNames() = listOf(R.string.system_default, R.string.light, R.string.dark, R.string.black).map { getString(it) }

    private fun currentLanguage(): Int {
        val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (tag.isEmpty()) return 0
        val i = tags.indexOfFirst { it.isNotEmpty() && tag.startsWith(it) }
        return if (i < 0) 0 else i
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.setup(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        val list = findViewById<LinearLayout>(R.id.list)
        val inflater = LayoutInflater.from(this)

        val languageRow = inflater.inflate(R.layout.item_row, list, false)
        languageRow.findViewById<TextView>(R.id.label).setText(R.string.language)
        languageValue = languageRow.findViewById(R.id.value)
        languageValue.text = languageNames()[currentLanguage()]
        languageRow.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language)
                .setSingleChoiceItems(languageNames().toTypedArray(), currentLanguage()) { dialog, which ->
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags[which]))
                    dialog.dismiss()
                }
                .show()
        }
        list.addView(languageRow)

        val themeRow = inflater.inflate(R.layout.item_row, list, false)
        themeRow.findViewById<TextView>(R.id.label).setText(R.string.theme)
        themeValue = themeRow.findViewById(R.id.value)
        themeValue.text = themeNames()[PrefUtil.theme(this)]
        themeRow.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.theme)
                .setSingleChoiceItems(themeNames().toTypedArray(), PrefUtil.theme(this)) { dialog, which ->
                    PrefUtil.setTheme(this, which)
                    ThemeHelper.apply(this)
                    dialog.dismiss()
                    recreate()
                }
                .show()
        }
        list.addView(themeRow)

        val privacyRow = inflater.inflate(R.layout.item_row, list, false)
        privacyRow.findViewById<TextView>(R.id.label).setText(R.string.privacy_policy)
        privacyRow.findViewById<TextView>(R.id.value).text = BuildConfig.PRIVACY_POLICY_URL
        privacyRow.setOnClickListener {
            try {
                startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(BuildConfig.PRIVACY_POLICY_URL)))
            } catch (e: Exception) {
            }
        }
        list.addView(privacyRow)
    }
}
