package com.sports.venue.service;

import com.sports.venue.enums.MatchStatus;
import com.sports.venue.enums.WeatherCondition;
import com.sports.venue.model.Court;
import com.sports.venue.model.Match;
import com.sports.venue.model.Stadium;
import com.sports.venue.model.WeatherRecord;
import com.sports.venue.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    @Autowired
    private WeatherRecordRepository weatherRecordRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private CancellationService cancellationService;

    private final Random random = new Random();

    public WeatherRecord getCurrentWeather(LocalDate date) {
        Optional<WeatherRecord> existing = weatherRecordRepository.findLatestByDate(date);
        if (existing.isPresent()) {
            return existing.get();
        }
        return generateWeatherRecord(date);
    }

    private WeatherRecord generateWeatherRecord(LocalDate date) {
        WeatherRecord record = new WeatherRecord();
        record.setRecordDate(date);
        
        WeatherCondition[] conditions = WeatherCondition.values();
        int index = random.nextInt(conditions.length);
        
        int weatherType = random.nextInt(10);
        if (weatherType < 7) {
            WeatherCondition[] normalConditions = {
                    WeatherCondition.SUNNY, WeatherCondition.CLOUDY, WeatherCondition.SUNNY
            };
            record.setCondition(normalConditions[random.nextInt(normalConditions.length)]);
        } else {
            WeatherCondition[] abnormalConditions = {
                    WeatherCondition.RAINY, WeatherCondition.STORMY, 
                    WeatherCondition.SNOWY, WeatherCondition.FOGGY
            };
            record.setCondition(abnormalConditions[random.nextInt(abnormalConditions.length)]);
        }

        record.setTemperature(15 + random.nextDouble() * 20);
        record.setHumidity(40 + random.nextDouble() * 40);
        record.setWindDirection("东北风");
        record.setWindSpeed(random.nextDouble() * 15);
        record.setDescription(record.getCondition().getDescription());
        record.setSource("模拟天气系统");
        record.setRecordedAt(LocalDateTime.now());

        return weatherRecordRepository.save(record);
    }

    @Scheduled(cron = "0 0 6 * * ?")
    public void dailyWeatherCheck() {
        logger.info("执行每日天气检查任务");
        LocalDate today = LocalDate.now();
        WeatherRecord weather = getCurrentWeather(today);

        if (weather.isAbnormal()) {
            logger.warn("检测到异常天气: {}, {}", weather.getCondition(), weather.getDescription());
            handleAbnormalWeather(weather, today);
        } else {
            logger.info("今日天气正常: {}", weather.getDescription());
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void hourlyWeatherCheck() {
        LocalDate today = LocalDate.now();
        WeatherRecord weather = getCurrentWeather(today);

        if (weather.isAbnormal()) {
            logger.warn("检测到异常天气: {}", weather.getCondition());
            handleAbnormalWeather(weather, today);
        }
    }

    private void handleAbnormalWeather(WeatherRecord weather, LocalDate date) {
        List<Match> matches = matchRepository.findByMatchDate(date);
        
        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.CONFIRMED && 
                match.getStatus() != MatchStatus.BOOKED) {
                continue;
            }

            if (isOutdoorMatch(match)) {
                logger.info("赛事 {} 在室外场地，天气异常，需要处理", match.getId());
                try {
                    cancellationService.cancelMatchDueToWeather(match.getId(), weather.getDescription());
                    logger.info("已取消室外赛事 {} 由于天气原因", match.getId());
                } catch (Exception e) {
                    logger.error("取消赛事时出错: {}", e.getMessage());
                }
            } else {
                logger.info("赛事 {} 在室内场地，不受天气影响", match.getId());
            }
        }
    }

    private boolean isOutdoorMatch(Match match) {
        Optional<Court> courtOpt = courtRepository.findById(match.getCourtId());
        if (!courtOpt.isPresent()) {
            return false;
        }
        Court court = courtOpt.get();
        
        Optional<Stadium> stadiumOpt = stadiumRepository.findById(court.getStadiumId());
        if (!stadiumOpt.isPresent()) {
            return false;
        }
        Stadium stadium = stadiumOpt.get();
        
        return stadium.getVenueType() == com.sports.venue.enums.VenueType.OUTDOOR;
    }

    public WeatherRecord recordManualWeather(LocalDate date, WeatherCondition condition,
                                               String description, double temperature,
                                               double humidity, String windDirection, double windSpeed) {
        WeatherRecord record = new WeatherRecord();
        record.setRecordDate(date);
        record.setCondition(condition);
        record.setDescription(description);
        record.setTemperature(temperature);
        record.setHumidity(humidity);
        record.setWindDirection(windDirection);
        record.setWindSpeed(windSpeed);
        record.setSource("人工录入");
        record.setRecordedAt(LocalDateTime.now());
        
        return weatherRecordRepository.save(record);
    }

    public List<WeatherRecord> getWeatherHistory(LocalDate startDate, LocalDate endDate) {
        return weatherRecordRepository.findByDateBetween(startDate, endDate);
    }

    public boolean isWeatherAbnormal(LocalDate date) {
        Optional<WeatherRecord> record = weatherRecordRepository.findLatestByDate(date);
        return record.map(WeatherRecord::isAbnormal).orElse(false);
    }
}