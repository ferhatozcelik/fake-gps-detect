package com.ferhatozcelik.fakegpsdetect

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Best-effort detector for fake / mock GPS usage.
 *
 * The detector combines three independent heuristics:
 *
 * 1. The legacy *Allow mock locations* developer setting.
 * 2. The presence of well-known fake GPS applications installed on the device.
 * 3. Whether the most recent known location is flagged as mock by the platform.
 *
 * No single heuristic is bulletproof, so [detect] returns a [DetectionResult] with
 * every signal and a convenience [DetectionResult.isFakeGpsDetected] flag.
 *
 * ```kotlin
 * val result = FakeGpsDetect().detect(context)
 * if (result.isFakeGpsDetected) {
 *     // handle suspicious location
 * }
 * ```
 */
class FakeGpsDetect {

    /**
     * Snapshot of every check performed by [FakeGpsDetect.detect].
     *
     * @property mockLocationEnabled the legacy mock-location setting is enabled.
     * @property fakeGpsApps package names of installed known fake GPS apps.
     * @property usingMockLocation the last known location is reported as mock.
     */
    data class DetectionResult(
        val mockLocationEnabled: Boolean,
        val fakeGpsApps: List<String>,
        val usingMockLocation: Boolean,
    ) {
        /** `true` when at least one heuristic suggests fake GPS usage. */
        val isFakeGpsDetected: Boolean
            get() = mockLocationEnabled || fakeGpsApps.isNotEmpty() || usingMockLocation
    }

    /** Runs every heuristic and returns the combined [DetectionResult]. */
    fun detect(context: Context): DetectionResult = DetectionResult(
        mockLocationEnabled = isMockLocationEnabled(context),
        fakeGpsApps = getInstalledFakeGpsApps(context),
        usingMockLocation = isUsingMockLocation(context),
    )

    /** Convenience shortcut for `detect(context).isFakeGpsDetected`. */
    fun isFakeGpsDetected(context: Context): Boolean = detect(context).isFakeGpsDetected

    /**
     * Checks whether the legacy *Allow mock locations* developer setting is enabled.
     *
     * On Android 6.0+ the setting has no effect and usually reports `false`, so this
     * check is only a weak signal.
     */
    @Suppress("DEPRECATION")
    fun isMockLocationEnabled(context: Context): Boolean = try {
        Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ALLOW_MOCK_LOCATION,
            0,
        ) != 0
    } catch (e: Settings.SettingNotFoundException) {
        false
    }

    /** Returns the package names of installed applications from [KNOWN_FAKE_GPS_APPS]. */
    fun getInstalledFakeGpsApps(context: Context): List<String> {
        val packageManager = context.packageManager
        return KNOWN_FAKE_GPS_APPS.filter { packageName ->
            try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    /** `true` when any known fake GPS application is installed. */
    fun isFakeGpsAppInstalled(context: Context): Boolean = getInstalledFakeGpsApps(context).isNotEmpty()

    /**
     * Inspects the most recent known location from any enabled provider and returns
     * whether the platform flags it as mock.
     */
    @SuppressLint("MissingPermission")
    fun isUsingMockLocation(context: Context): Boolean {
        val location = getLastKnownLocation(context) ?: return false
        return isMock(location)
    }

    /** Returns the most recent known location across all enabled providers, if permitted. */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        return try {
            manager.getProviders(true)
                .mapNotNull { provider ->
                    try {
                        manager.getLastKnownLocation(provider)
                    } catch (e: SecurityException) {
                        null
                    }
                }
                .maxByOrNull { it.time }
        } catch (e: Exception) {
            null
        }
    }

    /** `true` when the app holds coarse or fine location permission. */
    fun hasLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @Suppress("DEPRECATION")
    private fun isMock(location: Location): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            location.isFromMockProvider
        }

    @Deprecated(
        message = "Use isFakeGpsDetected(context) instead.",
        replaceWith = ReplaceWith("isFakeGpsDetected(context)"),
    )
    fun isFakeGpsAppLaod(context: Context): Boolean = isFakeGpsDetected(context)

    companion object {
        /**
         * Package names of commonly used fake GPS / mock location applications.
         *
         * Consumers can extend the detection with their own list by querying
         * [android.content.pm.PackageManager] directly.
         */
        val KNOWN_FAKE_GPS_APPS: List<String> = listOf(
            "com.lexa.fakegps",
            "com.lexa.fakegpsdonate",
            "com.fakegps.mock",
            "com.blogspot.newapphorizons.fakegps",
            "com.incorporateapps.fakegps.fre",
            "com.gsmartstudio.fakegps",
            "com.chatous.fakegps",
            "ru.gavrikov.mocklocations",
            "com.marlon.floating.fake.location",
            "com.theappninjas.fakegpsjoystick",
            "com.rosteam.gpsemulator",
            "com.fakegps.min",
            "com.fakegps.pro",
            "com.flashfake.gps",
            "com.lkr.fakelocation",
            "com.evezzon.fakegps",
        )
    }
}
