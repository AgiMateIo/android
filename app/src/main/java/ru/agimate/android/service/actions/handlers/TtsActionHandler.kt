package ru.agimate.android.service.actions.handlers

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.serialization.json.jsonPrimitive
import ru.agimate.android.data.model.ActionTask
import ru.agimate.android.data.model.ActionType
import ru.agimate.android.service.actions.base.IActionHandler
import ru.agimate.android.util.Logger
import java.util.Locale

class TtsActionHandler(
    private val context: Context
) : IActionHandler {

    override val actionType = ActionType.TTS

    private var tts: TextToSpeech? = null
    private var isReady = false

    override fun initialize() {
        Logger.i("Initializing TTS handler")
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                isReady = result != TextToSpeech.LANG_MISSING_DATA &&
                         result != TextToSpeech.LANG_NOT_SUPPORTED
                Logger.i("TTS initialized: ready=$isReady")
            } else {
                Logger.e("TTS initialization failed with status: $status")
                isReady = false
            }
        }
    }

    override suspend fun execute(task: ActionTask) {
        if (!isReady) {
            Logger.e("TTS is not ready, cannot execute task")
            return
        }

        val text = task.parameters["text"]?.jsonPrimitive?.content
        if (text == null) {
            Logger.e("TTS task missing 'text' parameter")
            return
        }

        Logger.i("TTS speaking: $text")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun cleanup() {
        Logger.i("Cleaning up TTS handler")
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
