package com.sports.venue.config;

import com.sports.venue.enums.SportType;
import com.sports.venue.enums.VenueType;
import com.sports.venue.enums.WeatherCondition;
import com.sports.venue.model.*;
import com.sports.venue.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private RefereeRepository refereeRepository;

    @Autowired
    private WeatherRecordRepository weatherRecordRepository;

    @Override
    public void run(String... args) {
        logger.info("开始初始化基础数据...");

        initializeStadiumsAndCourts();
        initializeReferees();
        initializeWeather();

        logger.info("基础数据初始化完成！");
    }

    private void initializeStadiumsAndCourts() {
        Stadium indoorStadium = new Stadium(
                "城市室内体育馆",
                "北京市朝阳区体育馆路1号",
                VenueType.INDOOR
        );
        indoorStadium.setSupportedSports(Arrays.asList(
                SportType.BASKETBALL, SportType.BADMINTON, 
                SportType.TABLE_TENNIS, SportType.VOLLEYBALL
        ));
        indoorStadium = stadiumRepository.save(indoorStadium);
        logger.info("已创建场馆: {}", indoorStadium.getName());

        Stadium outdoorStadium = new Stadium(
                "城市运动公园",
                "北京市海淀区公园路88号",
                VenueType.OUTDOOR
        );
        outdoorStadium.setSupportedSports(Arrays.asList(
                SportType.FOOTBALL, SportType.TENNIS, SportType.BASKETBALL
        ));
        outdoorStadium = stadiumRepository.save(outdoorStadium);
        logger.info("已创建场馆: {}", outdoorStadium.getName());

        Court basketballCourt1 = createCourt(
                indoorStadium.getId(), "篮球馆1号场地", "B-001",
                Arrays.asList(SportType.BASKETBALL), 100,
                new BigDecimal("200.00"), new BigDecimal("250.00")
        );
        logger.info("已创建场地: {}", basketballCourt1.getName());

        Court basketballCourt2 = createCourt(
                indoorStadium.getId(), "篮球馆2号场地", "B-002",
                Arrays.asList(SportType.BASKETBALL), 100,
                new BigDecimal("200.00"), new BigDecimal("250.00")
        );
        logger.info("已创建场地: {}", basketballCourt2.getName());

        Court badmintonCourt1 = createCourt(
                indoorStadium.getId(), "羽毛球馆1号场地", "BM-001",
                Arrays.asList(SportType.BADMINTON), 20,
                new BigDecimal("80.00"), new BigDecimal("100.00")
        );
        logger.info("已创建场地: {}", badmintonCourt1.getName());

        Court badmintonCourt2 = createCourt(
                indoorStadium.getId(), "羽毛球馆2号场地", "BM-002",
                Arrays.asList(SportType.BADMINTON), 20,
                new BigDecimal("80.00"), new BigDecimal("100.00")
        );
        logger.info("已创建场地: {}", badmintonCourt2.getName());

        Court tableTennisCourt = createCourt(
                indoorStadium.getId(), "乒乓球馆", "TT-001",
                Arrays.asList(SportType.TABLE_TENNIS), 30,
                new BigDecimal("50.00"), new BigDecimal("60.00")
        );
        logger.info("已创建场地: {}", tableTennisCourt.getName());

        Court footballField = createCourt(
                outdoorStadium.getId(), "足球场", "F-001",
                Arrays.asList(SportType.FOOTBALL), 200,
                new BigDecimal("300.00"), new BigDecimal("400.00")
        );
        logger.info("已创建场地: {}", footballField.getName());

        Court tennisCourt1 = createCourt(
                outdoorStadium.getId(), "网球场1号", "T-001",
                Arrays.asList(SportType.TENNIS), 8,
                new BigDecimal("150.00"), new BigDecimal("200.00")
        );
        logger.info("已创建场地: {}", tennisCourt1.getName());

        Court tennisCourt2 = createCourt(
                outdoorStadium.getId(), "网球场2号", "T-002",
                Arrays.asList(SportType.TENNIS), 8,
                new BigDecimal("150.00"), new BigDecimal("200.00")
        );
        logger.info("已创建场地: {}", tennisCourt2.getName());

        Court outdoorBasketball = createCourt(
                outdoorStadium.getId(), "室外篮球场", "OB-001",
                Arrays.asList(SportType.BASKETBALL), 50,
                new BigDecimal("100.00"), new BigDecimal("120.00")
        );
        logger.info("已创建场地: {}", outdoorBasketball.getName());
    }

    private Court createCourt(String stadiumId, String name, String code,
                               List<SportType> sports, int capacity,
                               BigDecimal hourlyRate, BigDecimal overtimeRate) {
        Court court = new Court();
        court.setStadiumId(stadiumId);
        court.setName(name);
        court.setCode(code);
        court.setSupportedSports(sports);
        court.setCapacity(capacity);
        court.setHourlyRate(hourlyRate);
        court.setOvertimeRate(overtimeRate);
        court.setActive(true);

        for (DayOfWeek day : DayOfWeek.values()) {
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                court.getAvailableHours().put(day, new Court.TimeSlot(LocalTime.of(8, 0), LocalTime.of(22, 0)));
            } else {
                court.getAvailableHours().put(day, new Court.TimeSlot(LocalTime.of(9, 0), LocalTime.of(21, 0)));
            }
        }

        return courtRepository.save(court);
    }

    private void initializeReferees() {
        Referee referee1 = createReferee(
                "张三", "13800138001", "110101199001011234",
                Arrays.asList(SportType.BASKETBALL, SportType.FOOTBALL),
                "一级裁判", new BigDecimal("200.00")
        );
        logger.info("已创建裁判: {}", referee1.getName());

        Referee referee2 = createReferee(
                "李四", "13800138002", "110101198505155678",
                Arrays.asList(SportType.BADMINTON, SportType.TENNIS),
                "国家级裁判", new BigDecimal("300.00")
        );
        logger.info("已创建裁判: {}", referee2.getName());

        Referee referee3 = createReferee(
                "王五", "13800138003", "110101198808089012",
                Arrays.asList(SportType.FOOTBALL, SportType.VOLLEYBALL),
                "一级裁判", new BigDecimal("180.00")
        );
        logger.info("已创建裁判: {}", referee3.getName());

        Referee referee4 = createReferee(
                "赵六", "13800138004", "110101199202023456",
                Arrays.asList(SportType.TABLE_TENNIS, SportType.BADMINTON),
                "二级裁判", new BigDecimal("150.00")
        );
        logger.info("已创建裁判: {}", referee4.getName());

        Referee referee5 = createReferee(
                "钱七", "13800138005", "110101198707077890",
                Arrays.asList(SportType.BASKETBALL, SportType.VOLLEYBALL),
                "国家级裁判", new BigDecimal("350.00")
        );
        logger.info("已创建裁判: {}", referee5.getName());
    }

    private Referee createReferee(String name, String phone, String idCard,
                                   List<SportType> qualifiedSports,
                                   String qualificationLevel, BigDecimal hourlyRate) {
        Referee referee = new Referee(name, phone, qualificationLevel);
        referee.setIdCard(idCard);
        referee.setQualifiedSports(qualifiedSports);
        referee.setHourlyRate(hourlyRate);
        referee.setActive(true);

        for (DayOfWeek day : DayOfWeek.values()) {
            List<Referee.TimeSlot> slots = new java.util.ArrayList<>();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                slots.add(new Referee.TimeSlot(LocalTime.of(8, 0), LocalTime.of(12, 0)));
                slots.add(new Referee.TimeSlot(LocalTime.of(13, 0), LocalTime.of(18, 0)));
                slots.add(new Referee.TimeSlot(LocalTime.of(19, 0), LocalTime.of(22, 0)));
            } else {
                slots.add(new Referee.TimeSlot(LocalTime.of(18, 0), LocalTime.of(22, 0)));
            }
            referee.getAvailableHours().put(day, slots);
        }

        return refereeRepository.save(referee);
    }

    private void initializeWeather() {
        WeatherRecord todayWeather = new WeatherRecord();
        todayWeather.setRecordDate(LocalDate.now());
        todayWeather.setCondition(WeatherCondition.SUNNY);
        todayWeather.setDescription("晴天，适宜运动");
        todayWeather.setTemperature(22.5);
        todayWeather.setHumidity(55.0);
        todayWeather.setWindDirection("东北风");
        todayWeather.setWindSpeed(2.5);
        todayWeather.setSource("初始化数据");
        todayWeather.setRecordedAt(java.time.LocalDateTime.now());
        weatherRecordRepository.save(todayWeather);
        
        logger.info("已初始化今日天气: {}", todayWeather.getDescription());
    }
}