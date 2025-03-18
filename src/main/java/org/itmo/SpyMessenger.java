package org.itmo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class SpyMessenger {
    private static final int MAX_MESSAGES = 5;
    private static final long MESSAGE_LIFETIME_MS = 1500;

    private final Map<String, List<Message>> userMessages = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    void sendMessage(String sender, String receiver, String message, String passcode) {
        userMessages.putIfAbsent(receiver, new ArrayList<>());
        List<Message> messages = userMessages.get(receiver);

        if (messages.size() >= MAX_MESSAGES) {
            messages.remove(0);
        }

        Message newMessage = new Message(sender, receiver, message, passcode);
        messages.add(newMessage);

        scheduleMessageDeletion(newMessage);
    }

    String readMessage(String user, String passcode) {
        List<Message> messages = userMessages.get(user);
        if (messages == null) {
            return null;
        }

        Iterator<Message> iterator = messages.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (message.passcode.equals(passcode)) {
                iterator.remove();
                return message.content;
            }
        }
        return null;
    }

    private void scheduleMessageDeletion(Message message) {
        scheduler.schedule(() -> {
            List<Message> messages = userMessages.get(message.receiver);
            if (messages != null) {
                messages.remove(message);
            }
        }, MESSAGE_LIFETIME_MS, TimeUnit.MILLISECONDS);
    }

    private static class Message {
        String sender;
        String receiver;
        String content;
        String passcode;

        Message(String sender, String receiver, String content, String passcode) {
            this.sender = sender;
            this.receiver = receiver;
            this.content = content;
            this.passcode = passcode;
        }
    }
}