package io.dustin.apps.board.domain.blockeduser.service;


import io.dustin.apps.board.domain.blockeduser.model.BlockedUser;
import io.dustin.apps.board.domain.blockeduser.repository.BlockedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.dustin.apps.common.utils.OptionalUtils.getEntity;

@RequiredArgsConstructor
@Service
public class ReadBlockedUserService {

    private final BlockedUserRepository blockedUserRepository;

    public BlockedUser getBlockedUser(Long fromUserId, Long toUserId) { return getEntity(blockedUserRepository.findByFromUserIdAndToUserId(fromUserId, toUserId), BlockedUser.class, "bookmark not found"); }

    /** fromUserId에  */
    public List<BlockedUser> getToUserIdList(Long fromUserId) {return blockedUserRepository.findByFromUserId(fromUserId); }

    public List<BlockedUser> getFromUserIdList(Long toUserId) {return blockedUserRepository.findByToUserId(toUserId); }



}
