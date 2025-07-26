package org.balanceus.topping.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.domain.model.SggCode;
import org.balanceus.topping.domain.repository.SggCodeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sggcode")
public class SggCodeController {
    
    private final SggCodeRepository sggCodeRepository;
    
    @GetMapping("/regions")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getRegions() {
        try {
            List<SggCode> allCodes = sggCodeRepository.findAll(PageRequest.of(0, 1));
            
            // Group by region to get unique regions
            List<String> regions = allCodes.stream()
                .map(SggCode::getSggCdNmRegion)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("regions", regions);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "지역 정보를 불러오는데 실패했습니다.");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/cities")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getCitiesByRegion(@RequestParam String region) {
        try {
            List<SggCode> codes = sggCodeRepository.findBySggCdNmRegion(region);
            
            // Convert to map with code and name for dropdown
            List<Map<String, Object>> cities = codes.stream()
                .map(code -> {
                    Map<String, Object> city = new HashMap<>();
                    city.put("code", code.getSggCd5());
                    city.put("name", code.getSggCdNmCity());
                    city.put("fullName", code.getSggCdNm());
                    return city;
                })
                .sorted((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")))
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cities", cities);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "시/군/구 정보를 불러오는데 실패했습니다.");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/all")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAllSggCodes() {
        try {
            List<SggCode> allCodes = sggCodeRepository.findAll(PageRequest.of(0, 1));
            
            // Convert to map for dropdown usage
            List<Map<String, Object>> codes = allCodes.stream()
                .map(code -> {
                    Map<String, Object> codeMap = new HashMap<>();
                    codeMap.put("code", code.getSggCd5());
                    codeMap.put("name", code.getSggCdNm());
                    codeMap.put("region", code.getSggCdNmRegion());
                    codeMap.put("city", code.getSggCdNmCity());
                    return codeMap;
                })
                .sorted((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")))
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("codes", codes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "지역 코드 정보를 불러오는데 실패했습니다.");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}