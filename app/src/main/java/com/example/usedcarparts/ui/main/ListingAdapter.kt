package com.example.usedcarparts.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.usedcarparts.data.Listing
import com.example.usedcarparts.databinding.ItemListingBinding

class ListingAdapter(
    private var listings: List<Listing>,
    private val onListingClick: (Listing) -> Unit
) : RecyclerView.Adapter<ListingAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemListingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listing = listings[position]
        holder.binding.tvTitle.text = listing.title
        holder.binding.tvDetails.text = "${listing.carMakeModelYear} · ${listing.condition}"
        holder.binding.tvPrice.text = "$${listing.price}"
        
        holder.itemView.setOnClickListener {
            onListingClick(listing)
        }
    }

    override fun getItemCount() = listings.size

    fun updateListings(newListings: List<Listing>) {
        listings = newListings
        notifyDataSetChanged()
    }
}
