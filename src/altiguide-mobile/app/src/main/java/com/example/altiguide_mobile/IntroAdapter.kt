package com.example.altiguide_mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class IntroSlide(
    val title: String,
    val description: String,
    val imageResId: Int
)

class IntroAdapter(private val slides: List<IntroSlide>) :
    RecyclerView.Adapter<IntroAdapter.IntroViewHolder>() {

    inner class IntroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivBackground: ImageView = view.findViewById(R.id.iv_background)
        private val tvTitle: TextView = view.findViewById(R.id.tv_title)
        private val tvDescription: TextView = view.findViewById(R.id.tv_description)

        fun bind(slide: IntroSlide) {
            ivBackground.setImageResource(slide.imageResId)
            tvTitle.text = slide.title
            tvDescription.text = slide.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_intro_slide, parent, false)
        return IntroViewHolder(view)
    }

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        holder.bind(slides[position])
    }

    override fun getItemCount(): Int = slides.size
}
