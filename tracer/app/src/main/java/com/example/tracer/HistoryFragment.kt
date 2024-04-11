package com.example.tracer

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import java.util.Date
import java.util.TimerTask

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment(), AdapterView.OnItemClickListener {

    val historyViewModel: HistoryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyViewModel.fetchHistory()

        var items = arrayOfNulls<String>(historyViewModel.history.size)

        for (i in historyViewModel.history.indices)
        {
            items[i] = historyViewModel.history[i].date.toString()
        }

        var historyList: ListView = view.findViewById(R.id.history_list)

        historyList.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        historyList.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var navController: NavController = findNavController()

        val bundle = Bundle()

        bundle.putInt("Index", position)

        navController.navigate(R.id.history_view_fragment, bundle)
    }

}