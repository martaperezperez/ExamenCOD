import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Instant;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


public class main {
    /** Application name. */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /** Directory to store authorization tokens for this application. */
    private static final String TOKENS_DIRECTORY_PATH = "resources";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     *
     * En el ejemplo original esta readonly metadatos, por lo tanto si lo dejamos asi
     * no podremos descargar ficheros, solo listarlos
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credential.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = main.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("736476175694-qnbtukpdshk30equ1rl43fb20g8fabsn.apps.googleusercontent.com");
        //returns an authorized Credential object.
        return credential;
    }
    public static void main(String[] args) throws IOException, GeneralSecurityException{
        final String token = args[0];
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();


        EmbedCreateSpec pdf = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Title")
                .url("https://neliosoftware.com/es/blog/imagenes-gratuitas-para-tu-blog/")
                .author("MARTA", "https://docs.google.com/document/d/1tydL3YBPH09OVxhUBW632ryRB_LCm30kQu0CXsFmV7o/edit?usp=sharing", "https://docs.google.com/document/d/1tydL3YBPH09OVxhUBW632ryRB_LCm30kQu0CXsFmV7o/edit?usp=sharing")
                .description("examen")
                .thumbnail("https://docs.google.com/document/d/1tydL3YBPH09OVxhUBW632ryRB_LCm30kQu0CXsFmV7o/edit?usp=sharing")
                .addField("examen", "examen", true)
                .addField("\u200B", "\u200B", true)
                .addField("examen", "examen", true)
                .image("https://docs.google.com/document/d/1tydL3YBPH09OVxhUBW632ryRB_LCm30kQu0CXsFmV7o/edit?usp=sharing")
                .timestamp(Instant.now())
                .footer("examen", "https://docs.google.com/document/d/1tydL3YBPH09OVxhUBW632ryRB_LCm30kQu0CXsFmV7o/edit?usp=sharing")
                .build();
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            if ("/pdf".equals(message.getContent())) {
                final MessageChannel channel = message.getChannel().block();
                channel.createMessage(pdf).block();

            }
        });
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Filtra para encontrar la carpeta que se llama imagenesBot
        FileList result = service.files().list()
                .setQ("name contains 'examen' and mimeType = 'application/vnd.google-apps.folder'")
                .setPageSize(100)
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();

        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            String dirPdf = null;
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
                dirPdf = file.getId();
            }
            // busco la imagen en el directorio
            FileList resultPdf = service.files().list()
                    .setQ("name contains 'examen' and parents in '"+dirPdf+"'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> filesPdf = resultPdf.getFiles();
            gateway.on(MessageCreateEvent.class).subscribe(event -> {
                final Message message = event.getMessage();
                if ("!lista_drive".equals(message.getContent())) {
                    for (File file : filesPdf) {

                        // guardamos el 'stream' en el fichero aux.jpeg qieune qe existir
                        OutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream("C:\\Users\\Interno\\Documents\\examen.pdf");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        try {
                            service.files().get(file.getId())
                                    .executeMediaAndDownloadTo(outputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final MessageChannel channel = message.getChannel().block();
                        channel.createMessage(file.getName()).block();
                    }


                }
            });
    }

        gateway.onDisconnect().block();

    }
}



