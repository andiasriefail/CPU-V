package com.CPU.V.Vhynlain

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class SensorDetailActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var manager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var chart: LineChartView
    private lateinit var liveValues: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.setup(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_detail)
        manager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = manager.getSensorList(Sensor.TYPE_ALL)[intent.getIntExtra("index", 0)]
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = sensor.name
        toolbar.setNavigationOnClickListener { finish() }
        chart = findViewById(R.id.chart)
        liveValues = findViewById(R.id.liveValues)
        RowBinder.bind(findViewById<LinearLayout>(R.id.rows), SystemInfo.sensorRows(this, sensor))
    }

    override fun onResume() {
        super.onResume()
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        manager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        chart.add(*event.values.copyOf(minOf(3, event.values.size)))
        liveValues.text = event.values.take(6).joinToString("\n") { String.format(Locale.US, "%.4f", it) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
