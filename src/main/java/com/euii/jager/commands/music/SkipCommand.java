package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.GuildAudioController;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collections;
import java.util.List;

public class SkipCommand extends AbstractCommand {

    public SkipCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Skip";
    }

    @Override
    public String getDescription() {
        return "Skips to the next song in the music queue. So you can escape the 10 hour videos...";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Skips to the next song in the queue");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("skip");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildAudioController controller = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (controller.getPlayer().getPlayingTrack() == null)
            return sendErrorMessage(message, "Nothing to skip. You can add songs using `!play`");

        if (!controller.getScheduler().getQueue().isEmpty()) {
            AudioHandler.skipTrack(message);
            return true;
        }

        MessageFactory.makeSuccess(message, "**Looks like we've reached the end of the line.** Since I "
        + "have nothing else to play, I'm going to leave the voice channel. You can always add me back using "
        + "`!play <song>`! :wave:").queue();

        controller.getPlayer().stopTrack();
        message.getGuild().getAudioManager().closeAudioConnection();

        return true;
    }
}
