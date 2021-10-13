package com.example.myfuel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add.view.*
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt
import java.util.*

class AddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add, container,false)
    }

    lateinit var dtoFuel: DTOFuel

    override fun onStart() {
        super.onStart()

        val databaseHandler = DatabaseHandler(requireContext())



        requireView().add.setOnClickListener {
            if(requireView().odometer.text.toString() != "" || requireView().odometer.text.toString() != "" || requireView().odometer.text.toString() != "") {
                 dtoFuel = DTOFuel(
                     id = 0,
                     odometer = parseInt(requireView().odometer.text.toString()),
                     fuelAmount = parseDouble(requireView().fuelAmount.text.toString()),
                     fuelPrice = parseDouble(requireView().fuelPrice.text.toString()),
                     date = Date()
                 )

                databaseHandler.addFuelRecord(dtoFuel)

                parentFragmentManager.beginTransaction().replace(R.id.fragment_main, StatsFragment()).commit()
            }

        }

        requireView().cancel.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_main, StatsFragment()).commit()
        }
    }
}


