package com.sma.collectivesortingtp2sma.models;

import java.util.HashMap;
import java.util.LinkedList;

public class Mailbox {
    private HashMap<Integer, LinkedList<Mail>> mailboxes;

    Mailbox(){
        mailboxes = new HashMap<Integer, LinkedList<Mail>>();
    }

    public void register(int box){
        mailboxes.put(box, new LinkedList<Mail>());
    }


    public boolean isRegister(int box){
        return mailboxes.containsKey(box);
    }

    public Mail[] retrieve(int box){
        Mail[] mails = mailboxes.get(box).toArray(new Mail[0]);
        mailboxes.get(box).clear();
        return mails;
    }

    public void send(int from, int to, Action action, Coordinates coordinates){
        if(!isRegister(to)) register(to);
        Mail mail = new Mail(from, to, action, coordinates);
        mailboxes.get(to).add(mail);
    }
}
