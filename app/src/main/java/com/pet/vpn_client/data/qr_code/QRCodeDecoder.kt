package com.pet.vpn_client.data.qr_code

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.EnumMap
import androidx.core.graphics.createBitmap
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.qrcode.QRCodeReader
import javax.inject.Inject

class QRCodeDecoder @Inject constructor() {
    val HINTS: MutableMap<DecodeHintType, Any?> = EnumMap(DecodeHintType::class.java)

    fun createQRCode(text: String, size: Int = 800): Bitmap? {
        return runCatching {
            val hints = mapOf(EncodeHintType.CHARACTER_SET to Charsets.UTF_8)
            val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            val pixels = IntArray(size * size) { i ->
                if (bitMatrix.get(i % size, i / size)) 0xff000000.toInt() else 0xffffffff.toInt()
            }
            createBitmap(size, size).apply {
                setPixels(pixels, 0, size, 0, 0, size, size)
            }
        }.getOrNull()
    }

    fun syncDecodeQRCode(picturePath: String): String? {
        return syncDecodeQRCode(getDecodeAbleBitmap(picturePath))
    }

    fun syncDecodeQRCode(bitmap: Bitmap?): String? {
        return bitmap?.let {
            runCatching {
                val pixels = IntArray(it.width * it.height).also { array ->
                    it.getPixels(array, 0, it.width, 0, 0, it.width, it.height)
                }
                val source = RGBLuminanceSource(it.width, it.height, pixels)
                val qrReader = QRCodeReader()

                try {
                    qrReader.decode(
                        BinaryBitmap(GlobalHistogramBinarizer(source)),
                        mapOf(DecodeHintType.TRY_HARDER to true)
                    ).text
                } catch (e: NotFoundException) {
                    qrReader.decode(
                        BinaryBitmap(GlobalHistogramBinarizer(source.invert())),
                        mapOf(DecodeHintType.TRY_HARDER to true)
                    ).text
                }
            }.getOrNull()
        }
    }

    private fun getDecodeAbleBitmap(picturePath: String): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(picturePath, options)
            var sampleSize = options.outHeight / 400
            if (sampleSize <= 0) {
                sampleSize = 1
            }
            options.inSampleSize = sampleSize
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(picturePath, options)
        } catch (e: Exception) {
            null
        }
    }

    init {
        val allFormats: List<BarcodeFormat> = arrayListOf(
            BarcodeFormat.AZTEC,
            BarcodeFormat.CODABAR,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.EAN_8,
            BarcodeFormat.EAN_13,
            BarcodeFormat.ITF,
            BarcodeFormat.MAXICODE,
            BarcodeFormat.PDF_417,
            BarcodeFormat.QR_CODE,
            BarcodeFormat.RSS_14,
            BarcodeFormat.RSS_EXPANDED,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.UPC_EAN_EXTENSION
        )
        //странно что try_harder приравнивается к QR_CODE
        HINTS[DecodeHintType.TRY_HARDER] = BarcodeFormat.QR_CODE
        HINTS[DecodeHintType.POSSIBLE_FORMATS] = allFormats
        HINTS[DecodeHintType.CHARACTER_SET] = Charsets.UTF_8
    }


}