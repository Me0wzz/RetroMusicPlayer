package io.github.me0wzz.music.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import io.github.me0wzz.appthemehelper.ThemeStore

class AccentIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : AppCompatImageView(context, attrs, defStyleAttr) {
    init {
        imageTintList = ColorStateList.valueOf(ThemeStore.accentColor(context))
    }
}