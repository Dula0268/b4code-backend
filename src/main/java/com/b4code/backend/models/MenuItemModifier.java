package com.b4code.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_item_modifiers", schema = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private MenuItem menuItem;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "menu_item_modifier_options", schema = "staff", joinColumns = @JoinColumn(name = "modifier_id"))
    private List<MenuItemModifierOption> options = new ArrayList<>();
}
