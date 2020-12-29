package example;

import example.pojo.EventBean;
import example.pojo.Response;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Component
public class ThisDoorBot extends TelegramLongPollingBot {
    public String getBotUsername() {
        //return "${{ secrets.BOTNAME }}";
        return System.getenv("ThisDoor_bot");   // for heroku config vars
    }

    public String getBotToken() {
        //return "${{ secrets.BOTTOKEN }}";
        return System.getenv("BOTTOKEN");   // for heroku config vars
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            handleMessage(chat_id,message_text);
        }
    }

    private void handleMessage(long chat_id, String message_text){
        String replyMessage = "wrong format";
        if(message_text.equals("/start")){
            LocalDate now = LocalDate.now();
            replyMessage = messageEvent(now.getMonth().getValue(),now.getDayOfMonth());
            try {   // send today message
                execute(sendMessage(replyMessage,chat_id));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            replyMessage = "This bot shows this day in history, type month and day separated by a space to query(e.g. 1 23)";
        }
        else{
            int month = 0;
            int day = 0;
            String [] arr = message_text.split("\\s+");
            if(arr.length > 1 && isInteger(arr[0]) && isInteger(arr[1])){   // be sure have two int args
                month = Integer.parseInt(arr[0]);
                if(month < 13 && month > 0){
                    day = Integer.parseInt(arr[1]);
                    if(day < 32 && day > 0){
                        // yes that's correct format
                        //replyMessage = "right format";
                        try{
                            replyMessage = messageEvent(month,day);
                        }catch (Exception e){
                            replyMessage = "wrong format";
                        }

                    }
                }
            }
        }
        try {
                execute(sendMessage(replyMessage,chat_id));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        return;
    }

    private static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    private String messageEvent(int month, int day) throws NullPointerException{
        StringBuilder sb = new StringBuilder();
        String url = "https://insidedoor.herokuapp.com/api/thisdayinhistory/v1/zh/" + month + "/" + day;
        RestTemplate restTemplate = new RestTemplate();
        Response response = restTemplate.getForObject(url, Response.class);
        sb.append(month).append("月").append(day).append("日").append("\n");
        for(EventBean event : response.getEvent()){
            sb.append(event.getYear()).append(" ").append(event.getTitle()).append("\n");
        }
        return sb.toString();
    }

    private SendMessage sendMessage(String msg,long chat_id){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chat_id));
        message.setText(msg);
        return message;
    }
}
