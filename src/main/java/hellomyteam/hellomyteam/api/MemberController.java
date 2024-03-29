package hellomyteam.hellomyteam.api;

import hellomyteam.hellomyteam.entity.Member;
import hellomyteam.hellomyteam.exception.JwtTokenException;
import hellomyteam.hellomyteam.exception.MemberNotFoundException;
import hellomyteam.hellomyteam.dto.CommonResponse;
import hellomyteam.hellomyteam.repository.MemberRepository;
import hellomyteam.hellomyteam.service.MemberService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @ApiOperation(value = "user/me", notes = "Access Token 통한 본인 확인")
    @ApiImplicitParam(name = "Authorization", value = "Access Token(접두사: Bearer) 입력 필요")
    @ApiResponses({
            @ApiResponse(code=496, message="잘못된 JWT 서명")
            , @ApiResponse(code=497, message="지원되지 않는 JWT 토큰")
            , @ApiResponse(code=498, message="만료된 JWT 토큰")
            , @ApiResponse(code=499, message="JWT 토큰 형식 오류")
    })
    @GetMapping("/user/me")
    public CommonResponse<?> getMyMemberInfo(HttpServletResponse response) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member savedMember = memberRepository.findByEmail(email);

        if ("anonymousUser".equals(email)) {
            switch (response.getStatus()) {
                case 496: return CommonResponse.createError(new JwtTokenException("MalformedJwtException").getMessage());
                case 497: return CommonResponse.createError(new JwtTokenException("UnsupportedJwtException").getMessage());
                case 498: return CommonResponse.createError(new JwtTokenException("ExpiredJwtException").getMessage());
                case 499: return CommonResponse.createError(new JwtTokenException("Exception").getMessage());
            }
        } else if (ObjectUtils.isEmpty(savedMember)) {
            return CommonResponse.createError(new MemberNotFoundException(email, null).getMessage());
        }

        return CommonResponse.createSuccess(savedMember);
    }

    @ApiOperation(value = "로그인 후 가입한 팀 id와 팀 이름 가져오기", notes = "user/me를 통해 userId전달, 데이터 미존재시 - 미가입 페이지 리턴")
    @GetMapping("/user/teams/{memberid}")
    public CommonResponse<?> getAfterTeamsByMemberId(@PathVariable(value = "memberid") Long memberId) {
        return memberService.findJoinTeam(memberId);
    }
}
