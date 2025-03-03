package dev.amble.lib.api.sync.manager.client;

import dev.amble.lib.api.sync.Disposable;
import dev.amble.lib.api.sync.Exclude;


public class ClientComponentData implements Disposable {
    @Exclude
    private boolean aged = false;

    public void age() {
        this.aged = true;
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isAged() {
        return aged;
    }
}
