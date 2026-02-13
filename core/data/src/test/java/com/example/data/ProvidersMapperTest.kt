package com.example.data

import com.example.data.mapper.toDomain
import com.example.model.ProviderStatus
import com.example.network.dto.apiDTO.ProviderDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProvidersMapperTest {
    @Test
    fun `ProviderDto toDomain maps fields and defaults name to empty string`() {
        val dto =
            ProviderDto(
                id = 42,
                name = null,
                webSite = "https://example.com",
                baseUrl = "https://api.example.com",
                status = ProviderStatus.Enabled,
            )

        val domain = dto.toDomain()

        assertEquals(42, domain.id)
        assertEquals("", domain.name)
        assertEquals("https://example.com", domain.webSite)
        assertEquals(ProviderStatus.Enabled, domain.status)
    }

    @Test
    fun `ProviderDto toDomain keeps nullable webSite`() {
        val dto =
            ProviderDto(
                id = 1,
                name = "Name",
                webSite = null,
                baseUrl = null,
                status = ProviderStatus.None,
            )

        val domain = dto.toDomain()

        assertNull(domain.webSite)
        assertEquals(ProviderStatus.None, domain.status)
    }

    @Test
    fun `List ProviderDto toDomain maps each element`() {
        val list =
            listOf(
                ProviderDto(
                    id = 1,
                    name = "A",
                    webSite = "a",
                    baseUrl = null,
                    status = ProviderStatus.Enabled,
                ),
                ProviderDto(
                    id = 2,
                    name = "B",
                    webSite = "b",
                    baseUrl = null,
                    status = ProviderStatus.Disables,
                ),
            )

        val domain = list.toDomain()

        assertEquals(2, domain.size)
        assertEquals(1, domain[0].id)
        assertEquals("A", domain[0].name)
        assertEquals(ProviderStatus.Enabled, domain[0].status)
        assertEquals(2, domain[1].id)
        assertEquals("B", domain[1].name)
        assertEquals(ProviderStatus.Disables, domain[1].status)
    }
}
