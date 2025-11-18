package com.example.playlist_maker_main.search.data.network

import com.example.playlist_maker_main.search.data.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("/search?entity=song&limit=50")
    suspend fun search(@Query("term") term: String): SearchResponseDto
}
