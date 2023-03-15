package com.example.locationworkmanager

import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val timeInterval = BuildConfig.TIME_INTERVAL // Get the time interval from BuildConfig

@HiltViewModel
class MainActivityViewModel @Inject constructor(val workManager: WorkManager): ViewModel() {

    fun queueUpWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicWorker>(timeInterval, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(PeriodicWorker.WORK_TAG)
            .build()

        stopWork()
        workManager.enqueue(periodicWorkRequest)
    }

    fun stopWork() {
        workManager.cancelAllWorkByTag(PeriodicWorker.WORK_TAG)
    }

    fun makeClickableTextView(textView: TextView, clickableText: String, onClickListener: View.OnClickListener) {
        val text = textView.text
        val spannable = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClickListener.onClick(widget)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false // remove underline from clickable text
            }
        }

        val startIndex = text.indexOf(clickableText)
        val endIndex = startIndex + clickableText.length

        spannable.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}