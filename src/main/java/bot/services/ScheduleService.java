package bot.services;

import bot.storage.models.*;
import bot.storage.repositories.ScheduleDataRepository;
import bot.storage.repositories.ScheduleEntryRepository;
import com.fasterxml.jackson.core.PrettyPrinter;
import org.aspectj.weaver.ArrayReferenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleService {
    private final ScheduleEntryRepository repository;
    private final ScheduleDataRepository scheduleDataRepository;
    private final BossService bossService;

    private Map<String, Integer> cachedCurrentLaps;

    @Autowired
    public ScheduleService(ScheduleEntryRepository repository, ScheduleDataRepository scheduleDataRepository, BossService bossService) {
        this.repository = repository;
        this.scheduleDataRepository = scheduleDataRepository;
        this.bossService = bossService;

        cachedCurrentLaps = new HashMap<>();
    }

    public int getNumberOfSchedulesForGuild(String guildId) {
        return repository.getNumberOfSchedulesForGuild(guildId);
    }

    public void createNewScheduleForGuild(String guildId, String scheduleName) {
        if (scheduleName.equals("")) scheduleName = "base";
        ScheduleDataEntity data = new ScheduleDataEntity();
        data.setGuildId(guildId);
        data.setName(scheduleName);
        data.setCurrentBossPos(1);
        data.setCurrentLap(1);
        scheduleDataRepository.save(data);
    }

   public void setLapAndPosition(String guildId, int newLap, int newPos, String scheduleName) {
       System.out.println("DEBUG SETTING LAP AND POSITION");
       if (scheduleName.equals("")) scheduleName = "base";
        ScheduleDataEntity data = scheduleDataRepository.getScheduleDataEntityByGuildIdAndName(guildId, scheduleName);
        data.setCurrentLap(newLap);
        data.setCurrentBossPos(newPos);
        scheduleDataRepository.save(data);
        cachedCurrentLaps.put(scheduleName, newLap);
       System.out.println("SAVED LAP AND POSITION DATA IN DB");
   }

   public BossEntity getCurrentBoss(String guildId, String scheduleName) {
       if (scheduleName.equals("")) scheduleName = "base";
        ScheduleDataEntity data = scheduleDataRepository.getScheduleDataEntityByGuildIdAndName(guildId, scheduleName);
        int lap = data.getCurrentLap();
        int pos = data.getCurrentBossPos();
        return bossService.getBossFromLapAndPosition(lap, pos);
   }

   public int getCurrentLap(String guildId, String scheduleName) {
       if (scheduleName.equals("")) scheduleName = "base";
       Integer cachedLap = cachedCurrentLaps.get(scheduleName);
       if (cachedLap != null) return cachedLap;
       System.out.println("Getting schedule data with id: " + guildId + " and name: " + scheduleName);
       ScheduleDataEntity data = scheduleDataRepository.getScheduleDataEntityByGuildIdAndName(guildId, scheduleName);
       cachedCurrentLaps.put(scheduleName, data.getCurrentLap());
       return data.getCurrentLap();
   }

   public int getCurrentPos(String guildId, String scheduleName) {
       if (scheduleName.equals("")) scheduleName = "base";
        ScheduleDataEntity data = scheduleDataRepository.getScheduleDataEntityByGuildIdAndName(guildId, scheduleName);
       return data.getCurrentBossPos();
   }

   public List<ScheduleEntryEntity> getScheduleEntitiesForLapAndPos(String guildId, String scheduleName, int lap, int pos) {
       if (scheduleName.equals("")) scheduleName = "base";
        return repository.getAllByLapAndBossAndGuildIdAndScheduleId(lap, pos, guildId, scheduleName);
   }

   @Transactional
   public void addAttacker(String guildId, String scheduleName, int lap, int pos, String attackerId, String attackerNick, boolean isOvertime) {
       System.out.println("ScheduleService adding attacker with attackerID: " + attackerId + " and attackerNickname " + attackerNick);
       attackerNick = attackerNick.replaceAll("[^\\x00-\\x7F]", "?");
       if (scheduleName.equals("")) scheduleName = "base";
       ScheduleEntryEntity newEntry = new ScheduleEntryEntity();
        newEntry.setScheduleId(scheduleName);
        newEntry.setGuildId(guildId);
        newEntry.setLap(lap);
        newEntry.setBoss(pos);
        newEntry.setOvertime(isOvertime);
        newEntry.setAttackerId(attackerId);
        newEntry.setAttackerNick(attackerNick);
        repository.save(newEntry);
       System.out.println("ScheduleService addAttacker saved new entry: " + newEntry);
   }

   @Transactional
   public void setExpectedDamage(String guildId, String scheduleName, int pos, int lap, String attackerId, int expectedDamage) {
        ScheduleEntryEntity entry = repository.getByLapAndBossAndAttackerIdAndGuildIdAndScheduleId(lap, pos, attackerId, guildId, scheduleName);
        entry.setExpectedDamage(expectedDamage);
        repository.save(entry);
   }

   public boolean userIsAttackingBoss(String guildId, String scheduleName, String userId, int lap, int pos) {
        if (scheduleName.equals("")) scheduleName = "base";
        ScheduleEntryEntity entry = repository.getByLapAndBossAndAttackerIdAndGuildIdAndScheduleId(lap, pos, userId, guildId, scheduleName);
        return entry != null;
   }

   @Transactional
   public boolean removeUserFromBoss(String guildId, String scheduleName, String userId, int lap, int pos) {
        if (scheduleName.equals("")) scheduleName = "base";
        if (!userIsAttackingBoss(guildId, scheduleName, userId, lap, pos)) return false;
       System.out.println("Trying to remove user with guildId: " + guildId + " scheduleName: " + scheduleName + " userId: " + userId + " lap: " + lap + " pos: " + pos);
        repository.deleteScheduleEntryEntityByGuildIdAndScheduleIdAndAttackerIdAndLapAndBoss(guildId, scheduleName, userId, lap, pos);
        return true;
   }

   @Transactional
   public void deleteSchedule(String guildId, String scheduleName) {
        if (scheduleName.equals("")) scheduleName = "base";
        scheduleDataRepository.deleteByGuildIdAndName(guildId, scheduleName);
        repository.deleteScheduleEntryEntitiesByGuildIdAndScheduleId(guildId, scheduleName);
   }

   public int getExpectedAttacks(String guildId, String scheduleName, int pos) {
       if (scheduleName.equals("")) scheduleName = "base";
        ScheduleDataEntity data = scheduleDataRepository.getScheduleDataEntityByGuildIdAndName(guildId, scheduleName);
        return switch (pos) {
            case 1 -> data.getExpected_1();
            case 2 -> data.getExpected_2();
            case 3 -> data.getExpected_3();
            case 4 -> data.getExpected_4();
            case 5 -> data.getExpected_5();
            default -> -1;
        };
   }

   public void setExpectedAttacks(String guildId, String scheduleName, int pos, int expected) {
        if (scheduleName.equals("")) scheduleName = "base";
        ScheduleDataEntity data = scheduleDataRepository.getScheduleDataEntityByGuildIdAndName(guildId, scheduleName);
        switch (pos) {
            case 1 -> data.setExpected_1(expected);
            case 2 -> data.setExpected_2(expected);
            case 3 -> data.setExpected_3(expected);
            case 4 -> data.setExpected_4(expected);
            case 5 -> data.setExpected_5(expected);
        }
        scheduleDataRepository.save(data);
   }

   public List<String> getScheduleNamesForGuild(String guildId) {
        List<ScheduleDataEntity> entities = scheduleDataRepository.getScheduleDataEntitiesByGuildId(guildId);
        return new ArrayList<>() {{
            for (ScheduleDataEntity entity : entities) {
                add(entity.getName());
            }
        }};
   }

   @Transactional
   public void resetSchedule(String guildID, String scheduleName) {
       if (scheduleName.equals("")) scheduleName = "base";
       repository.deleteScheduleEntryEntitiesByGuildIdAndScheduleId(guildID, scheduleName);
       setLapAndPosition(guildID, 1, 1, scheduleName);
   }

}
