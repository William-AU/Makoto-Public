package bot.services;

import bot.common.CBUtils;
import bot.exceptions.schedule.ScheduleDoesNotExistException;
import bot.storage.models.BossEntity;
import bot.storage.models.DBScheduleEntity;
import bot.storage.models.ExpectedAttacksEntity;
import bot.storage.models.GuildEntity;
import bot.storage.repositories.DBScheduleRepository;
import bot.storage.repositories.ExpectedAttacksRepository;
import bot.storage.repositories.GuildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseScheduleService {
    private final DBScheduleRepository scheduleRepository;
    private final GuildRepository guildRepository;
    private final ExpectedAttacksRepository expectedAttacksRepository;

    @Autowired
    public DatabaseScheduleService(DBScheduleRepository scheduleRepository, GuildRepository guildRepository, ExpectedAttacksRepository expectedAttacksRepository) {
        this.scheduleRepository = scheduleRepository;
        this.guildRepository = guildRepository;
        this.expectedAttacksRepository = expectedAttacksRepository;
    }

    public void addUserToBoss(String guildId, int lap, int position, DBScheduleEntity.ScheduleUser user) {
        DBScheduleEntity newSchedule = new DBScheduleEntity();
        newSchedule.setGuildId(guildId);
        newSchedule.setPos(position);
        newSchedule.setLap(lap);
        int stage = CBUtils.getStageFromLap(lap);
        newSchedule.setStage(stage);
        newSchedule.setUser(user);
        scheduleRepository.save(newSchedule);
    }

    public void removeUserFromBoss(String guildId, int lap, int position, DBScheduleEntity.ScheduleUser user) {
        scheduleRepository.deleteDBScheduleEntityByLapAndPositionAndUserId(guildId, lap, position, user.getUserId());
    }

    public List<DBScheduleEntity.ScheduleUser> getUsersForBoss(String guildId, int lap, int position) {
        // In theory this can be done with a single query, but it has to be done manually because ScheduleUser is embeddable
        // and JPA doesn't work super well with embeddable classes... would be something like
        // SELECT (user_id, user_nick, has_attacked) FROM dbschedule_entity WHERE guildId = guildId AND lap = lap AND pos = pos
        // If I remember my SQL correctly (i likely don't)
        // But then again... no tuples i don't know
        List<DBScheduleEntity> schedules = scheduleRepository.getDBScheduleEntitiesByGuildIdAndLapAndPos(guildId, lap, position);
        List<DBScheduleEntity.ScheduleUser> users = new ArrayList<>();
        for (DBScheduleEntity schedule : schedules) {
            users.add(schedule.getUser());
        }
        return users;
    }

    public void toggleUserAttack(String guildId, int lap, int position, DBScheduleEntity.ScheduleUser user) {
        // Do a deep copy here to make sure removeUserFromBoss call doesn't fail - although it shouldn't be needed this might change later
        DBScheduleEntity.ScheduleUser newUser = new DBScheduleEntity.ScheduleUser();
        newUser.setUserId(user.getUserId());
        newUser.setUserNick(user.getUserNick());
        newUser.setHasAttacked(!user.isHasAttacked());
        removeUserFromBoss(guildId, lap, position, user);
        addUserToBoss(guildId, lap, position, newUser);
    }

    public void setExpectedAttacks(String guildId, int pos, int lap, int expected) {
        int stage = CBUtils.getStageFromLap(lap);
        GuildEntity guild = guildRepository.getById(guildId);
        List<ExpectedAttacksEntity> oldAttacks = guild.getExpectedAttacks();
        ExpectedAttacksEntity newAttack = new ExpectedAttacksEntity();
        newAttack.setPos(pos);
        newAttack.setStage(stage);
        newAttack.setGuildEntity(guild);

        for (ExpectedAttacksEntity attack : oldAttacks) {
            boolean stageMatches = attack.getStage() == newAttack.getStage();
            boolean posMatches = attack.getPos() == newAttack.getPos();
            if (stageMatches && posMatches) {
                oldAttacks.remove(attack);
                break;
            }
        }
        oldAttacks.add(newAttack);
        guild.setExpectedAttacks(oldAttacks);
        expectedAttacksRepository.save(newAttack);
        guildRepository.save(guild);
    }

    public void reset(String guildId) {
        scheduleRepository.deleteDBScheduleEntitiesByGuildId(guildId);
    }


}
