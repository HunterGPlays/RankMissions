package net.terrocidepvp.rankmissions.configuration;

import java.util.List;

public class Actions {
    private final List<String> onClick;
    private final List<String> onStart;
    private final List<String> onComplete;

    public Actions(List<String> onClick,
                   List<String> onStart,
                   List<String> onComplete) {
        this.onClick = onClick;
        this.onStart = onStart;
        this.onComplete = onComplete;
    }

    public List<String> getOnClick() {
        return onClick;
    }

    public List<String> getOnStart() {
        return onStart;
    }

    public List<String> getOnComplete() {
        return onComplete;
    }
}
