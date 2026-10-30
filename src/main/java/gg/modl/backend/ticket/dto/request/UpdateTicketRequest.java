package gg.modl.backend.ticket.dto.request;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record UpdateTicketRequest(
        @Nullable String status,
        @Nullable Boolean locked,
        @Nullable AddReplyRequest newReply,
        @Nullable AddNoteRequest newNote,
        @Nullable List<String> tags,
        @Nullable Map<String, Object> data,
        @Nullable Boolean hidden,
        @Nullable List<String> assignedTo
) {
}
