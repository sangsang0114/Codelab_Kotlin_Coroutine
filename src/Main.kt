import kotlin.system.*
import kotlinx.coroutines.*

fun main() {
    runBlocking {
        println("Weather forecast")
        println(getWeatherReport())
        println("Have a nice day!")
    }
}

suspend fun getForecast(): String {
    delay(1000)
    return "Sunny"
}

suspend fun getTemperature(): String {
    delay(500)
    throw AssertionError("No city Selected")
    return "30\u00b0C"
}

suspend fun getWeatherReport() = coroutineScope {
    val forecast = async { getForecast() }
    val temperature = async { getTemperature() }
    delay(200)
    temperature.cancel()
    "${forecast.await()}"
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
    }
 */