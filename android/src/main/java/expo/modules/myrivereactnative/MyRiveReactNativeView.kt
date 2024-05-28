package expo.modules.myrivereactnative

import android.content.Context
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView
import androidx.lifecycle.*
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.controllers.RiveFileController
import app.rive.runtime.kotlin.core.*
import app.rive.runtime.kotlin.core.errors.*
import app.rive.runtime.kotlin.renderers.PointerEvents
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.HttpHeaderParser
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.ExceptionsManagerModule
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.rivereactnative.RNAlignment
import com.rivereactnative.RNDirection
import com.rivereactnative.RNFit
import com.rivereactnative.RNLoopMode
import com.rivereactnative.RNRiveError
import java.io.UnsupportedEncodingException

class MyRiveReactNativeView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
    private var riveAnimationView: RiveAnimationView = RiveAnimationView(context)
    private var resId: Int = -1
    private var url: String? = null
    private var animationName: String? = null
    private var stateMachineName: String? = null
    private var artboardName: String? = null
    private var fit: Fit = Fit.CONTAIN
    private var alignment: Alignment = Alignment.CENTER
    private var autoplay: Boolean = false
    private var shouldBeReloaded = true
    private var isUserHandlingErrors = false
    private var willDispose = false

    init {
        riveAnimationView.registerListener(object : RiveFileController.Listener {
            override fun notifyLoop(animation: PlayableInstance) {
                if (animation is LinearAnimationInstance) {
                    onLoopEnd(animation.name, RNLoopMode.mapToRNLoopMode(animation.loop))
                } else {
                    throw IllegalArgumentException("Only animation can be passed as an argument")
                }
            }

            override fun notifyPause(animation: PlayableInstance) {
                if (animation is LinearAnimationInstance) {
                    onPause(animation.name)
                }
                if (animation is StateMachineInstance) {
                    onPause(animation.name, true)
                }
            }

            override fun notifyPlay(animation: PlayableInstance) {
                if (animation is LinearAnimationInstance) {
                    onPlay(animation.name)
                }
                if (animation is StateMachineInstance) {
                    onPlay(animation.name, true)
                }
            }

            override fun notifyStateChanged(stateMachineName: String, stateName: String) {
                onStateChanged(stateMachineName, stateName)
            }

            override fun notifyStop(animation: PlayableInstance) {
                if (animation is LinearAnimationInstance) {
                    onStop(animation.name)
                }
                if (animation is StateMachineInstance) {
                    onStop(animation.name, true)
                }
            }
        })

        riveAnimationView.addEventListener(object : RiveFileController.RiveEventListener {
            override fun notifyEvent(event: RiveEvent) {
                when (event) {
                    is RiveGeneralEvent -> onRiveEventReceived(event as RiveGeneralEvent)
                    is RiveOpenURLEvent -> onRiveEventReceived(event as RiveOpenURLEvent)
                }
            }
        })

        addView(riveAnimationView)
    }

    private fun onPlay(animationName: String, isStateMachine: Boolean = false) {
        val data = Arguments.createMap()
        data.putString("animationName", animationName)
        data.putBoolean("isStateMachine", isStateMachine)
        // sendEvent("onPlay", data)
    }

    private fun onPause(animationName: String, isStateMachine: Boolean = false) {
        val data = Arguments.createMap()
        data.putString("animationName", animationName)
        data.putBoolean("isStateMachine", isStateMachine)
        // sendEvent("onPause", data)
    }

    private fun onStop(animationName: String, isStateMachine: Boolean = false) {
        val data = Arguments.createMap()
        data.putString("animationName", animationName)
        data.putBoolean("isStateMachine", isStateMachine)
        // sendEvent("onStop", data)
    }

    private fun onLoopEnd(animationName: String, loopMode: RNLoopMode) {
        val data = Arguments.createMap()
        data.putString("animationName", animationName)
        data.putString("loopMode", loopMode.toString())
        // sendEvent("onLoopEnd", data)
    }

    private fun onStateChanged(stateMachineName: String, stateName: String) {
        val data = Arguments.createMap()
        data.putString("stateMachineName", stateMachineName)
        data.putString("stateName", stateName)
        // sendEvent("onStateChanged", data)
    }

    private fun onRiveEventReceived(event: RiveEvent) {
        val eventProperties = Arguments.createMap().apply {
            putString("name", event.name)
            putDouble("delay", event.delay.toDouble())
            putMap("properties", convertHashMapToWritableMap(event.properties))
        }

        if (event is RiveOpenURLEvent) {
            eventProperties.putString("url", event.url)
            eventProperties.putString("target", event.target)
        }

        val topLevelDict = Arguments.createMap().apply {
            putMap("riveEvent", eventProperties)
        }

        // sendEvent("onRiveEventReceived", topLevelDict)
    }

    private fun convertHashMapToWritableMap(hashMap: HashMap<String, Any>): WritableMap {
        val writableMap = Arguments.createMap()
        for ((key, value) in hashMap) {
            when (value) {
                is String -> writableMap.putString(key, value)
                is Int -> writableMap.putInt(key, value)
                is Float -> writableMap.putDouble(key, value.toDouble())
                is Double -> writableMap.putDouble(key, value)
                is Boolean -> writableMap.putBoolean(key, value)
            }
        }
        return writableMap
    }

    fun play(animationName: String, rnLoopMode: RNLoopMode, rnDirection: RNDirection, isStateMachine: Boolean) {
        val loop = RNLoopMode.mapToRiveLoop(rnLoopMode)
        val direction = RNDirection.mapToRiveDirection(rnDirection)
        try {
            riveAnimationView.play(animationName, loop, direction, isStateMachine)
        } catch (ex: RiveException) {
            handleRiveException(ex)
        }
    }

    fun pause() {
        try {
            riveAnimationView.pause()
        } catch (ex: RiveException) {
            handleRiveException(ex)
        }
    }

    fun stop() {
        try {
            riveAnimationView.stop()
        } catch (ex: RiveException) {
            handleRiveException(ex)
        }
    }

    fun reset() {
        if (resId == -1) {
            riveAnimationView.artboardRenderer?.reset()
        } else {
            riveAnimationView.reset()
        }
    }

    fun touchBegan(x: Float, y: Float) {
        riveAnimationView.controller.pointerEvent(PointerEvents.POINTER_DOWN, x, y)
    }

    fun touchEnded(x: Float, y: Float) {
        riveAnimationView.controller.pointerEvent(PointerEvents.POINTER_UP, x, y)
    }

    fun setTextRunValue(textRunName: String, textValue: String) {
        try {
            riveAnimationView.controller.activeArtboard?.textRun(textRunName)?.text = textValue
        } catch (ex: RiveException) {
            handleRiveException(ex)
        }
    }

    fun update() {
        reloadIfNeeded()
    }

    fun setResourceName(resourceName: String?) {
        resourceName?.let {
            resId = resources.getIdentifier(resourceName, "raw", context.packageName)
            if (resId == 0) {
                resId = -1
            }
        } ?: run {
            resId = -1
        }
        shouldBeReloaded = true
    }

    fun setFit(rnFit: RNFit) {
        val riveFit = RNFit.mapToRiveFit(rnFit)
        this.fit = riveFit
        riveAnimationView.fit = riveFit
    }

    fun setAlignment(rnAlignment: RNAlignment) {
        val riveAlignment = RNAlignment.mapToRiveAlignment(rnAlignment)
        this.alignment = riveAlignment
        riveAnimationView.alignment = riveAlignment
    }

    fun setAutoplay(autoplay: Boolean) {
        this.autoplay = autoplay
        shouldBeReloaded = true
    }

    fun setUrl(url: String?) {
        this.url = url
        shouldBeReloaded = true
    }

    private fun reloadIfNeeded() {
        if (shouldBeReloaded) {
            if (resId != -1) {
                try {
                    riveAnimationView.setRiveResource(
                            resId,
                            artboardName,
                            animationName,
                            stateMachineName,
                            autoplay,
                            fit,
                            alignment,
                    )
                    url = null
                } catch (ex: RiveException) {
                    handleRiveException(ex)
                }
            } else if (url != null) {
                setUrlRiveResource(url!!)
            } else {
                handleFileNotFound()
            }
            shouldBeReloaded = false
        }
    }

    private fun setUrlRiveResource(url: String) {
        val queue = Volley.newRequestQueue(context)
        val stringRequest = RNRiveFileRequest(url, { bytes ->
            try {
                riveAnimationView.setRiveBytes(
                        bytes,
                        artboardName,
                        animationName,
                        stateMachineName,
                        autoplay,
                        fit,
                        alignment,
                )
            } catch (ex: RiveException) {
                handleRiveException(ex)
            }
        }, {
            if (isUserHandlingErrors) {
                val rnRiveError = RNRiveError.IncorrectRiveFileUrl
                rnRiveError.message = "Unable to download Rive file from: $url"
                sendErrorToRN(rnRiveError)
            } else {
                showRNRiveError("Unable to download Rive file $url", it)
            }
        })
        queue.add(stringRequest)
    }

    fun setArtboardName(artboardName: String) {
        try {
            this.artboardName = artboardName
            riveAnimationView.artboardName = artboardName
        } catch (ex: RiveException) {
            handleRiveException(ex)
        }
    }

    fun setAnimationName(animationName: String) {
        this.animationName = animationName
        shouldBeReloaded = true
    }

    fun setStateMachineName(stateMachineName: String) {
        this.stateMachineName = stateMachineName
        shouldBeReloaded = true
    }

    fun setIsUserHandlingErrors(isUserHandlingErrors: Boolean) {
        this.isUserHandlingErrors = isUserHandlingErrors
    }

    fun fireState(stateMachineName: String, inputName: String) {
        try {
            riveAnimationView.fireState(stateMachineName, inputName)
        } catch (ex: RiveException) {
            handleRiveException(ex)
        }
    }

    fun setBooleanState(stateMachineName: String, inputName: String, value: Boolean) {
        try {
            riveAnimationView.setBooleanState(stateMachineName, inputName, value)
        } catch (ex: RiveException) {
            handleRiveException(ex)
        }
    }

    fun setNumberState(stateMachineName: String, inputName: String, value: Float) {
        try {
            riveAnimationView.setNumberState(stateMachineName, inputName, value)
        } catch (ex: RiveException) {
            handleRiveException(ex)
        }
    }

    private fun handleRiveException(exception: RiveException) {
        if (isUserHandlingErrors) {
            val rnRiveError = RNRiveError.mapToRNRiveError(exception)
            rnRiveError?.let {
                sendErrorToRN(rnRiveError)
            }
        } else {
            showRNRiveError("${exception.message}", exception)
        }
    }

    private fun handleFileNotFound() {
        if (isUserHandlingErrors) {
            val rnRiveError = RNRiveError.FileNotFound
            rnRiveError.message = "File resource not found. You must provide correct url or resourceName!"
            sendErrorToRN(rnRiveError)
        } else {
            throw IllegalStateException("File resource not found. You must provide correct url or resourceName!")
        }
    }

    private fun sendErrorToRN(error: RNRiveError) {
        val data = Arguments.createMap()
        data.putString("type", error.toString())
        data.putString("message", error.message)
        // sendEvent("onError", data)
    }

    private fun showRNRiveError(message: String, error: Throwable) {
        val errorMap = Arguments.createMap()
        errorMap.putString("message", message)
        errorMap.putArray("stack", createStackTraceForRN(error.stackTrace))
        // exceptionManager?.reportException(errorMap)
    }

    private fun createStackTraceForRN(stackTrace: Array<StackTraceElement>): ReadableArray {
        val stackTraceReadableArray = Arguments.createArray()
        for (stackTraceElement in stackTrace) {
            val stackTraceElementMap = Arguments.createMap()
            stackTraceElementMap.putString("methodName", stackTraceElement.methodName)
            stackTraceElementMap.putInt("lineNumber", stackTraceElement.lineNumber)
            stackTraceElementMap.putString("file", stackTraceElement.fileName)
            stackTraceReadableArray.pushMap(stackTraceElementMap)
        }
        return stackTraceReadableArray
    }
}

class RNRiveFileRequest(
        url: String,
        private val listener: Response.Listener<ByteArray>,
        errorListener: Response.ErrorListener
) : Request<ByteArray>(Method.GET, url, errorListener) {
    override fun deliverResponse(response: ByteArray) = listener.onResponse(response)
    override fun parseNetworkResponse(response: NetworkResponse?): Response<ByteArray> {
        return try {
            val bytes = response?.data ?: ByteArray(0)
            Response.success(bytes, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        }
    }
}
