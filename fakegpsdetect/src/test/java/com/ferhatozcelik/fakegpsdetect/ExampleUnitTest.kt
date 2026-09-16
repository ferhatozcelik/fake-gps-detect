package com.ferhatozcelik.fakegpsdetect

import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun knownFakeGpsApps_isNotEmpty() {
        assertTrue(FakeGpsDetect.KNOWN_FAKE_GPS_APPS.isNotEmpty())
    }
}
