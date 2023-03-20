package com.group.leavemanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @Autowired
    private LeaveRepository leaveRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HolidayRepository holidayRepository;
    List<Holiday> holidayList;
    @PostMapping()
    public Leave createLeave(@RequestBody Leave leave)
    {
        int numOfLeaves = getActualNumberOfLeaves(leave);
        leave.setNumberOfLeaves(numOfLeaves);
        leave.setLeaveStatus(LeaveStatus.Applied.toString());
        leave.setCreationTime(LocalDate.now());
        leave.setUpdateTime(LocalDate.now());
        return leaveRepository.save(leave);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Leave> getLeaveById(@PathVariable(value = "id") Long leaveId)
    {
        try
        {
            Leave leave = leaveRepository.findById(leaveId).get();
            return ResponseEntity.ok().body(leave);
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/")
    public List<Leave> getAllLeave()
    {
        return leaveRepository.findAll();
    }

    @GetMapping("/leavebystatus")
    public List<Leave> getAllLeaveByStatus(@RequestParam("employeeId") Long employeeId, @RequestParam("leaveStatus") String leaveStatus)
    {
        return leaveRepository.findAllByLeaveStatus(employeeId, leaveStatus.toString());
    }
    @GetMapping("/leavebymanagerid")
    public List<Leave> getAllLeaveByManagerId(@RequestParam("managerId") Long managerId)
    {
        return leaveRepository.findAllByManagerId(managerId);
    }

    @GetMapping("/leavebystatusformanager")
    public List<Leave> getAllLeaveByStatusForManager(@RequestParam("managerId") Long managerId, @RequestParam("leaveStatus") LeaveStatus leaveStatus)
    {
        return leaveRepository.findAllByLeaveForManagerByStatus(managerId, leaveStatus.toString());
    }

    @GetMapping("/leavecountbystatusformanager")
    public Long getLeaveCountByStatusForManager(@RequestParam("managerId") Long managerId, @RequestParam("leaveStatus") LeaveStatus leaveStatus)
    {
        return leaveRepository.getLeaveCountByStatusForManager(managerId, leaveStatus.toString());
    }
    @GetMapping("/leavebyemployeeid")
    public List<Leave> getAllLeaveByEmployeeId(@RequestParam("employeeId") Long employeeId)
    {
        return leaveRepository.findAllByEmployeeId(employeeId, LocalDate.now().minusMonths(3));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<Leave> updateLeave(@PathVariable(value = "id") Long leaveId, @RequestBody Leave leavePatch)
    {
        try
        {
            Leave leave = leaveRepository.findById(leaveId).get();
            leave.setLeaveStatus(leavePatch.getLeaveStatus());
            leave.setRemark(leavePatch.getRemark());
            leave.setUpdateTime(LocalDate.now());
            leaveRepository.save(leave);
            return ResponseEntity.ok().body(leave);
        }
        catch (Exception ex)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Leave> deleteLeave(@PathVariable(value = "id") Long leaveId)
    {
        try
        {
            Leave leave = leaveRepository.findById(leaveId).get();
            leaveRepository.delete(leave);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    private int getActualNumberOfLeaves(Leave leave)
    {
        holidayList = holidayRepository.findAll();

        int holidayCount = 0;
        for(Holiday holiday : holidayList)
        {
            if( holiday.getDate().compareTo(leave.getStartDate()) >= 0 &&
                holiday.getDate().compareTo(leave.getEndDate()) <= 0)
            {
                holidayCount++;
            }
        }

        return (int)ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate())
                + 1 - holidayCount;
    }
}
