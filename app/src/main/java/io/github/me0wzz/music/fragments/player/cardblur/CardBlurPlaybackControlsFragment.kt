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
package io.github.me0wzz.music.fragments.player.cardblur

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import io.github.me0wzz.appthemehelper.util.ColorUtil
import io.github.me0wzz.appthemehelper.util.TintHelper
import io.github.me0wzz.music.R
import io.github.me0wzz.music.databinding.FragmentCardBlurPlayerPlaybackControlsBinding
import io.github.me0wzz.music.extensions.applyColor
import io.github.me0wzz.music.extensions.getSongInfo
import io.github.me0wzz.music.extensions.hide
import io.github.me0wzz.music.extensions.show
import io.github.me0wzz.music.fragments.base.AbsPlayerControlsFragment
import io.github.me0wzz.music.helper.MusicPlayerRemote
import io.github.me0wzz.music.helper.PlayPauseButtonOnClickHandler
import io.github.me0wzz.music.util.PreferenceUtil
import io.github.me0wzz.music.util.color.MediaNotificationProcessor

class CardBlurPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_card_blur_player_playback_controls) {

    private var _binding: FragmentCardBlurPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    override val progressSlider: SeekBar
        get() = binding.progressSlider

    override val shuffleButton: ImageButton
        get() = binding.mediaButton.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.mediaButton.repeatButton

    override val nextButton: ImageButton
        get() = binding.mediaButton.nextButton

    override val previousButton: ImageButton
        get() = binding.mediaButton.previousButton

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCardBlurPlayerPlaybackControlsBinding.bind(view)
        setUpPlayPauseFab()
        binding.progressSlider.applyColor(Color.WHITE)
    }

    override fun setColor(color: MediaNotificationProcessor) {
        lastPlaybackControlsColor = Color.WHITE
        lastDisabledPlaybackControlsColor = ColorUtil.withAlpha(Color.WHITE, 0.3f)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
        updateProgressTextColor()

        volumeFragment?.tintWhiteColor()
    }

    private fun setUpPlayPauseFab() {
        binding.mediaButton.playPauseButton.apply {
            TintHelper.setTintAuto(this, Color.WHITE, true)
            TintHelper.setTintAuto(this, Color.BLACK, false)
            setOnClickListener(PlayPauseButtonOnClickHandler())
        }
    }

    private fun updatePlayPauseDrawableState() {
        when {
            MusicPlayerRemote.isPlaying -> binding.mediaButton.playPauseButton.setImageResource(R.drawable.ic_pause)
            else -> binding.mediaButton.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun updateProgressTextColor() {
        val color = Color.WHITE
        binding.songTotalTime.setTextColor(color)
        binding.songCurrentProgress.setTextColor(color)
        binding.songInfo.setTextColor(color)
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    private fun updateSong() {
        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(MusicPlayerRemote.currentSong)
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    public override fun show() {
        binding.mediaButton.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        binding.mediaButton.playPauseButton.apply {
            scaleX = 0f
            scaleY = 0f
            rotation = 0f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
