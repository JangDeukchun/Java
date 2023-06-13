package io.dustin.apps.board.api.usecase.notice;

import io.dustin.apps.board.domain.notice.model.Notice;
import io.dustin.apps.board.domain.notice.model.dto.NoticeDto;
import io.dustin.apps.board.domain.notice.service.ReadNoticeService;
import io.dustin.apps.board.domain.notice.service.WriteNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ModifyNoticeUseCase {

    private final ReadNoticeService readNoticeService;
    private final WriteNoticeService writeNoticeService;

    public NoticeDto execute(Long userId, Long id, String subject, String content) {
        Notice notice = readNoticeService.getNotice(id);
        if (!notice.getAdmin().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        writeNoticeService.updateContent(notice, subject, content);
        return NoticeDto.from(notice);
    }


}
