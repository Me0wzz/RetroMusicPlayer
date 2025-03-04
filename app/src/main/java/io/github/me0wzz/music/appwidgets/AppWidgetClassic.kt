/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package io.github.me0wzz.music.appwidgets

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.RemoteViews
import io.github.me0wzz.appthemehelper.util.MaterialValueHelper
import io.github.me0wzz.appthemehelper.util.VersionUtils
import io.github.me0wzz.music.R
import io.github.me0wzz.music.activities.MainActivity
import io.github.me0wzz.music.appwidgets.base.BaseAppWidget
import io.github.me0wzz.music.glide.GlideApp
import io.github.me0wzz.music.glide.RetroGlideExtension
import io.github.me0wzz.music.glide.palette.BitmapPaletteWrapper
import io.github.me0wzz.music.service.MusicService
import io.github.me0wzz.music.service.MusicService.Companion.ACTION_REWIND
import io.github.me0wzz.music.service.MusicService.Companion.ACTION_SKIP
import io.github.me0wzz.music.service.MusicService.Companion.ACTION_TOGGLE_PAUSE
import io.github.me0wzz.music.util.ImageUtil
import io.github.me0wzz.music.util.PreferenceUtil
import io.github.me0wzz.music.util.RetroUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

class AppWidgetClassic : BaseAppWidget() {
    private var target: Target<BitmapPaletteWrapper>? = null // for cancellation

    /**
     * Initialize given widgets to default state, where we launch Music on default click and hide
     * actions if service not running.
     */
    override fun defaultAppWidget(context: Context, appWidgetIds: IntArray) {
        val appWidgetView = RemoteViews(context.packageName, R.layout.app_widget_classic)

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_audio_art)
        appWidgetView.setImageViewBitmap(
            R.id.button_next,
            createBitmap(
                RetroUtil.getTintedVectorDrawable(
                    context,
                    R.drawable.ic_skip_next,
                    MaterialValueHelper.getSecondaryTextColor(context, true)
                ), 1f
            )
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_prev,
            createBitmap(
                RetroUtil.getTintedVectorDrawable(
                    context,
                    R.drawable.ic_skip_previous,
                    MaterialValueHelper.getSecondaryTextColor(context, true)
                ), 1f
            )
        )
        appWidgetView.setImageViewBitmap(
            R.id.button_toggle_play_pause,
            createBitmap(
                RetroUtil.getTintedVectorDrawable(
                    context,
                    R.drawable.ic_play_arrow_white_32dp,
                    MaterialValueHelper.getSecondaryTextColor(context, true)
                ), 1f
            )
        )

        linkButtons(context, appWidgetView)
        pushUpdate(context, appWidgetIds, appWidgetView)
    }

    /**
     * Update all active widget instances by pushing changes
     */
    override fun performUpdate(service: MusicService, appWidgetIds: IntArray?) {
        val appWidgetView = RemoteViews(service.packageName, R.layout.app_widget_classic)

        val isPlaying = service.isPlaying
        val song = service.currentSong

        // Set the titles and artwork
        if (song.title.isEmpty() && song.artistName.isEmpty()) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE)
            appWidgetView.setTextViewText(R.id.title, song.title)
            appWidgetView.setTextViewText(R.id.text, getSongArtistAndAlbum(song))
        }

        // Link actions buttons to intents
        linkButtons(service, appWidgetView)

        if (imageSize == 0) {
            imageSize =
                service.resources.getDimensionPixelSize(R.dimen.app_widget_classic_image_size)
        }
        if (cardRadius == 0f) {
            cardRadius = service.resources.getDimension(R.dimen.app_widget_card_radius)
        }

        // Load the album cover async and push the update on completion
        val appContext = service.applicationContext
        service.runOnUiThread {
            if (target != null) {
                Glide.with(service).clear(target)
            }
            target = GlideApp.with(service).asBitmapPalette().songCoverOptions(song)
                .load(RetroGlideExtension.getSongModel(song))
                //.checkIgnoreMediaStore()
                .centerCrop()
                .into(object : SimpleTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
                    override fun onResourceReady(
                        resource: BitmapPaletteWrapper,
                        transition: Transition<in BitmapPaletteWrapper>?
                    ) {
                        val palette = resource.palette
                        update(
                            resource.bitmap,
                            palette.getVibrantColor(
                                palette.getMutedColor(
                                    MaterialValueHelper.getSecondaryTextColor(
                                        service,
                                        true
                                    )
                                )
                            )
                        )
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        update(null, Color.WHITE)
                    }

                    private fun update(bitmap: Bitmap?, color: Int) {
                        // Set correct drawable for pause state
                        val playPauseRes =
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                        appWidgetView.setImageViewBitmap(
                            R.id.button_toggle_play_pause,
                            ImageUtil.createBitmap(
                                ImageUtil.getTintedVectorDrawable(
                                    service,
                                    playPauseRes,
                                    color
                                )
                            )
                        )

                        // Set prev/next button drawables
                        appWidgetView.setImageViewBitmap(
                            R.id.button_next,
                            ImageUtil.createBitmap(
                                ImageUtil.getTintedVectorDrawable(
                                    service,
                                    R.drawable.ic_skip_next,
                                    color
                                )
                            )
                        )
                        appWidgetView.setImageViewBitmap(
                            R.id.button_prev,
                            ImageUtil.createBitmap(
                                ImageUtil.getTintedVectorDrawable(
                                    service,
                                    R.drawable.ic_skip_previous,
                                    color
                                )
                            )
                        )

                        val image = getAlbumArtDrawable(service.resources, bitmap)
                        val roundedBitmap =
                            createRoundedBitmap(
                                image,
                                imageSize,
                                imageSize,
                                cardRadius,
                                0F,
                                cardRadius,
                                0F
                            )
                        appWidgetView.setImageViewBitmap(R.id.image, roundedBitmap)

                        pushUpdate(appContext, appWidgetIds, appWidgetView)
                    }
                })
        }
    }

    /**
     * Link up various button actions using [PendingIntent].
     */
    private fun linkButtons(context: Context, views: RemoteViews) {
        val action = Intent(context, MainActivity::class.java)
            .putExtra(
                MainActivity.EXPAND_PANEL,
                PreferenceUtil.isExpandPanel
            )

        val serviceName = ComponentName(context, MusicService::class.java)

        // Home
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        var pendingIntent = PendingIntent.getActivity(
            context, 0, action, if (VersionUtils.hasMarshmallow())
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )
        views.setOnClickPendingIntent(R.id.image, pendingIntent)
        views.setOnClickPendingIntent(R.id.media_titles, pendingIntent)

        // Previous track
        pendingIntent = buildPendingIntent(context, ACTION_REWIND, serviceName)
        views.setOnClickPendingIntent(R.id.button_prev, pendingIntent)

        // Play and pause
        pendingIntent = buildPendingIntent(context, ACTION_TOGGLE_PAUSE, serviceName)
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent)

        // Next track
        pendingIntent = buildPendingIntent(context, ACTION_SKIP, serviceName)
        views.setOnClickPendingIntent(R.id.button_next, pendingIntent)
    }

    companion object {

        const val NAME = "app_widget_classic"

        private var mInstance: AppWidgetClassic? = null
        private var imageSize = 0
        private var cardRadius = 0f

        val instance: AppWidgetClassic
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = AppWidgetClassic()
                }
                return mInstance!!
            }
    }
}
