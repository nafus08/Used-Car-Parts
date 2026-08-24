package com.example.usedcarparts.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.usedcarparts.data.Listing
import com.example.usedcarparts.databinding.ItemListingBinding

class ListingAdapter(
    private var listings: List<Listing>,
    private val currentUserId: String?,
    private val onListingClick: (Listing) -> Unit,
    private val onDeleteClick: ((Listing) -> Unit)? = null,
    private val onAddToCartClick: ((Listing) -> Unit)? = null
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
        
        // Show delete button only if current user is the owner
        if (currentUserId != null && listing.traderFirebaseId == currentUserId) {
            holder.binding.btnDelete.visibility = android.view.View.VISIBLE
            holder.binding.btnDelete.setOnClickListener {
                onDeleteClick?.invoke(listing)
            }
            holder.binding.btnAddToCart.visibility = android.view.View.GONE
        } else {
            holder.binding.btnDelete.visibility = android.view.View.GONE
            holder.binding.btnAddToCart.visibility = android.view.View.VISIBLE
            holder.binding.btnAddToCart.setOnClickListener {
                onAddToCartClick?.invoke(listing)
            }
        }

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
