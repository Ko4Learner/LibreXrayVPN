package com.pet.vpn_client.domain.models

/**
 * Raw camera frame data with associated metadata.
 */
data class FrameData(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val imageFormat: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FrameData

        if (width != other.width) return false
        if (height != other.height) return false
        if (rotationDegrees != other.rotationDegrees) return false
        if (imageFormat != other.imageFormat) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + rotationDegrees
        result = 31 * result + imageFormat
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}