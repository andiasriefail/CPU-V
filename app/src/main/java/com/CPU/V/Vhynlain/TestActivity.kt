package com.CPU.V.Vhynlain

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.util.Locale
import kotlin.math.log10
import kotlin.math.sqrt

class TestActivity : AppCompatActivity(), SensorEventListener {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var testId: String
    private lateinit var content: FrameLayout
    private lateinit var title: TextView
    private lateinit var info: TextView
    private lateinit var level: ProgressBar
    private var tone: ToneGenerator? = null
    private var previousMode = AudioManager.MODE_NORMAL
    private var recording = false
    private var colorIndex = 0
    private val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.WHITE, Color.BLACK)

    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startMic() else info.setText(R.string.permission_needed)
    }

    private val beep = object : Runnable {
        override fun run() {
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            handler.postDelayed(this, 900)
        }
    }

    private val refreshBattery = object : Runnable {
        override fun run() {
            info.text = SystemInfo.rows(this@TestActivity, "battery").take(3).joinToString("\n") { it[0] + ": " + it[1] } +
                "\n" + getString(R.string.current) + ": " + SystemInfo.currentMa(this@TestActivity) + " mA"
            handler.postDelayed(this, 1000)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun label(size: Float, bold: Boolean) = TextView(this).apply {
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        gravity = Gravity.CENTER
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun finishWith(result: Int) {
        PrefUtil.setTestResult(this, testId, result)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.setup(this)
        super.onCreate(savedInstanceState)
        testId = intent.getStringExtra("id") ?: ids[0]
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            fitsSystemWindows = true
        }
        content = FrameLayout(this)
        root.addView(content, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        title = label(22f, true).apply { setPadding(dp(24), dp(48), dp(24), dp(24)) }
        root.addView(title, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP))

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(16), dp(24), dp(48))
        }
        val question = label(18f, false).apply {
            setText(if (testId == "multitouch") R.string.question_multitouch else R.string.question_generic)
            setPadding(0, 0, 0, dp(24))
        }
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val no = MaterialButton(this).apply {
            setText(R.string.no)
            setOnClickListener { finishWith(2) }
        }
        val yes = MaterialButton(this).apply {
            setText(R.string.yes)
            setOnClickListener { finishWith(1) }
        }
        val gap = View(this)
        buttons.addView(no)
        buttons.addView(gap, LinearLayout.LayoutParams(dp(32), 1))
        buttons.addView(yes)
        bottom.addView(question)
        bottom.addView(buttons)
        root.addView(bottom, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM))
        setContentView(root)

        title.setText(titles[ids.indexOf(testId)])
        buildContent()
    }

    private fun buildContent() {
        info = label(28f, true)
        level = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100 }
        when (testId) {
            "display" -> {
                content.setBackgroundColor(colors[0])
                content.setOnClickListener {
                    colorIndex = (colorIndex + 1) % colors.size
                    content.setBackgroundColor(colors[colorIndex])
                }
                info.setText(R.string.hint_display)
                info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                info.setShadowLayer(6f, 0f, 0f, Color.BLACK)
                content.addView(info, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
            }
            "multitouch" -> {
                title.text = getString(R.string.touches_detected, 0)
                val touch = TouchView(this)
                touch.setListener { count -> title.text = getString(R.string.touches_detected, count) }
                content.addView(touch, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
            else -> {
                val box = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(dp(24), 0, dp(24), 0)
                }
                box.addView(info)
                if (testId == "microphone") {
                    box.addView(level, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(24) })
                }
                content.addView(box, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
        }
    }

    private fun sensorType() = when (testId) {
        "proximity" -> Sensor.TYPE_PROXIMITY
        "light" -> Sensor.TYPE_LIGHT
        "accelerometer" -> Sensor.TYPE_ACCELEROMETER
        else -> -1
    }

    private fun setTorch(on: Boolean) {
        if (Build.VERSION.SDK_INT < 23) return
        try {
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId = manager.cameraIdList.firstOrNull {
                manager.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null) manager.setTorchMode(cameraId, on)
        } catch (e: Exception) {
        }
    }

    private fun startMic() {
        if (recording) return
        recording = true
        Thread {
            val rate = 44100
            val minBuffer = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2)
            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                recording = false
                recorder.release()
                return@Thread
            }
            recorder.startRecording()
            val buffer = ShortArray(1024)
            while (recording) {
                val n = recorder.read(buffer, 0, buffer.size)
                if (n > 0) {
                    var sum = 0.0
                    for (i in 0 until n) sum += buffer[i] * buffer[i].toDouble()
                    val db = 20 * log10(sqrt(sum / n) + 1)
                    val value = (db / 90 * 100).toInt().coerceIn(0, 100)
                    handler.post {
                        level.progress = value
                        info.text = getString(R.string.mic_level) + ": " + value + "%"
                    }
                }
            }
            recorder.stop()
            recorder.release()
        }.start()
    }

    @Suppress("DEPRECATION")
    private fun startTest() {
        when (testId) {
            "flashlight" -> {
                info.setText(R.string.test_flashlight)
                setTorch(true)
            }
            "loudspeaker" -> {
                info.setText(R.string.test_loudspeaker)
                tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                handler.post(beep)
            }
            "earpiece" -> {
                info.setText(R.string.test_earpiece)
                val audio = getSystemService(AUDIO_SERVICE) as AudioManager
                previousMode = audio.mode
                audio.mode = AudioManager.MODE_IN_COMMUNICATION
                audio.isSpeakerphoneOn = false
                tone = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100)
                handler.post(beep)
            }
            "microphone" -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    startMic()
                } else {
                    micPermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            "charging" -> handler.post(refreshBattery)
            "vibration" -> {
                info.setText(R.string.test_vibration)
                (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(longArrayOf(0, 500, 500), 0)
            }
        }
        val type = sensorType()
        if (type != -1) {
            val manager = getSystemService(SENSOR_SERVICE) as SensorManager
            val sensor = manager.getDefaultSensor(type)
            if (sensor == null) info.setText(R.string.not_supported) else manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    @Suppress("DEPRECATION")
    private fun stopTest() {
        handler.removeCallbacksAndMessages(null)
        recording = false
        tone?.release()
        tone = null
        when (testId) {
            "flashlight" -> setTorch(false)
            "earpiece" -> (getSystemService(AUDIO_SERVICE) as AudioManager).mode = previousMode
            "vibration" -> (getSystemService(VIBRATOR_SERVICE) as Vibrator).cancel()
        }
        (getSystemService(SENSOR_SERVICE) as SensorManager).unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        startTest()
    }

    override fun onPause() {
        super.onPause()
        stopTest()
    }

    override fun onSensorChanged(event: SensorEvent) {
        info.text = when (testId) {
            "proximity" -> String.format(Locale.US, "%.1f cm", event.values[0])
            "light" -> String.format(Locale.US, "%.1f lx", event.values[0])
            else -> String.format(Locale.US, "x %.2f\ny %.2f\nz %.2f", event.values[0], event.values[1], event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    companion object {
        val ids = listOf(
            "display", "multitouch", "flashlight", "loudspeaker", "earpiece", "microphone",
            "proximity", "light", "accelerometer", "charging", "vibration"
        )
        val titles = intArrayOf(
            R.string.test_display, R.string.test_multitouch, R.string.test_flashlight, R.string.test_loudspeaker,
            R.string.test_earpiece, R.string.test_microphone, R.string.test_proximity, R.string.test_light,
            R.string.test_accelerometer, R.string.test_charging, R.string.test_vibration
        )
    }
}
