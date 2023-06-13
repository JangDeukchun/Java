package io.dustin.apps.board.api.usecase.community.posting;

import io.dustin.apps.board.domain.community.comment.model.Comment;
import io.dustin.apps.board.domain.community.comment.model.dto.CommentDto;
import io.dustin.apps.board.domain.community.posting.model.Posting;
import io.dustin.apps.board.domain.community.posting.model.dto.PostingDto;
import io.dustin.apps.board.domain.community.posting.service.ReadPostingService;
import io.dustin.apps.board.domain.community.posting.service.WritePostingSerivice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WritePostingUseCase {

    private final ReadPostingService readPostingService;
    private final WritePostingSerivice writePostingSerivice;

    public PostingDto execute(Long userId, String subject, String content) {
        Posting posting = writePostingSerivice.create(subject, content, userId);
        PostingDto dto = PostingDto.from(posting);
        return PostingDto.from(posting);
    }


}
