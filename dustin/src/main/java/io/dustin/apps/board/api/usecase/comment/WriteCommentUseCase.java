package io.dustin.apps.board.api.usecase.comment;

import io.dustin.apps.board.domain.qna.answer.service.ReadAnswerService;
import io.dustin.apps.board.domain.qna.comment.service.WriteCommentService;
import io.dustin.apps.board.domain.qna.model.Comment;
import io.dustin.apps.board.domain.qna.model.Question;
import io.dustin.apps.board.domain.qna.model.SiteUser;
import io.dustin.apps.board.domain.qna.model.dto.CommentDto;
import io.dustin.apps.board.domain.qna.question.service.ReadQuestionService;
import io.dustin.apps.board.domain.qna.user.service.ReadUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class WriteCommentUseCase {

    private final ReadQuestionService readQuestionService;
    private final ReadUserService readUserService;
    private final WriteCommentService writeCommentService;
    private final ReadAnswerService readAnswerService;

    public CommentDto execute(Principal principal, Long questionId, String content) {
        Question question = readQuestionService.getQuestion(questionId);
        SiteUser siteUser = readUserService.getUser(principal.getName());
        Comment comment = writeCommentService.create(question, content, siteUser);
        CommentDto dto = CommentDto.from(comment);
        return CommentDto.from(comment);
    }
    }


