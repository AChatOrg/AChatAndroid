package com.hyapp.achat.view.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.hyapp.achat.R
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@SuppressLint("SetTextI18n")
class TypingTextView : AppCompatTextView, CoroutineScope {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    )

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var job: Job? = null
    private val typing = context.getString(R.string.typing)


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        job?.cancel()
        job = launch {
            while (true) {
                text = "$typing."
                delay(500)
                text = "$typing.."
                delay(500)
                text = "$typing..."
                delay(500)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel()
    }
}