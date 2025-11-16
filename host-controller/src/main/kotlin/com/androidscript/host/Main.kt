package com.androidscript.host

import com.androidscript.host.cli.*

/**
 * AndroidScript Host Controller
 * Main entry point for the multi-device orchestration system
 */
fun main(args: Array<String>) {
    AndroidScriptCLI()
        .subcommands(
            ServerCommand(),
            DevicesCommand(),
            InfoCommand(),
            ExecuteCommand(),
            ScreenshotCommand()
        )
        .main(args)
}
