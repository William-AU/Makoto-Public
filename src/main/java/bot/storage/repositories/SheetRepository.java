package bot.storage.repositories;

import bot.storage.models.GuildEntity;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@Service
public class SheetRepository {
    private Sheets service;

    @Value("${sheet.name}")
    private String sheetName;
    @Value("${sheet.table}")
    private String sheetTable;
    @Value("${sheet.members}")
    private String sheetMembers;
    @Value("${sheet.base.id}")
    private String BASE_SPREADSHEET_ID;

    private static final String APPLICATION_NAME = "Makoto";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    public SheetRepository() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SheetRepository.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private UpdateValuesResponse writeToSpreadsheet(String spreadsheetId, List<ArrayList<Object>> values, String range, String majorDimension) {
        ValueRange valueRange = new ValueRange().setValues(Collections.unmodifiableList(values)).setMajorDimension(majorDimension);
        UpdateValuesResponse response = null;
        try {
             response = service.spreadsheets().values().update(spreadsheetId, range, valueRange)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private List<List<Object>> readFromSpreadsheet(String spreadsheetId, String range, String majorDimension) {
        try {
            return service.spreadsheets().values().get(spreadsheetId, range).setMajorDimension(majorDimension).execute().getValues();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setUsers(List<User> users, String spreadsheetId) {
        List<User> first30 = users.stream().limit(30).collect(Collectors.toList());
        List<ArrayList<Object>> arrayLists = new ArrayList<>();
        ArrayList<Object> nameList = new ArrayList<>();
        ArrayList<Object> idList = new ArrayList<>();
        for (User user : first30) {
            nameList.add(user.getName());
            idList.add(user.getId());
        }
        arrayLists.add(idList);
        arrayLists.add(nameList);
        writeToSpreadsheet(spreadsheetId, arrayLists, sheetName + sheetMembers, "COLUMNS");
        return true;
    }

    public void addBattle(String userId, GuildEntity guild, String damage) {
        final List<ArrayList<Object>> battle = Collections.singletonList(new ArrayList<>());
        battle.get(0).add(damage);
        List<List<Object>> readResults = readFromSpreadsheet(guild.getSpreadsheetId(), sheetName + sheetTable, "ROWS");

        int index = readResults.stream().map(objects -> objects.get(0)).collect(Collectors.toList()).indexOf(userId);
        int columnIndex = IntStream.range(0, readResults.size()).filter(i -> readResults.get(index).get(i).toString().isEmpty()).findFirst().orElse(-1);
        String range = sheetName + "!" + findWriteColumn(columnIndex, false) + (index + 3);
        writeToSpreadsheet(guild.getSpreadsheetId(), battle, range, "ROWS");
    }

    public void redoBattle(String userId, GuildEntity guild, String damage) {
        final List<ArrayList<Object>> battle = Collections.singletonList(new ArrayList<>());
        battle.get(0).add(damage);
        List<List<Object>> readResults = readFromSpreadsheet(guild.getSpreadsheetId(), sheetName + sheetTable, "ROWS");
        int index = readResults.stream().map(objects -> objects.get(0)).collect(Collectors.toList()).indexOf(userId);
        int columnIndex = IntStream.range(0, readResults.get(index).size()).filter(i -> readResults.get(index).get(i).toString().isEmpty()).findFirst().orElse(21); // Why 21? should this not be -1?
        String range = sheetName + "!" + findWriteColumn(columnIndex, true) + (index + 3);
        writeToSpreadsheet(guild.getSpreadsheetId(), battle, range, "ROWS");
    }

    public void addCarryover(String userId, GuildEntity guild, String damage) {
        final List<ArrayList<Object>> battle = Collections.singletonList(new ArrayList<>());
        battle.get(0).add(damage);
        List<List<Object>> readResults = readFromSpreadsheet(guild.getSpreadsheetId(), sheetName + sheetTable, "ROWS");
        int index = readResults.stream().map(objects -> objects.get(0)).collect(Collectors.toList()).indexOf(userId);
        int columnIndex = IntStream.range(0, readResults.get(index).size()).filter(i -> readResults.get(index).get(i).toString().isEmpty()).findFirst().orElse(21);
        int writeColumnIndex = findWriteColumn(columnIndex, true) - 'A';
        int sum = Integer.parseInt((String) battle.get(0).get(0))
                + Integer.parseInt(readResults.get(index).get(writeColumnIndex).toString().replaceAll("[^\\d]", "").trim());
        battle.get(0).set(0, sum);
        String range = sheetName + "!" + findWriteColumn(columnIndex, true) + (index + 3);
        writeToSpreadsheet(guild.getSpreadsheetId(), battle, range, "ROWS");
    }


    private boolean verifyIfSheetExists(String sheetName, String spreadsheetId) {
        try {
            return service.spreadsheets().get(spreadsheetId).execute()
                    .getSheets().stream()
                    .anyMatch(sheet -> sheet.getProperties().getTitle().equals(sheetName));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public SheetProperties setupBaseSpreadsheet(String spreadsheetId) {
        if (!verifyIfSheetExists(sheetName, spreadsheetId)) throw new IllegalArgumentException("Sheet already exists");
        SheetProperties properties = copySheet(BASE_SPREADSHEET_ID, 133099640, spreadsheetId);
        ArrayList<Request> requests = new ArrayList<>() {{
            new Request().setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
                    .setProperties(properties.setTitle(sheetName))
                    .setFields("title"));
        }};
        try {
            service.spreadsheets().batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests)).execute();
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeMembersFromSheet(List<String> userIds, String spreadsheetId) {
        final List<List<Object>> rows = readFromSpreadsheet(spreadsheetId, sheetName + sheetTable, "COLUMNS");
        final List<ArrayList<Object>> writeArray = new ArrayList<>();
        writeArray.add(new ArrayList<>(rows.get(0)));
        writeArray.add(new ArrayList<>(rows.get(1)));
        List<Integer> indexes = indexOfMembers(userIds, rows.get(0));
        indexes.forEach(integer -> {
            writeArray.get(0).set(integer, "");
            writeArray.get(1).set(integer, "");
        });
        writeToSpreadsheet(spreadsheetId, writeArray, sheetName + sheetMembers, "COLUMNS");
    }

    public void addMembersToSheet(List<User> userList, String spreadsheetId) {
        final List<List<Object>> rows = readFromSpreadsheet(spreadsheetId, sheetName + sheetTable, "COLUMNS");
        final List<ArrayList<Object>> writeArray = new ArrayList<>();
        AtomicInteger numberUsers = new AtomicInteger(userList.size());
        writeArray.add(new ArrayList<>(rows.get(0)));
        writeArray.add(new ArrayList<>(rows.get(1)));
        while (writeArray.get(0).size() < 30) {
            writeArray.get(0).add("");
            writeArray.get(1).add("");
        }
        IntStream.range(0, writeArray.get(0).size()).filter(i -> writeArray.get(0).get(i) == "")
                .forEach(value -> {
                    if (numberUsers.get() > 0) {
                        writeArray.get(0).set(value, userList.get(numberUsers.get() - 1).getId());
                        writeArray.get(1).set(value, userList.get(numberUsers.get() - 1).getName());
                        numberUsers.getAndDecrement();
                    }
                });
        if (numberUsers.get() != 0) throw new IllegalArgumentException();
        writeToSpreadsheet(spreadsheetId, writeArray, sheetName + sheetMembers, "COLUMNS");
    }

    private SheetProperties copySheet(String spreadsheetId, int sheetId, String targetSpreadsheetId) {
        CopySheetToAnotherSpreadsheetRequest copyRequest = new CopySheetToAnotherSpreadsheetRequest().setDestinationSpreadsheetId(targetSpreadsheetId);
        try {
            return service.spreadsheets().sheets().copyTo(spreadsheetId, sheetId, copyRequest).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private char findWriteColumn(int index, boolean update) {
        int writeIndex = index;
        if (update) {
            writeIndex = ((writeIndex - 2) % 4 == 0) ? writeIndex - 2 : writeIndex - 1;
        }
        return (char) ('A' + writeIndex);
    }

    private List<Integer> indexOfMembers(List<String> memberIds, List<Object> sheetIdColumn) {
        List<Integer> indexes = new ArrayList<>();

        List<String> ids = sheetIdColumn.stream().map(Object::toString).collect(Collectors.toList());
        ids.forEach(id -> {
            if (memberIds.stream().anyMatch(str -> str.equals(id))) {
                indexes.add(ids.indexOf(id));
            }
        });
        return indexes;
    }
}
