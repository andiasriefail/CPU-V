package com.CPU.V.Vhynlain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CodecsFragment : Fragment() {
    private val adapter = RowAdapter(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        return RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = this@CodecsFragment.adapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Thread {
            val rows = SystemInfo.codecRows()
            activity?.runOnUiThread { if (isAdded) adapter.setRows(rows) }
        }.start()
    }
}
