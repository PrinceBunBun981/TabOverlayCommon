package de.codecrafter47.taboverlay.config.placeholder;

import de.codecrafter47.taboverlay.config.context.Context;
import de.codecrafter47.taboverlay.config.template.text.TextTemplate;
import de.codecrafter47.taboverlay.config.view.AbstractActiveElement;
import de.codecrafter47.taboverlay.config.view.text.TextView;
import de.codecrafter47.taboverlay.config.view.text.TextViewUpdateListener;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CustomPlaceholderAnimated extends AbstractActiveElement<Runnable> implements PlaceholderDataProvider<Context, String>, TextViewUpdateListener {

    private Future<?> task;
    private final List<TextTemplate> elements; // todo using text views instead of templates here might improve performance
    private TextView activeElement;
    private int nextElementIndex;
    private final long intervalMS;

    public CustomPlaceholderAnimated(List<TextTemplate> elements, float interval) {
        this.elements = elements;
        this.intervalMS = (long) (interval * 1000);
    }

    @Override
    public String getData() {
        return activeElement.getText();
    }

    private void switchActiveElement() {
        activeElement.deactivate();
        if (nextElementIndex >= elements.size()) {
            nextElementIndex = 0;
        }
        activeElement = elements.get(nextElementIndex++).instantiate();
        activeElement.activate(getContext(), this);
        if (hasListener()) {
            getListener().run();
        }
    }

    @Override
    protected void onActivation() {
        task = getContext().getTabEventQueue().scheduleAtFixedRate(this::switchActiveElement, intervalMS, intervalMS, TimeUnit.MILLISECONDS);
        activeElement = elements.get(0).instantiate();
        activeElement.activate(getContext(), this);
        nextElementIndex = 1;
    }

    @Override
    protected void onDeactivation() {
        task.cancel(false);
        activeElement.deactivate();
    }

    @Override
    public void onTextUpdated() {
        if (hasListener()) {
            getListener().run();
        }
    }
}