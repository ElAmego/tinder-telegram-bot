package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBotApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "telegram-bot-name"; // necessarily
    public static final String TELEGRAM_BOT_TOKEN = "telegram-bot-token"; // necessarily
    public static final String OPEN_AI_TOKEN = "open-ai-token"; // optional

    private final ChatGPTService chatGpt = new ChatGPTService(OPEN_AI_TOKEN);
    private ArrayList<String> messagesList = new ArrayList<>();
    private DialogMode dialogMode;
    private UserInfo userInfo;
    private UserInfo friendInfo;
    private int questionCount;

    public TinderBotApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        // Menu in the TG bot.
        showMainMenu(
                "Start", "/start",
                "Tinder profile generation", "/profile",
                "Message for acquaintance", "/opener",
                "Correspondence on your behalf", "/message",
                "Messaging with stars", "/date",
                "Question to GPT bot", "/gpt"
        );

        // Reads a user message.
        final String userMessage = getMessageText();

        // Treats the command /start
        if (userMessage.equals("/start")) {
            dialogMode = DialogMode.MAIN;
            final String messageToUser = loadMessage("main");

            sendPhotoMessage("main");
            sendTextMessage(messageToUser);
            return;
        }

        // Treats the command /profile
        if (userMessage.equals("/profile")) {
            dialogMode = DialogMode.PROFILE;
            final String messageToUser = loadMessage("profile");
            userInfo = new UserInfo();
            questionCount = 0;

            sendPhotoMessage("profile");
            sendTextMessage(messageToUser);
            sendTextMessage("Как вас зовут?");
            return;
        }

        // Treats the command /opener
        if (userMessage.equals("/opener")) {
            dialogMode = DialogMode.OPENER;
            final String messageToUser = loadMessage("opener");
            friendInfo = new UserInfo();
            questionCount = 0;

            sendPhotoMessage("opener");
            sendTextMessage(messageToUser);
            sendTextMessage("Как его/её зовут?");
            return;
        }

        // Treats the command /message
        if (userMessage.equals("/message")) {
            dialogMode = DialogMode.MESSAGE;
            final String messageToUser = loadMessage("message");

            sendPhotoMessage("message");
            sendTextButtonsMessage(
                    messageToUser,
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date",
                    "Сбросить историю переписки", "reset_chat_history"
            );
            return;
        }

        // Treats the command /date
        if (userMessage.equals("/date")) {
            dialogMode = DialogMode.DATE;
            final String messageToUser = loadMessage("date");

            sendPhotoMessage("date");
            sendTextButtonsMessage(
                    messageToUser,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райан Гослинг", "date_gosling",
                    "Том Харди", "date_hardy"
            );

            return;
        }

        // Treats the command /gpt
        if (userMessage.equals("/gpt")) {
            dialogMode = DialogMode.GPT;
            final String messageToUser = loadMessage("gpt");

            sendPhotoMessage("gpt");
            sendTextMessage(messageToUser);
            return;
        }

        // Treating messages with PROFILE mode.
        if (dialogMode == DialogMode.PROFILE) {
            switch (questionCount) {
                case 0:
                    userInfo.name = userMessage;
                    questionCount++;
                    sendTextMessage("Какой у вас пол?");
                    break;
                case 1:
                    userInfo.sex = userMessage;
                    questionCount++;
                    sendTextMessage("Сколько вам лет?");
                    break;
                case 2:
                    userInfo.age = userMessage;
                    questionCount++;
                    sendTextMessage("Из какого вы города?");
                    break;
                case 3:
                    userInfo.city = userMessage;
                    questionCount++;
                    sendTextMessage("Кем вы работаете?");
                    break;
                case 4:
                    userInfo.occupation = userMessage;
                    questionCount++;
                    sendTextMessage("Есть ли у вас хобби?");
                    break;
                case 5:
                    userInfo.hobby = userMessage;
                    questionCount++;
                    sendTextMessage("Красота, привлекательность в баллах (максимум 10 баллов)");
                    break;
                case 6:
                    userInfo.handsome = userMessage;
                    questionCount++;
                    sendTextMessage("Какой у вас доход/богатство");
                    break;
                case 7:
                    userInfo.wealth = userMessage;
                    questionCount++;
                    sendTextMessage("Что вас раздражает в людях?");
                    break;
                case 8:
                    userInfo.annoys = userMessage;
                    questionCount++;
                    sendTextMessage("Какие у вас цели знакомств?");
                    break;
                case 9:
                    userInfo.goals = userMessage;
                    final String prompt = loadPrompt("profile");
                    final Message tempMessage = sendTextMessage("Подождите несколько секунд...");
                    final String respond = chatGpt.sendMessage(prompt, userInfo.toString());


                    updateTextMessage(tempMessage, respond);
                    break;
            }

            return;
        }

        // Treating messages with PROFILE mode.
        if (dialogMode == DialogMode.OPENER) {
            switch (questionCount) {
                case 0:
                    friendInfo.name = userMessage;
                    questionCount++;
                    sendTextMessage("Сколько ему/ей лет?");
                    break;
                case 1:
                    friendInfo.age = userMessage;
                    questionCount++;
                    sendTextMessage("Из какого он/она города?");
                    break;
                case 2:
                    friendInfo.city = userMessage;
                    questionCount++;
                    sendTextMessage("Кем он/она работает?");
                    break;
                case 3:
                    friendInfo.occupation = userMessage;
                    questionCount++;
                    sendTextMessage("Есть ли у него/неё хобби?");
                    break;
                case 4:
                    friendInfo.hobby = userMessage;
                    questionCount++;
                    sendTextMessage("Красота, привлекательность в баллах (максимум 10 баллов)");
                    break;
                case 5:
                    friendInfo.handsome = userMessage;
                    questionCount++;
                    sendTextMessage("Цели знакомства?");
                    break;
                case 6:
                    friendInfo.goals = userMessage;
                    final String prompt = loadPrompt("opener");
                    final Message tempMessage = sendTextMessage("Подождите несколько секунд...");
                    final String respond = chatGpt.sendMessage(prompt, friendInfo.toString());

                    updateTextMessage(tempMessage, respond);
                    return;
            }

            return;
        }

        // Treating messages with MESSAGE mode.
        if (dialogMode == DialogMode.MESSAGE) {
            final String query = getCallbackQueryButtonKey();

            if (query.startsWith("message_")) {
                final String prompt = loadPrompt(query);
                final String messages = String.join("\n\n", messagesList);
                final Message tempMessage = sendTextMessage("Подождите несколько секунд...");
                final String respond = chatGpt.sendMessage(prompt, messages);
                updateTextMessage(tempMessage, respond);
                return;
            }

            if (query.equals("reset_chat_history")) {
                messagesList.clear();
                sendTextMessage("История очищена.");
            }

            messagesList.add(userMessage);
            return;
        }

        // Treating messages with DATE mode.
        if (dialogMode == DialogMode.DATE) {
            final String query = getCallbackQueryButtonKey();
            final Message tempMessage = sendTextMessage("Подождите несколько секунд...");

            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор! Твоя задача пригласить парня/девушку на свидание за 5 сообщений.");

                final String prompt = loadPrompt(query);
                chatGpt.setPrompt(prompt);
                return;
            }

            final String respond = chatGpt.addMessage(userMessage);
            updateTextMessage(tempMessage, respond);
            return;
        }

        // Treating messages with GPT mode.
        if (dialogMode == DialogMode.GPT) {
            final Message tempMessage = sendTextMessage("Подождите несколько секунд...");
            final String gptPrompt = loadPrompt("gpt");
            final String response = chatGpt.sendMessage(gptPrompt, userMessage);

            updateTextMessage(tempMessage, response);
            return;
        }

        // Treats an incorrect text or commands.
        sendTextButtonsMessage("You wrote an incorrect command.");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBotApp());
    }
}
