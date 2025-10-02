package org.librexray.vpn.data.repository_impl_test

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import org.librexray.vpn.data.repository_impl.SettingsRepositoryImpl
import org.librexray.vpn.domain.interfaces.KeyValueStorage
import org.librexray.vpn.domain.models.ThemeMode
import io.mockk.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale

class SettingsRepositoryImplTest {
    private lateinit var storage: KeyValueStorage
    private lateinit var repo: SettingsRepositoryImpl

    @Before
    fun setUp() {
        storage = mockk(relaxUnitFun = true)

        every { storage.decodeSettingsString("language") } returns ""
        every { storage.decodeSettingsString("theme") } returns "system"
        Locale.setDefault(Locale.forLanguageTag("en"))

        repo = SettingsRepositoryImpl(storage)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observeTheme emits current value from storage`() = runTest {
        every { storage.decodeSettingsString("theme") } returns "dark"
        repo = SettingsRepositoryImpl(storage)

        repo.observeTheme().test {
            assertThat(awaitItem()).isEqualTo(ThemeMode.DARK)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTheme persists tag and updates flow`() = runTest {
        repo.observeTheme().test {
            awaitItem()
            repo.setTheme(ThemeMode.LIGHT)
            verify { storage.encodeSettingsString("theme", ThemeMode.LIGHT.toTag()) }
            assertThat(awaitItem()).isEqualTo(ThemeMode.LIGHT)
            cancelAndIgnoreRemainingEvents()
        }
    }
}