package com.sentinel.dash.api;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentinel.dash.application.DashWorkspaceService;
import com.sentinel.dash.domain.DashException;
import com.sentinel.dash.infrastructure.DashSseHub;

@RestController
public class WorkspaceController {

    private final DashWorkspaceService workspace;
    private final DashSseHub sse;

    public WorkspaceController(DashWorkspaceService workspace, DashSseHub sse) {
        this.workspace = workspace;
        this.sse = sse;
    }

    @GetMapping("/v1/workspace")
    public Map<String, Object> getWorkspace() {
        DashContext ctx = DashContext.get();
        return DashErrorWriter.success(workspace.workspace(ctx.principal(), ctx.organizationId()), null, null, null);
    }

    @GetMapping("/v1/workspace/dashboard")
    public Map<String, Object> getWorkspaceDashboard() {
        DashContext ctx = DashContext.get();
        return DashErrorWriter.success(workspace.dashboard(ctx.principal(), ctx.organizationId()), null, null, null);
    }

    @GetMapping("/v1/workspace/queues/{queueType}")
    public Map<String, Object> getWorkQueue(@PathVariable String queueType) {
        DashContext ctx = DashContext.get();
        return DashErrorWriter.success(workspace.queue(ctx.organizationId(), queueType), null, null, null);
    }

    @GetMapping("/v1/workspace/widgets")
    public Map<String, Object> listWorkspaceWidgets(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false) Integer limit) {
        DashContext ctx = DashContext.get();
        var all = workspace.widgets(ctx.organizationId());
        int cap = limit == null ? 50 : Math.max(1, Math.min(limit, 100));
        int start = 0;
        if (cursor != null) {
            for (int i = 0; i < all.size(); i++) {
                if (cursor.equals(String.valueOf(all.get(i).get("id")))) {
                    start = i + 1;
                    break;
                }
            }
        }
        int end = Math.min(all.size(), start + cap);
        boolean hasMore = end < all.size();
        String next = hasMore ? String.valueOf(all.get(end - 1).get("id")) : cursor;
        return DashErrorWriter.success(all.subList(start, end), cap, next, hasMore);
    }

    @PostMapping(value = "/v1/workspace/widgets/{widgetId}/interactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> logWidgetInteraction(@PathVariable UUID widgetId, @RequestBody JsonNode body) {
        DashContext ctx = DashContext.get();
        String type = body != null && body.get("interactionType") != null ? body.get("interactionType").asText() : null;
        return DashErrorWriter.success(workspace.interact(ctx.principal(), ctx.organizationId(), widgetId, type), null, null, null);
    }

    @GetMapping(value = "/v1/workspace/subscriptions/{channel}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeWorkspaceChannel(
            @PathVariable String channel, @RequestHeader(name = "Last-Event-ID", required = false) String lastEventId) {
        if (!DashSseHub.CHANNELS.contains(channel)) {
            throw DashException.notFound("Unknown channel");
        }
        return sse.subscribe(DashContext.get().organizationId(), channel, lastEventId);
    }
}
