package com.joy.common.image

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun ImageView.loadNetworkImage(
    data: Any?,
    onSuccess: ((Drawable) -> Unit)? = null,
    onError: (() -> Unit)? = null,
) {
    Glide.with(this)
        .load(data)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean,
            ): Boolean {
                onError?.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean,
            ): Boolean {
                onSuccess?.invoke(resource)
                return false
            }
        })
        .into(this)
}

fun ImageView.loadNetworkImageCircle(
    data: Any?,
    onSuccess: ((Drawable) -> Unit)? = null,
    onError: (() -> Unit)? = null,
) {
    Glide.with(this)
        .load(data)
        .transform(CircleCrop())
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean,
            ): Boolean {
                onError?.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean,
            ): Boolean {
                onSuccess?.invoke(resource)
                return false
            }
        })
        .into(this)
}
