import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class main {
    public static void main(String[] args) {
        final String token = "OTUzNjI5NjM2NDY4ODEzODU0.YjHWzg.3KOuFz4DtewzmeVG5xd5FBfdDMI";
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                //.color(Color.GREEN)
                .title("descargas")
                .image("C:\\Users\\Interno\\Desktop\\descargar.jpeg")
                .build();


        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            if ("!ping".equals(message.getContent())) {
                final MessageChannel channel = message.getChannel().block();
                channel.createMessage("Pong!").block();
            }
            if ("!embed".equals(message.getContent())) {
                final MessageChannel channel = message.getChannel().block();

                InputStream fileAsInputStream = null;
                try {
                    fileAsInputStream = new FileInputStream("descargar.jpeg");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                ;
                channel.createMessage(MessageCreateSpec.builder()
                        .content("content? content")
                        .addFile("descargar.jpeg", fileAsInputStream)
                        .addEmbed(embed)
                        .build()).subscribe();
            }
        });

        gateway.onDisconnect().block();
    }
}
