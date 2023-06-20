package io.dustin.apps.board.domain.community.comment.repository;

import io.dustin.apps.board.domain.community.comment.model.Comment;
import io.dustin.apps.board.domain.community.posting.model.Posting;
import io.dustin.apps.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepository extends BaseRepository<Comment, Long> {

    Page<Comment> findAll(Pageable pageable);


}
