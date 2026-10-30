package gg.modl.backend.player.controller;

import gg.modl.backend.infrastructure.rest.RESTMappingV1;
import gg.modl.backend.infrastructure.util.UuidUtils;
import gg.modl.backend.player.external.CrafatarProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(RESTMappingV1.PUBLIC_PLAYERS)
@RequiredArgsConstructor
@Validated
public class PublicPlayerController {
    private final CrafatarProxyService crafatarProxyService;

    @GetMapping("/avatar/{uuid}")
    public ResponseEntity<byte[]> proxyAvatar(
            @PathVariable String uuid,
            @RequestParam(defaultValue = "32") int size,
            @RequestParam(defaultValue = "true") boolean overlay
    ) {
        String normalized = UuidUtils.dashed(uuid);
        if (normalized == null) {
            return ResponseEntity.badRequest().build();
        }

        byte[] avatar = crafatarProxyService.getAvatar(normalized, size, overlay);
        if (avatar == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(avatar);
    }
}
