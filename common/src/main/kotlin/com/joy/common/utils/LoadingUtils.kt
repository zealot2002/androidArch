package com.joy.common.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

object LoadingUtils {
    private var dialog: Dialog? = null

    fun show(context: Context?) {
        if (context == null) return
        dismiss()
        
        val progressBar = ProgressBar(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        val textView = TextView(context).apply {
            text = "加载中..."
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        }
        
        dialog = Dialog(context).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setPadding(48, 48, 48, 48)
                addView(progressBar)
                addView(textView)
            }
            
            setContentView(layout)
            
            window?.apply {
                setGravity(Gravity.CENTER)
                setBackgroundDrawableResource(android.R.color.transparent)
            }
            show()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}