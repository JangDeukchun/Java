package io.dustin.apps.board.domain.community.comment.service;

import io.dustin.apps.board.domain.community.comment.model.dto.CommentDto;
import io.dustin.apps.board.domain.community.comment.repository.CommentRepository;
import io.dustin.apps.board.domain.community.comment.model.Comment;
import io.dustin.apps.board.domain.community.posting.model.Posting;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.dustin.apps.common.utils.OptionalUtils.getEntity;

/**
 * cqrs -> srp
 */
@Service
@RequiredArgsConstructor
public class ReadCommentService {

    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByPosting(long loginId, long postingId, int size, Long nextId) {
        return commentRepository.commentsByPosting(loginId, postingId, size, nextId);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getReplyByComment(long loginId, long commentId, int size, Long nextId) {
        return commentRepository.replyByComment(loginId, commentId, size, nextId);
    }

    @Transactional(readOnly = true)
    public Page<Comment> getList(Pageable pageable) {
        /**
         * 대댓글 같이 가져오기
         * 좋아요 표시할 수 있는 데이터 넣기
         */
//        List<Sort.Order> sorts = new ArrayList<>();
//        sorts.add(Sort.Order.desc("createDate"));
//        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return commentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Comment findById(Long id) {
        /**
         * 포스팅과 같은 이유
         */
        return getEntity(this.commentRepository.findById(id), Comment.class, "comment not fount");
    }

}
