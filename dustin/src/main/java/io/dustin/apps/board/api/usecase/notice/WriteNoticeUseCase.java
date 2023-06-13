package io.dustin.apps.board.api.usecase.notice;

import io.dustin.apps.board.domain.notice.model.Notice;
import io.dustin.apps.board.domain.notice.model.dto.NoticeDto;
import io.dustin.apps.board.domain.notice.service.ReadNoticeService;
import io.dustin.apps.board.domain.notice.service.WriteNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WriteNoticeUseCase {

    private final ReadNoticeService readNoticeService;
    private final WriteNoticeService writeNoticeService;

    public NoticeDto execute(Long userId, String subject, String content) {
        Notice notice = writeNoticeService.create(subject, content, userId);
        NoticeDto dto = NoticeDto.from(notice);
        return NoticeDto.from(notice);
    }
}
