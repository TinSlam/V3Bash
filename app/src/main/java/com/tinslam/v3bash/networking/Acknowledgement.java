package com.tinslam.comic.networking;

import io.socket.client.Ack;

public class Acknowledgement implements Ack{
    private boolean rec = false;

    @Override
    public void call(Object... args) {
        rec = true;
    }

    public boolean isRec() {
        return rec;
    }

    public void setRec(boolean rec) {
        this.rec = rec;
    }
}
