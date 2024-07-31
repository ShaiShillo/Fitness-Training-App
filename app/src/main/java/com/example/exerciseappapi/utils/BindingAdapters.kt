package com.example.exerciseappapi.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageUrl")
fun bindImage(imageView: ImageView, url: String?) {
    url?.let {
        Glide.with(imageView.context)
            .load(it)
            .into(imageView)
    }
}

@BindingAdapter("secondaryMusclesText")
fun setSecondaryMusclesText(view: TextView, secondaryMuscles: List<String>?) {
    view.text = secondaryMuscles?.joinToString(", ") ?: ""
}

@BindingAdapter("instructionsText")
fun setInstructionsText(view: TextView, instructions: List<String>?) {
    view.text = instructions?.joinToString("\n") ?: ""
}
