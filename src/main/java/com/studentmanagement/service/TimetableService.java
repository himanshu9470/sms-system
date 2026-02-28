package com.studentmanagement.service;

import com.studentmanagement.model.Timetable;
import com.studentmanagement.repository.TimetableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TimetableService {

    @Autowired
    private TimetableRepository timetableRepository;

    public List<Timetable> getAll() {
        return (List<Timetable>) timetableRepository.findAllByOrderByDayOfWeekAscStartTimeAsc();
    }

    public Optional<Timetable> getById(Long id) {
        return timetableRepository.findById(id);
    }

    public List<Timetable> getByDay(Timetable.DayOfWeek day) {
        return timetableRepository.findByDayOfWeekOrderByStartTimeAsc(day);
    }

    public List<Timetable> getByFaculty(Long facultyId) {
        return timetableRepository.findByFacultyIdOrderByDayOfWeekAscStartTimeAsc(facultyId);
    }

    public List<Timetable> getByCourse(Long courseId) {
        return timetableRepository.findByCourseIdOrderByDayOfWeekAscStartTimeAsc(courseId);
    }

    public Map<Timetable.DayOfWeek, List<Timetable>> getGroupedByDay() {
        List<Timetable> all = getAll();
        return all.stream().collect(
                Collectors.groupingBy(Timetable::getDayOfWeek, () -> new LinkedHashMap<>(), Collectors.toList()));
    }

    public Map<Timetable.DayOfWeek, List<Timetable>> getGroupedByDayForFaculty(Long facultyId) {
        List<Timetable> list = getByFaculty(facultyId);
        return list.stream().collect(
                Collectors.groupingBy(Timetable::getDayOfWeek, () -> new LinkedHashMap<>(), Collectors.toList()));
    }

    public Timetable save(Timetable timetable) {
        return timetableRepository.save(timetable);
    }

    public void delete(Long id) {
        timetableRepository.deleteById(id);
    }
}
