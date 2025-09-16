package org.librexray.vpn.data.repository_impl_test

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import org.librexray.vpn.coreandroid.utils.Constants
import org.librexray.vpn.data.repository_impl.SettingsRepositoryImpl
import org.librexray.vpn.domain.interfaces.KeyValueStorage
import org.librexray.vpn.domain.models.AppLocale
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

    private val realDefaultLocale = Locale.getDefault()

    @Before
    fun setUp() {
        storage = mockk(relaxUnitFun = true)

        every { storage.decodeSettingsString("language") } returns "system"
        every { storage.decodeSettingsString("theme") } returns "system"
        Locale.setDefault(Locale.forLanguageTag("en"))

        repo = SettingsRepositoryImpl(storage)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Locale.setDefault(realDefaultLocale)
    }

    @Test
    fun `observeLocale emits current effective locale on start (SYSTEM - en)`() = runTest {
        repo.observeLocale().test {
            assertThat(awaitItem()).isEqualTo(Locale.forLanguageTag(Constants.EN_LOCALE_TAG))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeLocale reflects SYSTEM-ru when device is ru`() = runTest {
        Locale.setDefault(Locale.forLanguageTag(Constants.RU_LOCALE_TAG))
        repo = SettingsRepositoryImpl(storage)

        repo.observeLocale().test {
            assertThat(awaitItem()).isEqualTo(Locale.forLanguageTag(Constants.RU_LOCALE_TAG))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getLocale returns RU when LANGUAGE=ru`() {
        every { storage.decodeSettingsString("language") } returns "ru"
        repo = SettingsRepositoryImpl(storage)

        val locale = repo.getLocale()
        assertThat(locale).isEqualTo(Locale.forLanguageTag(Constants.RU_LOCALE_TAG))
    }

    @Test
    fun `setLocale persists tag and updates flow`() = runTest {
        repo.observeLocale().test {
            awaitItem()
            repo.setLocale(AppLocale.RU)
            verify { storage.encodeSettingsString("language", AppLocale.RU.toTag()) }
            assertThat(awaitItem()).isEqualTo(Locale.forLanguageTag(Constants.RU_LOCALE_TAG))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SYSTEM falls back to EN for unknown device language`() {
        Locale.setDefault(Locale.forLanguageTag("fr"))
        repo = SettingsRepositoryImpl(storage)

        assertThat(repo.getLocale())
            .isEqualTo(Locale.forLanguageTag(Constants.EN_LOCALE_TAG))
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