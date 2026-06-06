package com.bintianqi.owndroid.feature.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PreferentialNetworkServiceInfo(
    val enabled: Boolean = true,
    val id: Int = -1,
    @SerialName("allow_fallback") val allowFallback: Boolean = false,
    @SerialName("block_non_matching") val blockNonMatching: Boolean = false,
    @SerialName("excluded_uids") val excludedUids: List<Int> = emptyList(),
    @SerialName("included_uids") val includedUids: List<Int> = emptyList()
)
