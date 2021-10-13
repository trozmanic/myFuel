package com.example.myfuel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_stats.view.*



class StatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =inflater.inflate(R.layout.fragment_stats, container,false)
        val button = view.findViewById<Button>(R.id.add)

        view.add.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_main, AddFragment()).commit()
        }
        return view
    }


    override fun onStart() {
        super.onStart()

//        MobileAds.initialize(requireContext()) {}
//
//        val adRequest = AdRequest.Builder().build()
//        requireView().adView.loadAd(adRequest)



        val databaseHandler = DatabaseHandler(requireContext())

        val data: DTOStats? =  databaseHandler.getStatistics()


        requireView().fuelConsumption.text = data!!.avgFuel.toString()

        requireView().fuelCost.text = data!!.avgMonthlyFuelCost.toString()

        requireView().avgDistance.text = data!!.avgMonthlyDistance.toString()

    }

}