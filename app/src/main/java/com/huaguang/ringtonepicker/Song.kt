package com.huaguang.ringtonepicker

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(
    val songTitle: String,
    val artist: String,
    val songUri: Uri
) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(songTitle)
        dest.writeString(artist)
        dest.writeString(songUri.toString())
    }
}