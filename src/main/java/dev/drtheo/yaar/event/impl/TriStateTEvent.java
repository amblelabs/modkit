package dev.drtheo.yaar.event.impl;

import dev.drtheo.yaar.event.TEvent;
import dev.drtheo.yaar.event.TEvents;

public abstract class TriStateTEvent<T extends TEvents> implements TEvent.Result<T, TriStateTEvent.Result> {

    protected Result result;

    @Override
    public Result result() {
        return result;
    }

    @Override
    public void handleAll(Iterable<T> subscribed) {
        for (T e : subscribed) {
            Result newRes = TEvent.handleSilent(this, e, () -> this.handle(e), Result.PASS);

            if (newRes != Result.PASS) continue;

            this.result = newRes;
            break;
        }
    }

    public abstract Result handle(T handler);

    public enum Result {
        DENY,
        PASS,
        ALLOW
    }
}
