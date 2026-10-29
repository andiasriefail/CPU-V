package com.CPU.V.Vhynlain

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TestsFragment : Fragment() {
    private lateinit var adapter: RowAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        adapter = RowAdapter { index ->
            startActivity(Intent(ctx, TestActivity::class.java).putExtra("id", TestActivity.ids[index]))
        }
        return RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = this@TestsFragment.adapter
        }
    }

    override fun onResume() {
        super.onResume()
        val ctx = requireContext()
        adapter.setRows(TestActivity.ids.mapIndexed { i, id ->
            val status = when (PrefUtil.testResult(ctx, id)) {
                1 -> "\u2713 " + getString(R.string.passed)
                2 -> "\u2717 " + getString(R.string.failed)
                else -> getString(R.string.not_tested)
            }
            arrayOf(getString(TestActivity.titles[i]), status)
        })
    }
}
