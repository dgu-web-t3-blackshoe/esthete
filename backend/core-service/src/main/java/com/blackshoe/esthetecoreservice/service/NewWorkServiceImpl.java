package com.blackshoe.esthetecoreservice.service;

import com.blackshoe.esthetecoreservice.dto.NewWorkDto;
import com.blackshoe.esthetecoreservice.entity.Exhibition;
import com.blackshoe.esthetecoreservice.entity.Photo;
import com.blackshoe.esthetecoreservice.entity.Support;
import com.blackshoe.esthetecoreservice.entity.User;
import com.blackshoe.esthetecoreservice.exception.*;
import com.blackshoe.esthetecoreservice.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class NewWorkServiceImpl implements NewWorkService{
    private final NewWorkRepository newWorkRepository;
    private final RedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final SupportRepository supportRepository;
    private final PhotoRepository photoRepository;
    private final ExhibitionRepository exhibitionRepository;

    @Override
    public List<NewWorkDto.ReadNewWorkResponse> readNewWork(UUID userId) {

        List<NewWorkDto.ReadNewWorkResponse> newWorkReadResponseNewWorks = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱을 위한 ObjectMapper

        //get all supporting photographers from userId and get keys from those
        List<Support> supports = supportRepository.findBySupporterId(userId);
        Set<String> allKeys = new HashSet<>();

        for (Support support : supports) {
            String pattern = "photographer_" + support.getPhotographer().getUserId().toString() + "_exhibition_*";
            allKeys.addAll(redisTemplate.keys(pattern));
        }

        for (String key : allKeys) {
            try {
                String jsonValue = (String) redisTemplate.opsForValue().get(key);
                // jsonValue가 null이 아닌 경우에만 처리
                if (jsonValue != null) {
                    List<List<String>> values = objectMapper.readValue(jsonValue, new TypeReference<List<List<String>>>() {
                    });

                    // 특정 userId가 JSON 배열 안에 존재하는지 확인
                    boolean userIdExists = values.stream().anyMatch(pair -> userId.toString().equals(pair.get(0)));

                    if (userIdExists) {
                        String hasNew = values.stream()
                                .filter(pair -> userId.toString().equals(pair.get(0)))
                                .findFirst()
                                .map(pair -> pair.get(1))
                                .orElse("false");

                        String[] splitKey = key.split("_");

                        String photographerId = splitKey[1];
                        String exhibitionId = splitKey[3];

                        NewWorkDto.ReadNewWorkResponse newWorkReadNewWorkResponse;

                        newWorkReadNewWorkResponse = NewWorkDto.ReadNewWorkResponse.builder()
                                .exhibitionId(exhibitionId)
                                .hasNewExhibition(hasNew)
                                .build();

                        newWorkRepository.findByPhotographerIdAndExhibitionId(UUID.fromString(photographerId), UUID.fromString(exhibitionId)).ifPresent(newWork -> {
                            newWorkReadNewWorkResponse.setUpdatedAt(newWork.getUpdatedAt().toString());
                            newWorkReadNewWorkResponse.setPhotographerId(newWork.getPhotographerId().toString());
                        });

                        User photographer = userRepository.findByUserId(UUID.fromString(photographerId)).orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_FOUND));

                        Exhibition exhibition = exhibitionRepository.findByExhibitionId(UUID.fromString(exhibitionId)).orElseThrow(() -> new ExhibitionException(ExhibitionErrorResult.EXHIBITION_NOT_FOUND));

                        Photo photo = photoRepository.findByPhotoId(UUID.fromString(exhibition.getThumbnail())).orElseThrow(() -> new PhotoException(PhotoErrorResult.PHOTO_NOT_FOUND));

                        newWorkReadNewWorkResponse.setPhotographerId(photographerId);
                        newWorkReadNewWorkResponse.setProfileImg(photographer.getProfileImgUrl().getCloudfrontUrl());
                        newWorkReadNewWorkResponse.setNickname(photographer.getNickname());
                        newWorkReadNewWorkResponse.setThumbnail(photo.getPhotoUrl().getCloudfrontUrl());
                        newWorkReadNewWorkResponse.setExhibitionTitle(exhibition.getTitle());
                        newWorkReadResponseNewWorks.add(newWorkReadNewWorkResponse);
                    }
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.info("JSON 파싱에 실패했습니다.");
            }
        }

        return newWorkReadResponseNewWorks;
    }


    @Override
    public NewWorkDto.UpdateNewWorkResponse viewNewExhibition(NewWorkDto.UpdateViewOfExhibitionRequest updateRequest) throws JsonProcessingException {
        String userId = updateRequest.getUserId();
        String exhibitionId = updateRequest.getExhibitionId();

        // ObjectMapper 인스턴스 생성
        ObjectMapper objectMapper = new ObjectMapper();

        Exhibition exhibition = exhibitionRepository.findByExhibitionId(UUID.fromString(exhibitionId)).orElseThrow(() -> new ExhibitionException(ExhibitionErrorResult.EXHIBITION_NOT_FOUND));

        // Redis에서 exhibitionId에 해당하는 데이터 검색
        String redisKey = "photographer_"+ exhibition.getUser().getUserId().toString() +"_exhibition_" + exhibitionId;

        String jsonValue = (String) redisTemplate.opsForValue().get(redisKey);

        List<List<String>> supporters = objectMapper.readValue(jsonValue, new TypeReference<List<List<String>>>() {});

        // userId에 해당하는 값을 "false"로 변경
        supporters.forEach(supporter -> {
            if (supporter.get(0).equals(userId)) {
                supporter.set(1, "false");
            }
        });

        // 변경된 데이터를 다시 JSON 문자열로 변환
        String updatedJsonValue = objectMapper.writeValueAsString(supporters);

        // Redis에 업데이트
        redisTemplate.opsForValue().set(redisKey, updatedJsonValue);

        // 응답 생성
        NewWorkDto.UpdateNewWorkResponse newWorkUpdateNewWorkResponse = NewWorkDto.UpdateNewWorkResponse.builder()
                .updatedAt(LocalDateTime.now().toString())
                .build();
        return newWorkUpdateNewWorkResponse;
    }
}
