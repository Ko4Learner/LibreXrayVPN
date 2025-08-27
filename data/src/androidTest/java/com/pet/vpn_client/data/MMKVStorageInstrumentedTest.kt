package com.pet.vpn_client.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.pet.vpn_client.data.mmkv.MMKVStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@RunWith(AndroidJUnit4::class)
class MMKVStorageInstrumentedTest {
    private lateinit var context: Context
    private lateinit var storage: MMKVStorage

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        MMKV.initialize(context)
        MMKV.mmkvWithID("MAIN", MMKV.SINGLE_PROCESS_MODE).clearAll()
        MMKV.mmkvWithID("PROFILE_FULL_CONFIG", MMKV.SINGLE_PROCESS_MODE).clearAll()
        MMKV.mmkvWithID("SETTING", MMKV.SINGLE_PROCESS_MODE).clearAll()

        storage = MMKVStorage(Gson())
    }

    @Test fun selected_server_round_trip() {
        assertThat(storage.getSelectedServer()).isNull()
        storage.setSelectedServer("guid-1")
        assertThat(storage.getSelectedServer()).isEqualTo("guid-1")
    }

    @Test fun encodeServerConfig_generates_prepend_and_selects_if_empty() {
        val generated = storage.encodeServerConfig("", mockk<ConfigProfileItem>(relaxed = true))
        val uuid32 = Pattern.compile("^[0-9a-f]{32}$")
        assertThat(uuid32.matcher(generated).matches()).isTrue()
        assertThat(storage.decodeServerList().first()).isEqualTo(generated)
        assertThat(storage.getSelectedServer()).isEqualTo(generated)
    }

    @Test fun decodeServerConfig_null_on_blank_or_missing() {
        assertThat(storage.decodeServerConfig("")).isNull()
        assertThat(storage.decodeServerConfig("no-such")).isNull()
    }

    @Test fun removeServer_updates_list_and_selection() {
        val a = storage.encodeServerConfig("", mockk<ConfigProfileItem>(relaxed = true))
        val b = storage.encodeServerConfig("", mockk<ConfigProfileItem>(relaxed = true))

        storage.removeServer(a)
        assertThat(storage.decodeServerList()).doesNotContain(a)

        storage.setSelectedServer(b)
        assertThat(storage.getSelectedServer()).isEqualTo(b)
    }

    @Test fun settings_string_round_trip_and_nulls() {
        assertThat(storage.decodeSettingsString("LANG")).isNull()
        storage.encodeSettingsString("LANG", "en")
        assertThat(storage.decodeSettingsString("LANG")).isEqualTo("en")
        storage.encodeSettingsString("LANG", null)
        assertThat(storage.decodeSettingsString("LANG")).isNull()
    }
}