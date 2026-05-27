package com.joy.featuregoods.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.joy.featuregoods.databinding.ItemGoodsImageBinding

class GoodsImagePagerAdapter : RecyclerView.Adapter<GoodsImagePagerAdapter.ImageVH>() {

    private val items = mutableListOf<String>()

    val currentList: List<String> get() = items.toList()

    fun submit(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val binding = ItemGoodsImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageVH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ImageVH, position: Int) {
        holder.bind(items[position])
    }

    class ImageVH(private val binding: ItemGoodsImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String) {
            Glide.with(binding.ivGoods.context)
                .load(url)
                .into(binding.ivGoods)
        }
    }
}