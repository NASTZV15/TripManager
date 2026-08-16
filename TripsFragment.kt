package com.tripmanager.ui.trips

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.tripmanager.R
import com.tripmanager.data.database.DatabaseHelper
import com.tripmanager.data.database.TripRepository
import com.tripmanager.data.models.Trip
import com.tripmanager.databinding.FragmentTripsBinding
import com.tripmanager.utils.Constants
import kotlinx.coroutines.launch

class TripsFragment : Fragment() {

    private var _binding: FragmentTripsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: TripRepository
    private lateinit var adapter: TripAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripsBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(
            Constants.TAG,
            "TripsFragment: onViewCreated started"
        )

        repository = TripRepository(requireContext())

        setupRecyclerView()
        loadTrips()

        binding.btnAddTrip.setOnClickListener {
            Log.d(
                Constants.TAG,
                "Add trip button clicked"
            )

            showAddTripDialog()
        }

        binding.btnAddTrip.setOnLongClickListener {
            checkDatabase()
            true
        }
    }

    private fun setupRecyclerView() {

        adapter = TripAdapter(

            onItemClick = { trip ->

                Log.d(
                    Constants.TAG,
                    "Trip clicked: ${trip.name}"
                )

                Toast.makeText(
                    requireContext(),
                    "Выбрана: ${trip.name}",
                    Toast.LENGTH_SHORT
                ).show()
            },

            onDeleteClick = { trip ->
                deleteTrip(trip)
            }
        )

        binding.rvTrips.apply {

            layoutManager =
                LinearLayoutManager(requireContext())

            adapter = this@TripsFragment.adapter
        }
    }

    private fun loadTrips() {

        Log.d(
            Constants.TAG,
            "loadTrips: started"
        )

        lifecycleScope.launch {

            try {

                val trips = repository.getAllTrips()

                Log.d(
                    Constants.TAG,
                    "loadTrips: received ${trips.size} trips"
                )

                adapter.submitList(trips)

                updateEmptyState(trips.isEmpty())

            } catch (e: Exception) {

                Log.e(
                    Constants.TAG,
                    "loadTrips: error",
                    e
                )

                Toast.makeText(
                    requireContext(),
                    "Ошибка загрузки: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateEmptyState(
        isEmpty: Boolean
    ) {

        if (isEmpty) {

            binding.tvEmpty.visibility =
                View.VISIBLE

            binding.rvTrips.visibility =
                View.GONE

        } else {

            binding.tvEmpty.visibility =
                View.GONE

            binding.rvTrips.visibility =
                View.VISIBLE
        }
    }

    private fun deleteTrip(
        trip: Trip
    ) {

        lifecycleScope.launch {

            try {

                repository.deleteTrip(trip)

                Toast.makeText(
                    requireContext(),
                    "Поездка удалена",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    Constants.TAG,
                    "Trip deleted: ${trip.name}"
                )

                loadTrips()

            } catch (e: Exception) {

                Log.e(
                    Constants.TAG,
                    "deleteTrip: error",
                    e
                )

                Toast.makeText(
                    requireContext(),
                    "Ошибка удаления: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showAddTripDialog() {

        Log.d(
            Constants.TAG,
            "showAddTripDialog: started"
        )

        try {

            val dialogView = layoutInflater.inflate(
                R.layout.dialog_add_trip,
                null
            )

            val etName =
                dialogView.findViewById<TextInputEditText>(
                    R.id.et_trip_name
                )

            val etDestination =
                dialogView.findViewById<TextInputEditText>(
                    R.id.et_trip_destination
                )

            AlertDialog.Builder(requireContext())

                .setTitle("✈️ Новая поездка")

                .setView(dialogView)

                .setPositiveButton("Создать") { _, _ ->

                    val name =
                        etName.text
                            ?.toString()
                            ?.trim()
                            ?: ""

                    if (name.isEmpty()) {

                        Toast.makeText(
                            requireContext(),
                            "Введите название",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@setPositiveButton
                    }

                    val destination =
                        etDestination.text
                            ?.toString()
                            ?.trim()
                            ?.ifEmpty { null }

                    lifecycleScope.launch {

                        try {

                            val trip = Trip(
                                name = name,
                                destination = destination
                            )

                            val id =
                                repository.insertTrip(trip)

                            Log.d(
                                Constants.TAG,
                                "Trip created with id: $id"
                            )

                            val count =
                                repository.getCount()

                            Log.d(
                                Constants.TAG,
                                "Total trips after insert: $count"
                            )

                            Toast.makeText(
                                requireContext(),
                                "✅ Поездка создана!",
                                Toast.LENGTH_SHORT
                            ).show()

                            loadTrips()

                        } catch (e: Exception) {

                            Log.e(
                                Constants.TAG,
                                "Create trip error",
                                e
                            )

                            Toast.makeText(
                                requireContext(),
                                "❌ Ошибка: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                .setNegativeButton(
                    "Отмена",
                    null
                )

                .show()

        } catch (e: Exception) {

            Log.e(
                Constants.TAG,
                "showAddTripDialog: error",
                e
            )

            Toast.makeText(
                requireContext(),
                "Ошибка: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkDatabase() {

        lifecycleScope.launch {

            try {

                val count =
                    repository.getCount()

                Log.d(
                    Constants.TAG,
                    "checkDatabase: total trips: $count"
                )

                val trips =
                    repository.getAllTrips()

                Log.d(
                    Constants.TAG,
                    "checkDatabase: trips: ${trips.size}"
                )

                for (trip in trips) {

                    Log.d(
                        Constants.TAG,
                        "trip: id=${trip.id}, name=${trip.name}"
                    )
                }

                adapter.submitList(trips)

                updateEmptyState(
                    trips.isEmpty()
                )

                if (trips.isEmpty()) {

                    Toast.makeText(
                        requireContext(),
                        "⚠️ БД пуста",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    Toast.makeText(
                        requireContext(),
                        "✅ Найдено ${trips.size} поездок",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Log.e(
                    Constants.TAG,
                    "checkDatabase: error",
                    e
                )
            }
        }
    }

    fun forceRefresh() {

        Log.d(
            Constants.TAG,
            "forceRefresh: called"
        )

        loadTrips()
    }

    override fun onResume() {

        super.onResume()

        Log.d(
            Constants.TAG,
            "TripsFragment: onResume - refreshing data"
        )

        if (::repository.isInitialized) {
            loadTrips()
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}