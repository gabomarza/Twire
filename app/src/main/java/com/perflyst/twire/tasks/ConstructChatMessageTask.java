package com.perflyst.twire.tasks;

import android.os.AsyncTask;

import com.perflyst.twire.chat.ChatManager;
import com.perflyst.twire.model.ChatEmote;
import com.perflyst.twire.model.ChatMessage;
import com.perflyst.twire.model.Emote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sebastian Rask Jepsen on 07/08/16.
 */
public class ConstructChatMessageTask extends AsyncTask<Void, Void, ChatMessage> {
    private Callback callback;
    private List<Emote> bttvEmotes, twitchEmotes, subscriberEmotes;
    private ChatManager chatManager;
    private String message;
    private HashMap<String, Integer> wordOccurenc;

    public ConstructChatMessageTask(Callback callback, List<Emote> bttvEmotes, List<Emote> twitchEmotes, List<Emote> subscriberEmotes, ChatManager chatManager, String message) {
        this.callback = callback;
        this.bttvEmotes = bttvEmotes;
        this.twitchEmotes = twitchEmotes;
        this.subscriberEmotes = subscriberEmotes;
        this.chatManager = chatManager;
        this.message = message;
        this.wordOccurenc = new HashMap<>();
    }

    @Override
    protected ChatMessage doInBackground(Void... voids) {
        try {

            return new ChatMessage(
                    message,
                    chatManager.getUserDisplayName(),
                    chatManager.getUserColor(),
                    chatManager.getBadgeUrls(chatManager.getUserBadges()),
                    getMessageChatEmotes(),
                    false
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(ChatMessage chatMessage) {
        super.onPostExecute(chatMessage);
        callback.onMessageConstructed(chatMessage);
    }

    private ArrayList<ChatEmote> getMessageChatEmotes() {
        ArrayList<ChatEmote> result = new ArrayList<>();
        List<String> words = Arrays.asList(message.split(" "));

        if (subscriberEmotes != null) {
            result.addAll(getEmotesFromList(words, subscriberEmotes));
        }

        if (twitchEmotes != null) {
            result.addAll(getEmotesFromList(words, twitchEmotes));
        }

        if (bttvEmotes != null) {
            result.addAll(getEmotesFromList(words, bttvEmotes));
        }

        return result;
    }

    private List<ChatEmote> getEmotesFromList(List<String> words, List<Emote> emotesToCheck) {
        List<ChatEmote> result = new ArrayList<>();

        for (String word : words) {
            if (emotesToCheck != null) {
                for (Emote emote : emotesToCheck) {
                    if (word.equals(emote.getKeyword())) {
                        String emoteUrl = chatManager.getEmoteFromId(emote.getEmoteId(), emote.isBetterTTVEmote());
                        int fromIndex = wordOccurenc.containsKey(word) ? wordOccurenc.get(word) : 0;
                        int wordIndex = message.indexOf(word, fromIndex);

                        wordOccurenc.put(word, wordIndex + word.length() - 1);

                        result.add(new ChatEmote(
                                new String[]{
                                        wordIndex + "-" + (wordIndex + word.length() - 1)
                                },
                                emoteUrl
                        ));
                    }
                }
            }
        }

        return result;
    }

    public interface Callback {
        void onMessageConstructed(ChatMessage chatMessage);
    }
}
