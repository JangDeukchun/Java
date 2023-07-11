package io.dustin.apps.board.domain.community.posting.repository.custom.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dustin.apps.board.domain.blockeduser.model.BlockedUser;
import io.dustin.apps.board.domain.blockeduser.repository.BlockedUserRepository;
import io.dustin.apps.board.domain.blockeduser.service.ReadBlockedUserService;
import io.dustin.apps.board.domain.community.posting.model.Posting;
import io.dustin.apps.board.domain.community.posting.model.dto.PostingDto;
import io.dustin.apps.board.domain.community.posting.repository.custom.CustomPostingRepository;
import io.dustin.apps.board.domain.community.posting.service.ReadPostingService;
import io.dustin.apps.board.domain.follow.model.Follow;
import io.dustin.apps.board.domain.follow.service.ReadFollowService;
import io.dustin.apps.common.code.BoardType;
import io.dustin.apps.common.code.YesOrNo;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

import static com.querydsl.core.types.Projections.constructor;
import static io.dustin.apps.board.domain.bookmark.model.QBookmark.bookmark;
import static io.dustin.apps.board.domain.community.posting.model.QPosting.posting;
import static io.dustin.apps.board.domain.like.model.QLike.like;

@RequiredArgsConstructor
public class CustomPostingRepositoryImpl implements CustomPostingRepository {

    private final JPAQueryFactory query;
    private final ReadBlockedUserService readBlockedUserService;
    private final ReadFollowService readFollowService;

    @Override
    public PostingDto getPosting(long userId, long postingId) {
        /***
         게시물에 댓글을 허용하는 경우의 수는 세가지
         1. 게시물 작성자의 팔로잉 리스트에 포함
         2. 게시물 작성자의 팔로워 리스트에 포함
         3. 게시물 작성자의 팔로잉 리스트 + 팔로워 리스트
         각각을의 값을 true, false 주고 설정에 따라 true 표시된 유저들만 댓글 작성할 수 있음
         */
        Posting selectedPosting = query.selectFrom(posting).where(posting.id.eq(postingId)).fetchOne();

        Long postingAuthorId = selectedPosting.getUserId();

        /** 게시물 작성자의 팔로잉 리스트를 가져온다 */
        List<Follow> followingIdList = readFollowService.getFollowingIdList(postingAuthorId);
        List<Long> followingIds = followingIdList.stream().map(Follow::getFollowingId).collect(Collectors.toList());
        Boolean isFollowing = followingIds.contains(userId);

        /** 게시물 작성자의 팔로워 리스트를 가져온다 */
        List<Follow> followerIdList = readFollowService.getFollowerIdList(postingAuthorId);
        List<Long> followerIds = followerIdList.stream().map(Follow::getFollowerId).collect(Collectors.toList());
        Boolean isFollower = followerIds.contains(userId);

        /** 게시물 작성자의 팔로잉 리스트 + 팔로워 리스트를 가져온다 */
        Boolean isFollowingAndFollower = isFollowing || isFollower ? true : false;

        JPAQuery<PostingDto> jPAQuery = query.select(constructor(PostingDto.class,
                        posting.id,
                        posting.userId,
                        posting.subject,
                        posting.content,
                        new CaseBuilder().when(like.id.isNotNull()).then(true).otherwise(false).as("isLike"),
                        new CaseBuilder().when(bookmark.id.isNotNull()).then(true).otherwise(false).as("isBookmark"),
                        Expressions.constant(isFollowing),
                        Expressions.constant(isFollower),
                        Expressions.constant(isFollowingAndFollower),
                        posting.commentCount.as("commentCnt"),
                        posting.clickCount.as("clickCnt"),
                        posting.likeCount,
                        posting.createdAt
                ))
                .from(posting)
                .leftJoin(like).on(
                        like.boardType.eq(BoardType.POSTING)
                                .and(like.boardId.eq(posting.id))
                                .and(like.userId.eq(userId))

                )
                .leftJoin(bookmark).on(
                        bookmark.boardId.eq(posting.id)
                                .and(bookmark.userId.eq(userId))
                )
                .where(
                        posting.id.eq(postingId)
                );

        return jPAQuery.fetchOne();
    }



    @Override
    public List<PostingDto> getPostingList(long userId, Long nextId, int size) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if(nextId != null) {
            booleanBuilder.and(posting.id.lt(nextId));
        }

        /** userID가 차단한 리스트를 가져온다*/
        List<BlockedUser> toUserIdList = readBlockedUserService.getToUserIdList(userId);
        List<Long> toUserIds = toUserIdList.stream().map(BlockedUser::getToUserId).collect(Collectors.toList());

        /** userID를 차단한 리스트를 가져온다*/
        List<BlockedUser> fromUserIdList = readBlockedUserService.getFromUserIdList(userId);
        List<Long> fromUserIds = fromUserIdList.stream().map(BlockedUser::getFromUserId).collect(Collectors.toList());

