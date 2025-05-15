package com.pet.vpn_client.utils

import java.net.URI

fun CharSequence?.isNotNullEmpty(): Boolean = this != null && this.isNotEmpty()

val URI.idnHost: String
    get() = host?.replace("[", "")?.replace("]", "").orEmpty()
