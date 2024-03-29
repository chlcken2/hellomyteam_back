package hellomyteam.hellomyteam.service;

import hellomyteam.hellomyteam.dto.*;
import hellomyteam.hellomyteam.entity.Board;
import hellomyteam.hellomyteam.entity.BoardCategory;
import hellomyteam.hellomyteam.entity.TeamMemberInfo;
import hellomyteam.hellomyteam.entity.status.BoardAndCommentStatus;
import hellomyteam.hellomyteam.repository.BoardRepository;
import hellomyteam.hellomyteam.repository.LikeRepository;
import hellomyteam.hellomyteam.repository.TeamMemberInfoRepository;
import hellomyteam.hellomyteam.repository.custom.impl.BoardCustomImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {
    private final static String VIEWCOOKIENAME = "alreadyViewCookie";
    private final BoardRepository boardRepository;
    private final BoardCustomImpl boardCustomImpl;
    private final TeamMemberInfoRepository teamMemberInfoRepository;
    private final LikeRepository likeRepository;

    private final EntityManager em;

    public CommonResponse<?> getBoards(Long teamId, int pageNum, int pageSize, BoardCategory category, String srchType, String srchKwd, String sortType){

        // 정렬 조건 Check
        if("created_date".equals(sortType)){
            sortType = "createdDate";
        }
        if("like_count".equals(sortType)){
            sortType = "likeCount";
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, sortType));

        //검색 타입 Check
        if("".equals(srchType) || srchType != null){
            Page<BoardListResDto> boards = boardRepository.srchResult(teamId, category, pageable, srchType, srchKwd);
            return CommonResponse.createSuccess(boards, "boardsList success");
        }else {
            Page<BoardListResDto> boards = boardRepository.getBoards(teamId, category, pageable);
            return CommonResponse.createSuccess(boards, "boardsList success");
        }
    }
    public CommonResponse createBoard(Long teamId, BoardWriteDto boardWriteDto) {
        TeamMemberInfo teamMemberInfo = teamMemberInfoRepository.findTeamMemberInfoById(boardWriteDto.getTeamMemberInfoId());
        if (!teamMemberInfo.getTeam().getId().equals(teamId)) {
            return CommonResponse.createError("입력한 teamid와 회원이 가입한 팀 id가 다릅니다.");
        }

        TeamMemberInfo findTeamMemberInfo = teamMemberInfoRepository.findById(boardWriteDto.getTeamMemberInfoId())
                .orElseThrow(()-> new IllegalStateException("teamMemberInfo id가 누락되었습니다."));

        Board board = Board.builder()
                .boardCategory(boardWriteDto.getBoardCategory())
                .writer(findTeamMemberInfo.getMember().getName())
                .title(boardWriteDto.getTitle())
                .contents(boardWriteDto.getContents())
                .boardStatus(BoardAndCommentStatus.NORMAL)
                .teamMemberInfo(findTeamMemberInfo)
                .team(findTeamMemberInfo.getTeam())
                .viewCount(0)
                .commentCount(0)
                .likeCount(0)
                .build();
        boardRepository.save(board);
        return CommonResponse.createSuccess(board, "저장되었습니다.");
    }

    public BoardDetailResDto getBoard(Long id) {
        BoardDetailResDto findBoard = boardCustomImpl.findBoardById(id);
        return findBoard;
    }

    public Board updateBoard(Long boardId, BoardUpdateDto boardUpdateDto) {
        Board findBoard = em.find(Board.class, boardId);
        findBoard.setBoardCategory(boardUpdateDto.getChangeBoardCategory());
        findBoard.setContents(boardUpdateDto.getChangeContents());
        findBoard.setTitle(boardUpdateDto.getChangeTitle());
        return findBoard;
    }

    public void deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
    }

    public int updateView(Long boardId, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        boolean checkCookie = false;
        int result = 0;
        if(cookies != null){
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals(VIEWCOOKIENAME+boardId)) checkCookie = true;

            }
            if(!checkCookie){
                Cookie newCookie = createCookieForForNotOverlap(boardId);
                response.addCookie(newCookie);
                result = boardCustomImpl.updateView(boardId);
            }
        } else {
            Cookie newCookie = createCookieForForNotOverlap(boardId);
            response.addCookie(newCookie);
            result = boardCustomImpl.updateView(boardId);
        }
        return result;
    }

    //TODO utils로 옮기기
    private Cookie createCookieForForNotOverlap(Long boardId) {
        Cookie cookie = new Cookie(VIEWCOOKIENAME+boardId, String.valueOf(boardId));
        cookie.setComment("조회수 중복 증가 방지 쿠키");	// 쿠키 용도 설명 기재
        cookie.setMaxAge(getRemainSecondForTommorow()); 	// 쿠키유지 : 하루
        cookie.setHttpOnly(true);				// 서버에서만 조작 가능
        return cookie;
    }
    // 다음 날 정각까지 남은 시간(초)
    private int getRemainSecondForTommorow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tommorow = LocalDateTime.now().plusDays(1L).truncatedTo(ChronoUnit.DAYS);
        return (int) now.until(tommorow, ChronoUnit.SECONDS);
    }

    public Board getBoardById(Long boardId) {
        Board board = boardRepository.getBoardById(boardId);
        return board;
    }
}
