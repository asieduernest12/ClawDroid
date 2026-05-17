package com.example.clawdroid.acceptance

import com.example.clawdroid.acceptance.scenarios.AppLaunchScenario
import com.example.clawdroid.acceptance.scenarios.ConfigScenario
import com.example.clawdroid.acceptance.scenarios.ServerScenario
import com.example.clawdroid.acceptance.scenarios.TerminalScenario
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    AppLaunchScenario::class,
    ConfigScenario::class,
    ServerScenario::class,
    TerminalScenario::class
)
class AcceptanceTestSuite