        booleanBuilder.and(posting.userId.notIn(toUserIds).and(posting.userId.notIn(fromUserIds)));


        JPAQuery<PostingDto> jPAQuery = query.select(constructor(PostingDto.class,
                        posting.id,
                        posting.userId,
                        posting.subject,
                        posting.content,
                        new CaseBuilder().when(like.id.isNotNull()).then(true).otherwise(false).as("isLike"),
                        new CaseBuilder().when(bookmark.id.isNotNull()).then(true).otherwise(false).as("isBookmark"),
                        posting.commentCount.as("commentCnt"),
                        posting.clickCount.as("clickCnt"),
                        posting.likeCount,
                        posting.createdAt
                ))
                .from(posting)
                .leftJoin(like).on(
                        like.boardType.eq(BoardType.POSTING)
                        .and(like.boardId.eq(posting.id))
                        .and(like.userId.eq(userId))

                )
                .leftJoin(bookmark).on(
                        bookmark.boardId.eq(posting.id)
                        .and(bookmark.userId.eq(userId))
                )
                .where(
                    booleanBuilder,
                    posting.isDeleted.ne(YesOrNo.Y),
                    /** 내 게시물은 가져오지 않는다 */
                    posting.userId.ne(userId)

                )
                .orderBy(posting.id.desc())
                .limit(size);

        return jPAQuery.fetch();
    }

    @Override
    public PostingDto getMyPosting(long userId, long postingId) {
        /***
         게시물에 댓글을 허용하는 경우의 수는 세가지
         1. 게시물 작성자의 팔로잉 리스트에 포함
         2. 게시물 작성자의 팔로워 리스트에 포함
         3. 게시물 작성자의 팔로잉 리스트 + 팔로워 리스트
         4. 각각을의 값을 true, false 주고 설정에 따라 true 표시된 유저들만 댓글 작성할 수 있음
         5. 내 게시물은 전부 true
         */

        JPAQuery<PostingDto> jPAQuery = query.select(constructor(PostingDto.class,
                        posting.id,
                        posting.userId,
                        posting.subject,
                        posting.content,
                        new CaseBuilder().when(like.id.isNotNull()).then(true).otherwise(false).as("isLike"),
                        new CaseBuilder().when(bookmark.id.isNotNull()).then(true).otherwise(false).as("isBookmark"),
                        new CaseBuilder().when(posting.userId.eq(userId)).then(true).otherwise(false).as("following"),
                        new CaseBuilder().when(posting.userId.eq(userId)).then(true).otherwise(false).as("follower"),
                        new CaseBuilder().when(posting.userId.eq(userId)).then(true).otherwise(false).as("followingAndFollower"),
                        posting.commentCount.as("commentCnt"),
                        posting.clickCount.as("clickCnt"),
                        posting.likeCount,
                        posting.createdAt
                ))
                .from(posting)
                .leftJoin(like).on(
                        like.boardType.eq(BoardType.POSTING)
                                .and(like.boardId.eq(posting.id))
                                .and(like.userId.eq(userId))

                )
                .leftJoin(bookmark).on(
                        bookmark.boardId.eq(posting.id)
                                .and(bookmark.userId.eq(userId))
                )
                .where(
                        posting.id.eq(postingId)
                );

        return jPAQuery.fetchOne();
    }

    @Override
    public List<PostingDto> getMyPostingList(long userId, Long nextId, int size) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if(nextId != null) {
            booleanBuilder.and(posting.id.lt(nextId));
        }

        JPAQuery<PostingDto> jPAQuery = query.select(constructor(PostingDto.class,
                        posting.id,
                        posting.userId,
                        posting.subject,
                        posting.content,
                        new CaseBuilder().when(like.id.isNotNull()).then(true).otherwise(false).as("isLike"),
                        new CaseBuilder().when(bookmark.id.isNotNull()).then(true).otherwise(false).as("isBookmark"),
                        new CaseBuilder().when(posting.userId.eq(userId)).then(true).otherwise(false).as("following"),
                        new CaseBuilder().when(posting.userId.eq(userId)).then(true).otherwise(false).as("follower"),
                        new CaseBuilder().when(posting.userId.eq(userId)).then(true).otherwise(false).as("followingAndFollower"),
                        posting.commentCount.as("commentCnt"),
                        posting.clickCount.as("clickCnt"),
                        posting.likeCount,
                        posting.createdAt
                ))
                .from(posting)
                .leftJoin(like).on(
                        like.boardType.eq(BoardType.POSTING)
                                .and(like.boardId.eq(posting.id))
                                .and(like.userId.eq(userId))

                )
                .leftJoin(bookmark).on(
                        bookmark.boardId.eq(posting.id)
                                .and(bookmark.userId.eq(userId))
                )
                .where(
                        booleanBuilder,
                        posting.isDeleted.ne(YesOrNo.Y),
                        posting.userId.eq(userId)

                )
                .orderBy(posting.id.desc())
                .limit(size);

        return jPAQuery.fetch();
    }
}
