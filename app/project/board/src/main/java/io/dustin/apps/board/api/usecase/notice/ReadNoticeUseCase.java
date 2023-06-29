package io.dustin.apps.board.api.usecase.notice;

import io.dustin.board.common.model.CountByPagingInfo;
import io.dustin.board.common.model.QueryPage;
import io.dustin.board.common.model.ResponseWithScroll;
import io.dustin.board.domain.notice.model.dto.NoticeDto;
import io.dustin.board.domain.notice.service.ReadNoticeService;
import io.dustin.board.domain.notice.service.WriteNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.dustin.board.common.model.ResponseWithScrollSetting.getCountByPagingInfo;

@Service
@RequiredArgsConstructor
public class ReadNoticeUseCase {

    private final ReadNoticeService readNoticeService;
    private final WriteNoticeService writeNoticeService;

    public ResponseWithScroll<List<NoticeDto>> execute(QueryPage queryPage) {
        /**
         * 게시물 리스트 보여줌
         */
        long userId = 1;
        int realSize = queryPage.getSize();
        int querySize = realSize + 1;

        List<NoticeDto> result = readNoticeService.getNoticeList(userId, queryPage.getNextId(), querySize);
        CountByPagingInfo<NoticeDto> cbi = getCountByPagingInfo(result, realSize);

        return ResponseWithScroll.from(cbi.result(), cbi.isLast(), cbi.nextId());
    }

    @Transactional
    public NoticeDto noticeDetail(Long noticeId) {
        long userId = 1;
        NoticeDto notice = readNoticeService.getNotice(userId, noticeId);
        writeNoticeService.click(noticeId);
        return notice;


    }



}
