package com.example.tracer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.activityViewModels

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryViewFragment : Fragment() {
    val historyViewModel: HistoryViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val index: Int? = arguments?.getInt("Index")

        if (index != null) displayHike(historyViewModel.history[index])
    }

    private fun displayHike(hike: Hike)
    {
        // Display Mean Speed
        view?.findViewById<TextView>(R.id.speed_text_view)?.text = hike.meanSpeed.toString()

        // Display Mean Acceleration
        view?.findViewById<TextView>(R.id.acceleration_text_view)?.text = hike.meanAcceleration.toString()

        // Display Steps Count
        view?.findViewById<TextView>(R.id.steps_text_view)?.text = hike.stepsCount.toString()

        // Display Average Temperature
        view?.findViewById<TextView>(R.id.temperature_text_view)?.text = hike.meanTemperature.toString()
    }
}