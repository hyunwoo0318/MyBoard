package Lim.boardApp.domain.text.service;

import Lim.boardApp.domain.text.entity.Text;
import Lim.boardApp.domain.text.repository.text.TextRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ViewCountService {

    private final RedisTemplate redisTemplate;
    private final TextRepository textRepository;
    /**
     * 조회수를 증가시키는 메서드
     *
     * @param text 조회하는 글
     * @param customerId 조회하는 회원의 ID
     * @return 조회수
     *     <p>조회수 증가 조건 1. 작성자가 아닌 다른 사람이 조회할때만 증가함 2. 해당 글을 조회한 사람은 6시간 이내에는 해당 글을 조회해도 조회수가
     *     증가하지않음.
     *     <p>구현 : redis를 이용한 캐싱을 통해 조회수를 캐시에 저장해 두었다가 3분에 한번씩 DB에 업데이트
     *     <p>조회수 관련 redis key naming - 캐싱한 조회수(즉 DB update시 올려야하는 조회수): viewCnt::{textId} - 해당 글을
     *     조회했는지 체크 : viewCheck::{textId}::{customerId}
     *     <p>해당 키의 유효기간을 6시간으로 설정하고 해당 키로 조회했을때 redis상에 존재하면 조회수를 올리지않음. 해당 키로 조회했을때 redis상에 존재하지
     *     않을경우, 해당키를 redis에 저장하고 조회수를 1 올림.
     */
    public Long increaseViewCnt(Text text, Long customerId) {
        String keyForCnt = "viewCnt::" + text.getId();
        String keyForCheck = "viewCheck::" + text.getId() + "::" + customerId;

        ValueOperations ops = redisTemplate.opsForValue();
        if (ops.get(keyForCheck) == null) {
            // 해당 키가 존재하지 않을 경우 -> 조회수를 1상승
            ops.set(keyForCheck, "T", Duration.ofHours(6));
            if (ops.get(keyForCnt) == null) {
                ops.set(keyForCnt, "1");
            } else {
                ops.increment(keyForCnt);
            }
        }

        return text.getViewCount();
    }

    /** 3분에 한번씩 캐시에 있는 조회수를 바탕으로 조회수 상승 */
    @Scheduled(cron = "0 0/3 * * * ?")
    @Transactional
    public void updateViewCount() {
        Set<String> keySet = redisTemplate.keys("viewCnt*");

        Optional.ofNullable(keySet).ifPresent(keys -> {
            keys.forEach(viewCntKey -> {
                Long textId = Long.parseLong(viewCntKey.split("::")[1]);
                Long viewCnt = Long.parseLong((String) redisTemplate.opsForValue().get(viewCntKey));

                Long updatedViewCnt = textRepository.updateViewCount(textId, viewCnt);

                redisTemplate.delete(viewCntKey);
            });
        });

    }
}
