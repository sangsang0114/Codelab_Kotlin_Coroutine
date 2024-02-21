import kotlin.system.*
import kotlinx.coroutines.*

fun main() {
    val time = measureTimeMillis {
        runBlocking {
            println("Wheater Forecast")
            val forecast: Deferred<String> = async {
                getForecast()
            }
            val temperature: Deferred<String> = async {
                getTemperature()
            }
            println("${forecast.await()} ${temperature.await()}")
            println("Have a nice day")
        }
    }
    println(time.toDouble() / 1000)
}

suspend fun getForecast(): String {
    delay(1000)
    return "Sunny"
}

suspend fun getTemperature(): String {
    delay(1000)
    return "30\u00b0C"
}

suspend fun getWeatherReport() = coroutineScope {
    val forecast = async { getForecast() }
    val temperature = async { getTemperature() }
    "${forecast.await()} ${temperature.await()}"
}

/*
fun mainUsingLaunch() {
        runBlocking {
            println("Wheater Forecast")
            val forecast: Deferred<String> = async {
                getForecast()
            }
            val temperature: Deferred<String> = async {
                getTemperature()
            }
            println("${forecast.await()} ${temperature.await()}")
            println("Have a nice day")
        }
    }
    println(time.toDouble() / 1000)
 */
}