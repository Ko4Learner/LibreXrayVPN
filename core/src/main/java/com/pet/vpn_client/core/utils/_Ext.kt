package com.pet.vpn_client.core.utils

import java.net.URI

val URI.idnHost: String
    get() = host?.replace("[", "")?.replace("]", "").orEmpty()