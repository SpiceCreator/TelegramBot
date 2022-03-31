package net.broscorp.bot.statemachine.core;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

  @Override
  public void configure(StateMachineConfigurationConfigurer<States, Events> config)
      throws Exception {
    config
        .withConfiguration()
        .listener(stateListener())
        .autoStartup(true);
  }

  private StateMachineListener<States, Events> stateListener() {
    return new StateMachineListenerAdapter<>() {
      @Override
      public void transition(Transition<States, Events> transition) {
        log.info("User change state {} to state {}", onNullable(transition.getSource()),
            onNullable(transition.getTarget()));
      }

      private Object onNullable(State state) {
        return Optional.ofNullable(state)
            .map(State::getId)
            .orElse(null);
      }
    };
  }

  @Override
  public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
    states.withStates()
        .initial(States.Start)
        .state(States.MainMenu, mainMenuShower())
        .state(States.News, newsMenuShower())
        .state(States.Rates, ratesMenuShower())
        .state(States.Weather, weatherMenuShower());
  }

  private Action<States, Events> mainMenuShower() {
    return stateContext -> {
      KeyboardButton news = KeyboardButton.builder().text("News").build();
      KeyboardButton rates = KeyboardButton.builder().text("Rates").build();
      KeyboardButton weather = KeyboardButton.builder().text("Weather").requestLocation(true).build();

      KeyboardRow newsRow = new KeyboardRow();
      newsRow.add(news);
      KeyboardRow ratesRow = new KeyboardRow();
      ratesRow.add(rates);
      KeyboardRow weatherRow = new KeyboardRow();
      weatherRow.add(weather);

      sendMessage(stateContext, new KeyboardRow[]{newsRow, ratesRow, weatherRow}, "Hello. What's next?");
    };
  }

  private Action<States, Events> newsMenuShower() {
    return stateContext -> {
      KeyboardButton dou = KeyboardButton.builder().text("DOU").build();
      KeyboardButton bbc = KeyboardButton.builder().text("BBC").build();
      KeyboardButton newYorkTimes = KeyboardButton.builder().text("New York Times").build();
      KeyboardButton back = KeyboardButton.builder().text("Back").build();

      KeyboardRow topRow = new KeyboardRow();
      topRow.add(dou);
      topRow.add(bbc);
      KeyboardRow bottomRow = new KeyboardRow();
      bottomRow.add(newYorkTimes);
      bottomRow.add(back);

      sendMessage(stateContext, new KeyboardRow[]{topRow, bottomRow}, "News Selected");
    };
  }

  private Action<States, Events> ratesMenuShower() {
    return stateContext -> log.info("Rates Menu showed");
  }

  private Action<States, Events> weatherMenuShower() {
    return stateContext -> log.info("Weather Menu showed");
  }

  @Override
  public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
      throws Exception {
    transitions
        .withExternal().source(States.Start).event(Events.MainMenuSelected).target(States.MainMenu)
        .and()
        .withExternal().source(States.MainMenu).event(Events.NewsSelected).target(States.News)
        .and()
        .withExternal().source(States.MainMenu).event(Events.RatesSelected).target(States.Rates)
        .and()
        .withExternal().source(States.MainMenu).event(Events.WeatherSelected).target(States.Weather)
        .and()
        .withExternal().source(States.News).event(Events.MainMenuSelected).target(States.MainMenu)
        .and()
        .withExternal().source(States.Rates).event(Events.MainMenuSelected).target(States.MainMenu)
        .and()
        .withExternal().source(States.Weather).event(Events.MainMenuSelected)
        .target(States.MainMenu);
  }

  private void sendMessage(StateContext stateContext, KeyboardRow[] keyboardRows, String text) {
    ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder keyboard = ReplyKeyboardMarkup.builder();

    for (KeyboardRow keyboardRow : keyboardRows) {
        keyboard.keyboardRow(keyboardRow);
    }

    SendMessage message = new SendMessage();
    message.setReplyMarkup(keyboard.build());
    Chat chat = (Chat) stateContext.getExtendedState().getVariables().get("chat");
    System.out.println("chat = " + chat);
    message.setChatId(chat.getId().toString());
    message.setText(text);
    AbsSender absSender = (AbsSender) stateContext.getExtendedState().getVariables()
        .get("absSender");
    try {
      absSender.execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }
}
