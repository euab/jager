package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.ConnectionStatus;
import com.euii.jager.audio.TrackResponse;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class DenverCommand extends AbstractCommand {

    public DenverCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Denver laugh";
    }

    @Override
    public String getDescription() {
        return "Listen to that famous laugh";
    }

    @Override
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("denver");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        ConnectionStatus connectionStatus = AudioHandler.connectToVoiceChannel(message);
        if (!connectionStatus.isSuccess()) {
            MessageFactory.makeWarning(message, EmoteReference.WARNING + "**" + connectionStatus.getErrorMessage() + "**")
                    .queue();
            return false;
        }

        AudioHandler.loadThenExecute(message, "https://www.youtube.com/watch?v=24qqsxow5Qk").handle((Consumer<TrackResponse>)
                (TrackResponse response) -> {
                    if (response.getController().getPlayer().isPaused()) {
                        response.getController().getPlayer().setPaused(false);
                    }
                }, throwable -> MessageFactory.makeError(message, throwable.getMessage()).queue());

        return true;
    }
}
