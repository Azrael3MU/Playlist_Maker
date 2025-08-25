package com.example.playlist_maker_main

import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("/search")
    suspend fun search(
        @Query("term") term: String,
        @Query("entity") entity: String = "song"
    ): SearchResponse
}
