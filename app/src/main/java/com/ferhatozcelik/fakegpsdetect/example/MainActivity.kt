package com.ferhatozcelik.fakegpsdetect.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ferhatozcelik.fakegpsdetect.FakeGpsDetect
import com.ferhatozcelik.fakegpsdetect.example.databinding.ActivityMainBinding

/**
 * Sample app that runs every [FakeGpsDetect] heuristic and displays the result.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val detector = FakeGpsDetect()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { refreshStatus() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.checkButton.setOnClickListener { refreshStatus() }

        requestLocationPermissionIfNeeded()
        refreshStatus()
    }

    private fun requestLocationPermissionIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        val missing = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing) {
            locationPermissionLauncher.launch(permissions)
        }
    }

    private fun refreshStatus() {
        val result = detector.detect(this)

        binding.status.text = getString(
            if (result.isFakeGpsDetected) R.string.status_fake else R.string.status_ok,
        )

        val apps = result.fakeGpsApps.ifEmpty { listOf(getString(R.string.none)) }
        binding.details.text = buildString {
            appendLine(getString(R.string.detail_mock_setting, result.mockLocationEnabled))
            appendLine(getString(R.string.detail_using_mock, result.usingMockLocation))
            append(getString(R.string.detail_installed_apps, apps.joinToString()))
        }
    }
}
