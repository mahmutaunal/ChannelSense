package com.mahmutalperenunal.channelsense.wifi.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConnectedWifiProviderTest {
    @Test
    fun `removes framework quotes from ssid`() {
        assertEquals("Home WiFi", normalizeSsid("\"Home WiFi\""))
    }

    @Test
    fun `preserves an unquoted ssid`() {
        assertEquals("Office", normalizeSsid("Office"))
    }

    @Test
    fun `rejects redacted and blank ssids`() {
        assertNull(normalizeSsid("<unknown ssid>"))
        assertNull(normalizeSsid("unknown ssid"))
        assertNull(normalizeSsid("  "))
        assertNull(normalizeSsid(null))
    }
}
