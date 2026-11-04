package link.sharedworld;

import link.sharedworld.api.SharedWorldModels.ObserveWaitingResponseDto;
import link.sharedworld.api.SharedWorldModels.WorldRuntimeStatusDto;
import net.minecraft.network.chat.Component;

final class SharedWorldWaitingFlowLogic {
    private SharedWorldWaitingFlowLogic() {
    }

    static PollDecision evaluateObservation(boolean hostChangeFlow, ObserveWaitingResponseDto observation) {
        if (observation == null || observation.action() == null) {
            return PollDecision.error(Component.translatable("screen.sharedworld.waiting_error_missing_action").getString());
        }
        WorldRuntimeStatusDto runtime = observation.runtime();
        return switch (observation.action()) {
            case "connect" -> {
                String target = runtime == null ? null : runtime.joinTarget();
                if (target == null || target.isBlank()) {
                    yield PollDecision.error(Component.translatable("screen.sharedworld.waiting_error_missing_join_target").getString());
                }
                yield PollDecision.connect(target, runtime.runtimeEpoch());
            }
            case "host" -> PollDecision.error(Component.translatable("screen.sharedworld.waiting_error_unexpected_host").getString());
            case "restart" -> PollDecision.restart();
            case "wait" -> {
                String waiterSessionId = observation.waiterSessionId();
                if (waiterSessionId == null || waiterSessionId.isBlank()) {
                    yield PollDecision.error(Component.translatable("screen.sharedworld.waiting_error_missing_waiter_session").getString());
                }
                yield PollDecision.waiting(
                        waiterSessionId,
                        runtime,
                        statusMessageFor(hostChangeFlow, runtime),
                        progressLabelFor(runtime)
                );
            }
            default -> PollDecision.error(Component.translatable("screen.sharedworld.waiting_error_unknown_action").getString());
        };
    }

    static boolean samePlayerUuid(String left, String right) {
        return left != null && right != null && left.replace("-", "").equalsIgnoreCase(right.replace("-", ""));
    }

    private static Component progressLabelFor(WorldRuntimeStatusDto runtime) {
        if (runtime != null && runtime.startupProgress() != null) {
            String label = runtime.startupProgress().label();
            if (label != null && !label.isBlank()) {
                return localizeProgressLabel(label);
            }
        }
        if (runtime != null && "host-finalizing".equals(runtime.phase())) {
            return Component.translatable("screen.sharedworld.progress.finalizing_previous_host");
        }
        return Component.translatable("screen.sharedworld.progress.waiting_for_host");
    }

    private static Component localizeProgressLabel(String label) {
        String normalized = label.trim().toLowerCase();
        return switch (normalized) {
            case "uploading the world", "screen.sharedworld.progress.uploading_world", "release_preparing", "release_finishing" ->
                    Component.translatable("screen.sharedworld.progress.uploading_world");
            case "preparing world", "screen.sharedworld.progress.preparing_world", "preparing_world" ->
                    Component.translatable("screen.sharedworld.progress.preparing_world");
            case "finishing up", "screen.sharedworld.progress.finishing_up", "finishing_up" ->
                    Component.translatable("screen.sharedworld.progress.finishing_up");
            case "becoming the host", "screen.sharedworld.progress.becoming_host", "becoming_host" ->
                    Component.translatable("screen.sharedworld.progress.becoming_host");
            case "connecting", "screen.sharedworld.progress.connecting" ->
                    Component.translatable("screen.sharedworld.progress.connecting");
            case "saving world...", "saving world", "screen.sharedworld.progress.saving_world", "saving_world" ->
                    Component.translatable("screen.sharedworld.progress.saving_world");
            case "leaving world...", "leaving world", "screen.sharedworld.progress.leaving_world", "leaving_world" ->
                    Component.translatable("screen.sharedworld.progress.leaving_world");
            case "downloading the world", "screen.sharedworld.progress.syncing_world", "syncing_world" ->
                    Component.translatable("screen.sharedworld.progress.syncing_world");
            case "recovering your world", "screen.sharedworld.progress.recovering_local_world", "recovering_local_world" ->
                    Component.translatable("screen.sharedworld.progress.recovering_local_world");
            case "uploading unsaved changes from your last session", "screen.sharedworld.progress.uploading_local_changes", "uploading_local_changes" ->
                    Component.translatable("screen.sharedworld.progress.uploading_local_changes");
            case "waiting for the host", "screen.sharedworld.progress.waiting_for_host", "handoff_wait" ->
                    Component.translatable("screen.sharedworld.progress.waiting_for_host");
            case "waiting for the previous host to finish saving", "screen.sharedworld.progress.finalizing_previous_host", "finalizing_previous_host" ->
                    Component.translatable("screen.sharedworld.progress.finalizing_previous_host");
            default -> Component.literal(label);
        };
    }

    private static String statusMessageFor(boolean hostChangeFlow, WorldRuntimeStatusDto runtime) {
        if (runtime == null) {
            return Component.translatable("screen.sharedworld.waiting").getString();
        }
        if (runtime.candidatePlayerName() != null) {
            return Component.translatable(hostChangeFlow
                    ? "screen.sharedworld.host_change_to_player"
                    : "screen.sharedworld.joining_waiting_for", runtime.candidatePlayerName()).getString();
        }
        if ("host-finalizing".equals(runtime.phase())) {
            return Component.translatable(hostChangeFlow
                    ? "screen.sharedworld.host_change_finalizing"
                    : "screen.sharedworld.joining_finalizing").getString();
        }
        return Component.translatable(hostChangeFlow
                ? "screen.sharedworld.host_change_waiting_start"
                : "screen.sharedworld.joining_waiting_start").getString();
    }

    record PollDecision(
            Outcome outcome,
            String connectTarget,
            long runtimeEpoch,
            String waiterSessionId,
            WorldRuntimeStatusDto runtimeStatus,
            String statusMessage,
            Component progressLabel,
            String errorMessage
    ) {
        static PollDecision connect(String target, long runtimeEpoch) {
            return new PollDecision(Outcome.CONNECT, target, runtimeEpoch, null, null, null, null, null);
        }

        static PollDecision restart() {
            return new PollDecision(Outcome.RESTART, null, 0L, null, null, null, null, null);
        }

        static PollDecision waiting(String waiterSessionId, WorldRuntimeStatusDto runtimeStatus, String statusMessage, Component progressLabel) {
            return new PollDecision(Outcome.STAY_WAITING, null, 0L, waiterSessionId, runtimeStatus, statusMessage, progressLabel, null);
        }

        static PollDecision error(String errorMessage) {
            return new PollDecision(Outcome.ERROR, null, 0L, null, null, null, null, errorMessage);
        }
    }

    enum Outcome {
        CONNECT,
        RESTART,
        STAY_WAITING,
        ERROR
    }
}
