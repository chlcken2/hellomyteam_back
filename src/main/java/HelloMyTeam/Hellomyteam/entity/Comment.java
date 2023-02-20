package HelloMyTeam.Hellomyteam.entity;

import HelloMyTeam.Hellomyteam.entity.status.BoardAndCommentStatus;
import com.sun.istack.NotNull;
import lombok.Getter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @Column(name = "comment_id")
    private Long id;

    @NotNull
    private String contents;

    @NotNull
    private String writer;

    @Enumerated(EnumType.STRING)
    @NotNull
    private BoardAndCommentStatus commentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @OneToMany(mappedBy = "comment")
    private List<CommentReply> commentReply = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamMemberInfo_id")
    private TeamMemberInfo teamMemberInfo;
}