package com.pet.vpn_client.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MainDispatcherRule(
    internal val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestRule {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            Dispatchers.setMain(testDispatcher)
            try { base.evaluate() } finally { Dispatchers.resetMain() }
        }
    }
}