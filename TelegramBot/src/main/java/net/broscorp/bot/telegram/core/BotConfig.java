package net.broscorp.bot.telegram.core;

import java.util.List;
import lombok.SneakyThrows;
import net.broscorp.bot.statemachine.core.Events;
import net.broscorp.bot.statemachine.persister.StateMachinePersister;
import net.broscorp.bot.statemachine.core.States;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

@Component
public class BotConfig extends TelegramLongPollingCommandBot {

  @Value("${bot.name}")
  private String botName;

  @Value("${bot.token}")
  private String botToken;

  @Autowired
  StateMachineFactory<States, Events> stateMachineFactory;

  @Autowired
  StateMachinePersister persister;

  public BotConfig() {}

  @Override
  public String getBotUsername() {
    return botName;
  }

  @Override
  public String getBotToken() {
    return botToken;
  }

  @Override
  public void processNonCommandUpdate(Update update) {
  }

  @SneakyThrows
  @Override
  public void onUpdatesReceived(List<Update> updates) {
    for (Update update : updates) {
      if (update.getMessage().getText() != null) {
        switch (update.getMessage().getText()) {
          case ("/start"): {
            initStateMachine(update);
            break;
          }
          case ("News"): {
            changeState(update, Events.NewsSelected);
            break;
          }
          case ("Back"): {
            changeState(update, Events.MainMenuSelected);
            break;
          }
          default: {
            sendMessage(update, "Not implemented");
          }
        }
      } else {
        if (update.getMessage().hasLocation()) {
          sendMessage(update, "Not implemented");
        } else {
          sendMessage(update, "Invalid message");
        }
      }
    }
  }

  private void sendMessage(Update update, String text) throws TelegramApiException {
    SendMessage message = new SendMessage();
    message.setChatId(update.getMessage().getChatId().toString());
    message.setText("Not implemented");
    this.execute(message);
  }

  private void initStateMachine(Update update) throws Exception {
    StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();

    stateMachine.getExtendedState().getVariables()
        .put("chat", update.getMessage().getChat());
    stateMachine.getExtendedState().getVariables().put("absSender", this);

    stateMachine.sendEvent(
        Mono.just(MessageBuilder.withPayload(Events.MainMenuSelected).build())).subscribe();
    persister.persist(stateMachine, update.getMessage().getChatId().toString());
  }

  private void changeState(Update update, Events event) throws Exception {
    StateMachine<States, Events> stateMachine = persister.restore(
        stateMachineFactory.getStateMachine(), update.getMessage().getChatId().toString());
    stateMachine.sendEvent(
        Mono.just(MessageBuilder.withPayload(event).build())).subscribe();
    persister.persist(stateMachine, update.getMessage().getChatId().toString());
  }

  @Override
  public void onRegister() {}
}
