package bot.storage.units;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

public class UnitNameIDContext {
    private Map<Integer, Unit> idUnitMap;
    private Map<String, Integer> nameIDMap;

    public UnitNameIDContext() {
        idUnitMap = new HashMap<>();
        nameIDMap = null;
    }

    public void addUnit(int id, Unit unit) {
        idUnitMap.put(id, unit);
    }

    public String getEnglishNameFromID(int id) {
        return idUnitMap.get(id).getEnglishName();
    }

    public String getJapaneseNameFromID(int id) {
        return idUnitMap.get(id).getJapaneseName();
    }

    public String getIDFromJapaneseName(String name) {
        for (Map.Entry<Integer, Unit> entry : idUnitMap.entrySet()) {
            if (entry.getValue().getJapaneseName().equals(name)) {
                return entry.getKey() + "";
            }
        }
        return null;
    }

    public int getIDFromName(String name) {
        if (nameIDMap == null) {
            nameIDMap = new HashMap<>();
            for (Map.Entry<Integer, Unit> entry : idUnitMap.entrySet()) {
                String englishName = entry.getValue().getEnglishName();
                String japaneseName = entry.getValue().getJapaneseName();
                if (englishName != null) {
                    nameIDMap.put(englishName, entry.getKey());
                }
                if (japaneseName != null) {
                    nameIDMap.put(japaneseName, entry.getKey());
                }
            }
        }
        return nameIDMap.get(name);
    }

    @Data
    public static class Unit {
        private String englishName;
        private String japaneseName;

        public Unit(String englishName, String japaneseName) {
            this.englishName = englishName;
            this.japaneseName = japaneseName;
        }
    }
}
