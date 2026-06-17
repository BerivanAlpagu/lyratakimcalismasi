package com.turkcell.lyraapp.data.home

/**
 * Ana sayfa içeriğinin veri kaynağı soyutlaması.
 *
 * Backend REST API'si hazır olmadığından geçici implementasyon [MockHomeRepository]'dir;
 * gerçek API geldiğinde yalnızca implementasyon ve `di/HomeModule.kt` bağlaması değişir
 * (bkz. mvi-overview.md §6).
 */
interface HomeRepository {

    /** Ana sayfa beslemesinin tamamını tek seferde döndürür. */
    suspend fun getHomeFeed(): Result<HomeFeed>
}
