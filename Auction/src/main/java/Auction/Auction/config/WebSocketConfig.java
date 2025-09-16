package Auction.Auction.config;

import Auction.Auction.security.JwtTokenProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Clients subscribe here
        config.setApplicationDestinationPrefixes("/app"); // Clients send messages here
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-auction")
                .setAllowedOrigins("*")
                .addInterceptors(new JwtHandshakeInterceptor(jwtTokenProvider))
                .withSockJS();
    }

    private static class JwtHandshakeInterceptor implements HandshakeInterceptor {

        private final JwtTokenProvider jwtTokenProvider;

        public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider) {
            this.jwtTokenProvider = jwtTokenProvider;
        }

        @Override
        public boolean beforeHandshake(ServerHttpRequest request,
                                       ServerHttpResponse response,
                                       WebSocketHandler wsHandler,
                                       Map<String, Object> attributes) {
            String query = request.getURI().getQuery();
            if (query != null && query.contains("access_token=")) {
                String token = query.split("access_token=")[1];
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getEmailFromToken(token);
                    attributes.put("username", username); // Store user info in WS session
                    return true;
                }
            }
            response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Exception exception) {
            // No-op
        }
    }
}
