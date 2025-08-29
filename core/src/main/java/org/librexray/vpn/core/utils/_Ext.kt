/* SPDX-License-Identifier: GPL-3.0-or-later */
/*
 * This file is part of LibreXrayVPN.
 *
 * Modified from v2rayNG (https://github.com/2dust/v2rayNG).
 */

package org.librexray.vpn.core.utils

import java.net.URI

/**
 * Returns the host part of the URI without square brackets, typically used for IPv6 addresses.
 * Always returns a non-null string, even if the host is absent.
 */
val URI.idnHost: String
    get() = host?.replace("[", "")?.replace("]", "").orEmpty()