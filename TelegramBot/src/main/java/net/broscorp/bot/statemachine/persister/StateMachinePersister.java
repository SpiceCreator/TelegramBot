package net.broscorp.bot.statemachine.persister;

import net.broscorp.bot.statemachine.core.Events;
import net.broscorp.bot.statemachine.core.States;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.AbstractStateMachinePersister;
import org.springframework.stereotype.Component;

@Component
public class StateMachinePersister extends AbstractStateMachinePersister<States, Events, String> {

  public StateMachinePersister(StateMachinePersist<States, Events, String> stateMachinePersist) {
    super(stateMachinePersist);
  }

  @Override
  protected StateMachineContext<States, Events> buildStateMachineContext(
      StateMachine<States, Events> stateMachine) {
    return super.buildStateMachineContext(stateMachine);
  }
}