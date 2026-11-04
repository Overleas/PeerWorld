package link.sharedworld;

import link.sharedworld.api.SharedWorldModels.ObserveWaitingResponseDto;
import link.sharedworld.api.SharedWorldModels.WorldRuntimeStatusDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SharedWorldWaitingFlowLogicTest {
    @Test
    void runtimeWithNewJoinTargetConnectsImmediately() {
        SharedWorldWaitingFlowLogic.PollDecision decision = SharedWorldWaitingFlowLogic.evaluateObservation(
                false,
                new ObserveWaitingResponseDto(
                        "connect",
                        new WorldRuntimeStatusDto("world-1", "host-live", 7L, "player-host", "Host", null, null, "new.target", null, null, null, null),
                        null,
                        null
                )
        );

        assertEquals(SharedWorldWaitingFlowLogic.Outcome.CONNECT, decision.outcome());
        assertEquals("new.target", decision.connectTarget());
        assertEquals(7L, decision.runtimeEpoch());
    }

    @Test
    void hostObservationIsRejectedBecauseWaitingPromotionMustRestart() {
        SharedWorldWaitingFlowLogic.PollDecision decision = SharedWorldWaitingFlowLogic.evaluateObservation(
                true,
                new ObserveWaitingResponseDto(
                        "host",
                        new WorldRuntimeStatusDto("world-1", "host-starting", 8L, "player-host", "Host", "player-host", "Host", null, null, null, null, null),
                        null,
                        null
                )
        );

        assertEquals(SharedWorldWaitingFlowLogic.Outcome.ERROR, decision.outcome());
        assertEquals(
                "screen.sharedworld.waiting_error_unexpected_host",
                decision.errorMessage()
        );
    }

    @Test
    void relaysProgressLabelLocalized() {
        for (String label : java.util.List.of("Uploading the world", "Відправка світу в хмару", "screen.sharedworld.progress.uploading_world")) {
            SharedWorldWaitingFlowLogic.PollDecision decision = SharedWorldWaitingFlowLogic.evaluateObservation(
                    true,
                    new ObserveWaitingResponseDto(
                            "wait",
                            new WorldRuntimeStatusDto("world-1", "host-finalizing", 8L, "player-host", "Host", null, null, null, null, null, null,
                                    new link.sharedworld.api.SharedWorldModels.StartupProgressDto(label, "indeterminate", null, null)),
                            null,
                            "waiter-1"
                    )
            );

            assertEquals(SharedWorldWaitingFlowLogic.Outcome.STAY_WAITING, decision.outcome());
            assertEquals("screen.sharedworld.progress.uploading_world", ((net.minecraft.network.chat.contents.TranslatableContents) decision.progressLabel().getContents()).getKey());
        }
    }
}
