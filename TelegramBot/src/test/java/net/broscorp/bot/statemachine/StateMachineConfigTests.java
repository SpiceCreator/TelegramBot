package net.broscorp.bot.statemachine;

import net.broscorp.bot.statemachine.core.Events;
import net.broscorp.bot.statemachine.core.StateMachineConfig;
import net.broscorp.bot.statemachine.core.States;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StateMachineConfig.class)
public class StateMachineConfigTests {

  @Autowired
  private StateMachineFactory<States, Events> stateMachineFactory;

  private StateMachine<States, Events> stateMachine;

  @Before
  public void setStateMachine() {
    stateMachine = stateMachineFactory.getStateMachine();
  }

  @Test
  public void testCreatingOfStateMachine() {
    Assertions.assertNotNull(stateMachine);
    Assertions.assertEquals(stateMachine.getState().getId(), States.MainMenu);
  }

  @Test
  public void testTransition() {
    Flux<StateMachineEventResult<States, Events>> resultFluxAfterRates = stateMachine.sendEvent(
        Mono.just(MessageBuilder.withPayload(Events.RatesSelected).build()));
    resultFluxAfterRates.subscribe();
    Flux<StateMachineEventResult<States, Events>> resultFluxAfterReturn = stateMachine.sendEvent(
        Mono.just(MessageBuilder.withPayload(Events.MainMenuSelected).build()));
    resultFluxAfterReturn.subscribe();

    Assertions.assertEquals(stateMachine.getState().getId(), States.MainMenu);
  }
}
