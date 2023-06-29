package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String leadUrl = "https://tetac.bitrix24.ru/rest/5123/lw2v2g8yaloxxtw5/crm.lead.list.json";
    private static final String dealUrl = "https://tetac.bitrix24.ru/rest/55/tl6u4rlm6inwyujb/crm.deal.list.json";


    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {
        List<String[]> rows = new ArrayList<>();
        String[] headers = new String[]{"EmailHash", "STATUS_ID", "UF_CRM_1669861542", "STAGE_ID", "UF_CRM_1668770545109"};

        String filePath = "data.csv";
        HttpClient httpClient = HttpClient.newHttpClient();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите данные:");
        while (scanner.hasNext()) {
            String emailhash = scanner.nextLine();

            String urlParams = String.format("order[DATE_MODIFY]=DESC&filter[UF_CRM_1654104535]=%s&select[]=UF_CRM_1669861542&select[]=*", emailhash);
            URI uri = URI.create(leadUrl + "?" + urlParams);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode resultNode = objectMapper.readTree(responseBody).get("result");
                List<Entity> entitiesForEmailhash = new ArrayList<>();
                boolean isConverted = false;
                for (JsonNode node: resultNode) {
                    String statusId = node.get("STATUS_ID").asText();
                    Entity entity = new Entity();
                    entity.setEmailhash(emailhash);
                    entity.setId(node.get("ID").asText());
                    entity.setStatusId(statusId);
                    entity.setUF_CRM_1669861542(node.get("UF_CRM_1669861542").asText());
                    entitiesForEmailhash.add(entity);
                    if (statusId.equals("CONVERTED")) {
                        isConverted = true;
                        break;
                    }
                }
                String[] rowData = null;
                if (isConverted) {
                    for (Entity entity: entitiesForEmailhash) {
                        if (entity.getStatusId().equals("CONVERTED")) {
                            rowData = printDeal(entity.getId(), entity.getEmailhash(), httpClient);
                            break;
                        }
                    }
                } else {
                    for (Entity entity: entitiesForEmailhash) {
                        if (!entity.getStatusId().equals("27")) {
                            rowData = new String[] {
                                    entity.getEmailhash(),
                                    entity.getStatusId(),
                                    entity.getUF_CRM_1669861542(),
                                    "null",
                                    "null"
                            };

                            System.out.println("STATUS_ID: " + entity.getStatusId());
                            System.out.println("UF_CRM_1669861542: " + entity.getUF_CRM_1669861542());
                            System.out.println("emailhash: " + entity.getEmailhash());
                            break;
                        } else {
                            rowData = new String[] {
                                    entity.getEmailhash(),
                                    entity.getStatusId(),
                                    "null",
                                    "null",
                                    "null"
                            };
                        }
                    }
                }

                if (rowData == null) {
                    rowData = new String[] {
                            emailhash,
                            null,
                            null,
                            null,
                            null
                    };
                    rows.add(rowData);
                } else {
                    rows.add(rowData);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            writeToCSV(headers, rows, filePath);
            rows.clear();
            Thread.sleep(1000);
        }
    }




    public static String[] printDeal(String id, String emailhash, HttpClient httpClient) {
        String[] rowData = null;

        String queryParams = String.format("filter[LEAD_ID]=%s&select[]=UF_CRM_1668770545109&select[]=*", id);
        URI uri = URI.create(dealUrl + "?" + queryParams);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode resultNode = objectMapper.readTree(responseBody).get("result");

            for (JsonNode node: resultNode) {
                String stageId = node.get("STAGE_ID").asText();
                String ufCrmField = node.get("UF_CRM_1668770545109").asText();

                rowData = new String[] {
                        emailhash,
                        "null",
                        "null",
                        stageId,
                        ufCrmField
                };
                System.out.println("STAGE_ID: " + stageId);
                System.out.println("UF_CRM_1668770545109: " + ufCrmField);
                System.out.println("emailhash: " + emailhash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rowData;
    }


    public static void writeToCSV(String[] headers, List<String[]> rows, String filePath) {

        try {
            boolean fileExists = Files.exists(Paths.get(filePath));

            CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(filePath, true), CSVFormat.DEFAULT);

            if (!fileExists) {
                csvPrinter.printRecord((Object[]) headers);
            }

            for (String[] row : rows) {
                csvPrinter.printRecord((Object[]) row);
            }

            csvPrinter.flush();
            csvPrinter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}