package hellomyteam.hellomyteam.repository.custom.impl;

;
import hellomyteam.hellomyteam.dto.*;
import hellomyteam.hellomyteam.entity.status.BoardAndCommentStatus;
import hellomyteam.hellomyteam.util.QueryDslUtil;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import static hellomyteam.hellomyteam.entity.QBoard.board;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BoardCustomImpl {

    private final JPAQueryFactory queryFactory;

    public BoardDetailResDto findBoardById(Long id) {
        return queryFactory.select(new QBoardDetailResDto(
                        board.writer
                        , board.title
                        , board.boardStatus
                        , board.boardCategory
                        , board.contents
                        , board.viewCount
                        , board.likeCount
                        , board.createdDate))
                .from(board)
                .where(board.id.eq(id))
                .where(board.boardStatus.eq(BoardAndCommentStatus.NORMAL))
                .fetchOne();
    }

    public int updateView(Long boardId) {
        return (int) queryFactory.update(board)
                .set(board.viewCount, board.viewCount.add(1))
                .where(board.id.eq(boardId))
                .execute();
    }

    private List<OrderSpecifier> getAllOrderSpecifiers(Pageable pageable) {
        log.info("pageable: " + pageable);
        List<OrderSpecifier> orders = new ArrayList<>();

        if (!isEmpty(pageable.getSort())) {
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                switch (order.getProperty()) {
                    case "createdDate":
                        OrderSpecifier<?> orderCreatedDate = QueryDslUtil.getSortedColumn(direction, board, "createdDate");
                        orders.add(orderCreatedDate);
                        break;
                    case "viewCount":
                        OrderSpecifier<?> orderViewCount = QueryDslUtil.getSortedColumn(direction, board, "viewCount");
                        orders.add(orderViewCount);
                        break;
                    case "likeCount":
                        OrderSpecifier<?> orderLikeCount = QueryDslUtil.getSortedColumn(direction, board, "likeCount");
                        orders.add(orderLikeCount);
                        break;
                    default:
                        break;
                }
            }
        }
        return orders;
    }
}
