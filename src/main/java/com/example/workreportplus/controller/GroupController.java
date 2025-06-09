/**
 * @author Alex Sereda
 * @date 09.06.2025 10:45
 */
package com.example.workreportplus.controller;

import com.example.workreportplus.service.RegionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import com.example.workreportplus.dto.GroupDto;
import com.example.workreportplus.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupController {

    private final GroupService groupService;
    private final RegionService regionService;

    public GroupController(GroupService groupService, RegionService regionService) {
        this.groupService = groupService;
        this.regionService = regionService;
    }

    // ✅ GET ?all=true|false
    @GetMapping

    public ResponseEntity<List<GroupDto>> getAllGroups(@RequestParam(defaultValue = "false") boolean all) {
        Map<UUID, String> map = regionService.getAllRegionIdNameMap();
        List<GroupDto> groups = all ? groupService.getAllGroupsAnyStatus() : groupService.getAllGroups();
        groups.forEach(group -> group.setRegionName(map.get(group.getRegionId())));
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable UUID id) {
        GroupDto group = groupService.getGroupById(id);
        return group != null ? ResponseEntity.ok(group) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Void> addGroup(@RequestBody GroupDto dto) {
        if (groupService.groupExistsByName(dto.getName())) {
            return ResponseEntity.badRequest().build();
        }
        groupService.insertGroup(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateGroup(@PathVariable UUID id, @RequestBody GroupDto dto) {
        groupService.updateGroup(id, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateGroupStatus(@PathVariable UUID id, @RequestParam boolean active) {
        groupService.updateGroupStatus(id, active);
        return ResponseEntity.ok().build();
    }

}
