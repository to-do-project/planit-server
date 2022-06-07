package com.planz.planit.src.service;

import com.planz.planit.config.BaseException;
import com.planz.planit.config.BaseResponseStatus;
import com.planz.planit.src.domain.notice.Notice;
import com.planz.planit.src.domain.notice.NoticeRepository;
import com.planz.planit.src.domain.notice.dto.GetNoticesResDTO;
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

    @Autowired
    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }


    public void createNotice(String title, String content) throws BaseException {

        try {
            Notice notice = Notice.builder()
                    .title(title)
                    .content(content)
                    .build();
            saveNotice(notice);
        } catch (BaseException e) {
            throw e;
        }
    }

    public void saveNotice(Notice noticeEntity) throws BaseException {
        try {
            noticeRepository.save(noticeEntity);
        } catch (Exception e) {
            log.error("saveNotice() : noticeRepository.save(noticeEntity) 실행 중 데이터베이스 에러 발생");
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public GetNoticesResDTO getNotices() throws BaseException {
        try {
            List<Notice> result = findAllNoticesOrderByCreateAt();

            List<GetNoticesResDTO.NoticeDTO> noticeList = result.stream().map(notice ->
                    GetNoticesResDTO.NoticeDTO.builder()
                            .noticeId(notice.getNoticeId())
                            .title(notice.getTitle())
                            .content(notice.getContent())
                            .createAt(notice.getCreateAt())
                            .build()
            ).collect(Collectors.toList());

            log.info("여기까지 문제없나? " + noticeList.get(0).getCreateAt());

            return GetNoticesResDTO.builder()
                    .totalNoticeCnt(noticeList.size())
                    .noticeList(noticeList)
                    .build();
        } catch (BaseException e) {
            throw e;
        }
    }

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
