package com.CPU.V.Vhynlain

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SensorsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        val manager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val list = manager.getSensorList(Sensor.TYPE_ALL)
        return RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = SensorAdapter(list) { index ->
                startActivity(Intent(ctx, SensorDetailActivity::class.java).putExtra("index", index))
            }
        }
    }
}
