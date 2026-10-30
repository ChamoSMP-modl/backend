package gg.modl.backend.ticket.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TicketReply {
    private String id;
    private String name;
    private String avatar;
    private String content;
    private String type;
    private Date created;
    private boolean staff;
    private String action;
    @Builder.Default
    private List<Object> attachments = new ArrayList<>();
    private String creatorIdentifier;
}
