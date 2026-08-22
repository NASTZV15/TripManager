package com.tripmanager.ui.trips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tripmanager.databinding.ItemTripBinding
import com.tripmanager.data.models.Trip
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(
    private val onItemClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    private var trips: List<Trip> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun submitList(newTrips: List<Trip>) {
        trips = newTrips
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    override fun getItemCount(): Int = trips.size

    inner class TripViewHolder(private val binding: ItemTripBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: Trip) {
            binding.apply {
                tvTripName.text = trip.name
                tvTripDestination.text = trip.destination ?: "Направление не указано"

                val dates = mutableListOf<String>()
                trip.startDate?.let { dates.add("с ${dateFormat.format(it)}") }
                trip.endDate?.let { dates.add("по ${dateFormat.format(it)}") }
                tvTripDates.text = if (dates.isNotEmpty()) dates.joinToString(" ") else "Даты не указаны"

                root.setOnClickListener { onItemClick(trip) }
                btnDelete.setOnClickListener { onDeleteClick(trip) }
            }
        }
    }
}
