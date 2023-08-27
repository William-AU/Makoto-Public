package bot.utils;

import bot.storage.units.UnitNameIDContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for generating an in memory representation of unit ID and name pairs.
 * This should only be called once on startup since it is incredibly slow
 */
public class GenerateUnitIDNameMap {
    public static UnitNameIDContext generate() {
        UnitNameIDContext result = new UnitNameIDContext();
        try {
            // Use the spring utils class to access resources folder
            String englishFile = null; //ResourceUtils.getFile("classpath:iconNameCSVs/english_names.csv");
            String japaneseFile = null; //ResourceUtils.getFile("classpath:iconNameCSVs/japanese_names.csv");
            InputStream english = new ClassPathResource("iconNameCSVs/english_names.csv").getInputStream();
            InputStream japanese = new ClassPathResource(("iconNameCSVs/japanese_names.csv")).getInputStream();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(english))){
                englishFile = reader.lines().collect(Collectors.joining("\n"));
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(japanese))){
                japaneseFile = reader.lines().collect(Collectors.joining("\n"));
            }
            //System.out.println("CREATED FILES");

            Scanner englishScanner = new Scanner(englishFile);
            Scanner japaneseScanner = new Scanner(japaneseFile);
            //japaneseScanner.useLocale(Locale.JAPANESE);
            Map<Integer, UnitNameIDContext.Unit> unitMap = new HashMap<>();
            // Start with japanese because it has more units
            japaneseScanner.useDelimiter(",");
            //System.out.println(japaneseFile);
            while (japaneseScanner.hasNext()) {
                String line = japaneseScanner.nextLine();
                String[] info = line.split(",");
                //System.out.println("ID: " + info[0]);
                int id = Integer.parseInt(info[0]);
                String jpName = new String(info[1].getBytes(StandardCharsets.UTF_8));
                //System.out.println("JP Adding ID: " + info[0] + " name: " + jpName);
                unitMap.put(id, new UnitNameIDContext.Unit(null, jpName));
            }

            englishScanner.useDelimiter(",");
            while (englishScanner.hasNext()) {
                String line = englishScanner.nextLine();
                String[] info = line.split(",");
                int id = Integer.parseInt(info[0]);
                String name = info[1];
                if (unitMap.get(id) == null) {
                    //System.out.println("Found weird ID: " + id + " with name: " + name);
                    //System.out.println("Map: " + unitMap);
                } else {
                    unitMap.get(id).setEnglishName(name);
                }
            }
            for (Map.Entry<Integer, UnitNameIDContext.Unit> entry : unitMap.entrySet()) {
                result.addUnit(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        UnitNameIDContext context = generate();
        System.out.println(context.getIDFromJapaneseName(new String("風宮 あかり".getBytes(StandardCharsets.UTF_8))));
        System.out.println(new String("風宮 あかり".getBytes(StandardCharsets.UTF_8)));
        System.out.println(new String("風宮 あかり".getBytes(StandardCharsets.UTF_16)));
        System.out.println("風宮 あかり".equals(new String("風宮 あかり".getBytes(StandardCharsets.UTF_8))));
        System.out.println();
        //System.out.println(context.getIDFromName("Hiyori Harusaki"));
        //System.out.println(new String("風宮 あかり".getBytes(StandardCharsets.UTF_8)));
        /*
        try {
            File jap = ResourceUtils.getFile("classpath:iconNameCSVs/japanese_names.csv");
            Scanner sc = new Scanner(jap, "ISO8859_1");
            sc.useLocale(Locale.JAPAN);
            //sc.useDelimiter(",");
            System.out.println(sc.hasNext());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

         */

        System.out.println(System.getProperty("file.encoding"));
    }
}
