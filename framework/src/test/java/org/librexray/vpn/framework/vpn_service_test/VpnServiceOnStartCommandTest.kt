package org.librexray.vpn.framework.vpn_service_test

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.core.app.NotificationCompat
import com.google.common.truth.Truth.assertThat
import org.librexray.vpn.coreandroid.utils.Constants
import org.librexray.vpn.domain.interfaces.ServiceManager
import org.librexray.vpn.domain.interfaces.repository.ServiceStateRepository
import org.librexray.vpn.domain.state.ServiceState
import org.librexray.vpn.framework.notification.NotificationFactory
import org.librexray.vpn.framework.services.VPNService
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildService
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(
    application = HiltTestApplication::class,
    sdk = [Build.VERSION_CODES.Q]
)
@RunWith(RobolectricTestRunner::class)
class VpnServiceOnStartCommandTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var serviceManager: ServiceManager
    @Inject
    lateinit var serviceStateRepository: ServiceStateRepository

    @BindValue
    lateinit var notificationFactory: NotificationFactory

    private lateinit var controller: ServiceController<VPNService>
    private lateinit var service: VPNService

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                "test",
                "test",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        notificationFactory = mockk {
            every { createNotificationBuilder() } returns NotificationCompat.Builder(
                ctx,
                "test"
            )
                .setSmallIcon(R.drawable.ic_dialog_info)
        }

        hiltRule.inject()

        controller = buildService(VPNService::class.java)
        service = controller.get()
        controller.create()
    }

    @Test
    fun `start command calls startCoreLoop and returns sticky`() {
        every { serviceManager.startCoreLoop() } returns false

        val intent = Intent().putExtra(Constants.EXTRA_COMMAND, Constants.COMMAND_START_SERVICE)
        val res = service.onStartCommand(intent, 0, 1)

        verify { serviceManager.startCoreLoop() }
        assertThat(res).isEqualTo(Service.START_STICKY)
    }

    @Test
    fun `stop command updates state and stops service and returns not sticky`() {
        val intent = Intent().putExtra(Constants.EXTRA_COMMAND, Constants.COMMAND_STOP_SERVICE)
        val res = service.onStartCommand(intent, 0, 2)

        verify { serviceStateRepository.updateState(ServiceState.Stopped) }
        verify { serviceManager.stopCoreLoop() }
        assertThat(res).isEqualTo(Service.START_NOT_STICKY)

        val shadow = Shadows.shadowOf(service)
        assertThat(shadow.isStoppedBySelf).isTrue()
    }

    @Test
    fun `restart command delegates to service manager`() {
        val intent = Intent().putExtra(Constants.EXTRA_COMMAND, Constants.COMMAND_RESTART_SERVICE)
        service.onStartCommand(intent, 0, 3)

        verify { serviceManager.restartService() }
    }
}