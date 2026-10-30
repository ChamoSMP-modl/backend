package gg.modl.backend.player.data.punishment;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.BiConsumer;

public enum PunishmentToggleOption {
    ALT_BLOCKING(PunishmentData.ALT_BLOCKING, "alt-blocking", PunishmentDataView::setAltBlocking),
    STAT_WIPE(PunishmentData.WIPE_AFTER_EXPIRY, "stat wipe", PunishmentDataView::setWipeAfterExpiry);

    private final String dataKey;
    private final String displayName;
    private final BiConsumer<PunishmentDataView, Boolean> mutator;

    PunishmentToggleOption(String dataKey, String displayName, BiConsumer<PunishmentDataView, Boolean> mutator) {
        this.dataKey = dataKey;
        this.displayName = displayName;
        this.mutator = mutator;
    }

    @Nullable
    public static PunishmentToggleOption from(String option) {
        if (option == null || option.isBlank()) {
            return null;
        }
        try {
            return PunishmentToggleOption.valueOf(option.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public String dataKey() {
        return dataKey;
    }

    public String displayName() {
        return displayName;
    }

    public void apply(PunishmentDataView data, boolean enabled) {
        mutator.accept(data, enabled);
    }
}
