package com.example.locationworkmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.locationworkmanager.R.layout
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    @Inject lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var button: Button
    private lateinit var buttonStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        val textView = findViewById<TextView>(R.id.hello_world)
        button = findViewById(R.id.button)
        buttonStop = findViewById(R.id.buttonStop)
        val clickableText = "Hello"
        val onClickListener = View.OnClickListener {
            Toast.makeText(this,clickableText,Toast.LENGTH_LONG).show()
        }

        /////////Task 1
        viewModel.makeClickableTextView(textView, clickableText, onClickListener)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Permissions have already been granted
            PeriodicWorker.toastResults(lifecycleScope, this, fusedLocationProviderClient)
            setUpButtons()
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    /**
     * Necessary to get back the results of the Permission Check
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions have been granted
                PeriodicWorker.toastResults(lifecycleScope, this, fusedLocationProviderClient)
                setUpButtons()

            } else {
                button.visibility = View.INVISIBLE
                buttonStop.visibility = View.INVISIBLE
                // Permissions have been denied, handle the situation accordingly
                Toast.makeText(this, "You denied the permissions", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * setup buttons with click listeners.  One button starts the task, one stops it
     */
    fun setUpButtons() {
        val onClickListenerButton = View.OnClickListener {
            viewModel.queueUpWork()
        }

        val onClickButtonStop = View.OnClickListener {
            viewModel.stopWork()
            Toast.makeText(this, "work is stopped", Toast.LENGTH_LONG).show()
        }

        button.setOnClickListener(onClickListenerButton)
        buttonStop.setOnClickListener(onClickButtonStop)
    }
}