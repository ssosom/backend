package com.sosom.websocket.interceptor;

import com.sosom.exception.CustomException;
import com.sosom.exception.ErrorCode;
import com.sosom.security.jwt.JwtTokenUtil;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.sosom.websocket.WebSokcetConst.EMAIL;

@Component
public class InBoundChannelInterceptor implements ChannelInterceptor {

    @Value("${jwt.token.secret}")
    private String secretKey;
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);


        if(accessor.getMessageType() == SimpMessageType.CONNECT || accessor.getMessageType() == SimpMessageType.MESSAGE){
            String jwtHeader = accessor.getFirstNativeHeader("Authorization");

            if(jwtHeader != null && jwtHeader.startsWith("Bearer ")){
                String token = jwtHeader.split(" ")[1];

                if(JwtTokenUtil.validateJwtToken(token,secretKey)){
                    return MessageBuilder.fromMessage(message)
                            .setHeader(EMAIL,JwtTokenUtil.getEmail(token,secretKey))
                            .build();
                }

            }else{
                throw new CustomException(ErrorCode.FAIL_AUTHORIZATION);
            }
        }

        return message;
    }
}
