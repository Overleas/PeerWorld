package link.sharedworld.screen;

import link.sharedworld.api.SharedWorldModels.WorldSummaryDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class SharedWorldMetadataFormatTest {
    @Test
    void cleanMotdStripsMarker() {
        assertEquals("Hello world", SharedWorldMetadataFormat.cleanMotd("Hello world \u200B[pw:mp=16]"));
        assertEquals("Hello world", SharedWorldMetadataFormat.cleanMotd("Hello world\u200B[pw:mp=64]"));
        assertEquals("Hello world", SharedWorldMetadataFormat.cleanMotd("Hello world"));
        assertEquals("", SharedWorldMetadataFormat.cleanMotd(null));
    }

    @Test
    void attachMaxPlayersAppendsMarker() {
        assertEquals("Hello world \u200B[pw:mp=16]", SharedWorldMetadataFormat.attachMaxPlayers("Hello world", 16));
        // Clamping to even numbers in range [2, 64]
        assertEquals("Hello world \u200B[pw:mp=16]", SharedWorldMetadataFormat.attachMaxPlayers("Hello world \u200B[pw:mp=8]", 16));
        assertEquals("Hello world \u200B[pw:mp=64]", SharedWorldMetadataFormat.attachMaxPlayers("Hello world", 100));
        assertEquals("Hello world \u200B[pw:mp=2]", SharedWorldMetadataFormat.attachMaxPlayers("Hello world", 1));
        assertEquals("Hello world \u200B[pw:mp=20]", SharedWorldMetadataFormat.attachMaxPlayers("Hello world", 21));
    }

    @Test
    void extractMaxPlayersParsesCorrectly() {
        assertEquals(16, SharedWorldMetadataFormat.extractMaxPlayers("Hello world \u200B[pw:mp=16]"));
        assertEquals(64, SharedWorldMetadataFormat.extractMaxPlayers("Hello world \u200B[pw:mp=64]"));
        assertEquals(8, SharedWorldMetadataFormat.extractMaxPlayers("Hello world"));
        assertEquals(8, SharedWorldMetadataFormat.extractMaxPlayers(null));
    }

    @Test
    void editWorldPacksFormNormalizesUrls() {
        assertEquals("https://example.com/pack.zip", EditWorldPacksForm.normalizeUrl("example.com/pack.zip"));
        assertEquals("https://example.com/pack.zip", EditWorldPacksForm.normalizeUrl("  example.com/pack.zip  "));
        assertEquals("http://example.com/pack.zip", EditWorldPacksForm.normalizeUrl("http://example.com/pack.zip"));
        assertEquals("https://example.com/pack.zip", EditWorldPacksForm.normalizeUrl("https://example.com/pack.zip"));
        assertEquals("", EditWorldPacksForm.normalizeUrl("   "));
        assertNull(EditWorldPacksForm.normalizeUrl(null));
    }

    @Test
    void worldSummaryDtoHelpers() {
        WorldSummaryDto dto = new WorldSummaryDto(
                "w1", "slug", "My World", "owner",
                "Cool world \u200B[pw:mp=24]", null, null, 1, "idle",
                null, null, null, null, null, 0, new String[0],
                "google-drive", true, null
        );
        assertEquals("Cool world", dto.displayMotd());
        assertEquals(24, dto.maxPlayers());
    }
}
