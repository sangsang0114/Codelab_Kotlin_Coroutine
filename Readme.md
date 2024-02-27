# 사전과제

[Kotlin 플레이그라운드의 코루틴 소개 (android.com)](https://developer.android.com/codelabs/basic-android-kotlin-compose-coroutines-kotlin-playground?hl=ko#4)

# 동기 호출

동기 함수는 태스크가 완전히 완료된 경우에만 리턴.
`main()`의 마지막 구문이 실행된 후에 모든 작업이 완료.

`main()`함수가 리턴되고 프로그램이 종료.

`runBlocking()` : 동기식. 람다 블록 내의 모든 작업이 완료될 때 까지는 반환되지 않음. 안드로이드에서 이는 사용하지 않고 테스트에 사용함.

`delay(n)` 호출의 작업이 완료될 때 까지 n밀리 초(ms) 대기. 다만, **Suspend function 'delay' should be called only from a coroutine or
another suspend function** 오류가 발생할 수 있기 때문에, `runBlocking` 메서드로 감싸서 사용해야한다.

```kotlin
import kotlinx.coroutines.*

fun main() {
    runBlocking {
        println("Weather forecast")
        delay(1000)
        println("Sunny")
    }
}
```

위 코드는 동기식이기 때문에, 한 번에 한 작업만을 수행할 수 있음.

## 정지 함수

### 정의

`suspend` 키워드로 정의

**코루틴이나 다른 정지 함수에서만 호출할 수 있다(정지 되었다가 나중에 다시 시작하기 위해)**

### 정지 지점

**함수 내에서 함수 실행을 정지할 수 있는 위치. 정지 함수 내에 1개 이상 존재(없을 수도 있다)**

`measureTimeMills{stmt...}` : 함수 내부를 실행하는데 걸린 총 소요시간.

`import kotlin.system.*` 추가 필요

# 비동기 코드

## 구조화된 동시성

**새 코루틴은 코루틴의 수명을 제한하는 특정 코루틴 스코프에서만 시작할 수 있다**

코드가 순차적이며 `launch()` 를 호출하는 등의 동시 실행을 명시적으로 요청하지 않는 한, 기본 이벤트 루프와 협력.

### 일반적인 가정

- 함수를 호출하면 함수 내부 구현에 사용된 코루틴 수와 무관하게 함수는 종료되기 전까지 작업을 완전히 완료 해아한다
- 함수에서 예외가 발생했을 경우에는 더 이상의 작업은 없다.

**결론: Control Flow가 함수를 벗어나면 모든 작업이 완료된 것으로 간주한다**

### launch

```kotlin
import kotlinx.coroutines.*

fun main() {
    runBlocking {
        println("Weather forecast")
        launch {
            printForecast() 
        }
      launch { 
        printTemperature()
        }
        println("Have a good day!")
    }
}

suspend fun printForecast() {
    delay(1000)
    println("Sunny")
}

suspend fun printTemperature() {
    delay(1000)
    println("30\u00b0C")
}
```

- `launch()` 함수를 사용하여 각각 자체 코루틴으로 이동시킴
- 두 개의 함수 호출이 별도의 코루틴에 존재하기 때문에, 동시에 실행함으로써, 소요 시간이 더 짧아진다. 즉, 제일 처음 호출한 printForecast() 함수내에서 delay 함수로 정지되는 동안.
  printTemperature 함수가 호출되어 delay 함수에 의해 정지된다.
- 두 번재 함수가 호출 되고 정지되는 시간에서 추가로 약간의 시간이 지나면, 첫 번째 함수에서 1초가 지나가고 남은 과정을 수행한다
- 따라서, 최소 2초가 소요된 기존 코드보다는 조금 더 적은 시간(최소 1초)이 소요된다.
- 각각의 코루틴에서 각각의 함수가 실행되기 때문에, launch 함수가 종료되기 전에 have a good day가 출력된다.

### async

코루틴으로 실행하여 호출한 함수의 예상 시간을 예측할 수 없을 때 즉, 어떠한 값을 코루틴으로 실행한 함수로부터 받아 어떠한 작업을 해야할 때 이를 기다렸다가 받을 때 사용

**Deferred<DataType> : DataType 자료형을 담는 클래스.**

`Deffered.await()` : 코루틴의 결과에 접근. 즉, 코루틴이 종료된 후 반환되는 값에 접근

**Now in Android App 참고**

### 병렬 분해

**문제를 병렬로 해결할 수 있는 더 작은 하위 태스크로 세분화**

`coroutineScope()` : 함수가 내부적으로 동시에 작업을 하고 있더라도, 모든 작업 완료 전까지는 함수가 종료되지 않기 때문에, **함수 호출자 입장에서는 동기 작업처럼 보인다**

# 예외 및 취소

### 생산자와 소비자

```kotlin
val temperature = async {
    try {
        getTemperature()
    } catch (e: AssertionError) {
        println("Caught exception $e")
        "{ No temperature found}"
    }
}
"${forecast.await()} ${temperature.await()}"
```

- 생산자 : `async()` 작업을 실행하고 결과를 생성
- 소비자 : `await()` 코루틴의 결과를 소비

### launch와 async에서의 예외 발생 시의 차이

`launch()` : 시작된 코루틴 내에서 예외가 즉시 발생하므로 `try-catch` 블록으로 감싸야 함

- 예제

    ```kotlin
    class LoginViewModel(
        private val loginRepository: LoginRepository
    ): ViewModel() {
    
    fun makeLoginRequest(username: String, token: String) {
        viewModelScope.launch {
            val jsonBody = "{ username: \"$username\", token: \"$token\"}"
            val result = try {
            loginRepository.makeLoginRequest(jsonBody)
            } catch(e: Exception) {
            Result.Error(Exception("Network request failed"))
            }
            when (result) {
                is Result.Success<LoginResponse> -> pass // Happy path
                else -> pass // Show error in UI
                }
            }
        }
    }
    ```

### 취소

이벤트로 인해 앱이 이전에 시작한 작업을 취소하게 된 경우. **사용자 주도적**

# 코루틴 개념

## 작업

`val job: Job = launch { ... }`

### 작업  취소

`job.cancel()`

## 작업 계층 구조

코루틴이 다른 코루틴을 실행하는 경우.

```kotlin
val job = launch {
    ...
    val childJob = launch { ... }
    ...
}
```

- 상위 작업이 취소되면 그의 하위 작업도 취소
- 하위 작업이 취소되더라도 상위 작업이 취소되지는 않음
- **오류 상향 전파** : 작업에서 예외가 발생할 경우, 이 예외가 상위 작업으로 전파

## CoroutineScope

코루틴이 관리되지 않아 손실되는 일을 없앰으로써 리소스 낭비 방지

## CoroutineContext

코루틴이 실행된 컨텍스트에 관한 정보를 제공. Map 자료형

- name : 코루틴 식별 (기본 값: ‘coroutine’)
- job : 코루틴의 수명 주기 제어 (기본 값: 상위 작업 없음)
- dispatcher : 작업을 적절한 스레드에 전달 (기본 값: `Dispatchers.Default`)
- Exception Handler : 코루틴에서 실행되는 코드에서 발생하는 예외 처리 (기본 값: 예외 핸들러 없음)

정의 방법: `Job() + Dispatchers.Main + excpetionHandler`

## Dispatcher

실행에 사용할 스레드를 결정하는 역할. 스레드를 시작하거나, 작업을 마친 경우 스레드를 종료한다.

- blocking(차단): 일반적인 함수의 경우로, 작업이 완료될 때 까지 호출(call) 스레드를 차단(생성X)
- non-blocking(비차단): 특정 조건이 충족될 때까지 호출 스레드를 생성하므로 그 동안 다른 작업 실행 → 비동기 함수

### 코틀린 기본 제공 Dispatcher

- `Dispatchers.Main` : Android 스레드에서 코루틴을 실행시 사용. **주로 UI 업데이트 및 상호작용 처, 빠른 작업의 처리**
- `Dispatcheres.IO`: 기본 스레드 외부에서 디스크 및 네트워크 IO작업을 실행
- `Dispatchers.Default` : 컨텍스트에 디스패처를 지정하지 않은 상태에서 `launch()` 또는 `async()` 호출 시 사용. **기본 스레드 외부에서 연산이 많이 필요한 작업 수행**