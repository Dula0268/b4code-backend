package com.b4code.backend.rest;

import com.b4code.backend.dao.MenuRepository;
import com.b4code.backend.dto.MenuDto;
import com.b4code.backend.dto.MenuRequest;
import com.b4code.backend.models.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MenuController {

    private final MenuRepository menuRepository;

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<MenuDto>> getMenusByProperty(@PathVariable Long propertyId) {
        log.info("Fetching menus for property: {}", propertyId);
        List<Menu> menus = menuRepository.findByPropertyId(propertyId);
        return ResponseEntity.ok(menus.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<MenuDto> createMenu(@RequestBody MenuRequest request) {
        log.info("Creating menu '{}' for property: {}", request.getName(), request.getPropertyId());
        Menu menu = new Menu();
        menu.setPropertyId(request.getPropertyId());
        menu.setName(request.getName());
        menu.setDescription(request.getDescription());
        menu.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        Menu saved = menuRepository.save(menu);
        return ResponseEntity.ok(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuDto> updateMenu(@PathVariable Long id, @RequestBody MenuRequest request) {
        return menuRepository.findById(id)
                .map(menu -> {
                    if (request.getName() != null) menu.setName(request.getName());
                    if (request.getDescription() != null) menu.setDescription(request.getDescription());
                    if (request.getStatus() != null) menu.setStatus(request.getStatus());
                    return ResponseEntity.ok(toDto(menuRepository.save(menu)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        log.info("Deleting menu id: {}", id);
        menuRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private MenuDto toDto(Menu menu) {
        return MenuDto.builder()
                .id(menu.getId())
                .propertyId(menu.getPropertyId())
                .name(menu.getName())
                .description(menu.getDescription())
                .status(menu.getStatus())
                .build();
    }
}
