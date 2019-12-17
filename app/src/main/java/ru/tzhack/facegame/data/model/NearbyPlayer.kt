package ru.tzhack.facegame.data.model

data class NearbyPlayer(
    val playerNickname: String,
    val playerEndPoint: String,
    var connectSuccess: Boolean
) /* TODO: Parcelable */ {
}