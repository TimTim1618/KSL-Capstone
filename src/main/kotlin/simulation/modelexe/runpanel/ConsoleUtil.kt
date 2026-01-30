package simulation.modelexe.runpanel

import java.time.LocalTime

object ConsoleUtil {

    fun ts(): String =
        LocalTime.now().withNano(0).toString()

    fun header(title: String): String =
        "\n===== $title | ${ts()} =====\n"

    fun tag(tag: String, msg: String): String =
        "[${ts()}] [$tag] $msg"
}
