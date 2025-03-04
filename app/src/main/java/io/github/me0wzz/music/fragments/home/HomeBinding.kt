package io.github.me0wzz.music.fragments.home

import io.github.me0wzz.music.databinding.FragmentHomeBinding

class HomeBinding(
    homeBinding: FragmentHomeBinding
) {
    val root = homeBinding.root
    val container = homeBinding.container
    val contentContainer = homeBinding.contentContainer
    val appBarLayout = homeBinding.appBarLayout
    val toolbar = homeBinding.toolbar
    val bannerImage = homeBinding.imageLayout.bannerImage
    val userImage = homeBinding.imageLayout.userImage
    val lastAdded = homeBinding.homeContent.absPlaylists.lastAdded
    val topPlayed = homeBinding.homeContent.absPlaylists.topPlayed
    val actionShuffle = homeBinding.homeContent.absPlaylists.actionShuffle
    val history = homeBinding.homeContent.absPlaylists.history
    val recyclerView = homeBinding.homeContent.recyclerView
    val titleWelcome = homeBinding.imageLayout.titleWelcome
    val appNameText = homeBinding.appNameText
    val suggestions = homeBinding.homeContent.suggestions
}