/* SPDX-License-Identifier: GPL-3.0-or-later */
/*
 * This file is part of LibreXrayVPN.
 *
 * Modified from v2rayNG (https://github.com/2dust/v2rayNG).
 */

package org.librexray.vpn.domain.models

/**
 * Supported network transport protocols for outbound VPN connections.
 * Utility:
 * - [fromString] maps a raw string to its [NetworkType] value, falling back to [TCP] if unknown or null.
 */
enum class NetworkType(val type: String) {
    TCP("tcp"),
    KCP("kcp"),
    WS("ws"),
    HTTP_UPGRADE("httpupgrade"),
    XHTTP("xhttp"),
    HTTP("http"),
    H2("h2"),
    GRPC("grpc");

    companion object {
        fun fromString(type: String?) = entries.find { it.type == type } ?: TCP
    }
}
