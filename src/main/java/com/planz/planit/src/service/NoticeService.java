package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.config.fcm.FirebaseCloudMessageService;
import com.planz.planit.src.domain.notice.Notice;
import com.planz.planit.src.domain.notice.NoticeRepository;
import com.planz.planit.src.domain.notice.dto.GetNoticesResDTO;
import com.planz.planit.src.domain.notification.NotificationSmallCategory;
import com.planz.planit.src.domain.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.planz.planit.config.BaseResponseStatus.DATABASE_ERROR;

@Service
@Log4j2
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final FirebaseCloudMessageService firebaseCloudMessageService;
    private final DeviceTokenService deviceTokenService;

    @Autowired
    public NoticeService(NoticeRepository noticeRepository, NotificationService notificationService, UserService userService, FirebaseCloudMessageService firebaseCloudMessageService, DeviceTokenService deviceTokenService) {
        this.noticeRepository = noticeRepository;
        this.notificationService = notificationService;
        this.userService = userService;
        this.firebaseCloudMessageService = firebaseCloudMessageService;
        this.deviceTokenService = deviceTokenService;
    }

    /**
     * 공지사항 생성 API (ROLE_ADMIN 권한을 가진 사용자만 이용 가능)
     * 1. Notice 엔티티 생성
     * 2. Notice 엔티티 저장
     * 3. 공지사항 알림 생성
     * 4. FCM 보내기
     */
    public void createNotice(String title, String content) throws BaseException {

        try {
            // 1. Notice 엔티티 생성
            Notice notice = Notice.builder()
                    .title(title)
                    .content(content)
                    .build();

            // 2. Notice 엔티티 저장
            saveNotice(notice);

            // 3. 공지사항 알림 생성
            List<User> allUser = userService.findAllUser();
            for (User user : allUser) {
                notificationService.createNotification(user, NotificationSmallCategory.NOTICE_TWO, "[공지] " + notice.getTitle(), null, null, notice);
            }

            // 4. FCM 보내기
            try {
                List<String> deviceTokens = deviceTokenService.findAllDeviceTokens_noticeFlag1();
                firebaseCloudMessageService.sendMessageTo(deviceTokens, "[공지사항 업데이트]", "공지사항이 업데이트 되었습니다.");
                log.info("[FCM 전송 성공] notice_flag가 1인 모든 사용자에게, 공지사항 FCM 전송 성공");
            }
            catch (BaseException e){
                log.error("[FCM 전송 실패] " + e.getStatus());
            }

        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * Notice 저장 혹은 업데이트
     */
    public void saveNotice(Notice noticeEntity) throws BaseException {
        try {
            noticeRepository.save(noticeEntity);
        } catch (Exception e) {
            log.error("saveNotice() : noticeRepository.save(noticeEntity) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 공지사항 조회 API
     * 1. 모든 공지사항 리스트를 생성일의 내림차순으로 정렬하여 조회하기
     * 2. 결과 반환
     */
    public GetNoticesResDTO getNotices() throws BaseException {
        try {
            // 1. 모든 공지사항 리스트를 생성일의 내림차순으로 정렬하여 조회하기
            List<Notice> result = findAllNoticesOrderByCreateAt();

            // 2. 결과 반환
            List<GetNoticesResDTO.NoticeDTO> noticeList = result.stream().map(notice ->
                    GetNoticesResDTO.NoticeDTO.builder()
                            .noticeId(notice.getNoticeId())
                            .title(notice.getTitle())
                            .content(notice.getContent())
                            .createAt(notice.getCreateAt())
                            .build()
            ).collect(Collectors.toList());

            return GetNoticesResDTO.builder()
                    .totalNoticeCnt(noticeList.size())
                    .noticeList(noticeList)
                    .build();
        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * DB에서 생성일의 내림차순으로 정렬하여, 모든 공지사항 리스트 조회해오기
     */
    public List<Notice> findAllNoticesOrderByCreateAt() throws BaseException {
        try {
            // 자바 클래스의 필드명 입력
            return noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createAt"));
        } catch (Exception e) {
            log.error("findAllNoticesOrderByCreateAt() : noticeRepository.findAll(Sort sort) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
