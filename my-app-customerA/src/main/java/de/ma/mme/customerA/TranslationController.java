package de.ma.mme.customerA;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/translations")
public class TranslationController {

    @GetMapping
    public Map<String, String> getAllTranslations() {
        return Arrays.stream(Translations.values())
                .collect(Collectors.toMap(
                        Translations::name,
                        Translations::getTranslation
                ));
    }

    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> getTranslation(@PathVariable String key) {
        try {
            Translations translation = Translations.valueOf(key.toUpperCase());
            Map<String, String> result = Map.of(translation.name(), translation.getTranslation());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/grouped")
    public Map<String, Map<String, String>> getGroupedTranslations() {
        Map<String, Map<String, String>> result = new HashMap<>();

        // Group translations by categories (this is just an example grouping)
        Map<String, String> general = new HashMap<>();
        Map<String, String> ui = new HashMap<>();
        Map<String, String> actions = new HashMap<>();

        for (Translations translation : Translations.values()) {
            String key = translation.name();
            String value = translation.getTranslation();

            if (key.startsWith("WELCOME") || key.startsWith("HELLO") || key.startsWith("GOODBYE")) {
                general.put(key, value);
            } else if (key.equals("YES") || key.equals("NO") || key.equals("CANCEL") || key.equals("SUBMIT")) {
                actions.put(key, value);
            } else {
                ui.put(key, value);
            }
        }

        result.put("general", general);
        result.put("ui", ui);
        result.put("actions", actions);

        return result;
    }
}
