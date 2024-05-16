package com.jackanota.pronunciador

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.View
import android.widget.TextView
import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class EvaluatorActivity() : AppCompatActivity(), OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var textToPronounce: String
    private lateinit var tvText: TextView
    private lateinit var btnPronounce: Button
    private lateinit var speechRecognizer: SpeechRecognizer
    private val RecordAudioRequestCode = 100
    var bestMatch: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluator)

        textToPronounce = intent.getStringExtra("text").toString()
        tts = TextToSpeech(this, this)

        btnPronounce = findViewById(R.id.btn_pronounce)
        tvText = findViewById(R.id.tv_text)
        tvText.text = "Di: $textToPronounce"

        //Audio permission check
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        } else {
            initializeSpeechRecognizer()
        }
    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RecordAudioRequestCode
        )
    }

    //Process result of audio permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RecordAudioRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeSpeechRecognizer()
            } else {
                callNewToast("Debes permitir el acceso al microfono")
            }
        }
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            //Process transcription results
            override fun onResults(results: Bundle) {
                changeButtonState()

                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    bestMatch = matches[0]

                    //Compare results
                    if (bestMatch.equals(textToPronounce, ignoreCase = true)) {
                        callNewToast("Pronunciación correcta")
                    } else {
                        callNewToast("Pronunciación incorrecta")
                    }
                } else {
                    callNewToast("Intentelo de nuevo, por favor")
                }
            }

            override fun onReadyForSpeech(params: Bundle) {
                changeButtonState()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        callNewToast("No se detecto un dispositivo de entrada de voz")
                    }

                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        callNewToast("Tiempo de espera agotado, por favor intenta de nuevo")
                    }
                }
                if (btnPronounce.isEnabled == false) {
                    changeButtonState()
                }
            }

            override fun onPartialResults(partialResults: Bundle) {}

            override fun onEvent(eventType: Int, params: Bundle) {
                when (eventType) {
                    else -> callNewToast("Evento desconocido, intentalo de nuevo")
                }
                changeButtonState()
            }
        })
    }

    //Method for the pronounce button
    fun startListening(view: View) {
        //Check permission before start
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        } else {
            bestMatch = null //Clean bestMatch for new result
            speechRecognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
        }
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.ERROR) {
            tts.language = Locale.US
        }
    }

    //Listen to how the text should be pronounced
    fun listenText(view: View) {
        tts.speak(textToPronounce, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    //Method to not repeat code creating toast
    fun callNewToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    //Activate/deactivate: button for pronounce
    private fun changeButtonState() {
        btnPronounce.isEnabled = !btnPronounce.isEnabled
        btnPronounce.text = if (btnPronounce.isEnabled) "Pronunciar" else "Escuchando..."
    }

    override fun onStop() {
        super.onStop()
        if (speechRecognizer != null) {
            speechRecognizer.stopListening()
        }
    }

    //in case of closing the application
    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
