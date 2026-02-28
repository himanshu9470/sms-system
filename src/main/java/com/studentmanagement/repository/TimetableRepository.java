package com.studentmanagement.repository;

import com.studentmanagement.model.Timetable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimetableRepository extends CrudRepository<Timetable, Long> {
    List<Timetable> findByDayOfWeekOrderByStartTimeAsc(Timetable.DayOfWeek dayOfWeek);

    List<Timetable> findByFacultyIdOrderByDayOfWeekAscStartTimeAsc(Long facultyId);

    List<Timetable> findByCourseIdOrderByDayOfWeekAscStartTimeAsc(Long courseId);

    List<Timetable> findBySection(String section);

    List<Timetable> findAllByOrderByDayOfWeekAscStartTimeAsc();
}
