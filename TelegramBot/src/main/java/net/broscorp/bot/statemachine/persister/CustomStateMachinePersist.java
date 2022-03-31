package net.broscorp.bot.statemachine.persister;

import java.util.HashMap;
import net.broscorp.bot.statemachine.core.Events;
import net.broscorp.bot.statemachine.core.States;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

@Component
public class CustomStateMachinePersist implements StateMachinePersist<States, Events, String> {

  private final HashMap<String, StateMachineContext<States, Events>> contexts = new HashMap<>();

  @Override
  public void write(final StateMachineContext<States, Events> context, String contextObj) {
    contexts.put(contextObj, context);
  }

  @Override
  public StateMachineContext<States, Events> read(final String contextObj) {
    return contexts.get(contextObj);
  }
}
